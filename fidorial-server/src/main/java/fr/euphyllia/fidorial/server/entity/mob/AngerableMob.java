package fr.euphyllia.fidorial.server.entity.mob;

import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.mob.Angerable;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * A mob that leaves the others alone until provoked, then holds a grudge: see
 * {@link PersistentAnger}, which answers for it to plugins.
 */
public interface AngerableMob extends Angerable {

    /**
     * {@return the grudge this mob holds}
     */
    PersistentAnger anger();

    @Override
    default boolean isAngry() {
        return anger().isAngry();
    }

    @Override
    default long remainingAngerTicks() {
        return anger().remainingTicks();
    }

    @Override
    default @Nullable UUID angryAt() {
        return anger().angryAt();
    }

    @Override
    default boolean isAngryAt(final LivingEntity entity) {
        return entity instanceof final AbstractLivingEntity living && anger().isAngryAt(living);
    }

    @Override
    default void setAngryAt(final LivingEntity entity, final long ticks) {
        if (entity instanceof final AbstractLivingEntity living) {
            anger().provoke(living, ticks);
        }
    }

    @Override
    default void stopBeingAngry() {
        anger().stop();
    }
}
