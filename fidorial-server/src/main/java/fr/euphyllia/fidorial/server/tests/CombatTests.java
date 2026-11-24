package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.combat.ChargedProjectiles;
import fr.euphyllia.fidorial.server.combat.CombatEngine;
import fr.euphyllia.fidorial.server.combat.CombatRules;
import fr.euphyllia.fidorial.server.combat.DamageSourceFactoryImpl;
import fr.euphyllia.fidorial.server.combat.RangedWeapons;
import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.euphyllia.fidorial.server.entity.mob.MobFactories;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.entity.projectile.ArrowEntity;
import fr.euphyllia.fidorial.server.entity.projectile.ArrowProjectileEntity;
import fr.euphyllia.fidorial.server.entity.projectile.TridentEntity;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.math.Location;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MobEffect;
import fr.fidorial.registry.keys.EnchantmentKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.MobEffectKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * The vanilla combat rules: weapons, cooldown, critical hits, sweeps, armor, effects, shields,
 * totems, maces, bows, crossbows and tridents.
 */
@SuppressWarnings("unused")
public final class CombatTests {

    private static final double FLOOR = 65.0;

    private static FidorialServer server() {
        return FidorialServer.getInstance();
    }

    private static CombatEngine combat() {
        return server().entities().combat();
    }

    private static ServerPlayer player(final ScenarioTestHelper helper, final String name, final double x, final double z,
                                       final float yaw) {
        return (ServerPlayer) helper.summonPlayer(name, Location.of(helper.world(), x, FLOOR, z, yaw, 0f), GameMode.SURVIVAL);
    }

    private static AbstractMob mob(final ScenarioTestHelper helper, final fr.fidorial.entity.EntityType type,
                                   final double x, final double z) {
        final AbstractMob mob = MobFactories.create(type, server().entities().ids().allocate(),
                Location.of(helper.world(), x, FLOOR, z, 0f, 0f));
        server().entities().spawn(mob);
        return mob;
    }

    private static MobEffectInstance effect(final TypedKey<MobEffect> type, final int amplifier) {
        return server().registries().effects().builder(type).amplifier(amplifier).duration(200).build();
    }

    private static ItemStack enchanted(final TypedKey<fr.fidorial.registry.data.Item> item,
                                       final TypedKey<fr.fidorial.item.enchantment.EnchantmentDefinition> enchantment,
                                       final int level) {
        return server().registries().enchantments().enchant(ItemStack.of(item), enchantment, level);
    }

    private static void approx(final ScenarioTestHelper helper, final float actual, final float expected, final String what) {
        helper.assertTrue(Math.abs(actual - expected) < 1.0e-3f, what + ": expected " + expected + ", got " + actual);
    }

