package fr.euphyllia.fidorial.server.entity.mob.monster.zombie;

import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.fidorial.math.Location;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.sound.Sound;

public final class Husk extends Zombie {

    public static final float MAX_HEALTH = 20f;

    public Husk(final int entityId, final Location location) {
        this(entityId, location, true);
    }

    /**
     * @param spawned {@code true} for a husk that spawns, with the random traits of a zombie that does
     */
    public Husk(final int entityId, final Location location, final boolean spawned) {
        super(entityId, EntityTypes.HUSK, location, spawned);
    }

    @Override
    protected Sound.Type ambientSound() {
        return SoundEvents.HUSK_AMBIENT;
    }

    @Override
    protected Sound.Type hurtSound() {
        return SoundEvents.HUSK_HURT;
    }
    @Override
    protected Sound.Type deathSound() {
        return SoundEvents.HUSK_DEATH;
    }

    @Override
    protected Sound.Type stepSound() {
        return SoundEvents.HUSK_STEP;
    }

    @Override
    protected Zombie drownedForm(final int entityId, final Location location) {
        return new Zombie(entityId, location, false);
    }

    @Override
    protected Sound.Type drownedSound() {
        return SoundEvents.HUSK_CONVERTED_TO_ZOMBIE;
    }

    @Override
    protected Zombie frozenForm(final int entityId, final Location location) {
        return new Zombie(entityId, location, false);
    }

    @Override
    protected Sound.Type frozenSound() {
        return SoundEvents.HUSK_CONVERTED_TO_ZOMBIE;
    }
}
