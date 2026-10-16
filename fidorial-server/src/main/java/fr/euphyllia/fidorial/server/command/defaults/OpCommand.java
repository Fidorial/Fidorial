package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.resolvers.PlayerProfilesResolver;
import fr.fidorial.entity.Player;
import fr.fidorial.entity.PlayerProfile;
import net.kyori.adventure.text.Component;

import java.util.List;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;
import static fr.fidorial.command.argument.Arguments.arguments;

public final class OpCommand {

    private OpCommand() {
    }

    public static LiteralCommandNode<CommandSource> createOp() {
        return create("op", true);
    }

    public static LiteralCommandNode<CommandSource> createDeop() {
        return create("deop", false);
    }

    private static LiteralCommandNode<CommandSource> create(final String name, final boolean grant) {
        final ArgumentType<PlayerProfilesResolver> playerArgument = arguments().playerProfiles(player -> player.isOperator() != grant);
        return literal(name)
                .requires(source ->
                        source.sender().hasPermission(grant ? "fidorial.command.op" : "fidorial.command.deop"))
                .then(argument("player", playerArgument)
                        .executes(context -> execute(context, grant))).build();
    }

    private static int execute(final CommandContext<CommandSource> context, final boolean grant) throws CommandSyntaxException {
        final List<PlayerProfile> targets = PlayerProfilesResolver.playerProfiles(context, "player");

        int changed = 0;

        for (final PlayerProfile targetProfile : targets) {
            final List<? extends Player> players = context.getSource().server().players().online().stream()
                    .filter(player -> player.uuid().equals(targetProfile.uuid()))
                    .toList();

            for (final Player target : players) {
                target.setOperator(grant);
                target.sendMessage(
                        Component.translatable(grant ? "command.op.granted.self" : "command.op.revoked.self"));

                context.getSource()
                        .sendMessage(Component.translatable(
                                grant ? "command.op.granted.other" : "command.op.revoked.other",
                                Component.text(target.name())));
                changed++;
            }
        }

        return changed;
    }
}
