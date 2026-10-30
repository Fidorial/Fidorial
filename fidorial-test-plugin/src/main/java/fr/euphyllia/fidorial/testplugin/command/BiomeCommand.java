package fr.euphyllia.fidorial.testplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.testplugin.TestPlugin;
import fr.euphyllia.fidorial.testplugin.terrain.TestBiomes;
import fr.fidorial.command.CommandSource;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.WritableRegistry;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.environment.Attribute;
import fr.fidorial.world.environment.EnvironmentAttributes;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;
import static fr.fidorial.command.argument.Arguments.arguments;

public final class BiomeCommand {

    private static final String SAMPLE_JSON = """
            {
              "has_precipitation": true,
              "temperature": 0.35,
              "downfall": 0.8,
              "effects": {
                "water_color": "#4a6bd6",
                "grass_color": "#79c05a"
              },
              "attributes": {
                "minecraft:visual/sky_color": "#7ba4ff",
                "minecraft:visual/water_fog_end_distance": {
                  "modifier": "multiply",
                  "argument": 0.6
                },
                "minecraft:gameplay/can_pillager_patrol_spawn": false
              }
            }
            """;

    private static TestPlugin plugin;

    public BiomeCommand(final TestPlugin plugin) {
        BiomeCommand.plugin = plugin;
    }

    public LiteralCommandNode<CommandSource> create() {
        return literal("testbiome")
                .then(literal("list").executes(BiomeCommand::list))
                .then(literal("count").executes(BiomeCommand::count))
                .then(literal("json").executes(BiomeCommand::fromJson))
                .then(literal("info")
                        .then(argument("key", arguments().key())
                                .executes(ctx -> info(ctx, ctx.getArgument("key", Key.class)))))
                .then(literal("register")
                        .then(argument("key", arguments().key())
                                .executes(ctx -> register(ctx, ctx.getArgument("key", Key.class)))))
                .then(literal("unregister")
                        .then(argument("key", arguments().key())
                                .executes(ctx -> unregister(ctx, ctx.getArgument("key", Key.class)))))
                .build();
    }

    private static WritableRegistry<BiomeDefinition> biomes() {
        return plugin.server().registries().writable(RegistryKey.BIOME);
    }

    private static TypedKey<BiomeDefinition> typed(final Key key) {
        return TypedKey.create(RegistryKey.BIOME, key);
    }

