package fr.euphyllia.fidorial.server.item.enchantment.effect;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.fidorial.attribute.AttributeModifier;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.enchantment.EnchantedItem;
import fr.fidorial.item.enchantment.EnchantmentBehaviour;
import fr.fidorial.item.enchantment.EnchantmentEffects;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Attribute;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * The compiled {@code effects} object of an enchantment, run as its first {@link EnchantmentBehaviour}.
 *
 * <p>Every vanilla component is accepted and sent to clients; only those in {@link #SUPPORTED} are run.
 * Effects the server cannot evaluate are {@linkplain #skipped() skipped}.</p>
 */
public final class EnchantmentEffectSet implements EnchantmentBehaviour {

    /**
     * Every effect component of vanilla data.
     */
    public static final Set<Key> KNOWN = Set.of(
            EnchantmentEffects.AMMO_USE, EnchantmentEffects.ARMOR_EFFECTIVENESS, EnchantmentEffects.ATTRIBUTES,
            EnchantmentEffects.BLOCK_EXPERIENCE, EnchantmentEffects.CROSSBOW_CHARGE_TIME,
            EnchantmentEffects.CROSSBOW_CHARGING_SOUNDS, EnchantmentEffects.DAMAGE, EnchantmentEffects.DAMAGE_IMMUNITY,
            EnchantmentEffects.DAMAGE_PROTECTION, EnchantmentEffects.EQUIPMENT_DROPS, EnchantmentEffects.FISHING_LUCK_BONUS,
            EnchantmentEffects.FISHING_TIME_REDUCTION, EnchantmentEffects.HIT_BLOCK, EnchantmentEffects.ITEM_DAMAGE,
            EnchantmentEffects.KNOCKBACK, EnchantmentEffects.LOCATION_CHANGED, EnchantmentEffects.MOB_EXPERIENCE,
            EnchantmentEffects.POST_ATTACK, EnchantmentEffects.POST_PIERCING_ATTACK, EnchantmentEffects.PREVENT_ARMOR_CHANGE,
            EnchantmentEffects.PREVENT_EQUIPMENT_DROP, EnchantmentEffects.PROJECTILE_COUNT,
            EnchantmentEffects.PROJECTILE_PIERCING, EnchantmentEffects.PROJECTILE_SPAWNED,
            EnchantmentEffects.PROJECTILE_SPREAD, EnchantmentEffects.REPAIR_WITH_XP,
            EnchantmentEffects.SMASH_DAMAGE_PER_FALLEN_BLOCK, EnchantmentEffects.TICK,
            EnchantmentEffects.TRIDENT_RETURN_ACCELERATION, EnchantmentEffects.TRIDENT_SOUND,
            EnchantmentEffects.TRIDENT_SPIN_ATTACK_STRENGTH);

    /**
     * The components the server runs.
     */
    public static final Set<Key> SUPPORTED = Set.of(
            EnchantmentEffects.AMMO_USE, EnchantmentEffects.ARMOR_EFFECTIVENESS, EnchantmentEffects.ATTRIBUTES,
            EnchantmentEffects.CROSSBOW_CHARGE_TIME, EnchantmentEffects.DAMAGE,
            EnchantmentEffects.DAMAGE_IMMUNITY, EnchantmentEffects.DAMAGE_PROTECTION, EnchantmentEffects.ITEM_DAMAGE,
            EnchantmentEffects.KNOCKBACK, EnchantmentEffects.POST_ATTACK, EnchantmentEffects.PREVENT_ARMOR_CHANGE,
            EnchantmentEffects.PREVENT_EQUIPMENT_DROP, EnchantmentEffects.PROJECTILE_COUNT,
            EnchantmentEffects.PROJECTILE_PIERCING, EnchantmentEffects.PROJECTILE_SPAWNED,
            EnchantmentEffects.PROJECTILE_SPREAD, EnchantmentEffects.SMASH_DAMAGE_PER_FALLEN_BLOCK,
            EnchantmentEffects.TICK, EnchantmentEffects.TRIDENT_RETURN_ACCELERATION);

    /**
     * An enchantment without effect.
     */
    public static final EnchantmentEffectSet EMPTY = new EnchantmentEffectSet(
            Set.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
            List.of(), false, false, List.of(), Projectiles.NONE);

    private final Set<Key> components;
    private final List<Conditional<ValueEffect>> damage;
    private final List<Conditional<ValueEffect>> damageProtection;
    private final List<Conditional<ValueEffect>> knockback;
    private final List<Conditional<ValueEffect>> armorEffectiveness;
    private final List<Conditional<Boolean>> damageImmunity;
    private final List<Targeted> postAttack;
    private final List<Conditional<EntityEffect>> tick;
    private final List<AttributeEffect> attributes;
    private final List<String> skipped;
    private final boolean preventArmorChange;
    private final boolean preventEquipmentDrop;
    private final List<Key> notRun;
    private final Projectiles projectiles;

    private EnchantmentEffectSet(
            final Set<Key> components,
            final List<Conditional<ValueEffect>> damage,
            final List<Conditional<ValueEffect>> damageProtection,
            final List<Conditional<ValueEffect>> knockback,
            final List<Conditional<ValueEffect>> armorEffectiveness,
            final List<Conditional<Boolean>> damageImmunity,
            final List<Targeted> postAttack,
            final List<Conditional<EntityEffect>> tick,
            final List<AttributeEffect> attributes,
            final List<String> skipped,
            final boolean preventArmorChange,
            final boolean preventEquipmentDrop,
            final List<Key> notRun,
            final Projectiles projectiles
    ) {
        this.components = components;
        this.damage = damage;
        this.damageProtection = damageProtection;
        this.knockback = knockback;
        this.armorEffectiveness = armorEffectiveness;
        this.damageImmunity = damageImmunity;
        this.postAttack = postAttack;
        this.tick = tick;
        this.attributes = attributes;
        this.skipped = skipped;
        this.preventArmorChange = preventArmorChange;
        this.preventEquipmentDrop = preventEquipmentDrop;
        this.notRun = notRun;
        this.projectiles = projectiles;
    }

    /**
     * Compiles an {@code effects} object.
     *
     * @param effects the object, {@code null} for none
     * @return the compiled effects
     * @throws IllegalArgumentException if the object names an unknown component, or holds malformed data
     */
    public static EnchantmentEffectSet compile(final @Nullable JsonObject effects) {
        if (effects == null || effects.isEmpty()) {
            return EMPTY;
        }
        final Set<Key> components = new LinkedHashSet<>();
        final List<String> skipped = new ArrayList<>();
        final List<Key> notRun = new ArrayList<>();
        List<Conditional<ValueEffect>> damage = List.of();
        List<Conditional<ValueEffect>> damageProtection = List.of();
        List<Conditional<ValueEffect>> knockback = List.of();
        List<Conditional<ValueEffect>> armorEffectiveness = List.of();
        List<Conditional<Boolean>> damageImmunity = List.of();
        List<Targeted> postAttack = List.of();
        List<Conditional<EntityEffect>> tick = List.of();
        List<AttributeEffect> attributes = List.of();
        boolean preventArmorChange = false;
        boolean preventEquipmentDrop = false;
        List<Conditional<ValueEffect>> itemDamage = List.of();
        List<Conditional<ValueEffect>> ammoUse = List.of();
        List<Conditional<ValueEffect>> projectilePiercing = List.of();
        List<Conditional<ValueEffect>> projectileCount = List.of();
        List<Conditional<ValueEffect>> projectileSpread = List.of();
        List<Conditional<ValueEffect>> tridentReturnAcceleration = List.of();
        List<Conditional<ValueEffect>> smashDamagePerFallenBlock = List.of();
        List<Conditional<EntityEffect>> projectileSpawned = List.of();
        ValueEffect crossbowChargeTime = null;

        for (final Map.Entry<String, JsonElement> entry : effects.entrySet()) {
            final Key component = EffectJson.key(entry.getKey());
            if (!KNOWN.contains(component)) {
                throw new IllegalArgumentException("Unknown enchantment effect component " + component.asString());
            }
            if (!components.add(component)) {
                throw new IllegalArgumentException("Enchantment effect component " + component.asString() + " is listed twice");
            }
            final JsonElement value = entry.getValue();
            final String name = component.value();
            final String where = component.asString();
            switch (name) {
                case "damage" -> damage = conditionals(value, ValueEffect::parse, where, skipped);
                case "damage_protection" -> damageProtection = conditionals(value, ValueEffect::parse, where, skipped);
                case "knockback" -> knockback = conditionals(value, ValueEffect::parse, where, skipped);
                case "armor_effectiveness" -> armorEffectiveness = conditionals(value, ValueEffect::parse, where, skipped);
                case "damage_immunity" -> damageImmunity = conditionals(value, unit -> Boolean.TRUE, where, skipped);
                case "post_attack" -> postAttack = targeted(value, where, skipped);
                case "tick" -> tick = conditionals(value, EntityEffect::parse, where, skipped);
                case "attributes" -> attributes = attributes(value, where);
                case "prevent_armor_change" -> preventArmorChange = true;
                case "prevent_equipment_drop" -> preventEquipmentDrop = true;
                case "item_damage" -> itemDamage = conditionals(value, ValueEffect::parse, where, skipped);
                case "ammo_use" -> ammoUse = conditionals(value, ValueEffect::parse, where, skipped);
                case "projectile_piercing" -> projectilePiercing = conditionals(value, ValueEffect::parse, where, skipped);
                case "projectile_count" -> projectileCount = conditionals(value, ValueEffect::parse, where, skipped);
                case "projectile_spread" -> projectileSpread = conditionals(value, ValueEffect::parse, where, skipped);
                case "trident_return_acceleration" ->
                        tridentReturnAcceleration = conditionals(value, ValueEffect::parse, where, skipped);
                case "smash_damage_per_fallen_block" ->
                        smashDamagePerFallenBlock = conditionals(value, ValueEffect::parse, where, skipped);
                case "projectile_spawned" -> projectileSpawned = conditionals(value, EntityEffect::parse, where, skipped);
                case "crossbow_charge_time" -> crossbowChargeTime = ValueEffect.parse(value);
                default -> notRun.add(component);
            }
        }
        return new EnchantmentEffectSet(Collections.unmodifiableSet(components), damage, damageProtection, knockback,
                armorEffectiveness, damageImmunity, postAttack, tick, attributes, List.copyOf(skipped),
                preventArmorChange, preventEquipmentDrop, List.copyOf(notRun),
                new Projectiles(itemDamage, ammoUse, projectilePiercing, projectileCount, projectileSpread,
                        tridentReturnAcceleration, smashDamagePerFallenBlock, projectileSpawned, crossbowChargeTime));
    }

    private static <T> List<Conditional<T>> conditionals(final JsonElement value,
                                                         final Function<JsonElement, T> parser,
                                                         final String where,
                                                         final List<String> skipped) {
        final List<Conditional<T>> compiled = new ArrayList<>();
        final List<JsonElement> entries = EffectJson.list(value, where);
        for (int index = 0; index < entries.size(); index++) {
            final JsonObject entry = EffectJson.object(entries.get(index), where + "[" + index + "]");
            try {
                compiled.add(new Conditional<>(parser.apply(entry.get("effect")), Condition.parseOptional(entry.get("requirements"))));
            } catch (final UnsupportedEffectException exception) {
                skipped.add(where + "[" + index + "]: " + exception.getMessage());
            } catch (final IllegalArgumentException exception) {
                throw new IllegalArgumentException(where + "[" + index + "]: " + exception.getMessage(), exception);
            }
        }
        return List.copyOf(compiled);
    }

    private static List<Targeted> targeted(final JsonElement value, final String where, final List<String> skipped) {
        final List<Targeted> compiled = new ArrayList<>();
        final List<JsonElement> entries = EffectJson.list(value, where);
        for (int index = 0; index < entries.size(); index++) {
            final JsonObject entry = EffectJson.object(entries.get(index), where + "[" + index + "]");
            try {
                compiled.add(new Targeted(
                        Target.parse(EffectJson.string(entry, "enchanted")),
                        Target.parse(EffectJson.string(entry, "affected")),
                        EntityEffect.parse(entry.get("effect")),
                        Condition.parseOptional(entry.get("requirements"))));
            } catch (final UnsupportedEffectException exception) {
                skipped.add(where + "[" + index + "]: " + exception.getMessage());
            } catch (final IllegalArgumentException exception) {
                throw new IllegalArgumentException(where + "[" + index + "]: " + exception.getMessage(), exception);
            }
        }
        return List.copyOf(compiled);
    }

    private static List<AttributeEffect> attributes(final JsonElement value, final String where) {
        final List<AttributeEffect> compiled = new ArrayList<>();
        final List<JsonElement> entries = EffectJson.list(value, where);
        for (int index = 0; index < entries.size(); index++) {
            final JsonObject entry = EffectJson.object(entries.get(index), where + "[" + index + "]");
            try {
                compiled.add(new AttributeEffect(
                        EffectJson.key(entry, "id"),
                        TypedKey.withRegistry(RegistryKey.ATTRIBUTE, EffectJson.key(entry, "attribute")),
                        LevelValue.parse(entry.get("amount")),
                        operation(EffectJson.string(entry, "operation"))));
            } catch (final IllegalArgumentException exception) {
                throw new IllegalArgumentException(where + "[" + index + "]: " + exception.getMessage(), exception);
            }
        }
        return List.copyOf(compiled);
    }

    private static AttributeModifier.Operation operation(final String name) {
        for (final AttributeModifier.Operation operation : AttributeModifier.Operation.values()) {
            if (operation.serializedName().equals(name)) {
                return operation;
            }
        }
        throw new IllegalArgumentException("Unknown attribute operation " + name);
    }

    /**
     * {@return the components of the {@code effects} object}
     */
    public Set<Key> components() {
        return components;
    }

    /**
     * {@return why effects of supported components are left out, one line per effect}
     */
    public List<String> skipped() {
        return skipped;
    }

    /**
     * {@return the components present that the server does not run}
     */
    public List<Key> notRun() {
        return notRun;
    }

    private static EffectContext context(final EnchantedItem item, final Entity self, final @Nullable DamageSource source) {
        return new EffectContext(item.level(), self, source, item.item(), ThreadLocalRandom.current());
    }

    private static float run(final List<Conditional<ValueEffect>> effects, final EffectContext context, final float value) {
        float result = value;
        for (final Conditional<ValueEffect> effect : effects) {
            if (effect.requirements().test(context)) {
                result = effect.effect().apply(context.level(), context.random(), result);
            }
        }
        return result;
    }

    @Override
    public float modifyDamage(final EnchantedItem item, final LivingEntity victim, final DamageSource source, final float value) {
        return damage.isEmpty() ? value : run(damage, context(item, victim, source), value);
    }

    @Override
    public float modifyProtection(final EnchantedItem item, final DamageSource source, final float protection) {
        return damageProtection.isEmpty() ? protection : run(damageProtection, context(item, item.owner(), source), protection);
    }

    @Override
    public boolean isImmuneTo(final EnchantedItem item, final DamageSource source) {
        if (damageImmunity.isEmpty()) {
            return false;
        }
        final EffectContext context = context(item, item.owner(), source);
        for (final Conditional<Boolean> immunity : damageImmunity) {
            if (immunity.requirements().test(context)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public float modifyKnockback(final EnchantedItem item, final LivingEntity victim, final DamageSource source, final float value) {
        return knockback.isEmpty() ? value : run(knockback, context(item, victim, source), value);
    }

    @Override
    public float modifyArmorEffectiveness(final EnchantedItem item, final LivingEntity victim, final DamageSource source,
                                          final float effectiveness) {
        return armorEffectiveness.isEmpty() ? effectiveness : run(armorEffectiveness, context(item, victim, source), effectiveness);
    }

    @Override
    public void onAttack(final EnchantedItem item, final LivingEntity victim, final DamageSource source, final float dealt) {
        postAttack(Target.ATTACKER, item, victim, source);
    }

    @Override
    public void onHurt(final EnchantedItem item, final @Nullable LivingEntity attacker, final DamageSource source, final float dealt) {
        postAttack(Target.VICTIM, item, item.owner(), source);
    }

    private void postAttack(final Target enchanted, final EnchantedItem item, final LivingEntity victim, final DamageSource source) {
        if (postAttack.isEmpty()) {
            return;
        }
        final EffectContext context = context(item, victim, source);
        for (final Targeted effect : postAttack) {
            if (effect.enchanted() != enchanted || !effect.requirements().test(context)) {
                continue;
            }
            final Entity affected = switch (effect.affected()) {
                case ATTACKER -> source.causingEntity();
                case DAMAGING_ENTITY -> source.directEntity();
                case VICTIM -> victim;
            };
            if (affected != null) {
                effect.effect().apply(context, item, affected);
            }
        }
    }

    @Override
    public void onTick(final EnchantedItem item) {
        if (tick.isEmpty()) {
            return;
        }
        final EffectContext context = context(item, item.owner(), null);
        for (final Conditional<EntityEffect> effect : tick) {
            if (effect.requirements().test(context)) {
                effect.effect().apply(context, item, item.owner());
            }
        }
    }

    @Override
    public List<AttributeModifier> attributeModifiers(final EnchantedItem item) {
        if (attributes.isEmpty()) {
            return List.of();
        }
        final List<AttributeModifier> modifiers = new ArrayList<>(attributes.size());
        for (final AttributeEffect attribute : attributes) {
            modifiers.add(AttributeModifier.of(attribute.attribute(), attribute.id(),
                    attribute.amount().at(item.level()), attribute.operation()));
        }
        return modifiers;
    }

    @Override
    public boolean preventsArmorChange(final EnchantedItem item) {
        return preventArmorChange;
    }

    @Override
    public boolean preventsDrop(final EnchantedItem item) {
        return preventEquipmentDrop;
    }

    /**
     * {@return the durability a stack loses once this enchantment of it changed it, as Unbreaking
     * lowers it}
     *
     * @param item  the enchanted stack, whose durability is at stake
     * @param value the durability it is about to lose
     */
    public float modifyItemDamage(final EnchantedItem item, final float value) {
        return projectiles.itemDamage().isEmpty() ? value : run(projectiles.itemDamage(), context(item, item.owner(), null), value);
    }

    /**
     * {@return how much ammunition a shot uses once this enchantment of the weapon changed it, as
     * Infinity sets it to nothing for arrows}
     *
     * @param item  the enchanted weapon
     * @param ammo  the ammunition about to be used
     * @param value how much of it is about to be used
     */
    public float modifyAmmoUse(final EnchantedItem item, final ItemStack ammo, final float value) {
        if (projectiles.ammoUse().isEmpty()) {
            return value;
        }
        final EffectContext context = new EffectContext(item.level(), item.owner(), null, ammo, ThreadLocalRandom.current());
        return run(projectiles.ammoUse(), context, value);
    }

    /**
     * {@return how many entities a projectile goes through, as Piercing raises it}
     */
    public float modifyProjectilePiercing(final EnchantedItem item, final float value) {
        return projectiles.piercing().isEmpty() ? value : run(projectiles.piercing(), context(item, item.owner(), null), value);
    }

    /**
     * {@return how many projectiles a shot fires, as Multishot raises it}
     */
    public float modifyProjectileCount(final EnchantedItem item, final float value) {
        return projectiles.count().isEmpty() ? value : run(projectiles.count(), context(item, item.owner(), null), value);
    }

    /**
     * {@return the angle, in degrees, the projectiles of a shot spread over, as Multishot raises it}
     */
    public float modifyProjectileSpread(final EnchantedItem item, final float value) {
        return projectiles.spread().isEmpty() ? value : run(projectiles.spread(), context(item, item.owner(), null), value);
    }

    /**
     * {@return how fast a thrown trident flies back to its thrower, as Loyalty raises it}
     */
    public float modifyTridentReturnAcceleration(final EnchantedItem item, final float value) {
        return projectiles.tridentReturnAcceleration().isEmpty()
                ? value
                : run(projectiles.tridentReturnAcceleration(), context(item, item.owner(), null), value);
    }

    /**
     * {@return the damage a smash attack adds per block fallen, as Density raises it}
     */
    public float modifySmashDamagePerFallenBlock(final EnchantedItem item, final LivingEntity victim,
                                                 final DamageSource source, final float value) {
        return projectiles.smashDamagePerFallenBlock().isEmpty()
                ? value
                : run(projectiles.smashDamagePerFallenBlock(), context(item, victim, source), value);
    }

    /**
     * {@return the time a crossbow takes to load, in seconds, as Quick Charge lowers it}
     */
    public float modifyCrossbowChargeTime(final EnchantedItem item, final float value) {
        final ValueEffect effect = projectiles.crossbowChargeTime();
        return effect == null ? value : effect.apply(item.level(), ThreadLocalRandom.current(), value);
    }

    /**
     * Runs the effects this enchantment of a weapon has on the projectiles it shoots, as Flame sets
     * them on fire.
     *
     * @param item       the enchanted weapon
     * @param projectile the projectile just shot
     */
    public void onProjectileSpawned(final EnchantedItem item, final Entity projectile) {
        if (projectiles.spawned().isEmpty()) {
            return;
        }
        final EffectContext context = context(item, projectile, null);
        for (final Conditional<EntityEffect> effect : projectiles.spawned()) {
            if (effect.requirements().test(context)) {
                effect.effect().apply(context, item, projectile);
            }
        }
    }

    /**
     * The effects of an enchantment on durability, ammunition, projectiles and smash attacks.
     */
    record Projectiles(
            List<Conditional<ValueEffect>> itemDamage,
            List<Conditional<ValueEffect>> ammoUse,
            List<Conditional<ValueEffect>> piercing,
            List<Conditional<ValueEffect>> count,
            List<Conditional<ValueEffect>> spread,
            List<Conditional<ValueEffect>> tridentReturnAcceleration,
            List<Conditional<ValueEffect>> smashDamagePerFallenBlock,
            List<Conditional<EntityEffect>> spawned,
            @Nullable ValueEffect crossbowChargeTime
    ) {

        static final Projectiles NONE = new Projectiles(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), null);
    }

    /**
     * An effect and the requirements it runs under.
     */
    public record Conditional<T>(T effect, Condition requirements) {
    }

    /**
     * A {@code post_attack} effect: which side of the hit carries the enchantment, which side it is
     * applied to.
     */
    public record Targeted(Target enchanted, Target affected, EntityEffect effect, Condition requirements) {
    }

    /**
     * A {@code minecraft:attributes} entry.
     */
    public record AttributeEffect(Key id, TypedKey<Attribute> attribute, LevelValue amount, AttributeModifier.Operation operation) {
    }

    /**
     * A side of a hit, as {@code post_attack} names it.
     */
    public enum Target {
        ATTACKER,
        DAMAGING_ENTITY,
        VICTIM;

        static Target parse(final String name) {
            return switch (name) {
                case "attacker" -> ATTACKER;
                case "damaging_entity" -> DAMAGING_ENTITY;
                case "victim" -> VICTIM;
                default -> throw new IllegalArgumentException("Unknown enchantment target " + name);
            };
        }
    }
}
