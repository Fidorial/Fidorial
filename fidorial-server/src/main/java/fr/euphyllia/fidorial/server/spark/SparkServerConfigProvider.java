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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.configuration.ServerConfiguration;
import fr.euphyllia.fidorial.server.configuration.WorldConfiguration;
import fr.euphyllia.fidorial.server.configuration.WorldConfigurationContainer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.WorldManager;
import me.lucko.spark.common.platform.serverconfig.ConfigParser;
import me.lucko.spark.common.platform.serverconfig.ExcludedConfigFilter;
import me.lucko.spark.common.platform.serverconfig.ServerConfigProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public final class SparkServerConfigProvider extends ServerConfigProvider {

    private static final Collection<String> HIDDEN_PATHS = ImmutableSet.<String>builder()
            .addAll(BASE_HIDDEN_PATHS)
            .add("network.proxy.secret")
            .add("resource-pack.url")
            .add("resource-pack.hash")
            .add("resource-pack.id")
            .add("worlds.level-seed")
            .build();

    public SparkServerConfigProvider(final FidorialServer server) {
        super(sources(server), HIDDEN_PATHS);
    }

    private static Map<String, ConfigParser> sources(final FidorialServer server) {
        return ImmutableMap.of(
                "fidorial", new LiveSource(() -> encode(ServerConfiguration.CODEC, server.config())),
                "worlds", new LiveSource(() -> encodeWorlds(server.worldManager())));
    }

    private static JsonElement encodeWorlds(final WorldManager manager) throws IOException {
        final WorldConfigurationContainer configurations = manager.configurations();
        final JsonObject root = new JsonObject();
        root.add("default", encode(WorldConfiguration.CODEC, configurations.defaults()));
        for (final ServerWorld world : manager.worlds()) {
            root.add(world.key().asString(), encode(configurations.overrideCodec(), world.configuration()));
        }
        return root;
    }

    private static <T> JsonElement encode(final Codec<T> codec, final T value) throws IOException {
        return codec.encodeStart(JsonOps.INSTANCE, value)
                .getOrThrow(message -> new IOException("Could not encode the configuration: " + message));
    }

    @FunctionalInterface
    private interface Snapshot {
        JsonElement take() throws IOException;
    }

    private record LiveSource(Snapshot snapshot) implements ConfigParser {

        @Override
        public JsonElement load(final String name, final ExcludedConfigFilter filter) throws IOException {
            return filter.apply(snapshot.take());
        }

        @Override
        public Map<String, Object> parse(final BufferedReader reader) {
            throw new UnsupportedOperationException("Live configurations are encoded, not parsed");
        }
    }
}
