package fr.euphyllia.fidorial.server.command.brigadier.argument.bossbar;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.fidorial.command.argument.Arguments;
import net.kyori.adventure.bossbar.BossBar;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class BossBarFlagArgument {

    private static final DynamicCommandExceptionType ERROR_INVALID_VALUE = ExceptionFactory.dynamic("argument.enum.invalid");

    private BossBarFlagArgument() {
    }

    public static ArgumentType<BossBar.Flag> bossBarFlag() {
        return Arguments.arguments().map(StringArgumentType.word(), BossBarFlagArgument::parse, BossBarFlagArgument::suggest);
    }

    private static BossBar.Flag parse(final String value, final StringReader reader) throws CommandSyntaxException {
        for (final BossBar.Flag flag : BossBar.Flag.values()) {
            if (flag.name().equalsIgnoreCase(value)) return flag;
        }
        throw ERROR_INVALID_VALUE.createWithContext(reader, value);
    }

    private static <S> CompletableFuture<Suggestions> suggest(final CommandContext<S> ctx, final SuggestionsBuilder builder) {
        for (final BossBar.Flag flag : BossBar.Flag.values()) {
            builder.suggest(flag.name().toLowerCase(Locale.ROOT));
        }
        return builder.buildFuture();
    }
}
