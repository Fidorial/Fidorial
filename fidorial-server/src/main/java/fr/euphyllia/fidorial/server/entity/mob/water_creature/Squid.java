package fr.euphyllia.fidorial.server.entity.mob.water_creature;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.fidorial.math.Location;

import java.util.UUID;

public final class Squid extends AbstractMob implements Category.Neutral {

    public static final float MAX_HEALTH = 10f;

    public Squid(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.SQUID, location, MAX_HEALTH);
    }
}
