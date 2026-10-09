package fr.euphyllia.fidorial.server.entity.mob.monster;

import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.fidorial.entity.EntityType;
import fr.fidorial.math.Location;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.sound.Sound;

public final class Husk extends Zombie {

    public static final float MAX_HEALTH = 20f;

    public Husk(final int entityId, final Location location) {
        super(entityId, EntityTypes.HUSK, location, SpawnData.roll());
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
    protected EntityType waterConversionType() {
        return EntityTypes.ZOMBIE;
    }

    @Override
    protected Sound.Type waterConversionSound() {
        return SoundEvents.HUSK_CONVERTED_TO_ZOMBIE;
    }
}
