package fr.euphyllia.fidorial.server.command.brigadier.argument.resource;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.KeyReader;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.fidorial.registry.Registry;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import net.kyori.adventure.key.Key;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class ResourceKeyArgument<T> implements ArgumentType<TypedKey<T>> {

    private final RegistryKey<T> registryKey;

    private ResourceKeyArgument(final RegistryKey<T> registryKey) {
        this.registryKey = registryKey;
    }

    public static <T> ResourceKeyArgument<T> resourceKey(final RegistryKey<T> registryKey) {
        return new ResourceKeyArgument<>(registryKey);
    }

    @Override
    public TypedKey<T> parse(final StringReader reader) throws CommandSyntaxException {
        return TypedKey.create(registryKey, KeyReader.readKey(reader));
    }

    private static final String SPLITTERS = "._/";

    private static boolean matchesSubStr(final String pattern, final String input) {
        int index = 0;

        while (!input.startsWith(pattern, index)) {
            int next = -1;

            for (final char c : SPLITTERS.toCharArray()) {
                final int i = input.indexOf(c, index);

                if (i != -1 && (next == -1 || i < next)) {
                    next = i;
                }
            }

            if (next < 0) {
                return false;
            }

            index = next + 1;
        }

        return true;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        final Registry<T> registry = registry();

        final String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

        final boolean hasNamespace = remaining.indexOf(':') >= 0;

        for (final TypedKey<T> typedKey : registry.keys()) {
            final Key key = typedKey.key();

            final String namespace = key.namespace();
            final String path = key.value();

            final String full = key.asString();

            if (hasNamespace) {
                if (matchesSubStr(remaining, full)) {
                    builder.suggest(full);
                }
            } else {
                if (matchesSubStr(remaining, namespace) || matchesSubStr(remaining, path)) {
                    builder.suggest(full);
                }
            }
        }

        return builder.buildFuture();
    }

    private Registry<T> registry() {
        return FidorialServer.getInstance().registries().registry(registryKey);
    }

    public RegistryKey<T> registryKey() {
        return registryKey;
    }

    public static final class Info<T> implements ArgumentTypeRegistrar<ResourceKeyArgument<T>, Info<T>.Spec> {

        @Override
        public Spec access(final ResourceKeyArgument<T> argument) {
            return new Spec(argument.registryKey());
        }

        @Override
        public void serialize(final Spec spec, final PacketBuffer buf) {
            buf.writeRegistryKey(spec.registryKey);
        }

        @Override
        public Spec deserialize(final PacketBuffer buf) {
            final Key key = buf.readKey();
            return new Spec(RegistryKey.of(key));
        }

        @Override
        public void serializeJson(final Spec spec, final JsonObject value) {
            value.addProperty("registry", spec.registryKey.key().asString());
        }

        public final class Spec implements ArgumentTypeRegistrar.Spec<ResourceKeyArgument<T>> {

            private final RegistryKey<T> registryKey;

            public Spec(final RegistryKey<T> registryKey) {
                this.registryKey = registryKey;
            }

            @Override
            public ResourceKeyArgument<T> instantiate() {
                return ResourceKeyArgument.resourceKey(registryKey);
            }

            @Override
            public ArgumentTypeRegistrar<ResourceKeyArgument<T>, ?> type() {
                return Info.this;
            }
        }
    }
}
