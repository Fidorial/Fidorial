package fr.fidorial.world.environment;

import net.kyori.adventure.key.Key;

/**
 * One entry of the {@code minecraft:visual/ambient_particles} environment attribute: a particle
 * randomly spawned around the camera.
 *
 * @param type        the {@code minecraft:particle_type} key, e.g. {@code minecraft:ash}
 * @param probability chance, per random tick of an empty space, that the particle spawns; must be
 *                    in {@code [0, 1]}
 * @since 0.1.0
 */
public record AmbientParticle(Key type, float probability) {

    public AmbientParticle {
        if (probability < 0F || probability > 1F) {
            throw new IllegalArgumentException("The probability of an ambient particle must be within [0, 1], got " + probability);
        }
    }

    /**
     * Creates an ambient particle.
     *
     * @param type        the particle type key
     * @param probability spawn probability
     * @return the particle
     */
    public static AmbientParticle of(final Key type, final float probability) {
        return new AmbientParticle(type, probability);
    }
}
