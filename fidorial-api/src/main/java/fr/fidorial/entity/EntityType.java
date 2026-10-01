package fr.fidorial.entity;

import com.google.common.base.Preconditions;
import net.kyori.adventure.key.Key;


public record EntityType(Key key, Category category, float width, float height) {

    public EntityType {
        Preconditions.checkNotNull(key, "The key of an entity type must not be null");
        Preconditions.checkNotNull(category, "The category of an entity type must not be null");
    }

    public enum Category {
        PLAYER,
        MONSTER,
        CREATURE,
        AMBIENT,
        WATER_CREATURE,
        MISC
    }
}