    private static int list(final CommandContext<CommandSource> ctx) {
        final CommandSource source = ctx.getSource();
        final WritableRegistry<BiomeDefinition> biomes = biomes();
        final List<TypedKey<BiomeDefinition>> custom = biomes.keys().stream().filter(biomes::isCustom).toList();

        if (custom.isEmpty()) {
            plugin.msg(source, "<yellow>No biome is added or replaced by a plugin.");
            return Command.SINGLE_SUCCESS;
        }

        plugin.msg(source, "<gold>Biomes added or replaced by plugins:");
        for (final TypedKey<BiomeDefinition> biome : custom) {
            plugin.msg(source, "<gray> - <white>%s".formatted(biome.key().asString()));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int count(final CommandContext<CommandSource> ctx) {
        final WritableRegistry<BiomeDefinition> biomes = biomes();
        plugin.msg(ctx.getSource(), "<gold>%d biomes registered, <aqua>%d<gold> of them from plugins."
                .formatted(biomes.size(), biomes.keys().stream().filter(biomes::isCustom).count()));
        return Command.SINGLE_SUCCESS;
    }

    private static int info(final CommandContext<CommandSource> ctx, final Key key) {
        final CommandSource source = ctx.getSource();
        final WritableRegistry<BiomeDefinition> biomes = biomes();
        final Optional<BiomeDefinition> biome = biomes.find(typed(key));

        if (biome.isEmpty()) {
            plugin.msg(source, "<red>Unknown biome: " + key.asString());
            return 0;
        }

        plugin.msg(source, "<gold>%s <gray>- custom: <white>%s".formatted(key.asString(), biomes.isCustom(typed(key))));
        describe(source, biome.get());
        return Command.SINGLE_SUCCESS;
    }

    private static void describe(final CommandSource source, final BiomeDefinition biome) {
        plugin.msg(source, "<gray>  temperature <white>%s<gray> (%s), downfall <white>%s<gray>, precipitation <white>%s"
                .formatted(biome.temperature(), biome.temperatureModifier().id(),
                        biome.downfall(), biome.hasPrecipitation()));

        plugin.msg(source, "<gray>  water <white>#%06x<gray>, grass <white>%s<gray>, modifier <white>%s"
                .formatted(biome.effects().waterColor(),
                        hex(biome.effects().grassColor()),
                        biome.effects().grassColorModifier().id()));

        final EnvironmentAttributes attributes = biome.attributes();
        if (attributes.isEmpty()) {
            plugin.msg(source, "<gray>  no environment attribute");
            return;
        }

        plugin.msg(source, "<gray>  sky <white>%s<gray>, fog <white>%s<gray>, water fog <white>%s"
                .formatted(attribute(attributes.skyColor()),
                        attribute(attributes.fogColor()),
                        attribute(attributes.waterFogColor())));

        if (!attributes.ambientParticles().isEmpty()) {
            plugin.msg(source, "<gray>  particles <white>%s".formatted(
                    attributes.ambientParticles().stream()
                            .map(particle -> particle.type().asString() + " @ " + particle.probability())
                            .collect(Collectors.joining(", "))));
        }

        if (attributes.ambientSounds() != null) {
            plugin.msg(source, "<gray>  ambient sounds present");
        }

        final Attribute<Float> distance = attributes.waterFogEndDistance();
        if (distance != null) {
            plugin.msg(source, "<gray>  water fog distance <white>%s<gray> via <white>%s"
                    .formatted(distance.value(), distance.modifier().id()));
        }
    }

    private static int register(final CommandContext<CommandSource> ctx, final Key key) {
        final CommandSource source = ctx.getSource();
        final WritableRegistry<BiomeDefinition> biomes = biomes();

        if (biomes.contains(typed(key))) {
            plugin.msg(source, "<red>" + key.asString() + " already exists.");
            return 0;
        }

        final BiomeDefinition clone = BiomeDefinition.builder(TestBiomes.VOLCANIC_PLAINS).key(key).build();
        biomes.register(clone, plugin);

        plugin.msg(source, "<green>%s registered. Reconnect to see it.".formatted(key.asString()));
        return Command.SINGLE_SUCCESS;
    }

    private static int unregister(final CommandContext<CommandSource> ctx, final Key key) {
        final CommandSource source = ctx.getSource();

        if (!biomes().unregister(typed(key))) {
            plugin.msg(source, "<red>No biome registered by a plugin under " + key.asString());
            return 0;
        }

        plugin.msg(source, "<green>%s removed, or restored to vanilla.".formatted(key.asString()));
        return Command.SINGLE_SUCCESS;
    }

    private static int fromJson(final CommandContext<CommandSource> ctx) {
        final CommandSource source = ctx.getSource();
        final Key key = Key.key("fidorial", "datapack_sample");
        final WritableRegistry<BiomeDefinition> biomes = biomes();

        biomes.unregister(typed(key));

        try {
            final BiomeDefinition biome = biomes.registerFromJson(typed(key), SAMPLE_JSON, plugin);
            plugin.msg(source, "<green>JSON read: %s, temperature %s, sky %s"
                    .formatted(biome.key().asString(), biome.temperature(),
                            attribute(biome.attributes().skyColor())));
        } catch (final IllegalArgumentException invalid) {
            plugin.msg(source, "<red>JSON rejected: " + invalid.getMessage());
            return 0;
        }

        return Command.SINGLE_SUCCESS;
    }

    private static String attribute(final @Nullable Attribute<Integer> attribute) {
        return attribute == null ? "inherited" : "#%06x".formatted(attribute.value());
    }

    private static String hex(final @Nullable Integer color) {
        return color == null ? "computed" : "#%06x".formatted(color);
    }
}
