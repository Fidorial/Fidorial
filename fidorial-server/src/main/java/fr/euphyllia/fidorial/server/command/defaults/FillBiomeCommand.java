package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundChunksBiomesPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.command.argument.resolvers.BlockPosResolver;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.data.Biome;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.biome.BiomeRegistry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

public final class FillBiomeCommand {

    private static final String PERMISSION = "fidorial.command.fillbiome";

    private FillBiomeCommand() {
        throw new UnsupportedOperationException("FillBiomeCommand cannot be instantiated.");
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("fillbiome")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .then(argument("from", ArgumentTypes.blockPosition())
                        .then(argument("to", ArgumentTypes.blockPosition())
                                .then(argument("biome", ArgumentTypes.resource(RegistryKey.BIOME))
                                        .executes(context -> fill(context, null))
                                        .then(literal("replace")
                                                .then(argument("filter", ArgumentTypes.resource(RegistryKey.BIOME))
                                                        .executes(context -> fill(
                                                                context,
                                                                context.getArgument("filter", Biome.class).key())))))))
                .build();
    }

    private static int fill(
            final CommandContext<CommandSource> context,
            final @Nullable Key filter
    ) throws CommandSyntaxException {

        final FidorialServer server = FidorialServer.getInstance();
        final BiomeRegistry biomes = server.biomes();

        final Biome target = context.getArgument("biome", Biome.class);
        final Key biome = target.key();

        if (filter != null && !biomes.contains(filter)) {
            context.getSource().sendMessage(
                    Component.translatable("command.fillbiome.unknown", Component.text(filter.asString())));
            return 0;
        }

        final ServerWorld world = worldOf(context);
        if (world == null) {
            context.getSource().sendMessage(Component.translatable("command.fillbiome.no_world"));
            return 0;
        }
        final BlockPosition from = context.getArgument("from", BlockPosResolver.class).resolve(context.getSource());
        final BlockPosition to = context.getArgument("to", BlockPosResolver.class).resolve(context.getSource());

        final long volume = span(from.blockX(), to.blockX()) * span(from.blockY(), to.blockY()) * span(from.blockZ(), to.blockZ());
        final int limit = world.gameRuleValues().getInt(GameRuleKeys.MAX_BLOCK_MODIFICATIONS);
        if (volume > limit) {
            context.getSource().sendMessage(Component.translatable(
                    "command.fillbiome.toobig", Component.text(limit), Component.text(volume)));
            return 0;
        }

        final int minX = Math.min(from.blockX(), to.blockX());
        final int minZ = Math.min(from.blockZ(), to.blockZ());
        final int maxX = Math.max(from.blockX(), to.blockX());
        final int maxZ = Math.max(from.blockZ(), to.blockZ());

        final int floor = world.minY();
        final int ceiling = floor + world.height() - 1;
        final int minY = Math.max(floor, Math.min(from.blockY(), to.blockY()));
        final int maxY = Math.min(ceiling, Math.max(from.blockY(), to.blockY()));

        if (minY > maxY) {
            context.getSource().sendMessage(Component.translatable("command.fillbiome.outofworld"));
            return 0;
        }

        final Map<ChunkPos, List<int[]>> byChunk = new LinkedHashMap<>();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                final ChunkPos chunkPos = new ChunkPos(x >> 4, z >> 4);
                for (int y = minY; y <= maxY; y++) {
                    byChunk.computeIfAbsent(chunkPos, _ -> new ArrayList<>()).add(new int[]{x, y, z});
                }
            }
        }

        final AtomicInteger changed = new AtomicInteger();
        final Set<ChunkPos> touched = ConcurrentHashMap.newKeySet();
        final List<CompletableFuture<Void>> pending = new ArrayList<>(byChunk.size());

        for (final Map.Entry<ChunkPos, List<int[]>> entry : byChunk.entrySet()) {
            final ChunkPos chunkPos = entry.getKey();
            final List<int[]> points = entry.getValue();
            final CompletableFuture<Void> future = new CompletableFuture<>();
            pending.add(future);

            world.scheduler().execute(world.key(), chunkPos, () -> {
                try {
                    for (final int[] p : points) {
                        final int x = p[0];
                        final int y = p[1];
                        final int z = p[2];
                        if (filter != null && !filter.equals(world.getBiome(x, y, z))) {
                            continue;
                        }
                        if (world.setBiome(x, y, z, biome)) {
                            changed.incrementAndGet();
                            touched.add(chunkPos);
                        }
                    }
                    future.complete(null);
                } catch (final IOException failure) {
                    future.completeExceptionally(failure);
                }
            });
        }

        CompletableFuture.allOf(pending.toArray(new CompletableFuture[0])).whenComplete((_, failure) -> {
            if (failure != null) {
                context.getSource().sendMessage(Component.translatable(
                        "command.fillbiome.failed", Component.text(rootMessage(failure))));
                return;
            }

            resend(server, world, touched);

            context.getSource().sendMessage(Component.translatable(
                    "command.fillbiome.success",
                    Component.text(changed.get()),
                    Component.text(biome.asString())));
        });

        return Command.SINGLE_SUCCESS;
    }

    private static long span(final int a, final int b) {
        return Math.abs((long) (a & ~3) - (b & ~3)) + 1L;
    }

    private static String rootMessage(final Throwable failure) {
        Throwable cause = failure;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }

    private static void resend(final FidorialServer server, final ServerWorld world, final Set<ChunkPos> chunks) {
        if (chunks.isEmpty()) {
            return;
        }

        final List<ChunkColumn> columns = new ArrayList<>(chunks.size());
        for (final ChunkPos pos : chunks) {
            try {
                columns.add(world.getChunk(pos.x(), pos.z()));
            } catch (final IOException _) {
            }
        }

        if (columns.isEmpty()) {
            return;
        }

        final ClientboundChunksBiomesPacket packet =
                new ClientboundChunksBiomesPacket(server.chunkSerializer(), columns);

        for (final ServerPlayer player : server.players()) {
            if (!player.isRemoved() && player.world().equals(world)) {
                player.connection().send(packet);
            }
        }
    }

    private static @Nullable ServerWorld worldOf(final CommandContext<CommandSource> context) {
        final CommandSource source = context.getSource();
        return source.location() instanceof final Location location
                && location.world() instanceof final ServerWorld world ? world : null;
    }
}
