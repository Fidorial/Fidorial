/*
 * This file is part of spark.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.euphyllia.fidorial.server.spark;

import fr.fidorial.plugin.Plugin;
import fr.fidorial.plugin.PluginManager;
import fr.fidorial.plugin.PluginMeta;
import me.lucko.spark.common.sampler.source.ClassSourceLookup;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps the class loader of each loaded plugin to its name, so spark can attribute profiler frames
 * to the plugin that owns them.
 */
public final class SparkClassSourceLookup extends ClassSourceLookup.ByClassLoader {

    private final Map<ClassLoader, String> classLoaderToPlugin;

    public SparkClassSourceLookup(final PluginManager pluginManager) {
        this.classLoaderToPlugin = new HashMap<>();
        for (final PluginMeta meta : pluginManager.loaded()) {
            pluginManager
                    .plugin(meta.id())
                    .ifPresent((final Plugin plugin) ->
                            this.classLoaderToPlugin.put(plugin.getClass().getClassLoader(), meta.name()));
        }
    }

    @Override
    public @Nullable String identify(final ClassLoader loader) {
        return this.classLoaderToPlugin.get(loader);
    }
}
