
package fr.euphyllia.fidorial.server.item.enchantment;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.item.enchantment.effect.EnchantmentEffectSet;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.component.ItemEnchantments;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.item.enchantment.EnchantedItem;
import fr.fidorial.item.enchantment.EnchantmentBehaviour;
import fr.fidorial.item.enchantment.EnchantmentDefinition;
import fr.fidorial.registry.TypedKey;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Runs the enchantment hooks of the entities involved in a hit or a tick.
 *
 * <p>Attack hooks read the weapon in the main hand; other hooks read every stack in a slot of its
 * enchantment. A failing behaviour is logged and skipped.</p>
 */
public final class EnchantmentEngine {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(EnchantmentEngine.class);

    /**
     * How deep hits started by enchantments may nest, so that two players wearing Thorns cannot bounce
     * damage back and forth forever.
     */
    private static final int MAX_POST_ATTACK_DEPTH = 2;

    private static final ThreadLocal<int[]> POST_ATTACK_DEPTH = ThreadLocal.withInitial(() -> new int[1]);

    private EnchantmentEngine() {
        throw new UnsupportedOperationException("EnchantmentEngine cannot be instantiated.");
    }

    public static FidorialEnchantments registry() {
        return FidorialServer.getInstance().registries().enchantments();
    }

    @FunctionalInterface
    interface Visitor {
        void visit(EnchantmentBehaviour behaviour, EnchantedItem item);
    }

    /**
     * Visits the behaviours of every enchantment of the stacks owner wears or holds,
     * in a slot where that enchantment acts.
     */
    static void forEachEquipped(final LivingEntity owner, final String hook, final Visitor visitor) {
        final FidorialEnchantments registry = registry();
        for (final EquipmentSlot slot : EquipmentSlot.values()) {
            final ItemStack stack = owner.equipment(slot);
            if (stack.isEmpty()) {
                continue;
            }
            visit(registry, owner, stack, slot, true, hook, visitor);
        }
    }

    /**
     * Visits the behaviours of every enchantment of one stack.
     *
     * @param checkSlot true to skip enchantments that do not act in slot
     */
    static void visit(final FidorialEnchantments registry, final LivingEntity owner, final ItemStack stack,
                      final @Nullable EquipmentSlot slot, final boolean checkSlot, final String hook,
                      final Visitor visitor) {
        final ItemEnchantments enchantments = stack.getOrDefault(
                DataComponentTypes.ENCHANTMENTS, ItemEnchantments.EMPTY);

        if (enchantments.isEmpty()) {
            return;
        }

        for (final Map.Entry<TypedKey<EnchantmentDefinition>, Integer> enchantment
                : enchantments.levels().entrySet()) {

            final List<EnchantmentBehaviour> behaviours = registry.behaviours(enchantment.getKey());

            if (behaviours.isEmpty()) {
                continue;
            }

            if (checkSlot) {
                final EnchantmentDefinitionImpl definition = registry.definition(enchantment.getKey());
                if (definition == null || slot == null || !definition.matchesSlot(slot)) {
                    continue;
                }
            }

            final EnchantedItem item = new EnchantedItemImpl(
                    enchantment.getKey(), enchantment.getValue(), stack, slot, owner);

            for (final EnchantmentBehaviour behaviour : behaviours) {
                try {
                    visitor.visit(behaviour, item);
                } catch (final Throwable throwable) {
                    LOGGER.error("Error in the {} of {} for the enchantment {} on {}",
                            hook, behaviour.getClass().getName(), enchantment.getKey(), owner, throwable);
                }
            }
        }
    }

    private static ItemStack weapon(final @Nullable Entity attacker) {
        return attacker instanceof final LivingEntity living
                ? living.equipment(EquipmentSlot.MAIN_HAND)
                : ItemStack.EMPTY;
    }

    /**
     * Returns the damage of a melee hit once the enchantments changed it.
     */
    public static float modifyDamage(final LivingEntity attacker, final LivingEntity victim,
                                     final DamageSource source, final float damage) {
        return modifyDamage(attacker, weapon(attacker), victim, source, damage);
    }

    /**
     * Returns the damage of a hit once the enchantments of the weapon changed it.
     */
    public static float modifyDamage(final LivingEntity attacker, final ItemStack weapon,
                                     final LivingEntity victim, final DamageSource source,
                                     final float damage) {
        if (weapon.isEmpty()) {
            return damage;
        }

        final AtomicReference<Float> value = new AtomicReference<>(damage);

        visit(registry(), attacker, weapon, EquipmentSlot.MAIN_HAND, false, "modifyDamage",
                (behaviour, item) -> value.set(
                        behaviour.modifyDamage(item, victim, source, value.get())));

        return value.get();
    }

    /**
     * Returns the knockback bonus the weapon gives a melee hit.
     */
    public static float knockbackBonus(final LivingEntity attacker, final LivingEntity victim,
                                       final DamageSource source) {
        return knockbackBonus(attacker, weapon(attacker), victim, source);
    }

    /**
     * Returns the knockback bonus the weapon gives a hit.
     */
    public static float knockbackBonus(final LivingEntity attacker, final ItemStack weapon,
                                       final LivingEntity victim, final DamageSource source) {
        if (weapon.isEmpty()) {
            return 0f;
        }

        final AtomicReference<Float> value = new AtomicReference<>(0f);

        visit(registry(), attacker, weapon, EquipmentSlot.MAIN_HAND, false, "modifyKnockback",
                (behaviour, item) -> value.set(
                        behaviour.modifyKnockback(item, victim, source, value.get())));

        return value.get();
    }

