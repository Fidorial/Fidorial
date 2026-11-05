package fr.euphyllia.fidorial.server.command.brigadier.packet.registry;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;

public interface ArgumentTypeRegistrar<T extends ArgumentType<?>, I extends ArgumentTypeRegistrar.Spec<T>> {

    void serialize(I spec, PacketBuffer buf);

    I deserialize(PacketBuffer buf);

    void serializeJson(I spec, JsonObject value);

    I access(T argument);

    interface Spec<T extends ArgumentType<?>> {

        T instantiate();

        ArgumentTypeRegistrar<T, ?> type();
    }
}
