package fr.euphyllia.fidorial.server.world.generation.random;

import net.kyori.adventure.key.Key;

/**
 * Hands out streams tied to a block position or to a name, all derived from one seed. Asking twice for the same
 * position or the same name gives two streams that produce the same values, whatever was asked in between:
 * generation does not depend on the order chunks are made in.
 *
 * <p>There are two kinds: the xoroshiro one, and the legacy one built on the 48-bit generator, which the noise
 * settings of a dimension ask for with {@code legacy_random_source}. Immutable, safe to share between threads; the
 * streams it returns are not.</p>
 */
public sealed interface PositionalRandom {

    /**
     * {@return a fresh stream for a block position}
     */
    BitRandom at(int x, int y, int z);

    /**
     * {@return a fresh stream for a name}
     */
    BitRandom named(String name);

    /**
     * {@return a fresh stream for a namespaced identifier}
     */
    default BitRandom named(final Key id) {
        return named(id.asString());
    }

    /**
     * {@return the factory of a world seed}: the seed spreads over 128 bits, then the first two outputs of that
     * stream become the factory seed
     */
    static PositionalRandom ofWorld(final long worldSeed) {
        return new XoroshiroSource(worldSeed).splitPositional();
    }

    /**
     * {@return the legacy factory of a world seed}: the first output of a 48-bit generator seeded with it becomes
     * the factory seed
     */
    static PositionalRandom ofLegacyWorld(final long worldSeed) {
        return new LegacyRandom(worldSeed).splitPositional();
    }

    /**
     * Streams seeded over 128 bits: a position or the digest of a name, mixed with the seed of the factory.
     *
     * @param low  the low half of the seed
     * @param high the high half of the seed
     */
    record Xoroshiro(long low, long high) implements PositionalRandom {

        @Override
        public XoroshiroSource at(final int x, final int y, final int z) {
            return new XoroshiroSource(Seeds.ofPosition(x, y, z) ^ low, high);
        }

        @Override
        public XoroshiroSource named(final String name) {
            final long[] hash = Seeds.ofName(name);
            return new XoroshiroSource(hash[0] ^ low, hash[1] ^ high);
        }
    }

    /**
     * Streams of the 48-bit generator: the seed of a position or the hash code of a name, mixed with the seed of
     * the factory.
     *
     * @param seed the seed of the factory
     */
    record Legacy(long seed) implements PositionalRandom {

        @Override
        public LegacyRandom at(final int x, final int y, final int z) {
            return new LegacyRandom(Seeds.ofPosition(x, y, z) ^ seed);
        }

        @Override
        public LegacyRandom named(final String name) {
            return new LegacyRandom(name.hashCode() ^ seed);
        }
    }
}
