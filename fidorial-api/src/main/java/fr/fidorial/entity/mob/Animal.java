package fr.fidorial.entity.mob;

import fr.fidorial.entity.player.Player;
import fr.fidorial.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * An animal that breeds: fed its food, it falls in love and looks for a partner of its kind, and
 * the two have a baby.
 *
 * <p>Like every entity, change it from the region thread owning its chunk.</p>
 *
 * @since 0.1.0
 */
public interface Animal extends Ageable {

    /**
     * How long an animal stays in love, in ticks.
     *
     * @since 0.1.0
     */
    int LOVE_TICKS = 600;

    /**
     * {@return {@code true} if this animal eats an item}: fed to it, the item makes it fall in love
     * or a baby grow up faster
     *
     * @param stack the item
     * @since 0.1.0
     */
    boolean isFood(ItemStack stack);

    /**
     * {@return {@code true} while this animal is in love and looks for a partner}
     *
     * @since 0.1.0
     */
    boolean isInLove();

    /**
     * {@return how many ticks this animal stays in love, {@code 0} if it is not}
     *
     * @since 0.1.0
     */
    int inLoveTime();

    /**
     * Makes this animal fall in love for {@value #LOVE_TICKS} ticks, hearts rising above it.
     *
     * @param cause the player who fed it, credited for the baby; {@code null} for none
     * @since 0.1.0
     */
    void setInLove(@Nullable Player cause);

    /**
     * Makes this animal stop looking for a partner.
     *
     * @since 0.1.0
     */
    void resetLove();

    /**
     * {@return the player who made this animal fall in love last, {@code null} if unknown}
     *
     * @since 0.1.0
     */
    @Nullable UUID loveCause();

    /**
     * {@return {@code true} while this animal runs away from what hurt it}; it breeds with no one
     * meanwhile
     *
     * @since 0.1.0
     */
    boolean isPanicking();
}
