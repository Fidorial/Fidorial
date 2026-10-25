package fr.euphyllia.fidorial.server.item.enchantment.effect;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMovingMob;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.player.Player;
import fr.fidorial.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A requirement of an enchantment effect.
 *
 * <p>Supports {@code all_of}, {@code any_of}, {@code inverted}, {@code random_chance},
 * {@code entity_properties}, {@code damage_source_properties} and {@code weather_check}. Entity
 * predicates may test the type and the {@code is_on_fire}, {@code is_on_ground}, {@code is_sneaking} and
 * {@code is_sprinting} flags. Other conditions throw an {@link UnsupportedEffectException}.</p>
 */
@FunctionalInterface
public interface Condition {

    /**
     * A condition that always holds, for effects without requirements.
     */
    Condition ALWAYS = context -> true;

    Key ENTITY_TYPE = RegistryKey.ENTITY_TYPE.key();

    Key DAMAGE_TYPE = RegistryKey.DAMAGE_TYPE.key();

    boolean test(EffectContext context);

    static Condition parseOptional(final @Nullable JsonElement element) {
        return element == null || element.isJsonNull() ? ALWAYS : parse(element);
    }

    static Condition parse(final JsonElement element) {
        final JsonObject object = EffectJson.object(element, "A condition");
        final String type = LevelValue.vanillaType(object);
        return switch (type) {
            case "all_of" -> {
                final List<Condition> terms = terms(object);
                yield context -> {
                    for (final Condition term : terms) {
                        if (!term.test(context)) {
                            return false;
                        }
                    }
                    return true;
                };
            }
            case "any_of" -> {
                final List<Condition> terms = terms(object);
                yield context -> {
                    for (final Condition term : terms) {
                        if (term.test(context)) {
                            return true;
                        }
                    }
                    return false;
                };
            }
            case "inverted" -> {
                final Condition term = parse(object.get("term"));
                yield context -> !term.test(context);
            }
            case "random_chance" -> {
                final FloatSource chance = FloatSource.parse(object.get("chance"));
                yield context -> context.random().nextFloat() < chance.sample(context.level(), context.random());
            }
            case "entity_properties" -> {
                final Function<EffectContext, @Nullable Entity> target = entityTarget(EffectJson.string(object, "entity"));
                final JsonObject predicateJson = EffectJson.optionalObject(object, "predicate");
                final Predicate<Entity> predicate = predicateJson == null ? entity -> true : entityPredicate(predicateJson);
                yield context -> {
                    final Entity entity = target.apply(context);
                    return entity != null && predicate.test(entity);
                };
            }
            case "damage_source_properties" -> {
                final Predicate<DamageSource> predicate = damageSourcePredicate(EffectJson.object(object, "predicate"));
                yield context -> context.source() != null && predicate.test(context.source());
            }
            case "weather_check" -> {
                final Boolean raining = EffectJson.optionalBoolean(object, "raining");
                final Boolean thundering = EffectJson.optionalBoolean(object, "thundering");
                yield context -> {
                    if (!(context.self().world() instanceof final ServerWorld world)) {
                        return false;
                    }
                    return (raining == null || raining == world.weather().isRaining())
                            && (thundering == null || thundering == world.weather().isThundering());
                };
            }
            case "random_chance_with_enchanted_bonus", "killed_by_player", "entity_scores", "match_block",
                 "match_tool", "table_bonus", "survives_explosion", "location_check", "time_check",
                 "int_value_check", "float_value_check", "enchantment_active_check", "environment_attribute_check" ->
                    throw new UnsupportedEffectException("the condition minecraft:" + type + " is not supported");
            default -> throw new IllegalArgumentException("Unknown condition type " + object.get("type"));
        };
    }

    private static List<Condition> terms(final JsonObject object) {
        final List<Condition> terms = new ArrayList<>();
        for (final JsonElement term : EffectJson.list(object, "terms")) {
            terms.add(parse(term));
        }
        return List.copyOf(terms);
    }

    private static Function<EffectContext, @Nullable Entity> entityTarget(final String name) {
        return switch (name) {
            case "this" -> EffectContext::self;
            case "attacker" -> EffectContext::attacker;
            case "direct_attacker" -> EffectContext::directAttacker;
            case "attacking_player" -> context -> context.attacker() instanceof final Player player ? player : null;
            case "target_entity", "interacting_entity" ->
                    throw new UnsupportedEffectException("the entity target " + name + " does not exist for enchantments");
            default -> throw new IllegalArgumentException("Unknown entity target " + name);
        };
    }

