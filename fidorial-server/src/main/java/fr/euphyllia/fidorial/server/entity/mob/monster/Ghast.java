package fr.euphyllia.fidorial.server.entity.mob.monster;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.fidorial.math.Location;

import java.util.UUID;

public final class Ghast extends AbstractMob implements Category.Monster {

    public static final float MAX_HEALTH = 10f;

    public Ghast(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.GHAST, location, MAX_HEALTH);
    }
}
