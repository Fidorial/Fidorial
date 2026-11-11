package fr.euphyllia.fidorial.server.network.protocol.catalog;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.kyori.adventure.key.Key;

/**
 * Network IDs for the {@code handshake/serverbound} state/direction.
 *
 * <p>Generated from Mojang's packets report; do not edit.</p>
 */
public interface HandshakeServerboundPacketIds {
    /**
     * Returned by {@link #id(Key)} when the identifier is unknown.
     */
    int UNKNOWN = -1;

    /**
     * {@code minecraft:intention}
     */
    int INTENTION_ID = 0;

    /**
     * Identifier to protocol ID lookup table.
     */
    Object2IntMap<Key> BY_IDENTIFIER = new Object2IntOpenHashMap<>(
        new Key[] {
            Key.key("intention")
        },
        new int[] {
            INTENTION_ID
        }
    );

    /**
     * Resolves the protocol ID for a namespaced identifier.
     *
     * @param identifier namespaced identifier, e.g. {@code Key.key("minecraft", "chest")}
     * @return the protocol ID, or {@link #UNKNOWN} when the identifier is unknown
     */
    static int id(final Key identifier) {
        return BY_IDENTIFIER.getOrDefault(identifier, UNKNOWN);
    }
}
