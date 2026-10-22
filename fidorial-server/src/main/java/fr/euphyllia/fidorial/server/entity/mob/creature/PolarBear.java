package fr.euphyllia.fidorial.server.entity.mob.creature;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.fidorial.math.Location;

import java.util.UUID;

public final class PolarBear extends AbstractMob implements Category.Neutral {

    public static final float MAX_HEALTH = 30f;

    public PolarBear(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.POLAR_BEAR, location, MAX_HEALTH);
    }
}
