package fr.euphyllia.fidorial.server.world.storage.datafixers.fidorial.V26_4.world_data;

import ca.spottedleaf.converter.DataConverter;
import ca.spottedleaf.converter.types.MapType;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Wraps a saved-data payload written at the file root under {@code data}.
 */
public final class V5122_2 extends DataConverter<MapType, MapType> {

    public static final int TO_VERSION = 5122;

    private static final String DATA = "data";
    private static final String DATA_VERSION = "DataVersion";

    public V5122_2() {
        super(TO_VERSION, 2);
    }

    @Override
    @Nullable public MapType convert(final @Nullable MapType root, final long sourceVersion, final long toVersion) {
        if (root == null || root.getMap(DATA, null) != null) {
            return root;
        }
        final MapType payload = root.createEmptyMap();
        for (final String key : List.copyOf(root.keys())) {
            if (DATA_VERSION.equals(key)) {
                continue;
            }
            payload.setGeneric(key, root.getGeneric(key));
            root.remove(key);
        }
        root.setMap(DATA, payload);
        return root;
    }
}
