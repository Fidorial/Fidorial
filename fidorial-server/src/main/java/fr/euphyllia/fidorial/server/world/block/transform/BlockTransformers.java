package fr.euphyllia.fidorial.server.world.block.transform;

import fr.euphyllia.fidorial.server.world.block.FarmlandBlock;
import fr.euphyllia.fidorial.server.world.block.interaction.FidorialBlockInteractionContext;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.item.ItemDefaults;
import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.keys.BlockTransformerKeys;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.interaction.BlockInteractionContext;
import fr.fidorial.world.block.interaction.InteractionResult;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockTransformers {

    private static final Set<Key> TO_FARMLAND = Set.of(
            BlockTypeKeys.DIRT.key(),
            BlockTypeKeys.GRASS_BLOCK.key(),
            BlockTypeKeys.DIRT_PATH.key());

    private static final Sound TILL_SOUND =
            Sound.sound(SoundEvents.HOE_TILL, Sound.Source.BLOCK, 1.0f, 1.0f);

    private static final Map<Key, List<BlockTransformerRule>> ENTRIES = Map.of(
            BlockTransformerKeys.HOE.key(), hoe());

    private BlockTransformers() {
        throw new UnsupportedOperationException("BlockTransformers cannot be instantiated.");
    }

    public static List<BlockTransformerRule> rules(final Key transformer) {
        return ENTRIES.getOrDefault(transformer, List.of());
    }

    public static @Nullable Key of(final ItemStack stack) {
        return stack.isEmpty() ? null : ItemDefaults.blockTransformer(stack.id(), stack);
    }

    public static Set<Key> transformableBlocks() {
        return Stream.concat(TO_FARMLAND.stream(), Stream.of(
                        BlockTypeKeys.COARSE_DIRT.key(),
                        BlockTypeKeys.ROOTED_DIRT.key()))
                .collect(Collectors.toUnmodifiableSet());
    }

    public static InteractionResult use(final BlockInteractionContext context) {
        if (!(context instanceof final FidorialBlockInteractionContext ctx)) {
            return InteractionResult.PASS;
        }
        final Key transformer = of(ctx.heldItem());
        if (transformer == null) {
            return InteractionResult.PASS;
        }

        for (final BlockTransformerRule rule : rules(transformer)) {
            if (rule.disallowedFaces().contains(ctx.face())) {
                continue;
            }
            final BlockState transformed = rule.provider().provide(ctx);
            if (transformed == null) {
                continue;
            }
            if (!ctx.setBlock(transformed)) {
                return InteractionResult.PASS;
            }
            if (rule.sound() != null) {
                ctx.playSound(rule.sound());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static List<BlockTransformerRule> hoe() {
        final Set<BlockFace> notFromBelow = Set.of(BlockFace.DOWN);
        return List.of(
                new BlockTransformerRule(
                        ctx -> is(ctx, TO_FARMLAND) && airAbove(ctx) ? farmland(ctx) : null,
                        TILL_SOUND, notFromBelow),
                new BlockTransformerRule(
                        ctx -> is(ctx, BlockTypeKeys.COARSE_DIRT.key()) && airAbove(ctx)
                                ? BlockState.of(BlockTypeKeys.DIRT.key())
                                : null,
                        TILL_SOUND, notFromBelow),
                // TODO: loot minecraft:till/rooted_dirt, dropped from the clicked face.
                new BlockTransformerRule(
                        ctx -> is(ctx, BlockTypeKeys.ROOTED_DIRT.key())
                                ? BlockState.of(BlockTypeKeys.DIRT.key())
                                : null,
                        TILL_SOUND, Set.of()));
    }

    private static boolean is(final FidorialBlockInteractionContext ctx, final Key block) {
        return block.equals(ctx.state().name());
    }

    private static boolean is(final FidorialBlockInteractionContext ctx, final Set<Key> blocks) {
        return blocks.contains(ctx.state().name());
    }

    private static BlockState farmland(final FidorialBlockInteractionContext ctx) {
        final boolean hydrated = FarmlandBlock.isHydrated(ctx.world(), ctx.pos());
        return FarmlandBlock.withMoisture(hydrated ? FarmlandBlock.MAX_MOISTURE : 0);
    }

    private static boolean airAbove(final FidorialBlockInteractionContext ctx) {
        return ctx.stateAt(0, 1, 0).isAir();
    }
}
