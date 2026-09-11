package fr.euphyllia.fidorial.server.world.structure.gen;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.structure.BlockValidator;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockProperty;
import fr.fidorial.world.block.BlockRegistry;
import fr.fidorial.world.block.BlockType;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class RegistryBlockValidator implements BlockValidator {

    private record Request(Key name, Map<String, String> properties) {
    }

    private final BlockRegistry registry;
    private final Map<Request, Optional<BlockState>> cache = new ConcurrentHashMap<>();

    public RegistryBlockValidator(final BlockRegistry registry) {
        this.registry = registry;
    }

    @Override
    public @Nullable BlockState resolve(final Key name, final Map<String, String> properties) {
        return cache.computeIfAbsent(new Request(name, Map.copyOf(properties)), this::compute).orElse(null);
    }

    private Optional<BlockState> compute(final Request request) {
        final BlockType type = registry.type(request.name()).orElse(null);
        if (type == null) {
            return Optional.empty();
        }
        final Map<String, String> valid = new HashMap<>();
        for (final Map.Entry<String, String> entry : request.properties().entrySet()) {
            final BlockProperty property = type.property(entry.getKey());
            if (property != null && property.isValid(entry.getValue())) {
                valid.put(entry.getKey(), entry.getValue());
            }
        }
        BlockData data = type.dataOrNull(valid);
        if (data == null) {
            data = type.defaultData();
        }
        if (data == null) {
            return Optional.of(BlockState.of(request.name(), valid));
        }
        final Map<String, String> full = new HashMap<>();
        data.propertyMap().forEach((property, value) -> {
            if (value != null) {
                full.put(property, value);
            }
        });
        return Optional.of(BlockState.of(data.key(), full));
    }
}
