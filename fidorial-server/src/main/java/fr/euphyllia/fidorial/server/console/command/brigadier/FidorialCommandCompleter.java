package fr.euphyllia.fidorial.server.console.command.brigadier;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import fr.euphyllia.fidorial.server.command.CommandManager;
import fr.fidorial.command.CommandSource;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public final class FidorialCommandCompleter implements Completer {

    private final CommandManager commandManager;
    private final Supplier<CommandSource> sourceFactory;
    private final CandidateFactory candidateFactory;

    public FidorialCommandCompleter(final CommandManager commandManager, final Supplier<CommandSource> sourceFactory) {
        this(commandManager, sourceFactory, FidorialCommandCompleter::adventureTooltip);
    }

    public FidorialCommandCompleter(
            final CommandManager commandManager,
            final Supplier<CommandSource> sourceFactory,
            final CandidateFactory candidateFactory
    ) {
        this.commandManager = commandManager;
        this.sourceFactory = sourceFactory;
        this.candidateFactory = candidateFactory;
    }

    @Override
    public void complete(final LineReader reader, final ParsedLine line, final List<Candidate> candidates) {
        final Suggestions suggestions = resolveSuggestions(line);
        final int replaceFrom = suggestions.getRange().getStart();

        for (final Suggestion suggestion : suggestions.getList()) {
            if (suggestion.getText().isEmpty()) {
                continue;
            }
            final String rangeGap = line.line().substring(replaceFrom, suggestion.getRange().getStart());
            final String value = rangeGap + suggestion.getText();
            candidates.add(this.candidateFactory.create(value, suggestion.getTooltip()));
        }
    }

    private Suggestions resolveSuggestions(final ParsedLine line) {
        final ParseResults<CommandSource> parseResults = this.commandManager.parse(new StringReader(line.line()), this.sourceFactory.get());
        return this.commandManager.completionSuggestions(parseResults, line.cursor()).join();
    }

    private static Candidate adventureTooltip(final String value, final @Nullable Message tooltip) {
        if (tooltip == null) {
            return new Candidate(value, value, null, null, null, null, false);
        }
        final String description = tooltip.getString();
        return new Candidate(value, value, null, description, null, null, false);
    }

    @FunctionalInterface
    public interface CandidateFactory {
        Candidate create(final String value, final Message tooltip);
    }
}
