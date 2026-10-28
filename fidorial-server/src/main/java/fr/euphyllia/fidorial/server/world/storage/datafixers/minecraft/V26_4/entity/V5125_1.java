package fr.euphyllia.fidorial.server.world.storage.datafixers.minecraft.V26_4.entity;

import ca.spottedleaf.converter.DataConverter;
import ca.spottedleaf.converter.types.ListType;
import ca.spottedleaf.converter.types.MapType;
import fr.euphyllia.fidorial.server.world.storage.datafixers.minecraft.V26_4.item.V5125;
import org.jspecify.annotations.Nullable;

public final class V5125_1 extends DataConverter<MapType, MapType> {

    public static final int TO_VERSION = V5125.TO_VERSION;

    public V5125_1() {
        super(TO_VERSION, 1);
    }

    @Override
    @Nullable public MapType convert(final @Nullable MapType entityChunk, final long sourceVersion, final long toVersion) {
        if (entityChunk == null) {
            return null;
        }
        final ListType entities = entityChunk.getListUnchecked("Entities", null);
        if (entities == null) {
            return entityChunk;
        }
        for (int i = 0, len = entities.size(); i < len; ++i) {
            final MapType entity = entities.getMap(i, null);
            if (entity != null) {
                V5125.upgrade(entity.getMap("Item", null));
            }
        }
        return entityChunk;
    }
}
