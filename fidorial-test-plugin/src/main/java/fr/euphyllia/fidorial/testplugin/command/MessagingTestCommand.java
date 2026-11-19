package fr.euphyllia.fidorial.testplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.testplugin.TestPlugin;
import fr.fidorial.command.CommandSource;
import fr.fidorial.entity.player.Player;
import fr.fidorial.messaging.PluginMessage;
import fr.fidorial.messaging.PluginMessenger;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

/**
 * Plugin messages, tested through the proxy in front of the server, on the BungeeCord channel.
 *
 * <ul>
 *     <li>{@code /testmessaging} asks the proxy questions through the player's connection, and reports
 *     each answer.</li>
 *     <li>{@code /testmessaging info} shows the brand of the client and the channels each side listens to.</li>
 *     <li>{@code /testmessaging connect <server>} asks the proxy to send the player to another server.</li>
 * </ul>
 */
public final class MessagingTestCommand {

    private static final Key BUNGEECORD = Key.key("bungeecord", "main");
    private static final long ANSWER_TIMEOUT_SECONDS = 5;

    private final TestPlugin plugin;
    private final PluginMessenger messenger;
    // The questions waiting for the proxy, by subchannel, in the order they were asked: it answers in order.
    private final Map<String, Queue<CompletableFuture<DataInputStream>>> pending = new ConcurrentHashMap<>();
    private final List<Check> checks = List.of(
            new Check("The proxy names this server",
                    player -> ask(player, "GetServer").thenApply(in -> "this server is '" + readUtf(in) + "'")),
            new Check("The proxy lists its servers",
                    player -> ask(player, "GetServers").thenApply(MessagingTestCommand::readUtf)),
            new Check("The proxy counts the players of the network",
                    player -> ask(player, "PlayerCount", "ALL").thenApply(in -> {
                        readUtf(in);
                        return readInt(in) + " player(s)";
                    })));

    public MessagingTestCommand(final TestPlugin plugin, final PluginMessenger messenger) {
        this.plugin = plugin;
        this.messenger = messenger;
        messenger.listen(BUNGEECORD, this::onProxyMessage);
    }

    public LiteralCommandNode<CommandSource> create() {
        return literal("testmessaging")
                .executes(this::runChecks)
                .then(literal("info").executes(this::info))
                .then(literal("connect")
                        .then(argument("server", StringArgumentType.word()).executes(this::connect)))
                .build();
    }

    /**
     * Hands an answer of the proxy to the oldest question of its subchannel.
     */
    private void onProxyMessage(final PluginMessage message) {
        final DataInputStream in = new DataInputStream(new ByteArrayInputStream(message.data()));
        final String subchannel;
        try {
            subchannel = in.readUTF();
        } catch (final IOException malformed) {
            return;
        }
        final Queue<CompletableFuture<DataInputStream>> waiting = pending.get(subchannel);
        final CompletableFuture<DataInputStream> answer = waiting == null ? null : waiting.poll();
        if (answer != null) {
            answer.complete(in);
        }
    }

    private CompletableFuture<DataInputStream> ask(final Player player, final String subchannel, final String... arguments) {
        final CompletableFuture<DataInputStream> answer = new CompletableFuture<>();
        final Queue<CompletableFuture<DataInputStream>> waiting =
                pending.computeIfAbsent(subchannel, _ -> new ConcurrentLinkedQueue<>());
        waiting.add(answer);
        answer.orTimeout(ANSWER_TIMEOUT_SECONDS, TimeUnit.SECONDS).whenComplete((_, _) -> waiting.remove(answer));
        player.sendPluginMessage(BUNGEECORD, write(subchannel, arguments));
        return answer;
    }

