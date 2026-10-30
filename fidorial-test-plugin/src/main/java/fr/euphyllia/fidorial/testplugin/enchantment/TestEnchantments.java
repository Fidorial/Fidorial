package fr.euphyllia.fidorial.testplugin.enchantment;

import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.item.enchantment.EnchantmentDefinition;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.GameRegistries;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.WritableRegistry;
import fr.fidorial.registry.keys.EnchantmentTagKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.List;

/**
 * Registers the test enchantments.
 *
 * <ul>
 *     <li>{@link #LIFESTEAL}, built in Java with an {@code EnchantmentBehaviour};</li>
 *     <li>{@link #GLACIAL}, read from data pack JSON and run through its effect components.</li>
 * </ul>
 */
public final class TestEnchantments {

    public static final TypedKey<EnchantmentDefinition> LIFESTEAL =
            TypedKey.create(RegistryKey.ENCHANTMENT, Key.key("fidorialtest", "lifesteal"));

    public static final TypedKey<EnchantmentDefinition> GLACIAL =
            TypedKey.create(RegistryKey.ENCHANTMENT, Key.key("fidorialtest", "glacial"));

    private static final String GLACIAL_JSON = """
            {
              "description": {"text": "Glacial", "color": "aqua"},
              "supported_items": "#minecraft:enchantable/weapon",
              "exclusive_set": ["minecraft:fire_aspect"],
              "weight": 2,
              "max_level": 2,
              "min_cost": {"base": 10, "per_level_above_first": 20},
              "max_cost": {"base": 60, "per_level_above_first": 20},
              "anvil_cost": 4,
              "slots": ["mainhand"],
              "effects": {
                "minecraft:post_attack": [
                  {
                    "enchanted": "attacker",
                    "affected": "victim",
                    "effect": {
                      "type": "minecraft:apply_mob_effect",
                      "to_apply": "minecraft:slowness",
                      "min_duration": 2,
                      "max_duration": {"type": "minecraft:linear", "base": 2, "per_level_above_first": 2},
                      "min_amplifier": 0,
                      "max_amplifier": {"type": "minecraft:linear", "base": 0, "per_level_above_first": 1}
                    },
                    "requirements": {"type": "minecraft:damage_source_properties", "predicate": {"is_direct": true}}
                  }
                ],
                "minecraft:damage": [
                  {
                    "effect": {"type": "minecraft:add", "value": {"type": "minecraft:linear", "base": 2, "per_level_above_first": 1}},
                    "requirements": {
                      "type": "minecraft:entity_properties",
                      "entity": "this",
                      "predicate": {"minecraft:flags": {"is_on_fire": true}}
                    }
                  }
                ]
              }
            }
            """;

    private TestEnchantments() {
    }

    public static void registerAll(final GameRegistries registries, final Plugin owner, final ComponentLogger logger) {
        final WritableRegistry<EnchantmentDefinition> enchantments = registries.writable(RegistryKey.ENCHANTMENT);
        enchantments.register(registries.enchantments().define(LIFESTEAL)
                .description(Component.text("Lifesteal", NamedTextColor.DARK_RED))
                .supportedItemTag(Key.key("enchantable/weapon"))
                .primaryItemTag(Key.key("enchantable/sharp_weapon"))
                .maxLevel(3)
                .weight(2)
                .anvilCost(4)
                .minCost(15, 9)
                .maxCost(65, 9)
                .slots(EquipmentSlotGroup.MAIN_HAND)
                .behaviour(new LifestealBehaviour())
                .build(), owner);
        enchantments.registerFromJson(GLACIAL, GLACIAL_JSON, owner);

        for (final TypedKey<EnchantmentDefinition> enchantment : List.of(LIFESTEAL, GLACIAL)) {
            for (final TagKey<EnchantmentDefinition> tag : List.of(EnchantmentTagKeys.IN_ENCHANTING_TABLE,
                    EnchantmentTagKeys.NON_TREASURE, EnchantmentTagKeys.TRADEABLE, EnchantmentTagKeys.ON_RANDOM_LOOT)) {
                enchantments.addToTag(tag, enchantment, owner);
            }
        }
        logger.info("[TestPlugin] Enchantments {} and {} registered", LIFESTEAL.key(), GLACIAL.key());
    }

    public static void unregisterAll(final GameRegistries registries, final Plugin owner) {
        registries.writable(RegistryKey.ENCHANTMENT).unregisterAll(owner);
    }
}
