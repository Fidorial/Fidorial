package fr.fidorial.item;

import com.google.common.base.Preconditions;
import fr.fidorial.item.data.DataComponentEditor;
import fr.fidorial.item.data.DataComponentHolder;
import fr.fidorial.item.data.DataComponentMap;
import fr.fidorial.item.data.DataComponentType;
import fr.fidorial.item.data.DataComponentTypes;
import net.kyori.adventure.key.Key;

import java.util.function.Consumer;

/**
 * Everything the server needs to build an item a plugin invented.
 *
 * @param key         the identifier the item is created and saved under
 * @param networkType the vanilla item the client renders
 * @param components  the components every stack of this item starts with
 * @since 0.1.0
 */
public record ItemDefinition(Key key, Key networkType, DataComponentMap components)
        implements DataComponentHolder {

    /**
     * What an item stacks to when it says nothing about it.
     */
    public static final int DEFAULT_MAX_STACK_SIZE = 1;

    public ItemDefinition {
        Preconditions.checkNotNull(key, "The key of an item definition must not be null");
        Preconditions.checkNotNull(networkType, "The network type of an item definition must not be null");
        Preconditions.checkNotNull(components, "The components of an item definition must not be null");

        final int stackSize = components.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, DEFAULT_MAX_STACK_SIZE);
        final int damage = components.getOrDefault(DataComponentTypes.MAX_DAMAGE, 0);

        if (stackSize < 1) {
            throw new IllegalArgumentException("maxStackSize must be at least 1, got " + stackSize);
        }

        if (damage < 0) {
            throw new IllegalArgumentException("maxDamage cannot be negative, got " + damage);
        }

        if (stackSize > 1 && damage > 0) {
            throw new IllegalArgumentException(
                    "An item that stacks cannot have durability: " + key + " stacks to " + stackSize);
        }
    }

    /**
     * @param key         the identifier the item is created and saved under
     * @param networkType the vanilla item the client renders
     * @return a builder pre-filled with the defaults of a plain, unstackable tool
     * @since 0.1.0
     */
    public static Builder builder(final Key key, final Key networkType) {
        return new Builder(key, networkType);
    }

    /**
     * @return a builder pre-populated with this definition
     * @since 0.1.0
     */
    public Builder toBuilder() {
        return new Builder(key, networkType, DataComponentEditor.of(components));
    }

    /**
     * Assembles an {@link ItemDefinition}.
     *
     * @since 0.1.0
     */
    public static final class Builder {

        private final Key key;
        private final Key networkType;
        private final DataComponentEditor components;

        private Builder(final Key key, final Key networkType) {
            final DataComponentEditor editor = DataComponentEditor.empty();
            editor.set(DataComponentTypes.MAX_STACK_SIZE, DEFAULT_MAX_STACK_SIZE);
            this(key, networkType, editor);
        }

        private Builder(final Key key, final Key networkType, final DataComponentEditor components) {
            this.key = Preconditions.checkNotNull(key, "The key of an item definition builder must not be null");
            this.networkType = Preconditions.checkNotNull(networkType, "The network type of an item definition builder must not be null");
            this.components = Preconditions.checkNotNull(components, "The components of an item definition builder must not be null");
        }

        /**
         * Describes what every stack of this item starts out looking like, through
         * the same editor {@link ItemStack#edit(Consumer)} hands out.
         *
         * @param editor given the editor this builder is filling in
         * @return this builder
         * @since 0.1.0
         */
        public Builder edit(final Consumer<DataComponentEditor> editor) {
            editor.accept(components);
            return this;
        }

        /**
         * @param type  the component to set
         * @param value the value
         * @param <T>   the component's value type
         * @return this builder
         * @since 0.1.0
         */
        public <T> Builder set(final DataComponentType<T> type, final T value) {
            components.set(type, value);
            return this;
        }

        /**
         * @param type the component to stop declaring
         * @return this builder
         * @since 0.1.0
         */
        public Builder reset(final DataComponentType<?> type) {
            components.reset(type);
            return this;
        }

        /**
         * @return the assembled definition
         * @since 0.1.0
         */
        public ItemDefinition build() {
            return new ItemDefinition(key, networkType, components.components());
        }
    }
}
