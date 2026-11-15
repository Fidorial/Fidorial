package fr.euphyllia.fidorial.server.entity.mob;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An attribute of a mob as vanilla keeps it: a base value and the modifiers added to it, each with
 * its identifier, so that a mob saved here reads the same in vanilla and the other way round.
 *
 * <p>The value is vanilla's: the base plus the {@link Operation#ADD_VALUE added values}, plus that
 * sum times each {@link Operation#ADD_MULTIPLIED_BASE share of the base}, the whole multiplied by
 * one plus each {@link Operation#ADD_MULTIPLIED_TOTAL share of the total}.</p>
 */
public final class MobAttribute {

    private final Key id;
    private double base;
    private final Map<Key, Modifier> modifiers = new LinkedHashMap<>();

    /**
     * @param id   the identifier of the attribute, {@code minecraft:max_health} for instance
     * @param base its value without any modifier
     */
    public MobAttribute(final Key id, final double base) {
        this.id = id;
        this.base = base;
    }

    public Key id() {
        return id;
    }

    public double base() {
        return base;
    }

    public void setBase(final double base) {
        this.base = base;
    }

    /**
     * Adds a modifier, in place of the one with the same identifier if there is one.
     */
    public void put(final Key modifierId, final double amount, final Operation operation) {
        modifiers.put(modifierId, new Modifier(amount, operation));
    }

    public void remove(final Key modifierId) {
        modifiers.remove(modifierId);
    }

    /**
     * {@return the amount of a modifier, {@code 0} if the attribute does not have it}
     */
    public double amount(final Key modifierId) {
        final Modifier modifier = modifiers.get(modifierId);
        return modifier == null ? 0.0 : modifier.amount();
    }

    public boolean has(final Key modifierId) {
        return modifiers.containsKey(modifierId);
    }

    /**
     * {@return the value of the attribute, its modifiers applied}
     */
    public double value() {
        double sum = base;
        for (final Modifier modifier : modifiers.values()) {
            if (modifier.operation() == Operation.ADD_VALUE) {
                sum += modifier.amount();
            }
        }
        double value = sum;
        for (final Modifier modifier : modifiers.values()) {
            if (modifier.operation() == Operation.ADD_MULTIPLIED_BASE) {
                value += sum * modifier.amount();
            }
        }
        for (final Modifier modifier : modifiers.values()) {
            if (modifier.operation() == Operation.ADD_MULTIPLIED_TOTAL) {
                value *= 1.0 + modifier.amount();
            }
        }
        return value;
    }

    /**
     * Writes attributes as vanilla's {@code attributes} list.
     */
    public static void save(final CompoundBinaryTag.Builder tag, final MobAttribute... attributes) {
        final ListBinaryTag.Builder<BinaryTag> list = ListBinaryTag.builder();
        for (final MobAttribute attribute : attributes) {
            final CompoundBinaryTag.Builder entry = CompoundBinaryTag.builder();
            entry.putString("id", attribute.id.asString());
            entry.putDouble("base", attribute.base);
            if (!attribute.modifiers.isEmpty()) {
                final ListBinaryTag.Builder<BinaryTag> saved = ListBinaryTag.builder();
                attribute.modifiers.forEach((modifierId, modifier) -> saved.add(CompoundBinaryTag.builder()
                        .putString("id", modifierId.asString())
                        .putDouble("amount", modifier.amount())
                        .putString("operation", modifier.operation().savedName)
                        .build()));
                entry.put("modifiers", saved.build());
            }
            list.add(entry.build());
        }
        tag.put("attributes", list.build());
    }

    /**
     * Reads back, from vanilla's {@code attributes} list, those of the given attributes that were
     * saved; the others are left as they are.
     */
    public static void load(final CompoundBinaryTag tag, final MobAttribute... attributes) {
        for (final BinaryTag element : tag.getList("attributes")) {
            if (!(element instanceof final CompoundBinaryTag entry)) {
                continue;
            }
            final Key id = parse(entry.getString("id"));
            if (id == null) {
                continue;
            }
            for (final MobAttribute attribute : attributes) {
                if (attribute.id.equals(id)) {
                    attribute.read(entry);
                }
            }
        }
    }

    @SuppressWarnings("PatternValidation")
    private static @Nullable Key parse(final String raw) {
        return !raw.isEmpty() && Key.parseable(raw) ? Key.key(raw) : null;
    }

    private void read(final CompoundBinaryTag entry) {
        base = entry.getDouble("base", base);
        modifiers.clear();
        for (final BinaryTag element : entry.getList("modifiers")) {
            if (!(element instanceof final CompoundBinaryTag saved)) {
                continue;
            }
            final Operation operation = Operation.bySavedName(saved.getString("operation"));
            final Key modifierId = parse(saved.getString("id"));
            if (operation != null && modifierId != null) {
                modifiers.put(modifierId, new Modifier(saved.getDouble("amount"), operation));
            }
        }
    }

    /**
     * How a modifier changes the value of an attribute.
     */
    public enum Operation {
        ADD_VALUE("add_value"),
        ADD_MULTIPLIED_BASE("add_multiplied_base"),
        ADD_MULTIPLIED_TOTAL("add_multiplied_total");

        private final String savedName;

        Operation(final String savedName) {
            this.savedName = savedName;
        }

        private static @Nullable Operation bySavedName(final String name) {
            for (final Operation operation : values()) {
                if (operation.savedName.equals(name)) {
                    return operation;
                }
            }
            return null;
        }
    }

    private record Modifier(double amount, Operation operation) {
    }
}
