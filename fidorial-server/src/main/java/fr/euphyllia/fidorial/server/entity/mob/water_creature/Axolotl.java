package fr.euphyllia.fidorial.server.entity.mob.water_creature;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.fidorial.math.Location;

import java.util.UUID;

public final class Axolotl extends AbstractMob implements Category.Neutral {

    public static final float MAX_HEALTH = 14f;

    public Axolotl(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.AXOLOTL, location, MAX_HEALTH);
    }
}
