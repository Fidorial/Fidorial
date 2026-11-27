package fr.euphyllia.fidorial.server.inventory;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.hopper.HopperBlock;
import fr.fidorial.inventory.Container;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MenuType;
import fr.fidorial.registry.keys.MenuTypeKeys;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The window of a hopper: its five slots, then the player inventory.
 *
 * <p>The slots are read from and written to the block entity of the hopper, which players looking
 * into it share, and which the hopper keeps filling and emptying while the window is open.</p>
 */
public final class HopperMenu extends ContainerMenu {

    private final BlockHopperInventory hopper;
    private final Component title;

    /**
     * @param hopper the slots of the hopper at {@code position}
     */
    public HopperMenu(final ServerPlayer player, final int windowId, final ServerWorld world, final BlockPosition position,
                      final BlockHopperInventory hopper) {
        super(player, windowId, position, HopperBlock.KEY);
        Preconditions.checkArgument(hopper != null, "The window of the hopper at %s needs its slots", position);
        Preconditions.checkArgument(hopper.world() == world && hopper.position().equals(position),
                "The window of the hopper at %s shows the slots of another hopper, at %s", position, hopper.position());
        this.hopper = hopper;
        this.title = hopper.customName().orElseGet(() -> Component.translatable("container.hopper"));
    }

    /**
     * {@return the window of the hopper at {@code position}}
     */
    public static HopperMenu of(final ServerPlayer player, final ServerWorld world, final BlockPosition position) {
        return new HopperMenu(player, player.allocateWindowId(), world, position, HopperBlock.inventory(world, position));
    }

    @Override
    public Container container() {
        return hopper;
    }

    @Override
    protected List<Slot> createSlots() {
        final List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < hopper.size(); i++) {
            slots.add(new Slot(hopper, i));
        }
        addPlayerInventory(slots);
        return slots;
    }

    @Override
    public TypedKey<MenuType> menuType() {
        return MenuTypeKeys.HOPPER;
    }

    @Override
    public Component title() {
        return title;
    }
}
