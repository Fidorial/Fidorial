package fr.euphyllia.fidorial.server.plugin;

import fr.fidorial.plugin.Plugin;

public final class BuiltInPlugin implements Plugin {

    public static final Plugin INSTANCE = new BuiltInPlugin();

    private BuiltInPlugin() {
    }

    @Override
    public String toString() {
        return "built-in";
    }
}
