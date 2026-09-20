package fr.euphyllia.fidorial.server.entity.mob.monster;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.fidorial.math.Location;

import java.util.UUID;

public final class MagmaCube extends AbstractMob implements Category.Monster {

    public static final float MAX_HEALTH = 16f;

    public MagmaCube(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.MAGMA_CUBE, location, MAX_HEALTH);
    }
}
