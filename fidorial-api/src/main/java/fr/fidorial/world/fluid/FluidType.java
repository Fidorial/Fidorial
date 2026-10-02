package fr.fidorial.world.fluid;

import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

/**
 * The fluids the server simulates, with their spreading rules.
 *
 * @since 0.1.0
 */
public enum FluidType {
    /**
     * Water: fast, and two adjacent sources form a new one.
     */
    WATER(Key.key("water"), 5, 1, 7, true),
    /**
     * Lava: slower than water, and spreading less far.
     */
    LAVA(Key.key("lava"), 30, 2, 6, false);

    private final Key blockKey;
    private final int tickDelay;
    private final int dropOff;
    private final int maxSpreadLevel;
    private final boolean canFormSources;

    FluidType(final Key blockKey, final int tickDelay, final int dropOff, final int maxSpreadLevel, final boolean canFormSources) {
        this.blockKey = blockKey;
        this.tickDelay = tickDelay;
        this.dropOff = dropOff;
        this.maxSpreadLevel = maxSpreadLevel;
        this.canFormSources = canFormSources;
    }

    /**
     * {@return the fluid a block is made of, or {@code null} if it is not a fluid block}
     *
     * @param key the block key
     * @since 0.1.0
     */
    public static @Nullable FluidType byBlockKey(final Key key) {
        for (final FluidType type : values()) {
            if (type.blockKey.equals(key)) {
                return type;
            }
        }
        return null;
    }

    /**
     * {@return the key of the block this fluid is placed as}
     *
     * @since 0.1.0
     */
    public Key blockKey() {
        return blockKey;
    }

    /**
     * {@return the number of ticks between two spreading steps}
     *
     * @since 0.1.0
     */
    public int tickDelay() {
        return tickDelay;
    }

    /**
     * {@return how much the level decreases per block of horizontal spread}
     *
     * @since 0.1.0
     */
    public int dropOff() {
        return dropOff;
    }

    /**
     * {@return the highest level a flowing block of this fluid can reach}
     *
     * @since 0.1.0
     */
    public int maxSpreadLevel() {
        return maxSpreadLevel;
    }

    /**
     * {@return {@code true} if two adjacent sources turn the block between them into a source}
     *
     * @since 0.1.0
     */
    public boolean canFormSources() {
        return canFormSources;
    }
}
