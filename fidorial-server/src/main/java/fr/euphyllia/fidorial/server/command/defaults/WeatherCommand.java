package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.weather.WorldWeather;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.entity.Entity;
import fr.fidorial.world.World;
import fr.fidorial.world.weather.Weather;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

public final class WeatherCommand {

    private static final String DURATION = "duration";
    private static final String WORLD = "world";

    private WeatherCommand() {
    }

    private static Component describe(final Weather weather) {
        return switch (weather) {
            case CLEAR -> Component.translatable("weather.clear");
            case RAIN -> Component.translatable("weather.rain");
            case THUNDER -> Component.translatable("weather.thunder");
        };
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("weather")
                .requires(source -> source.sender().hasPermission("fidorial.command.weather"))
                .then(literal("get")
                        .executes(context -> get(context.getSource(), null))
                        .then(argument(WORLD, ArgumentTypes.world())
                                .executes(context -> get(context.getSource(), targetWorld(context)))))
                .then(weather("clear", Weather.CLEAR))
                .then(weather("rain", Weather.RAIN))
                .then(weather("thunder", Weather.THUNDER))
                .build();
    }

    private static LiteralCommandNode<CommandSource> weather(final String name, final Weather weather) {
        return literal(name)
                .executes(context -> set(context.getSource(), weather, 0, null))
                .then(argument(DURATION, ArgumentTypes.time(0))
                        .executes(context -> set(context.getSource(), weather, duration(context), null))
                        .then(argument(WORLD, ArgumentTypes.world())
                                .executes(context -> set(context.getSource(), weather, duration(context), targetWorld(context)))))
                .build();
    }

    private static int duration(final CommandContext<CommandSource> context) {
        return context.getArgument(DURATION, Integer.class);
    }

    private static ServerWorld targetWorld(final CommandContext<CommandSource> context) {
        return (ServerWorld) context.getArgument(WORLD, World.class);
    }

    private static @Nullable ServerWorld resolve(final CommandSource source, final @Nullable ServerWorld requested) {
        if (requested != null) {
            if (requested.weather().hasCycle()) {
                return requested;
            }
            source.sender().sendMessage(Component.translatable(
                    "command.weather.noweather", Component.text(requested.key().asString())));
            return null;
        }
        final ServerWorld current = worldOf(source);
        if (current != null && current.weather().hasCycle()) {
            return current;
        }
        return FidorialServer.getInstance().weatherEngine().primaryWorld();
    }

    private static @Nullable ServerWorld worldOf(final CommandSource source) {
        if (source.sender() instanceof final Entity entity && entity.world() instanceof final ServerWorld world) {
            return world;
        }
        final Entity executor = source.executor();
        if (executor != null && executor.world() instanceof final ServerWorld world) {
            return world;
        }
        return null;
    }

    private static int get(final CommandSource source, final @Nullable ServerWorld requested) {
        final ServerWorld world = resolve(source, requested);
        if (world == null) {
            return 0;
        }
        source.sender().sendMessage(Component.translatable(
                "command.weather.current",
                describe(world.weather().weather()),
                Component.text(world.key().asString())));
        return Command.SINGLE_SUCCESS;
    }

    private static int set(
            final CommandSource source,
            final Weather target,
            final int durationTicks,
            final @Nullable ServerWorld requested) {
        final ServerWorld world = resolve(source, requested);
        if (world == null) {
            return 0;
        }
        final WorldWeather weather = world.weather();
        weather.setWeather(target, durationTicks);

        final Component worldName = Component.text(world.key().asString());
        if (durationTicks > 0) {
            source.sender().sendMessage(Component.translatable(
                    "command.weather.changed.duration", describe(target), Component.text(durationTicks / 20), worldName));
        } else {
            source.sender().sendMessage(Component.translatable("command.weather.changed", describe(target), worldName));
        }
        return Command.SINGLE_SUCCESS;
    }
}
