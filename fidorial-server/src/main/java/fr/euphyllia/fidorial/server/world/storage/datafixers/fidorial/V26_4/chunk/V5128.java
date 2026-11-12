package fr.euphyllia.fidorial.server.world.storage.datafixers.fidorial.V26_4.chunk;

import ca.spottedleaf.converter.DataConverter;
import ca.spottedleaf.converter.types.MapType;
import org.jspecify.annotations.Nullable;

/// Drops the stored live heightmaps, which older builds wrote with the wrong rules, so they're rebuilt on first use.
public final class V5128 extends DataConverter<MapType, MapType> {

    public static final int TO_VERSION = 5128;

    private static final String[] LIVE_HEIGHTMAPS = {
            "WORLD_SURFACE", "OCEAN_FLOOR", "MOTION_BLOCKING", "MOTION_BLOCKING_NO_LEAVES"
    };

    public V5128() {
        super(TO_VERSION);
    }

    @Override
    public @Nullable MapType convert(final @Nullable MapType data, final long sourceVersion, final long toVersion) {
        if (data == null) {
            return null;
        }
        final MapType heightmaps = data.getMap("Heightmaps", null);
        if (heightmaps == null) {
            return data;
        }
        for (final String key : LIVE_HEIGHTMAPS) {
            heightmaps.remove(key);
        }
        data.setMap("Heightmaps", heightmaps);
        return data;
    }
}
