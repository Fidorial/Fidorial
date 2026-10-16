package fr.euphyllia.fidorial.server.entity.mob.creature;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.fidorial.math.Location;

import java.util.UUID;

public final class Pig extends AbstractMob implements Category.Neutral {

    public static final float MAX_HEALTH = 10f;

    public Pig(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.PIG, location, MAX_HEALTH);
    }
}
