package fr.euphyllia.fidorial.server.item.enchantment;

import fr.fidorial.entity.LivingEntity;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.enchantment.EnchantedItem;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Enchantment;
import org.jspecify.annotations.Nullable;

public record EnchantedItemImpl(
        TypedKey<Enchantment> enchantment,
        int level,
        ItemStack item,
        @Nullable EquipmentSlot slot,
        LivingEntity owner
) implements EnchantedItem {
}