    /**
     * Reads an entity predicate: an object whose keys name sub-predicates, such as
     * {@code minecraft:entity_type} or {@code minecraft:flags}.
     */
    static Predicate<Entity> entityPredicate(final JsonObject object) {
        final List<Predicate<Entity>> parts = new ArrayList<>();
        for (final Map.Entry<String, JsonElement> entry : object.entrySet()) {
            final Key part = EffectJson.key(entry.getKey());
            if (!Key.MINECRAFT_NAMESPACE.equals(part.namespace())) {
                throw new IllegalArgumentException("Unknown entity predicate " + part.asString());
            }
            switch (part.value()) {
                case "entity_type" -> {
                    final KeyMatcher types = KeyMatcher.parse(ENTITY_TYPE, entry.getValue());
                    parts.add(entity -> types.test(entity.type().key()));
                }
                case "flags" -> parts.add(flags(EffectJson.object(entry.getValue(), "'flags'")));
                default -> throw new UnsupportedEffectException(
                        "the entity predicate minecraft:" + part.value() + " is not supported");
            }
        }
        final List<Predicate<Entity>> all = List.copyOf(parts);
        return entity -> {
            for (final Predicate<Entity> predicate : all) {
                if (!predicate.test(entity)) {
                    return false;
                }
            }
            return true;
        };
    }

    private static Predicate<Entity> flags(final JsonObject object) {
        final List<Predicate<Entity>> flags = new ArrayList<>();
        for (final String flag : object.keySet()) {
            final Boolean expected = EffectJson.optionalBoolean(object, flag);
            if (expected == null) {
                continue;
            }
            final Predicate<Entity> test = switch (flag) {
                case "is_on_fire" -> entity -> entity instanceof final LivingEntity living && living.fireTicks() > 0;
                case "is_sneaking" -> entity -> entity instanceof final ServerPlayer player && player.isSneaking();
                case "is_sprinting" -> entity -> entity instanceof final ServerPlayer player && player.isSprinting();
                case "is_on_ground" -> entity -> switch (entity) {
                    case final ServerPlayer player -> player.onGround();
                    case final AbstractMovingMob mob -> mob.onGround();
                    default -> true;
                };
                case "is_swimming", "is_flying", "is_baby", "is_in_water", "is_fall_flying" ->
                        throw new UnsupportedEffectException("the entity flag " + flag + " is not supported");
                default -> throw new IllegalArgumentException("Unknown entity flag " + flag);
            };
            flags.add(entity -> test.test(entity) == expected);
        }
        final List<Predicate<Entity>> all = List.copyOf(flags);
        return entity -> {
            for (final Predicate<Entity> flag : all) {
                if (!flag.test(entity)) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * Reads a damage source predicate: its damage type {@code tags}, {@code is_direct}, and the
     * {@code source_entity} and {@code direct_entity} predicates.
     */
    static Predicate<DamageSource> damageSourcePredicate(final JsonObject object) {
        final List<Predicate<DamageSource>> parts = new ArrayList<>();
        for (final Map.Entry<String, JsonElement> entry : object.entrySet()) {
            switch (entry.getKey()) {
                case "tags" -> {
                    for (final JsonElement tag : EffectJson.list(entry.getValue(), "'tags'")) {
                        final JsonObject tagObject = EffectJson.object(tag, "A damage type tag");
                        final KeyMatcher types = KeyMatcher.parse(DAMAGE_TYPE, tagObject.get("id"));
                        final boolean expected = EffectJson.bool(tagObject, "expected");
                        parts.add(source -> types.test(source.type().key()) == expected);
                    }
                }
                case "is_direct" -> {
                    final Boolean expected = EffectJson.optionalBoolean(object, "is_direct");
                    if (expected != null) {
                        parts.add(source -> (source.causingEntity() == source.directEntity()) == expected);
                    }
                }
                case "source_entity" -> {
                    final Predicate<Entity> predicate = entityPredicate(EffectJson.object(entry.getValue(), "'source_entity'"));
                    parts.add(source -> source.causingEntity() != null && predicate.test(source.causingEntity()));
                }
                case "direct_entity" -> {
                    final Predicate<Entity> predicate = entityPredicate(EffectJson.object(entry.getValue(), "'direct_entity'"));
                    parts.add(source -> source.directEntity() != null && predicate.test(source.directEntity()));
                }
                default -> throw new IllegalArgumentException("Unknown damage source predicate " + entry.getKey());
            }
        }
        final List<Predicate<DamageSource>> all = List.copyOf(parts);
        return source -> {
            for (final Predicate<DamageSource> predicate : all) {
                if (!predicate.test(source)) {
                    return false;
                }
            }
            return true;
        };
    }
}
