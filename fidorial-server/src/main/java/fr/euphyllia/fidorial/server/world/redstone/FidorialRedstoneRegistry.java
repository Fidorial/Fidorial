package fr.euphyllia.fidorial.server.world.redstone;

import com.google.common.base.Preconditions;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneRegistry;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class FidorialRedstoneRegistry implements RedstoneRegistry {

    private final Map<TypedKey<BlockType>, RedstoneBehaviour> active = new ConcurrentHashMap<>();
    private final Map<TypedKey<BlockType>, List<Attached>> attached = new ConcurrentHashMap<>();
    private final Map<TypedKey<BlockType>, RedstoneBehaviour> vanilla = new ConcurrentHashMap<>();

    @Override
    public void attach(final TypedKey<BlockType> block, final RedstoneBehaviour behaviour, final Plugin owner) {
        Preconditions.checkArgument(block != null, "The block passed to RedstoneRegistry.attach() must not be null");
        Preconditions.checkArgument(behaviour != null, "The behaviour passed to RedstoneRegistry.attach() must not be null");
        Preconditions.checkArgument(owner != null, "The owner passed to RedstoneRegistry.attach() must not be null");
        attached.computeIfAbsent(block, _ -> new ArrayList<>(1)).add(new Attached(behaviour, owner));
        active.put(block, behaviour);
    }

    @Override
    public boolean detach(final TypedKey<BlockType> block, final Plugin owner) {
        final List<Attached> stack = attached.get(block);
        if (stack == null || !stack.removeIf(entry -> entry.owner() == owner)) {
            return false;
        }
        refresh(block, stack);
        return true;
    }

    @Override
    public void detachAll(final Plugin owner) {
        Preconditions.checkArgument(owner != null, "The owner passed to RedstoneRegistry.detachAll() must not be null");
        for (final TypedKey<BlockType> block : List.copyOf(attached.keySet())) {
            final List<Attached> stack = attached.get(block);
            if (stack != null && stack.removeIf(entry -> entry.owner() == owner)) {
                refresh(block, stack);
            }
        }
    }

    /**
     * Sets the vanilla behaviour of a block type: in effect while no plugin attached one, and again
     * once they all detached theirs.
     */
    public void attachVanilla(final TypedKey<BlockType> block, final RedstoneBehaviour behaviour) {
        vanilla.put(block, behaviour);
        if (!attached.containsKey(block)) {
            active.put(block, behaviour);
        }
    }

    private void refresh(final TypedKey<BlockType> block, final List<Attached> stack) {
        if (!stack.isEmpty()) {
            active.put(block, stack.getLast().behaviour());
            return;
        }
        attached.remove(block);
        final RedstoneBehaviour fallback = vanilla.get(block);
        if (fallback != null) {
            active.put(block, fallback);
        } else {
            active.remove(block);
        }
    }

    @Override
    public Optional<RedstoneBehaviour> behaviour(final TypedKey<BlockType> block) {
        return Optional.ofNullable(active.get(block));
    }

    @Override
    public Set<TypedKey<BlockType>> blocks() {
        return Set.copyOf(active.keySet());
    }

    public @Nullable RedstoneBehaviour behaviourOrNull(final TypedKey<BlockType> block) {
        return active.get(block);
    }

    private record Attached(RedstoneBehaviour behaviour, Plugin owner) {

    }
}
