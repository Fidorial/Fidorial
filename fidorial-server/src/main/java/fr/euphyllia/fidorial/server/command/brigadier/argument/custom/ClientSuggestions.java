package fr.euphyllia.fidorial.server.command.brigadier.argument.custom;

import net.kyori.adventure.key.Key;

import java.util.List;

public sealed interface ClientSuggestions permits ClientSuggestionsArgumentType {
    /// The `suggestionSource` that the client should use to resolve suggestions for this argument.
    Key suggestionSource();

    /// The values sent along with [#suggestionSource()], empty for most sources
    List<String> suggestionValues();
}
