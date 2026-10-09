package fr.euphyllia.fidorial.testplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.testplugin.TestPlugin;
import fr.euphyllia.fidorial.testplugin.pregen.PregenTask;
import fr.fidorial.Server;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.entity.Player;
import fr.fidorial.math.Location;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.World;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

public final class PregenCommand {

    private static TestPlugin plugin;

    public PregenCommand(final TestPlugin plugin) {
        PregenCommand.plugin = plugin;
    }

    public LiteralCommandNode<CommandSource> create() {
        return literal("pregen")
                .then(literal("start")
                        .requires(_ -> !isTaskRunning())
                        .then(argument("radius", ArgumentTypes.integer(1, Integer.MAX_VALUE))
                                .executes(PregenCommand::startDefault)
                                .then(argument("centerX", IntegerArgumentType.integer())
                                        .then(argument("centerZ", IntegerArgumentType.integer())
                                                .executes(PregenCommand::startCentered)))))
                .then(literal("stop")
                        .requires(_ -> isTaskRunning())
                        .executes(PregenCommand::stopCommand))
                .then(literal("status").executes(PregenCommand::statusCommand))
                .build();
    }

    private static boolean isTaskRunning() {
        final PregenTask task = plugin.getTask();
        return task != null && task.isRunning();
    }

    private static int startDefault(final CommandContext<CommandSource> ctx) {
        final CommandSource source = ctx.getSource();
        final Location location = source.location();
        if (location == null) {
            plugin.msg(source, "<red>Aucun monde cible.</red>");
            return Command.SINGLE_SUCCESS;
        }

        final ChunkPos center = location.chunk();
        start(source, location.world(), center.x(), center.z(), IntegerArgumentType.getInteger(ctx, "radius"));
        return Command.SINGLE_SUCCESS;
    }

    private static int startCentered(final CommandContext<CommandSource> ctx) {
        final CommandSource source = ctx.getSource();
        final Location location = source.location();
        if (location == null) {
            plugin.msg(source, "<red>Aucun monde cible.</red>");
            return Command.SINGLE_SUCCESS;
        }

        start(
                source,
                location.world(),
                IntegerArgumentType.getInteger(ctx, "centerX"),
                IntegerArgumentType.getInteger(ctx, "centerZ"),
                IntegerArgumentType.getInteger(ctx, "radius"));
        return Command.SINGLE_SUCCESS;
    }

    private static void start(
            final CommandSource source,
            final World world,
            final int centerX,
            final int centerZ,
            final int radius
    ) {
        final long total = (2L * radius + 1) * (2L * radius + 1);
        plugin.msg(source, "Pre-generation de " + total + " chunks (rayon " + radius + ")...");

        final PregenTask task = new PregenTask(
                world,
                plugin.logger,
                centerX,
                centerZ,
                radius,
                message -> {
                    plugin.logger.info("[Pregen] {}", message);
                    plugin.msg(source, "<gray>[Pregen]</gray> " + message);
                },
                PregenCommand::resendCommands,
                PregenCommand::resendCommands);

        plugin.setTask(task);
        task.start();
    }

    private static int stopCommand(final CommandContext<CommandSource> ctx) {
        final CommandSource source = ctx.getSource();
        final PregenTask task = plugin.getTask();

        if (task == null || !task.isRunning()) {
            plugin.msg(source, "Aucune pre-generation en cours.");
            return 0;
        }

        task.cancel();
        plugin.msg(source, "Arret de la pre-generation demande.");
        return Command.SINGLE_SUCCESS;
    }

    private static int statusCommand(final CommandContext<CommandSource> ctx) {
        final CommandSource source = ctx.getSource();
        final PregenTask task = plugin.getTask();

        if (task == null || !task.isRunning()) {
            plugin.msg(source, "Aucune pre-generation en cours.");
            return Command.SINGLE_SUCCESS;
        }

        plugin.msg(source, "Pre-generation : " + task.status());
        return Command.SINGLE_SUCCESS;
    }

    public static void resendCommands() {
        final Server server = plugin.server();
        for (final Player player : server.onlinePlayers()) {
            player.refreshCommands();
        }
    }
}
