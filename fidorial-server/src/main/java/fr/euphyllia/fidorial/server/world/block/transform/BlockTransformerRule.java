package fr.euphyllia.fidorial.server.world.block.transform;

import fr.euphyllia.fidorial.server.world.block.interaction.FidorialBlockInteractionContext;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.world.BlockFace;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

public record BlockTransformerRule(StateProvider provider,
                                   @Nullable Sound sound,
                                   Set<BlockFace> disallowedFaces) {

    public BlockTransformerRule {
        Objects.requireNonNull(provider, "provider");
        disallowedFaces = Set.copyOf(Objects.requireNonNull(disallowedFaces, "disallowedFaces"));
    }

    @FunctionalInterface
    public interface StateProvider {
        @Nullable BlockState provide(final FidorialBlockInteractionContext context);
    }
}
