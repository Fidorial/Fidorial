package fr.fidorial.entity.mob.monster;

import fr.fidorial.entity.Entity;
import fr.fidorial.entity.mob.Angerable;
import fr.fidorial.entity.player.Player;
import fr.fidorial.world.block.BlockData;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * An enderman ({@code minecraft:enderman}): it leaves everyone alone until a player stares at its
 * eyes or hurts it, then screams and teleports around them. It goes after the endermites, teleports
 * away from water, projectiles and the open sky in daylight, and picks up and puts down some
 * blocks when mobs may change the world.
 *
 * <p>Like every entity, change it from the region thread owning its chunk.</p>
 *
 * @since 0.1.0
 */
public interface Enderman extends Angerable {

    /**
     * {@return the block this enderman carries, empty when its hands are empty}
     *
     * @since 0.1.0
     */
    Optional<BlockData> carriedBlock();

    /**
     * Makes this enderman carry a block, or none. It drops the block it carries when it dies, and
     * does not despawn while it carries one.
     *
     * @param block the block, {@code null} or air for none
     * @since 0.1.0
     */
    void setCarriedBlock(@Nullable BlockData block);

    /**
     * {@return {@code true} while this enderman has a target}: it then opens its mouth and screams
     *
     * @since 0.1.0
     */
    boolean isScreaming();

    /**
     * {@return {@code true} once a player provoked this enderman by staring at it}, until it gives up
     * its target
     *
     * @since 0.1.0
     */
    boolean hasBeenStaredAt();

    /**
     * {@return {@code true} if a player stares at the eyes of this enderman}, unless the player wears
     * an item of the {@code minecraft:gaze_disguise_equipment} tag, such as a carved pumpkin
     *
     * @param player the player
     * @since 0.1.0
     */
    boolean isBeingStaredBy(Player player);

    /**
     * Tries to teleport this enderman somewhere within 32 blocks across and up or down, as it does
     * when hurt: it lands on solid ground, out of water and away from the blocks of the
     * {@code minecraft:enderman_does_not_teleport_to} tag.
     *
     * @return {@code true} if it teleported
     * @since 0.1.0
     */
    boolean teleportRandomly();

    /**
     * Tries to teleport this enderman closer to an entity, as it does to a player it goes after
     * that stays far away.
     *
     * @param entity the entity to get close to
     * @return {@code true} if it teleported
     * @since 0.1.0
     */
    boolean teleportTowards(Entity entity);
}
