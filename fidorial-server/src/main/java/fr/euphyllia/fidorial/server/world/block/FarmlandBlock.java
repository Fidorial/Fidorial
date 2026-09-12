package fr.euphyllia.fidorial.server.world.block;

import fr.euphyllia.fidorial.server.item.Hoes;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.interaction.FidorialBlockInteractionContext;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.block.interaction.BlockInteractionContext;
import fr.fidorial.world.block.interaction.InteractionResult;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class FarmlandBlock {

    public static final Key KEY = BlockTypeKeys.FARMLAND.key();
    public static final String MOISTURE = "moisture";
    public static final int MAX_MOISTURE = 7;
    public static final int WATER_RANGE = 4;

    private static final Set<Key> TO_FARMLAND = Set.of(
            BlockTypeKeys.DIRT.key(),
            BlockTypeKeys.GRASS_BLOCK.key(),
            BlockTypeKeys.DIRT_PATH.key());

    private static final Set<Key> TO_DIRT = Set.of(
            BlockTypeKeys.COARSE_DIRT.key(),
            BlockTypeKeys.ROOTED_DIRT.key());

    private static final Set<Key> TILLABLE = Stream.concat(TO_FARMLAND.stream(), TO_DIRT.stream())
            .collect(Collectors.toUnmodifiableSet());

    private static final Sound TILL_SOUND =
            Sound.sound(SoundEvents.HOE_TILL, Sound.Source.BLOCK, 1.0f, 1.0f);

    private static final ComponentLogger LOGGER = ComponentLogger.logger(FarmlandBlock.class);

    private FarmlandBlock() {
        throw new UnsupportedOperationException("FarmlandBlock cannot be instantiated.");
    }

    public static Set<Key> tillableBlocks() {
        return TILLABLE;
    }

    public static boolean is(final BlockState state) {
        return KEY.equals(state.name());
    }

    public static int moisture(final BlockState state) {
        final String raw = state.properties().get(MOISTURE);
        if (raw == null) {
            return 0;
        }
        try {
            return Integer.parseInt(raw);
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    public static BlockState withMoisture(final int moisture) {
        final int clamped = Math.clamp(moisture, 0, MAX_MOISTURE);
        return BlockState.of(KEY, Map.of(MOISTURE, Integer.toString(clamped)));
    }

    public static boolean isHydrated(final ServerWorld world, final BlockPos pos) {
        for (int dx = -WATER_RANGE; dx <= WATER_RANGE; dx++) {
            for (int dz = -WATER_RANGE; dz <= WATER_RANGE; dz++) {
                for (int dy = 0; dy <= 1; dy++) {
                    final BlockState state = FidorialBlockInteractionContext.readState(
                            world, pos.offset(dx, dy, dz));
                    if (isWater(state)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isWater(final BlockState state) {
        return BlockTypeKeys.WATER.key().equals(state.name())
                || "true".equals(state.properties().get("waterlogged"));
    }

    public static InteractionResult till(final BlockInteractionContext context) {
        if (!(context instanceof final FidorialBlockInteractionContext ctx)) {
            return InteractionResult.PASS;
        }
        if (!Hoes.is(ctx.heldItem())) {
            return InteractionResult.PASS;
        }

        if (!ctx.stateAt(0, 1, 0).isAir()) {
            return InteractionResult.PASS;
        }

        if (ctx.face() == BlockFace.DOWN) {
            return InteractionResult.PASS;
        }

        final BlockState tilled = tilledState(ctx.world(), ctx.pos(), ctx.state());
        if (tilled == null) {
            return InteractionResult.PASS;
        }
        if (!ctx.setBlock(tilled)) {
            return InteractionResult.PASS;
        }
        ctx.playSound(TILL_SOUND);
        return InteractionResult.SUCCESS;
    }

    private static @Nullable BlockState tilledState(
            final ServerWorld world, final BlockPos pos, final BlockState clicked) {
        final Key name = clicked.name();
        if (TO_FARMLAND.contains(name)) {
            return withMoisture(isHydrated(world, pos) ? MAX_MOISTURE : 0);
        }
        if (TO_DIRT.contains(name)) {
            return BlockState.of(BlockTypeKeys.DIRT.key());
        }
        return null;
    }
}
