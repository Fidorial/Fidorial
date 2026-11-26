package fr.euphyllia.fidorial.server.inventory;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.chest.ChestBlock;
import fr.euphyllia.fidorial.server.world.block.chest.ChestBlocks;
import fr.fidorial.inventory.ChestInventory;
import fr.fidorial.inventory.Container;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MenuType;
import fr.fidorial.registry.keys.MenuTypeKeys;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The window of a chest, three rows of slots, or of a large chest, six rows, its first half on top:
 * the slots of the chest then the player inventory.
 *
 * <p>The slots are read from and written to the block entities of the chests: players looking
 * into the same chest see each other's moves, and nothing waits for the window to close to be
 * saved. The lids open while anyone looks inside.</p>
 */
public final class ChestMenu extends ContainerMenu {

    private final ServerWorld world;
    private final ChestBlock block;
    private final List<BlockPosition> halves;
    private final Container contents;
    private final Component title;

    /**
     * @param position where the chest used is
     * @param block    the chest used
     * @param halves   the chest alone, or both halves of its large chest, the first one first
     * @param contents what the window shows, the slots of the halves in that order
     * @param title    the title of the window
     */
    public ChestMenu(final ServerPlayer player, final int windowId, final ServerWorld world, final BlockPosition position,
                     final ChestBlock block, final List<BlockPosition> halves, final Container contents,
                     final Component title) {
        super(player, windowId, position, block.key());
        Preconditions.checkArgument(contents != null, "The window of the chest at %s needs the slots it shows", position);
        Preconditions.checkArgument(title != null, "The window of the chest at %s needs a title", position);
        Preconditions.checkArgument(halves.size() == 1 || halves.size() == 2,
                "The window of the chest at %s shows one chest or the two halves of a large chest, not %s",
                position, halves.size());
        Preconditions.checkArgument(contents.size() == halves.size() * ChestInventory.SIZE,
                "The window of the chest at %s shows %s chests, which hold %s slots, not %s",
                position, halves.size(), halves.size() * ChestInventory.SIZE, contents.size());
        this.world = world;
        this.block = block;
        this.halves = List.copyOf(halves);
        this.contents = contents;
        this.title = title;
    }

    /**
     * {@return the chest alone, or both halves of the large chest, the first one first}
     */
    public List<BlockPosition> halves() {
        return halves;
    }

    @Override
    public Container container() {
        return contents;
    }

    @Override
    protected List<Slot> createSlots() {
        final List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < contents.size(); i++) {
            slots.add(new Slot(contents, i));
        }
        addPlayerInventory(slots);
        return slots;
    }

    @Override
    public TypedKey<MenuType> menuType() {
        return contents.size() > ChestInventory.SIZE ? MenuTypeKeys.GENERIC_9X6 : MenuTypeKeys.GENERIC_9X3;
    }

    @Override
    public Component title() {
        return title;
    }

    /**
     * Valid while each half is still a chest of the family of the one used, a copper chest taking
     * another oxidation included, and within reach.
     */
    @Override
    public boolean stillValid() {
        if (player.world() != world) {
            return false;
        }
        for (final BlockPosition half : halves) {
            if (!block.joins(ChestBlocks.stateAt(world, half))
                    || !player.isWithinBlockInteractionRange(half, STILL_VALID_REACH_BUFFER)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onOpened() {
        BlockLid.open(server(), world, halves, block.openSound());
    }

    @Override
    protected void onClosed() {
        BlockLid.close(server(), world, halves, block.closeSound());
    }

    private FidorialServer server() {
        return player.connection().server();
    }
}
