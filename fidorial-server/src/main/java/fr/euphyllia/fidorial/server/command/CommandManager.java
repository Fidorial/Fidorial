package fr.euphyllia.fidorial.server.command;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.command.brigadier.builtin.exceptions.TranslatableExceptions;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypes;
import fr.euphyllia.fidorial.server.command.defaults.BanCommand;
import fr.euphyllia.fidorial.server.command.defaults.BanIpCommand;
import fr.euphyllia.fidorial.server.command.defaults.BanListCommand;
import fr.euphyllia.fidorial.server.command.defaults.BossBarCommand;
import fr.euphyllia.fidorial.server.command.defaults.DatapackCommand;
import fr.euphyllia.fidorial.server.command.defaults.FillBiomeCommand;
import fr.euphyllia.fidorial.server.command.defaults.GameModeCommand;
import fr.euphyllia.fidorial.server.command.defaults.LocateCommand;
import fr.euphyllia.fidorial.server.command.defaults.OpCommand;
import fr.euphyllia.fidorial.server.command.defaults.PardonCommand;
import fr.euphyllia.fidorial.server.command.defaults.PardonIpCommand;
import fr.euphyllia.fidorial.server.command.defaults.PlaceCommand;
import fr.euphyllia.fidorial.server.command.defaults.RespawnCommand;
import fr.euphyllia.fidorial.server.command.defaults.SpawnPointCommand;
import fr.euphyllia.fidorial.server.command.defaults.StopCommand;
import fr.euphyllia.fidorial.server.command.defaults.SummonCommand;
import fr.euphyllia.fidorial.server.command.defaults.TimeCommand;
import fr.euphyllia.fidorial.server.command.defaults.TpsCommand;
import fr.euphyllia.fidorial.server.command.defaults.WeatherCommand;
import fr.euphyllia.fidorial.server.command.defaults.WhitelistCommand;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundCommandsPacket;
import fr.fidorial.command.CommandRegistry;
import fr.fidorial.command.CommandSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static fr.euphyllia.fidorial.server.adventure.brigadier.BrigadierAdventureHelper.convert;
import static fr.euphyllia.fidorial.server.command.brigadier.builtin.exceptions.TranslatableExceptions.DISPATCHER_UNKNOWN_ARGUMENT;
import static fr.euphyllia.fidorial.server.command.brigadier.builtin.exceptions.TranslatableExceptions.DISPATCHER_UNKNOWN_COMMAND;

