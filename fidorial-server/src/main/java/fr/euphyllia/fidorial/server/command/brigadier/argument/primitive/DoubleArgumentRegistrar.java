package fr.euphyllia.fidorial.server.command.brigadier.argument.primitive;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.NumberFlags;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;

public final class DoubleArgumentRegistrar
        implements ArgumentTypeRegistrar<DoubleArgumentType, DoubleArgumentRegistrar.Spec> {

    @Override
    public void serialize(final Spec spec, final PacketBuffer buf) {
        final boolean hasMin = spec.min() != -Double.MAX_VALUE;
        final boolean hasMax = spec.max() != Double.MAX_VALUE;
        buf.writeByte(NumberFlags.create(hasMin, hasMax));
        if (hasMin) buf.writeDouble(spec.min());
        if (hasMax) buf.writeDouble(spec.max());
    }

    @Override
    public Spec deserialize(final PacketBuffer buf) {
        final byte flags = buf.readByte();
        final double min = NumberFlags.hasMin(flags) ? buf.readDouble() : -Double.MAX_VALUE;
        final double max = NumberFlags.hasMax(flags) ? buf.readDouble() : Double.MAX_VALUE;
        return new Spec(min, max);
    }

    @Override
    public void serializeJson(final Spec spec, final JsonObject json) {
        if (spec.min() != -Double.MAX_VALUE) json.addProperty("min", spec.min());
        if (spec.max() != Double.MAX_VALUE) json.addProperty("max", spec.max());
    }

    @Override
    public Spec access(final DoubleArgumentType argument) {
        return new Spec(argument.getMinimum(), argument.getMaximum());
    }

    public record Spec(double min, double max) implements ArgumentTypeRegistrar.Spec<DoubleArgumentType> {
        @Override
        public DoubleArgumentType instantiate() {
            return DoubleArgumentType.doubleArg(min, max);
        }

        @Override
        public ArgumentTypeRegistrar<DoubleArgumentType, ?> type() {
            return new DoubleArgumentRegistrar();
        }
    }
}
