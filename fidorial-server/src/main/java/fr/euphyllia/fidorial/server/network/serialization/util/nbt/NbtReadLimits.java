package fr.euphyllia.fidorial.server.network.serialization.util.nbt;

import io.netty.handler.codec.DecoderException;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagType;
import net.kyori.adventure.nbt.BinaryTagTypes;

import java.util.Map;

public final class NbtReadLimits {

    public static final int MAX_NESTING = 512;

    private final long byteBudget;
    private final int nestingLimit;
    private long spent;

    private NbtReadLimits(final long byteBudget, final int nestingLimit) {
        this.byteBudget = byteBudget;
        this.nestingLimit = nestingLimit;
    }

    public static NbtReadLimits withBudget(final long bytes) {
        return new NbtReadLimits(bytes, MAX_NESTING);
    }

    public static NbtReadLimits withBudget(final long bytes, final int nestingLimit) {
        return new NbtReadLimits(bytes, nestingLimit);
    }

    public static NbtReadLimits noBudget() {
        return new NbtReadLimits(Long.MAX_VALUE, MAX_NESTING);
    }

    public void spend(final long amount) {
        if (amount < 0) {
            throw new DecoderException("Refusing to spend a negative NBT byte amount: " + amount);
        }
        spent += amount;
        if (spent > byteBudget) {
            throw new DecoderException("NBT decode exceeded its byte budget (" + spent + "/" + byteBudget + ")");
        }
    }

    public void spendFor(final BinaryTagType<? extends BinaryTag> type) {
        spend(cost(type));
    }

    private static final Map<BinaryTagType<?>, Long> COSTS = Map.ofEntries(
            Map.entry(BinaryTagTypes.END, 0L),
            Map.entry(BinaryTagTypes.BYTE, 9L),
            Map.entry(BinaryTagTypes.SHORT, 10L),
            Map.entry(BinaryTagTypes.INT, 12L),
            Map.entry(BinaryTagTypes.FLOAT, 12L),
            Map.entry(BinaryTagTypes.LONG, 16L),
            Map.entry(BinaryTagTypes.DOUBLE, 16L),
            Map.entry(BinaryTagTypes.BYTE_ARRAY, 24L),
            Map.entry(BinaryTagTypes.INT_ARRAY, 24L),
            Map.entry(BinaryTagTypes.LONG_ARRAY, 24L),
            Map.entry(BinaryTagTypes.STRING, 36L),
            Map.entry(BinaryTagTypes.LIST, 36L),
            Map.entry(BinaryTagTypes.COMPOUND, 48L)
    );

    private static long cost(final BinaryTagType<? extends BinaryTag> type) {
        final Long cost = COSTS.get(type);
        if (cost == null) {
            throw new DecoderException("Unknown NBT tag type: " + type);
        }
        return cost;
    }

    public void spendStringValue(final int length) {
        spend(2L * length);
    }

    public void spendCompoundKey(final int length) {
        spend(28L + 2L * length);
    }

    public void spendNewCompoundEntry() {
        spend(36L);
    }

    public void checkDepth(final int depth) {
        if (depth > nestingLimit) {
            throw new DecoderException("NBT payload nesting exceeds limit of " + nestingLimit);
        }
    }
}
