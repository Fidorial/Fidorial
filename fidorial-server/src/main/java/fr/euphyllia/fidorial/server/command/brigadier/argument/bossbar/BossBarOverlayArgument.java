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

public final class BossBarOverlayArgument {

    private static final DynamicCommandExceptionType ERROR_INVALID_VALUE = ExceptionFactory.dynamic("argument.enum.invalid");

    private BossBarOverlayArgument() {
    }

    public static ArgumentType<BossBar.Overlay> bossBarOverlay() {
        return Arguments.arguments().map(StringArgumentType.word(), BossBarOverlayArgument::parse, BossBarOverlayArgument::suggest);
    }

    private static BossBar.Overlay parse(final String value, final StringReader reader) throws CommandSyntaxException {
        for (final BossBar.Overlay overlay : BossBar.Overlay.values()) {
            if (overlay.name().equalsIgnoreCase(value)) return overlay;
        }
        throw ERROR_INVALID_VALUE.createWithContext(reader, value);
    }

    private static <S> CompletableFuture<Suggestions> suggest(final CommandContext<S> ctx, final SuggestionsBuilder builder) {
        for (final BossBar.Overlay overlay : BossBar.Overlay.values()) {
            builder.suggest(overlay.name().toLowerCase(Locale.ROOT));
        }
        return builder.buildFuture();
    }
}
