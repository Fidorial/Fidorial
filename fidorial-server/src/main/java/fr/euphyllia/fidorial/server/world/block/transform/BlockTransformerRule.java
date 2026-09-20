package fr.euphyllia.fidorial.server.world.block.transform;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.world.block.interaction.FidorialBlockInteractionContext;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.world.BlockFace;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

public record BlockTransformerRule(Set<Key> from,
                                   StateProvider provider,
                                   @Nullable Sound sound,
                                   Set<BlockFace> disallowedFaces) {

    public BlockTransformerRule {
        Objects.requireNonNull(from, "from cannot be null");
        Objects.requireNonNull(provider, "provider cannot be null");
        Objects.requireNonNull(disallowedFaces, "disallowedFaces cannot be null");
        from = Set.copyOf(from);
        disallowedFaces = Set.copyOf(disallowedFaces);
        Preconditions.checkArgument(!from.isEmpty(),
                "a transformer rule needs at least one source block, otherwise nothing ever reaches it");
    }

    public boolean accepts(final Key block, final BlockFace face) {
        return from.contains(block) && !disallowedFaces.contains(face);
    }

    @FunctionalInterface
    public interface StateProvider {
        @Nullable BlockState provide(final FidorialBlockInteractionContext context);
    }
}
