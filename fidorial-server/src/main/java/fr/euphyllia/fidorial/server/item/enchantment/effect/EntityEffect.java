package fr.euphyllia.fidorial.server.item.enchantment.effect;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.entity.effect.MobEffectRegistry;
import fr.fidorial.item.enchantment.EnchantedItem;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.DamageType;
import fr.fidorial.registry.data.MobEffect;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

import java.util.ArrayList;
import java.util.List;

/**
 * An entity effect of an enchantment.
 *
 * <p>Supports {@code all_of}, {@code ignite}, {@code damage_entity}, {@code apply_mob_effect} and
 * {@code play_sound}. Other types throw an {@link UnsupportedEffectException}, and are skipped inside
 * {@code all_of}.</p>
 */
@FunctionalInterface
public interface EntityEffect {

    Key MOB_EFFECT = RegistryKey.MOB_EFFECT.key();

    /**
     * Runs the effect.
     *
     * @param context  the level and the random source
     * @param item     the enchanted stack and its owner
     * @param affected the entity the effect is applied to
     */
    void apply(EffectContext context, EnchantedItem item, Entity affected);

    static EntityEffect parse(final JsonElement element) {
        final JsonObject object = EffectJson.object(element, "An entity effect");
        final String type = LevelValue.vanillaType(object);
        return switch (type) {
            case "all_of" -> allOf(object);
            case "ignite" -> ignite(LevelValue.parse(object.get("duration")));
            case "damage_entity" -> damageEntity(
                    LevelValue.parse(object.get("min_damage")),
                    LevelValue.parse(object.get("max_damage")),
                    TypedKey.create(RegistryKey.DAMAGE_TYPE, EffectJson.key(object, "damage_type")));
            case "apply_mob_effect" -> applyMobEffect(
                    KeyMatcher.parse(MOB_EFFECT, object.get("to_apply")),
                    LevelValue.parse(object.get("min_duration")),
                    LevelValue.parse(object.get("max_duration")),
                    LevelValue.parse(object.get("min_amplifier")),
                    LevelValue.parse(object.get("max_amplifier")));
            case "play_sound" -> playSound(sounds(object.get("sound")),
                    FloatSource.parse(object.get("volume")), FloatSource.parse(object.get("pitch")));
            case "apply_exhaustion", "apply_impulse", "change_item_damage", "explode", "replace_block",
                 "replace_disk", "run_function", "set_block_properties", "spawn_particles", "summon_entity" ->
                    throw new UnsupportedEffectException("the entity effect minecraft:" + type + " is not supported");
            default -> throw new IllegalArgumentException("Unknown entity effect type " + object.get("type"));
        };
    }

    private static EntityEffect allOf(final JsonObject object) {
        final List<EntityEffect> effects = new ArrayList<>();
        UnsupportedEffectException skipped = null;
        for (final JsonElement effect : EffectJson.list(object, "effects")) {
            try {
                effects.add(parse(effect));
            } catch (final UnsupportedEffectException exception) {
                skipped = exception;
            }
        }
        if (effects.isEmpty() && skipped != null) {
            throw skipped;
        }
        final List<EntityEffect> all = List.copyOf(effects);
        return (context, item, affected) -> {
            for (final EntityEffect effect : all) {
                effect.apply(context, item, affected);
            }
        };
    }

    private static EntityEffect ignite(final LevelValue duration) {
        return (context, item, affected) -> {
            if (affected instanceof final AbstractLivingEntity living) {
                living.igniteForTicks((int) Math.floor(duration.at(context.level()) * 20.0f));
            }
        };
    }

    private static EntityEffect damageEntity(final LevelValue min, final LevelValue max, final TypedKey<DamageType> type) {
        return (context, item, affected) -> {
            if (!(affected instanceof final LivingEntity living)) {
                return;
            }
            final float damage = FloatSource.between(context.random(), min.at(context.level()), max.at(context.level()));
            FidorialServer.getInstance().entities().combat().damage(living, DamageSource.of(type, item.owner()), damage);
        };
    }

    private static EntityEffect applyMobEffect(final KeyMatcher effects, final LevelValue minDuration,
                                               final LevelValue maxDuration, final LevelValue minAmplifier,
                                               final LevelValue maxAmplifier) {
        return (context, item, affected) -> {
            if (!(affected instanceof final LivingEntity living)) {
                return;
            }
            final List<Key> candidates = effects.resolve();
            if (candidates.isEmpty()) {
                return;
            }
            final MobEffectRegistry registry = FidorialServer.getInstance().registries().effects();
            final TypedKey<MobEffect> chosen = TypedKey.create(RegistryKey.MOB_EFFECT,
                    candidates.get(context.random().nextInt(candidates.size())));
            if (!registry.isEffect(chosen)) {
                return;
            }
            final int level = context.level();
            final int ticks = Math.round(FloatSource.between(context.random(),
                    minDuration.at(level), maxDuration.at(level)) * 20.0f);
            final int amplifier = Math.clamp(Math.round(FloatSource.between(context.random(),
                    minAmplifier.at(level), maxAmplifier.at(level))), 0, MobEffectInstance.MAX_AMPLIFIER);
            if (ticks > 0) {
                living.addEffect(registry.instance(chosen, ticks, amplifier));
            }
        };
    }

    private static List<Key> sounds(final JsonElement element) {
        final List<Key> sounds = new ArrayList<>();
        if (element != null && element.isJsonArray()) {
            for (final JsonElement sound : EffectJson.list(element, "'sound'")) {
                sounds.add(sound(sound));
            }
        } else {
            sounds.add(sound(element));
        }
        if (sounds.isEmpty()) {
            throw new IllegalArgumentException("A play_sound effect needs at least one sound");
        }
        return List.copyOf(sounds);
    }

    private static Key sound(final JsonElement element) {
        if (element != null && element.isJsonObject()) {
            return EffectJson.key(element.getAsJsonObject(), "sound_id");
        }
        if (element == null || !element.isJsonPrimitive()) {
            throw new IllegalArgumentException("A sound must be an id or a sound event, got " + element);
        }
        return EffectJson.key(element.getAsString());
    }

    private static EntityEffect playSound(final List<Key> sounds, final FloatSource volume, final FloatSource pitch) {
        return (context, item, affected) -> {
            final Key sound = sounds.get(Math.clamp(context.level() - 1, 0, sounds.size() - 1));
            final Sound.Source source = affected instanceof ServerPlayer ? Sound.Source.PLAYER : Sound.Source.NEUTRAL;
            final Location at = affected.location();
            final ClientboundSoundPacket packet = new ClientboundSoundPacket(Sound.sound(sound, source,
                    volume.sample(context.level(), context.random()), pitch.sample(context.level(), context.random())),
                    at.x(), at.y(), at.z());
            if (affected instanceof final AbstractEntity entity) {
                entity.sendToTrackers(packet);
            }
            if (affected instanceof final ServerPlayer player) {
                player.connection().send(packet);
            }
        };
    }
}
