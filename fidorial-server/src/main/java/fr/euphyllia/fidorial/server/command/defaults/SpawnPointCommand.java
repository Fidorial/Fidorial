package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.command.argument.resolvers.PositionResolver;
import fr.fidorial.command.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import fr.fidorial.entity.Player;
import fr.fidorial.math.Location;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

public final class SpawnPointCommand {

    private static final String PERMISSION = "fidorial.command.spawnpoint";

    public static LiteralCommandNode<CommandSource> create() {
        return literal("spawnpoint")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .executes(SpawnPointCommand::executeSelf)
                .then(literal("clear")
                        .executes(context -> clear(context, selfOrFail(context)))
                        .then(argument("targets", ArgumentTypes.players())
                                .executes(context -> clear(context, targets(context)))))
                .then(argument("targets", ArgumentTypes.players())
                        .executes(context -> set(context, targets(context), null))
                        .then(argument("position", ArgumentTypes.position())
                                .executes(context -> set(
                                        context,
                                        targets(context),
                                        context.getArgument("position", PositionResolver.class)
                                                .resolve(context.getSource())))))
                .build();
    }

    private static int executeSelf(final CommandContext<CommandSource> context) {
        return set(context, selfOrFail(context), null);
    }

    private static List<Player> targets(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        return context.getArgument("targets", PlayerSelectorArgumentResolver.class)
                .resolve(context.getSource());
    }

    private static List<Player> selfOrFail(final CommandContext<CommandSource> context) {
        if (context.getSource().sender() instanceof final Player sender) {
            return List.of(sender);
        }
        context.getSource().sender().sendMessage(Component.translatable("command.spawnpoint.console"));
        return List.of();
    }

    private static int set(
            final CommandContext<CommandSource> context,
            final List<Player> targets,
            final @Nullable Location position) {

        for (final Player target : targets) {
            final Location location = position != null ? position : target.location();
            target.setRespawnPoint(new RespawnPoint(target.world(), location));

            final Component where = describe(location);
            target.sendMessage(Component.translatable("command.spawnpoint.set.self", where));

            if (context.getSource().sender() != target) {
                context.getSource()
                        .sender()
                        .sendMessage(Component.translatable(
                                "command.spawnpoint.set.other", Component.text(target.name()), where));
            }
        }

        return targets.size();
    }

    private static int clear(final CommandContext<CommandSource> context, final List<Player> targets) {
        for (final Player target : targets) {
            target.setRespawnPoint((RespawnPoint) null);

            target.sendMessage(Component.translatable("command.spawnpoint.cleared.self"));

            if (context.getSource().sender() != target) {
                context.getSource()
                        .sender()
                        .sendMessage(Component.translatable(
                                "command.spawnpoint.cleared.other", Component.text(target.name())));
            }
        }

        return targets.size();
    }

    private static Component describe(final Location location) {
        return Component.text("%.2f, %.2f, %.2f".formatted(location.x(), location.y(), location.z()));
    }
}
