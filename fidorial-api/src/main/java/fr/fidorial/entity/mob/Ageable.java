package fr.fidorial.entity.mob;

/**
 * A mob that is born a baby and grows up: farm animals among others.
 *
 * <p>Its age counts in ticks: negative while it is a baby, rising to {@code 0} as it grows up;
 * positive for an adult that has just bred, falling to {@code 0} before it may breed again.</p>
 *
 * <p>Like every entity, change it from the region thread owning its chunk.</p>
 *
 * @since 0.1.0
 */
public interface Ageable extends Mob {

    /**
     * How old a newborn baby is: it grows up after {@code 24000} ticks, a full day.
     *
     * @since 0.1.0
     */
    int BABY_START_AGE = -24000;

    /**
     * {@return {@code true} while this mob is a baby}
     *
     * @since 0.1.0
     */
    boolean isBaby();

    /**
     * Makes this mob a newborn baby, or an adult ready to breed.
     *
     * @param baby {@code true} for a baby
     * @since 0.1.0
     */
    void setBaby(boolean baby);

    /**
     * {@return the age of this mob in ticks}: negative while it is a baby, positive while it cannot
     * breed again, {@code 0} otherwise
     *
     * @since 0.1.0
     */
    int age();

    /**
     * Changes the age of this mob; a negative age makes it a baby.
     *
     * @param age the new age, in ticks
     * @since 0.1.0
     */
    void setAge(int age);

    /**
     * {@return {@code true} if this baby stopped growing up}, as one fed a golden dandelion does
     *
     * @since 0.1.0
     */
    boolean isAgeLocked();

    /**
     * Stops this baby from growing up, or lets it grow again.
     *
     * @param ageLocked {@code true} to stop it
     * @since 0.1.0
     */
    void setAgeLocked(boolean ageLocked);
}
