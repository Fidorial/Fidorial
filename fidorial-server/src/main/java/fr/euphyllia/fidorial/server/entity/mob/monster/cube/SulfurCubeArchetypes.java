package fr.euphyllia.fidorial.server.entity.mob.monster.cube;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.mob.MobAttribute;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.DamageType;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.data.SulfurCubeArchetype;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * What the block swallowed by a sulfur cube makes of it, as the entries of the
 * {@code minecraft:sulfur_cube_archetype} registry describe it: each entry names the items it
 * covers, the attributes it changes, whether the cube floats, how hard a hit sends it, the sounds it
 * makes, the damage it deals by touch and whether it explodes.
 */
final class SulfurCubeArchetypes {

    /**
     * How hard a hit sends a sulfur cube whose block no entry covers.
     */
    static final Knockback DEFAULT_KNOCKBACK = new Knockback(0.33f, 0.06f);
    /**
     * The sounds of a sulfur cube whose block no entry covers.
     */
    static final Sounds DEFAULT_SOUNDS = new Sounds(sound("entity.sulfur_cube.regular.hit"),
            sound("entity.sulfur_cube.regular.push"), 0.2f, 0.5f);

    private SulfurCubeArchetypes() {
    }

    /**
     * {@return the entries covering an item}, in the order of the registry; none for an empty stack
     */
    static List<Archetype> matching(final ItemStack stack) {
        if (stack.isEmpty()) {
            return List.of();
        }
        final FidorialServer server = FidorialServer.getInstance();
        final ServerRegistry<SulfurCubeArchetype> registry = server.registries().registry(RegistryKey.SULFUR_CUBE_ARCHETYPE);
        final ServerRegistry<Item> items = server.registries().registry(RegistryKey.ITEM);
        final List<Archetype> found = new ArrayList<>();
        for (final TypedKey<SulfurCubeArchetype> key : registry.keys()) {
            final JsonElement data = registry.data(key);
            if (data != null && data.isJsonObject() && covers(items, data.getAsJsonObject().get("items"), stack.id())) {
                found.add(read(key.key(), data.getAsJsonObject()));
            }
        }
        return found;
    }

    /**
     * {@return {@code true} if a set of items holds one}: a tag ({@code #...}), an item or a list of
     * items
     */
    private static boolean covers(final ServerRegistry<Item> items, final @Nullable JsonElement set, final TypedKey<Item> item) {
        if (set == null) {
            return false;
        }
        if (set.isJsonArray()) {
            for (final JsonElement member : set.getAsJsonArray()) {
                if (covers(items, member, item)) {
                    return true;
                }
            }
            return false;
        }
        if (!set.isJsonPrimitive()) {
            return false;
        }
        final String name = set.getAsString();
        if (name.startsWith("#")) {
            final String tag = name.substring(1);
            return Key.parseable(tag) && items.isTagged(item, TagKey.withRegistry(RegistryKey.ITEM, Key.key(tag)));
        }
        return Key.parseable(name) && item.key().equals(Key.key(name));
    }

    private static Archetype read(final Key id, final JsonObject data) {
        final List<Modifier> modifiers = new ArrayList<>();
        if (data.get("attribute_modifiers") instanceof final JsonArray array) {
            for (final JsonElement element : array) {
                final Modifier modifier = element.isJsonObject() ? modifier(element.getAsJsonObject()) : null;
                if (modifier != null) {
                    modifiers.add(modifier);
                }
            }
        }
        return new Archetype(id, List.copyOf(modifiers), bool(data, "buoyant"), knockback(data.get("knockback_modifiers")),
                sounds(data.get("sound_settings")), contactDamage(data.get("contact_damage")), explosion(data.get("explosion")));
    }

    private static @Nullable Modifier modifier(final JsonObject data) {
        final String attribute = string(data, "attribute");
        final String id = string(data, "id");
        final MobAttribute.Operation operation = MobAttribute.Operation.bySavedName(string(data, "operation"));
        if (!Key.parseable(attribute) || !Key.parseable(id) || operation == null || !data.has("amount")) {
            return null;
        }
        return new Modifier(Key.key(attribute), Key.key(id), data.get("amount").getAsDouble(), operation);
    }

    private static Knockback knockback(final @Nullable JsonElement data) {
        if (!(data instanceof final JsonObject object)) {
            return DEFAULT_KNOCKBACK;
        }
        return new Knockback(number(object, "horizontal_power", DEFAULT_KNOCKBACK.horizontalPower()),
                number(object, "vertical_power", DEFAULT_KNOCKBACK.verticalPower()));
    }

