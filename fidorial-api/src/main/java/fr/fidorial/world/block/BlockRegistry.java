package fr.fidorial.world.block;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Every block type the server knows, vanilla and plugin-defined.
 *
 * @see Blocks#registry()
 * @since 0.1.0
 */
public interface BlockRegistry {

    /**
     * {@return the block type registered under a key, if any}
     *
     * @param key the block key
     * @since 0.1.0
     */
    Optional<BlockType> type(Key key);

    /**
     * {@return the block type registered under a key, if any}
     *
     * @param key the block key, for instance {@code minecraft:stone}
     * @since 0.1.0
     */
    default Optional<BlockType> type(@KeyPattern final String key) {
        return type(Key.key(key));
    }

    /**
     * {@return the state with the given network identifier, or {@code null} if it is unknown}
     *
     * @param networkId the network identifier
     * @since 0.1.0
     */
    @Nullable BlockData fromNetworkId(int networkId);

    /**
     * Registers a block type.
     *
     * @param type the block type
     * @since 0.1.0
     */
    void register(BlockType type);

    /**
     * Registers the block type a behaviour drives.
     *
     * @param behaviour the behaviour
     * @since 0.1.0
     */
    default void register(final BlockBehaviour behaviour) {
        register(behaviour.type());
    }

    /**
     * {@return the behaviour driving a block type, if any}
     *
     * @param key the block key
     * @since 0.1.0
     */
    default Optional<BlockBehaviour> behaviour(final Key key) {
        return Optional.empty();
    }

    /**
     * {@return the behaviour driving the type of a state, if any}
     *
     * @param data the state
     * @since 0.1.0
     */
    default Optional<BlockBehaviour> behaviour(final BlockData data) {
        return behaviour(data.key());
    }

    /**
     * {@return every registered block type}
     *
     * @since 0.1.0
     */
    Collection<BlockType> types();

    /**
     * Parses a state in command syntax, for instance {@code minecraft:oak_stairs[facing=north]}.
     *
     * @param input the state to parse
     * @return the state, or {@code null} if the block is unknown
     * @throws IllegalArgumentException if the syntax is malformed or a property is unknown
     * @since 0.1.0
     */
    @SuppressWarnings("PatternValidation")
    default @Nullable BlockData parse(final String input) {
        String name = input;
        Map<String, String> values = Map.of();
        final int bracket = input.indexOf('[');
        if (bracket >= 0) {
            if (!input.endsWith("]")) {
                throw new IllegalArgumentException("Missing closing ']' in '" + input + "'");
            }
            name = input.substring(0, bracket);
            values = new LinkedHashMap<>();
            final String body = input.substring(bracket + 1, input.length() - 1);
            if (!body.isEmpty()) {
                for (final String pair : body.split(",")) {
                    final int eq = pair.indexOf('=');
                    if (eq < 0) {
                        throw new IllegalArgumentException("Invalid property '" + pair + "' in '" + input + "'");
                    }
                    values.put(pair.substring(0, eq).trim(), pair.substring(eq + 1).trim());
                }
            }
        }
        final BlockType type = type(name).orElse(null);
        return type == null ? null : type.data(values);
    }
}