    /**
     * Returns the share of damage the armor absorbs, between 0 and 1.
     */
    public static float armorEffectiveness(final LivingEntity victim, final DamageSource source,
                                           final float effectiveness) {
        final ItemStack weapon = weapon(source.causingEntity());

        if (weapon.isEmpty() || !(source.causingEntity() instanceof final LivingEntity attacker)) {
            return effectiveness;
        }

        final AtomicReference<Float> value = new AtomicReference<>(effectiveness);

        visit(registry(), attacker, weapon, EquipmentSlot.MAIN_HAND, false, "modifyArmorEffectiveness",
                (behaviour, item) -> value.set(
                        behaviour.modifyArmorEffectiveness(item, victim, source, value.get())));

        return Math.clamp(value.get(), 0f, 1f);
    }

    /**
     * Returns the protection points the stacks victim wears give against a hit.
     */
    public static float protection(final LivingEntity victim, final DamageSource source) {
        final AtomicReference<Float> value = new AtomicReference<>(0f);

        forEachEquipped(victim, "modifyProtection",
                (behaviour, item) -> value.set(
                        behaviour.modifyProtection(item, source, value.get())));

        return value.get();
    }

    /**
     * Returns true if a stack victim wears makes it ignore a hit.
     */
    public static boolean isImmune(final LivingEntity victim, final DamageSource source) {
        final AtomicBoolean immune = new AtomicBoolean(false);

        forEachEquipped(victim, "isImmuneTo",
                (behaviour, item) -> immune.set(
                        immune.get() || behaviour.isImmuneTo(item, source)));

        return immune.get();
    }

    /**
     * Runs the effects of a hit that landed: those of the attacker, then those of the victim.
     *
     * @param attackerEffects false to only run those of the victim, for a projectile hit
     */
    public static void postAttack(final LivingEntity victim, final DamageSource source,
                                  final float damage, final boolean attackerEffects) {
        final int[] depth = POST_ATTACK_DEPTH.get();

        if (depth[0] >= MAX_POST_ATTACK_DEPTH) {
            return;
        }

        depth[0]++;

        try {
            final LivingEntity attacker =
                    source.causingEntity() instanceof final LivingEntity living && living != victim
                            ? living
                            : null;

            if (attacker != null && attackerEffects) {
                forEachEquipped(attacker, "onAttack",
                        (behaviour, item) -> behaviour.onAttack(item, victim, source, damage));
            }

            forEachEquipped(victim, "onHurt",
                    (behaviour, item) -> behaviour.onHurt(item, attacker, source, damage));

        } finally {
            depth[0]--;
        }
    }

    /**
     * Changes a value with the vanilla effects of every enchantment of one stack.
     */
    @FunctionalInterface
    public interface ValueHook {
        float apply(EnchantmentEffectSet effects, EnchantedItem item, float value);
    }

    /**
     * Returns value once the vanilla effects of the enchantments changed it.
     */
    public static float processStack(final LivingEntity owner, final ItemStack stack,
                                     final float value, final String name, final ValueHook hook) {
        if (stack.isEmpty()) {
            return value;
        }

        final AtomicReference<Float> result = new AtomicReference<>(value);

        visit(registry(), owner, stack, null, false, name, (behaviour, item) -> {
            if (behaviour instanceof final EnchantmentEffectSet effects) {
                result.set(hook.apply(effects, item, result.get()));
            }
        });

        return result.get();
    }

    /**
     * Returns the durability stack loses once Unbreaking and the like changed it.
     */
    public static int itemDamage(final LivingEntity owner, final ItemStack stack, final int damage) {
        return Math.max(0, Math.round(processStack(owner, stack, damage, "itemDamage",
                (effects, item, value) -> effects.modifyItemDamage(item, value))));
    }

    /**
     * Returns how much ammo a shot uses, as Infinity changes it.
     */
    public static int ammoUse(final LivingEntity owner, final ItemStack weapon,
                              final ItemStack ammo, final int count) {
        return Math.max(0, Math.round(processStack(owner, weapon, count, "ammoUse",
                (effects, item, value) -> effects.modifyAmmoUse(item, ammo, value))));
    }

    /**
     * Runs the effects the enchantments of a weapon have on a projectile it just shot.
     */
    public static void projectileSpawned(final LivingEntity owner, final ItemStack weapon,
                                         final Entity projectile) {
        processStack(owner, weapon, 0f, "projectileSpawned", (effects, item, value) -> {
            effects.onProjectileSpawned(item, projectile);
            return value;
        });
    }

    /**
     * Returns the damage a smash attack adds per block fallen.
     */
    public static float smashDamagePerFallenBlock(final LivingEntity attacker, final ItemStack weapon,
                                                  final LivingEntity victim, final DamageSource source) {
        return processStack(attacker, weapon, 0f, "smashDamagePerFallenBlock",
                (effects, item, value) ->
                        effects.modifySmashDamagePerFallenBlock(item, victim, source, value));
    }

    /**
     * Returns true if an enchantment of a stack worn in an armor slot keeps it there.
     */
    public static boolean preventsArmorChange(final LivingEntity owner, final ItemStack stack,
                                              final EquipmentSlot slot) {
        final AtomicBoolean prevents = new AtomicBoolean(false);

        visit(registry(), owner, stack, slot, false, "preventsArmorChange",
                (behaviour, item) -> prevents.set(
                        prevents.get() || behaviour.preventsArmorChange(item)));

        return prevents.get();
    }

    /**
     * Returns true if an enchantment destroys its stack instead of letting it drop when its owner dies.
     */
    public static boolean preventsDrop(final LivingEntity owner, final ItemStack stack) {
        final AtomicBoolean prevents = new AtomicBoolean(false);

        visit(registry(), owner, stack, null, false, "preventsDrop",
                (behaviour, item) -> prevents.set(
                        prevents.get() || behaviour.preventsDrop(item)));

        return prevents.get();
    }
}
