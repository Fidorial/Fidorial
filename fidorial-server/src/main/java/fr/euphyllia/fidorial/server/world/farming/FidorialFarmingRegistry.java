package fr.euphyllia.fidorial.server.world.farming;

import com.google.common.base.Preconditions;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.farming.FarmingBehaviour;
import fr.fidorial.world.farming.FarmingRegistry;
import fr.fidorial.world.farming.Tillage;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;

/**
 * The server's {@link FarmingRegistry}.
 *
 * <p>Each thing declared, the behaviour of a block type, the plant of a seed, a fertilizer, a hoe,
 * the tillage of a soil, is a stack of declarations under one key: the built-in one at the bottom,
 * then those of plugins in the order they came. The one on top is in effect, and is what the
 * region threads read, without a lock.</p>
 */
public final class FidorialFarmingRegistry implements FarmingRegistry {

    private final Declarations<TypedKey<BlockType>, FarmingBehaviour> behaviours = new Declarations<>();
    private final Declarations<TypedKey<Item>, TypedKey<BlockType>> seeds = new Declarations<>();
    private final Declarations<TypedKey<Item>, Boolean> fertilizers = new Declarations<>();
    private final Declarations<TypedKey<Item>, Boolean> hoes = new Declarations<>();
    private final Declarations<TypedKey<BlockType>, Tillage> tillages = new Declarations<>();
    private final List<TaggedTillage> taggedTillages = new CopyOnWriteArrayList<>();
    private volatile BiPredicate<TypedKey<Item>, TagKey<Item>> itemTags = (_, _) -> false;
    private volatile BiPredicate<TypedKey<BlockType>, TagKey<BlockType>> blockTags = (_, _) -> false;
    private volatile @Nullable TagKey<Item> hoeTag;

    /**
     * Sets how to tell whether a block type belongs to a tag, for the soils declared as a tag.
     *
     * @param tags how to tell whether a block type belongs to a tag
     */
    public void blockTags(final BiPredicate<TypedKey<BlockType>, TagKey<BlockType>> tags) {
        this.blockTags = tags;
    }

    /**
     * Makes the items of a tag hoes, without each being declared.
     *
     * @param tags how to tell whether an item belongs to a tag
     * @param tag  the tag of the hoes
     */
    public void hoeTag(final BiPredicate<TypedKey<Item>, TagKey<Item>> tags, final TagKey<Item> tag) {
        this.itemTags = tags;
        this.hoeTag = tag;
    }

    @Override
    public void attach(final TypedKey<BlockType> block, final FarmingBehaviour behaviour, final Plugin owner) {
        Preconditions.checkArgument(block != null, "The block passed to FarmingRegistry.attach() must not be null");
        Preconditions.checkArgument(behaviour != null, "The behaviour passed to FarmingRegistry.attach() must not be null");
        Preconditions.checkArgument(owner != null, "The owner passed to FarmingRegistry.attach() must not be null");
        behaviours.declare(block, behaviour, owner);
    }

    /**
     * Sets the built-in behaviour of a block type: in effect while no plugin attached one, and again
     * once they all detached theirs.
     */
    public void attachBuiltin(final TypedKey<BlockType> block, final FarmingBehaviour behaviour) {
        behaviours.declare(block, behaviour, null);
    }

    @Override
    public boolean detach(final TypedKey<BlockType> block, final Plugin owner) {
        Preconditions.checkArgument(block != null, "The block passed to FarmingRegistry.detach() must not be null");
        Preconditions.checkArgument(owner != null, "The owner passed to FarmingRegistry.detach() must not be null");
        return behaviours.release(block, owner);
    }

    @Override
    public void detachAll(final Plugin owner) {
        Preconditions.checkArgument(owner != null, "The owner passed to FarmingRegistry.detachAll() must not be null");
        behaviours.releaseAll(owner);
        seeds.releaseAll(owner);
        fertilizers.releaseAll(owner);
        hoes.releaseAll(owner);
        tillages.releaseAll(owner);
    }

    @Override
    public Optional<FarmingBehaviour> behaviour(final TypedKey<BlockType> block) {
        return Optional.ofNullable(behaviours.get(block));
    }

    /**
     * {@return the behaviour in effect for a block type, {@code null} for a block taking no part in
     * farming}: what the engine reads on every random tick, without wrapping it
     */
    public @Nullable FarmingBehaviour behaviourOrNull(final TypedKey<BlockType> block) {
        return behaviours.get(block);
    }

    @Override
    public Set<TypedKey<BlockType>> blocks() {
        return behaviours.keys();
    }

    @Override
    public void seed(final TypedKey<Item> item, final TypedKey<BlockType> plant, final Plugin owner) {
        Preconditions.checkArgument(item != null, "The item passed to FarmingRegistry.seed() must not be null");
        Preconditions.checkArgument(plant != null, "The plant passed to FarmingRegistry.seed() must not be null");
        Preconditions.checkArgument(owner != null, "The owner passed to FarmingRegistry.seed() must not be null");
        seeds.declare(item, plant, owner);
    }

