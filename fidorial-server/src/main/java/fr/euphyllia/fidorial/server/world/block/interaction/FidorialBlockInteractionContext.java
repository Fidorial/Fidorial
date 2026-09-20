package fr.euphyllia.fidorial.server.world.block.interaction;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.entity.GameMode;
import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.Vec3f;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.block.interaction.BlockInteractionContext;
import fr.fidorial.world.block.interaction.InteractionHand;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.io.IOException;
import java.util.Objects;

public record FidorialBlockInteractionContext(FidorialServer server,
                                              ServerWorld world,
                                              ServerPlayer player,
                                              BlockPos pos,
                                              BlockState state,
                                              BlockFace face,
                                              InteractionHand hand,
                                              ItemStack heldItem,
                                              Vec3f cursor,
                                              boolean insideBlock) implements BlockInteractionContext {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(FidorialBlockInteractionContext.class);

    private static final int OFFHAND_SLOT = 40;

    public boolean setBlock(final BlockState newState) {
        return server.blockEdits().set(world, pos, newState);
    }

    public boolean setBlockAt(final BlockPos position, final BlockState newState) {
        return server.blockEdits().set(world, position, newState);
    }

    public boolean setBlockAt(final int dx, final int dy, final int dz, final BlockState newState) {
        return setBlockAt(pos.offset(dx, dy, dz), newState);
    }

    public BlockState stateAt(final int dx, final int dy, final int dz) {
        return readState(world, pos.offset(dx, dy, dz));
    }

    @Override
    public BlockData block() {
        return toData(state);
    }

    @Override
    public BlockData blockAt(final BlockPos position) {
        return toData(readState(world, position));
    }

    @Override
    public boolean setBlock(final BlockPos position, final BlockData data) {
        return setBlockAt(position, server.blockStateRegistry().toBlockState(data));
    }

    @Override
    public void consumeHeldItem(final int amount) {
        if (amount <= 0 || player.gameMode() == GameMode.CREATIVE) {
            return;
        }
        final int slot = hand == InteractionHand.OFF_HAND ? OFFHAND_SLOT : player.selectedSlot();
        final ItemStack current = player.inventory().get(slot);
        if (current.isEmpty()) {
            return;
        }
        player.inventory().set(slot, current.plus(-amount));
        player.updateInventory();
    }

    @Override
    public void playSound(final Sound sound) {
        final double x = pos.x() + 0.5;
        final double y = pos.y() + 0.5;
        final double z = pos.z() + 0.5;
        server.broadcastNear(world, x, y, z, new ClientboundSoundPacket(sound, x, y, z));
    }

    public static BlockState readState(final ServerWorld world, final BlockPos position) {
        if (!world.isChunkLoaded(position.chunkX(), position.chunkZ())) {
            return BlockState.of(BlockTypeKeys.AIR.key());
        }
        try {
            return world.getBlock(position.x(), position.y(), position.z());
        } catch (final IOException e) {
            LOGGER.debug("Unable to read the block at {}", position, e);
            return BlockState.of(BlockTypeKeys.AIR.key());
        }
    }

    private BlockData toData(final BlockState blockState) {
        final BlockData data = server.blockStateRegistry().resolve(blockState);
        return data != null ? data : air();
    }

    private static BlockData air() {
        return Objects.requireNonNull(
                Objects.requireNonNull(Blocks.type(BlockTypeKeys.AIR.key()), "air block type").defaultData(),
                "air block data");
    }
}
