package fr.euphyllia.fidorial.server.permission;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OperatorList {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(OperatorList.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path file;
    private final Map<UUID, Entry> operators = new ConcurrentHashMap<>();

    public OperatorList(final Path file) {
        this.file = Objects.requireNonNull(file, "file");
    }

    public void load() {
        if (!Files.exists(file)) {
            return;
        }
        try (final Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            final List<Entry> entries = GSON.fromJson(reader, new TypeToken<List<Entry>>() {}.getType());
            operators.clear();
            if (entries != null) {
                for (final Entry entry : entries) {
                    operators.put(entry.uuid, entry);
                }
            }
            LOGGER.debug("There are currently {} op", operators.size());
        } catch (final Exception e) {
            LOGGER.error("Unable to read {}", file, e);
        }
    }

    public synchronized void save() {
        try (final Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            GSON.toJson(new ArrayList<>(operators.values()), writer);
        } catch (final IOException e) {
            LOGGER.error("Unable to save {}", file, e);
        }
    }

    public boolean isOp(final UUID uuid) {
        return operators.containsKey(uuid);
    }

    public boolean setOp(final UUID uuid, final String name, final boolean value) {
        Objects.requireNonNull(uuid, "uuid");
        final boolean changed;
        if (value) {
            changed = operators.putIfAbsent(uuid, new Entry(uuid, name)) == null;
        } else {
            changed = operators.remove(uuid) != null;
        }
        if (changed) {
            save();
        }
        return changed;
    }

    private record Entry(UUID uuid, String name) {
    }
}
