package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.fidorial.command.CommandSender;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.command.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import fr.fidorial.entity.Player;
import net.kyori.adventure.text.Component;

import java.util.List;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

public final class RespawnCommand {

    private static final String PERMISSION = "fidorial.command.respawn";

    public static LiteralCommandNode<CommandSource> create() {
        return literal("respawn")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .executes(RespawnCommand::executeSelf)
                .then(argument("targets", ArgumentTypes.players()).executes(RespawnCommand::executeTargets))
                .build();
    }

    private static int executeSelf(final CommandContext<CommandSource> context) {
        if (!(context.getSource().sender() instanceof final Player sender)) {
            context.getSource().sender().sendMessage(Component.translatable("command.respawn.console"));
            return 0;
        }
        return respawn(context, List.of(sender));
    }

    private static int executeTargets(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        final List<Player> targets = context.getArgument("targets", PlayerSelectorArgumentResolver.class)
                .resolve(context.getSource());
        return respawn(context, targets);
    }

    private static int respawn(final CommandContext<CommandSource> context, final List<Player> targets) {
        final CommandSender sender = context.getSource().sender();

        for (final Player target : targets) {
            target.respawn().whenComplete((succeeded, _) -> {
                if (!Boolean.TRUE.equals(succeeded)) {
                    sender.sendMessage(Component.translatable("command.respawn.alive", Component.text(target.name())));
                    return;
                }
                if (sender != target) {
                    sender.sendMessage(Component.translatable("command.respawn.done", Component.text(target.name())));
                }
            });
        }

        return targets.size();
    }
}
