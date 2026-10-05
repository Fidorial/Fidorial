package fr.euphyllia.fidorial.server.command.brigadier.argument.custom;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import fr.fidorial.command.CommandSource;
import org.jspecify.annotations.Nullable;

public sealed interface ServerSuggestions permits ServerSuggestionsArgumentType, MappedArgumentType {
    @Nullable SuggestionProvider<CommandSource> suggestionProvider();
}
