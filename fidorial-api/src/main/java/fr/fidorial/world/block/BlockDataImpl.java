package fr.fidorial.world.block;

import com.google.common.base.Preconditions;

import java.util.List;

abstract class BlockDataImpl implements BlockData {

    private final BlockType type;
    private final int ordinal;

    BlockDataImpl(final BlockType type, final int ordinal) {
        this.type = type;
        this.ordinal = ordinal;
    }

    @Override
    public final BlockType type() {
        return type;
    }

    @Override
    public final int networkId() {
        return type.networkIdAt(ordinal);
    }

    @Override
    public final int ordinal() {
        return ordinal;
    }

    @Override
    public final <T> T get(final BlockProperty<T> property) {
        checkProperty(property);
        return type.value(ordinal, property);
    }

    @Override
    public final boolean has(final BlockProperty<?> property) {
        checkProperty(property);
        return type.position(property) >= 0;
    }

    @Override
    public final <T> BlockData with(final BlockProperty<T> property, final T value) {
        checkProperty(property);
        Preconditions.checkArgument(value != null, "The value of a block data property must not be null");
        return type.stateAt(type.withValue(ordinal, property, value));
    }

    @Override
    public final List<PropertyValue<?>> values() {
        return type.valuesOf(ordinal);
    }

    @Override
    public final String toString() {
        return asString();
    }

    private static void checkProperty(final BlockProperty<?> property) {
        Preconditions.checkArgument(property != null, "The property of a block data must not be null");
    }
}
