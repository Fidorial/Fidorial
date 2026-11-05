package fr.euphyllia.fidorial.server.schedulers.tick;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundTickingStatePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundTickingStepPacket;
import fr.euphyllia.fidorial.server.schedulers.DayNightThread;
import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.world.weather.WeatherEngine;

import java.util.List;
import java.util.function.Consumer;

/**
 * Server-wide entry point for {@code /tick}: forwards each change to every region and to the
 * global tickers, which apply it on their own threads, and keeps clients in sync.
 */
public final class ServerTickRates {

    private final ThreadedRegionRegionizer regionizer;
    private final DayNightThread dayNight;
    private final WeatherEngine weather;

    /**
     * When the current step is expected to end, in {@link System#nanoTime()}; regions step on
     * their own, so this is an estimate at the current rate.
     */
    private volatile long stepEndsAt;

    public ServerTickRates(final ThreadedRegionRegionizer regionizer, final DayNightThread dayNight, final WeatherEngine weather) {
        this.regionizer = regionizer;
        this.dayNight = dayNight;
        this.weather = weather;
    }

    public float rate() {
        return regionizer.tickDefaults().rate();
    }

    public boolean isFrozen() {
        return regionizer.tickDefaults().frozen();
    }

    public long nanosPerTick() {
        return new TickState(regionizer.tickDefaults()).nanosPerTick();
    }

    public boolean isStepping() {
        return remainingSteps() > 0;
    }

    public void setRate(final float rate) {
        change(new TickChange.SetRate(rate));
        broadcast(statePacket());
    }

    public void setFrozen(final boolean frozen) {
        if (frozen) {
            stepEndsAt = 0L;
        }
        change(new TickChange.SetFrozen(frozen));
        broadcast(statePacket());
    }

    /**
     * Runs {@code ticks} game ticks while frozen.
     *
     * @return {@code false} when the game is not frozen
     */
    public boolean step(final int ticks) {
        if (!isFrozen()) {
            return false;
        }
        stepEndsAt = System.nanoTime() + ticks * nanosPerTick();
        change(new TickChange.Step(ticks));
        broadcast(new ClientboundTickingStepPacket(ticks));
        return true;
    }

    /**
     * @return {@code false} when no step was in progress
     */
    public boolean stopStepping() {
        if (!isStepping()) {
            return false;
        }
        stepEndsAt = 0L;
        change(new TickChange.StopStep());
        broadcast(new ClientboundTickingStepPacket(0));
        return true;
    }

    /**
     * Sends the current state to a joining player.
     */
    public void syncTo(final Consumer<ClientboundPacket<?>> sender) {
        sender.accept(statePacket());
        sender.accept(new ClientboundTickingStepPacket(remainingSteps()));
    }

    private int remainingSteps() {
        final long left = stepEndsAt - System.nanoTime();
        return left <= 0L ? 0 : (int) Math.ceilDiv(left, nanosPerTick());
    }

    private void change(final TickChange change) {
        regionizer.changeTicking(change);
        dayNight.changeTicking(change);
        weather.changeTicking(change);
    }

    private ClientboundTickingStatePacket statePacket() {
        return new ClientboundTickingStatePacket(rate(), isFrozen());
    }

    private static void broadcast(final ClientboundPacket<?> packet) {
        final List<ServerPlayer> players = FidorialServer.getInstance().players().online();
        for (final ServerPlayer player : players) {
            if (player.connection().isInPlayState()) {
                player.connection().send(packet);
            }
        }
    }
}
