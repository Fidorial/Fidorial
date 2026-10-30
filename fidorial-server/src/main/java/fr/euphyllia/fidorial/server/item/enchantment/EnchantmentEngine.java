package fr.euphyllia.fidorial.server.item.enchantment;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.component.ItemEnchantments;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.item.enchantment.EnchantedItem;
import fr.fidorial.item.enchantment.EnchantmentBehaviour;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

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
     * Visits the behaviours of every enchantment of the stacks {@code owner} wears or holds, in a slot
     * where that enchantment acts.
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
     * @param checkSlot {@code true} to skip the enchantments that do not act in {@code slot}
     */
    static void visit(final FidorialEnchantments registry, final LivingEntity owner, final ItemStack stack,
                      final @Nullable EquipmentSlot slot, final boolean checkSlot, final String hook, final Visitor visitor) {
        final ItemEnchantments enchantments = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) {
            return;
        }
        for (final Map.Entry<Key, Integer> enchantment : enchantments.levels().entrySet()) {
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
            final EnchantedItem item = new EnchantedItemImpl(TypedKey.create(RegistryKey.ENCHANTMENT, enchantment.getKey()),
                    enchantment.getValue(), stack, slot, owner);
            for (final EnchantmentBehaviour behaviour : behaviours) {
                try {
                    visitor.visit(behaviour, item);
                } catch (final Throwable throwable) {
                    LOGGER.error("Error in the {} of {} for the enchantment {} on {}", hook, behaviour.getClass().getName(),
                            enchantment.getKey().asString(), owner, throwable);
                }
            }
        }
    }

    private static ItemStack weapon(final @Nullable Entity attacker) {
        return attacker instanceof final LivingEntity living ? living.equipment(EquipmentSlot.MAIN_HAND) : ItemStack.EMPTY;
    }

    /**
     * {@return the damage of a melee hit once the enchantments of the weapon of {@code attacker} changed it}
     */
    public static float modifyDamage(final LivingEntity attacker, final LivingEntity victim,
                                     final DamageSource source, final float damage) {
        final ItemStack weapon = weapon(attacker);
        if (weapon.isEmpty()) {
            return damage;
        }
        final float[] value = {damage};
        visit(registry(), attacker, weapon, EquipmentSlot.MAIN_HAND, false, "modifyDamage",
                (behaviour, item) -> value[0] = behaviour.modifyDamage(item, victim, source, value[0]));
        return value[0];
    }

    /**
     * {@return the knockback bonus the weapon of {@code attacker} gives a melee hit, in vanilla units}
     */
    public static float knockbackBonus(final LivingEntity attacker, final LivingEntity victim, final DamageSource source) {
        final ItemStack weapon = weapon(attacker);
        if (weapon.isEmpty()) {
            return 0f;
        }
        final float[] value = {0f};
        visit(registry(), attacker, weapon, EquipmentSlot.MAIN_HAND, false, "modifyKnockback",
                (behaviour, item) -> value[0] = behaviour.modifyKnockback(item, victim, source, value[0]));
        return value[0];
    }

    /**
     * {@return the share of the damage the armor of {@code victim} absorbs, once the weapon of the
     * attacker changed it, between {@code 0} and {@code 1}}
     */
    public static float armorEffectiveness(final LivingEntity victim, final DamageSource source, final float effectiveness) {
        final ItemStack weapon = weapon(source.causingEntity());
        if (weapon.isEmpty() || !(source.causingEntity() instanceof final LivingEntity attacker)) {
            return effectiveness;
        }
        final float[] value = {effectiveness};
        visit(registry(), attacker, weapon, EquipmentSlot.MAIN_HAND, false, "modifyArmorEffectiveness",
                (behaviour, item) -> value[0] = behaviour.modifyArmorEffectiveness(item, victim, source, value[0]));
        return Math.clamp(value[0], 0f, 1f);
    }

    /**
     * {@return the protection points the stacks {@code victim} wears give against a hit}
     */
    public static float protection(final LivingEntity victim, final DamageSource source) {
        final float[] value = {0f};
        forEachEquipped(victim, "modifyProtection",
                (behaviour, item) -> value[0] = behaviour.modifyProtection(item, source, value[0]));
        return value[0];
    }

    /**
     * {@return {@code true} if a stack {@code victim} wears makes it ignore a hit}
     */
    public static boolean isImmune(final LivingEntity victim, final DamageSource source) {
        final boolean[] immune = {false};
        forEachEquipped(victim, "isImmuneTo", (behaviour, item) -> immune[0] |= behaviour.isImmuneTo(item, source));
        return immune[0];
    }

    /**
     * Runs the effects of a hit that landed: those of the stacks of the attacker, then those of the
     * stacks of the victim.
     *
     * @param attackerEffects {@code false} to only run those of the victim, for a projectile hit
     */
    public static void postAttack(final LivingEntity victim, final DamageSource source, final float damage,
                                  final boolean attackerEffects) {
        final int[] depth = POST_ATTACK_DEPTH.get();
        if (depth[0] >= MAX_POST_ATTACK_DEPTH) {
            return;
        }
        depth[0]++;
        try {
            final LivingEntity attacker = source.causingEntity() instanceof final LivingEntity living && living != victim
                    ? living
                    : null;
            if (attacker != null && attackerEffects) {
                forEachEquipped(attacker, "onAttack", (behaviour, item) -> behaviour.onAttack(item, victim, source, damage));
            }
            forEachEquipped(victim, "onHurt", (behaviour, item) -> behaviour.onHurt(item, attacker, source, damage));
        } finally {
            depth[0]--;
        }
    }

    /**
     * {@return {@code true} if an enchantment of {@code stack}, worn in an armor slot, keeps it there}
     */
    public static boolean preventsArmorChange(final LivingEntity owner, final ItemStack stack, final EquipmentSlot slot) {
        final boolean[] prevents = {false};
        visit(registry(), owner, stack, slot, false, "preventsArmorChange",
                (behaviour, item) -> prevents[0] |= behaviour.preventsArmorChange(item));
        return prevents[0];
    }

    /**
     * {@return {@code true} if an enchantment of {@code stack} destroys it instead of letting it drop
     * when its owner dies}
     */
    public static boolean preventsDrop(final LivingEntity owner, final ItemStack stack) {
        final boolean[] prevents = {false};
        visit(registry(), owner, stack, null, false, "preventsDrop",
                (behaviour, item) -> prevents[0] |= behaviour.preventsDrop(item));
        return prevents[0];
    }
}
