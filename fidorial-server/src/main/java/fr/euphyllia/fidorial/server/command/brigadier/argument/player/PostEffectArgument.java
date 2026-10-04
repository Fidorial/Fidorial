package fr.euphyllia.fidorial.server.command.brigadier.argument.player;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.fidorial.command.CommandSource;
import net.kyori.adventure.key.Key;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class PostEffectArgument {

    public static final Key SUGGESTION_SOURCE = Key.key("post_effects");

    // https://minecraft.wiki/w/Shader#List_of_post-processing_effects
    private static final List<Key> DEFAULT = Stream.of("blur", "creeper", "entity_outline", "invert", "spider")
            .map(Key::key)
            .toList();

    private PostEffectArgument() {
    }

    public static CompletableFuture<Suggestions> suggestBuiltin(final CommandContext<CommandSource> context, final SuggestionsBuilder builder) {
        final String remaining = builder.getRemainingLowerCase();
        for (final Key effect : DEFAULT) {
            if (effect.asString().startsWith(remaining) || effect.value().startsWith(remaining)) {
                builder.suggest(effect.asString());
            }
        }
        return builder.buildFuture();
    }
}
