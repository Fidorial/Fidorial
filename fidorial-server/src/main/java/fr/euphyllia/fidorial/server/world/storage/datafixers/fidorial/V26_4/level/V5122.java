package fr.euphyllia.fidorial.server.world.storage.datafixers.fidorial.V26_4.level;

import ca.spottedleaf.converter.DataConverter;
import ca.spottedleaf.converter.types.MapType;
import org.jspecify.annotations.Nullable;

/**
 * Rewrites a numeric {@code difficulty_settings.difficulty} as the vanilla string id.
 */
public final class V5122 extends DataConverter<MapType, MapType> {

    public static final int TO_VERSION = 5122;

    private static final String[] DIFFICULTIES = { "peaceful", "easy", "normal", "hard" };

    public V5122() {
        super(TO_VERSION);
    }

    @Override
    @Nullable public MapType convert(final @Nullable MapType data, final long sourceVersion, final long toVersion) {
        if (data == null) {
            return null;
        }
        final MapType settings = data.getMap("difficulty_settings", null);
        if (settings == null) {
            return data;
        }
        final Number id = settings.getNumber("difficulty", null);
        if (id == null) {
            return data;
        }
        final int index = id.intValue();
        settings.setString("difficulty", index >= 0 && index < DIFFICULTIES.length ? DIFFICULTIES[index] : "normal");
        data.setMap("difficulty_settings", settings);
        return data;
    }
}
