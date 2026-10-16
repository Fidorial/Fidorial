package fr.euphyllia.fidorial.server.world.storage.datafixers.fidorial.V26_4.player;

import ca.spottedleaf.converter.DataConverter;
import ca.spottedleaf.converter.types.MapType;
import org.jspecify.annotations.Nullable;

/**
 * Moves player data to the vanilla keys: {@code playerGameModeId} to {@code playerGameType}, and the flat
 * {@code Spawn*} keys into the {@code respawn} compound.
 */
public final class V5122_1 extends DataConverter<MapType, MapType> {

    public static final int TO_VERSION = 5122;

    private static final String[] LEGACY_SPAWN_KEYS = {
            "SpawnDimension", "SpawnX", "SpawnY", "SpawnZ", "SpawnAngle", "SpawnPitch", "SpawnForced"
    };

    public V5122_1() {
        super(TO_VERSION, 1);
    }

    @Override
    @Nullable public MapType convert(final @Nullable MapType player, final long sourceVersion, final long toVersion) {
        if (player == null) {
            return null;
        }

        if (player.hasKey("playerGameModeId")) {
            final Number id = player.getNumber("playerGameModeId", null);
            if (id != null && !player.hasKey("playerGameType")) {
                player.setInt("playerGameType", id.intValue());
            }
            player.remove("playerGameModeId");
        }

        if (!player.hasKey("respawn")) {
            final Number x = player.getNumber("SpawnX", null);
            final Number y = player.getNumber("SpawnY", null);
            final Number z = player.getNumber("SpawnZ", null);
            if (x != null && y != null && z != null) {
                final MapType respawn = player.createEmptyMap();
                respawn.setString("dimension", player.getString("SpawnDimension", "minecraft:overworld"));
                respawn.setInts("pos", new int[] {floor(x), floor(y), floor(z)});
                respawn.setFloat("yaw", floatOf(player, "SpawnAngle"));
                respawn.setFloat("pitch", floatOf(player, "SpawnPitch"));
                respawn.setBoolean("forced", player.getBoolean("SpawnForced", false));
                player.setMap("respawn", respawn);
            }
        }

        for (final String key : LEGACY_SPAWN_KEYS) {
            player.remove(key);
        }
        return player;
    }

    private static int floor(final Number value) {
        return (int) Math.floor(value.doubleValue());
    }

    private static float floatOf(final MapType map, final String key) {
        final Number value = map.getNumber(key, null);
        return value == null ? 0f : value.floatValue();
    }
}
