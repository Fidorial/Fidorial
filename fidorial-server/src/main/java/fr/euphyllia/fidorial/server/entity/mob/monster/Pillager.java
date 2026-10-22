package fr.euphyllia.fidorial.server.entity.mob.monster;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.fidorial.math.Location;

import java.util.UUID;

public final class Pillager extends AbstractMob implements Category.Monster {

    public static final float MAX_HEALTH = 24f;

    public Pillager(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.PILLAGER, location, MAX_HEALTH);
    }
}
