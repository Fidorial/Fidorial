package fr.euphyllia.fidorial.server.world.structure;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.Map;

@FunctionalInterface
public interface BlockValidator {

    @Nullable BlockState resolve(Key name, Map<String, String> properties);

    static BlockValidator permissive() {
        return BlockState::of;
    }
}
