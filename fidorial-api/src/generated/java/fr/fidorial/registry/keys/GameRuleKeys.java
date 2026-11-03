package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.GameRule;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:game_rule} registry.
 */
public final class GameRuleKeys {
    /**
     * Key for {@code minecraft:advance_time}.
     */
    public static final TypedKey<GameRule> ADVANCE_TIME = create("advance_time");

    /**
     * Key for {@code minecraft:advance_weather}.
     */
    public static final TypedKey<GameRule> ADVANCE_WEATHER = create("advance_weather");

    /**
     * Key for {@code minecraft:allow_entering_nether_using_portals}.
     */
    public static final TypedKey<GameRule> ALLOW_ENTERING_NETHER_USING_PORTALS = create("allow_entering_nether_using_portals");

    /**
     * Key for {@code minecraft:block_drops}.
     */
    public static final TypedKey<GameRule> BLOCK_DROPS = create("block_drops");

    /**
     * Key for {@code minecraft:block_explosion_drop_decay}.
     */
    public static final TypedKey<GameRule> BLOCK_EXPLOSION_DROP_DECAY = create("block_explosion_drop_decay");

    /**
     * Key for {@code minecraft:command_block_output}.
     */
    public static final TypedKey<GameRule> COMMAND_BLOCK_OUTPUT = create("command_block_output");

    /**
     * Key for {@code minecraft:command_blocks_work}.
     */
    public static final TypedKey<GameRule> COMMAND_BLOCKS_WORK = create("command_blocks_work");

    /**
     * Key for {@code minecraft:drowning_damage}.
     */
    public static final TypedKey<GameRule> DROWNING_DAMAGE = create("drowning_damage");

    /**
     * Key for {@code minecraft:elytra_movement_check}.
     */
    public static final TypedKey<GameRule> ELYTRA_MOVEMENT_CHECK = create("elytra_movement_check");

    /**
     * Key for {@code minecraft:ender_pearls_vanish_on_death}.
     */
    public static final TypedKey<GameRule> ENDER_PEARLS_VANISH_ON_DEATH = create("ender_pearls_vanish_on_death");

    /**
     * Key for {@code minecraft:entity_drops}.
     */
    public static final TypedKey<GameRule> ENTITY_DROPS = create("entity_drops");

    /**
     * Key for {@code minecraft:fall_damage}.
     */
    public static final TypedKey<GameRule> FALL_DAMAGE = create("fall_damage");

    /**
     * Key for {@code minecraft:fire_damage}.
     */
    public static final TypedKey<GameRule> FIRE_DAMAGE = create("fire_damage");

    /**
     * Key for {@code minecraft:fire_spread_radius_around_player}.
     */
    public static final TypedKey<GameRule> FIRE_SPREAD_RADIUS_AROUND_PLAYER = create("fire_spread_radius_around_player");

    /**
     * Key for {@code minecraft:forgive_dead_players}.
     */
    public static final TypedKey<GameRule> FORGIVE_DEAD_PLAYERS = create("forgive_dead_players");

    /**
     * Key for {@code minecraft:freeze_damage}.
     */
    public static final TypedKey<GameRule> FREEZE_DAMAGE = create("freeze_damage");

    /**
     * Key for {@code minecraft:global_sound_events}.
     */
    public static final TypedKey<GameRule> GLOBAL_SOUND_EVENTS = create("global_sound_events");

    /**
     * Key for {@code minecraft:immediate_respawn}.
     */
    public static final TypedKey<GameRule> IMMEDIATE_RESPAWN = create("immediate_respawn");

    /**
     * Key for {@code minecraft:keep_inventory}.
     */
    public static final TypedKey<GameRule> KEEP_INVENTORY = create("keep_inventory");

    /**
     * Key for {@code minecraft:lava_source_conversion}.
     */
    public static final TypedKey<GameRule> LAVA_SOURCE_CONVERSION = create("lava_source_conversion");

    /**
     * Key for {@code minecraft:limited_crafting}.
     */
    public static final TypedKey<GameRule> LIMITED_CRAFTING = create("limited_crafting");

    /**
     * Key for {@code minecraft:locator_bar}.
     */
    public static final TypedKey<GameRule> LOCATOR_BAR = create("locator_bar");

    /**
     * Key for {@code minecraft:log_admin_commands}.
     */
    public static final TypedKey<GameRule> LOG_ADMIN_COMMANDS = create("log_admin_commands");

    /**
     * Key for {@code minecraft:max_block_modifications}.
     */
    public static final TypedKey<GameRule> MAX_BLOCK_MODIFICATIONS = create("max_block_modifications");

    /**
     * Key for {@code minecraft:max_command_forks}.
     */
    public static final TypedKey<GameRule> MAX_COMMAND_FORKS = create("max_command_forks");

