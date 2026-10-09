package fr.euphyllia.fidorial.server.entity.mob.water_creature;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.fidorial.math.Location;

import java.util.UUID;

public final class TropicalFish extends AbstractMob implements Category.Neutral {

    public static final float MAX_HEALTH = 3f;

    public TropicalFish(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.TROPICAL_FISH, location, MAX_HEALTH);
    }
}
