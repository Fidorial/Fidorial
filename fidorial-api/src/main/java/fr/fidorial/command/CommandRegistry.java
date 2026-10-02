package fr.fidorial.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.fidorial.plugin.PluginMeta;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * Handles the registration and execution of commands.
 *
 * @since 0.1.0
 */
public interface CommandRegistry {

    /**
     * Registers a command from the given builder.
     *
     * @param namespace the plugin namespace
     * @param command   the command builder to register
     * @since 0.1.0
     */
    default void register(String namespace, LiteralArgumentBuilder<CommandSource> command) {
        register(namespace, command.build());
    }

    /**
     * Registers a command from the given builder.
     *
     * @param meta    the plugin meta
     * @param command the command builder to register
     * @since 0.1.0
     */
    default void register(PluginMeta meta, LiteralArgumentBuilder<CommandSource> command) {
        register(meta.id(), command.build());
    }

    /**
     * Registers a command node without aliases.
     *
     * @param namespace the plugin namespace
     * @param command   the command node to register
     * @since 0.1.0
     */
    default void register(String namespace, LiteralCommandNode<CommandSource> command) {
        register(namespace, command, Set.of());
    }

    /**
     * Registers a command node without aliases.
     *
     * @param meta    the plugin meta
     * @param command the command node to register
     * @since 0.1.0
     */
    default void register(PluginMeta meta, LiteralCommandNode<CommandSource> command) {
        register(meta.id(), command, Set.of());
    }

    /**
     * Registers a command from the given builder with the specified aliases.
     *
     * @param namespace the plugin namespace
     * @param command   the command builder to register
     * @param aliases   additional aliases that should point to this command
     * @since 0.1.0
     */
    default void register(String namespace, LiteralArgumentBuilder<CommandSource> command, Set<String> aliases) {
        register(namespace, command.build(), aliases);
    }

    /**
     * Registers a command from the given builder with the specified aliases.
     *
     * @param meta    the plugin meta
     * @param command the command builder to register
     * @param aliases additional aliases that should point to this command
     * @since 0.1.0
     */
    default void register(PluginMeta meta, LiteralArgumentBuilder<CommandSource> command, Set<String> aliases) {
        register(meta.id(), command.build(), aliases);
    }

    /**
     * Registers a command node with the specified aliases.
     *
     * <p>Aliases are additional names that can be used to execute the command
     * besides its primary literal name.</p>
     *
     * @param namespace the plugin namespace
     * @param command   the command node to register
     * @param aliases   additional aliases that should point to this command
     * @since 0.1.0
     */
    void register(String namespace, LiteralCommandNode<CommandSource> command, Set<String> aliases);

    /**
     * Registers a command node with the specified aliases.
     *
     * <p>Aliases are additional names that can be used to execute the command
     * besides its primary literal name.</p>
     *
     * @param meta    the plugin meta
     * @param command the command node to register
     * @param aliases additional aliases that should point to this command
     * @since 0.1.0
     */
    default void register(PluginMeta meta, LiteralCommandNode<CommandSource> command, Set<String> aliases) {
        register(meta.id(), command, aliases);
    }

    /**
     * Unregisters the specified command alias from the manager, if registered.
     * Root literal names are also treated as aliases in this context.
     *
     * @param namespace the namespace of the command to unregister
     * @param alias     the command alias to unregister; if not currently registered, this method does nothing
     * @apiNote This removes both the namespaced {@code namespace:alias} and plain {@code alias} command nodes.
     * @since 0.1.0
     */
    void unregister(String namespace, String alias);

    /**
     * Unregisters all command aliases from the manager under this namespace.
     *
     * @param namespace the namespace to unregister
     * @apiNote This removes both the namespaced {@code namespace:alias} and plain {@code alias} command nodes.
     * @since 0.1.0
     */
    void unregisterNamespace(String namespace);

    /**
     * Unregisters all command aliases from the manager under the namespace provided by {@link PluginMeta#id()}.
     *
     * @param meta the plugin meta to unregister
     * @apiNote This removes both the namespaced {@code namespace:alias} and plain {@code alias} command nodes.
     * @since 0.1.0
     */
    default void unregisterNamespace(PluginMeta meta) {
        unregisterNamespace(meta.id());
    }

    /**
     * The outcome of a command dispatch.
     *
     * @param returnValue the raw brigadier return value from {@link CommandDispatcher#execute(ParseResults)}.
     *                    {@code 0} is used both when the command failed to parse or execute, and when a command had no effect.
     * @apiNote Use {@link #executed()} to distinguish outcome from raw value.
     * @since 0.1.0
     */
    record CommandResult(int returnValue) {
        /**
         * Returns whether the dispatch had any effect.
         *
         * @return {@code true} if {@link #returnValue()} is at least {@link Command#SINGLE_SUCCESS}
         * @since 0.1.0
         */
        public boolean executed() {
            return returnValue >= Command.SINGLE_SUCCESS;
        }
    }

    /**
     * Attempts to asynchronously execute a command from the given {@code cmdLine},
     * returning the raw brigadier result alongside an {@code executed} flag.
     * <p>
     * Useful for getting the amount of targets affected by the execution of the command.
     *
     * @param source  the source to execute the command for
     * @param cmdLine the command to run
     * @return a future completed with the {@link CommandResult}; never completes exceptionally
     * @since 0.1.0
     */
    CompletableFuture<CommandResult> dispatchAsyncResult(CommandSource source, String cmdLine);

    /**
     * Attempts to asynchronously execute a command from the given {@code cmdLine}.
     *
     * @param source  the source to execute the command for
     * @param cmdLine the command to run
     * @return a future completed with the result of the command execution; never completes exceptionally
     * @since 0.1.0
     */
    default CompletableFuture<Boolean> dispatchAsync(CommandSource source, String cmdLine) {
        return dispatchAsyncResult(source, cmdLine).thenApply(CommandResult::executed);
    }

    /**
     * Asynchronously collects suggestions to fill in the given command {@code cmdLine}.
     * Returns the brigadier {@link Suggestions} with tooltips for each result.
     *
     * @param source  the source to execute the command for
     * @param cmdLine the partially completed command
     * @return a {@link CompletableFuture} eventually completed with {@link Suggestions}, possibly
     * empty
     * @since 0.1.0
     */
    CompletableFuture<Suggestions> offerSuggestions(CommandSource source, String cmdLine);

    /**
     * Returns whether the given alias is registered on this manager.
     *
     * @param alias the command alias to check
     * @return true if the alias is registered; false otherwise
     * @apiNote If {@code alias} contains {@link net.kyori.adventure.key.Key#DEFAULT_SEPARATOR},
     * it is treated as a namespaced {@code namespace:alias} lookup;
     * otherwise it is treated as a plain-alias lookup
     * @since 0.1.0
     */
    boolean hasCommand(String alias);

    /**
     * Returns whether the given alias is registered on this manager
     * and can be used by the given {@link CommandSource}.
     * See {@link com.mojang.brigadier.builder.ArgumentBuilder#requires(Predicate)}
     *
     * @param alias  the command alias to check
     * @param source the command source
     * @return true if the alias is registered and usable; false otherwise
     * @since 0.1.0
     */
    boolean hasCommand(String alias, CommandSource source);
}
