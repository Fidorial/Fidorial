package fr.euphyllia.fidorial.server.entity.mob;

/**
 * A mob that leaves the others alone until provoked, then holds a grudge: see
 * {@link PersistentAnger}.
 */
public interface AngerableMob {

    /**
     * {@return the grudge this mob holds}
     */
    PersistentAnger anger();
}
