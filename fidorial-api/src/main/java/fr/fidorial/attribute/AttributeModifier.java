package fr.fidorial.attribute;

import com.google.common.base.Preconditions;
import fr.fidorial.inventory.EquipmentSlotGroup;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

public record AttributeModifier(Key attribute, Key id, double amount, Operation operation, EquipmentSlotGroup slot) {

    public AttributeModifier {
        Preconditions.checkNotNull(attribute, "The attribute of an attribute modifier must not be null");
        Preconditions.checkNotNull(id, "The ID of an attribute modifier must not be null");
        Preconditions.checkNotNull(operation, "The operation of an attribute modifier must not be null");
        Preconditions.checkNotNull(slot, "The slot of an attribute modifier must not be null");
    }

    public static AttributeModifier of(
            final Key attribute,
            final Key id,
            final double amount,
            final Operation operation,
            final EquipmentSlotGroup slot
    ) {
        return new AttributeModifier(attribute, id, amount, operation, slot);
    }

    public static AttributeModifier of(final Key attribute, final Key id, final double amount, final Operation operation) {
        return new AttributeModifier(attribute, id, amount, operation, EquipmentSlotGroup.ANY);
    }

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

        public int networkId() {
            return networkId;
        }

        public String serializedName() {
            return serializedName;
        }

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
