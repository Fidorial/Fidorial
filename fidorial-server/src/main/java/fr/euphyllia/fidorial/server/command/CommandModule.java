package fr.euphyllia.fidorial.server.command;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.adventure.ClickCallbackManager;
import fr.euphyllia.fidorial.server.console.command.ConsoleCommandReader;
import fr.euphyllia.fidorial.server.lifecycle.Quietly;
import fr.euphyllia.fidorial.server.lifecycle.ServerModule;
import fr.euphyllia.fidorial.server.lifecycle.ServiceRegistrar;
import fr.euphyllia.fidorial.server.registry.RegistryModule;
import fr.fidorial.command.CommandRegistry;
import fr.fidorial.command.CommandSource;
import fr.fidorial.service.ServiceRegistry;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Commands, the console and the click callbacks of chat components.
 *
 * <p>This is the server's {@link CommandRegistry}, backed by {@link #manager()}.</p>
 */
public final class CommandModule implements ServerModule, CommandRegistry {

    private final boolean headless;
    private final FidorialServer server;
    private final CommandManager manager = new CommandManager();
    private final ClickCallbackManager clickCallbacks = new ClickCallbackManager();
    private final ConsoleSender console;

    /**
     * @param headless whether the interactive console must not be started
     */
    public CommandModule(
            final FidorialServer server, final boolean headless,
            final RegistryModule registries, final ServiceRegistry services) {
        this.server = server;
        this.headless = headless;
        this.console = new ConsoleSender(server, registries.permissions(), services);
    }

    @Override
    public void registerServices(final ServiceRegistrar services) {
        services.provide(CommandRegistry.class, manager);
    }

    @Override
    public void start() {
        if (!headless) {
            console.setLocale(Locale.getDefault());
            new ConsoleCommandReader(manager, server::isRunning).start();
        }
    }

    @Override
    public void stop() {
        Quietly.run("commands", manager::shutdown);
        Quietly.run("click callbacks", clickCallbacks::close);
    }

    public CommandManager manager() {
        return manager;
    }

    public ConsoleSender console() {
        return console;
    }

    public ClickCallbackManager clickCallbacks() {
        return clickCallbacks;
    }

    // ---- CommandRegistry API, backed by the command manager

    @Override
    public void register(final String namespace, final LiteralCommandNode<CommandSource> command, final Set<String> aliases) {
        manager.register(namespace, command, aliases);
    }

    @Override
    public void unregister(final String namespace, final String alias) {
        manager.unregister(namespace, alias);
    }

    @Override
    public void unregisterNamespace(final String namespace) {
        manager.unregisterNamespace(namespace);
    }

    @Override
    public CompletableFuture<CommandResult> dispatchAsyncResult(final CommandSource source, final String cmdLine) {
        return manager.dispatchAsyncResult(source, cmdLine);
    }

    @Override
    public CompletableFuture<Suggestions> offerSuggestions(final CommandSource source, final String cmdLine) {
        return manager.offerSuggestions(source, cmdLine);
    }

    @Override
    public boolean hasCommand(final String alias) {
        return manager.hasCommand(alias);
    }

    @Override
    public boolean hasCommand(final String alias, final CommandSource source) {
        return manager.hasCommand(alias, source);
    }
}
