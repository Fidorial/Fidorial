package fr.euphyllia.fidorial.server.command.brigadier.argument.primitive;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType.StringType;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;

public final class StringArgumentRegistrar
        implements ArgumentTypeRegistrar<StringArgumentType, StringArgumentRegistrar.Spec> {

    @Override
    public void serialize(final Spec spec, final PacketBuffer buf) {
        buf.writeVarInt(spec.stringType().ordinal());
    }

    @Override
    public Spec deserialize(final PacketBuffer buf) {
        final StringType stringType = StringType.values()[buf.readVarInt()];
        return new Spec(stringType);
    }

    @Override
    public void serializeJson(final Spec spec, final JsonObject json) {
        json.addProperty(
                "type",
                switch (spec.stringType()) {
                    case SINGLE_WORD -> "word";
                    case QUOTABLE_PHRASE -> "phrase";
                    case GREEDY_PHRASE -> "greedy";
                });
    }

    @Override
    public Spec access(final StringArgumentType argument) {
        return new Spec(argument.getType());
    }

    public record Spec(StringType stringType) implements ArgumentTypeRegistrar.Spec<StringArgumentType> {
        @Override
        public StringArgumentType instantiate() {
            return switch (stringType) {
                case SINGLE_WORD -> StringArgumentType.word();
                case QUOTABLE_PHRASE -> StringArgumentType.string();
                case GREEDY_PHRASE -> StringArgumentType.greedyString();
            };
        }

        @Override
        public ArgumentTypeRegistrar<StringArgumentType, ?> type() {
            return new StringArgumentRegistrar();
        }
    }
}
