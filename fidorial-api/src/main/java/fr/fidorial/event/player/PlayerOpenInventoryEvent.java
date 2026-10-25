package fr.fidorial.event.player;

import fr.fidorial.event.Cancellable;
import fr.fidorial.inventory.Container;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MenuType;
import net.kyori.adventure.text.Component;

import java.util.Optional;

/**
 * Fired right before a window opens on top of the player's inventory, whatever it shows: a
 * crafting table, a crafter, an ender chest, and every window to come.
 *
 * <p>Cancelling the event keeps the window closed, and leaves whatever the player had open as it
 * was.</p>
 *
 * @since 0.1.0
 */
public interface PlayerOpenInventoryEvent extends PlayerEvent, Cancellable {

    /**
     * {@return the kind of window the client draws, one of {@code MenuTypeKeys}}
     *
     * @since 0.1.0
     */
    TypedKey<MenuType> menuType();

    /**
     * {@return the title shown at the top of the window}
     *
     * @since 0.1.0
     */
    Component title();

    /**
     * {@return the block the window is opened on, empty when it belongs to no block}
     *
     * @since 0.1.0
     */
    Optional<BlockPosition> blockPosition();

    /**
     * {@return what the window shows above the player's inventory, such as the player's ender chest
     * or the nine slots of a crafter; changes made to it are kept}
     *
     * @since 0.1.0
     */
    Container inventory();
}
