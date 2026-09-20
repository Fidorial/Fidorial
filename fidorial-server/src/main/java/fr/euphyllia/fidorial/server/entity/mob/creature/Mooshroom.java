package fr.euphyllia.fidorial.server.entity.mob.creature;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.fidorial.math.Location;
import fr.fidorial.world.World;

import java.util.UUID;

public final class Mooshroom extends AbstractMob implements Category.Neutral {

    public static final float MAX_HEALTH = 10f;

    public Mooshroom(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.MOOSHROOM, location, MAX_HEALTH);
    }
}
