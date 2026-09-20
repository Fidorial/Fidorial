package fr.euphyllia.fidorial.server.entity.mob.monster;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.fidorial.math.Location;

import java.util.UUID;

public final class ElderGuardian extends AbstractMob implements Category.Monster {

    public static final float MAX_HEALTH = 80f;

    public ElderGuardian(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.ELDER_GUARDIAN, location, MAX_HEALTH);
    }
}
