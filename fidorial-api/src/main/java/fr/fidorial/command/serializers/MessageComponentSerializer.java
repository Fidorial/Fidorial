package fr.fidorial.command.serializers;

import com.mojang.brigadier.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.jetbrains.annotations.ApiStatus;

import java.util.ServiceLoader;

/**
 * Converts between Brigadier {@link Message}s and {@link Component}s, for exception and tooltip messages.
 *
 * @since 0.1.0
 */
@ApiStatus.NonExtendable
public interface MessageComponentSerializer extends ComponentSerializer<Component, Component, Message> {

    /**
     * {@return the serializer}
     *
     * @since 0.1.0
     */
    static MessageComponentSerializer message() {
        final class Holder {
            static final MessageComponentSerializer INSTANCE = ServiceLoader
                    .load(MessageComponentSerializer.class, MessageComponentSerializer.class.getClassLoader())
                    .findFirst()
                    .orElseThrow();
        }
        return Holder.INSTANCE;
    }
}
