package fr.euphyllia.fidorial.server.world.structure.gen;

import fr.euphyllia.fidorial.server.codecs.BlockStateCodecs;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.structure.BlockValidator;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.block.BlockRegistry;
import fr.fidorial.world.block.BlockType;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class RegistryBlockValidator implements BlockValidator {

    private record Request(TypedKey<BlockType> name, Map<String, String> properties) {
    }

    private final BlockRegistry registry;
    private final Map<Request, Optional<BlockState>> cache = new ConcurrentHashMap<>();

    public RegistryBlockValidator(final BlockRegistry registry) {
        this.registry = registry;
    }

    @Override
    public @Nullable BlockState resolve(final TypedKey<BlockType> name, final Map<String, String> properties) {
        return cache.computeIfAbsent(new Request(name, Map.copyOf(properties)), this::compute).orElse(null);
    }

    private Optional<BlockState> compute(final Request request) {
        final TypedKey<BlockType> name = request.name();
        final BlockType type = registry.type(name).orElse(null);
        if (type == null) {
            return Optional.empty();
        }
        return Optional.of(BlockState.of(BlockStateCodecs.withNames(type.defaultData(), request.properties())));
    }
}
