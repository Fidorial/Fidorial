package fr.euphyllia.fidorial.server.world.block.crop;

import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.world.block.crop.CropRegistry;
import fr.fidorial.world.block.crop.CropType;
import fr.fidorial.world.block.interaction.BlockInteractionHandler;
import fr.fidorial.world.block.interaction.BlockInteractionRegistry;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class FidorialCropRegistry implements CropRegistry {

    public static final Object BUILT_IN = new Object();

    private final BlockInteractionRegistry interactions;
    private final BlockInteractionHandler plantHandler;

    private final Map<Key, Entry> bySeed = new ConcurrentHashMap<>();
    private final Map<Key, CropType> byBlock = new ConcurrentHashMap<>();
    private final Set<Key> attachedSoils = ConcurrentHashMap.newKeySet();

    public FidorialCropRegistry(final BlockInteractionRegistry interactions) {
        this.interactions = interactions;
        this.plantHandler = new CropPlanting(this)::plant;
    }

    public static FidorialCropRegistry createDefault(final BlockInteractionRegistry interactions) {
        final FidorialCropRegistry registry = new FidorialCropRegistry(interactions);
        registry.register(wheat(), BUILT_IN);
        return registry;
    }

    private static CropType wheat() {
        return CropType.builder(ItemKeys.WHEAT_SEEDS.key(), BlockTypeKeys.WHEAT.key())
                .maxAge(7)
                .averageTicksPerStage(600)
                .minLight(8)
                .requiresMoistSoil(true)
                .build();
    }

    @Override
    public void register(final CropType crop, final Object owner) {
        final Entry previous = bySeed.put(crop.seed(), new Entry(crop, owner, bySeed.get(crop.seed())));
        if (previous != null) {
            byBlock.remove(previous.crop().block(), previous.crop());
        }
        byBlock.put(crop.block(), crop);
        attachSoils(crop);
    }

    @Override
    public boolean unregister(final Key seed, final Object owner) {
        final Entry entry = bySeed.get(seed);
        if (entry == null || entry.owner() != owner) {
            return false;
        }
        byBlock.remove(entry.crop().block(), entry.crop());

        final Entry restored = firstNotOwnedBy(entry.previous(), owner);
        if (restored == null) {
            bySeed.remove(seed, entry);
        } else {
            bySeed.put(seed, restored);
            byBlock.put(restored.crop().block(), restored.crop());
            attachSoils(restored.crop());
        }
        return true;
    }

    private static @Nullable Entry firstNotOwnedBy(final @Nullable Entry entry, final Object owner) {
        Entry candidate = entry;
        while (candidate != null && candidate.owner() == owner) {
            candidate = candidate.previous();
        }
        return candidate;
    }

    @Override
    public void unregisterAll(final Object owner) {
        for (final Key seed : Set.copyOf(bySeed.keySet())) {
            unregister(seed, owner);
        }
    }

    @Override
    public @Nullable CropType bySeed(final Key seed) {
        final Entry entry = bySeed.get(seed);
        return entry == null ? null : entry.crop();
    }

    @Override
    public @Nullable CropType byBlock(final Key block) {
        return byBlock.get(block);
    }

    @Override
    public Collection<CropType> crops() {
        final List<CropType> result = new ArrayList<>(bySeed.size());
        for (final Entry entry : bySeed.values()) {
            result.add(entry.crop());
        }
        return List.copyOf(result);
    }

    @Override
    public Set<Key> soils() {
        final Set<Key> result = new LinkedHashSet<>();
        for (final Entry entry : bySeed.values()) {
            result.addAll(entry.crop().soils());
        }
        return Set.copyOf(result);
    }

    private void attachSoils(final CropType crop) {
        for (final Key soil : crop.soils()) {
            if (attachedSoils.add(soil)) {
                interactions.register(soil, plantHandler, BUILT_IN);
            }
        }
    }

    private record Entry(CropType crop, Object owner, @Nullable Entry previous) {
    }
}
