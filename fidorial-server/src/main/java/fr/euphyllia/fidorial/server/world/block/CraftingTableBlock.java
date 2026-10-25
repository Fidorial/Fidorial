package fr.euphyllia.fidorial.server.world.block;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.inventory.CraftingMenu;
import fr.fidorial.entity.player.Player;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import net.kyori.adventure.key.Key;

/**
 * The crafting table: using it opens a 3×3 crafting grid. The grid belongs to the player, not to
 * the block, and goes back to the inventory when the window closes.
 */
public final class CraftingTableBlock implements BlockBehaviour {

    public static final CraftingTableBlock INSTANCE = new CraftingTableBlock();

    public static final Key KEY = BlockTypeKeys.CRAFTING_TABLE.key();

    private CraftingTableBlock() {
    }

    @Override
    public BlockType type() {
        final BlockType type = Blocks.type(KEY);
        Preconditions.checkState(type != null, "The block type %s is not registered", KEY);
        return type;
    }

    @Override
    public boolean use(final Player player, final BlockPosition position, final BlockData data) {
        if (!(player instanceof final ServerPlayer serverPlayer)) {
            return false;
        }
        serverPlayer.openMenu(new CraftingMenu(serverPlayer, serverPlayer.allocateWindowId(), position));
        return true;
    }
}
