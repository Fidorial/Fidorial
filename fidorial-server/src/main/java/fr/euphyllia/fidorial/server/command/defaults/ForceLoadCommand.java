package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.command.brigadier.argument.location.ColumnPosArgument;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.command.CommandSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.world.ChunkPos;
import net.kyori.adventure.text.Component;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

public final class ForceLoadCommand {

    private static final String PERMISSION = "fidorial.command.forceload";
    private static final int MAX_CHUNKS = 256;
    private static final int WORLD_LIMIT = 30_000_000;

    private ForceLoadCommand() {
        throw new UnsupportedOperationException("ForceLoadCommand cannot be instantiated.");
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("forceload")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .then(literal("add")
                        .then(argument("from", ColumnPosArgument.columnPos())
                                .executes(context -> change(context, "from", true))
                                .then(argument("to", ColumnPosArgument.columnPos())
                                        .executes(context -> change(context, "to", true)))))
                .then(literal("remove")
                        .then(literal("all")
                                .executes(ForceLoadCommand::removeAll))
                        .then(argument("from", ColumnPosArgument.columnPos())
                                .executes(context -> change(context, "from", false))
                                .then(argument("to", ColumnPosArgument.columnPos())
                                        .executes(context -> change(context, "to", false)))))
                .then(literal("query")
                        .executes(ForceLoadCommand::list)
                        .then(argument("pos", ColumnPosArgument.columnPos())
                                .executes(ForceLoadCommand::query)))
                .build();
    }

    private static int change(final CommandContext<CommandSource> context, final String toArgument, final boolean add) {
        final CommandSource source = context.getSource();
        final ColumnPosArgument.Column from = column(context, "from");
        final ColumnPosArgument.Column to = column(context, toArgument);

        final int minX = Math.min(from.x(), to.x());
        final int minZ = Math.min(from.z(), to.z());
        final int maxX = Math.max(from.x(), to.x());
        final int maxZ = Math.max(from.z(), to.z());
        if (minX < -WORLD_LIMIT || minZ < -WORLD_LIMIT || maxX >= WORLD_LIMIT || maxZ >= WORLD_LIMIT) {
            source.sender().sendMessage(Component.translatable("argument.pos.outofworld"));
            return 0;
        }

        final int minChunkX = minX >> 4;
        final int minChunkZ = minZ >> 4;
        final int maxChunkX = maxX >> 4;
        final int maxChunkZ = maxZ >> 4;
        final long count = ((long) (maxChunkX - minChunkX) + 1L) * ((long) (maxChunkZ - minChunkZ) + 1L);
        if (count > MAX_CHUNKS) {
            source.sender().sendMessage(Component.translatable(
                    "commands.forceload.toobig", Component.text(MAX_CHUNKS), Component.text(count)));
            return 0;
        }

        final ServerWorld world = worldOf(source);
        int changed = 0;
        int firstX = 0;
        int firstZ = 0;
        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                if (world.setChunkForceLoaded(x, z, add) && ++changed == 1) {
                    firstX = x;
                    firstZ = z;
                }
            }
        }

        final String action = add ? "added" : "removed";
        final Component dimension = dimension(world);
        if (changed == 0) {
            source.sender().sendMessage(Component.translatable("commands.forceload." + action + ".failure"));
            return 0;
        }

        if (changed == 1) {
            source.sender().sendMessage(Component.translatable(
                    "commands.forceload." + action + ".single", chunk(firstX, firstZ), dimension));
        } else {
            source.sender().sendMessage(Component.translatable(
                    "commands.forceload." + action + ".multiple",
                    Component.text(changed),
                    dimension,
                    chunk(minChunkX, minChunkZ),
                    chunk(maxChunkX, maxChunkZ)));
        }
        return changed;
    }

    private static int removeAll(final CommandContext<CommandSource> context) {
        final ServerWorld world = worldOf(context.getSource());
        for (final ChunkPos pos : world.forceLoadedChunks()) {
            world.setChunkForceLoaded(pos.x(), pos.z(), false);
        }
        context.getSource().sender().sendMessage(
                Component.translatable("commands.forceload.removed.all", dimension(world)));
        return Command.SINGLE_SUCCESS;
    }

    private static int list(final CommandContext<CommandSource> context) {
        final ServerWorld world = worldOf(context.getSource());
        final Component dimension = dimension(world);
        final Set<ChunkPos> forced = world.forceLoadedChunks();

        if (forced.isEmpty()) {
            context.getSource().sender().sendMessage(
                    Component.translatable("commands.forceload.added.none", dimension));
            return 0;
        }

        final String joined = forced.stream()
                .sorted(Comparator.comparingInt(ChunkPos::x).thenComparingInt(ChunkPos::z))
                .map(pos -> format(pos.x(), pos.z()))
                .collect(Collectors.joining(", "));

        context.getSource().sender().sendMessage(forced.size() == 1
                ? Component.translatable("commands.forceload.list.single", dimension, Component.text(joined))
                : Component.translatable(
                "commands.forceload.list.multiple", Component.text(forced.size()), dimension, Component.text(joined)));
        return forced.size();
    }

    private static int query(final CommandContext<CommandSource> context) {
        final ServerWorld world = worldOf(context.getSource());
        final ChunkPos pos = column(context, "pos").chunk();
        final boolean forced = world.isChunkForceLoaded(pos);

        context.getSource().sender().sendMessage(Component.translatable(
                forced ? "commands.forceload.query.success" : "commands.forceload.query.failure",
                chunk(pos.x(), pos.z()),
                dimension(world)));
        return forced ? Command.SINGLE_SUCCESS : 0;
    }

    private static ColumnPosArgument.Column column(final CommandContext<CommandSource> context, final String name) {
        return context.getArgument(name, ColumnPosArgument.Resolver.class).resolve(context.getSource());
    }

    private static Component chunk(final int x, final int z) {
        return Component.text(format(x, z));
    }

    private static String format(final int x, final int z) {
        return "[" + x + ", " + z + "]";
    }

    private static Component dimension(final ServerWorld world) {
        return Component.text(world.key().asString());
    }

    private static ServerWorld worldOf(final CommandSource source) {
        if (source.sender() instanceof final ServerPlayer player && player.world() instanceof final ServerWorld world) {
            return world;
        }
        final Entity executor = source.executor();
        if (executor != null && executor.world() instanceof final ServerWorld world) {
            return world;
        }
        return FidorialServer.getInstance().worldManager().overworld();
    }
}
