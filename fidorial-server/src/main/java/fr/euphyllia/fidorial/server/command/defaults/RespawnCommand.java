package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.resolvers.PlayersResolver;
import fr.fidorial.entity.player.Player;
import net.kyori.adventure.text.Component;

import java.util.List;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;
import static fr.fidorial.command.argument.Arguments.arguments;

public final class RespawnCommand {

    private static final String PERMISSION = "fidorial.command.respawn";

    public static LiteralCommandNode<CommandSource> create() {
        return literal("respawn")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .executes(RespawnCommand::executeSelf)
                .then(argument("targets", arguments().players()).executes(RespawnCommand::executeTargets))
                .build();
    }

    private static int executeSelf(final CommandContext<CommandSource> context) {
        if (!(context.getSource().executor() instanceof final Player player)) {
            context.getSource().sendMessage(Component.translatable("command.respawn.not_player"));
            return 0;
        }
        return respawn(context, List.of(player));
    }

    private static int executeTargets(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        return respawn(context, PlayersResolver.players(context, "targets"));
    }

    private static int respawn(final CommandContext<CommandSource> context, final List<Player> targets) {
        final CommandSource source = context.getSource();

        for (final Player target : targets) {
            target.respawn().whenComplete((succeeded, _) -> {
                if (!Boolean.TRUE.equals(succeeded)) {
                    source.sendMessage(Component.translatable("command.respawn.alive", Component.text(target.name())));
                    return;
                }
                if (source.executor() != target) {
                    source.sendMessage(Component.translatable("command.respawn.done", Component.text(target.name())));
                } else {
                    source.sendMessage(Component.translatable("command.respawn.done.self"));
                }
            });
        }

        return targets.size();
    }
}
