package fr.euphyllia.fidorial.server.command.brigadier.packet.registry;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;

public interface ArgumentTypeRegistrar<T extends ArgumentType<?>, I extends ArgumentTypeRegistrar.Spec<T>> {

    void serialize(I spec, PacketBuffer buf);

    I deserialize(PacketBuffer buf);

    void serializeJson(I spec, JsonObject value);

    I access(T argument);

    /**
     * The network id of an argument whose type is split into several protocol argument types, such
     * as the string arguments since 26.4-snapshot-9.
     *
     * @param spec the argument
     * @return its network id, or {@code -1} to use the id the registrar was registered with
     */
    default int networkId(final I spec) {
        return -1;
    }

    interface Spec<T extends ArgumentType<?>> {

        T instantiate();

        ArgumentTypeRegistrar<T, ?> type();
    }
}
