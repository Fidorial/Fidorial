package fr.euphyllia.fidorial.server.entity.mob.monster;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.fidorial.math.Location;

import java.util.UUID;

public final class Warden extends AbstractMob implements Category.Monster {

    public static final float MAX_HEALTH = 500f;

    public Warden(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.WARDEN, location, MAX_HEALTH);
    }
}
