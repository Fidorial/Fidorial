package fr.euphyllia.fidorial.server.world.storage.datafixers;

import ca.spottedleaf.converter.DataConverter;
import ca.spottedleaf.converter.types.MapType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class DataFixerRegistry {

    private final List<DataConverter<MapType, MapType>> fixers;

    DataFixerRegistry(final List<DataConverter<MapType, MapType>> fixers) {
        final List<DataConverter<MapType, MapType>> sorted = new ArrayList<>(fixers);
        sorted.sort(DataConverter.LOWEST_VERSION_COMPARATOR);
        this.fixers = List.copyOf(sorted);
    }

    public @Nullable MapType apply(final @Nullable MapType data, final int sourceDataVersion) {
        if (data == null) {
            return null;
        }
        MapType current = data;
        for (final DataConverter<MapType, MapType> fixer : fixers) {
            final int toVersion = fixer.getToVersion();
            if (toVersion > sourceDataVersion) {
                current = fixer.convert(current, sourceDataVersion, toVersion);
            }
        }
        return current;
    }

    public int size() {
        return fixers.size();
    }
}
