package fr.fidorial.item.enchantment;

import fr.fidorial.entity.LivingEntity;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.TypedKey;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * An enchantment applied to a stack carried by a living entity, passed to the hooks of an
 * {@link EnchantmentBehaviour}.
 *
 * <p>Instances are only valid for the duration of the hook and must not be kept.</p>
 *
 * @since 0.1.0
 */
@ApiStatus.NonExtendable
public interface EnchantedItem {

    /**
     * {@return the enchantment}
     *
     * @since 0.1.0
     */
    TypedKey<EnchantmentDefinition> enchantment();

    /**
     * {@return the level of the enchantment on the stack, starting at {@code 1}}
     *
     * @since 0.1.0
     */
    int level();

    /**
     * {@return the enchanted stack}
     *
     * @since 0.1.0
     */
    ItemStack item();

    /**
     * {@return the slot the stack is worn or held in, or {@code null} if it is elsewhere in the
     * inventory}
     *
     * @since 0.1.0
     */
    @Nullable EquipmentSlot slot();

    /**
     * {@return the entity carrying the stack}
     *
     * @since 0.1.0
     */
    LivingEntity owner();
}
