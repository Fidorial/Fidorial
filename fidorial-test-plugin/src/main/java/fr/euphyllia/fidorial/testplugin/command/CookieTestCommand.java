package fr.euphyllia.fidorial.testplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.testplugin.TestPlugin;
import fr.fidorial.command.CommandSource;
import fr.fidorial.entity.player.Player;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

/**
 * Cookies, tested against the client of the player running the command.
 *
 * <ul>
 *     <li>{@code /testcookie} stores cookies and reads them back, step by step, and reports each step.</li>
 *     <li>{@code /testcookie set <text>} and {@code /testcookie get} store a note and read it back: the
 *     client keeps it through {@code /testtransfer}, so {@code get} on the next server still finds it.</li>
 * </ul>
 */
public final class CookieTestCommand {

    private static final Key COOKIE = Key.key("fidorial-testplugin", "cookie_test");
    private static final Key NOTE = Key.key("fidorial-testplugin", "note");
    private static final long ANSWER_TIMEOUT_SECONDS = 5;

    private final TestPlugin plugin;
    private final List<Step> steps = List.of(
            new Step("A stored cookie is read back", CookieTestCommand::readBack),
            new Step("A cookie stored again replaces the previous one", CookieTestCommand::overwrite),
            new Step("A cookie never stored is empty", CookieTestCommand::missing),
            new Step("A cookie of " + Player.MAX_COOKIE_SIZE + " bytes is read back", CookieTestCommand::largest),
            new Step("A cookie of " + (Player.MAX_COOKIE_SIZE + 1) + " bytes is refused", CookieTestCommand::oversized),
            new Step("Two reads at once get their own copy", CookieTestCommand::twoReads));

