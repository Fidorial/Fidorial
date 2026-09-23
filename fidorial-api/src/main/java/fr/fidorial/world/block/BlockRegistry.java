package fr.fidorial.world.block;

import fr.fidorial.plugin.Plugin;
import fr.fidorial.world.block.plant.CropBlock;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public interface BlockRegistry {

    Optional<BlockType> type(Key key);

    default Optional<BlockType> type(@KeyPattern final String key) {
        return type(Key.key(key));
    }

    @Nullable BlockData fromNetworkId(int networkId);

    void register(BlockType type);

    default void register(final BlockBehaviour behaviour) {
        register(behaviour.type());
    }

    default Optional<BlockBehaviour> behaviour(final Key key) {
        return Optional.empty();
    }

    default Optional<BlockBehaviour> behaviour(final BlockData data) {
        return behaviour(data.key());
    }

    /**
     * Gives a block type its behaviour. The type is registered first when it is
     * not known yet (a plugin block); when it is, the behaviour replaces the current
     * one until {@code owner} is disabled, then the previous one comes back.
     *
     * @param behaviour the behaviour to attach
     * @param owner     the plugin attaching it
     * @since 0.1.0
     */
    void register(BlockBehaviour behaviour, Plugin owner);

    /**
     * Starts a crop growing as {@code block}. Hand the result to
     * {@link #register(BlockBehaviour, Plugin)}.
     *
     * @param block a block type with an {@value CropBlock#AGE} property, registered beforehand
     * @return a builder for that crop
     * @since 0.1.0
     */
    CropBlock.Builder crop(Key block);

    /**
     * Declares that using an item places a block — seeds planting their crop,
     * cocoa beans their pod, a lily pad itself. Only needed when the item and the
     * block do not share an identifier.
     *
     * @param item  the item in hand
     * @param block the block it places
     * @param owner the plugin declaring it
     * @since 0.1.0
     */
    void registerBlockItem(Key item, Key block, Plugin owner);

    /**
     * @param item an item identifier
     * @return the block that item places when it was {@linkplain #registerBlockItem declared}
     * @since 0.1.0
     */
    Optional<Key> blockForItem(Key item);

    /**
     * Drops every behaviour and block item an owner declared, bringing back
     * whatever they shadowed.
     *
     * @param owner the plugin to clean up after
     * @since 0.1.0
     */
    void unregisterAll(Plugin owner);

    Collection<BlockType> types();

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
