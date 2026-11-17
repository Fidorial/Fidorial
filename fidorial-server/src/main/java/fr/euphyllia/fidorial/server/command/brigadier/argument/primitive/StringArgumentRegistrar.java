package fr.euphyllia.fidorial.server.command.brigadier.argument.primitive;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType.StringType;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.euphyllia.fidorial.server.registry.data.ArgumentTypeIds;

/**
 * Brigadier's string arguments.
 *
 * <p>Since 26.4-snapshot-9 the protocol has one argument type per kind of string,
 * {@code minecraft:word}, {@code minecraft:quoted_string} and {@code minecraft:greedy_string},
 * none with properties, instead of a single {@code minecraft:string} followed by the kind.</p>
 */
public final class StringArgumentRegistrar
        implements ArgumentTypeRegistrar<StringArgumentType, StringArgumentRegistrar.Spec> {

    @Override
    public void serialize(final Spec spec, final PacketBuffer buf) {
        // The kind of string is carried by the network id alone.
    }

    @Override
    public Spec deserialize(final PacketBuffer buf) {
        // Nothing tells the kinds apart past the network id; fall back on a quoted string.
        return new Spec(StringType.QUOTABLE_PHRASE);
    }

    @Override
    public int networkId(final Spec spec) {
        return switch (spec.stringType()) {
            case SINGLE_WORD -> ArgumentTypeIds.WORD_ARGUMENT_ID;
            case QUOTABLE_PHRASE -> ArgumentTypeIds.QUOTED_STRING_ARGUMENT_ID;
            case GREEDY_PHRASE -> ArgumentTypeIds.GREEDY_STRING_ARGUMENT_ID;
        };
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