    public CookieTestCommand(final TestPlugin plugin) {
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSource> create() {
        return literal("testcookie")
                .executes(this::runSteps)
                .then(literal("set")
                        .then(argument("text", StringArgumentType.greedyString()).executes(this::setNote)))
                .then(literal("get").executes(this::getNote))
                .build();
    }

    private int runSteps(final CommandContext<CommandSource> ctx) {
        final Player player = player(ctx);
        if (player == null) {
            return 0;
        }
        player.sendMessage(Component.text("[TestPlugin] Testing cookies against your client...", NamedTextColor.YELLOW));
        CompletableFuture<Integer> passed = CompletableFuture.completedFuture(0);
        for (final Step step : steps) {
            passed = passed.thenCompose(count -> run(player, step).thenApply(ok -> ok ? count + 1 : count));
        }
        passed.thenAccept(count -> player.sendMessage(Component.text(
                "[TestPlugin] Cookies: " + count + "/" + steps.size() + " steps passed",
                count == steps.size() ? NamedTextColor.GREEN : NamedTextColor.RED)));
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Runs a step and reports it; the client gets a few seconds to answer.
     */
    private static CompletableFuture<Boolean> run(final Player player, final Step step) {
        return start(player, step)
                .orTimeout(ANSWER_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .handle((problem, error) -> {
                    final String failure = error != null ? describe(error) : problem;
                    player.sendMessage(failure == null
                            ? Component.text("  ✔ " + step.name(), NamedTextColor.GREEN)
                            : Component.text("  ✘ " + step.name() + ": " + failure, NamedTextColor.RED));
                    return failure == null;
                });
    }

    private static CompletableFuture<@Nullable String> start(final Player player, final Step step) {
        try {
            return step.check().apply(player);
        } catch (final RuntimeException e) {
            return CompletableFuture.completedFuture(describe(e));
        }
    }

    private static CompletableFuture<@Nullable String> readBack(final Player player) {
        final byte[] stored = randomBytes(16);
        player.storeCookie(COOKIE, stored);
        return player.retrieveCookie(COOKIE).thenApply(cookie -> compare(cookie, stored));
    }

    private static CompletableFuture<@Nullable String> overwrite(final Player player) {
        final byte[] replaced = randomBytes(16);
        final byte[] stored = randomBytes(24);
        player.storeCookie(COOKIE, replaced);
        player.storeCookie(COOKIE, stored);
        return player.retrieveCookie(COOKIE).thenApply(cookie -> compare(cookie, stored));
    }

    private static CompletableFuture<@Nullable String> missing(final Player player) {
        final Key neverStored = Key.key("fidorial-testplugin", "never_stored_" + HexFormat.of().formatHex(randomBytes(4)));
        return player.retrieveCookie(neverStored).thenApply(cookie -> cookie.isEmpty()
                ? null
                : "the client sent " + cookie.get().length + " bytes");
    }

    private static CompletableFuture<@Nullable String> largest(final Player player) {
        final byte[] stored = randomBytes(Player.MAX_COOKIE_SIZE);
        player.storeCookie(COOKIE, stored);
        return player.retrieveCookie(COOKIE).thenApply(cookie -> compare(cookie, stored));
    }

    private static CompletableFuture<@Nullable String> oversized(final Player player) {
        try {
            player.storeCookie(COOKIE, new byte[Player.MAX_COOKIE_SIZE + 1]);
            return CompletableFuture.completedFuture("the server accepted it");
        } catch (final IllegalArgumentException expected) {
            return CompletableFuture.completedFuture(null);
        }
    }

    private static CompletableFuture<@Nullable String> twoReads(final Player player) {
        final byte[] stored = randomBytes(16);
        player.storeCookie(COOKIE, stored);
        return player.retrieveCookie(COOKIE).thenCombine(player.retrieveCookie(COOKIE), (first, second) -> {
            final String problem = compare(first, stored);
            if (problem != null) {
                return "first read: " + problem;
            }
            final String secondProblem = compare(second, stored);
            if (secondProblem != null) {
                return "second read: " + secondProblem;
            }
            return first.get() == second.get() ? "both reads share the same array" : null;
        });
    }

    private int setNote(final CommandContext<CommandSource> ctx) {
        final Player player = player(ctx);
        if (player == null) {
            return 0;
        }
        final byte[] note = StringArgumentType.getString(ctx, "text").getBytes(StandardCharsets.UTF_8);
        try {
            player.storeCookie(NOTE, note);
        } catch (final IllegalArgumentException tooLarge) {
            player.sendMessage(Component.text("[TestPlugin] " + tooLarge.getMessage(), NamedTextColor.RED));
            return 0;
        }
        player.sendMessage(Component.text("[TestPlugin] Note stored (" + note.length + " bytes). "
                + "Read it with /testcookie get, here or after /testtransfer.", NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    private int getNote(final CommandContext<CommandSource> ctx) {
        final Player player = player(ctx);
        if (player == null) {
            return 0;
        }
        player.retrieveCookie(NOTE)
                .orTimeout(ANSWER_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .whenComplete((note, error) -> player.sendMessage(error != null
                        ? Component.text("[TestPlugin] No answer from the client: " + describe(error), NamedTextColor.RED)
                        : note.map(bytes -> Component.text("[TestPlugin] Note: " + new String(bytes, StandardCharsets.UTF_8), NamedTextColor.GREEN))
                        .orElseGet(() -> Component.text("[TestPlugin] No note: store one with /testcookie set <text>", NamedTextColor.GRAY))));
        return Command.SINGLE_SUCCESS;
    }

    private static @Nullable String compare(final Optional<byte[]> cookie, final byte[] expected) {
        if (cookie.isEmpty()) {
            return "the client has no such cookie";
        }
        if (!Arrays.equals(cookie.get(), expected)) {
            return "expected " + expected.length + " bytes, the client sent " + cookie.get().length + " different ones";
        }
        return null;
    }

    private static byte[] randomBytes(final int size) {
        final byte[] bytes = new byte[size];
        ThreadLocalRandom.current().nextBytes(bytes);
        return bytes;
    }

    private static String describe(final Throwable error) {
        final Throwable cause = error instanceof CompletionException && error.getCause() != null
                ? error.getCause()
                : error;
        return cause instanceof TimeoutException
                ? "the client did not answer within " + ANSWER_TIMEOUT_SECONDS + " s"
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
     * A step of the test: what it checks, and the check, which completes with the problem or {@code null}.
     */
    private record Step(String name, Function<Player, CompletableFuture<@Nullable String>> check) {
    }
}
