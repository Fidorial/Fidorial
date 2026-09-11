package fr.euphyllia.fidorial.server.world.structure.processor;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.structure.BlockValidator;
import fr.euphyllia.fidorial.server.world.structure.Keys;
import fr.euphyllia.fidorial.server.world.structure.StateParser;
import fr.euphyllia.fidorial.server.world.structure.math.LegacyRandom;
import fr.euphyllia.fidorial.server.world.structure.worldgen.Heightmap;
import fr.euphyllia.fidorial.server.world.structure.worldgen.IntProvider;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class Processors {

    public static final StructureProcessor NOP = (context, lx, ly, lz, current) -> current;
    public static final StructureProcessor IGNORE_STRUCTURE_BLOCK = ignore(Set.of(Keys.STRUCTURE_BLOCK));
    public static final StructureProcessor IGNORE_STRUCTURE_AND_AIR = ignore(Set.of(Keys.STRUCTURE_BLOCK, Keys.AIR));
    public static final StructureProcessor TERRAIN_MATCHING = gravity(Heightmap.WORLD_SURFACE_WG, -1);

    private Processors() {
    }

    public static StructureProcessor ignore(final Set<Key> blocks) {
        return (context, lx, ly, lz, current) -> blocks.contains(current.state().name()) ? null : current;
    }

    public static StructureProcessor jigsawReplacement(final BlockValidator validator) {
        final BlockState air = validator.resolve(Keys.AIR, java.util.Map.of());
        return (context, lx, ly, lz, current) -> {
            if (!current.state().name().equals(Keys.JIGSAW)) {
                return current;
            }
            final CompoundBinaryTag nbt = current.nbt();
            final String raw = nbt == null ? "minecraft:air" : nbt.getString("final_state", "minecraft:air");
            final StateParser.Parsed parsed = StateParser.parseString(raw);
            if (parsed != null && parsed.name().equals(Keys.STRUCTURE_VOID)) {
                return null;
            }
            BlockState state = StateParser.resolve(parsed, validator);
            if (state == null) {
                state = air;
            }
            return state == null ? null : new PlacedBlock(current.x(), current.y(), current.z(), state, null);
        };
    }

    public static StructureProcessor gravity(final Heightmap heightmap, final int offset) {
        return (context, lx, ly, lz, current) ->
                current.withY(context.height(heightmap, current.x(), current.z()) + offset + ly);
    }

    public static StructureProcessor blockRot(final float integrity, final @Nullable Set<Key> rottable) {
        return (context, lx, ly, lz, current) -> {
            if (rottable != null && !rottable.contains(current.state().name())) {
                return current;
            }
            final LegacyRandom random = LegacyRandom.atPosition(current.x(), current.y(), current.z());
            return random.nextFloat() <= integrity ? current : null;
        };
    }

    public static StructureProcessor protectedBlocks(final Set<Key> cannotReplace) {
        return (context, lx, ly, lz, current) ->
                cannotReplace.contains(context.existing(current.x(), current.y(), current.z()).name()) ? null : current;
    }

    public static StructureProcessor lavaSubmerged(final BlockValidator validator) {
        final BlockState lava = validator.resolve(Keys.LAVA, java.util.Map.of());
        return (context, lx, ly, lz, current) -> {
            final BlockState existing = context.existing(current.x(), current.y(), current.z());
            if (lava != null && existing.name().equals(Keys.LAVA) && !current.state().name().equals(Keys.LAVA)
                    && !isFullBlock(current.state())) {
                return new PlacedBlock(current.x(), current.y(), current.z(), lava, current.nbt());
            }
            return current;
        };
    }

    private static boolean isFullBlock(final BlockState state) {
        final String name = state.name().value();
        return !(name.endsWith("_slab") || name.endsWith("_stairs") || name.endsWith("_wall") || name.endsWith("_fence")
                || name.endsWith("_pane") || name.endsWith("_bars") || name.endsWith("_door") || name.endsWith("_trapdoor"));
    }

    public record Rule(
            RuleTest input,
            RuleTest location,
            PosRuleTest position,
            BlockState output,
            BlockEntityModifier modifier
    ) {
    }

    public static StructureProcessor rule(final List<Rule> rules) {
        final List<Rule> copy = List.copyOf(rules);
        return (context, lx, ly, lz, current) -> {
            final LegacyRandom random = LegacyRandom.atPosition(current.x(), current.y(), current.z());
            final BlockState location = context.existing(current.x(), current.y(), current.z());
            for (final Rule rule : copy) {
                if (rule.input().test(current.state(), random)
                        && rule.location().test(location, random)
                        && rule.position().test(lx, ly, lz, current.x(), current.y(), current.z(),
                        context.pivotX(), context.pivotY(), context.pivotZ(), random)) {
                    return current.withState(rule.output(), rule.modifier().apply(random, current.nbt()));
                }
            }
            return current;
        };
    }

    public static StructureProcessor capped(final StructureProcessor delegate, final IntProvider limit) {
        return new StructureProcessor() {
            @Override
            public PlacedBlock process(final ProcessContext context, final int lx, final int ly, final int lz, final PlacedBlock current) {
                return current;
            }

            @Override
            public boolean needsWholePiece() {
                return true;
            }

            @Override
            public void finish(final ProcessContext context, final List<int[]> originals, final List<PlacedBlock> processed) {
                if (limit.maxValue() == 0 || processed.isEmpty()) {
                    return;
                }
                final LegacyRandom random = new LegacyRandom(context.worldSeed()
                        ^ LegacyRandom.positionSeed(context.pivotX(), context.pivotY(), context.pivotZ()));
                final int count = Math.min(limit.sample(random), processed.size());
                if (count < 1) {
                    return;
                }
                final List<Integer> order = new ArrayList<>(processed.size());
                for (int i = 0; i < processed.size(); i++) {
                    order.add(i);
                }
                random.shuffle(order);
                int changed = 0;
                for (int n = 0; n < order.size() && changed < count; n++) {
                    final int index = order.get(n);
                    final int[] local = originals.get(index);
                    final PlacedBlock before = processed.get(index);
                    final PlacedBlock after = delegate.process(context, local[0], local[1], local[2], before);
                    if (after != null && !after.equals(before)) {
                        changed++;
                        processed.set(index, after);
                    }
                }
            }
        };
    }
}
