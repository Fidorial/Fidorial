package fr.fidorial.entity.projectile;

/**
 * A spectral arrow ({@code minecraft:spectral_arrow}), which makes what it hits glow.
 *
 * @since 0.1.0
 */
public interface SpectralArrow extends ArrowProjectile {

    /**
     * {@return how many ticks what this arrow hits glows}
     *
     * @since 0.1.0
     */
    int glowingTicks();
}
