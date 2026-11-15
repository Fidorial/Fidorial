package fr.euphyllia.fidorial.server.entity.mob.monster.zombie;

import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.fidorial.math.Location;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.sound.Sound;

public final class Drowned extends Zombie {

    public static final float MAX_HEALTH = 20f;

    public Drowned(final int entityId, final Location location) {
        this(entityId, location, true);
    }

    /**
     * @param spawned {@code true} for a drowned that spawns, with the random traits of a zombie that
     *                does
     */
    public Drowned(final int entityId, final Location location, final boolean spawned) {
        super(entityId, EntityTypes.DROWNED, location, spawned);
    }

    @Override
    protected Sound.Type ambientSound() {
        return SoundEvents.DROWNED_AMBIENT;
    }

    @Override
    protected Sound.Type hurtSound() {
        return SoundEvents.DROWNED_HURT;
    }

    @Override
    protected Sound.Type deathSound() {
        return SoundEvents.DROWNED_DEATH;
    }

    @Override
    protected Sound.Type stepSound() {
        return SoundEvents.DROWNED_STEP;
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    protected Zombie frozenForm(final int entityId, final Location location) {
        return new Zombie(entityId, location, false);
    }

    @Override
    protected Sound.Type frozenSound() {
        return SoundEvents.DROWNED_CONVERTED_TO_ZOMBIE;
    }
}
