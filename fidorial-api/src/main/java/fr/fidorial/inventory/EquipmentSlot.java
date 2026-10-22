package fr.fidorial.inventory;

import fr.fidorial.item.ItemDefaults;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * One place an entity wears or holds an item, as an item's {@code minecraft:equippable} component
 * names it.
 *
 * @see ItemDefaults#equipmentSlot(Key)
 * @see PlayerInventory#slotOf(EquipmentSlot)
 * @since 0.1.0
 */
public enum EquipmentSlot {

    /**
     * The main hand.
     */
    MAIN_HAND("mainhand"),

    /**
     * The off hand, where a shield is held.
     */
    OFF_HAND("offhand"),

    /**
     * Boots.
     */
    FEET("feet"),

    /**
     * Leggings.
     */
    LEGS("legs"),

    /**
     * Chestplates and elytras.
     */
    CHEST("chest"),

    /**
     * Helmets, heads and carved pumpkins.
     */
    HEAD("head"),

    /**
     * The body armor of an animal: horse armor, wolf armor, llama carpets, harnesses...
     */
    BODY("body"),

    /**
     * The saddle of a mount.
     */
    SADDLE("saddle");

    private final String serializedName;

    EquipmentSlot(final String serializedName) {
        this.serializedName = serializedName;
    }

    /**
     * @param name the vanilla name, e.g. {@code head}
     * @return the matching slot, {@code null} when the name is unknown
     * @since 0.1.0
     */
    public static @Nullable EquipmentSlot byName(final @Nullable String name) {
        if (name == null) {
            return null;
        }
        final String lower = name.toLowerCase(Locale.ROOT);
        for (final EquipmentSlot slot : values()) {
            if (slot.serializedName.equals(lower)) {
                return slot;
            }
        }
        return null;
    }

    /**
     * @return the vanilla name, e.g. {@code head}
     * @since 0.1.0
     */
    public String serializedName() {
        return serializedName;
    }

    /**
     * @return {@code true} for the four slots of a player's armor
     * @since 0.1.0
     */
    public boolean isHumanoidArmor() {
        return this == FEET || this == LEGS || this == CHEST || this == HEAD;
    }
}
