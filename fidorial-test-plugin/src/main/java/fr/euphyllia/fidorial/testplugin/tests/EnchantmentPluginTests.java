package fr.euphyllia.fidorial.testplugin.tests;

import fr.euphyllia.fidorial.testplugin.enchantment.TestEnchantments;
import fr.fidorial.combat.CombatService;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.mob.Mob;
import fr.fidorial.entity.player.Player;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.enchantment.EnchantmentEffects;
import fr.fidorial.item.enchantment.EnchantmentRegistry;
import fr.fidorial.item.enchantment.EnchantmentTags;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.EnchantmentKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.MobEffectKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class EnchantmentPluginTests {

    private static final double FLOOR = 65.0;

    @ScenarioTest(group = "testplugin.enchantments", timeoutTicks = 20)
    public static void pluginEnchantmentsAreRegistered(final ScenarioTestHelper helper) {
        final Player probe = helper.summonPlayer("Probe", Location.of(helper.world(), 0.5, FLOOR, 0.5, 0f, 0f), GameMode.SURVIVAL);
        final EnchantmentRegistry enchantments = probe.server().registries().enchantments();
        helper.assertTrue(enchantments.isCustom(TestEnchantments.LIFESTEAL), "Lifesteal should be registered");
        helper.assertTrue(enchantments.isCustom(TestEnchantments.GLACIAL), "Glacial should be registered");
        helper.assertTrue(enchantments.isTagged(TestEnchantments.LIFESTEAL, EnchantmentTags.IN_ENCHANTING_TABLE),
                "Lifesteal should be offered by the enchanting table");
        helper.assertTrue(enchantments.definition(TestEnchantments.GLACIAL).orElseThrow().hasEffect(EnchantmentEffects.POST_ATTACK),
                "Glacial should carry its post-attack effect");
        helper.assertTrue(!enchantments.areCompatible(TestEnchantments.GLACIAL, EnchantmentKeys.FIRE_ASPECT),
                "Glacial and Fire Aspect are exclusive, both ways");
        helper.assertTrue(enchantments.canEnchant(ItemStack.of(ItemKeys.IRON_SWORD.key()), TestEnchantments.LIFESTEAL),
                "Lifesteal goes on swords");
    }

    @ScenarioTest(group = "testplugin.enchantments", timeoutTicks = 20)
    public static void pluginEnchantedBooksKeepTheirEnchantmentsOnHover(final ScenarioTestHelper helper) {
        final Player probe = helper.summonPlayer("Reader", Location.of(helper.world(), 0.5, FLOOR, 0.5, 0f, 0f), GameMode.SURVIVAL);
        final ItemStack book = probe.server().registries().enchantments().enchantedBook(TestEnchantments.LIFESTEAL, 2);
        helper.assertTrue(ItemStack.of(book.asHoverEvent().value()).equals(book),
                "The hover event of the book should hold all of it");

        final MiniMessage miniMessage = MiniMessage.miniMessage();
        final Component text = miniMessage.deserialize(miniMessage.serialize(Component.text("Book").hoverEvent(book)));
        final ItemStack shown = ItemStack.of((HoverEvent.ShowItem) text.hoverEvent().value());
        helper.assertTrue(shown.equals(book), "MiniMessage changed the book: " + shown);
    }

    @ScenarioTest(group = "testplugin.enchantments", timeoutTicks = 20)
    public static void lifestealHealsTheAttacker(final ScenarioTestHelper helper) {
        final Player player = helper.summonPlayer("Vampire", Location.of(helper.world(), 0.5, FLOOR, 0.5, 0f, 0f), GameMode.SURVIVAL);
        final EnchantmentRegistry enchantments = player.server().registries().enchantments();
        player.setHealth(10f);
        player.setHeldItem(enchantments.enchant(ItemStack.of(ItemKeys.IRON_SWORD.key()), TestEnchantments.LIFESTEAL, 3));
        final Mob cow = cow(player, helper, 1.5);
        helper.assertTrue(combat(player).attack(player, cow), "The hit should land");
        final float dealt = cow.maxHealth() - cow.health();
        helper.assertTrue(dealt > 0f, "The cow should be hurt");
        helper.assertTrue(Math.abs(player.health() - (10f + dealt * 0.3f)) < 1.0e-3f,
                "Lifesteal III should heal 30% of " + dealt + ", the player has " + player.health());
    }

    @ScenarioTest(group = "testplugin.enchantments", timeoutTicks = 20)
    public static void glacialSlowsAndHitsBurningTargetsHarder(final ScenarioTestHelper helper) {
        final Player cold = helper.summonPlayer("Cold", Location.of(helper.world(), 0.5, FLOOR, 0.5, 0f, 0f), GameMode.SURVIVAL);
        final EnchantmentRegistry enchantments = cold.server().registries().enchantments();
        final ItemStack sword = enchantments.enchant(ItemStack.of(ItemKeys.IRON_SWORD.key()), TestEnchantments.GLACIAL, 2);
        final Player colder = helper.summonPlayer("Colder", Location.of(helper.world(), 3.5, FLOOR, 0.5, 0f, 0f), GameMode.SURVIVAL);
        cold.setHeldItem(sword);
        colder.setHeldItem(sword);
        final Mob calm = cow(cold, helper, 1.5);
        final Mob burning = cow(cold, helper, 4.5);
        burning.setFireTicks(100);

        helper.assertTrue(combat(cold).attack(cold, calm), "The first hit should land");
        helper.assertTrue(combat(cold).attack(colder, burning), "The second hit should land");
        helper.assertTrue(calm.hasEffect(MobEffectKeys.SLOWNESS), "Glacial should slow the cow down");
        final float calmDamage = calm.maxHealth() - calm.health();
        final float burningDamage = burning.maxHealth() - burning.health();
        helper.assertTrue(Math.abs(burningDamage - calmDamage - 3f) < 1.0e-3f,
                "Glacial II deals 3 more to a burning target: " + calmDamage + " vs " + burningDamage);
    }

    private static CombatService combat(final Player player) {
        return player.server().services().find(CombatService.class).orElseThrow();
    }

    /**
     * A cow rather than a pig: only the mobs exposing a {@link Mob} handle can be spawned through the API.
     */
    private static Mob cow(final Player player, final ScenarioTestHelper helper, final double x) {
        return player.server().registries().mobs()
                .spawn(Key.key("cow"), Location.of(helper.world(), x, FLOOR, 0.5, 0f, 0f))
                .orElseThrow(() -> new IllegalStateException("The cow could not be spawned"));
    }
}
