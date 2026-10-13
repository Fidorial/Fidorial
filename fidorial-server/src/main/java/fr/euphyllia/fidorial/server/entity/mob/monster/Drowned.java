package fr.euphyllia.fidorial.server.entity.mob.monster;

import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.fidorial.entity.EntityType;
import fr.fidorial.math.Location;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

public final class Drowned extends Zombie {

    public static final float MAX_HEALTH = 20f;

    public Drowned(final int entityId, final Location location) {
        super(entityId, EntityTypes.DROWNED, location, SpawnData.roll());
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
    protected @Nullable EntityType waterConversionType() {
        return null;
    }

    @Override
    protected EntityType powderSnowConversionType() {
        return EntityTypes.ZOMBIE;
    }

    @Override
    protected Sound.Type powderSnowConversionSound() {
        return SoundEvents.DROWNED_CONVERTED_TO_ZOMBIE;
    }
}
