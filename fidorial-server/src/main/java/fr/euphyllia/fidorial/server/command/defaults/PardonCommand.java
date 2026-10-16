package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.resolvers.PlayerProfileListResolver;
import fr.fidorial.entity.PlayerProfile;
import fr.fidorial.moderation.BanManager;
import net.kyori.adventure.text.Component;

import java.util.Collection;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;
import static fr.fidorial.command.argument.Arguments.arguments;

public final class PardonCommand {

    private static final String PERMISSION = "fidorial.command.pardon";

    private PardonCommand() {
    }

    public static LiteralCommandNode<CommandSource> create() {
        final ArgumentType<PlayerProfileListResolver> playerArgument =
                arguments().playerProfiles(player ->
                        server().players().bans().isBanned(player.uuid()));

        return literal("pardon")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .then(argument("player", playerArgument)
                        .executes(PardonCommand::pardon))
                .build();
    }

    private static int pardon(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        final CommandSource source = context.getSource();
        final BanManager bans = server().players().bans();

        final Collection<PlayerProfile> targets =
                context.getArgument("player", PlayerProfileListResolver.class).resolve(source);

        int pardoned = 0;

        for (final PlayerProfile target : targets) {
            final Component name = Component.text(target.name());

            if (!bans.pardon(target.uuid())) {
                source.sendMessage(Component.translatable("commands.pardon.failed", name));
                continue;
            }

            source.sendMessage(Component.translatable("commands.pardon.success", name));
            pardoned++;
        }

        return pardoned;
    }

    private static FidorialServer server() {
        return FidorialServer.getInstance();
    }
}
