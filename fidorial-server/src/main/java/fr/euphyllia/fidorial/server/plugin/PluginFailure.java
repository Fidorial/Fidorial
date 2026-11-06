package fr.euphyllia.fidorial.server.plugin;

import org.jspecify.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Why a plugin did not start, kept so that {@code /plugins} can show it.
 *
 * <p>Only text is kept, never the {@link Throwable} itself: an exception thrown by a plugin can be
 * an instance of one of its classes, and holding on to it would keep the discarded class loader
 * alive.</p>
 *
 * @param phase   the startup step that failed
 * @param summary a one-line description of the error
 * @param details the stack trace, at most {@link #MAX_DETAIL_LINES} lines, empty if there was no exception
 */
public record PluginFailure(Phase phase, String summary, List<String> details) {

    static final int MAX_DETAIL_LINES = 40;

    public PluginFailure {
        details = List.copyOf(details);
    }

    static PluginFailure of(final Phase phase, final String summary) {
        return new PluginFailure(phase, summary, List.of());
    }

    static PluginFailure of(final Phase phase, final Throwable error) {
        return new PluginFailure(phase, describe(error), stackTrace(error));
    }

    private static String describe(final Throwable error) {
        Throwable root = error;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        if (root == error) {
            return error.toString();
        }
        return error + " (caused by " + root + ")";
    }

    private static List<String> stackTrace(final Throwable error) {
        final StringWriter out = new StringWriter();
        error.printStackTrace(new PrintWriter(out));
        final List<String> lines = out.toString().lines().toList();
        return lines.size() <= MAX_DETAIL_LINES ? lines : lines.subList(0, MAX_DETAIL_LINES);
    }

    /**
     * The startup step a plugin failed at.
     */
    public enum Phase {
        /**
         * {@code fidorial.json} is missing a field, malformed, or the jar is unreadable.
         */
        DESCRIPTOR,
        /**
         * The libraries declared by the plugin could not be resolved.
         */
        LIBRARIES,
        /**
         * A dependency is missing, failed itself, or is part of a cycle; or the ID is taken.
         */
        DEPENDENCY,
        /**
         * The main class could not be created, or {@code onLoad} threw.
         */
        LOAD,
        /**
         * {@code onEnable} threw.
         */
        ENABLE;

        /**
         * {@return the translation key naming this phase}
         */
        public String translationKey() {
            return "command.plugins.phase." + name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    /**
     * A plugin as {@code /plugins} lists it: running, stopped, or failed.
     *
     * @param key     how to look the plugin up: its ID, or the jar name when the descriptor is unreadable
     * @param name    the display name
     * @param version the version, if the descriptor could be read
     * @param authors the authors, empty if unknown
     * @param enabled whether the plugin is currently enabled
     * @param failure why it failed, if it did
     */
    public record Status(
            String key,
            String name,
            @Nullable String version,
            List<String> authors,
            boolean enabled,
            @Nullable PluginFailure failure
    ) {
        public Status {
            authors = List.copyOf(authors);
        }
    }
}
