package fr.euphyllia.fidorial.server.world.structure.processor;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jspecify.annotations.Nullable;

public record PlacedBlock(int x, int y, int z, BlockState state, @Nullable CompoundBinaryTag nbt) {

    public PlacedBlock withState(final BlockState newState) {
        return new PlacedBlock(x, y, z, newState, nbt);
    }

    public PlacedBlock withState(final BlockState newState, final @Nullable CompoundBinaryTag newNbt) {
        return new PlacedBlock(x, y, z, newState, newNbt);
    }

    public PlacedBlock withY(final int newY) {
        return new PlacedBlock(x, newY, z, state, nbt);
    }
}