    private static Sounds sounds(final @Nullable JsonElement data) {
        if (!(data instanceof final JsonObject object)) {
            return DEFAULT_SOUNDS;
        }
        return new Sounds(soundOf(object.get("hit_sound")), soundOf(object.get("push_sound")),
                number(object, "push_sound_impulse_threshold", DEFAULT_SOUNDS.pushImpulseThreshold()),
                number(object, "push_sound_cooldown", DEFAULT_SOUNDS.pushCooldown()));
    }

    private static Sound.@Nullable Type soundOf(final @Nullable JsonElement data) {
        final String name = data instanceof final JsonObject object ? string(object, "sound_id")
                : data != null && data.isJsonPrimitive() ? data.getAsString() : "";
        return Key.parseable(name) && !name.isEmpty() ? sound(name) : null;
    }

    private static @Nullable ContactDamage contactDamage(final @Nullable JsonElement data) {
        if (!(data instanceof final JsonObject object) || !Key.parseable(string(object, "damage_type"))) {
            return null;
        }
        final JsonElement amount = object.get("amount");
        float min = 0f;
        float max = 0f;
        if (amount != null && amount.isJsonPrimitive()) {
            min = amount.getAsFloat();
            max = min;
        } else if (amount instanceof final JsonObject provider) {
            // a uniform spread: the only other shape the archetypes use
            min = number(provider, "min_inclusive", 0f);
            max = number(provider, "max_exclusive", min);
        }
        return new ContactDamage(TypedKey.withRegistry(RegistryKey.DAMAGE_TYPE, Key.key(string(object, "damage_type"))),
                min, max, bool(object, "attribute_to_source"));
    }

    private static @Nullable Explosion explosion(final @Nullable JsonElement data) {
        if (!(data instanceof final JsonObject object)) {
            return null;
        }
        return new Explosion((int) number(object, "power", 0f), bool(object, "causes_fire"), (int) number(object, "fuse", 1f));
    }

    private static String string(final JsonObject object, final String name) {
        final JsonElement value = object.get(name);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : "";
    }

    private static boolean bool(final JsonObject object, final String name) {
        final JsonElement value = object.get(name);
        return value != null && value.isJsonPrimitive() && value.getAsBoolean();
    }

    private static float number(final JsonObject object, final String name, final float fallback) {
        final JsonElement value = object.get(name);
        return value != null && value.isJsonPrimitive() ? value.getAsFloat() : fallback;
    }

    private static Sound.Type sound(final String name) {
        final Key key = Key.key(name);
        return () -> key;
    }

    /**
     * One entry of the registry, as a sulfur cube uses it.
     *
     * @param id            the key of the entry, in the registry
     * @param modifiers     the attributes it changes
     * @param buoyant       {@code true} if the cube floats in water and lava
     * @param knockback     how hard a hit sends the cube
     * @param sounds        the sounds the cube makes
     * @param contactDamage the damage the cube deals what touches it, {@code null} for none
     * @param explosion     how the cube explodes, {@code null} if it does not
     */
    record Archetype(Key id, List<Modifier> modifiers, boolean buoyant, Knockback knockback, Sounds sounds,
                     @Nullable ContactDamage contactDamage, @Nullable Explosion explosion) {
    }

    /**
     * A change to an attribute of the cube, under its own id.
     */
    record Modifier(Key attribute, Key id, double amount, MobAttribute.Operation operation) {
    }

    /**
     * How hard a hit sends the cube, across and up.
     */
    record Knockback(float horizontalPower, float verticalPower) {
    }

    /**
     * The sounds of a hit and of a push, the latter only for a push hard enough and not too soon
     * after the last one.
     *
     * @param pushImpulseThreshold how fast a push has to send the cube for it to sound
     * @param pushCooldown         how long the cube keeps quiet after a push sounded, in seconds
     */
    record Sounds(Sound.@Nullable Type hit, Sound.@Nullable Type push, float pushImpulseThreshold, float pushCooldown) {
    }

    /**
     * The damage the cube deals what touches it.
     *
     * @param attributeToSource {@code true} if the cube itself deals it; it always does to a player
     */
    record ContactDamage(TypedKey<DamageType> type, float min, float max, boolean attributeToSource) {

        float sample() {
            return max > min ? min + ThreadLocalRandom.current().nextFloat() * (max - min) : min;
        }
    }

    /**
     * How the cube explodes once lit.
     *
     * @param fuse how long it burns before it explodes, in ticks
     */
    record Explosion(int power, boolean causesFire, int fuse) {
    }
}
