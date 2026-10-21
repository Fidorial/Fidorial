package fr.euphyllia.fidorial.server.command.brigadier.argument.resource;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.KeyReader;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import net.kyori.adventure.key.Key;

public final class KeyArgument implements ArgumentType<Key> {

    public static KeyArgument key() {
        return new KeyArgument();
    }

    @Override
    public Key parse(final StringReader reader) throws CommandSyntaxException {
        return KeyReader.readKey(reader);
    }

    public static final class Info implements ArgumentTypeRegistrar<KeyArgument, Info.Spec> {

        @Override
        public void serialize(final Spec spec, final PacketBuffer buf) {
        }

        @Override
        public Spec deserialize(final PacketBuffer buf) {
            return new Spec();
        }

        @Override
        public void serializeJson(final Spec spec, final JsonObject json) {
        }

        @Override
        public Spec access(final KeyArgument argument) {
            return new Spec();
        }

        public record Spec() implements ArgumentTypeRegistrar.Spec<KeyArgument> {
            @Override
            public KeyArgument instantiate() {
                return KeyArgument.key();
            }

            @Override
            public ArgumentTypeRegistrar<KeyArgument, ?> type() {
                return new Info();
            }
        }
    }
}
