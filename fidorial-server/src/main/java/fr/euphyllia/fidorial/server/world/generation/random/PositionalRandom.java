package fr.euphyllia.fidorial.server.world.generation.random;

import net.kyori.adventure.key.Key;

/**
 * Hands out streams tied to a block position or to a name, all derived from one 128-bit seed. Asking twice for
 * the same position or the same name gives two streams that produce the same values, whatever was asked in
 * between: generation does not depend on the order chunks are made in.
 *
 * <p>Immutable, safe to share between threads; the streams it returns are not.</p>
 *
 * @param low  the low half of the seed
 * @param high the high half of the seed
 */
public record PositionalRandom(long low, long high) {

    /**
     * {@return a fresh stream for a block position}
     */
    public XoroshiroSource at(final int x, final int y, final int z) {
        return new XoroshiroSource(Seeds.ofPosition(x, y, z) ^ low, high);
    }

    /**
     * {@return a fresh stream for a namespaced identifier}
     */
    public XoroshiroSource named(final Key id) {
        return named(id.asString());
    }

    /**
     * {@return a fresh stream for a name}
     */
    public XoroshiroSource named(final String name) {
        final long[] hash = Seeds.ofName(name);
        return new XoroshiroSource(hash[0] ^ low, hash[1] ^ high);
    }

    /**
     * {@return a fresh stream for a 64-bit value}
     */
    public XoroshiroSource ofSeed(final long seed) {
        return new XoroshiroSource(seed ^ low, seed ^ high);
    }

    /**
     * {@return the factory of a world seed: the seed spreads over 128 bits, then the first two outputs of that
     * stream become the factory seed}
     */
    public static PositionalRandom ofWorld(final long worldSeed) {
        return new XoroshiroSource(worldSeed).splitPositional();
    }
}
