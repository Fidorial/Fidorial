package fr.euphyllia.fidorial.server.world.gamerule;

import fr.euphyllia.fidorial.server.VersionConstants;
import fr.fidorial.gamerule.GameRuleDefinition;
import fr.fidorial.gamerule.GameRuleType;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.GameRule;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.NumberBinaryTag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class GameRuleOverrides {

    public static final String FILE_NAME = "game_rules.dat";

    private static final ComponentLogger LOGGER = ComponentLogger.logger(GameRuleOverrides.class);

    private static final String TAG_DATA_VERSION = "DataVersion";
    private static final String TAG_DATA = "data";

    private final Key worldKey;
    private final GameRuleValues base;
    private final AtomicReferenceArray<@Nullable Integer> overrides =
            new AtomicReferenceArray<>(VanillaGameRules.ALL.size());
    private final AtomicInteger count = new AtomicInteger();
    private final AtomicBoolean dirty = new AtomicBoolean();
    private final AtomicBoolean restored = new AtomicBoolean();

    public GameRuleOverrides(final Key worldKey, final GameRuleValues base) {
        this.worldKey = Objects.requireNonNull(worldKey, "The key of the world owning the game rule overrides must not be null");
        this.base = Objects.requireNonNull(base, "The base game rule values the world inherits must not be null");
    }

    public GameRuleValues base() {
        return base;
    }

    public boolean getBoolean(final TypedKey<GameRule> rule) {
        return get(GameRuleValues.index(rule, GameRuleType.BOOLEAN)) != 0;
    }

    public int getInt(final TypedKey<GameRule> rule) {
        return get(GameRuleValues.index(rule, GameRuleType.INTEGER));
    }

    public int get(final GameRuleDefinition definition) {
        return get(GameRuleValues.requireIndex(definition.key().key()));
    }

    private int get(final int index) {
        if (count.get() > 0) {
            final Integer override = overrides.get(index);
            if (override != null) {
                return override;
            }
        }
        return base.get(VanillaGameRules.ALL.get(index));
    }

    public @Nullable Integer override(final GameRuleDefinition definition) {
        return overrides.get(GameRuleValues.requireIndex(definition.key().key()));
    }

    public List<GameRuleDefinition> overridden() {
        final List<GameRuleDefinition> overridden = new ArrayList<>();
        for (int i = 0; i < VanillaGameRules.ALL.size(); i++) {
            if (overrides.get(i) != null) {
                overridden.add(VanillaGameRules.ALL.get(i));
            }
        }
        return overridden;
    }

    @Nullable Integer exchange(final GameRuleDefinition definition, final @Nullable Integer value) {
        final Integer previous = overrides.getAndSet(GameRuleValues.requireIndex(definition.key().key()), value);
        if (previous == null && value != null) {
            count.incrementAndGet();
        } else if (previous != null && value == null) {
            count.decrementAndGet();
        }
        if (!Objects.equals(previous, value)) {
            dirty.set(true);
        }
        return previous;
    }

    public int restore(final Path file) throws IOException {
        if (!restored.compareAndSet(false, true) || !Files.isRegularFile(file)) {
            return 0;
        }

        final CompoundBinaryTag root = BinaryTagIO.reader().readNamed(file, BinaryTagIO.Compression.GZIP).getValue();
        final CompoundBinaryTag data = root.getCompound(TAG_DATA);

        int restoredCount = 0;
        for (final String name : data.keySet()) {
            final int index = VanillaGameRules.indexOf(name);
            final BinaryTag raw = data.get(name);
            if (index < 0 || raw == null) {
                LOGGER.warn("Ignoring unknown game rule '{}' overridden in {}", name, worldKey);
                continue;
            }
            final GameRuleDefinition definition = VanillaGameRules.ALL.get(index);
            if (!(raw instanceof final NumberBinaryTag number)) {
                LOGGER.warn("Ignoring the override of game rule '{}' in {}: {}", name, worldKey, raw);
                continue;
            }
            if (overrides.getAndSet(index, GameRuleValues.sanitize(definition, number.intValue(), name)) == null) {
                count.incrementAndGet();
            }
            restoredCount++;
        }
        return restoredCount;
    }

    public boolean saveIfDirty(final Path file) throws IOException {
        if (!dirty.compareAndSet(true, false)) {
            return false;
        }
        try {
            write(file);
            return true;
        } catch (final IOException | RuntimeException e) {
            dirty.set(true);
            throw e;
        }
    }

    private void write(final Path file) throws IOException {
        final CompoundBinaryTag.Builder data = CompoundBinaryTag.builder();
        for (int i = 0; i < VanillaGameRules.ALL.size(); i++) {
            final Integer value = overrides.get(i);
            if (value == null) {
                continue;
            }
            final GameRuleDefinition definition = VanillaGameRules.ALL.get(i);
            final String id = definition.key().key().asString();
            if (definition.isBoolean()) {
                data.putBoolean(id, value != 0);
            } else {
                data.putInt(id, value);
            }
        }

        final CompoundBinaryTag root = CompoundBinaryTag.builder()
                .put(TAG_DATA, data.build())
                .putInt(TAG_DATA_VERSION, VersionConstants.DATA_VERSION)
                .build();

        Files.createDirectories(file.getParent());
        final Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        BinaryTagIO.writer().writeNamed(Map.entry("", root), tmp, BinaryTagIO.Compression.GZIP);
        try {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (final AtomicMoveNotSupportedException e) {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
