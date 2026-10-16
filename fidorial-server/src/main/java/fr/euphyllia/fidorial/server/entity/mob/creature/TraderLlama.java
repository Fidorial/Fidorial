package fr.euphyllia.fidorial.server.entity.mob.creature;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.fidorial.math.Location;

import java.util.UUID;

public final class TraderLlama extends AbstractMob implements Category.Neutral {

    public static final float MAX_HEALTH = 15f;

    public TraderLlama(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.TRADER_LLAMA, location, MAX_HEALTH);
    }
}
