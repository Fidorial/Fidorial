package fr.fidorial.translation;

import fr.fidorial.Server;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * Renders translatable components into a given locale.
 *
 * <p>A single store is active at a time, reachable statically through {@link #current()} or from
 * {@link Server#translationStore()}.</p>
 *
 * @since 0.1.0
 */
public interface TranslationStore {

    /**
     * Replaces the active translation store implementation, loading it and unloading the previous one.
     *
     * @param store new {@link TranslationStore}
     */
    static void setStore(final TranslationStore store) {
        final TranslationStore previous = current();

        store.load();
        Holder.INSTANCE = store;

        if (previous != null) {
            previous.unload();
        }
    }

    /**
     * Gets the active translation store implementation.
     *
     * @return active {@link TranslationStore}
     */
    static TranslationStore current() {
        return Holder.INSTANCE;
    }

    /**
     * Renders a component using a locale.
     *
     * @param component {@link Component} to render
     * @param locale    {@link Locale} to render with
     * @return rendered {@link Component}
     */
    static Component render(final Component component, final Locale locale) {
        return current().renderComponent(component, locale);
    }

    /**
     * Gets the default locale of the current translation store.
     *
     * @return default {@link Locale}
     */
    static Locale defaultLocale() {
        return current().getDefaultLocale();
    }

    /**
     * Renders a component, translating every translatable part into a locale.
     *
     * @param component the component to render
     * @param locale    the target locale
     * @return the rendered component
     * @since 0.1.0
     */
    Component renderComponent(Component component, Locale locale);

    /**
     * {@return the locale used when a translation is missing in the requested one}
     *
     * @since 0.1.0
     */
    Locale getDefaultLocale();

    /**
     * Loads the translations; called when the store becomes active.
     *
     * @since 0.1.0
     */
    void load();

    /**
     * Releases the translations; called when another store replaces this one.
     *
     * @since 0.1.0
     */
    void unload();

    /**
     * Holds the active store.
     *
     * @hidden
     */
    final class Holder {
        private Holder() {
        }

        private static volatile @Nullable TranslationStore INSTANCE;
    }
}
