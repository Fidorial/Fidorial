package fr.fidorial.entity.mob.creature;

import fr.fidorial.entity.mob.Animal;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.PigSoundVariant;
import fr.fidorial.registry.data.PigVariant;

/**
 * A pig ({@code minecraft:pig}): it looks and sounds after the biome it spawned in.
 *
 * <p>Like every entity, change it from the region thread owning its chunk.</p>
 *
 * @since 0.1.0
 */
public interface Pig extends Animal {

    /**
     * {@return how this pig looks}
     *
     * @since 0.1.0
     */
    TypedKey<PigVariant> variant();

    /**
     * Changes how this pig looks.
     *
     * @param variant the new look
     * @since 0.1.0
     */
    void setVariant(TypedKey<PigVariant> variant);

    /**
     * {@return how this pig sounds}
     *
     * @since 0.1.0
     */
    TypedKey<PigSoundVariant> soundVariant();

    /**
     * Changes how this pig sounds.
     *
     * @param soundVariant the new sounds
     * @since 0.1.0
     */
    void setSoundVariant(TypedKey<PigSoundVariant> soundVariant);
}
