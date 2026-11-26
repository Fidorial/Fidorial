package fr.euphyllia.fidorial.testplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.testplugin.TestPlugin;
import fr.fidorial.command.CommandSource;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.generation.NoiseTerrain;
import fr.fidorial.world.generation.NoiseTerrains;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;
import static fr.fidorial.command.argument.Arguments.arguments;

/**
 * Exercises the {@link NoiseTerrains} API on the world the sender stands in.
 */
public final class WorldgenCommand {

    private static final int DEFAULT_RADIUS = 3000;
    private static final int STEP = 32;
    private static final List<Key> FUNCTIONS = List.of(
            Key.key("overworld/continents"), Key.key("overworld/erosion"),
            Key.key("overworld/ridges_folded"), Key.key("overworld/depth"));

    private final TestPlugin plugin;

    public WorldgenCommand(final TestPlugin plugin) {
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSource> create() {
        return literal("worldgen")
                .then(literal("info").executes(this::info))
                .then(literal("land").executes(this::land))
                .then(literal("locate")
                        .then(argument("biome", arguments().resource(RegistryKey.BIOME))
                                .executes(ctx -> locate(ctx, ctx.getArgument("biome", BiomeDefinition.class).key(), DEFAULT_RADIUS))
                                .then(argument("radius", IntegerArgumentType.integer(16, 30000))
                                        .executes(ctx -> locate(
                                                ctx,
                                                ctx.getArgument("biome", BiomeDefinition.class).key(),
                                                IntegerArgumentType.getInteger(ctx, "radius"))))))
                .build();
    }

    private int info(final CommandContext<CommandSource> ctx) {
        final CommandSource source = ctx.getSource();

        final Location location = source.location();
        final NoiseTerrain terrain = terrain(source, location);
        if (terrain == null || location == null) {
            return 0;
        }

        final int x = (int) Math.floor(location.x());
        final int y = (int) Math.floor(location.y());
        final int z = (int) Math.floor(location.z());

        plugin.msg(source, "<gold>Generation en <white>%d, %d<gold> (graine <white>%d<gold>, reglages <white>%s<gold>)"
                .formatted(x, z, terrain.seed(), terrain.settings().asString()));
        plugin.msg(source, "<gray> biome here <white>%s<gray>, sol a <white>y=%d<gray>, sea level <white>%d"
                .formatted(terrain.biomeAt(x, y, z).key().asString(), terrain.surfaceHeight(x, z), terrain.seaLevel()));
        for (final Key function : FUNCTIONS) {
            try {
                plugin.msg(source, "<gray>  %s <white>%.4f".formatted(function.asString(), terrain.density(function, x, y, z)));
            } catch (final IllegalArgumentException unknown) {
                plugin.msg(source, "<gray>  %s <red>inconnue".formatted(function));
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private int land(final CommandContext<CommandSource> ctx) {
        final CommandSource source = ctx.getSource();
        final Location location = source.location();
        final NoiseTerrain terrain = terrain(source, location);
        if (terrain == null) {
            return 0;
        }

        for (int distance = 0; distance <= DEFAULT_RADIUS; distance += STEP) {
            final int samples = Math.max(8, distance / 4);
            for (int i = 0; i < samples; i++) {
                final double angle = i * 2.0 * Math.PI / samples;
                final int x = (int) (Math.cos(angle) * distance);
                final int z = (int) (Math.sin(angle) * distance);
                final int ground = terrain.surfaceHeight(x, z);
                if (ground > terrain.seaLevel()) {
                    plugin.msg(source, "<green>Land closest to the origin : <white>%d, %d, %d<green> (%s)"
                            .formatted(x, ground, z, terrain.biomeAt(x, ground, z).key().asString()));
                    return Command.SINGLE_SUCCESS;
                }
            }
        }
        plugin.msg(source, "<red>No land is emerged within %d blocks of the origin.".formatted(DEFAULT_RADIUS));
        return 0;
    }

    private int locate(final CommandContext<CommandSource> ctx, final TypedKey<BiomeDefinition> biome, final int radius) {
        final CommandSource source = ctx.getSource();
        final Location location = source.location();
        final NoiseTerrain terrain = terrain(source, location);
        if (terrain == null || location == null) {
            return 0;
        }
        final int centreX = (int) Math.floor(location.x());
        final int centreZ = (int) Math.floor(location.z());

        // spiral search: the closest rings come first, so the first match is the closest at this resolution
        for (int distance = 0; distance <= radius; distance += STEP) {
            final int samples = Math.max(8, distance / 4);
            for (int i = 0; i < samples; i++) {
                final double angle = i * 2.0 * Math.PI / samples;
                final int x = centreX + (int) (Math.cos(angle) * distance);
                final int z = centreZ + (int) (Math.sin(angle) * distance);
                final int ground = terrain.surfaceHeight(x, z);
                if (!terrain.biomeAt(x, ground, z).equals(biome)) {
                    continue;
                }
                plugin.msg(source, "<green>%s found at <white>%d, %d<green> (%d blocks, sol y=%d)"
                        .formatted(biome.key().asString(), x, z, distance, ground));
                return Command.SINGLE_SUCCESS;
            }
        }

        plugin.msg(source, "<red>%s not found within a radius of %d blocks.".formatted(biome.key().asString(), radius));
        return 0;
    }

    private @Nullable NoiseTerrain terrain(final CommandSource source, final @Nullable Location location) {
        if (location == null) {
            plugin.msg(source, "<red>No world loaded.");
            return null;
        }
        final NoiseTerrains terrains = plugin.server().services().find(NoiseTerrains.class).orElse(null);
        if (terrains == null) {
            plugin.msg(source, "<red>The grounds service is not available.");
            return null;
        }
        final NoiseTerrain terrain = terrains.of(location.world()).orElse(null);
        if (terrain == null) {
            plugin.msg(source, "<red>This world is not generated from noise.");
        }
        return terrain;
    }
}
