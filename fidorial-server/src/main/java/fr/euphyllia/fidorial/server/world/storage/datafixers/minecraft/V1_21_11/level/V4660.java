package fr.euphyllia.fidorial.server.world.storage.datafixers.minecraft.V1_21_11.level;

import ca.spottedleaf.converter.DataConverter;
import ca.spottedleaf.converter.types.MapType;
import ca.spottedleaf.converter.util.RenameHelper;

import java.util.List;

/**
 * This collapses two datafixers related to game rules:
 * <p>
 * - 25w44a: game rules have been moved into the {@code minecraft:game_rule} registry. The level's {@code GameRules}
 * compound becomes {@code game_rules}, camelCase string values become registry keys holding typed
 * values, and integer rules are clamped to the new vanilla bounds.
 * <p>
 * - 25w45a: max_entity_cramming min bound has been changed from 1 to 0 compared to 25w44a
 */
public final class V4660 extends DataConverter<MapType, MapType> {

    private static final int VERSION = 4660;
    private static final int MAX = Integer.MAX_VALUE;

    private static final String LEGACY_GAME_RULES = "GameRules";
    private static final String GAME_RULES = "game_rules";

    private static final String DO_FIRE_TICK = "doFireTick";
    private static final String ALLOW_FIRE_TICKS_AWAY_FROM_PLAYER = "allowFireTicksAwayFromPlayer";
    private static final String FIRE_SPREAD_RADIUS = "minecraft:fire_spread_radius_around_player";

    private static final List<String> REMOVED = List.of(
            "spawnChunkRadius", // 1.21.9
            "entitiesWithPassengersCanUsePortals", // 1.21-pre1
            "gameLoopFunction"); // 17w49b

    private static final List<Rule> RULES = List.of(
            new Bool("allowEnteringNetherUsingPortals", "minecraft:allow_entering_nether_using_portals"),
            new Bool("announceAdvancements", "minecraft:show_advancement_messages"),
            new Bool("blockExplosionDropDecay", "minecraft:block_explosion_drop_decay"),
            new Bool("commandBlockOutput", "minecraft:command_block_output"),
            new Bool("enableCommandBlocks", "minecraft:command_blocks_work"),
            new Bool("commandBlocksEnabled", "minecraft:command_blocks_work"),
            new Bounded("commandModificationBlockLimit", "minecraft:max_block_modifications", 1, MAX),
            new Inverted("disableElytraMovementCheck", "minecraft:elytra_movement_check"),
            new Inverted("disablePlayerMovementCheck", "minecraft:player_movement_check"),
            new Inverted("disableRaids", "minecraft:raids"),
            new Bool("doDaylightCycle", "minecraft:advance_time"),
            new Bool("doEntityDrops", "minecraft:entity_drops"),
            new Bool("doImmediateRespawn", "minecraft:immediate_respawn"),
            new Bool("doInsomnia", "minecraft:spawn_phantoms"),
            new Bool("doLimitedCrafting", "minecraft:limited_crafting"),
            new Bool("doMobLoot", "minecraft:mob_drops"),
            new Bool("doMobSpawning", "minecraft:spawn_mobs"),
            new Bool("doPatrolSpawning", "minecraft:spawn_patrols"),
            new Bool("doTileDrops", "minecraft:block_drops"),
            new Bool("doTraderSpawning", "minecraft:spawn_wandering_traders"),
            new Bool("doVinesSpread", "minecraft:spread_vines"),
            new Bool("doWardenSpawning", "minecraft:spawn_wardens"),
            new Bool("doWeatherCycle", "minecraft:advance_weather"),
            new Bool("drowningDamage", "minecraft:drowning_damage"),
            new Bool("enderPearlsVanishOnDeath", "minecraft:ender_pearls_vanish_on_death"),
            new Bool("fallDamage", "minecraft:fall_damage"),
            new Bool("fireDamage", "minecraft:fire_damage"),
            new Bool("forgiveDeadPlayers", "minecraft:forgive_dead_players"),
            new Bool("freezeDamage", "minecraft:freeze_damage"),
            new Bool("globalSoundEvents", "minecraft:global_sound_events"),
            new Bool("keepInventory", "minecraft:keep_inventory"),
            new Bool("lavaSourceConversion", "minecraft:lava_source_conversion"),
            new Bool("locatorBar", "minecraft:locator_bar"),
            new Bool("logAdminCommands", "minecraft:log_admin_commands"),
            new Bounded("maxCommandChainLength", "minecraft:max_command_sequence_length", 0, MAX),
            new Bounded("maxCommandForkCount", "minecraft:max_command_forks", 0, MAX),
            new Bounded("maxEntityCramming", "minecraft:max_entity_cramming", 0, MAX),
            new Bounded("minecartMaxSpeed", "minecraft:max_minecart_speed", 1, 1_000),
            new Bool("mobExplosionDropDecay", "minecraft:mob_explosion_drop_decay"),
            new Bool("mobGriefing", "minecraft:mob_griefing"),
            new Bool("naturalRegeneration", "minecraft:natural_health_regeneration"),
            new Bounded("playersNetherPortalCreativeDelay", "minecraft:players_nether_portal_creative_delay", 0, MAX),
            new Bounded("playersNetherPortalDefaultDelay", "minecraft:players_nether_portal_default_delay", 0, MAX),
            new Bounded("playersSleepingPercentage", "minecraft:players_sleeping_percentage", 0, MAX),
            new Bool("projectilesCanBreakBlocks", "minecraft:projectiles_can_break_blocks"),
            new Bool("pvp", "minecraft:pvp"),
            new Bounded("randomTickSpeed", "minecraft:random_tick_speed", 0, MAX),
            new Bool("reducedDebugInfo", "minecraft:reduced_debug_info"),
            new Bool("sendCommandFeedback", "minecraft:send_command_feedback"),
            new Bool("showDeathMessages", "minecraft:show_death_messages"),
            new Bounded("snowAccumulationHeight", "minecraft:max_snow_accumulation_height", 0, 8),
            new Bool("spawnMonsters", "minecraft:spawn_monsters"),
            new Bounded("spawnRadius", "minecraft:respawn_radius", 0, MAX),
            new Bool("spawnerBlocksEnabled", "minecraft:spawner_blocks_work"),
            new Bool("spectatorsGenerateChunks", "minecraft:spectators_generate_chunks"),
            new Bool("tntExplodes", "minecraft:tnt_explodes"),
            new Bool("tntExplosionDropDecay", "minecraft:tnt_explosion_drop_decay"),
            new Bool("universalAnger", "minecraft:universal_anger"),
            new Bool("waterSourceConversion", "minecraft:water_source_conversion"));