    private static <T extends AbstractEntity> List<T> entities(final ScenarioTestHelper helper, final Class<T> type,
                                                               final Predicate<T> filter) {
        final List<T> found = new ArrayList<>();
        ((ServerWorld) helper.world()).entityManager().forEachInChunkRange(0, 0, 2, entity -> {
            if (type.isInstance(entity) && !entity.isRemoved() && filter.test(type.cast(entity))) {
                found.add(type.cast(entity));
            }
        });
        return found;
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void weaponsSetTheAttackDamageAndSpeed(final ScenarioTestHelper helper) {
        final ServerPlayer player = player(helper, "Armory", 0.5, 0.5, 0f);
        approx(helper, combat().attackDamage(player), 1.0f, "A bare hand deals 1");
        helper.assertTrue(combat().attackCooldownTicks(player) == 5, "A bare hand recharges in 5 ticks");
        player.setHeldItem(ItemStack.of(ItemKeys.DIAMOND_SWORD));
        approx(helper, combat().attackDamage(player), 7.0f, "A diamond sword deals 7");
        helper.assertTrue(combat().attackCooldownTicks(player) == 13,
                "A sword recharges in 12.5 ticks, got " + combat().attackCooldownTicks(player));
        player.setHeldItem(ItemStack.of(ItemKeys.NETHERITE_AXE));
        approx(helper, combat().attackDamage(player), 10.0f, "A netherite axe deals 10");
        player.inventory().set(PlayerInventory.OFF_HAND, ItemStack.of(ItemKeys.DIAMOND_SWORD));
        approx(helper, combat().attackDamage(player), 10.0f, "A sword in the off hand adds nothing");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void aSpammedSwingDealsAFifth(final ScenarioTestHelper helper) {
        final ServerPlayer player = player(helper, "Spammer", 0.5, 0.5, 0f);
        player.setHeldItem(ItemStack.of(ItemKeys.DIAMOND_SWORD));
        final AbstractMob pig = mob(helper, EntityTypes.PIG, 0.5, 1.5);
        player.resetAttackCooldown();
        helper.assertTrue(combat().attack(player, pig), "The hit should land");
        final float scale = CombatRules.attackDamageScale(0.5f / 12.5f);
        approx(helper, pig.maxHealth() - pig.health(), 7.0f * scale, "An uncharged swing");
        helper.assertTrue(player.heldItem().getOrDefault(DataComponentTypes.DAMAGE, 0) == 1, "The sword wears by one");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void fallingMakesACriticalHit(final ScenarioTestHelper helper) {
        final ServerPlayer player = player(helper, "Critic", 0.5, 0.5, 0f);
        final AbstractMob pig = mob(helper, EntityTypes.PIG, 0.5, 1.5);
        player.setOnGround(false);
        player.setFallDistance(1.0);
        helper.assertTrue(combat().attack(player, pig), "The hit should land");
        approx(helper, pig.maxHealth() - pig.health(), 1.5f, "A critical hit deals half more");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void aSwordSweepsTheEntitiesAround(final ScenarioTestHelper helper) {
        final ServerPlayer player = player(helper, "Sweeper", 0.5, 0.5, 0f);
        player.setHeldItem(ItemStack.of(ItemKeys.WOODEN_SWORD));
        player.setOnGround(true);
        final AbstractMob target = mob(helper, EntityTypes.PIG, 0.5, 2.0);
        final AbstractMob beside = mob(helper, EntityTypes.PIG, 1.5, 2.0);
        helper.assertTrue(combat().attack(player, target), "The hit should land");
        approx(helper, target.maxHealth() - target.health(), 4.0f, "A wooden sword deals 4");
        approx(helper, beside.maxHealth() - beside.health(), 1.0f, "A sweep without Sweeping Edge deals 1");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void aSpearThrustsThroughEveryEntityInFront(final ScenarioTestHelper helper) {
        final ServerPlayer player = (ServerPlayer) helper.summonPlayer("Spearman",
                Location.of(helper.world(), 0.5, FLOOR, 0.5, 0f, 20f), GameMode.SURVIVAL);
        player.setHeldItem(ItemStack.of(ItemKeys.IRON_SPEAR));
        final AbstractMob near = mob(helper, EntityTypes.PIG, 0.5, 3.0);
        final AbstractMob far = mob(helper, EntityTypes.PIG, 0.5, 4.0);
        final AbstractMob aside = mob(helper, EntityTypes.PIG, 3.5, 3.0);
        helper.assertTrue(combat().stab(player), "The thrust should land");
        approx(helper, near.maxHealth() - near.health(), 3f, "An iron spear deals 3 to the first pig");
        approx(helper, far.maxHealth() - far.health(), 3f, "and 3 to the one behind it");
        approx(helper, aside.health(), aside.maxHealth(), "The pig aside is not hit");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void armorSoftensHitsAndWears(final ScenarioTestHelper helper) {
        final ServerPlayer player = player(helper, "Knight", 0.5, 0.5, 0f);
        player.inventory().set(PlayerInventory.HEAD, ItemStack.of(ItemKeys.DIAMOND_HELMET));
        player.inventory().set(PlayerInventory.CHEST, ItemStack.of(ItemKeys.DIAMOND_CHESTPLATE));
        player.inventory().set(PlayerInventory.LEGS, ItemStack.of(ItemKeys.DIAMOND_LEGGINGS));
        player.inventory().set(PlayerInventory.FEET, ItemStack.of(ItemKeys.DIAMOND_BOOTS));
        approx(helper, (float) player.armor(), 20f, "Full diamond armor gives 20 points");
        approx(helper, (float) player.armorToughness(), 8f, "and 8 of toughness");
        player.damage(DamageSourceFactoryImpl.INSTANCE.mobAttack(null), 10f);
        approx(helper, player.maxHealth() - player.health(), 3.0f, "Full diamond armor takes 70% of a 10 damage hit");
        helper.assertTrue(player.inventory().get(PlayerInventory.HEAD).getOrDefault(DataComponentTypes.DAMAGE, 0) == 2,
                "Each piece wears by a quarter of the hit");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void effectsChangeTheFight(final ScenarioTestHelper helper) {
        final ServerPlayer strong = player(helper, "Strong", 0.5, 0.5, 0f);
        strong.addEffect(effect(MobEffectKeys.STRENGTH, 0));
        approx(helper, combat().attackDamage(strong), 4.0f, "Strength I adds 3");

        final ServerPlayer resistant = player(helper, "Resistant", 3.5, 0.5, 0f);
        resistant.addEffect(effect(MobEffectKeys.RESISTANCE, 1));
        resistant.damage(DamageSourceFactoryImpl.INSTANCE.generic(), 10f);
        approx(helper, resistant.maxHealth() - resistant.health(), 6.0f, "Resistance II takes 40% off");

        final ServerPlayer fireproof = player(helper, "Fireproof", 6.5, 0.5, 0f);
        fireproof.addEffect(effect(MobEffectKeys.FIRE_RESISTANCE, 0));
        helper.assertFalse(fireproof.damage(DamageSourceFactoryImpl.INSTANCE.lava(), 4f), "Fire Resistance ignores lava");
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void aRaisedShieldBlocksWhatComesFromTheFront(final ScenarioTestHelper helper) {
        // yaw 0: the player looks along +z
        final ServerPlayer player = player(helper, "Shieldbearer", 0.5, 0.5, 0f);
        final ServerPlayer front = player(helper, "InFront", 0.5, 2.5, 180f);
        final ServerPlayer back = player(helper, "Behind", 0.5, -1.5, 0f);
        player.inventory().set(PlayerInventory.OFF_HAND, ItemStack.of(ItemKeys.SHIELD));
        helper.sequence()
                .execute(() -> combat().items().use(player, EquipmentSlot.OFF_HAND))
                .waitUntil(() -> combat().isBlocking(player), "The shield should be up after its delay")
                .execute(() -> {
                    helper.assertFalse(player.damage(DamageSourceFactoryImpl.INSTANCE.playerAttack(front), 5f),
                            "A hit from the front is blocked");
                    approx(helper, player.health(), player.maxHealth(), "The shield took the whole hit");
                    helper.assertTrue(player.inventory().get(PlayerInventory.OFF_HAND).getOrDefault(DataComponentTypes.DAMAGE, 0) == 6,
                            "Blocking 5 damage wears the shield by 6");
                    player.setInvulnerableTicks(0);
                    helper.assertTrue(player.damage(DamageSourceFactoryImpl.INSTANCE.playerAttack(back), 5f),
                            "A hit from behind gets through");
                    approx(helper, player.maxHealth() - player.health(), 5f, "The hit from behind hurts");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void anAxeDisablesAShield(final ScenarioTestHelper helper) {
        final ServerPlayer blocker = player(helper, "Blocker", 0.5, 0.5, 0f);
        final ServerPlayer axeman = player(helper, "Axeman", 0.5, 2.5, 180f);
        blocker.inventory().set(PlayerInventory.OFF_HAND, ItemStack.of(ItemKeys.SHIELD));
        axeman.setHeldItem(ItemStack.of(ItemKeys.IRON_AXE));
        helper.sequence()
                .execute(() -> combat().items().use(blocker, EquipmentSlot.OFF_HAND))
                .waitUntil(() -> combat().isBlocking(blocker), "The shield should be up after its delay")
                .execute(() -> {
                    helper.assertFalse(combat().attack(axeman, blocker), "The axe hits the shield");
                    helper.assertFalse(combat().isBlocking(blocker), "The axe lowered the shield");
                    helper.assertTrue(blocker.isCoolingDown(ItemKeys.SHIELD.key()), "The shield is disabled for a while");
                    helper.assertFalse(combat().items().use(blocker, EquipmentSlot.OFF_HAND) && blocker.itemUse().isUsing(),
                            "A disabled shield cannot be raised again");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void aTotemSavesFromDeath(final ScenarioTestHelper helper) {
        final ServerPlayer player = player(helper, "Undying", 0.5, 0.5, 0f);
        player.inventory().set(PlayerInventory.OFF_HAND, ItemStack.of(ItemKeys.TOTEM_OF_UNDYING));
        player.damage(DamageSourceFactoryImpl.INSTANCE.generic(), 100f);
        helper.assertFalse(player.isDead(), "The totem should have saved the player");
        approx(helper, player.health(), 1f, "A saved player is left with half a heart");
        helper.assertTrue(player.inventory().get(PlayerInventory.OFF_HAND).isEmpty(), "The totem is used up");
        helper.assertTrue(player.hasEffect(MobEffectKeys.REGENERATION) && player.hasEffect(MobEffectKeys.FIRE_RESISTANCE),
                "The totem gives Regeneration and Fire Resistance");
        approx(helper, player.absorptionAmount(), 8f, "Absorption II gives 4 hearts");

        final ServerPlayer killed = player(helper, "Killed", 3.5, 0.5, 0f);
        killed.inventory().set(PlayerInventory.OFF_HAND, ItemStack.of(ItemKeys.TOTEM_OF_UNDYING));
        killed.damage(DamageSourceFactoryImpl.INSTANCE.genericKill(), Float.MAX_VALUE);
        helper.assertTrue(killed.isDead(), "Nothing saves from /kill");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void aMaceHitsHarderAfterAFall(final ScenarioTestHelper helper) {
        approx(helper, (float) CombatRules.smashBonus(3.0), 12f, "4 per block over the first three");
        approx(helper, (float) CombatRules.smashBonus(8.0), 22f, "then 2 per block over the next five");
        approx(helper, (float) CombatRules.smashBonus(10.0), 24f, "then 1 per block");

        final ServerPlayer player = player(helper, "Smasher", 0.5, 0.5, 0f);
        player.setHeldItem(ItemStack.of(ItemKeys.MACE));
        final AbstractMob zombie = mob(helper, EntityTypes.ZOMBIE, 0.5, 1.5);
        player.setOnGround(false);
        player.setFallDistance(2.0);
        helper.assertTrue(combat().attack(player, zombie), "The smash should land");
        helper.assertTrue(zombie.isDead() || zombie.maxHealth() - zombie.health() > 6f,
                "A smash from 2 blocks deals more than a plain hit");
        approx(helper, (float) player.fallDistance(), 0f, "A smash ends the fall");
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void aBowShootsAnArrowAndUsesOne(final ScenarioTestHelper helper) {
        final ServerPlayer player = player(helper, "Archer", 0.5, 0.5, 0f);
        player.setHeldItem(ItemStack.of(ItemKeys.BOW));
        player.inventory().set(1, ItemStack.of(ItemKeys.ARROW, 2));
        helper.sequence()
                .execute(() -> helper.assertTrue(combat().items().use(player, EquipmentSlot.MAIN_HAND), "A bow is used"))
                .waitUntil(() -> player.itemUse().ticks() >= 20, "The bow should be drawn for a second")
                .execute(() -> combat().items().release(player))
                .waitUntil(() -> !entities(helper, ArrowEntity.class, arrow -> arrow.owner() == player).isEmpty()
                        || player.inventory().get(1).count() == 1, "The arrow should be shot")
                .execute(() -> {
                    helper.assertTrue(player.inventory().get(1).count() == 1, "One arrow is used");
                    helper.assertTrue(player.heldItem().getOrDefault(DataComponentTypes.DAMAGE, 0) == 1, "The bow wears by one");
                    approx(helper, RangedWeapons.bowPower(20), 1.0f, "A bow drawn a second is at full power");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void infinityKeepsTheArrow(final ScenarioTestHelper helper) {
        final ServerPlayer player = player(helper, "Infinite", 0.5, 0.5, 0f);
        player.setHeldItem(enchanted(ItemKeys.BOW, EnchantmentKeys.INFINITY, 1));
        player.inventory().set(1, ItemStack.of(ItemKeys.ARROW, 1));
        helper.sequence()
                .execute(() -> combat().items().use(player, EquipmentSlot.MAIN_HAND))
                .waitUntil(() -> player.itemUse().ticks() >= 20, "The bow should be drawn for a second")
                .execute(() -> {
                    combat().items().release(player);
                    helper.assertTrue(player.inventory().get(1).count() == 1, "Infinity keeps the arrow");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 80)
    public static void aCrossbowLoadsThenShoots(final ScenarioTestHelper helper) {
        final ServerPlayer player = player(helper, "Crossbowman", 0.5, 0.5, 0f);
        player.setHeldItem(ItemStack.of(ItemKeys.CROSSBOW));
        player.inventory().set(1, ItemStack.of(ItemKeys.ARROW, 2));
        helper.sequence()
                .execute(() -> combat().items().use(player, EquipmentSlot.MAIN_HAND))
                .waitUntil(() -> player.heldItem().get(ChargedProjectiles.TYPE) != null,
                        "The crossbow should load once charged")
                .execute(() -> {
                    helper.assertTrue(player.inventory().get(1).count() == 1, "Loading uses one arrow");
                    combat().items().use(player, EquipmentSlot.MAIN_HAND);
                    helper.assertTrue(player.heldItem().get(ChargedProjectiles.TYPE) == null, "Shooting empties the crossbow");
                    helper.assertFalse(entities(helper, ArrowEntity.class, arrow -> arrow.owner() == player).isEmpty(),
                            "The crossbow shot an arrow");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void aTridentIsThrown(final ScenarioTestHelper helper) {
        final ServerPlayer player = player(helper, "Thrower", 0.5, 0.5, 0f);
        player.setHeldItem(ItemStack.of(ItemKeys.TRIDENT));
        helper.sequence()
                .execute(() -> combat().items().use(player, EquipmentSlot.MAIN_HAND))
                .waitUntil(() -> player.itemUse().ticks() >= 10, "The trident should be readied")
                .execute(() -> {
                    combat().items().release(player);
                    helper.assertTrue(player.heldItem().isEmpty(), "The trident leaves the hand");
                    helper.assertFalse(entities(helper, TridentEntity.class, trident -> trident.owner() == player).isEmpty(),
                            "The trident flies");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void anArrowHurtsWhatItHits(final ScenarioTestHelper helper) {
        // a player nearby keeps the chunks loaded
        player(helper, "Watcher", 0.5, -3.5, 0f);
        final AbstractMob pig = mob(helper, EntityTypes.PIG, 4.5, 0.5);
        final ArrowEntity arrow = new ArrowEntity(server().entities().ids().allocate(),
                Location.of(helper.world(), 0.5, FLOOR + 0.5, 0.5, 0f, 0f), null, ItemStack.EMPTY, ItemStack.of(ItemKeys.ARROW));
        arrow.setPickup(ArrowProjectileEntity.PickupRule.ANY_PLAYER);
        arrow.launch(1.0, 0.0, 0.0, 2.0, 0.0);
        server().entities().spawn(arrow);
        helper.sequence()
                .waitUntil(() -> helper.assertTrue(pig.health() < pig.maxHealth(), "The arrow should hit the pig: arrow at "
                        + arrow.location() + ", removed: " + arrow.isRemoved() + ", in the ground: " + arrow.isStuck()))
                .execute(() -> {
                    approx(helper, pig.maxHealth() - pig.health(), 4f, "An arrow at 2 blocks per tick deals 4");
                    helper.assertTrue(arrow.isRemoved(), "The arrow is gone once it hit");
                })
                .build();
    }
}
