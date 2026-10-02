package fr.euphyllia.fidorial.server.entity.mob.monster;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.fidorial.math.Location;

import java.util.UUID;

public final class Evoker extends AbstractMob implements Category.Monster {

    public static final float MAX_HEALTH = 24f;

    public Evoker(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.EVOKER, location, MAX_HEALTH);
    }
}
