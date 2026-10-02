package fr.euphyllia.fidorial.server.entity.player.storage;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.VersionConstants;
import fr.fidorial.entity.GameMode;
import fr.fidorial.math.Location;
import fr.fidorial.storage.player.PlayerDataStorage;
import fr.fidorial.world.World;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class NbtPlayerDataStorage implements PlayerDataStorage {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(NbtPlayerDataStorage.class);
    private static final String ROOT_NAME = "PlayerData";

    // https://minecraft.wiki/w/Player.dat_format
    private static final String SPAWN_DIMENSION = "SpawnDimension";
    private static final String SPAWN_X = "SpawnX";
    private static final String SPAWN_Y = "SpawnY";
    private static final String SPAWN_Z = "SpawnZ";
    private static final String SPAWN_ANGLE = "SpawnAngle";
    private static final String SPAWN_PITCH = "SpawnPitch";

    private static final String DIMENSION = "Dimension";
    private static final String POS = "Pos";
    private static final String ROTATION = "Rotation";

    private static final String GAMEMODE_TYPE = "playerGameType";

    private final Path dataDir;
    private final boolean gzip;

    public NbtPlayerDataStorage(final Path playerRoot, final boolean gzip) {
        this.dataDir = playerRoot.resolve("data");
        this.gzip = gzip;
    }

    private static byte[] gzip(final byte[] plain) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(plain.length);
        try (final GZIPOutputStream out = new GZIPOutputStream(baos)) {
            out.write(plain);
        }
        return baos.toByteArray();
    }

    private static byte[] gunzip(final byte[] compressed) throws IOException {
        try (final GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            return in.readAllBytes();
        }
    }

    private Path fileFor(final UUID uuid) {
        return dataDir.resolve(uuid.toString());
    }

    @Override
    public boolean exists(final UUID uuid) {
        return Files.isRegularFile(fileFor(uuid));
    }

    @Override
    @SuppressWarnings("PatternValidation")
    public PlayerData load(final UUID uuid) throws IOException {
        final FidorialServer server = FidorialServer.getInstance();
        final GameMode defaultGameMode = GameMode.SURVIVAL; // todo: reintroduce config option

        final Path file = fileFor(uuid);
        if (!Files.isRegularFile(file)) {
            return new PlayerData(defaultGameMode, null, null);
        }

        final CompoundBinaryTag root = tryReadNamed(file);

        final GameMode gameMode = GameMode.byId(root.getInt(GAMEMODE_TYPE, 0));

        Location respawnLocation = null;
        if (root.contains(SPAWN_X) && root.contains(SPAWN_Y) && root.contains(SPAWN_Z)) {

            final String dimension = root.getString(SPAWN_DIMENSION, null);
            final Key parsed = Key.parseable(dimension) ? Key.key(dimension) : null;
            final World world = parsed != null ? server.world(parsed).orElse(null) : null;

            if (world == null) {
                LOGGER.warn("Unknown respawn world '{}' for {}, spawn location used instead", dimension, uuid);
            } else {
                final double x = root.getDouble(SPAWN_X);
                final double y = root.getDouble(SPAWN_Y);
                final double z = root.getDouble(SPAWN_Z);
                final float yaw = root.getFloat(SPAWN_ANGLE, 0);
                final float pitch = root.getFloat(SPAWN_PITCH, 0);
                respawnLocation = Location.of(world, x, y, z, yaw, pitch);
            }
        }

        Location location = null;
        if (root.contains(DIMENSION) && root.contains(POS, BinaryTagTypes.LIST)) {

            final String dimension = root.getString(DIMENSION);
            final Key parsed = Key.parseable(dimension) ? Key.key(dimension) : null;
            final World world = parsed != null ? server.world(parsed).orElse(null) : null;
            final ListBinaryTag pos = root.getList(POS, BinaryTagTypes.DOUBLE);

            if (world == null) {
                LOGGER.warn("Unknown last-played dimension '{}' for {}, spawn location used instead", dimension, uuid);
            } else if (pos.size() < 3) {
                LOGGER.warn("Malformed last-played position for {}, spawn location used instead", uuid);
            } else {
                final ListBinaryTag rotation = root.getList(ROTATION);
                final var x = pos.getDouble(0);
                final var y = pos.getDouble(1);
                final var z = pos.getDouble(2);
                final var yaw = rotation.getFloat(0, 0);
                final var pitch = rotation.getFloat(1, 0);
                location = Location.of(world, x, y, z, yaw, pitch);
            }
        }

        return new PlayerData(gameMode != null ? gameMode : defaultGameMode, respawnLocation, location);
    }

    private CompoundBinaryTag tryReadNamed(final Path file) throws IOException {
        try {
            return BinaryTagIO.reader().readNamed(file, BinaryTagIO.Compression.GZIP).getValue();
        } catch (final IOException e) {
            return BinaryTagIO.reader().readNamed(file).getValue();
        }
    }

    @Override
    public void save(final UUID uuid, final PlayerData data) throws IOException {
        Files.createDirectories(dataDir);

        final CompoundBinaryTag.Builder root = CompoundBinaryTag.builder();
        root.putInt("DataVersion", VersionConstants.DATA_VERSION);
        root.putInt("playerGameModeId", data.gameMode().id());

        final Location respawnLocation = data.respawnLocation();
        if (respawnLocation != null) {
            root.putString(SPAWN_DIMENSION, respawnLocation.world().key().asString());
            root.putDouble(SPAWN_X, respawnLocation.x());
            root.putDouble(SPAWN_Y, respawnLocation.y());
            root.putDouble(SPAWN_Z, respawnLocation.z());
            root.putFloat(SPAWN_ANGLE, respawnLocation.yaw());
            root.putFloat(SPAWN_PITCH, respawnLocation.pitch());
        }

        final Location location = data.lastLocation();
        if (location != null) {
            root.putString(DIMENSION, location.world().key().asString());
            root.put(POS, doubleList(location.x(), location.y(), location.z()));
            root.put(ROTATION, floatList(location.yaw(), location.pitch()));
        }

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BinaryTagIO.writer().writeNamed(Map.entry(ROOT_NAME, root.build()), baos);
        byte[] bytes = baos.toByteArray();
        if (gzip) {
            bytes = gzip(bytes);
        }

        final Path file = fileFor(uuid);
        final Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.write(tmp, bytes);
        try {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (final IOException atomicFailure) {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        }
        LOGGER.debug("Data for {} saved ({} bytes{})", uuid, bytes.length, gzip ? ", gzip" : "");
    }

    public Path dataDir() {
        return dataDir;
    }

    private static ListBinaryTag doubleList(final double a, final double b, final double c) {
        return ListBinaryTag.builder()
                .add(DoubleBinaryTag.doubleBinaryTag(a))
                .add(DoubleBinaryTag.doubleBinaryTag(b))
                .add(DoubleBinaryTag.doubleBinaryTag(c))
                .build();
    }

    private static ListBinaryTag floatList(final float a, final float b) {
        return ListBinaryTag.builder()
                .add(FloatBinaryTag.floatBinaryTag(a))
                .add(FloatBinaryTag.floatBinaryTag(b))
                .build();
    }
}
