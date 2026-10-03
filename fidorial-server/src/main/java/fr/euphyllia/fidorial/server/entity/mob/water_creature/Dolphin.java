package fr.euphyllia.fidorial.server.entity.mob.water_creature;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.fidorial.math.Location;

import java.util.UUID;

public final class Dolphin extends AbstractMob implements Category.Neutral {

    public static final float MAX_HEALTH = 10f;

    public Dolphin(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.DOLPHIN, location, MAX_HEALTH);
    }
}
