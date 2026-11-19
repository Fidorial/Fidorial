package fr.euphyllia.fidorial.testplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.testplugin.TestPlugin;
import fr.euphyllia.fidorial.testplugin.transfer.TransferTickets;
import fr.fidorial.command.CommandSource;
import fr.fidorial.entity.player.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jspecify.annotations.Nullable;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

/**
 * Transfers and cookies, on the player running the command.
 *
 * <ul>
 *     <li>{@code /testtransfer to <host> [port]} stores a signed {@link TransferTickets ticket} in a cookie,
 *     then sends the player to another server. That server needs {@code network.accepts-transfers: true},
 *     and checks the ticket when the player logs in if it runs this plugin too.</li>
 *     <li>{@code /testtransfer ticket} reads the ticket back from the client, and checks it.</li>
 * </ul>
 */
public final class TransferTestCommand {

    private static final int DEFAULT_PORT = 25565;

    private final TestPlugin plugin;

    public TransferTestCommand(final TestPlugin plugin) {
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSource> create() {
        return literal("testtransfer")
                .then(literal("to")
                        .then(argument("host", StringArgumentType.string())
                                .executes(ctx -> transfer(ctx, DEFAULT_PORT))
                                .then(argument("port", IntegerArgumentType.integer(1, 65535))
                                        .executes(ctx -> transfer(ctx, IntegerArgumentType.getInteger(ctx, "port"))))))
                .then(literal("ticket").executes(this::ticket))
                .build();
    }

    private int transfer(final CommandContext<CommandSource> ctx, final int port) {
        final Player player = player(ctx);
        if (player == null) {
            return 0;
        }
        final String host = StringArgumentType.getString(ctx, "host");
        player.storeCookie(TransferTickets.COOKIE, plugin.transferTickets().issue(player.uuid()));
        player.sendMessage(Component.text(
                "[TestPlugin] Ticket stored, transferring you to " + host + ":" + port, NamedTextColor.YELLOW));
        player.transfer(host, port);
        return Command.SINGLE_SUCCESS;
    }

    private int ticket(final CommandContext<CommandSource> ctx) {
        final Player player = player(ctx);
        if (player == null) {
            return 0;
        }
        // Answered on the network thread: fine for a message, not for touching the world.
        player.retrieveCookie(TransferTickets.COOKIE).whenComplete((cookie, failure) -> {
            if (failure != null) {
                return; // The player left before answering.
            }
            final TransferTickets.Verdict verdict = plugin.transferTickets().verify(cookie, player.uuid());
            plugin.msg(player, (verdict == TransferTickets.Verdict.VALID ? "<green>" : "<gray>")
                    + "[TestPlugin] Transfer ticket: " + verdict + cookie.map(c -> " (" + c.length + " bytes)").orElse(""));
        });
        return Command.SINGLE_SUCCESS;
    }

    private @Nullable Player player(final CommandContext<CommandSource> ctx) {
        if (ctx.getSource().executor() instanceof final Player player) {
            return player;
        }
        plugin.msg(ctx.getSource(), "<red>[TestPlugin] Run this command in game.");
        return null;
    }
}
