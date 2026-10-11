package fr.euphyllia.fidorial.server.world.storage;

import fr.euphyllia.fidorial.server.VersionConstants;
import fr.euphyllia.fidorial.server.world.ChunkGeneratorConfig;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.entity.AnvilEntitySerializer;
import fr.euphyllia.fidorial.server.world.gamerule.GameRuleValues;
import fr.euphyllia.fidorial.server.world.storage.datafixers.DataFixerType;
import fr.euphyllia.fidorial.server.world.storage.datafixers.registry.DataFixersRegistry;
import fr.euphyllia.fidorial.server.world.weather.WeatherState;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LevelData {

    private static final String DIMENSIONS = "dimensions";
    private static final String WORLD_CLOCKS = "WorldClocks";
    private static final List<String> DIFFICULTIES = List.of("peaceful", "easy", "normal", "hard");

    public static final Path GAME_RULES_PATH = Path.of("minecraft", "game_rules.dat");
    public static final Path WORLD_GEN_SETTINGS_PATH = Path.of("minecraft", "world_gen_settings.dat");
    public static final Path CUSTOM_BOSS_EVENTS_PATH = Path.of("minecraft", "custom_boss_events.dat");
    public static final Path WEATHER_PATH = Path.of("minecraft", "weather.dat");
    public static final Path WORLD_CLOCKS_PATH = Path.of("fidorial", "world_clocks.dat"); // we don't match vanilla here

    public final Map<Key, DimensionGenerator> generators = new ConcurrentHashMap<>();

    public String levelName = "Fidorial";
    public long seed = 0L;
    public long time = 0L;
    public long dayTime = 0L;

    public Key spawnDimension = Dimension.OVERWORLD.id();
    public int spawnX = 8;
    public int spawnY = -48;
    public int spawnZ = 8;
    public int spawnYaw = 0;
    public int spawnPitch = 0;

    public int gameType = 0;       // 0 = survival
    public int difficulty = 2;     // 2 = normal
    public boolean hardcore = false;
    public boolean difficultyLocked = false;
    public boolean allowCommands = true;

    public int dataVersion = VersionConstants.DATA_VERSION;
    public String versionName = VersionConstants.MINECRAFT_VERSION_NAME;
    public boolean snapshot = !VersionConstants.IS_RELEASE;
    public boolean wasModded = false;
    public final List<Integer> versionHistory = new ArrayList<>();
    private static final List<Key> DEFAULT_FEATURES = List.of(Key.key("vanilla"));

    public final List<String> enabledPacks = new ArrayList<>(List.of("vanilla"));
    public final List<String> disabledPacks = new ArrayList<>();
    public final List<Key> enabledFeatures = new ArrayList<>(DEFAULT_FEATURES);

    public @Nullable UUID singleplayerUuid;

    public final WeatherState weather = new WeatherState();

    public boolean doDaylightCycle = true;

    public final GameRuleValues gameRules = new GameRuleValues();

    public final Map<Key, WorldTime> worldTimes = new LinkedHashMap<>();
    public final Map<Key, BossBarData> bossBars = new LinkedHashMap<>();

    private CompoundBinaryTag worldGenSettings = CompoundBinaryTag.empty();

    public @Nullable WorldTime worldTime(final Key dimensionId) {
        final WorldTime stored = worldTimes.get(dimensionId);
        if (stored != null) {
            return stored;
        }
        if (Dimension.OVERWORLD.id().equals(dimensionId)) {
            return new WorldTime(time, dayTime, doDaylightCycle);
        }
        return null;
    }

    public void setWorldTime(final Key dimensionId, final long worldAge, final long dayTime, final boolean doDaylightCycle) {
        worldTimes.put(dimensionId, new WorldTime(worldAge, dayTime, doDaylightCycle));
        if (Dimension.OVERWORLD.id().equals(dimensionId)) {
            this.time = worldAge;
            this.dayTime = dayTime;
            this.doDaylightCycle = doDaylightCycle;
        }
    }

    public record WorldTime(long worldAge, long dayTime, boolean doDaylightCycle) {
    }

    public record BossBarData(
            Component name,
            int value,
            int max,
            BossBar.Color color,
            BossBar.Overlay overlay,
            Set<BossBar.Flag> flags,
            boolean visible,
            Set<UUID> players
    ) {
    }

    public static LevelData read(final Path dataDir, final Path levelDat) throws IOException {
        final CompoundBinaryTag root = BinaryTagIO.reader().readNamed(levelDat, BinaryTagIO.Compression.GZIP).getValue();
        final CompoundBinaryTag data = DataFixersRegistry.update(DataFixerType.LEVEL, root.getCompound("Data"), false);

        final LevelData l = new LevelData();
        l.dataVersion = VersionConstants.DATA_VERSION;
        l.levelName = data.getString("LevelName");
        l.time = data.getLong("Time");
        l.dayTime = data.getLong("DayTime");
        l.gameType = data.getInt("GameType");
        l.allowCommands = data.getBoolean("allowCommands");
        l.wasModded = data.getBoolean("WasModded");

        final CompoundBinaryTag version = data.getCompound("Version");
        if (version.contains("Name")) {
            l.versionName = version.getString("Name");
        }
        if (version.contains("Snapshot")) {
            l.snapshot = version.getBoolean("Snapshot");
        }
        for (final BinaryTag entry : data.getList("version_history")) {
            if (entry instanceof final IntBinaryTag tag) {
                l.versionHistory.add(tag.value());
            }
        }

        final CompoundBinaryTag dataPacks = data.getCompound("DataPacks");
        if (dataPacks.contains("Enabled")) {
            l.enabledPacks.clear();
            readStrings(dataPacks.getList("Enabled"), l.enabledPacks);
        }
        readStrings(dataPacks.getList("Disabled"), l.disabledPacks);

        if (data.contains("enabled_features")) {
            l.enabledFeatures.clear();
            readKeys(data.getList("enabled_features"), l.enabledFeatures);
        }

        final CompoundBinaryTag difficultySettings = data.getCompound("difficulty_settings");
        l.difficulty = parseDifficulty(difficultySettings.getString("difficulty"));
        l.hardcore = difficultySettings.getBoolean("hardcore");
        l.difficultyLocked = difficultySettings.getBoolean("locked");

        if (data.contains("singleplayer_uuid")) {
            final int[] uuid = data.getIntArray("singleplayer_uuid");
            if (uuid.length == 4) {
                l.singleplayerUuid = AnvilEntitySerializer.uuidFromInts(uuid);
            }
        }

        final CompoundBinaryTag spawn = data.getCompound("spawn");
        if (spawn.contains("dimension")) {
            l.spawnDimension = Key.key(spawn.getString("dimension"));
        }
        l.spawnYaw = spawn.getInt("yaw");
        l.spawnPitch = spawn.getInt("pitch");
        final int[] spawnPos = spawn.getIntArray("pos");
        if (spawnPos.length == 3) {
            l.spawnX = spawnPos[0];
            l.spawnY = spawnPos[1];
            l.spawnZ = spawnPos[2];
        }

        l.readWorldData(dataDir);
        return l;
    }

    private void readWorldData(final Path dataDir) throws IOException {
        final Optional<CompoundBinaryTag> rules = WorldDataFiles.read(dataDir.resolve(GAME_RULES_PATH));
        rules.ifPresent(gameRules::load);

        WorldDataFiles.read(dataDir.resolve(WEATHER_PATH)).ifPresent(weather::load);

        final Optional<CompoundBinaryTag> wgs = WorldDataFiles.read(dataDir.resolve(WORLD_GEN_SETTINGS_PATH));
        if (wgs.isPresent()) {
            worldGenSettings = wgs.get();
            seed = worldGenSettings.getLong("seed");
        }

        WorldDataFiles.read(dataDir.resolve(CUSTOM_BOSS_EVENTS_PATH)).ifPresent(this::readCustomBossEvents);
        WorldDataFiles.read(dataDir.resolve(WORLD_CLOCKS_PATH)).ifPresent(this::readWorldClocks);
    }

    private void readWorldClocks(final CompoundBinaryTag payload) {
        for (final BinaryTag entry : payload.getList(WORLD_CLOCKS)) {
            if (!(entry instanceof final CompoundBinaryTag clock)) continue;
            final String dimension = clock.getString("Dimension");
            if (dimension.isEmpty()) continue;
            worldTimes.put(Key.key(dimension), new WorldTime(
                    clock.getLong("WorldAge"),
                    clock.getLong("DayTime"),
                    !clock.contains("DoDaylightCycle") || clock.getBoolean("DoDaylightCycle")));
        }
    }

    private void readCustomBossEvents(final CompoundBinaryTag events) {
        for (final String id : events.keySet()) {
            if (!(events.get(id) instanceof final CompoundBinaryTag bar)) continue;

            final Set<UUID> players = new HashSet<>();
            for (final BinaryTag entry : bar.getList("Players")) {
                if (entry instanceof final IntArrayBinaryTag iat) {
                    players.add(AnvilEntitySerializer.uuidFromInts(iat.value()));
                }
            }

            final Set<BossBar.Flag> flags = new HashSet<>();
            if (bar.getBoolean("DarkenScreen")) flags.add(BossBar.Flag.DARKEN_SCREEN);
            if (bar.getBoolean("PlayBossMusic")) flags.add(BossBar.Flag.PLAY_BOSS_MUSIC);
            if (bar.getBoolean("CreateWorldFog")) flags.add(BossBar.Flag.CREATE_WORLD_FOG);

            final int max = bar.contains("Max") ? bar.getInt("Max") : 100;
            final int value = bar.getInt("Value");

            bossBars.put(Key.key(id), new BossBarData(
                    GsonComponentSerializer.gson().deserialize(bar.getString("Name")),
                    value,
                    max,
                    BossBar.Color.valueOf(bar.getString("Color").toUpperCase(Locale.ROOT)),
                    BossBar.Overlay.valueOf(bar.getString("Overlay").toUpperCase(Locale.ROOT)),
                    flags,
                    bar.getBoolean("Visible"),
                    players));
        }
    }

    public void write(final Path dataDir, final Path levelDat) throws IOException {
        writeLevelDat(levelDat);
        writeWorldData(dataDir);
    }

    private void writeLevelDat(final Path levelDat) throws IOException {
        Files.createDirectories(levelDat.getParent());

        final CompoundBinaryTag.Builder data = CompoundBinaryTag.builder();
        data.putInt("DataVersion", VersionConstants.DATA_VERSION);

        final CompoundBinaryTag.Builder version = CompoundBinaryTag.builder();
        version.putInt("Id", VersionConstants.DATA_VERSION);
        version.putString("Name", VersionConstants.MINECRAFT_VERSION_NAME);
        version.putString("Series", "main");
        version.putBoolean("Snapshot", !VersionConstants.IS_RELEASE);
        data.put("Version", version.build());

        if (versionHistory.isEmpty() || versionHistory.getLast() != dataVersion) {
            versionHistory.add(dataVersion);
        }
        final ListBinaryTag.Builder<BinaryTag> history = ListBinaryTag.builder();
        for (final int v : versionHistory) {
            history.add(IntBinaryTag.intBinaryTag(v));
        }
        data.put("version_history", history.build());

        data.putInt("version", 19133); // Anvil level format version
        data.putBoolean("initialized", true);
        data.putString("LevelName", levelName);
        data.putLong("Time", time);
        data.putLong("DayTime", dayTime);
        data.putLong("LastPlayed", System.currentTimeMillis());
        data.putBoolean("WasModded", wasModded);

        if (singleplayerUuid != null) {
            data.putIntArray("singleplayer_uuid", AnvilEntitySerializer.uuidToInts(singleplayerUuid));
        }

        final CompoundBinaryTag.Builder spawn = CompoundBinaryTag.builder();
        spawn.putString("dimension", spawnDimension.asString());
        spawn.putIntArray("pos", new int[] {spawnX, spawnY, spawnZ});
        spawn.putInt("yaw", spawnYaw);
        spawn.putInt("pitch", spawnPitch);
        data.put("spawn", spawn.build());

        data.putInt("GameType", gameType);

        final CompoundBinaryTag.Builder difficultySettings = CompoundBinaryTag.builder();
        difficultySettings.putString("difficulty", difficultyName(difficulty));
        difficultySettings.putBoolean("hardcore", hardcore);
        difficultySettings.putBoolean("locked", difficultyLocked);
        data.put("difficulty_settings", difficultySettings.build());

        data.putBoolean("allowCommands", allowCommands);

        final CompoundBinaryTag.Builder dataPacks = CompoundBinaryTag.builder();
        dataPacks.put("Enabled", stringList(enabledPacks));
        dataPacks.put("Disabled", stringList(disabledPacks));
        data.put("DataPacks", dataPacks.build());

        if (!enabledFeatures.equals(DEFAULT_FEATURES)) {
            data.put("enabled_features", keyList(enabledFeatures));
        }

        data.put("ServerBrands", ListBinaryTag.builder().add(StringBinaryTag.stringBinaryTag("Fidorial")).build());

        final CompoundBinaryTag root = CompoundBinaryTag.builder().put("Data", data.build()).build();

        BinaryTagIO.writer().writeNamed(Map.entry("", root), levelDat, BinaryTagIO.Compression.GZIP);
    }

    private void writeWorldData(final Path dataDir) throws IOException {
        final CompoundBinaryTag.Builder rules = CompoundBinaryTag.builder();
        gameRules.save(rules);
        WorldDataFiles.write(dataDir.resolve(GAME_RULES_PATH), rules.build());

        final CompoundBinaryTag.Builder weatherData = CompoundBinaryTag.builder();
        weather.save(weatherData);
        WorldDataFiles.write(dataDir.resolve(WEATHER_PATH), weatherData.build());

        WorldDataFiles.write(dataDir.resolve(WORLD_GEN_SETTINGS_PATH), buildWorldGenSettings());
        WorldDataFiles.write(dataDir.resolve(CUSTOM_BOSS_EVENTS_PATH), buildCustomBossEvents());
        WorldDataFiles.write(dataDir.resolve(WORLD_CLOCKS_PATH), buildWorldClocks());
    }

    public void applyInitialPacks(final List<Key> packs) {
        enabledPacks.clear();
        enabledFeatures.clear();
        enabledFeatures.add(Key.key("vanilla"));
        for (final Key pack : packs) {
            final boolean vanillaNamespace = Key.MINECRAFT_NAMESPACE.equals(pack.namespace());
            enabledPacks.add(vanillaNamespace ? pack.value() : pack.asString());
            // To be revisited: custom datapacks defining features in their pack.mcmeta
            if (vanillaNamespace && !enabledFeatures.contains(pack)) {
                enabledFeatures.add(pack);
            }
        }
    }

    private CompoundBinaryTag buildWorldClocks() {
        final ListBinaryTag.Builder<BinaryTag> clocks = ListBinaryTag.builder();
        for (final Map.Entry<Key, WorldTime> entry : worldTimes.entrySet()) {
            final WorldTime value = entry.getValue();
            clocks.add(CompoundBinaryTag.builder()
                    .putString("Dimension", entry.getKey().asString())
                    .putLong("WorldAge", value.worldAge())
                    .putLong("DayTime", value.dayTime())
                    .putBoolean("DoDaylightCycle", value.doDaylightCycle())
                    .build());
        }
        return CompoundBinaryTag.builder().put(WORLD_CLOCKS, clocks.build()).build();
    }

    // this is the framework for the built-in /bossbars command, which DOES persist, unlike plugin ones
    private CompoundBinaryTag buildCustomBossEvents() {
        final CompoundBinaryTag.Builder root = CompoundBinaryTag.builder();
        for (final Map.Entry<Key, BossBarData> entry : bossBars.entrySet()) {
            final BossBarData value = entry.getValue();
            final CompoundBinaryTag.Builder bar = CompoundBinaryTag.builder();

            final ListBinaryTag.Builder<BinaryTag> players = ListBinaryTag.builder();
            for (final UUID uuid : value.players()) {
                players.add(IntArrayBinaryTag.intArrayBinaryTag(AnvilEntitySerializer.uuidToInts(uuid)));
            }
            bar.put("Players", players.build());

            bar.putString("Color", value.color().name().toLowerCase(Locale.ROOT));
            bar.putString("Overlay", value.overlay().name().toLowerCase(Locale.ROOT));
            bar.putBoolean("CreateWorldFog", value.flags().contains(BossBar.Flag.CREATE_WORLD_FOG));
            bar.putBoolean("DarkenScreen", value.flags().contains(BossBar.Flag.DARKEN_SCREEN));
            bar.putBoolean("PlayBossMusic", value.flags().contains(BossBar.Flag.PLAY_BOSS_MUSIC));
            bar.putInt("Max", value.max());
            bar.putInt("Value", value.value());
            bar.putString("Name", GsonComponentSerializer.gson().serialize(value.name()));
            bar.putBoolean("Visible", value.visible());

            root.put(entry.getKey().asString(), bar.build());
        }
        return root.build();
    }

    private CompoundBinaryTag buildWorldGenSettings() {
        final CompoundBinaryTag.Builder wgs = CompoundBinaryTag.builder().put(worldGenSettings);
        wgs.putLong("seed", seed);
        if (!worldGenSettings.contains("generate_structures")) {
            wgs.putBoolean("generate_structures", true);
        }
        if (!worldGenSettings.contains("bonus_chest")) {
            wgs.putBoolean("bonus_chest", false);
        }

        final CompoundBinaryTag.Builder dimensions = CompoundBinaryTag.builder().put(worldGenSettings.getCompound(DIMENSIONS));
        for (final Map.Entry<Key, DimensionGenerator> entry : generators.entrySet()) {
            dimensions.put(entry.getKey().asString(), buildDimension(entry.getValue()));
        }
        wgs.put(DIMENSIONS, dimensions.build());
        return wgs.build();
    }

    private CompoundBinaryTag buildDimension(final DimensionGenerator generator) {
        return CompoundBinaryTag.builder()
                .putString("type", generator.dimension().dimensionType().asString())
                .put("generator", buildGeneratorSettings(generator.config()))
                .build();
    }

    private CompoundBinaryTag buildGeneratorSettings(final ChunkGeneratorConfig config) {
        return switch (config) {
            case ChunkGeneratorConfig.Flat(final BlockState floor, final int floorThickness, final Key biome) -> {
                final CompoundBinaryTag layer = CompoundBinaryTag.builder()
                        .putInt("height", floorThickness)
                        .putString("block", floor.name().asString())
                        .build();
                final CompoundBinaryTag settings = CompoundBinaryTag.builder()
                        .putBoolean("features", false)
                        .putBoolean("lakes", false)
                        .putString("biome", biome.asString())
                        .put("layers", ListBinaryTag.builder().add(layer).build())
                        .put("structure_overrides", ListBinaryTag.empty())
                        .build();
                yield CompoundBinaryTag.builder()
                        .putString("type", "minecraft:flat")
                        .put("settings", settings)
                        .build();
            }

            case ChunkGeneratorConfig.Debug _ -> CompoundBinaryTag.builder()
                    .putString("type", "minecraft:debug")
                    .build();

            case ChunkGeneratorConfig.Noise(final Key settings, final Key biomeSourcePreset) -> CompoundBinaryTag.builder()
                    .putString("type", "minecraft:noise")
                    .putString("settings", settings.asString())
                    .put("biome_source", CompoundBinaryTag.builder()
                            .putString("type", "minecraft:multi_noise")
                            .putString("preset", biomeSourcePreset.asString())
                            .build())
                    .build();
        };
    }

    private static int parseDifficulty(final String name) {
        final int id = DIFFICULTIES.indexOf(name);
        return id < 0 ? 2 : id;
    }

    private static String difficultyName(final int id) {
        return id >= 0 && id < DIFFICULTIES.size() ? DIFFICULTIES.get(id) : "normal";
    }

    public boolean exists(final Path levelDat) {
        return Files.isRegularFile(levelDat);
    }

    private static void readStrings(final ListBinaryTag list, final List<String> out) {
        for (final BinaryTag entry : list) {
            if (entry instanceof final StringBinaryTag string) {
                out.add(string.value());
            }
        }
    }

    private static void readKeys(final ListBinaryTag list, final List<Key> out) {
        for (final BinaryTag entry : list) {
            if (entry instanceof final StringBinaryTag string) {
                out.add(Key.key(string.value()));
            }
        }
    }

    private static ListBinaryTag stringList(final List<String> values) {
        final ListBinaryTag.Builder<BinaryTag> builder = ListBinaryTag.builder();
        for (final String value : values) {
            builder.add(StringBinaryTag.stringBinaryTag(value));
        }
        return builder.build();
    }

    private static ListBinaryTag keyList(final List<Key> values) {
        final ListBinaryTag.Builder<BinaryTag> builder = ListBinaryTag.builder();
        for (final Key value : values) {
            builder.add(StringBinaryTag.stringBinaryTag(value.asString()));
        }
        return builder.build();
    }

    public record DimensionGenerator(Dimension dimension, ChunkGeneratorConfig config) {
    }
}
