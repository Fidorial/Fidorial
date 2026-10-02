package fr.euphyllia.fidorial.server.adventure.providers;

import net.kyori.adventure.internal.properties.AdventureProperties;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage") // we are permitted
public final class AdventurePropertiesDefaultOverrideProviderImpl implements AdventureProperties.DefaultOverrideProvider {

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <T> T overrideDefault(final AdventureProperties.Property<T> property, final @Nullable T existingDefault) {
        if (property == AdventureProperties.TEXT_WARN_WHEN_LEGACY_FORMATTING_DETECTED) {
            // warn by default
            return (T) Boolean.TRUE;
        }
        return null;
    }
}
