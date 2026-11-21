package fr.fidorial.entity.mob.monster;

/**
 * A sulfur cube ({@code minecraft:sulfur_cube}): a cube that hurts no one. It is born a baby of
 * size 1 that grows up into an adult of size 2, faster when fed slime balls; a baby follows the
 * players holding slime balls, an adult those holding a block it could swallow. Once dead, an adult
 * splits into two babies.
 *
 * @since 0.1.0
 */
public interface SulfurCube extends CubeMob {

    /**
     * The size of an adult sulfur cube.
     *
     * @since 0.1.0
     */
    int ADULT_SIZE = 2;
}
