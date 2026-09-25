package fr.euphyllia.fidorial.server.world.storage.datafixers.minecraft.V26_4;

import fr.euphyllia.fidorial.server.world.storage.datafixers.DataFixer;
import fr.euphyllia.fidorial.server.world.storage.datafixers.DataFixerBuilder;
import fr.euphyllia.fidorial.server.world.storage.datafixers.DataFixerType;
import fr.euphyllia.fidorial.server.world.storage.datafixers.minecraft.V26_4.chunk.V5119;

public final class DataFixers {

    private DataFixers() {
        throw new UnsupportedOperationException("DataFixers cannot be instantiated.");
    }

    public static DataFixer buildDataFixers() {
        return new DataFixerBuilder()
                .addFixer(DataFixerType.CHUNK, new V5119())
                .build();
    }
}
