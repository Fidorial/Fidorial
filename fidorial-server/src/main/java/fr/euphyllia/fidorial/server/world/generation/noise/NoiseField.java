package fr.euphyllia.fidorial.server.world.generation.noise;

/**
 * A noise sampled at any point of space, in single precision. Implementations are immutable once built and can
 * be sampled from several threads.
 */
public interface NoiseField {

    /**
     * {@return the noise at a point}
     */
    float sample(double x, double y, double z);

    /**
     * {@return the largest absolute value the noise can take; used to skip computations whose result could not
     * change the outcome}
     */
    float bound();
}
