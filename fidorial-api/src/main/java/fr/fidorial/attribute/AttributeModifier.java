package fr.fidorial.attribute;

import com.google.common.base.Preconditions;
import fr.fidorial.inventory.EquipmentSlotGroup;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * A change applied to an attribute of the entity wearing or holding an item.
 *
 * @param attribute the attribute changed, see {@link fr.fidorial.registry.keys.AttributeKeys}
 * @param id        the identifier of the modifier, unique per attribute
 * @param amount    the amount applied, interpreted according to {@code operation}
 * @param operation how the amount combines with the attribute value
 * @param slot      the equipment slots the modifier applies in
 * @since 0.1.0
 */
public record AttributeModifier(Key attribute, Key id, double amount, Operation operation, EquipmentSlotGroup slot) {

    /**
     * Validates the components.
     */
    public AttributeModifier {
        Preconditions.checkArgument(attribute != null, "The attribute of an attribute modifier must not be null");
        Preconditions.checkArgument(id != null, "The ID of an attribute modifier must not be null");
        Preconditions.checkArgument(operation != null, "The operation of an attribute modifier must not be null");
        Preconditions.checkArgument(slot != null, "The slot of an attribute modifier must not be null");
    }

    /**
     * Creates a modifier.
     *
     * @param attribute the attribute changed
     * @param id        the identifier of the modifier
     * @param amount    the amount applied
     * @param operation how the amount combines with the attribute value
     * @param slot      the equipment slots the modifier applies in
     * @return the modifier
     * @since 0.1.0
     */
    public static AttributeModifier of(
            final Key attribute,
            final Key id,
            final double amount,
            final Operation operation,
            final EquipmentSlotGroup slot
    ) {
        return new AttributeModifier(attribute, id, amount, operation, slot);
    }

    /**
     * Creates a modifier applying in any slot.
     *
     * @param attribute the attribute changed
     * @param id        the identifier of the modifier
     * @param amount    the amount applied
     * @param operation how the amount combines with the attribute value
     * @return the modifier
     * @since 0.1.0
     */
    public static AttributeModifier of(final Key attribute, final Key id, final double amount, final Operation operation) {
        return new AttributeModifier(attribute, id, amount, operation, EquipmentSlotGroup.ANY);
    }

    /**
     * How the amount of a modifier combines with the attribute value.
     *
     * @since 0.1.0
     */
    public enum Operation {
        /**
         * Adds all of the modifiers' amounts to the base attribute.
         */
        ADD_VALUE(0, "add_value"),
        /**
         * Multiplies the base attribute by (1 + sum of modifiers' amounts).
         */
        ADD_MULTIPLIED_BASE(1, "add_multiplied_base"),
        /**
         * Multiplies the base attribute by (1 + modifiers' amounts) for every modifier.
         */
        ADD_MULTIPLIED_TOTAL(2, "add_multiplied_total");

        private final int networkId;
        private final String serializedName;

        Operation(final int networkId, final String serializedName) {
            this.networkId = networkId;
            this.serializedName = serializedName;
        }

        /**
         * {@return the protocol identifier of this operation}
         *
         * @since 0.1.0
         */
        public int networkId() {
            return networkId;
        }

        /**
         * {@return the name used in data components, for instance {@code add_value}}
         *
         * @since 0.1.0
         */
        public String serializedName() {
            return serializedName;
        }

        /**
         * {@return the operation with a serialized name, {@link #ADD_VALUE} when it is unknown or {@code null}}
         *
         * @param name the serialized name
         * @since 0.1.0
         */
        public static Operation byName(@Nullable final String name) {
            if (name == null) {
                return ADD_VALUE;
            }
            final String lower = name.toLowerCase(Locale.ROOT);
            for (final Operation operation : values()) {
                if (operation.serializedName.equals(lower)) {
                    return operation;
                }
            }
            return ADD_VALUE;
        }
    }
}
