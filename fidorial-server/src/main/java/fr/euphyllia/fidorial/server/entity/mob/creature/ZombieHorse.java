package fr.euphyllia.fidorial.server.entity.mob.creature;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.fidorial.math.Location;

import java.util.UUID;

public final class ZombieHorse extends AbstractMob implements Category.Monster {

    public static final float MAX_HEALTH = 15f;

    public ZombieHorse(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.ZOMBIE_HORSE, location, MAX_HEALTH);
    }
}
