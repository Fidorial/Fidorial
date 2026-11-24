package fr.euphyllia.fidorial.server.combat;

import fr.fidorial.attribute.AttributeModifier;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Attribute;
import fr.fidorial.registry.data.MobEffect;
import fr.fidorial.registry.keys.AttributeKeys;
import fr.fidorial.registry.keys.MobEffectKeys;
import net.kyori.adventure.key.Key;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * Works out the value of an attribute of a living entity: its base, then the modifiers of the
 * stacks it wears or holds in the slots they act in, of its effects and of anything else it has.
 */
public final class CombatAttributes {

    private static final Key STRENGTH = Key.key("effect.strength");
    private static final Key WEAKNESS = Key.key("effect.weakness");
    private static final Key HASTE = Key.key("effect.haste");
    private static final Key MINING_FATIGUE = Key.key("effect.mining_fatigue");
    private static final Key ABSORPTION = Key.key("effect.absorption");

    private static final double STRENGTH_PER_LEVEL = 3.0;
    private static final double WEAKNESS_PER_LEVEL = -4.0;
    private static final double HASTE_PER_LEVEL = 0.1;
    private static final double MINING_FATIGUE_PER_LEVEL = -0.1;
    private static final double ABSORPTION_PER_LEVEL = 4.0;

    /**
     * The range each attribute is clamped to once its modifiers are applied.
     */
    private static final Map<TypedKey<Attribute>, double[]> RANGES = Map.ofEntries(
            Map.entry(AttributeKeys.ARMOR, new double[]{0.0, 30.0}),
            Map.entry(AttributeKeys.ARMOR_TOUGHNESS, new double[]{0.0, 20.0}),
            Map.entry(AttributeKeys.ATTACK_DAMAGE, new double[]{0.0, 2048.0}),
            Map.entry(AttributeKeys.ATTACK_KNOCKBACK, new double[]{0.0, 5.0}),
            Map.entry(AttributeKeys.ATTACK_SPEED, new double[]{0.0, 1024.0}),
            Map.entry(AttributeKeys.ENTITY_INTERACTION_RANGE, new double[]{0.0, 64.0}),
            Map.entry(AttributeKeys.KNOCKBACK_RESISTANCE, new double[]{0.0, 1.0}),
            Map.entry(AttributeKeys.EXPLOSION_KNOCKBACK_RESISTANCE, new double[]{0.0, 1.0}),
            Map.entry(AttributeKeys.MAX_ABSORPTION, new double[]{0.0, 2048.0}),
            Map.entry(AttributeKeys.SWEEPING_DAMAGE_RATIO, new double[]{0.0, 1.0}));

    private CombatAttributes() {
        throw new UnsupportedOperationException("CombatAttributes cannot be instantiated.");
    }

    /**
     * {@return the value of an attribute: {@code base} with the modifiers of the equipment and the
     * effects of {@code entity}, then {@code extra}, applied in the order of their operations, and
     * clamped to the range of the attribute}
     *
     * @param attribute the attribute
     * @param base      its base value
     * @param entity    the entity
     * @param extra     further modifiers, such as those of enchantments, by id
     */
    public static double value(final TypedKey<Attribute> attribute, final double base, final LivingEntity entity,
                               final Map<Key, AttributeModifier> extra) {
        final Map<Key, AttributeModifier> modifiers = new LinkedHashMap<>();
        collectEquipment(entity, attribute, modifiers);
        collectEffects(entity, attribute, modifiers);
        modifiers.putAll(extra);
        return clamp(attribute, apply(base, modifiers.values()));
    }

    /**
     * {@return {@code base} with {@code modifiers} applied the vanilla way}: first every added
     * value, then every multiple of that sum, then every multiplier of the total
     */
    public static double apply(final double base, final Collection<AttributeModifier> modifiers) {
        return apply(base, modifiers, AttributeModifier::operation, AttributeModifier::amount);
    }

    /**
     * {@return {@code base} with modifiers of any shape applied the vanilla way}, as
     * {@link #apply(double, Collection)} does
     *
     * @param operation how each modifier changes the value
     * @param amount    the amount of each modifier
     */
    public static <T> double apply(final double base, final Iterable<T> modifiers,
                                   final Function<T, AttributeModifier.Operation> operation,
                                   final ToDoubleFunction<T> amount) {
        double added = base;
        for (final T modifier : modifiers) {
            if (operation.apply(modifier) == AttributeModifier.Operation.ADD_VALUE) {
                added += amount.applyAsDouble(modifier);
            }
        }
        double value = added;
        for (final T modifier : modifiers) {
            if (operation.apply(modifier) == AttributeModifier.Operation.ADD_MULTIPLIED_BASE) {
                value += added * amount.applyAsDouble(modifier);
            }
        }
        for (final T modifier : modifiers) {
            if (operation.apply(modifier) == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                value *= 1.0 + amount.applyAsDouble(modifier);
            }
        }
        return value;
    }

    /**
     * {@return a value clamped to the range of its attribute}
     */
    public static double clamp(final TypedKey<Attribute> attribute, final double value) {
        final double[] range = RANGES.get(attribute);
        return range == null ? value : Math.clamp(value, range[0], range[1]);
    }

    private static void collectEquipment(final LivingEntity entity, final TypedKey<Attribute> attribute,
                                         final Map<Key, AttributeModifier> into) {
        for (final EquipmentSlot slot : EquipmentSlot.values()) {
            final ItemStack stack = entity.equipment(slot);
            if (stack.isEmpty()) {
                continue;
            }
            for (final AttributeModifier modifier : ItemCombatData.of(stack).modifiers()) {
                if (modifier.attribute().equals(attribute) && modifier.slot().test(slot)) {
                    into.put(modifier.id(), modifier);
                }
            }
        }
    }

    private static void collectEffects(final LivingEntity entity, final TypedKey<Attribute> attribute,
                                       final Map<Key, AttributeModifier> into) {
        if (attribute.equals(AttributeKeys.ATTACK_DAMAGE)) {
            effect(entity, MobEffectKeys.STRENGTH, attribute, STRENGTH, STRENGTH_PER_LEVEL,
                    AttributeModifier.Operation.ADD_VALUE, into);
            effect(entity, MobEffectKeys.WEAKNESS, attribute, WEAKNESS, WEAKNESS_PER_LEVEL,
                    AttributeModifier.Operation.ADD_VALUE, into);
        } else if (attribute.equals(AttributeKeys.ATTACK_SPEED)) {
            effect(entity, MobEffectKeys.HASTE, attribute, HASTE, HASTE_PER_LEVEL,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, into);
            effect(entity, MobEffectKeys.MINING_FATIGUE, attribute, MINING_FATIGUE, MINING_FATIGUE_PER_LEVEL,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, into);
        } else if (attribute.equals(AttributeKeys.MAX_ABSORPTION)) {
            effect(entity, MobEffectKeys.ABSORPTION, attribute, ABSORPTION, ABSORPTION_PER_LEVEL,
                    AttributeModifier.Operation.ADD_VALUE, into);
        }
    }

    private static void effect(final LivingEntity entity, final TypedKey<MobEffect> type, final TypedKey<Attribute> attribute,
                               final Key id, final double perLevel, final AttributeModifier.Operation operation,
                               final Map<Key, AttributeModifier> into) {
        final MobEffectInstance effect = entity.effect(type).orElse(null);
        if (effect != null) {
            into.put(id, AttributeModifier.of(attribute, id, perLevel * (effect.amplifier() + 1), operation));
        }
    }
}
