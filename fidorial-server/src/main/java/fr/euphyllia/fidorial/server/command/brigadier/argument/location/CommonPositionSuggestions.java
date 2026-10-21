package fr.euphyllia.fidorial.server.command.brigadier.argument.location;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class CommonPositionSuggestions {

    private CommonPositionSuggestions() {
    }

    public static CompletableFuture<Suggestions> suggest(
            final SuggestionsBuilder builder,
            final ArgumentType<?> argument,
            final int axes
    ) {
        final String remaining = builder.getRemaining();
        final String[] typed = remaining.isBlank() ? new String[0] : remaining.trim().split(" ");
        if (typed.length >= axes) {
            return builder.buildFuture();
        }

        final String filler = remaining.startsWith("^") ? "^" : "~";
        final StringBuilder candidate = new StringBuilder(String.join(" ", typed));
        final List<String> completions = new ArrayList<>();
        for (int axis = typed.length; axis < axes; axis++) {
            if (!candidate.isEmpty()) {
                candidate.append(' ');
            }
            completions.add(candidate.append(filler).toString());
        }

        if (parses(argument, completions.getLast())) {
            completions.stream()
                    .filter(completion -> completion.startsWith(remaining))
                    .forEach(builder::suggest);
        }
        return builder.buildFuture();
    }

    private static boolean parses(final ArgumentType<?> argument, final String input) {
        final StringReader reader = new StringReader(input);
        try {
            argument.parse(reader);
            return !reader.canRead();
        } catch (final CommandSyntaxException _) {
            return false;
        }
    }
}
