package fr.euphyllia.fidorial.server.command.brigadier.argument.custom;

import net.kyori.adventure.key.Key;

public sealed interface ClientSuggestions permits ClientSuggestionsArgumentType {
    Key suggestionSource();
}
