package fr.euphyllia.fidorial.server.item.enchantment;

import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.attribute.AttributeModifier;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.component.ItemEnchantments;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.item.enchantment.EnchantmentBehaviour;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Tracks the enchantments a player has equipped, firing equip, unequip and tick hooks and updating the
 * attribute modifiers they grant.
 *
 * <p>Only accessed from the thread of the region owning the player.</p>
 */
public final class EquipmentEnchantments {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(EquipmentEnchantments.class);

    private static final EquipmentSlot[] SLOTS = EquipmentSlot.values();

    private final ServerPlayer owner;
    private final ItemStack[] worn = new ItemStack[SLOTS.length];
    private final List<List<EnchantedItemImpl>> active = new ArrayList<>(SLOTS.length);
    private Object registryVersion = new Object();

    public EquipmentEnchantments(final ServerPlayer owner) {
        this.owner = owner;
        Arrays.fill(worn, ItemStack.EMPTY);
        for (int i = 0; i < SLOTS.length; i++) {
            active.add(List.of());
        }
    }

    /**
     * Updates what is equipped, then ticks it.
     *
     * @param alive {@code false} while the player is dead: nothing counts as equipped then
     */
    public void tick(final boolean alive) {
        final FidorialEnchantments registry = EnchantmentEngine.registry();
        final Object version = registry.version();
        final boolean registryChanged = version != registryVersion;
        registryVersion = version;

        boolean changed = false;
        for (int i = 0; i < SLOTS.length; i++) {
            final ItemStack current = alive ? owner.equipment(SLOTS[i]) : ItemStack.EMPTY;
            if (!registryChanged && same(current, worn[i])) {
                continue;
            }
            final boolean hadEnchantments = !active.get(i).isEmpty();
            unequip(registry, i);
            worn[i] = current;
            equip(registry, i);
            changed |= hadEnchantments || !active.get(i).isEmpty();
        }
        if (changed) {
            owner.setEnchantmentModifiers(modifiers(registry));
        }
        if (alive) {
            for (final List<EnchantedItemImpl> items : active) {
                for (final EnchantedItemImpl item : items) {
                    run(registry, item, "onTick", behaviour -> behaviour.onTick(item));
                }
            }
        }
    }

    /**
     * Unequips everything, as when the player leaves, without telling the client.
     */
    public void clear() {
        final FidorialEnchantments registry = EnchantmentEngine.registry();
        for (int i = 0; i < SLOTS.length; i++) {
            unequip(registry, i);
            worn[i] = ItemStack.EMPTY;
        }
    }

    private static boolean same(final ItemStack current, final ItemStack previous) {
        return current == previous || current.equals(previous);
    }

    private void unequip(final FidorialEnchantments registry, final int slot) {
        for (final EnchantedItemImpl item : active.get(slot)) {
            run(registry, item, "onUnequip", behaviour -> behaviour.onUnequip(item));
        }
        active.set(slot, List.of());
    }

    private void equip(final FidorialEnchantments registry, final int slot) {
        final ItemStack stack = worn[slot];
        if (stack.isEmpty()) {
            return;
        }
        final ItemEnchantments enchantments = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) {
            return;
        }
        final List<EnchantedItemImpl> items = new ArrayList<>(enchantments.levels().size());
        for (final Map.Entry<Key, Integer> enchantment : enchantments.levels().entrySet()) {
            final EnchantmentDefinitionImpl definition = registry.definition(enchantment.getKey());
            if (definition == null || !definition.matchesSlot(SLOTS[slot]) || registry.behaviours(enchantment.getKey()).isEmpty()) {
                continue;
            }
            items.add(new EnchantedItemImpl(TypedKey.create(RegistryKey.ENCHANTMENT, enchantment.getKey()),
                    enchantment.getValue(), stack, SLOTS[slot], owner));
        }
        active.set(slot, List.copyOf(items));
        for (final EnchantedItemImpl item : items) {
            run(registry, item, "onEquip", behaviour -> behaviour.onEquip(item));
        }
    }

    /**
     * {@return the attribute modifiers of every equipped enchantment, by attribute, each id suffixed with
     * its slot as vanilla does}
     */
    private Map<Key, Map<Key, AttributeModifier>> modifiers(final FidorialEnchantments registry) {
        final Map<Key, Map<Key, AttributeModifier>> modifiers = new LinkedHashMap<>();
        for (final List<EnchantedItemImpl> items : active) {
            for (final EnchantedItemImpl item : items) {
                run(registry, item, "attributeModifiers", behaviour -> {
                    for (final AttributeModifier modifier : behaviour.attributeModifiers(item)) {
                        final EquipmentSlot slot = item.slot();
                        final Key id = Key.key(modifier.id().namespace(), modifier.id().value() + "/"
                                + (slot == null ? "any" : slot.serializedName()));
                        modifiers.computeIfAbsent(modifier.attribute(), _ -> new LinkedHashMap<>())
                                .put(id, AttributeModifier.of(modifier.attribute(), id, modifier.amount(),
                                        modifier.operation(), EquipmentSlotGroup.ANY));
                    }
                });
            }
        }
        return modifiers;
    }

    private void run(final FidorialEnchantments registry, final EnchantedItemImpl item, final String hook,
                     final Consumer<EnchantmentBehaviour> action) {
        for (final EnchantmentBehaviour behaviour : registry.behaviours(item.enchantment().key())) {
            try {
                action.accept(behaviour);
            } catch (final Throwable throwable) {
                LOGGER.error("Error in the {} of {} for the enchantment {} on {}", hook, behaviour.getClass().getName(),
                        item.enchantment().key().asString(), owner.name(), throwable);
            }
        }
    }
}
