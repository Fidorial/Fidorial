package fr.euphyllia.fidorial.server.command.brigadier;

import com.mojang.brigadier.Message;
import fr.fidorial.command.serializers.MessageComponentSerializer;
import net.kyori.adventure.text.Component;

public final class MessageComponentSerializerImpl implements MessageComponentSerializer {

    @Override
    public Component deserialize(final Message input) {
        return input.asComponent();
    }

    @Override
    public Message serialize(final Component component) {
        return Message.component(component);
    }
}
