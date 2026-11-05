package fr.euphyllia.fidorial.server.command.brigadier.argument.primitive;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.NumberFlags;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;

public final class IntegerArgumentRegistrar
        implements ArgumentTypeRegistrar<IntegerArgumentType, IntegerArgumentRegistrar.Spec> {

    @Override
    public void serialize(final Spec spec, final PacketBuffer buf) {
        final boolean hasMin = spec.min() != Integer.MIN_VALUE;
        final boolean hasMax = spec.max() != Integer.MAX_VALUE;
        buf.writeByte(NumberFlags.create(hasMin, hasMax));
        if (hasMin) buf.writeInt(spec.min());
        if (hasMax) buf.writeInt(spec.max());
    }

    @Override
    public Spec deserialize(final PacketBuffer buf) {
        final byte flags = buf.readByte();
        final int min = NumberFlags.hasMin(flags) ? buf.readInt() : Integer.MIN_VALUE;
        final int max = NumberFlags.hasMax(flags) ? buf.readInt() : Integer.MAX_VALUE;
        return new Spec(min, max);
    }

    @Override
    public void serializeJson(final Spec spec, final JsonObject json) {
        if (spec.min() != Integer.MIN_VALUE) json.addProperty("min", spec.min());
        if (spec.max() != Integer.MAX_VALUE) json.addProperty("max", spec.max());
    }

    @Override
    public Spec access(final IntegerArgumentType argument) {
        return new Spec(argument.getMinimum(), argument.getMaximum());
    }

    public record Spec(int min, int max) implements ArgumentTypeRegistrar.Spec<IntegerArgumentType> {
        @Override
        public IntegerArgumentType instantiate() {
            return IntegerArgumentType.integer(min, max);
        }

        @Override
        public ArgumentTypeRegistrar<IntegerArgumentType, ?> type() {
            return new IntegerArgumentRegistrar();
        }
    }
}
