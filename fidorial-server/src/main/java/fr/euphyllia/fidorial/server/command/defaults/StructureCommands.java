package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.structure.StructureService;
import fr.fidorial.command.CommandSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.Location;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Helpers shared by {@code /place}, {@code /locate} and {@code /datapack}.
 */
final class StructureCommands {

    private StructureCommands() {
        throw new UnsupportedOperationException("StructureCommands cannot be instantiated.");
    }

    static StructureService service() {
        return FidorialServer.getInstance().structures();
    }

    static SuggestionProvider<CommandSource> keys(final Supplier<Collection<Key>> keys) {
        return (context, builder) -> suggest(keys.get(), builder);
    }

    private static CompletableFuture<Suggestions> suggest(final Collection<Key> keys, final SuggestionsBuilder builder) {
        final String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (final Key key : keys) {
            final String full = key.asString();
            if (full.startsWith(remaining) || key.value().startsWith(remaining)
                    || (!remaining.contains(":") && key.value().contains("/" + remaining))) {
                builder.suggest(full);
            }
        }
        return builder.buildFuture();
    }

    static @Nullable ServerWorld worldOf(final CommandContext<CommandSource> context) {
        if (context.getSource().sender() instanceof final ServerPlayer player
                && player.world() instanceof final ServerWorld world) {
            return world;
        }
        final Entity executor = context.getSource().executor();
        if (executor != null && executor.world() instanceof final ServerWorld world) {
            return world;
        }
        return null;
    }

    static BlockPos here(final CommandContext<CommandSource> context) {
        final Location location = context.getSource().location();
        return new BlockPos((int) Math.floor(location.x()), (int) Math.floor(location.y()), (int) Math.floor(location.z()));
    }

    static String rootMessage(final Throwable failure) {
        Throwable cause = failure;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
    }
}