    private int runChecks(final CommandContext<CommandSource> ctx) {
        final Player player = player(ctx);
        if (player == null) {
            return 0;
        }
        player.sendMessage(Component.text("[TestPlugin] Asking the proxy on the BungeeCord channel...", NamedTextColor.YELLOW));
        CompletableFuture<Integer> passed = CompletableFuture.completedFuture(0);
        for (final Check check : checks) {
            passed = passed.thenCompose(count -> run(player, check).thenApply(ok -> ok ? count + 1 : count));
        }
        passed.thenAccept(count -> player.sendMessage(Component.text(
                "[TestPlugin] Messaging: " + count + "/" + checks.size() + " checks passed",
                count == checks.size() ? NamedTextColor.GREEN : NamedTextColor.RED)));
        return Command.SINGLE_SUCCESS;
    }

    private static CompletableFuture<Boolean> run(final Player player, final Check check) {
        return start(player, check).handle((detail, error) -> {
            player.sendMessage(error == null
                    ? Component.text("  ✔ " + check.name() + ": " + detail, NamedTextColor.GREEN)
                    : Component.text("  ✘ " + check.name() + ": " + describe(error), NamedTextColor.RED));
            return error == null;
        });
    }

    private static CompletableFuture<String> start(final Player player, final Check check) {
        try {
            return check.question().apply(player);
        } catch (final RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private int info(final CommandContext<CommandSource> ctx) {
        final Player player = player(ctx);
        if (player == null) {
            return 0;
        }
        final List<String> announced = player.pluginChannels().stream().map(Key::asString).sorted().toList();
        final List<String> listened = messenger.channels().stream().map(Key::asString).sorted().toList();
        player.sendMessage(Component.text("[TestPlugin] Client brand: " + player.clientBrand().orElse("not announced"), NamedTextColor.YELLOW));
        player.sendMessage(Component.text("[TestPlugin] Your client and the proxy listen to: "
                + (announced.isEmpty() ? "no channel" : String.join(", ", announced)), NamedTextColor.YELLOW));
        player.sendMessage(Component.text("[TestPlugin] This server listens to: "
                + (listened.isEmpty() ? "no channel" : String.join(", ", listened)), NamedTextColor.YELLOW));
        return Command.SINGLE_SUCCESS;
    }

    private int connect(final CommandContext<CommandSource> ctx) {
        final Player player = player(ctx);
        if (player == null) {
            return 0;
        }
        final String server = StringArgumentType.getString(ctx, "server");
        player.sendPluginMessage(BUNGEECORD, write("Connect", server));
        player.sendMessage(Component.text("[TestPlugin] Asked the proxy to send you to " + server, NamedTextColor.YELLOW));
        return Command.SINGLE_SUCCESS;
    }

    private static byte[] write(final String subchannel, final String... arguments) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (final DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeUTF(subchannel);
            for (final String argument : arguments) {
                out.writeUTF(argument);
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

    private static String readUtf(final DataInputStream in) {
        try {
            return in.readUTF();
        } catch (final IOException e) {
            throw new UncheckedIOException("The answer of the proxy is malformed", e);
        }
    }

    private static int readInt(final DataInputStream in) {
        try {
            return in.readInt();
        } catch (final IOException e) {
            throw new UncheckedIOException("The answer of the proxy is malformed", e);
        }
    }

    private static String describe(final Throwable error) {
        final Throwable cause = error instanceof CompletionException && error.getCause() != null
                ? error.getCause()
                : error;
        return cause instanceof TimeoutException
                ? "no answer within " + ANSWER_TIMEOUT_SECONDS + " s: is the server behind a proxy answering the BungeeCord channel?"
                : cause.getClass().getSimpleName() + (cause.getMessage() == null ? "" : " (" + cause.getMessage() + ")");
    }

    private @Nullable Player player(final CommandContext<CommandSource> ctx) {
        if (ctx.getSource().executor() instanceof final Player player) {
            return player;
        }
        plugin.msg(ctx.getSource(), "<red>[TestPlugin] Run this command in game.");
        return null;
    }

    /**
     * A question to the proxy: what it checks, and the question, which completes with what the proxy said.
     */
    private record Check(String name, Function<Player, CompletableFuture<String>> question) {
    }
}
