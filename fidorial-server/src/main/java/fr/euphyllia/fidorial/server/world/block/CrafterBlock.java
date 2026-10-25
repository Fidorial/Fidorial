package fr.euphyllia.fidorial.server.world.block;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.inventory.BlockViewerTracker;
import fr.euphyllia.fidorial.server.inventory.CrafterContainer;
import fr.euphyllia.fidorial.server.inventory.CrafterMenu;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.player.Player;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Locale;

/**
 * The crafter: nine slots kept in its block entity, some of which a player may disable, and the
 * recipe they make shown in its window.
 *
 * <p>Crafting itself happens on a redstone pulse and throws the result out of the front face:
 * redstone does not exist yet, so the crafter only stores its grid for now.</p>
 */
public final class CrafterBlock implements BlockBehaviour {

    public static final CrafterBlock INSTANCE = new CrafterBlock();

    public static final Key KEY = BlockTypeKeys.CRAFTER.key();

    private static final ComponentLogger LOGGER = ComponentLogger.logger(CrafterBlock.class);
    private static final String ORIENTATION = "orientation";
    private static final String TRIGGERED = "triggered";
    private static final String CRAFTING = "crafting";

    private CrafterBlock() {
    }

    @Override
    public BlockType type() {
        final BlockType type = Blocks.type(KEY);
        Preconditions.checkState(type != null, "The block type %s is not registered", KEY);
        return type;
    }

    /**
     * Like vanilla: the front faces the player; when it faces up or down, the top points away from
     * the player, or toward them.
     */
    @Override
    public @Nullable BlockData placementState(final BlockPlaceContext context) {
        final BlockData defaults = type().defaultData();
        if (defaults == null) {
            return null;
        }
        final BlockFace front = context.lookingDirection().opposite();
        final BlockFace top = switch (front) {
            case DOWN -> context.horizontalFacing().opposite();
            case UP -> context.horizontalFacing();
            default -> BlockFace.UP;
        };
        return defaults.with(ORIENTATION, name(front) + "_" + name(top))
                .with(TRIGGERED, "false")
                .with(CRAFTING, "false");
    }

    @Override
    public boolean use(final Player player, final BlockPosition position, final BlockData data) {
        if (!(player instanceof final ServerPlayer serverPlayer) || !(player.world() instanceof final ServerWorld world)) {
            return false;
        }
        final CrafterContainer crafter = shared(serverPlayer, world, position);
        if (crafter != null) {
            serverPlayer.openMenu(new CrafterMenu(serverPlayer, serverPlayer.allocateWindowId(), world, position, crafter));
        }
        return true;
    }

    /**
     * Like vanilla: the nine slots spill on the ground where the crafter stood.
     */
    @Override
    public void onRemove(final World world, final BlockPosition position, final BlockData data) {
        if (!(world instanceof final ServerWorld serverWorld)) {
            return;
        }
        final FidorialServer server = FidorialServer.getInstance();
        final BlockViewerTracker viewers = server.players().blockViewers();
        final CrafterContainer crafter;
        if (viewers.count(serverWorld.key(), position) > 0) {
            // Someone has the window open: what it shows is the latest.
            crafter = viewers.shared(serverWorld.key(), position, CrafterContainer::new);
        } else {
            try {
                crafter = CrafterContainer.load(serverWorld.blockEntityData(position));
            } catch (final IOException exception) {
                LOGGER.warn("Unable to read the crafter removed at {}: its contents are lost", position, exception);
                return;
            }
        }
        for (int slot = 0; slot < crafter.size(); slot++) {
            final ItemStack stack = crafter.get(slot);
            if (!stack.isEmpty()) {
                server.entities().dropNaturally(serverWorld,
                        position.blockX() + 0.5, position.blockY() + 0.5, position.blockZ() + 0.5, stack);
            }
        }
        // The windows still open on it must not hand the same items out a second time.
        crafter.clear();
    }

    /**
     * The contents every viewer of the crafter shares, read from its block entity by the first one.
     */
    private static @Nullable CrafterContainer shared(final ServerPlayer player, final ServerWorld world,
                                                     final BlockPosition position) {
        if (world.blockEntityData(position) == null) {
            LOGGER.warn("The crafter at {} has no block entity", position);
            return null;
        }
        return player.connection().server().players().blockViewers().shared(world.key(), position, () -> {
            try {
                return CrafterContainer.load(world.blockEntityData(position));
            } catch (final IOException exception) {
                LOGGER.error("Unable to read the crafter at {}, it opens empty", position, exception);
                return new CrafterContainer();
            }
        });
    }

    private static String name(final BlockFace face) {
        return face.name().toLowerCase(Locale.ROOT);
    }
}
