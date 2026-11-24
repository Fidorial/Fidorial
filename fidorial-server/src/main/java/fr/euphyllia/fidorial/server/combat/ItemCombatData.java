package fr.euphyllia.fidorial.server.combat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.euphyllia.fidorial.server.item.enchantment.effect.EffectJson;
import fr.fidorial.attribute.AttributeModifier;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.DamageType;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.data.MobEffect;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The default combat components of every item, read from the {@code item_combat.json} dataset
 * written by the {@code generateItemCombat} task.
 *
 * <p>An item the dataset does not know fights bare-handed: no modifier, no shield, no protection
 * from death.</p>
 */
public final class ItemCombatData {

    public static final String RESOURCE = "/fidorial-data/item_combat.json";

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ItemCombatData.class);

    private static final String ATTRIBUTE_MODIFIERS = "minecraft:attribute_modifiers";
    private static final String WEAPON = "minecraft:weapon";
    private static final String BLOCKS_ATTACKS = "minecraft:blocks_attacks";
    private static final String DEATH_PROTECTION = "minecraft:death_protection";
    private static final String DAMAGE_TYPE = "minecraft:damage_type";
    private static final String ATTACK_RANGE = "minecraft:attack_range";
    private static final String MINIMUM_ATTACK_CHARGE = "minecraft:minimum_attack_charge";
    private static final String USE_COOLDOWN = "minecraft:use_cooldown";
    private static final String PIERCING_WEAPON = "minecraft:piercing_weapon";

    private static final Combat NONE = new Combat(List.of(), null, null, null, null, null, 0f, null, null);

    private static volatile Map<TypedKey<Item>, Combat> items = Map.of();

    private ItemCombatData() {
        throw new UnsupportedOperationException("ItemCombatData cannot be instantiated.");
    }

    /**
     * Reads the bundled dataset. Without it, every item fights like a bare hand.
     */
    public static void bootstrap() {
        try (final InputStream input = ItemCombatData.class.getResourceAsStream(RESOURCE)) {
            if (input == null) {
                LOGGER.warn("Missing resource {}: every item will fight like a bare hand. "
                        + "Run ':fidorial-server:generateItemCombat'.", RESOURCE);
                return;
            }
            items = load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (final IOException exception) {
            throw new UncheckedIOException("Failed to load " + RESOURCE, exception);
        }
    }

    static Map<TypedKey<Item>, Combat> load(final Reader reader) {
        final Map<TypedKey<Item>, Combat> loaded = new HashMap<>();
        final JsonObject entries = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("items");
        if (entries != null) {
            for (final Map.Entry<String, JsonElement> entry : entries.entrySet()) {
                try {
                    loaded.put(TypedKey.withRegistry(RegistryKey.ITEM, entry.getKey()), parse(entry.getValue().getAsJsonObject()));
                } catch (final RuntimeException exception) {
                    LOGGER.warn("Skipping the combat data of {}: {}", entry.getKey(), exception.getMessage());
                }
            }
        }
        LOGGER.debug("Read the combat data of {} items", loaded.size());
        return Map.copyOf(loaded);
    }

    /**
     * {@return the combat components of a stack}
     *
     * @param stack the stack, {@link ItemStack#EMPTY} for a bare hand
     */
    public static Combat of(final ItemStack stack) {
        return stack.isEmpty() ? NONE : items.getOrDefault(stack.id(), NONE);
    }

    /**
     * {@return the combat components of an item}
     *
     * @param item the item
     */
    public static Combat of(final TypedKey<Item> item) {
        return items.getOrDefault(item, NONE);
    }

    private static Combat parse(final JsonObject components) {
        return new Combat(
                modifiers(components.get(ATTRIBUTE_MODIFIERS)),
                components.get(WEAPON) instanceof final JsonObject weapon ? weapon(weapon) : null,
                components.get(BLOCKS_ATTACKS) instanceof final JsonObject blocks ? blocksAttacks(blocks) : null,
                components.get(DEATH_PROTECTION) instanceof final JsonObject protection ? deathProtection(protection) : null,
                components.has(DAMAGE_TYPE)
                        ? TypedKey.withRegistry(RegistryKey.DAMAGE_TYPE, components.get(DAMAGE_TYPE).getAsString())
                        : null,
                components.get(ATTACK_RANGE) instanceof final JsonObject range ? attackRange(range) : null,
                components.has(MINIMUM_ATTACK_CHARGE) ? components.get(MINIMUM_ATTACK_CHARGE).getAsFloat() : 0f,
                components.get(USE_COOLDOWN) instanceof final JsonObject cooldown ? useCooldown(cooldown) : null,
                components.get(PIERCING_WEAPON) instanceof final JsonObject piercing ? piercingWeapon(piercing) : null);
    }

    private static List<AttributeModifier> modifiers(final @Nullable JsonElement element) {
        if (!(element instanceof final JsonArray array)) {
            return List.of();
        }
        final List<AttributeModifier> modifiers = new ArrayList<>(array.size());
        for (final JsonElement entry : array) {
            final JsonObject modifier = entry.getAsJsonObject();
            modifiers.add(AttributeModifier.of(
                    TypedKey.withRegistry(RegistryKey.ATTRIBUTE, modifier.get("type").getAsString()),
                    Key.key(modifier.get("id").getAsString()),
                    modifier.get("amount").getAsDouble(),
                    AttributeModifier.Operation.byName(modifier.get("operation").getAsString()),
                    EquipmentSlotGroup.byName(string(modifier, "slot", "any"))));
        }
        return List.copyOf(modifiers);
    }

    private static Weapon weapon(final JsonObject weapon) {
        return new Weapon(
                integer(weapon, "item_damage_per_attack", 1),
                decimal(weapon, "disable_blocking_for_seconds", 0f));
    }

    private static BlocksAttacks blocksAttacks(final JsonObject blocks) {
        final List<DamageReduction> reductions = new ArrayList<>();
        if (blocks.get("damage_reductions") instanceof final JsonArray array) {
            for (final JsonElement entry : array) {
                final JsonObject reduction = entry.getAsJsonObject();
                reductions.add(new DamageReduction(
                        decimal(reduction, "horizontal_blocking_angle", 90f),
                        typeFilter(reduction.get("type")),
                        decimal(reduction, "base", 0f),
                        decimal(reduction, "factor", 1f)));
            }
        } else {
            reductions.add(DamageReduction.ALL);
        }
        final ItemDamage itemDamage = blocks.get("item_damage") instanceof final JsonObject wear
                ? new ItemDamage(decimal(wear, "threshold", 0f), decimal(wear, "base", 0f), decimal(wear, "factor", 1f))
                : ItemDamage.DEFAULT;
        final String bypassedBy = string(blocks, "bypassed_by", null);
        return new BlocksAttacks(
                decimal(blocks, "block_delay_seconds", 0f),
                decimal(blocks, "disable_cooldown_scale", 1f),
                List.copyOf(reductions),
                itemDamage,
                bypassedBy == null ? null : TagKey.withRegistry(RegistryKey.DAMAGE_TYPE, Key.key(bypassedBy.substring(1))),
                key(blocks, "block_sound"),
                key(blocks, "disabled_sound"));
    }

    private static @Nullable List<TypeFilter> typeFilter(final @Nullable JsonElement element) {
        if (element == null) {
            return null;
        }
        final List<TypeFilter> filters = new ArrayList<>();
        final List<JsonElement> values = new ArrayList<>();
        if (element instanceof final JsonArray array) {
            array.forEach(values::add);
        } else {
            values.add(element);
        }
        for (final JsonElement value : values) {
            final String name = value.getAsString();
            filters.add(name.startsWith("#")
                    ? new TypeFilter(null, TagKey.withRegistry(RegistryKey.DAMAGE_TYPE, Key.key(name.substring(1))))
                    : new TypeFilter(TypedKey.withRegistry(RegistryKey.DAMAGE_TYPE, name), null));
        }
        return List.copyOf(filters);
    }

    private static DeathProtection deathProtection(final JsonObject protection) {
        final List<DeathEffect> effects = new ArrayList<>();
        if (protection.get("death_effects") instanceof final JsonArray array) {
            for (final JsonElement entry : array) {
                final JsonObject effect = entry.getAsJsonObject();
                final String type = Key.key(effect.get("type").getAsString()).value();
                switch (type) {
                    case "clear_all_effects" -> effects.add(new DeathEffect.ClearAll());
                    case "apply_effects" -> effects.add(new DeathEffect.Apply(appliedEffects(effect),
                            decimal(effect, "probability", 1f)));
                    case "remove_effects" -> effects.add(new DeathEffect.Remove(removedEffects(effect.get("effects"))));
                    case "play_sound" -> {
                        final JsonElement sound = effect.get("sound");
                        final String id = sound.isJsonObject() ? sound.getAsJsonObject().get("sound_id").getAsString() : sound.getAsString();
                        effects.add(new DeathEffect.PlaySound(Key.key(id)));
                    }
                    default -> LOGGER.debug("Death effect {} is not run by the server", type);
                }
            }
        }
        return new DeathProtection(List.copyOf(effects));
    }

    private static List<AppliedEffect> appliedEffects(final JsonObject effect) {
        final List<AppliedEffect> applied = new ArrayList<>();
        if (effect.get("effects") instanceof final JsonArray array) {
            for (final JsonElement entry : array) {
                final JsonObject instance = entry.getAsJsonObject();
                applied.add(new AppliedEffect(
                        TypedKey.withRegistry(RegistryKey.MOB_EFFECT, instance.get("id").getAsString()),
                        integer(instance, "duration", 1),
                        integer(instance, "amplifier", 0),
                        bool(instance, "ambient", false),
                        bool(instance, "show_particles", true),
                        bool(instance, "show_icon", true)));
            }
        }
        return List.copyOf(applied);
    }

    private static List<TypedKey<MobEffect>> removedEffects(final @Nullable JsonElement element) {
        final List<TypedKey<MobEffect>> removed = new ArrayList<>();
        if (element instanceof final JsonArray array) {
            for (final JsonElement entry : array) {
                removed.add(TypedKey.withRegistry(RegistryKey.MOB_EFFECT, entry.getAsString()));
            }
        } else if (element != null && element.isJsonPrimitive() && !element.getAsString().startsWith("#")) {
            removed.add(TypedKey.withRegistry(RegistryKey.MOB_EFFECT, element.getAsString()));
        }
        return List.copyOf(removed);
    }

    private static AttackRange attackRange(final JsonObject range) {
        return new AttackRange(
                decimal(range, "min_reach", 0f),
                decimal(range, "max_reach", 3f),
                decimal(range, "min_creative_reach", 0f),
                decimal(range, "max_creative_reach", 5f),
                decimal(range, "hitbox_margin", 0.3f),
                decimal(range, "mob_factor", 1f));
    }

    private static PiercingWeapon piercingWeapon(final JsonObject piercing) {
        return new PiercingWeapon(bool(piercing, "deals_knockback", true), key(piercing, "sound"), key(piercing, "hit_sound"));
    }

    private static UseCooldown useCooldown(final JsonObject cooldown) {
        return new UseCooldown(decimal(cooldown, "seconds", 0f), key(cooldown, "cooldown_group"));
    }

    private static float decimal(final JsonObject object, final String name, final float fallback) {
        return EffectJson.number(object, name, fallback);
    }

    private static int integer(final JsonObject object, final String name, final int fallback) {
        return object.has(name) ? EffectJson.integer(object, name) : fallback;
    }

    private static boolean bool(final JsonObject object, final String name, final boolean fallback) {
        final Boolean value = EffectJson.optionalBoolean(object, name);
        return value == null ? fallback : value;
    }

    private static @Nullable String string(final JsonObject object, final String name, final @Nullable String fallback) {
        final JsonElement value = object.get(name);
        return value == null || !value.isJsonPrimitive() ? fallback : value.getAsString();
    }

    private static @Nullable Key key(final JsonObject object, final String name) {
        final JsonElement value = object.get(name);
        if (value == null) {
            return null;
        }
        if (value.isJsonObject()) {
            final JsonElement id = value.getAsJsonObject().get("sound_id");
            return id == null ? null : Key.key(id.getAsString());
        }
        return Key.key(value.getAsString());
    }

    /**
     * The combat components of an item.
     *
     * @param modifiers           the attribute modifiers it gives while worn or held in their slots
     * @param weapon              how it wears and disables shields when hitting, {@code null} for none
     * @param blocksAttacks       how it blocks hits while raised, {@code null} if it cannot
     * @param deathProtection     how it saves its holder from death, {@code null} if it does not
     * @param damageType          the damage type of its melee hits, {@code null} for the default
     * @param attackRange         the reach of its melee hits, {@code null} for the default
     * @param minimumAttackCharge the charge a melee hit needs to land, {@code 0} for any
     * @param useCooldown         the cooldown after using it, {@code null} for none
     * @param piercingWeapon      how it thrusts through the entities in front of its wielder,
     *                            {@code null} if it cannot
     */
    public record Combat(
            List<AttributeModifier> modifiers,
            @Nullable Weapon weapon,
            @Nullable BlocksAttacks blocksAttacks,
            @Nullable DeathProtection deathProtection,
            @Nullable TypedKey<DamageType> damageType,
            @Nullable AttackRange attackRange,
            float minimumAttackCharge,
            @Nullable UseCooldown useCooldown,
            @Nullable PiercingWeapon piercingWeapon
    ) {
    }

    /**
     * The {@code minecraft:piercing_weapon} component: a thrust that hits every entity in front of
     * the wielder, within the reach of its {@code minecraft:attack_range}.
     *
     * @param dealsKnockback whether the thrust pushes what it hits
     * @param sound          the sound of the thrust, {@code null} for none
     * @param hitSound       the sound of each entity hit, {@code null} for none
     */
    public record PiercingWeapon(boolean dealsKnockback, @Nullable Key sound, @Nullable Key hitSound) {
    }

    /**
     * The {@code minecraft:weapon} component.
     *
     * @param itemDamagePerAttack       the durability a hit costs
     * @param disableBlockingForSeconds how long a hit keeps a raised shield down, {@code 0} for not at all
     */
    public record Weapon(int itemDamagePerAttack, float disableBlockingForSeconds) {
    }

    /**
     * The {@code minecraft:blocks_attacks} component.
     *
     * @param blockDelaySeconds    how long the item must be raised before it blocks
     * @param disableCooldownScale scales how long a disabling hit keeps it down
     * @param damageReductions     how much of each hit it takes away
     * @param itemDamage           the durability a blocked hit costs
     * @param bypassedBy           the damage types it never blocks, {@code null} for none
     * @param blockSound           the sound of a blocked hit, {@code null} for none
     * @param disabledSound        the sound of the item being disabled, {@code null} for none
     */
    public record BlocksAttacks(
            float blockDelaySeconds,
            float disableCooldownScale,
            List<DamageReduction> damageReductions,
            ItemDamage itemDamage,
            @Nullable TagKey<DamageType> bypassedBy,
            @Nullable Key blockSound,
            @Nullable Key disabledSound
    ) {
    }

    /**
     * A damage reduction of {@code minecraft:blocks_attacks}: a hit within {@code horizontalBlockingAngle}
     * degrees of where the holder looks, of one of the {@code types}, loses
     * {@code base + factor × damage}, between nothing and all of it.
     *
     * @param horizontalBlockingAngle the widest angle blocked, in degrees
     * @param types                   the damage types reduced, {@code null} for all
     * @param base                    the damage blocked whatever the hit
     * @param factor                  the share of the damage blocked
     */
    public record DamageReduction(float horizontalBlockingAngle, @Nullable List<TypeFilter> types, float base, float factor) {

        static final DamageReduction ALL = new DamageReduction(90f, null, 0f, 1f);
    }

    /**
     * A damage type, or a damage type tag, a reduction applies to.
     */
    public record TypeFilter(@Nullable TypedKey<DamageType> type, @Nullable TagKey<DamageType> tag) {
    }

    /**
     * How much durability a blocked hit costs: nothing under {@code threshold} damage, otherwise
     * {@code base + factor × damage}, rounded down.
     */
    public record ItemDamage(float threshold, float base, float factor) {

        static final ItemDamage DEFAULT = new ItemDamage(0f, 0f, 1f);
    }

    /**
     * The {@code minecraft:death_protection} component.
     *
     * @param deathEffects what happens to the holder once saved
     */
    public record DeathProtection(List<DeathEffect> deathEffects) {
    }

    /**
     * An effect applied by {@code minecraft:death_protection} or another consume effect.
     */
    public sealed interface DeathEffect {

        /**
         * Removes every effect.
         */
        record ClearAll() implements DeathEffect {
        }

        /**
         * Applies effects, with a probability.
         */
        record Apply(List<AppliedEffect> effects, float probability) implements DeathEffect {
        }

        /**
         * Removes some effects.
         */
        record Remove(List<TypedKey<MobEffect>> effects) implements DeathEffect {
        }

        /**
         * Plays a sound.
         */
        record PlaySound(Key sound) implements DeathEffect {
        }
    }

    /**
     * An effect instance to apply.
     */
    public record AppliedEffect(TypedKey<MobEffect> type, int duration, int amplifier, boolean ambient,
                                boolean showParticles, boolean showIcon) {
    }

    /**
     * The {@code minecraft:attack_range} component.
     */
    public record AttackRange(float minReach, float maxReach, float minCreativeReach, float maxCreativeReach,
                              float hitboxMargin, float mobFactor) {
    }

    /**
     * The {@code minecraft:use_cooldown} component.
     *
     * @param seconds the cooldown
     * @param group   the cooldown group, {@code null} for the item itself
     */
    public record UseCooldown(float seconds, @Nullable Key group) {
    }
}
