package fr.fidorial.world.block;

import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

/**
 * The server-side logic of a block type: how it is placed and how it interacts with light.
 *
 * @since 0.1.0
 */
public interface BlockBehaviour {

    /**
     * {@return the block type this behaviour drives}
     *
     * @since 0.1.0
     */
    BlockType type();

    /**
     * {@return the key of the block type this behaviour drives}
     *
     * @since 0.1.0
     */
    default Key key() {
        return type().key();
    }

    /**
     * Chooses the state a player places, for instance facing the player.
     *
     * @param context where and how the block is being placed
     * @return the state to place, or {@code null} to refuse the placement
     * @since 0.1.0
     */
    default @Nullable BlockData placementState(final BlockPlaceContext context) {
        return type().defaultData();
    }

    /**
     * {@return the light level the state emits, from {@code 0} to {@code 15}}
     *
     * @param data the state
     * @since 0.1.0
     */
    default int lightEmission(final BlockData data) {
        return 0;
    }

    /**
     * {@return how much light the state absorbs, from {@code 0} (transparent) to {@code 15} (opaque)}
     *
     * @param data the state
     * @since 0.1.0
     */
    default int lightOpacity(final BlockData data) {
        return 15;
    }
}
