package fr.euphyllia.fidorial.server.inventory;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.inventory.Container;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MenuType;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.MenuTypeKeys;
import fr.fidorial.registry.keys.SoundEventKeys;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The window of an ender chest: the player's own ender chest inventory, opened on a block whose
 * lid opens while anyone looks inside.
 */
public final class EnderChestMenu extends ContainerMenu {

    private final ServerWorld world;
    private final List<BlockPosition> block;

    public EnderChestMenu(final ServerPlayer player, final int windowId, final ServerWorld world, final BlockPosition position) {
        super(player, windowId, position, BlockTypeKeys.ENDER_CHEST);
        this.world = world;
        this.block = List.of(position);
    }

    @Override
    public Container container() {
        return player.enderChest();
    }

    @Override
    protected List<Slot> createSlots() {
        final List<Slot> slots = new ArrayList<>();
        final Container enderChest = player.enderChest();
        for (int i = 0; i < enderChest.size(); i++) {
            slots.add(new Slot(enderChest, i));
        }
        addPlayerInventory(slots);
        return slots;
    }

    @Override
    public TypedKey<MenuType> menuType() {
        return MenuTypeKeys.GENERIC_9X3;
    }

    @Override
    public Component title() {
        return Component.translatable("container.enderchest");
    }

    @Override
    public void onOpened() {
        BlockLid.open(server(), world, block, SoundEventKeys.BLOCK_ENDER_CHEST_OPEN);
    }

    @Override
    protected void onClosed() {
        BlockLid.close(server(), world, block, SoundEventKeys.BLOCK_ENDER_CHEST_CLOSE);
    }

    private FidorialServer server() {
        return player.connection().server();
    }
}
