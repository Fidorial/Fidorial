package fr.euphyllia.fidorial.server.entity.mob.monster;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.fidorial.math.Location;

import java.util.UUID;

public final class ZombieNautilus extends AbstractMob implements Category.Monster {

    public static final float MAX_HEALTH = 15f;

    public ZombieNautilus(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.ZOMBIE_NAUTILUS, location, MAX_HEALTH);
    }
}
