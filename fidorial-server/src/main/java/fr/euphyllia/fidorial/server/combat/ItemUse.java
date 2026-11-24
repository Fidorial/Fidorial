package fr.euphyllia.fidorial.server.combat;

import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * The item a living entity is using: a shield it raises, a bow it draws, a crossbow it loads, a
 * trident it readies.
 *
 * <p>Only touched from the region thread owning the entity.</p>
 */
public final class ItemUse {

    /**
     * The flag of the living entity data telling the viewers an item is in use.
     */
    private static final byte FLAG_USING = 0x01;

    /**
     * The flag of the living entity data telling the viewers the item in use is in the off hand.
     */
    private static final byte FLAG_OFF_HAND = 0x02;

    private @Nullable EquipmentSlot hand;
    private ItemStack item = ItemStack.EMPTY;
    private int ticks;
    private int stage;

    /**
     * {@return {@code true} while an item is in use}
     */
    public boolean isUsing() {
        return hand != null;
    }

    /**
     * {@return the hand holding the item in use, {@code null} when none is}
     */
    public @Nullable EquipmentSlot hand() {
        return hand;
    }

    /**
     * {@return the item in use, as it was when its use started}
     */
    public ItemStack item() {
        return item;
    }

    /**
     * {@return how many ticks the item has been in use}
     */
    public int ticks() {
        return ticks;
    }

    /**
     * {@return a step of the use, such as how far into its loading sounds a crossbow got}
     */
    public int stage() {
        return stage;
    }

    /**
     * @param stage the step the use reached
     */
    public void setStage(final int stage) {
        this.stage = stage;
    }

    /**
     * Starts using an item.
     *
     * @param hand the hand holding it
     * @param item the item
     */
    public void start(final EquipmentSlot hand, final ItemStack item) {
        this.hand = hand;
        this.item = item;
        this.ticks = 0;
        this.stage = 0;
    }

    /**
     * Stops using the item.
     */
    public void stop() {
        this.hand = null;
        this.item = ItemStack.EMPTY;
        this.ticks = 0;
        this.stage = 0;
    }

    /**
     * Counts one more tick of use.
     */
    public void tick() {
        if (hand != null) {
            ticks++;
        }
    }

    /**
     * {@return the hand states of the living entity data}
     */
    public byte flags() {
        if (hand == null) {
            return 0;
        }
        return (byte) (FLAG_USING | (hand == EquipmentSlot.OFF_HAND ? FLAG_OFF_HAND : 0));
    }
}
