package fr.fidorial.world;

/**
 * @param x the first component
 * @param y the second component
 * @param z the third component
 * @since 0.1.0
 */
public record Vec3f(float x, float y, float z) {

    /**
     * The vector whose components are all zero.
     *
     * @since 0.1.0
     */
    public static final Vec3f ZERO = new Vec3f(0.0f, 0.0f, 0.0f);

    /**
     * @param x the first component
     * @param y the second component
     * @param z the third component
     * @return a vector with those components, or {@link #ZERO} when they are all zero
     * @since 0.1.0
     */
    public static Vec3f of(final float x, final float y, final float z) {
        return x == 0.0f && y == 0.0f && z == 0.0f ? ZERO : new Vec3f(x, y, z);
    }
}
