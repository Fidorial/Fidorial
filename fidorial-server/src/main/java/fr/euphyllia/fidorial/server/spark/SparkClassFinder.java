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
import me.lucko.spark.common.util.classfinder.ClassFinder;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

public final class SparkClassFinder implements ClassFinder {

    private final Set<ClassLoader> pluginClassLoaders;

    public SparkClassFinder(final PluginManager pluginManager) {
        this.pluginClassLoaders = new LinkedHashSet<>();
        for (final PluginMeta meta : pluginManager.loaded()) {
            pluginManager
                    .plugin(meta.id())
                    .map((final Plugin plugin) -> plugin.getClass().getClassLoader())
                    .ifPresent(this.pluginClassLoaders::add);
        }
    }

    @Override
    public @Nullable Class<?> findClass(final String className) {
        for (final ClassLoader loader : this.pluginClassLoaders) {
            try {
                return Class.forName(className, false, loader);
            } catch (final Throwable ignored) {
            }
        }
        return null;
    }
}
