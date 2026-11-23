package fr.euphyllia.fidorial.server.world.fluid;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Fluid;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.FluidKeys;
import fr.fidorial.world.block.BlockType;
import org.jspecify.annotations.Nullable;

/**
 * The fluids the server simulates, with the block each one is placed as.
 */
public enum FluidType {

    WATER(BlockTypeKeys.WATER, FluidKeys.WATER),
    LAVA(BlockTypeKeys.LAVA, FluidKeys.LAVA);

    private final TypedKey<BlockType> blockKey;
    private final TypedKey<Fluid> fluidKey;

    FluidType(final TypedKey<BlockType> blockKey, final TypedKey<Fluid> fluidKey) {
        this.blockKey = blockKey;
        this.fluidKey = fluidKey;
    }

    /**
     * {@return the fluid a block is made of, {@code null} if it is not a fluid block}
     */
    public static @Nullable FluidType byBlockKey(final TypedKey<BlockType> key) {
        for (final FluidType type : values()) {
            if (type.blockKey.equals(key)) {
                return type;
            }
        }
        return null;
    }

    /**
     * {@return the fluid registered under a key, {@code null} for any other one}
     */
    public static @Nullable FluidType byFluidKey(final TypedKey<Fluid> key) {
        for (final FluidType type : values()) {
            if (type.fluidKey.equals(key)) {
                return type;
            }
        }
        return null;
    }

    /**
     * {@return the key of the block this fluid is placed as}
     */
    public TypedKey<BlockType> blockKey() {
        return blockKey;
    }

    /**
     * {@return the key of this fluid in the {@code minecraft:fluid} registry}
     */
    public TypedKey<Fluid> fluidKey() {
        return fluidKey;
    }
}
