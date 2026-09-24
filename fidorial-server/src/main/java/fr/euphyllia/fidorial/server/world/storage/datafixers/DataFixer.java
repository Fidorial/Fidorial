package fr.euphyllia.fidorial.server.world.storage.datafixers;

import ca.spottedleaf.converter.DataConverter;
import ca.spottedleaf.converter.datatypes.DataWalker;
import ca.spottedleaf.converter.types.ListType;
import ca.spottedleaf.converter.types.MapType;
import fr.euphyllia.fidorial.server.world.storage.datafixers.util.NestedType;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class DataFixer {

    private final int targetDataVersion;
    private final long encodedTargetVersion;
    private final Map<DataFixerType, DataFixerRegistry> registries;
    private final Map<DataFixerType, List<DataWalker<MapType>>> walkers;
    private final Map<DataFixerType, List<NestedType>> nesting;
    private final Map<DataFixerType, List<NestedType>> nestedMaps;

    DataFixer(
            final int targetDataVersion,
            final Map<DataFixerType, DataFixerRegistry> registries,
            final Map<DataFixerType, List<DataWalker<MapType>>> walkers,
            final Map<DataFixerType, List<NestedType>> nesting,
            final Map<DataFixerType, List<NestedType>> nestedMaps
    ) {
        this.targetDataVersion = targetDataVersion;
        this.encodedTargetVersion = DataConverter.encodeVersions(targetDataVersion, 0);
        this.registries = registries;
        this.walkers = walkers;
        this.nesting = nesting;
        this.nestedMaps = nestedMaps;
    }

    public int targetDataVersion() {
        return targetDataVersion;
    }

    @Nullable public MapType update(final DataFixerType type, final @Nullable MapType data, final int sourceDataVersion) {
        if (data == null) {
            return null;
        }
        MapType current = data;

        final DataFixerRegistry registry = registries.get(type);
        if (registry != null) {
            current = registry.apply(current, sourceDataVersion);
        }

        final List<DataWalker<MapType>> walkers = this.walkers.get(type);
        if (walkers != null) {
            for (final DataWalker<MapType> walker : walkers) {
                current = walker.walk(current, sourceDataVersion, encodedTargetVersion);
            }
        }

        if (current == null) {
            return null;
        }

        final List<NestedType> rules = nesting.get(type);
        if (rules != null) {
            for (final NestedType rule : rules) {
                final ListType list = current.getListUnchecked(rule.key(), null);
                if (list != null) {
                    updateList(rule.nestedType(), list, sourceDataVersion);
                    current.setList(rule.key(), list);
                }
            }
        }

        final List<NestedType> mapRules = nestedMaps.get(type);
        if (mapRules != null) {
            for (final NestedType rule : mapRules) {
                final MapType nested = current.getMap(rule.key(), null);
                if (nested != null) {
                    final MapType updated = update(rule.nestedType(), nested, sourceDataVersion);
                    if (updated != null) {
                        current.setMap(rule.key(), updated);
                    }
                }
            }
        }

        return current;
    }

    public void updateList(final DataFixerType type, final @Nullable ListType list, final int sourceDataVersion) {
        if (list == null) {
            return;
        }
        for (int i = 0, len = list.size(); i < len; ++i) {
            final MapType element = list.getMap(i, null);
            if (element != null) {
                final MapType updated = update(type, element, sourceDataVersion);
                if (updated != null) {
                    list.setMap(i, updated);
                }
            }
        }
    }
}
