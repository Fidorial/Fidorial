package fr.fidorial.world.block;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import org.jspecify.annotations.Nullable;

/**
 * Static access to the {@link BlockRegistry}, bootstrapped by the server at startup.
 *
 * @since 0.1.0
 */
public final class Blocks {

    private static volatile @Nullable BlockRegistry registry;

    private Blocks() {
    }

    /**
     * Installs the registry. Called once by the server.
     *
     * @param blockRegistry the registry
     * @throws IllegalStateException if a registry is already installed
     * @since 0.1.0
     */
    public static void bootstrap(final BlockRegistry blockRegistry) {
        if (registry != null) {
            throw new IllegalStateException("Block registry already bootstrapped");
        }
        registry = blockRegistry;
    }

    /**
     * {@return the installed registry}
     *
     * @throws IllegalStateException if the server has not bootstrapped it yet
     * @since 0.1.0
     */
    public static BlockRegistry registry() {
        final BlockRegistry current = registry;
        if (current == null) {
            throw new IllegalStateException("Block registry not bootstrapped yet");
        }
        return current;
    }

    /**
     * {@return the block type registered under a key, or {@code null}}
     *
     * @param key the block key, for instance {@code minecraft:stone}
     * @since 0.1.0
     */
    public static @Nullable BlockType type(@KeyPattern final String key) {
        return registry().type(key).orElse(null);
    }

    /**
     * {@return the block type registered under a key, or {@code null}}
     *
     * @param key the block key
     * @since 0.1.0
     */
    public static @Nullable BlockType type(final Key key) {
        return registry().type(key).orElse(null);
    }

    /**
     * Parses a state in command syntax.
     *
     * @param input the state to parse, for instance {@code minecraft:oak_stairs[facing=north]}
     * @return the state, or {@code null} if the block is unknown
     * @throws IllegalArgumentException if the syntax is malformed
     * @since 0.1.0
     */
    public static @Nullable BlockData data(final String input) {
        return registry().parse(input);
    }
}
