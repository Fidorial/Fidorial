package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer.RegionTpsSnapshot;
import fr.fidorial.command.CommandSource;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Locale;

import static fr.fidorial.command.Commands.literal;

public final class TpsCommand {

    private static final int MAX_LINES = 10;

    private TpsCommand() {
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("tps")
                .requires(source -> source.sender().hasPermission("fidorial.command.tps"))
                .executes(TpsCommand::execute)
                .build();
    }

    private static int execute(final CommandContext<CommandSource> context) {
        final List<RegionTpsSnapshot> snapshots =
                FidorialServer.getInstance().regionizer().tpsSnapshots();

        if (snapshots.isEmpty()) {
            context.getSource().sendMessage(Component.translatable("command.tps.noregion"));
            return 0;
        }

        double worstTps = Double.MAX_VALUE;
        double sumTps = 0;

        for (final RegionTpsSnapshot snapshot : snapshots) {
            worstTps = Math.min(worstTps, snapshot.tps());
            sumTps += snapshot.tps();
        }

        context.getSource()
                .sendMessage(Component.translatable(
                        "command.tps.summary",
                        Component.text(snapshots.size()),
                        Component.text(format1(worstTps)),
                        Component.text(format1(sumTps / snapshots.size()))));

        final int shown = Math.min(snapshots.size(), MAX_LINES);

        for (int i = 0; i < shown; i++) {
            final RegionTpsSnapshot snapshot = snapshots.get(i);

            context.getSource()
                    .sendMessage(Component.translatable(
                            "command.tps.line",
                            Component.text(snapshot.world().asString()),
                            Component.text(snapshot.sectionX()),
                            Component.text(snapshot.sectionZ()),
                            Component.text(snapshot.originChunkX()),
                            Component.text(snapshot.originChunkZ()),
                            Component.text(format1(snapshot.tps())),
                            Component.text(String.format(Locale.ROOT, "%.2f", snapshot.msptAvg())),
                            Component.text(format1(snapshot.cpuPercent())),
                            Component.text(snapshot.queuedTasks()),
                            Component.text(snapshot.tickets())));
        }

        if (snapshots.size() > shown) {
            context.getSource()
                    .sendMessage(Component.translatable("command.tps.more", Component.text(snapshots.size() - shown)));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static String format1(final double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
