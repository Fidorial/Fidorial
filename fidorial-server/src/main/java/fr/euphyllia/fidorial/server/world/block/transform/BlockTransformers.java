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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BlockTransformers {

    private static final Sound TILL_SOUND =
            Sound.sound(SoundEvents.HOE_TILL, Sound.Source.BLOCK, 1.0f, 1.0f);

    private static final Map<Key, List<BlockTransformerRule>> ENTRIES = Map.of(
            BlockTransformerKeys.HOE.key(), hoe());

    private static final Set<Key> TRANSFORMABLE_BLOCKS = transformableBlocks(ENTRIES);

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
        return TRANSFORMABLE_BLOCKS;
    }

    public static InteractionResult use(final BlockInteractionContext context) {
        if (!(context instanceof final FidorialBlockInteractionContext ctx)) {
            return InteractionResult.PASS;
        }
        final Key transformer = of(ctx.heldItem());
        if (transformer == null) {
            return InteractionResult.PASS;
        }

        final Key block = ctx.state().name();
        for (final BlockTransformerRule rule : rules(transformer)) {
            if (!rule.accepts(block, ctx.face())) {
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
                        Set.of(BlockTypeKeys.DIRT.key(),
                                BlockTypeKeys.GRASS_BLOCK.key(),
                                BlockTypeKeys.DIRT_PATH.key()),
                        ctx -> airAbove(ctx) ? farmland(ctx) : null,
                        TILL_SOUND, notFromBelow),
                new BlockTransformerRule(
                        Set.of(BlockTypeKeys.COARSE_DIRT.key()),
                        ctx -> airAbove(ctx) ? BlockState.of(BlockTypeKeys.DIRT.key()) : null,
                        TILL_SOUND, notFromBelow),
                // TODO: loot minecraft:till/rooted_dirt, dropped from the clicked face.
                new BlockTransformerRule(
                        Set.of(BlockTypeKeys.ROOTED_DIRT.key()),
                        _ -> BlockState.of(BlockTypeKeys.DIRT.key()),
                        TILL_SOUND, Set.of()));
    }

    private static Set<Key> transformableBlocks(final Map<Key, List<BlockTransformerRule>> entries) {
        final Set<Key> blocks = new HashSet<>();
        for (final List<BlockTransformerRule> rules : entries.values()) {
            for (final BlockTransformerRule rule : rules) {
                blocks.addAll(rule.from());
            }
        }
        return Set.copyOf(blocks);
    }

    private static BlockState farmland(final FidorialBlockInteractionContext ctx) {
        final boolean hydrated = FarmlandBlock.isHydrated(ctx.world(), ctx.pos());
        return FarmlandBlock.withMoisture(hydrated ? FarmlandBlock.MAX_MOISTURE : 0);
    }

    private static boolean airAbove(final FidorialBlockInteractionContext ctx) {
        return ctx.stateAt(0, 1, 0).isAir();
    }
}
