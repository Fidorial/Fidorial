package fr.euphyllia.fidorial.server.entity.mob.monster;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.fidorial.math.Location;

import java.util.UUID;

public final class Skeleton extends AbstractMob implements Category.Monster {

    public static final float MAX_HEALTH = 20f;

    public Skeleton(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.SKELETON, location, MAX_HEALTH);
    }
}
