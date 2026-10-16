package fr.euphyllia.fidorial.server.world.storage.datafixers.fidorial.V26_4;

import fr.euphyllia.fidorial.server.world.storage.datafixers.DataFixer;
import fr.euphyllia.fidorial.server.world.storage.datafixers.DataFixerBuilder;
import fr.euphyllia.fidorial.server.world.storage.datafixers.DataFixerType;
import fr.euphyllia.fidorial.server.world.storage.datafixers.fidorial.V26_4.level.V5122;
import fr.euphyllia.fidorial.server.world.storage.datafixers.fidorial.V26_4.player.V5122_1;
import fr.euphyllia.fidorial.server.world.storage.datafixers.fidorial.V26_4.world_data.V5122_2;

public final class DataFixers {

    private DataFixers() {
        throw new UnsupportedOperationException("DataFixers cannot be instantiated.");
    }

    public static DataFixer buildDataFixers() {
        return new DataFixerBuilder()
                .addFixer(DataFixerType.LEVEL, new V5122())
                .addFixer(DataFixerType.PLAYER, new V5122_1())
                .addFixer(DataFixerType.WORLD_DATA, new V5122_2())
                .build();
    }
}