public final class CommandManager implements CommandRegistry {
    private final @GuardedBy("lock") CommandDispatcher<CommandSource> dispatcher;
    private final ReadWriteLock lock;
    private final Map<String, RegisteredCommand> namespacedCommands = new ConcurrentHashMap<>();
    private final Map<String, RegisteredCommand> plainCommands = new ConcurrentHashMap<>();
    private final Map<String, LinkedHashSet<RegisteredCommand>> plainCandidates = new ConcurrentHashMap<>();
    private final ExecutorService commandExecutor =
            Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("fidorial-command-worker-", 0).factory());

    public CommandManager() {
        this.lock = new ReentrantReadWriteLock();
        this.dispatcher = new CommandDispatcher<>();

        CommandSyntaxException.BUILT_IN_EXCEPTIONS = new TranslatableExceptions();
        ArgumentTypes.bootstrap();

        registerDefaults();
    }

    private void registerDefaults() {
        registerInternal(WeatherCommand.create(), Set.of("w"));
        registerInternal(StopCommand.create(), Set.of("s"));
        registerInternal(OpCommand.createOp());
        registerInternal(OpCommand.createDeop());
        registerInternal(SummonCommand.create());
        registerInternal(GameModeCommand.create(), Set.of("gm"));
        registerInternal(TpsCommand.create());
        registerInternal(TimeCommand.create());
        registerInternal(BossBarCommand.create());
        registerInternal(BanCommand.create());
        registerInternal(PardonCommand.create(), Set.of("unban"));
        registerInternal(BanListCommand.create());
        registerInternal(WhitelistCommand.create());
        registerInternal(BanIpCommand.create(), Set.of("banip"));
        registerInternal(PardonIpCommand.create(), Set.of("unban-ip", "pardonip"));
        registerInternal(SpawnPointCommand.create());
        registerInternal(RespawnCommand.create());
        registerInternal(FillBiomeCommand.create());
        registerInternal(PlaceCommand.create());
        registerInternal(LocateCommand.create());
        registerInternal(DatapackCommand.create());
    }

    public void registerInternal(final LiteralCommandNode<CommandSource> command) {
        registerInternal(command, Set.of());
    }

    public void registerInternal(final LiteralCommandNode<CommandSource> command, final Set<String> aliases) {
        Preconditions.checkNotNull(command, "command");
        Preconditions.checkNotNull(aliases, "aliases");
        registerCommand(Key.MINECRAFT_NAMESPACE, command, aliases, true);
    }

    @Override
    public void register(final String namespace, final LiteralCommandNode<CommandSource> command, final Set<String> aliases) {
        Preconditions.checkNotNull(namespace, "namespace");
        Preconditions.checkArgument(!namespace.isBlank(), "namespace must not be blank");
        for (final char c : namespace.toCharArray()) {
            Preconditions.checkArgument(Key.allowedInNamespace(c), "namespace contains illegal characters");
        }
        Preconditions.checkArgument(
                !namespace.equalsIgnoreCase(Key.MINECRAFT_NAMESPACE),
                "namespace '%s' is reserved for internal server commands", Key.MINECRAFT_NAMESPACE);
        Preconditions.checkNotNull(command, "command");
        Preconditions.checkNotNull(aliases, "aliases");

        registerCommand(namespace, command, aliases, false);
    }

    private void registerCommand(
            final String namespace,
            final LiteralCommandNode<CommandSource> command,
            final Set<String> aliases,
            final boolean builtin
    ) {
        final String ns = namespace.toLowerCase(Locale.ROOT);
        final RegisteredCommand registered = new RegisteredCommand(ns, command, builtin);

        final Set<String> names = new HashSet<>(aliases);
        names.add(command.getName());

        lock.writeLock().lock();
        try {
            for (final String alias : names) {
                Preconditions.checkArgument(!alias.contains(":"), "alias '%s' must not contain ':'", alias);
                final String aliasKey = alias.toLowerCase(Locale.ROOT);

                registerNamespacedNode(ns, aliasKey, command, registered);
                claimPlainSlot(aliasKey, command, registered);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @GuardedBy("lock") private void registerNamespacedNode(
            final String ns,
            final String aliasKey,
            final LiteralCommandNode<CommandSource> command,
            final RegisteredCommand registered
    ) {
        final String namespacedName = ns + ":" + aliasKey;

        final RegisteredCommand previous = namespacedCommands.put(namespacedName, registered);
        if (previous != null) {
            dispatcher.getRoot().removeChild(namespacedName);
        }

        dispatcher.getRoot().addChild(cloneLiteral(namespacedName, command));
    }

    @GuardedBy("lock") private void claimPlainSlot(
            final String aliasKey,
            final LiteralCommandNode<CommandSource> command,
            final RegisteredCommand registered
    ) {
        final LinkedHashSet<RegisteredCommand> candidates =
                plainCandidates.computeIfAbsent(aliasKey, key -> new LinkedHashSet<>());
        candidates.add(registered);

        final RegisteredCommand current = plainCommands.get(aliasKey);
        final boolean shouldClaim = current == null || (current.builtin() && !registered.builtin());
        if (!shouldClaim) {
            if (current != registered) {
                FidorialServer.LOGGER.warn(
                        "Plain command '/{}' is already claimed by namespace '{}'; '{}:{}' will only be reachable as '/{}:{}'",
                        aliasKey, current.namespace(), registered.namespace(), aliasKey, registered.namespace(), aliasKey
                );
            }
            return;
        }

        if (current != null) {
            dispatcher.getRoot().removeChild(aliasKey);
        }

        final LiteralCommandNode<CommandSource> node =
                aliasKey.equals(command.getName()) ? command : cloneLiteral(aliasKey, command);

        dispatcher.getRoot().addChild(node);
        plainCommands.put(aliasKey, registered);
    }

    private static LiteralCommandNode<CommandSource> cloneLiteral(
            final String name,
            final LiteralCommandNode<CommandSource> original
    ) {
        final LiteralArgumentBuilder<CommandSource> builder =
                LiteralArgumentBuilder.<CommandSource>literal(name).requires(original.getRequirement());

        if (original.getCommand() != null) {
            builder.executes(original.getCommand());
        }

        for (final CommandNode<CommandSource> child : original.getChildren()) {
            builder.then(cloneNode(child));
        }

        return builder.build();
    }

    private static <S> CommandNode<S> cloneNode(final CommandNode<S> node) {
        final ArgumentBuilder<S, ?> builder = node.createBuilder();

        for (final CommandNode<S> child : node.getChildren()) {
            builder.then(cloneNode(child));
        }

        return builder.build();
    }

    @Override
    public void unregister(final String namespace, final String alias) {
        Preconditions.checkNotNull(namespace, "namespace");
        Preconditions.checkNotNull(alias, "alias");

        final String ns = namespace.toLowerCase(Locale.ROOT);
        final String aliasKey = alias.toLowerCase(Locale.ROOT);
        final String namespacedName = ns + ":" + aliasKey;

        lock.writeLock().lock();
        try {
            final RegisteredCommand registered = namespacedCommands.remove(namespacedName);
            if (registered == null) {
                return;
            }
            dispatcher.getRoot().removeChild(namespacedName);

            final LinkedHashSet<RegisteredCommand> candidates = plainCandidates.get(aliasKey);
            if (candidates != null) {
                candidates.remove(registered);
                if (candidates.isEmpty()) {
                    plainCandidates.remove(aliasKey, candidates);
                }
            }

            if (plainCommands.get(aliasKey) == registered) {
                dispatcher.getRoot().removeChild(aliasKey);
                plainCommands.remove(aliasKey);
                promoteNextPlainCandidate(aliasKey);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void unregisterNamespace(final String namespace) {
        Preconditions.checkNotNull(namespace, "namespace");
        final String ns = namespace.toLowerCase(Locale.ROOT);
        final String prefix = ns + ":";

        lock.writeLock().lock();
        try {
            final Set<String> aliasKeys = new HashSet<>();
            for (final String key : namespacedCommands.keySet()) {
                if (key.startsWith(prefix)) {
                    aliasKeys.add(key.substring(prefix.length()));
                }
            }
            for (final String aliasKey : aliasKeys) {
                unregister(ns, aliasKey);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @GuardedBy("lock") private void promoteNextPlainCandidate(final String aliasKey) {
        final LinkedHashSet<RegisteredCommand> candidates = plainCandidates.get(aliasKey);
        if (candidates == null || candidates.isEmpty()) {
            return;
        }

        RegisteredCommand next = null;
        for (final RegisteredCommand candidate : candidates) {
            if (!candidate.builtin()) {
                next = candidate;
                break;
            }
        }
        if (next == null) {
            next = candidates.getFirst();
        }

        final LiteralCommandNode<CommandSource> node =
                aliasKey.equals(next.node().getName()) ? next.node() : cloneLiteral(aliasKey, next.node());

        dispatcher.getRoot().addChild(node);
        plainCommands.put(aliasKey, next);
    }

    @Override
    public CompletableFuture<CommandResult> dispatchAsyncResult(final CommandSource source, final String cmdLine) {
        return CompletableFuture.supplyAsync(() -> {
            final ParseResults<CommandSource> parse;

            lock.readLock().lock();
            try {
                parse = dispatcher.parse(cmdLine, source);
            } finally {
                lock.readLock().unlock();
            }

            final CommandSyntaxException exception = getParseException(parse);
            final boolean isConsole = source.sender() instanceof ConsoleSender;

            if (exception != null) {
                source.sender().sendMessage(convert(exception.getRawMessage(), isConsole).color(NamedTextColor.RED));
                sendContext(source, exception, cmdLine, isConsole);
                return new CommandResult(0);
            }

            try {
                return new CommandResult(dispatcher.execute(parse));
            } catch (final CommandSyntaxException e) {
                source.sender().sendMessage(convert(e.getRawMessage(), isConsole).color(NamedTextColor.RED));
                return new CommandResult(0);
            }
        }, commandExecutor).exceptionally(ex -> {
            FidorialServer.LOGGER.error("Encountered an exception while executing command: \"/{}\"", cmdLine, ex);
            return new CommandResult(0);
        });
    }

    private static boolean hasExecutableNode(final ParseResults<CommandSource> parse) {
        return parse.getContext().getNodes().stream()
                .anyMatch(node -> node.getNode().getCommand() != null);
    }

    private static @Nullable CommandSyntaxException getParseException(final ParseResults<CommandSource> parse) {
        if (!parse.getExceptions().isEmpty()) {
            return parse.getExceptions().values().iterator().next();
        }

        if (parse.getContext().getNodes().isEmpty()) {
            return unknownCommand(parse);
        }

        if (parse.getReader().canRead()) {
            return DISPATCHER_UNKNOWN_ARGUMENT.createWithContext(parse.getReader());
        }

        if (!hasExecutableNode(parse)) {
            return DISPATCHER_UNKNOWN_COMMAND.createWithContext(parse.getReader());
        }

        return null;
    }

    private static CommandSyntaxException unknownCommand(final ParseResults<CommandSource> parse) {
        return DISPATCHER_UNKNOWN_COMMAND.createWithContext(parse.getReader());
    }

    private void sendContext(
            final CommandSource source,
            final CommandSyntaxException exception,
            final String command,
            final boolean isConsole
    ) {

        if (exception.getInput() == null || exception.getCursor() < 0) {
            return;
        }

        final int cursor = Math.min(exception.getInput().length(), exception.getCursor());

        Component context =
                Component.empty().color(NamedTextColor.GRAY).clickEvent(ClickEvent.suggestCommand("/" + command));

        if (cursor > 10) {
            context = context.append(Component.text("..."));
        }

        final int start = Math.max(0, cursor - 10);

        context = context.append(Component.text(exception.getInput().substring(start, cursor)));

        if (cursor < exception.getInput().length()) {
            context = context.append(Component.text(exception.getInput().substring(cursor))
                    .color(NamedTextColor.RED)
                    .decorate(TextDecoration.UNDERLINED));
        }

        context = context.append(
                isConsole
                        ? Component.translatable("console.command.context.here")
                        : Component.translatable("command.context.here")
                        .color(NamedTextColor.RED)
                        .decorate(TextDecoration.ITALIC));

        source.sender().sendMessage(context);
    }

    @Override
    public CompletableFuture<Suggestions> offerSuggestions(final CommandSource source, final String cmdLine) {
        return CompletableFuture.supplyAsync(() -> {
            lock.readLock().lock();
            try {
                final ParseResults<CommandSource> parse = dispatcher.parse(cmdLine, source);
                return dispatcher.getCompletionSuggestions(parse).join();
            } finally {
                lock.readLock().unlock();
            }
        }, commandExecutor);
    }

    @Override
    public boolean hasCommand(final String alias) {
        return command(alias) != null;
    }

    @Override
    public boolean hasCommand(final String alias, final CommandSource source) {
        lock.readLock().lock();
        try {
            final CommandNode<CommandSource> node = dispatcher.getRoot().getChild(alias.toLowerCase(Locale.ROOT));
            return node != null && node.canUse(source);
        } finally {
            lock.readLock().unlock();
        }
    }

    public ParseResults<CommandSource> parse(
            final StringReader reader,
            final CommandSource source
    ) {
        lock.readLock().lock();
        try {
            return dispatcher.parse(reader, source);
        } finally {
            lock.readLock().unlock();
        }
    }

    public ParseResults<CommandSource> parse(
            final String input,
            final CommandSource source
    ) {
        lock.readLock().lock();
        try {
            return dispatcher.parse(input, source);
        } finally {
            lock.readLock().unlock();
        }
    }

    public CompletableFuture<Suggestions> completionSuggestions(
            final ParseResults<CommandSource> parse,
            final int cursor
    ) {
        lock.readLock().lock();
        try {
            return dispatcher.getCompletionSuggestions(parse, cursor);
        } finally {
            lock.readLock().unlock();
        }
    }

    public ClientboundCommandsPacket createCommandsPacket(final ServerPlayer player) {
        lock.readLock().lock();
        try {
            return new ClientboundCommandsPacket(dispatcher, player);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void shutdown() {
        commandExecutor.shutdown();
        try {
            if (!commandExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                commandExecutor.shutdownNow();
            }
        } catch (final InterruptedException e) {
            commandExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public record RegisteredCommand(String namespace, LiteralCommandNode<CommandSource> node, boolean builtin) {
    }

    public @Nullable RegisteredCommand command(final String alias) {
        final String key = alias.toLowerCase(Locale.ROOT);
        return key.contains(":") ? namespacedCommands.get(key) : plainCommands.get(key);
    }
}
