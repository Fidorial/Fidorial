package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.entity.Player;
import fr.fidorial.world.World;
import fr.fidorial.world.time.DayNightCycle;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

/**
 * /time set day|noon|sunset|night|midnight|sunrise|&lt;ticks&gt;
 * /time add &lt;ticks&gt;
 * /time query daytime|gametime|day
 * /time freeze|resume
 */
public final class TimeCommand {

    private static final String PERMISSION = "fidorial.command.time";

    private TimeCommand() {
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("time")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .then(literal("set")
                        .then(preset("day", 1_000))
                        .then(preset("noon", DayNightCycle.NOON))
                        .then(preset("sunset", DayNightCycle.SUNSET))
                        .then(preset("night", DayNightCycle.NIGHT_START))
                        .then(preset("midnight", DayNightCycle.MIDNIGHT))
                        .then(preset("sunrise", DayNightCycle.SUNRISE))
                        .then(argument("ticks", ArgumentTypes.time(0))
                                .executes(context -> set(context, context.getArgument("ticks", Integer.class)))
                                .then(argument("world", ArgumentTypes.world())
                                        .executes(context ->
                                                set(context, context.getArgument("ticks", Integer.class))))))
                .then(literal("add")
                        .then(argument("ticks", ArgumentTypes.time(Integer.MIN_VALUE))
                                .executes(TimeCommand::add)
                                .then(argument("world", ArgumentTypes.world())
                                        .executes(TimeCommand::add))))
                .then(literal("query")
                        .then(query("daytime"))
                        .then(query("gametime"))
                        .then(query("day")))
                .then(toggle("freeze", false))
                .then(toggle("resume", true))
                .executes(context -> query(context, "daytime"))
                .build();
    }

    private static LiteralCommandNode<CommandSource> preset(final String name, final int ticks) {
        return literal(name)
                .executes(context -> set(context, ticks))
                .then(argument("world", ArgumentTypes.world())
                        .executes(context -> set(context, ticks)))
                .build();
    }

    private static LiteralCommandNode<CommandSource> toggle(final String name, final boolean running) {
        return literal(name)
                .executes(context -> freeze(context, running))
                .then(argument("world", ArgumentTypes.world())
                        .executes(context -> freeze(context, running)))
                .build();
    }

    private static LiteralCommandNode<CommandSource> query(final String kind) {
        return literal(kind)
                .executes(context -> query(context, kind))
                .then(argument("world", ArgumentTypes.world())
                        .executes(context -> query(context, kind)))
                .build();
    }

    private static @Nullable World target(final CommandContext<CommandSource> context) {
        for (final var node : context.getNodes()) {
            if ("world".equals(node.getNode().getName())) {
                return context.getArgument("world", World.class);
            }
        }
        if (context.getSource().sender() instanceof final Player player) {
            return player.world();
        }
        return FidorialServer.getInstance().worldManager().defaultWorld().orElse(null);
    }

    private static int set(final CommandContext<CommandSource> context, final int timeOfDay) {
        final World world = target(context);
        if (handleMissingWorld(context.getSource(), world)) {
            return 0;
        }
        final DayNightCycle cycle = world.dayNightCycle();
        cycle.setTime(cycle.day() * DayNightCycle.DAY_LENGTH + Math.floorMod(timeOfDay, DayNightCycle.DAY_LENGTH));
        context.getSource()
                .sender()
                .sendMessage(Component.translatable(
                        "command.time.set",
                        Component.text(cycle.timeOfDay()),
                        Component.text(world.key().asString())));
        return Command.SINGLE_SUCCESS;
    }

    private static int add(final CommandContext<CommandSource> context) {
        final World world = target(context);
        if (handleMissingWorld(context.getSource(), world)) {
            return 0;
        }
        final DayNightCycle cycle = world.dayNightCycle();
        cycle.addTime(context.getArgument("ticks", Integer.class));
        context.getSource()
                .sender()
                .sendMessage(Component.translatable(
                        "command.time.set",
                        Component.text(cycle.timeOfDay()),
                        Component.text(world.key().asString())));
        return Command.SINGLE_SUCCESS;
    }

    private static int query(final CommandContext<CommandSource> context, final String kind) {
        final World world = target(context);
        if (handleMissingWorld(context.getSource(), world)) {
            return 0;
        }
        final DayNightCycle cycle = world.dayNightCycle();
        final long value = switch (kind) {
            case "gametime" -> cycle.worldAge();
            case "day" -> cycle.day();
            default -> cycle.timeOfDay();
        };
        context.getSource()
                .sender()
                .sendMessage(Component.translatable(
                        "command.time.query",
                        Component.text(world.key().asString()),
                        Component.text(value),
                        Component.translatable("time.phase." + cycle.phase().name().toLowerCase(Locale.ROOT))));
        return Command.SINGLE_SUCCESS;
    }

    private static int freeze(final CommandContext<CommandSource> context, final boolean running) {
        final World world = target(context);
        if (handleMissingWorld(context.getSource(), world)) {
            return 0;
        }
        world.dayNightCycle().setDoDaylightCycle(running);
        context.getSource()
                .sender()
                .sendMessage(Component.translatable(
                        running ? "command.time.resumed" : "command.time.frozen",
                        Component.text(world.key().asString())));
        return Command.SINGLE_SUCCESS;
    }

    private static boolean handleMissingWorld(final CommandSource source, final @Nullable World world) {
        if (world == null) {
            source.sender().sendMessage(Component.translatable("command.time.no_world"));
            return true;
        }
        return false;
    }
}
