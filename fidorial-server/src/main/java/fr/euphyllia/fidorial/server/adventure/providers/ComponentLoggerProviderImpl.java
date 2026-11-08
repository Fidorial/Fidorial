package fr.euphyllia.fidorial.server.adventure.providers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.logger.slf4j.ComponentLoggerProvider;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.slf4j.LoggerFactory;

import java.util.Locale;

@SuppressWarnings("UnstableApiUsage") // we are permitted
public final class ComponentLoggerProviderImpl implements ComponentLoggerProvider {
    @Override
    public ComponentLogger logger(final ComponentLoggerProvider.LoggerHelper helper, final String name) {
        return helper.delegating(LoggerFactory.getLogger(name), this::serialize);
    }

    private String serialize(final Component message) {
        return ANSIComponentSerializer.ansi().serialize(GlobalTranslator.render(message, Locale.getDefault()));
    }
}
