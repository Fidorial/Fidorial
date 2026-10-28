package fr.euphyllia.fidorial.server.world.storage.datafixers.minecraft.V26_4;

import fr.euphyllia.fidorial.server.world.storage.datafixers.DataFixer;
import fr.euphyllia.fidorial.server.world.storage.datafixers.DataFixerBuilder;
import fr.euphyllia.fidorial.server.world.storage.datafixers.DataFixerType;
import fr.euphyllia.fidorial.server.world.storage.datafixers.minecraft.V26_4.chunk.V5119;
import fr.euphyllia.fidorial.server.world.storage.datafixers.minecraft.V26_4.entity.V5125_1;
import fr.euphyllia.fidorial.server.world.storage.datafixers.minecraft.V26_4.item.V5125;

public final class DataFixers {

    private DataFixers() {
        throw new UnsupportedOperationException("DataFixers cannot be instantiated.");
    }

    public static DataFixer buildDataFixers() {
        return new DataFixerBuilder()
                .addFixer(DataFixerType.CHUNK, new V5119())
                // 26.4-snapshot-6
                .addFixer(DataFixerType.ITEM_STACK, new V5125())
                .addFixer(DataFixerType.ENTITY, new V5125_1())
                .nestList(DataFixerType.BLOCK_ENTITY, "Items", DataFixerType.ITEM_STACK)
                .nestList(DataFixerType.CHUNK, "block_entities", DataFixerType.BLOCK_ENTITY)
                .nestList(DataFixerType.PLAYER, "Inventory", DataFixerType.ITEM_STACK)
                .nestList(DataFixerType.PLAYER, "EnderItems", DataFixerType.ITEM_STACK)
                .build();
    }
}
