package fr.euphyllia.fidorial.testplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.testplugin.TestPlugin;
import fr.euphyllia.fidorial.testplugin.worldgen.OverworldGenerator;
import fr.euphyllia.fidorial.testplugin.worldgen.climate.ClimatePoint;
import fr.fidorial.command.CommandSender;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.entity.Player;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.data.Biome;
import net.kyori.adventure.key.Key;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

public final class WorldgenCommand {

    private static final int DEFAULT_RADIUS = 3000;
    private static final int STEP = 32;

    private final TestPlugin plugin;

    public WorldgenCommand(final TestPlugin plugin) {
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSource> create() {
        return literal("worldgen")
                .then(literal("info").executes(this::info))
                .then(literal("spawn").executes(this::spawn))
                .then(literal("locate")
                        .then(argument("biome", ArgumentTypes.resource(RegistryKey.BIOME))
                                .executes(ctx -> locate(ctx, ctx.getArgument("biome", Biome.class).key(), DEFAULT_RADIUS))
                                .then(argument("radius", IntegerArgumentType.integer(16, 30000))
                                        .executes(ctx -> locate(
                                                ctx,
                                                ctx.getArgument("biome", Biome.class).key(),
                                                IntegerArgumentType.getInteger(ctx, "radius"))))))
                .build();
    }

    private int info(final CommandContext<CommandSource> ctx) {
        final CommandSender sender = ctx.getSource().sender();
        final OverworldGenerator generator = generator(sender);
        if (generator == null) {
            return 0;
        }
        if (!(sender instanceof final Player player)) {
            plugin.msg(sender, "<red>Cette sous-commande doit etre lancee en jeu.");
            return 0;
        }

        final Location location = player.location();
        final int x = (int) Math.floor(location.x());
        final int z = (int) Math.floor(location.z());

        final ClimatePoint point = generator.climateAt(x, z);
        final int ground = generator.surfaceAt(x, z, -64, 319);

        plugin.msg(sender, "<gold>Generation en <white>%d, %d<gold> (graine <white>%d<gold>)"
                .formatted(x, z, generator.settings().seed()));
        plugin.msg(sender, "<gray>  biome <white>%s<gray>, sol a <white>y=%d"
                .formatted(generator.biomeAt(x, z).asString(), ground));
        plugin.msg(sender, "<gray>  temperature <white>%.3f<gray> (niveau %d), humidite <white>%.3f<gray> (niveau %d)"
                .formatted(point.temperature(), point.temperatureLevel(), point.humidity(), point.humidityLevel()));
        plugin.msg(sender, "<gray>  continentalite <white>%.3f<gray> (%s), erosion <white>%.3f<gray> (niveau %d)"
                .formatted(point.continentalness(), continentName(point.continentLevel()),
                        point.erosion(), point.erosionLevel()));
        plugin.msg(sender, "<gray>  weirdness <white>%.3f<gray>, pics/vallees <white>%.3f<gray> (%s)"
                .formatted(point.weirdness(), point.peaksValleys(), peaksName(point.peaksValleysLevel())));
        return Command.SINGLE_SUCCESS;
    }

    private int spawn(final CommandContext<CommandSource> ctx) {
        final CommandSender sender = ctx.getSource().sender();
        final OverworldGenerator generator = generator(sender);
        if (generator == null) {
            return 0;
        }

        final int[] spawn = generator.findSpawn(0, 0, 4000, -64, 319);
        if (spawn == null) {
            plugin.msg(sender, "<red>Aucune terre emergee trouvee a moins de 4000 blocs de l'origine.");
            return 0;
        }
        plugin.msg(sender, "<green>Terre emergee la plus proche de l'origine : <white>%d, %d, %d"
                .formatted(spawn[0], spawn[1], spawn[2]));
        plugin.msg(sender, "<gray>Renseignez <white>spawn-x=%s spawn-y=%d spawn-z=%s<gray> dans la configuration."
                .formatted(spawn[0] + 0.5, spawn[1], spawn[2] + 0.5));
        return Command.SINGLE_SUCCESS;
    }

    private int locate(final CommandContext<CommandSource> ctx, final Key biome, final int radius) {
        final CommandSender sender = ctx.getSource().sender();
        final OverworldGenerator generator = generator(sender);
        if (generator == null) {
            return 0;
        }

        int centreX = 0;
        int centreZ = 0;
        if (sender instanceof final Player player) {
            centreX = (int) Math.floor(player.location().x());
            centreZ = (int) Math.floor(player.location().z());
        }

        // Recherche en spirale : les anneaux les plus proches sont explores en premier, donc le
        // premier resultat est bien le plus proche a la resolution du pas.
        for (int distance = 0; distance <= radius; distance += STEP) {
            final int samples = Math.max(8, distance / 4);
            for (int i = 0; i < samples; i++) {
                final double angle = i * 2.0 * Math.PI / samples;
                final int x = centreX + (int) (Math.cos(angle) * distance);
                final int z = centreZ + (int) (Math.sin(angle) * distance);
                if (!generator.biomeAt(x, z).equals(biome)) {
                    continue;
                }
                plugin.msg(sender, "<green>%s trouve a <white>%d, %d<green> (%d blocs, sol y=%d)"
                        .formatted(biome.asString(), x, z, distance, generator.surfaceAt(x, z, -64, 319)));
                return Command.SINGLE_SUCCESS;
            }
        }

        plugin.msg(sender, "<red>%s introuvable dans un rayon de %d blocs.".formatted(biome.asString(), radius));
        return 0;
    }

    private OverworldGenerator generator(final CommandSender sender) {
        final OverworldGenerator generator = plugin.generator();
        if (generator == null) {
            plugin.msg(sender, "<red>Le generateur d'Overworld n'est pas actif.");
        }
        return generator;
    }

    private static String continentName(final int level) {
        return switch (level) {
            case 0 -> "champignonniere";
            case 1 -> "ocean profond";
            case 2 -> "ocean";
            case 3 -> "cote";
            case 4 -> "terres proches";
            case 5 -> "terres moyennes";
            default -> "terres lointaines";
        };
    }

    private static String peaksName(final int level) {
        return switch (level) {
            case 0 -> "vallees";
            case 1 -> "bas";
            case 2 -> "milieu";
            case 3 -> "hauteurs";
            default -> "pics";
        };
    }
}