    /**
     * Declares a built-in seed.
     */
    public void seedBuiltin(final TypedKey<Item> item, final TypedKey<BlockType> plant) {
        seeds.declare(item, plant, null);
    }

    @Override
    public Optional<TypedKey<BlockType>> plant(final TypedKey<Item> item) {
        return Optional.ofNullable(seeds.get(item));
    }

    @Override
    public void fertilizer(final TypedKey<Item> item, final Plugin owner) {
        Preconditions.checkArgument(item != null, "The item passed to FarmingRegistry.fertilizer() must not be null");
        Preconditions.checkArgument(owner != null, "The owner passed to FarmingRegistry.fertilizer() must not be null");
        fertilizers.declare(item, Boolean.TRUE, owner);
    }

    /**
     * Declares a built-in fertilizer.
     */
    public void fertilizerBuiltin(final TypedKey<Item> item) {
        fertilizers.declare(item, Boolean.TRUE, null);
    }

    @Override
    public boolean isFertilizer(final TypedKey<Item> item) {
        return fertilizers.get(item) != null;
    }

    @Override
    public void hoe(final TypedKey<Item> item, final Plugin owner) {
        Preconditions.checkArgument(item != null, "The item passed to FarmingRegistry.hoe() must not be null");
        Preconditions.checkArgument(owner != null, "The owner passed to FarmingRegistry.hoe() must not be null");
        hoes.declare(item, Boolean.TRUE, owner);
    }

    @Override
    public boolean isHoe(final TypedKey<Item> item) {
        if (hoes.get(item) != null) {
            return true;
        }
        final TagKey<Item> tag = hoeTag;
        return tag != null && itemTags.test(item, tag);
    }

    @Override
    public void tillable(final TypedKey<BlockType> soil, final Tillage tillage, final Plugin owner) {
        Preconditions.checkArgument(soil != null, "The soil passed to FarmingRegistry.tillable() must not be null");
        Preconditions.checkArgument(tillage != null, "The tillage passed to FarmingRegistry.tillable() must not be null");
        Preconditions.checkArgument(owner != null, "The owner passed to FarmingRegistry.tillable() must not be null");
        tillages.declare(soil, tillage, owner);
    }

    /**
     * Declares what a hoe does to a built-in block type.
     */
    public void tillableBuiltin(final TypedKey<BlockType> soil, final Tillage tillage) {
        tillages.declare(soil, tillage, null);
    }

    /**
     * Declares what a hoe does to every block type of a tag, whatever the tag holds when the hoe
     * is used. A block type declared on its own comes first, and among tags the first declared.
     */
    public void tillableTagBuiltin(final TagKey<BlockType> soils, final Tillage tillage) {
        taggedTillages.add(new TaggedTillage(soils, tillage));
    }

    @Override
    public Optional<Tillage> tillage(final TypedKey<BlockType> soil) {
        final Tillage declared = tillages.get(soil);
        if (declared != null) {
            return Optional.of(declared);
        }
        final BiPredicate<TypedKey<BlockType>, TagKey<BlockType>> tags = blockTags;
        for (final TaggedTillage tagged : taggedTillages) {
            if (tags.test(soil, tagged.soils())) {
                return Optional.of(tagged.tillage());
            }
        }
        return Optional.empty();
    }

    /**
     * The declarations made under each key, and the one in effect for each.
     *
     * @param <K> what is declared about
     * @param <V> what is declared
     */
    private static final class Declarations<K, V> {

        private final Map<K, V> active = new ConcurrentHashMap<>();
        private final Map<K, List<Declared<V>>> stacks = new ConcurrentHashMap<>();

        /**
         * @param owner the plugin declaring, {@code null} for a built-in declaration, which stays at
         *              the bottom of the stack whenever it is made
         */
        void declare(final K key, final V value, final @Nullable Plugin owner) {
            stacks.compute(key, (_, stack) -> {
                final List<Declared<V>> declared = stack == null ? new ArrayList<>(1) : stack;
                if (owner == null) {
                    declared.removeIf(entry -> entry.owner() == null);
                    declared.addFirst(new Declared<>(value, null));
                } else {
                    declared.add(new Declared<>(value, owner));
                }
                active.put(key, declared.getLast().value());
                return declared;
            });
        }

        boolean release(final K key, final Plugin owner) {
            final boolean[] released = new boolean[1];
            stacks.computeIfPresent(key, (_, stack) -> {
                released[0] = stack.removeIf(entry -> entry.owner() == owner);
                if (stack.isEmpty()) {
                    active.remove(key);
                    return null;
                }
                active.put(key, stack.getLast().value());
                return stack;
            });
            return released[0];
        }

        void releaseAll(final Plugin owner) {
            for (final K key : List.copyOf(stacks.keySet())) {
                release(key, owner);
            }
        }

        @Nullable V get(final K key) {
            return active.get(key);
        }

        Set<K> keys() {
            return Set.copyOf(active.keySet());
        }
    }

    private record Declared<V>(V value, @Nullable Plugin owner) {
    }

    private record TaggedTillage(TagKey<BlockType> soils, Tillage tillage) {
    }
}
