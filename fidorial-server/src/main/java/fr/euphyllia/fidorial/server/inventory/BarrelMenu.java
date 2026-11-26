package fr.euphyllia.fidorial.server.inventory;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.configuration.WorldConfiguration;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.BarrelBlock;
import fr.euphyllia.fidorial.server.world.block.ContainerBlocks;
import fr.fidorial.inventory.Container;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MenuType;
import fr.fidorial.registry.data.SoundEvent;
import fr.fidorial.registry.keys.SoundEventKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.data.Directional;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The window of a barrel: the rows of slots it shows, from one to six, then the player inventory.
 *
 * <p>The slots are read from and written to the block entity of the barrel, which players looking
 * into it share. The rows the barrel no longer shows are not in the window: when the configuration
 * says so, they are emptied as the window opens, what they held being logged; otherwise they keep
 * it out of reach. The barrel shows {@code open=true} while anyone looks inside, and creaks when the
 * first player opens it and when the last one closes it.</p>
 */
public final class BarrelMenu extends ContainerMenu {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(BarrelMenu.class);
    private static final float VOLUME = 0.5f;

    private final ServerWorld world;
    private final BlockPosition position;
    private final BlockBarrelInventory barrel;
    private final HiddenRows hiddenRows;
    private final Component title;

    /**
     * @param barrel     the slots of the barrel at {@code position}, showing the rows of the window
     * @param hiddenRows what becomes of the rows the barrel no longer shows
     */
    public BarrelMenu(final ServerPlayer player, final int windowId, final ServerWorld world, final BlockPosition position,
                      final BlockBarrelInventory barrel, final HiddenRows hiddenRows) {
        super(player, windowId, position, BarrelBlock.KEY);
        Preconditions.checkArgument(barrel != null, "The window of the barrel at %s needs the slots it shows", position);
        Preconditions.checkArgument(barrel.world() == world && barrel.position().equals(position),
                "The window of the barrel at %s shows the slots of another barrel, at %s", position, barrel.position());
        Preconditions.checkArgument(hiddenRows != null,
                "The window of the barrel at %s needs to know what becomes of the rows it does not show", position);
        this.world = world;
        this.position = position;
        this.barrel = barrel;
        this.hiddenRows = hiddenRows;
        this.title = barrel.customName().orElseGet(() -> Component.translatable("container.barrel"));
    }

    /**
     * {@return the window of the barrel at {@code position}, showing the rows the configuration of its
     * world gives, the rows beyond following that configuration}
     */
    public static BarrelMenu of(final ServerPlayer player, final ServerWorld world, final BlockPosition position) {
        final WorldConfiguration.Barrel config = world.configuration().blocks().barrel();
        return new BarrelMenu(player, player.allocateWindowId(), world, position,
                new BlockBarrelInventory(world, position, config.rows()), config.hiddenRows());
    }

    /**
     * {@return the rows of the barrel the window shows}
     */
    public int rows() {
        return barrel.rows();
    }

    @Override
    public Container container() {
        return barrel;
    }

    @Override
    protected List<Slot> createSlots() {
        final List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < barrel.size(); i++) {
            slots.add(new Slot(barrel, i));
        }
        addPlayerInventory(slots);
        return slots;
    }

    @Override
    public TypedKey<MenuType> menuType() {
        return genericRows(barrel.rows());
    }

    @Override
    public Component title() {
        return title;
    }

    @Override
    public void onOpened() {
        if (hiddenRows == HiddenRows.CLEAR) {
            final List<ItemStack> cleared = barrel.clearHidden();
            if (!cleared.isEmpty()) {
                LOGGER.info("Cleared the rows the barrel at {} {} {} in {} no longer shows, beyond row {}, as {} opened it: {}",
                        position.blockX(), position.blockY(), position.blockZ(), world.key().asString(), barrel.rows(),
                        player.name(), HiddenRows.describe(cleared));
            }
        }
        if (server().players().blockViewers().open(world.key(), position) == 1) {
            sound(SoundEventKeys.BLOCK_BARREL_OPEN);
        }
        BarrelBlock.lookedInto(world, position);
    }

    @Override
    protected void onClosed() {
        if (server().players().blockViewers().close(world.key(), position) == 0) {
            sound(SoundEventKeys.BLOCK_BARREL_CLOSE);
        }
        BarrelBlock.lookedInto(world, position);
    }

    private FidorialServer server() {
        return player.connection().server();
    }

    /**
     * Plays a sound at the opening of the barrel, to the players near it.
     */
    private void sound(final TypedKey<SoundEvent> sound) {
        final BlockFace facing = ContainerBlocks.stateAt(world, position) instanceof final Directional directional
                ? directional.getFacing()
                : BlockFace.UP;
        final double x = position.blockX() + 0.5 + facing.dx() * 0.5;
        final double y = position.blockY() + 0.5 + facing.dy() * 0.5;
        final double z = position.blockZ() + 0.5 + facing.dz() * 0.5;
        final float pitch = ThreadLocalRandom.current().nextFloat() * 0.1f + 0.9f;
        server().players().broadcastNear(world, x, y, z,
                new ClientboundSoundPacket(Sound.sound(sound.key(), Sound.Source.BLOCK, VOLUME, pitch), x, y, z), null);
    }
}
