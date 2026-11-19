package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.resolvers.PlayersResolver;
import fr.fidorial.entity.player.Player;
import net.kyori.adventure.text.Component;

import java.util.List;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;
import static fr.fidorial.command.argument.Arguments.arguments;

/**
 * The {@code /transfer} command, sending players to another server.
 *
 * <pre>
 * /transfer &lt;hostname&gt; [&lt;port&gt;] [&lt;players&gt;]
 * </pre>
 *
 * <p>The port defaults to {@code 25565} and the players to the one running the command. The target server
 * must accept transfers, as this one does with {@code network.accepts-transfers}.</p>
 */
public final class TransferCommand {

    private static final String PERMISSION = "fidorial.command.transfer";

    private static final int DEFAULT_PORT = 25565;
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;

    private static final SimpleCommandExceptionType NO_PLAYERS =
            ExceptionFactory.simple("commands.transfer.error.no_players");
    private static final SimpleCommandExceptionType NOT_A_PLAYER =
            ExceptionFactory.simple("permissions.requires.player");

    private TransferCommand() {
        throw new UnsupportedOperationException("TransferCommand cannot be instantiated.");
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("transfer")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .then(argument("hostname", string())
                        .executes(context -> transfer(context, DEFAULT_PORT, List.of(self(context))))
                        .then(argument("port", integer(MIN_PORT, MAX_PORT))
                                .executes(context -> transfer(context, port(context), List.of(self(context))))
                                .then(argument("players", arguments().players())
                                        .executes(context -> transfer(context, port(context),
                                                PlayersResolver.players(context, "players"))))))
                .build();
    }

    private static Player self(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        if (context.getSource().executor() instanceof final Player player) {
            return player;
        }
        throw NOT_A_PLAYER.create();
    }

    private static int port(final CommandContext<CommandSource> context) {
        return context.getArgument("port", Integer.class);
    }

    private static int transfer(final CommandContext<CommandSource> context, final int port, final List<Player> players)
            throws CommandSyntaxException {
        if (players.isEmpty()) {
            throw NO_PLAYERS.create();
        }
        final String hostname = context.getArgument("hostname", String.class);
        for (final Player player : players) {
            player.transfer(hostname, port);
        }

        final Component host = Component.text(hostname);
        final Component portText = Component.text(port);
        context.getSource().sendMessage(players.size() == 1
                ? Component.translatable("commands.transfer.success.single", players.getFirst().displayName(), host, portText)
                : Component.translatable("commands.transfer.success.multiple", Component.text(players.size()), host, portText));
        return players.size();
    }
}
