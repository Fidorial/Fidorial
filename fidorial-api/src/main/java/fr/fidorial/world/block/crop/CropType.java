package fr.fidorial.world.block.crop;

import fr.fidorial.plugin.Plugin;
import net.kyori.adventure.key.Key;

import java.util.List;
import java.util.Set;

/**
 * A plantable crop: the seed that plants it, the block it grows as, and what it
 * accepts as soil.
 *
 * <p>Obtain one through {@link CropRegistry#builder(Key, Key)}, then hand it to
 * {@link CropRegistry#register(CropType, Plugin)}.</p>
 *
 * @since 0.1.0
 */
public interface CropType {

    /**
     * The block property crops carry their growth stage in, unless they say otherwise.
     *
     * @since 0.1.0
     */
    String DEFAULT_AGE_PROPERTY = "age";

    /**
     * @return the item that plants this crop
     * @since 0.1.0
     */
    Key seed();

    /**
     * @return the block this crop grows as
     * @since 0.1.0
     */
    Key block();

    /**
     * @return the block property carrying the growth stage
     * @since 0.1.0
     */
    String ageProperty();

    /**
     * @return the growth stage at which the crop is ripe
     * @since 0.1.0
     */
    int maxAge();

    /**
     * @return the blocks this crop can be planted on
     * @since 0.1.0
     */
    Set<Key> soils();

    /**
     * @return {@code true} when the soil has to be wet for planting to work
     * @since 0.1.0
     */
    boolean requiresMoistSoil();

    /**
     * How long one growth stage takes, on average.
     *
     * @return the average number of ticks one growth stage takes
     * @since 0.1.0
     */
    int averageTicksPerStage();

    /**
     * @return the light level the crop needs to grow; {@code 0} means it grows in the dark
     * @since 0.1.0
     */
    int minLight();

    /**
     * @return what breaking this crop gives back once it is ripe
     * @since 0.1.0
     */
    List<CropDrop> ripeDrops();

    /**
     * @return what breaking this crop gives back before it is ripe
     * @since 0.1.0
     */
    List<CropDrop> immatureDrops();

    /**
     * @param age the growth stage the crop was broken at
     * @return the drops that apply at that stage
     * @since 0.1.0
     */
    default List<CropDrop> dropsAt(final int age) {
        return age >= maxAge() ? ripeDrops() : immatureDrops();
    }

    /**
     * @param soil a block identifier
     * @return {@code true} when this crop accepts that block as soil
     * @since 0.1.0
     */
    default boolean acceptsSoil(final Key soil) {
        return soils().contains(soil);
    }

    /**
     * Assembles a {@link CropType}.
     *
     * @since 0.1.0
     */
    interface Builder {

        /**
         * @param ageProperty the block property carrying the growth stage
         * @return this builder
         * @since 0.1.0
         */
        Builder ageProperty(String ageProperty);

        /**
         * @param maxAge the growth stage at which the crop is ripe
         * @return this builder
         * @since 0.1.0
         */
        Builder maxAge(int maxAge);

        /**
         * Replaces the accepted soils. Defaults to farmland alone.
         *
         * @param soils the blocks the crop can be planted on
         * @return this builder
         * @since 0.1.0
         */
        Builder soils(Set<Key> soils);

        /**
         * @param requiresMoistSoil {@code false} to let the crop be planted on dry soil
         * @return this builder
         * @since 0.1.0
         */
        Builder requiresMoistSoil(boolean requiresMoistSoil);

        /**
         * @param averageTicksPerStage the average number of ticks one growth stage should take
         * @return this builder
         * @since 0.1.0
         */
        Builder averageTicksPerStage(int averageTicksPerStage);

        /**
         * @param minLight the light level the crop needs to grow; {@code 0} to let it grow in the dark
         * @return this builder
         * @since 0.1.0
         */
        Builder minLight(int minLight);

        /**
         * @param ripeDrops what breaking the ripe crop gives back
         * @return this builder
         * @since 0.1.0
         */
        Builder ripeDrops(List<CropDrop> ripeDrops);

        /**
         * @param immatureDrops what breaking the crop early gives back
         * @return this builder
         * @since 0.1.0
         */
        Builder immatureDrops(List<CropDrop> immatureDrops);

        /**
         * @return the crop
         * @since 0.1.0
         */
        CropType build();
    }
}