    /**
     * Key for {@code minecraft:max_command_sequence_length}.
     */
    public static final TypedKey<GameRule> MAX_COMMAND_SEQUENCE_LENGTH = create("max_command_sequence_length");

    /**
     * Key for {@code minecraft:max_entity_cramming}.
     */
    public static final TypedKey<GameRule> MAX_ENTITY_CRAMMING = create("max_entity_cramming");

    /**
     * Key for {@code minecraft:max_minecart_speed}.
     */
    public static final TypedKey<GameRule> MAX_MINECART_SPEED = create("max_minecart_speed");

    /**
     * Key for {@code minecraft:max_snow_accumulation_height}.
     */
    public static final TypedKey<GameRule> MAX_SNOW_ACCUMULATION_HEIGHT = create("max_snow_accumulation_height");

    /**
     * Key for {@code minecraft:mob_drops}.
     */
    public static final TypedKey<GameRule> MOB_DROPS = create("mob_drops");

    /**
     * Key for {@code minecraft:mob_explosion_drop_decay}.
     */
    public static final TypedKey<GameRule> MOB_EXPLOSION_DROP_DECAY = create("mob_explosion_drop_decay");

    /**
     * Key for {@code minecraft:mob_griefing}.
     */
    public static final TypedKey<GameRule> MOB_GRIEFING = create("mob_griefing");

    /**
     * Key for {@code minecraft:natural_health_regeneration}.
     */
    public static final TypedKey<GameRule> NATURAL_HEALTH_REGENERATION = create("natural_health_regeneration");

    /**
     * Key for {@code minecraft:player_movement_check}.
     */
    public static final TypedKey<GameRule> PLAYER_MOVEMENT_CHECK = create("player_movement_check");

    /**
     * Key for {@code minecraft:players_nether_portal_creative_delay}.
     */
    public static final TypedKey<GameRule> PLAYERS_NETHER_PORTAL_CREATIVE_DELAY = create("players_nether_portal_creative_delay");

    /**
     * Key for {@code minecraft:players_nether_portal_default_delay}.
     */
    public static final TypedKey<GameRule> PLAYERS_NETHER_PORTAL_DEFAULT_DELAY = create("players_nether_portal_default_delay");

    /**
     * Key for {@code minecraft:players_sleeping_percentage}.
     */
    public static final TypedKey<GameRule> PLAYERS_SLEEPING_PERCENTAGE = create("players_sleeping_percentage");

    /**
     * Key for {@code minecraft:projectiles_can_break_blocks}.
     */
    public static final TypedKey<GameRule> PROJECTILES_CAN_BREAK_BLOCKS = create("projectiles_can_break_blocks");

    /**
     * Key for {@code minecraft:pvp}.
     */
    public static final TypedKey<GameRule> PVP = create("pvp");

    /**
     * Key for {@code minecraft:raids}.
     */
    public static final TypedKey<GameRule> RAIDS = create("raids");

    /**
     * Key for {@code minecraft:random_tick_speed}.
     */
    public static final TypedKey<GameRule> RANDOM_TICK_SPEED = create("random_tick_speed");

    /**
     * Key for {@code minecraft:reduced_debug_info}.
     */
    public static final TypedKey<GameRule> REDUCED_DEBUG_INFO = create("reduced_debug_info");

    /**
     * Key for {@code minecraft:respawn_radius}.
     */
    public static final TypedKey<GameRule> RESPAWN_RADIUS = create("respawn_radius");

    /**
     * Key for {@code minecraft:send_command_feedback}.
     */
    public static final TypedKey<GameRule> SEND_COMMAND_FEEDBACK = create("send_command_feedback");

    /**
     * Key for {@code minecraft:show_advancement_messages}.
     */
    public static final TypedKey<GameRule> SHOW_ADVANCEMENT_MESSAGES = create("show_advancement_messages");

    /**
     * Key for {@code minecraft:show_death_messages}.
     */
    public static final TypedKey<GameRule> SHOW_DEATH_MESSAGES = create("show_death_messages");

    /**
     * Key for {@code minecraft:spawn_mobs}.
     */
    public static final TypedKey<GameRule> SPAWN_MOBS = create("spawn_mobs");

    /**
     * Key for {@code minecraft:spawn_monsters}.
     */
    public static final TypedKey<GameRule> SPAWN_MONSTERS = create("spawn_monsters");

    /**
     * Key for {@code minecraft:spawn_patrols}.
     */
    public static final TypedKey<GameRule> SPAWN_PATROLS = create("spawn_patrols");

    /**
     * Key for {@code minecraft:spawn_phantoms}.
     */
    public static final TypedKey<GameRule> SPAWN_PHANTOMS = create("spawn_phantoms");