    public V4660() {
        super(VERSION);
    }

    @Override
    public MapType convert(final MapType data, final long sourceVersion, final long toVersion) {
        RenameHelper.renameSingle(data, LEGACY_GAME_RULES, GAME_RULES);

        final MapType rules = data.getMap(GAME_RULES, null);
        if (rules == null) {
            return data;
        }

        convertFireTick(rules);
        for (final String removed : REMOVED) {
            rules.remove(removed);
        }

        for (final Rule rule : RULES) {
            if (!rules.hasKey(rule.from())) {
                continue;
            }
            final String raw = rules.getString(rule.from(), "");
            rules.remove(rule.from());
            switch (rule) {
                case Bool(_, final String to) -> rules.setBoolean(to, Boolean.parseBoolean(raw));
                case Inverted(_, final String to) -> rules.setBoolean(to, !Boolean.parseBoolean(raw));
                case Bounded(_, final String to, final int min, final int max) -> setBounded(rules, to, raw, min, max);
            }
        }
        data.setMap(GAME_RULES, rules);
        return data;
    }

    private static void convertFireTick(final MapType rules) {
        final boolean doFireTick = Boolean.parseBoolean(rules.getString(DO_FIRE_TICK, "true"));
        final boolean awayFromPlayer = Boolean.parseBoolean(rules.getString(ALLOW_FIRE_TICKS_AWAY_FROM_PLAYER, "false"));
        rules.remove(DO_FIRE_TICK);
        rules.remove(ALLOW_FIRE_TICKS_AWAY_FROM_PLAYER);

        if (!doFireTick) {
            rules.setInt(FIRE_SPREAD_RADIUS, 0);
        } else if (awayFromPlayer) {
            rules.setInt(FIRE_SPREAD_RADIUS, -1);
        }
    }

    private static void setBounded(final MapType rules, final String to, final String raw, final int min, final int max) {
        try {
            rules.setInt(to, Math.clamp(Integer.parseInt(raw), min, max));
        } catch (final NumberFormatException e) {
            rules.setString(to, raw);
        }
    }

    private sealed interface Rule permits Bool, Inverted, Bounded {
        String from();
    }

    private record Bool(String from, String to) implements Rule {
    }

    private record Inverted(String from, String to) implements Rule {
    }

    private record Bounded(String from, String to, int min, int max) implements Rule {
    }
}
