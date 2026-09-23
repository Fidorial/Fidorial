package fr.fidorial.world.block.plant;

import com.google.common.base.Preconditions;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.block.BlockAccess;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockDrop;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * A plant that sits on a soil block and climbs through growth stages carried in
 * its {@value #AGE} property: wheat, carrots, potatoes — or a plugin's own rice.
 *
 * @since 0.1.0
 */
public interface CropBlock extends BlockBehaviour {

    /**
     * The block property crops carry their growth stage in.
     *
     * @since 0.1.0
     */
    String AGE = "age";

    /**
     * @return the growth stage at which the crop is ripe
     * @since 0.1.0
     */
    int maxAge();

    /**
     * @return the blocks this crop can sit on
     * @since 0.1.0
     */
    Set<Key> soils();

    /**
     * @param data a state of this crop
     * @return its growth stage
     * @since 0.1.0
     */
    int age(BlockData data);

    /**
     * @param data a state of this crop
     * @param age  the growth stage wanted, clamped to the valid range
     * @return that state at that growth stage
     * @since 0.1.0
     */
    BlockData withAge(BlockData data, int age);

    /**
     * @param data a state of this crop
     * @return {@code true} when it is fully grown
     * @since 0.1.0
     */
    default boolean isRipe(final BlockData data) {
        return age(data) >= maxAge();
    }

    /**
     * Assembles a {@link CropBlock}. Unless told otherwise, a crop sits on
     * farmland, needs a light level of 9, grows at vanilla speed and drops nothing.
     *
     * @since 0.1.0
     */
    interface Builder {

        /**
         * @param soils the blocks the crop can sit on
         * @return this builder
         * @since 0.1.0
         */
        Builder soils(Set<Key> soils);

        /**
         * @param minLight the light level the crop needs to grow; {@code 0} to let it grow in the dark
         * @return this builder
         * @since 0.1.0
         */
        Builder minLight(int minLight);

        /**
         * @param condition what must hold for the crop to grow at all, on top of the light level
         * @return this builder
         * @since 0.1.0
         */
        Builder growsWhen(Condition condition);

        /**
         * @param growth how likely the crop is to grow on each random tick, replacing vanilla's rule
         * @return this builder
         * @since 0.1.0
         */
        Builder growth(Growth growth);

        /**
         * @param drops what breaking the ripe crop gives back
         * @return this builder
         * @since 0.1.0
         */
        Builder ripeDrops(List<BlockDrop> drops);

        /**
         * @param drops what breaking the crop early gives back
         * @return this builder
         * @since 0.1.0
         */
        Builder immatureDrops(List<BlockDrop> drops);

        /**
         * @param sound the sound played when the crop is planted, {@code null} for none
         * @return this builder
         * @since 0.1.0
         */
        Builder placeSound(Sound.@Nullable Type sound);

        /**
         * @return the crop
         * @since 0.1.0
         */
        CropBlock build();
    }

    /**
     * Something that must hold for a crop to grow: water nearby, darkness...
     *
     * @since 0.1.0
     */
    @FunctionalInterface
    interface Condition {

        /**
         * @param data  the crop's current state
         * @param world where it is
         * @param pos   its position
         * @return {@code true} when the crop may grow
         * @since 0.1.0
         */
        boolean test(BlockData data, BlockAccess world, BlockPos pos);
    }

    /**
     * How likely a crop is to move up one stage on a random tick.
     *
     * @since 0.1.0
     */
    @FunctionalInterface
    interface Growth {

        /**
         * @param data  the crop's current state
         * @param world where it is
         * @param pos   its position
         * @return a probability between {@code 0} and {@code 1}
         * @since 0.1.0
         */
        double chance(BlockData data, BlockAccess world, BlockPos pos);

        /**
         * @param chance a probability between {@code 0} and {@code 1}
         * @return a rule giving the same odds on every tick
         * @since 0.1.0
         */
        static Growth fixed(final double chance) {
            Preconditions.checkArgument(chance >= 0 && chance <= 1, "chance must be within [0, 1], got %s", chance);
            return (_, _, _) -> chance;
        }
    }
}
