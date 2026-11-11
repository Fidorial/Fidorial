package fr.euphyllia.fidorial.server.command.brigadier.argument.resource;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.fidorial.command.CommandSource;
import fr.fidorial.registry.Registry;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import net.kyori.adventure.key.Key;

import java.util.concurrent.CompletableFuture;

public final class RegistryKeySuggestions {

    private static final String MINECRAFT_PREFIX = Key.MINECRAFT_NAMESPACE + Key.DEFAULT_SEPARATOR;

    private RegistryKeySuggestions() {
    }

    public static <T> SuggestionProvider<CommandSource> registryKey(final RegistryKey<T> registryKey) {
        return (_, builder) -> suggest(FidorialServer.getInstance().registries().registry(registryKey), builder);
    }

    public static <T> CompletableFuture<Suggestions> suggest(final Registry<T> registry, final SuggestionsBuilder builder) {
        final String remaining = builder.getRemainingLowerCase();
        final boolean hasNamespace = remaining.indexOf(Key.DEFAULT_SEPARATOR) >= 0;

        for (final TypedKey<T> key : registry.keys()) {
            final String full = key.key().asString();
            final String shown = hasNamespace || !full.startsWith(MINECRAFT_PREFIX) ? full : full.substring(MINECRAFT_PREFIX.length());

            if (shown.contains(remaining)) {
                builder.suggest(full);
            }
        }

        return builder.buildFuture();
    }
}
