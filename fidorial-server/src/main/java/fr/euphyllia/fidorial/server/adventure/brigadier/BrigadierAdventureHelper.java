package fr.euphyllia.fidorial.server.adventure.brigadier;

import com.mojang.brigadier.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;

public final class BrigadierAdventureHelper {

    private BrigadierAdventureHelper() {
    }

    /**
     * {@return {@code message} as a component, with translation keys prefixed by {@code console.} for the console}
     */
    public static Component convert(final Message message, final boolean isConsole) {
        return switch (message.asComponent()) {
            case final TranslatableComponent translatable when isConsole -> Component.translatable(
                    "console." + translatable.key(), translatable.arguments().toArray(ComponentLike[]::new));
            case final Component component -> component;
        };
    }
}
