package fr.fidorial.world.block.crop;

import fr.fidorial.plugin.Plugin;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

/**
 * Where crops are declared: the built-in ones, and the ones plugins add.
 *
 * @since 0.1.0
 */
public interface CropRegistry {

    /**
     * Declares a crop.
     *
     * @param crop  the crop to declare
     * @param owner the plugin declaring it
     * @since 0.1.0
     */
    void register(CropType crop, Plugin owner);

    /**
     * Drops the crop one owner declared under a seed, bringing back whatever that
     * declaration shadowed.
     *
     * @param seed  the seed of the crop to drop
     * @param owner the plugin that declared it
     * @return {@code true} when a crop was dropped
     * @since 0.1.0
     */
    boolean unregister(Key seed, Plugin owner);

    /**
     * Drops every crop an owner declared.
     *
     * @param owner the plugin to clean up after
     * @since 0.1.0
     */
    void unregisterAll(Plugin owner);

    /**
     * @param seed an item identifier
     * @return the crop that seed plants, or {@code null} when it plants nothing
     * @since 0.1.0
     */
    @Nullable CropType bySeed(Key seed);

    /**
     * @param block a block identifier
     * @return the crop growing as that block, or {@code null} when it is not a crop
     * @since 0.1.0
     */
    @Nullable CropType byBlock(Key block);

    /**
     * @return every declared crop
     * @since 0.1.0
     */
    Collection<CropType> crops();

    /**
     * @return every block at least one declared crop accepts as soil
     * @since 0.1.0
     */
    Set<Key> soils();
}
