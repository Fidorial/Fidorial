package fr.fidorial.entity.mob.creature;

import fr.fidorial.entity.mob.Animal;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.CowSoundVariant;
import fr.fidorial.registry.data.CowVariant;

/**
 * A cow ({@code minecraft:cow}): it gives milk to a bucket, and looks and sounds after the biome it
 * spawned in.
 *
 * <p>Like every entity, change it from the region thread owning its chunk.</p>
 *
 * @since 0.1.0
 */
public interface Cow extends Animal {

    /**
     * {@return how this cow looks}
     *
     * @since 0.1.0
     */
    TypedKey<CowVariant> variant();

    /**
     * Changes how this cow looks.
     *
     * @param variant the new look
     * @since 0.1.0
     */
    void setVariant(TypedKey<CowVariant> variant);

    /**
     * {@return how this cow sounds}
     *
     * @since 0.1.0
     */
    TypedKey<CowSoundVariant> soundVariant();

    /**
     * Changes how this cow sounds.
     *
     * @param soundVariant the new sounds
     * @since 0.1.0
     */
    void setSoundVariant(TypedKey<CowSoundVariant> soundVariant);
}
