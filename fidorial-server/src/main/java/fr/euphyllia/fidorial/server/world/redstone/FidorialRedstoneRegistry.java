package fr.euphyllia.fidorial.server.world.redstone;

import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneRegistry;

import java.util.Optional;
import java.util.Set;

public final class FidorialRedstoneRegistry implements RedstoneRegistry {
    @Override
    public void attach(final TypedKey<BlockType> block, final RedstoneBehaviour behaviour, final Plugin owner) {

    }

    @Override
    public boolean detach(final TypedKey<BlockType> block, final Plugin owner) {
        return false;
    }

    @Override
    public void detachAll(final Plugin owner) {

    }

    @Override
    public Optional<RedstoneBehaviour> behaviour(final TypedKey<BlockType> block) {
        return Optional.empty();
    }

    @Override
    public Set<TypedKey<BlockType>> blocks() {
        return Set.of();
    }
}
