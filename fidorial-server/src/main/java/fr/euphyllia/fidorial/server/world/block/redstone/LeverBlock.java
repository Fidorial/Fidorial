package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.fidorial.entity.player.Player;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.registry.keys.SoundEventKeys;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.data.type.Switch;
import fr.fidorial.world.redstone.RedstoneWorld;
import net.kyori.adventure.sound.Sound;

public final class LeverBlock extends SwitchBlock {

    public static final LeverBlock INSTANCE = new LeverBlock();

    private LeverBlock() {
        super(BlockTypeKeys.LEVER);
    }

    @Override
    public boolean use(final Player player, final BlockPosition position, final BlockData data) {
        if (!(data instanceof final Switch lever)) {
            return false;
        }
        pull(Redstone.world(player.world()), position, lever);
        return true;
    }

    /**
     * Switches the lever, as a player using it does.
     *
     * @param world    the redstone of the world of the lever
     * @param position where the lever is
     * @param lever    its current state
     */
    public void pull(final RedstoneWorld world, final BlockPosition position, final Switch lever) {
        final Switch pulled = lever.withPowered(!lever.isPowered());
        world.setBlock(position, pulled, RedstoneWorld.Update.NEIGHBORS);
        updateNeighbours(world, position, pulled);
        Redstone.playSound(world.world(), position, SoundEventKeys.BLOCK_LEVER_CLICK, Sound.Source.BLOCK, 0.3f, pulled.isPowered() ? 0.6f : 0.5f, null);
        Redstone.gameEvent(world.world(), pulled.isPowered() ? GameEventKeys.BLOCK_ACTIVATE : GameEventKeys.BLOCK_DEACTIVATE, position);
    }
}
