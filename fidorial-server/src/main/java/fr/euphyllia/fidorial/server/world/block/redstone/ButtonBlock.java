package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.fidorial.entity.player.Player;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.type.Switch;
import fr.fidorial.world.redstone.RedstoneWorld;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class ButtonBlock extends SwitchBlock {

    private static final int STONE_TICKS = 20;
    private static final int WOOD_TICKS = 30;
    public static final List<ButtonBlock> ALL = List.of(
            new ButtonBlock(BlockTypeKeys.STONE_BUTTON, RedstoneBlockSet.STONE, STONE_TICKS),
            new ButtonBlock(BlockTypeKeys.POLISHED_BLACKSTONE_BUTTON, RedstoneBlockSet.STONE, STONE_TICKS),
            new ButtonBlock(BlockTypeKeys.OAK_BUTTON, RedstoneBlockSet.WOOD, WOOD_TICKS),
            new ButtonBlock(BlockTypeKeys.SPRUCE_BUTTON, RedstoneBlockSet.WOOD, WOOD_TICKS),
            new ButtonBlock(BlockTypeKeys.BIRCH_BUTTON, RedstoneBlockSet.WOOD, WOOD_TICKS),
            new ButtonBlock(BlockTypeKeys.JUNGLE_BUTTON, RedstoneBlockSet.WOOD, WOOD_TICKS),
            new ButtonBlock(BlockTypeKeys.ACACIA_BUTTON, RedstoneBlockSet.WOOD, WOOD_TICKS),
            new ButtonBlock(BlockTypeKeys.DARK_OAK_BUTTON, RedstoneBlockSet.WOOD, WOOD_TICKS),
            new ButtonBlock(BlockTypeKeys.PALE_OAK_BUTTON, RedstoneBlockSet.WOOD, WOOD_TICKS),
            new ButtonBlock(BlockTypeKeys.MANGROVE_BUTTON, RedstoneBlockSet.WOOD, WOOD_TICKS),
            new ButtonBlock(BlockTypeKeys.POPLAR_BUTTON, RedstoneBlockSet.WOOD, WOOD_TICKS),
            new ButtonBlock(BlockTypeKeys.CHERRY_BUTTON, RedstoneBlockSet.CHERRY, WOOD_TICKS),
            new ButtonBlock(BlockTypeKeys.BAMBOO_BUTTON, RedstoneBlockSet.BAMBOO, WOOD_TICKS),
            new ButtonBlock(BlockTypeKeys.CRIMSON_BUTTON, RedstoneBlockSet.NETHER_WOOD, WOOD_TICKS),
            new ButtonBlock(BlockTypeKeys.WARPED_BUTTON, RedstoneBlockSet.NETHER_WOOD, WOOD_TICKS));
    private final RedstoneBlockSet set;
    private final int ticksToStayPressed;

    private ButtonBlock(final TypedKey<BlockType> key, final RedstoneBlockSet set, final int ticksToStayPressed) {
        super(key);
        this.set = set;
        this.ticksToStayPressed = ticksToStayPressed;
    }

    @Override
    public boolean use(final Player player, final BlockPosition position, final BlockData data) {
        if (!(data instanceof final Switch button)) {
            return false;
        }
        if (!button.isPowered()) {
            press(Redstone.world(player.world()), position, button, player);
        }
        return true;
    }

    /**
     * Presses the button, as a player using it does.
     *
     * @param world    the redstone of the world of the button
     * @param position where the button is
     * @param button   its current state
     * @param presser  the player pressing it, whose client plays the click itself; {@code null} for
     *                 none, such as a plugin pressing it
     */
    public void press(final RedstoneWorld world, final BlockPosition position, final Switch button,
                      final @Nullable Player presser) {
        world.setBlock(position, button.withPowered(true), RedstoneWorld.Update.NEIGHBORS);
        updateNeighbours(world, position, button);
        world.scheduleTick(position, key, ticksToStayPressed);
        Redstone.playSound(world.world(), position, set.button(true), Sound.Source.BLOCK, 1.0f, 1.0f, presser);
        Redstone.gameEvent(world.world(), GameEventKeys.BLOCK_ACTIVATE, position);
    }

    @Override
    public void tick(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        if (!(state instanceof final Switch button) || !button.isPowered()) {
            return;
        }
        // nothing keeps the button pressed: no arrow can stick in it
        world.setBlock(position, button.withPowered(false), RedstoneWorld.Update.NEIGHBORS);
        updateNeighbours(world, position, button);
        Redstone.playSound(world.world(), position, set.button(false), Sound.Source.BLOCK, 1.0f, 1.0f, null);
        Redstone.gameEvent(world.world(), GameEventKeys.BLOCK_DEACTIVATE, position);
    }
}
