/*
 * This file is part of spark.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.euphyllia.fidorial.server.command.spark;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.spark.SparkCommandSender;
import fr.fidorial.command.CommandSource;
import me.lucko.spark.common.SparkPlatform;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;
import static fr.fidorial.command.argument.Arguments.arguments;

/**
 * Bridges spark's flat {@code String[]} command model onto Fidorial's Brigadier dispatcher.
 *
 * <p>spark parses its own arguments, so the whole tail of the command line is captured as a single
 * pattern argument matching the rest of the line and split back into tokens before being handed to
 * {@link SparkPlatform#executeCommand}. Suggestions are re-anchored onto the last token so the
 * client highlights the right span.</p>
 */
public final class SparkCommand implements SuggestionProvider<CommandSource> {

    private static final Pattern REST_OF_LINE = Pattern.compile(".*");

    private final SparkPlatform platform;
    private final Executor asyncExecutor;

    private SparkCommand(final SparkPlatform platform, final Executor asyncExecutor) {
        this.platform = platform;
        this.asyncExecutor = asyncExecutor;
    }

    /**
     * Builds the {@code /spark} command node.
     *
     * @param platform      the spark platform to dispatch to
     * @param name          the root literal, i.e. {@code spark}
     * @param asyncExecutor executor used to compute tab completions off the calling thread
     * @return the command node, ready to be passed to {@code CommandManager#registerInternal(...)}
     */
    public static LiteralCommandNode<CommandSource> create(
            final SparkPlatform platform, final String name, final Executor asyncExecutor) {
        final SparkCommand command = new SparkCommand(platform, asyncExecutor);

        return literal(name)
                .requires(command::canUse)
                // bare "/spark" -> spark prints its own usage
                .executes(ctx -> command.execute(ctx, new String[0]))
                // "/spark <anything else>"
                .then(argument("args", arguments().pattern(REST_OF_LINE, null))
                        .suggests(command)
                        .executes(ctx -> command.execute(ctx, splitInput(ctx, false))))
                .build();
    }

    private boolean canUse(final CommandSource source) {
        return this.platform.hasPermissionForAnyCommand(new SparkCommandSender(source.sender()));
    }

    private int execute(final CommandContext<CommandSource> ctx, final String @Nullable [] args) {
        if (args == null) {
            return 0;
        }

        // SparkPlatform#executeCommand dispatches onto SparkPlugin#executeAsync itself,
        // so this returns immediately and never blocks a region thread.
        this.platform.executeCommand(new SparkCommandSender(ctx.getSource().sender()), args);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(
            final CommandContext<CommandSource> ctx, final SuggestionsBuilder builder) {
        final String[] args = splitInput(ctx, true);
        if (args == null) {
            return Suggestions.empty();
        }

        // spark returns candidates for the *last* token only, so the builder has to be
        // re-anchored there — otherwise the client replaces the entire greedy argument.
        final int lastSpace = builder.getRemaining().lastIndexOf(' ');
        final SuggestionsBuilder anchored =
                lastSpace == -1 ? builder : builder.createOffset(builder.getStart() + lastSpace + 1);

        final SparkCommandSender sender = new SparkCommandSender(ctx.getSource().sender());

        return CompletableFuture.supplyAsync(
                () -> {
                    final List<String> completions = this.platform.tabCompleteCommand(sender, args);
                    for (final String completion : completions) {
                        anchored.suggest(completion);
                    }
                    return anchored.build();
                },
                this.asyncExecutor);
    }

    /**
     * Splits the raw command line into the token array spark expects, dropping the root literal.
     *
     * @param tabComplete when {@code true}, a trailing empty token is preserved so spark knows the
     *                    user has started a new argument
     * @return the arguments, or {@code null} if the input could not be parsed
     */
    private static String @Nullable [] splitInput(
            final CommandContext<CommandSource> ctx, final boolean tabComplete) {
        String input = ctx.getInput();
        if (input.startsWith("/")) {
            input = input.substring(1);
        }

        final String[] split = input.split(" ", tabComplete ? -1 : 0);
        if (split.length == 0) {
            return null;
        }

        return Arrays.stream(split, 1, split.length).toArray(String[]::new);
    }
}
