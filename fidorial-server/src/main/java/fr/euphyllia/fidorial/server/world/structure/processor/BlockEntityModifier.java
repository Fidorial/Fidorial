package fr.euphyllia.fidorial.server.world.structure.processor;

import fr.euphyllia.fidorial.server.world.structure.math.LegacyRandom;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jspecify.annotations.Nullable;

public interface BlockEntityModifier {

    BlockEntityModifier PASSTHROUGH = (random, nbt) -> nbt;
    BlockEntityModifier CLEAR = (random, nbt) -> CompoundBinaryTag.empty();

    @Nullable CompoundBinaryTag apply(LegacyRandom random, @Nullable CompoundBinaryTag nbt);

    static BlockEntityModifier appendStatic(final CompoundBinaryTag data) {
        return (random, nbt) -> {
            final CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
            if (nbt != null) {
                builder.put(nbt);
            }
            builder.put(data);
            return builder.build();
        };
    }

    static BlockEntityModifier appendLoot(final Key lootTable) {
        return (random, nbt) -> {
            final CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
            if (nbt != null) {
                builder.put(nbt);
            }
            builder.putString("LootTable", lootTable.asString());
            builder.putLong("LootTableSeed", random.nextLong());
            return builder.build();
        };
    }
}
