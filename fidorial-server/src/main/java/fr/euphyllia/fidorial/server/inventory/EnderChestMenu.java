package fr.euphyllia.fidorial.server.inventory;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundBlockEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.fidorial.inventory.Container;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MenuType;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.MenuTypeKeys;
import fr.fidorial.registry.keys.SoundEventKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The window of an ender chest: the player's own ender chest inventory, opened on a block whose
 * lid opens while anyone looks inside.
 */
public final class EnderChestMenu extends ContainerMenu {

    private final BlockPosition position;

    public EnderChestMenu(final ServerPlayer player, final int windowId, final BlockPosition position) {
        super(player, windowId, position, BlockTypeKeys.ENDER_CHEST.key());
        this.position = position;
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
        final int viewers = server().players().blockViewers().open(player.world().key(), position);
        lid(viewers);
        sound(SoundEventKeys.BLOCK_ENDER_CHEST_OPEN.key());
    }

    @Override
    protected void onClosed() {
        final int viewers = server().players().blockViewers().close(player.world().key(), position);
        lid(viewers);
        sound(SoundEventKeys.BLOCK_ENDER_CHEST_CLOSE.key());
    }

    private FidorialServer server() {
        return player.connection().server();
    }

    private void lid(final int viewers) {
        server().players().broadcast(ClientboundBlockEventPacket.chestViewers(position, viewers));
    }

    private void sound(final Key sound) {
        server().players().broadcast(new ClientboundSoundPacket(
                Sound.sound(sound, Sound.Source.BLOCK, 0.5f, 1.0f),
                position.x() + 0.5, position.y() + 0.5, position.z() + 0.5));
    }
}
