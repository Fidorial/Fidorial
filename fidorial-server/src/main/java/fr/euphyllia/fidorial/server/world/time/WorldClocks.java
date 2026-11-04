package fr.euphyllia.fidorial.server.world.time;

import fr.euphyllia.fidorial.server.world.storage.Dimension;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.WorldClock;
import fr.fidorial.registry.keys.WorldClockKeys;
import net.kyori.adventure.key.Key;

public class WorldClocks {

    public static final Key REGISTRY = RegistryKey.WORLD_CLOCK.key();

    private WorldClocks() {
    }

    public static TypedKey<WorldClock> forDimension(final Dimension dimension) {
        if (Dimension.THE_END.id().equals(dimension.id())) {
            return WorldClockKeys.THE_END;
        }
        return WorldClockKeys.OVERWORLD;
    }

}
