package fr.fidorial.inventory;

import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * The equipment slots an attribute modifier applies in.
 *
 * @since 0.1.0
 */
public enum EquipmentSlotGroup {
    /**
     * Any slot.
     */
    ANY(0, "any"),
    /**
     * The main hand.
     */
    MAIN_HAND(1, "mainhand"),
    /**
     * The off hand.
     */
    OFF_HAND(2, "offhand"),
    /**
     * Either hand.
     */
    HAND(3, "hand"),
    /**
     * The boots slot.
     */
    FEET(4, "feet"),
    /**
     * The leggings slot.
     */
    LEGS(5, "legs"),
    /**
     * The chestplate slot.
     */
    CHEST(6, "chest"),
    /**
     * The helmet slot.
     */
    HEAD(7, "head"),
    /**
     * Any armor slot.
     */
    ARMOR(8, "armor"),
    /**
     * The body armor slot of animals, such as horse armor.
     */
    BODY(9, "body");

    private final int networkId;
    private final String serializedName;

    EquipmentSlotGroup(final int networkId, final String serializedName) {
        this.networkId = networkId;
        this.serializedName = serializedName;
    }

    /**
     * {@return the protocol identifier of this group}
     *
     * @since 0.1.0
     */
    public int networkId() {
        return networkId;
    }

    /**
     * {@return the name used in data components, for instance {@code mainhand}}
     *
     * @since 0.1.0
     */
    public String serializedName() {
        return serializedName;
    }

    /**
     * {@return whether {@code slot} belongs to this group}
     *
     * @param slot the slot
     * @since 0.1.0
     */
    public boolean test(final EquipmentSlot slot) {
        return switch (this) {
            case ANY -> true;
            case MAIN_HAND -> slot == EquipmentSlot.MAIN_HAND;
            case OFF_HAND -> slot == EquipmentSlot.OFF_HAND;
            case HAND -> slot == EquipmentSlot.MAIN_HAND || slot == EquipmentSlot.OFF_HAND;
            case FEET -> slot == EquipmentSlot.FEET;
            case LEGS -> slot == EquipmentSlot.LEGS;
            case CHEST -> slot == EquipmentSlot.CHEST;
            case HEAD -> slot == EquipmentSlot.HEAD;
            case ARMOR -> slot.isHumanoidArmor() || slot == EquipmentSlot.BODY;
            case BODY -> slot == EquipmentSlot.BODY;
        };
    }

    /**
     * {@return the group with a serialized name, {@link #ANY} when it is unknown or {@code null}}
     *
     * @param name the serialized name
     * @since 0.1.0
     */
    public static EquipmentSlotGroup byName(@Nullable final String name) {
        if (name == null) {
            return ANY;
        }
        final String lower = name.toLowerCase(Locale.ROOT);
        for (final EquipmentSlotGroup group : values()) {
            if (group.serializedName.equals(lower)) {
                return group;
            }
        }
        return ANY;
    }
}
