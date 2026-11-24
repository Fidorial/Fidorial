package fr.euphyllia.fidorial.server.command.fidorial.utils;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer.RegionTpsSnapshot;
import fr.fidorial.entity.player.Player;
import fr.fidorial.math.Location;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The boss bars of {@code /tpsbar}: one per player, refreshed every second with the TPS, MSPT and
 * thread load of the region the player stands in, their ping and the memory of the server. The
 * MSPT is split in two: the ticks alone, as spark measures them, and the tasks the region ran
 * between them, which {@code /tps} counts in its MSPT.
 *
 * <p>The bars are refreshed on a thread of their own rather than on the region threads, so they keep
 * moving while the region they describe is lagging. The refresh only runs while a bar is enabled.</p>
 *
 * <p>A bar stays enabled across reconnections until it is turned off or the server stops.</p>
 */
public final class TpsBarManager {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(TpsBarManager.class);

    private static final long REFRESH_INTERVAL_MS = 1000L;

    /** The time budget of a tick at 20 TPS, in milliseconds. */
    private static final double TICK_BUDGET_MS = 50.0;

    private static final double BYTES_PER_GB = 1024.0 * 1024.0 * 1024.0;

    private static final String UNAVAILABLE = "--";

    private final FidorialServer server;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
            r -> Thread.ofPlatform().name("fidorial-tpsbar").daemon().unstarted(r));

    // guarded by this: showing and hiding a bar must not interleave with a refresh, or a bar turned
    // off during a refresh would be shown again
    private final Map<UUID, BossBar> bars = new HashMap<>();
    private @Nullable ScheduledFuture<?> refreshTask;

    public TpsBarManager(final FidorialServer server) {
        this.server = server;
    }

    /**
     * Turns the bar of a player on or off.
     *
     * @param player the player
     * @return {@code true} if the bar is now shown, {@code false} if it is now hidden
     */
    public synchronized boolean toggle(final Player player) {
        final BossBar removed = bars.remove(player.uuid());
        if (removed != null) {
            player.hideBossBar(removed);
            if (bars.isEmpty()) {
                stopRefreshing();
            }
            return false;
        }

        final BossBar bar = BossBar.bossBar(
                Component.empty(), 0.0F, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);
        bars.put(player.uuid(), bar);
        // fill the bar before showing it, so it never appears empty
        if (player instanceof final ServerPlayer serverPlayer) {
            refresh(serverPlayer, bar, Memory.now());
        }
        player.showBossBar(bar);
        startRefreshing();
        return true;
    }

    /**
     * Stops the refresh for good; called when the server stops.
     */
    public void shutdown() {
        synchronized (this) {
            stopRefreshing();
            bars.clear();
        }
        executor.shutdownNow();
    }

    private void startRefreshing() {
        if (refreshTask == null && !executor.isShutdown()) {
            refreshTask = executor.scheduleAtFixedRate(
                    this::refreshAll, REFRESH_INTERVAL_MS, REFRESH_INTERVAL_MS, TimeUnit.MILLISECONDS);
        }
    }

    private void stopRefreshing() {
        if (refreshTask != null) {
            refreshTask.cancel(false);
            refreshTask = null;
        }
    }

    private synchronized void refreshAll() {
        // an exception would cancel the scheduled task, and every bar would freeze
        try {
            if (bars.isEmpty()) {
                return;
            }
            final Memory memory = Memory.now();
            for (final ServerPlayer player : server.players().online()) {
                final BossBar bar = bars.get(player.uuid());
                if (bar == null || player.isRemoved()) {
                    continue;
                }
                refresh(player, bar, memory);
                // shows the bar again after a reconnection, does nothing otherwise
                player.showBossBar(bar);
            }
        } catch (final Throwable t) {
            LOGGER.error("Failed to refresh the TPS bars", t);
        }
    }

    private void refresh(final ServerPlayer player, final BossBar bar, final Memory memory) {
        final Location location = player.location();
        final RegionTpsSnapshot region = server.workers().regionizer()
                .snapshotAt(location.world().key(), location.chunk());

        final Component tps;
        final Component mspt;
        final Component tasks;
        final Component thread;
        final float progress;
        final BossBar.Color color;
        if (region == null) {
            // the region was just created and has not ticked enough to be measured
            tps = Component.text(UNAVAILABLE, NamedTextColor.GRAY);
            mspt = Component.text(UNAVAILABLE, NamedTextColor.GRAY);
            tasks = Component.text(UNAVAILABLE, NamedTextColor.GRAY);
            thread = Component.text(UNAVAILABLE, NamedTextColor.GRAY);
            progress = 0.0F;
            color = BossBar.Color.WHITE;
        } else {
            final Level tpsLevel = Level.ofTps(region.tps());
            final Level msptLevel = Level.ofMspt(region.msptAvg());
            // the ticks alone, then the tasks the region ran between them: both share the thread
            final double tickMspt = region.tickMsptAvg();
            final double tasksMspt = Math.max(0.0, region.msptAvg() - tickMspt);
            tps = Component.text(format1(region.tps()), tpsLevel.color());
            mspt = Component.text(format2(tickMspt), Level.ofMspt(tickMspt).color());
            tasks = Component.text(format2(tasksMspt), Level.ofMspt(tasksMspt).color());
            thread = Component.text(format1(region.cpuPercent()),
                    Level.ofThreadPercent(region.cpuPercent()).color());
            // the bar fills up with the share of the tick budget used, tasks included
            progress = (float) Math.clamp(region.msptAvg() / TICK_BUDGET_MS, 0.0, 1.0);
            color = Level.worst(tpsLevel, msptLevel).barColor();
        }

        final int ping = player.ping();
        final Component title = Component.translatable(
                "command.tpsbar.title",
                tps,
                mspt,
                Component.text(ping, Level.ofPing(ping).color()),
                Component.text(format1(memory.usedGb()) + "/" + format1(memory.maxGb()),
                        Level.ofMemoryPercent(memory.usedPercent()).color()),
                thread,
                tasks);

        // boss bar names are sent as they are, so translate them for the player here
        bar.name(GlobalTranslator.render(title, player.locale()));
        bar.progress(progress);
        bar.color(color);
    }

    private static String format1(final double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String format2(final double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    /**
     * How healthy a value is, from its thresholds.
     */
    private enum Level {
        GOOD,
        WARNING,
        CRITICAL;

        NamedTextColor color() {
            return switch (this) {
                case GOOD -> NamedTextColor.GREEN;
                case WARNING -> NamedTextColor.YELLOW;
                case CRITICAL -> NamedTextColor.RED;
            };
        }

        BossBar.Color barColor() {
            return switch (this) {
                case GOOD -> BossBar.Color.GREEN;
                case WARNING -> BossBar.Color.YELLOW;
                case CRITICAL -> BossBar.Color.RED;
            };
        }

        static Level worst(final Level a, final Level b) {
            return a.compareTo(b) >= 0 ? a : b;
        }

        static Level ofTps(final double tps) {
            return tps >= 18.0 ? GOOD : tps >= 15.0 ? WARNING : CRITICAL;
        }

        static Level ofMspt(final double mspt) {
            return mspt < 40.0 ? GOOD : mspt < TICK_BUDGET_MS ? WARNING : CRITICAL;
        }

        static Level ofPing(final int ping) {
            return ping < 100 ? GOOD : ping < 250 ? WARNING : CRITICAL;
        }

        static Level ofMemoryPercent(final double percent) {
            return percent < 70.0 ? GOOD : percent < 85.0 ? WARNING : CRITICAL;
        }

        static Level ofThreadPercent(final double percent) {
            return percent < 60.0 ? GOOD : percent < 85.0 ? WARNING : CRITICAL;
        }
    }

    /**
     * The heap in use and the most it may grow to, read once per refresh for every bar.
     */
    private record Memory(long used, long max) {

        static Memory now() {
            final Runtime runtime = Runtime.getRuntime();
            final long total = runtime.totalMemory();
            final long max = runtime.maxMemory();
            // maxMemory() is Long.MAX_VALUE when the heap has no limit
            return new Memory(total - runtime.freeMemory(), max == Long.MAX_VALUE ? total : max);
        }

        double usedGb() {
            return used / BYTES_PER_GB;
        }

        double maxGb() {
            return max / BYTES_PER_GB;
        }

        double usedPercent() {
            return max <= 0L ? 0.0 : used * 100.0 / max;
        }
    }
}