    /**
     * Key for {@code minecraft:spawn_wandering_traders}.
     */
    public static final TypedKey<GameRule> SPAWN_WANDERING_TRADERS = create("spawn_wandering_traders");

    /**
     * Key for {@code minecraft:spawn_wardens}.
     */
    public static final TypedKey<GameRule> SPAWN_WARDENS = create("spawn_wardens");

    /**
     * Key for {@code minecraft:spawner_blocks_work}.
     */
    public static final TypedKey<GameRule> SPAWNER_BLOCKS_WORK = create("spawner_blocks_work");

    /**
     * Key for {@code minecraft:spectators_generate_chunks}.
     */
    public static final TypedKey<GameRule> SPECTATORS_GENERATE_CHUNKS = create("spectators_generate_chunks");

    /**
     * Key for {@code minecraft:spread_vines}.
     */
    public static final TypedKey<GameRule> SPREAD_VINES = create("spread_vines");

    /**
     * Key for {@code minecraft:tnt_explodes}.
     */
    public static final TypedKey<GameRule> TNT_EXPLODES = create("tnt_explodes");

    /**
     * Key for {@code minecraft:tnt_explosion_drop_decay}.
     */
    public static final TypedKey<GameRule> TNT_EXPLOSION_DROP_DECAY = create("tnt_explosion_drop_decay");

    /**
     * Key for {@code minecraft:universal_anger}.
     */
    public static final TypedKey<GameRule> UNIVERSAL_ANGER = create("universal_anger");

    /**
     * Key for {@code minecraft:water_source_conversion}.
     */
    public static final TypedKey<GameRule> WATER_SOURCE_CONVERSION = create("water_source_conversion");

    private static final List<TypedKey<GameRule>> VALUES = List.of(
        ADVANCE_TIME,
        ADVANCE_WEATHER,
        ALLOW_ENTERING_NETHER_USING_PORTALS,
        BLOCK_DROPS,
        BLOCK_EXPLOSION_DROP_DECAY,
        COMMAND_BLOCK_OUTPUT,
        COMMAND_BLOCKS_WORK,
        DROWNING_DAMAGE,
        ELYTRA_MOVEMENT_CHECK,
        ENDER_PEARLS_VANISH_ON_DEATH,
        ENTITY_DROPS,
        FALL_DAMAGE,
        FIRE_DAMAGE,
        FIRE_SPREAD_RADIUS_AROUND_PLAYER,
        FORGIVE_DEAD_PLAYERS,
        FREEZE_DAMAGE,
        GLOBAL_SOUND_EVENTS,
        IMMEDIATE_RESPAWN,
        KEEP_INVENTORY,
        LAVA_SOURCE_CONVERSION,
        LIMITED_CRAFTING,
        LOCATOR_BAR,
        LOG_ADMIN_COMMANDS,
        MAX_BLOCK_MODIFICATIONS,
        MAX_COMMAND_FORKS,
        MAX_COMMAND_SEQUENCE_LENGTH,
        MAX_ENTITY_CRAMMING,
        MAX_MINECART_SPEED,
        MAX_SNOW_ACCUMULATION_HEIGHT,
        MOB_DROPS,
        MOB_EXPLOSION_DROP_DECAY,
        MOB_GRIEFING,
        NATURAL_HEALTH_REGENERATION,
        PLAYER_MOVEMENT_CHECK,
        PLAYERS_NETHER_PORTAL_CREATIVE_DELAY,
        PLAYERS_NETHER_PORTAL_DEFAULT_DELAY,
        PLAYERS_SLEEPING_PERCENTAGE,
        PROJECTILES_CAN_BREAK_BLOCKS,
        PVP,
        RAIDS,
        RANDOM_TICK_SPEED,
        REDUCED_DEBUG_INFO,
        RESPAWN_RADIUS,
        SEND_COMMAND_FEEDBACK,
        SHOW_ADVANCEMENT_MESSAGES,
        SHOW_DEATH_MESSAGES,
        SPAWN_MOBS,
        SPAWN_MONSTERS,
        SPAWN_PATROLS,
        SPAWN_PHANTOMS,
        SPAWN_WANDERING_TRADERS,
        SPAWN_WARDENS,
        SPAWNER_BLOCKS_WORK,
        SPECTATORS_GENERATE_CHUNKS,
        SPREAD_VINES,
        TNT_EXPLODES,
        TNT_EXPLOSION_DROP_DECAY,
        UNIVERSAL_ANGER,
        WATER_SOURCE_CONVERSION
    );

    private GameRuleKeys() {
        throw new UnsupportedOperationException("GameRuleKeys cannot be instantiated.");
    }

    private static TypedKey<GameRule> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.GAME_RULE, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<GameRule>> values() {
        return VALUES.stream();
    }
}
