package fr.fidorial.entity.mob.monster;

import fr.fidorial.entity.mob.Shearable;
import fr.fidorial.item.ItemStack;

/**
 * A sulfur cube ({@code minecraft:sulfur_cube}): a cube that hurts no one. It is born a baby of
 * size 1 that grows up into an adult of size 2, faster when fed slime balls; a baby follows the
 * players holding slime balls, an adult those holding a block it could swallow. Once dead, an adult
 * splits into two babies.
 *
 * <p>An adult swallows such a block when given one, or when it finds one lying around. It then
 * stops moving by itself and becomes a ball that players push and kick around: the block sets how
 * it bounces, slides and floats, how far a hit sends it, and whether touching it burns. Blows and
 * shots no longer hurt it then, they only send it flying. Shears make it spit the block out.</p>
 * @since 0.1.0
 */
public interface SulfurCube extends CubeMob, Shearable {

    /**
     * The size of an adult sulfur cube.
     *
     * @since 0.1.0
     */
    int ADULT_SIZE = 2;

    /**
     * {@return the block this cube swallowed}, an empty stack if it swallowed none
     *
     * @since 0.1.0
     */
    ItemStack swallowedItem();

    /**
     * Makes this cube hold one of an item, without a sound and without dropping what it held.
     *
     * @param stack the item, one of which it keeps; an empty stack for none
     * @since 0.1.0
     */
    void setSwallowedItem(ItemStack stack);
}
