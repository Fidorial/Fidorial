package fr.euphyllia.fidorial.server.world.block;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.inventory.EnderChestMenu;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.interaction.FidorialBlockInteractionContext;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.event.player.PlayerOpenEnderChestEvent;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.block.data.Directional;
import fr.fidorial.world.block.data.Waterlogged;
import fr.fidorial.world.block.interaction.BlockInteractionContext;
import fr.fidorial.world.block.interaction.InteractionResult;
import net.kyori.adventure.key.Key;

import java.util.Objects;

public final class EnderChestBlock implements BlockBehaviour {

    public static final Key KEY = BlockTypeKeys.ENDER_CHEST.key();

    public static final int LIGHT_EMISSION = 7;

    public static final int OBSIDIAN_DROPS = 8;

    private EnderChestBlock() {
        throw new UnsupportedOperationException("EnderChestBlock cannot be instantiated.");
    }

    public static boolean is(final BlockState state) {
        return KEY.equals(state.name());
    }

    public static boolean isBlockedAbove(final ServerWorld world, final BlockPos pos) {
        return ChestBlocks.isBlockedAbove(world, pos);
    }

    public static InteractionResult open(final BlockInteractionContext context) {
        if (!(context instanceof final FidorialBlockInteractionContext ctx)) {
            return InteractionResult.PASS;
        }
        final FidorialServer server = ctx.server();
        final ServerWorld world = ctx.world();
        final ServerPlayer player = ctx.player();
        final BlockPos pos = ctx.pos();

        if (isBlockedAbove(world, pos)) {
            return InteractionResult.CONSUME;
        }

        final PlayerOpenEnderChestEvent event =
                server.events().post(new PlayerOpenEnderChestEvent(player, pos, player.enderChest()));
        if (event.isCancelled()) {
            return InteractionResult.CONSUME;
        }

        player.openMenu(new EnderChestMenu(player, player.allocateWindowId(), pos));
        server.chestViewers().open(pos, (opened, viewers) -> ChestBlocks.broadcastLid(server, world, opened, viewers));
        ChestBlocks.broadcastSound(server, world, pos, SoundEvents.ENDER_CHEST_OPEN);
        return InteractionResult.CONSUME;
    }

    @Override
    public BlockType type() {
        return Objects.requireNonNull(Blocks.type(KEY));
    }

    @Override
    public BlockData placementState(final BlockPlaceContext context) {
        final BlockFace facing = context.horizontalFacing().opposite();
        BlockData state = Objects.requireNonNull(type().defaultData());
        state = ((Directional) state).setFacing(facing);
        state = ((Waterlogged) state).setWaterlogged(context.intoWater());
        return state;
    }

    @Override
    public int lightEmission(final BlockData data) {
        return LIGHT_EMISSION;
    }

    @Override
    public int lightOpacity(final BlockData data) {
        return 0;
    }
}
