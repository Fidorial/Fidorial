package fr.euphyllia.fidorial.server.network.protocol.catalog;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.kyori.adventure.key.Key;

/**
 * Network IDs for the {@code play/clientbound} state/direction.
 *
 * <p>Generated from Mojang's packets report; do not edit.</p>
 */
public interface PlayClientboundPacketIds {
    /**
     * Returned by {@link #id(Key)} when the identifier is unknown.
     */
    int UNKNOWN = -1;

    /**
     * {@code minecraft:add_entity}
     */
    int ADD_ENTITY_ID = 1;

    /**
     * {@code minecraft:add_entity_motion}
     */
    int ADD_ENTITY_MOTION_ID = 2;

    /**
     * {@code minecraft:add_transient_block}
     */
    int ADD_TRANSIENT_BLOCK_ID = 38;

    /**
     * {@code minecraft:animate}
     */
    int ANIMATE_ID = 3;

    /**
     * {@code minecraft:award_stats}
     */
    int AWARD_STATS_ID = 4;

    /**
     * {@code minecraft:block_changed_ack}
     */
    int BLOCK_CHANGED_ACK_ID = 5;

    /**
     * {@code minecraft:block_destruction}
     */
    int BLOCK_DESTRUCTION_ID = 6;

    /**
     * {@code minecraft:block_entity_data}
     */
    int BLOCK_ENTITY_DATA_ID = 7;

    /**
     * {@code minecraft:block_event}
     */
    int BLOCK_EVENT_ID = 8;

    /**
     * {@code minecraft:block_update}
     */
    int BLOCK_UPDATE_ID = 9;

    /**
     * {@code minecraft:boss_event}
     */
    int BOSS_EVENT_ID = 10;

    /**
     * {@code minecraft:bundle_delimiter}
     */
    int BUNDLE_DELIMITER_ID = 0;

    /**
     * {@code minecraft:change_difficulty}
     */
    int CHANGE_DIFFICULTY_ID = 11;

    /**
     * {@code minecraft:chunk_batch_finished}
     */
    int CHUNK_BATCH_FINISHED_ID = 12;

    /**
     * {@code minecraft:chunk_batch_start}
     */
    int CHUNK_BATCH_START_ID = 13;

    /**
     * {@code minecraft:chunks_biomes}
     */
    int CHUNKS_BIOMES_ID = 14;

    /**
     * {@code minecraft:clear_dialog}
     */
    int CLEAR_DIALOG_ID = 143;

    /**
     * {@code minecraft:clear_titles}
     */
    int CLEAR_TITLES_ID = 15;

    /**
     * {@code minecraft:command_suggestions}
     */
    int COMMAND_SUGGESTIONS_ID = 16;

    /**
     * {@code minecraft:commands}
     */
    int COMMANDS_ID = 17;

    /**
     * {@code minecraft:container_close}
     */
    int CONTAINER_CLOSE_ID = 18;

    /**
     * {@code minecraft:container_set_content}
     */
    int CONTAINER_SET_CONTENT_ID = 19;

    /**
     * {@code minecraft:container_set_data}
     */
    int CONTAINER_SET_DATA_ID = 20;

    /**
     * {@code minecraft:container_set_slot}
     */
    int CONTAINER_SET_SLOT_ID = 21;

    /**
     * {@code minecraft:cookie_request}
     */
    int COOKIE_REQUEST_ID = 22;

    /**
     * {@code minecraft:cooldown}
     */
    int COOLDOWN_ID = 23;

    /**
     * {@code minecraft:custom_chat_completions}
     */
    int CUSTOM_CHAT_COMPLETIONS_ID = 24;

    /**
     * {@code minecraft:custom_payload}
     */
    int CUSTOM_PAYLOAD_ID = 25;

    /**
     * {@code minecraft:custom_report_details}
     */
    int CUSTOM_REPORT_DETAILS_ID = 140;

    /**
     * {@code minecraft:damage_event}
     */
    int DAMAGE_EVENT_ID = 26;

    /**
     * {@code minecraft:debug/block_value}
     */
    int DEBUG_BLOCK_VALUE_ID = 27;

    /**
     * {@code minecraft:debug/chunk_value}
     */
    int DEBUG_CHUNK_VALUE_ID = 28;

    /**
     * {@code minecraft:debug/entity_value}
     */
    int DEBUG_ENTITY_VALUE_ID = 29;

    /**
     * {@code minecraft:debug/event}
     */
    int DEBUG_EVENT_ID = 30;

    /**
     * {@code minecraft:debug_sample}
     */
    int DEBUG_SAMPLE_ID = 31;

    /**
     * {@code minecraft:delete_chat}
     */
    int DELETE_CHAT_ID = 32;

    /**
     * {@code minecraft:disconnect}
     */
    int DISCONNECT_ID = 33;

    /**
     * {@code minecraft:disguised_chat}
     */
    int DISGUISED_CHAT_ID = 34;

    /**
     * {@code minecraft:entity_event}
     */
    int ENTITY_EVENT_ID = 35;

    /**
     * {@code minecraft:entity_position_sync}
     */
    int ENTITY_POSITION_SYNC_ID = 36;

    /**
     * {@code minecraft:explode}
     */
    int EXPLODE_ID = 37;

    /**
     * {@code minecraft:forget_level_chunk}
     */
    int FORGET_LEVEL_CHUNK_ID = 39;

    /**
     * {@code minecraft:game_event}
     */
    int GAME_EVENT_ID = 40;

    /**
     * {@code minecraft:game_rule_values}
     */
    int GAME_RULE_VALUES_ID = 41;

    /**
     * {@code minecraft:game_test_highlight_pos}
     */
    int GAME_TEST_HIGHLIGHT_POS_ID = 42;

    /**
     * {@code minecraft:hurt_animation}
     */
    int HURT_ANIMATION_ID = 44;

    /**
     * {@code minecraft:initialize_border}
     */
    int INITIALIZE_BORDER_ID = 45;

    /**
     * {@code minecraft:keep_alive}
     */
    int KEEP_ALIVE_ID = 46;

    /**
     * {@code minecraft:level_chunk_with_light}
     */
    int LEVEL_CHUNK_WITH_LIGHT_ID = 47;

    /**
     * {@code minecraft:level_event}
     */
    int LEVEL_EVENT_ID = 48;

    /**
     * {@code minecraft:level_particles}
     */
    int LEVEL_PARTICLES_ID = 49;

    /**
     * {@code minecraft:light_update}
     */
    int LIGHT_UPDATE_ID = 50;

    /**
     * {@code minecraft:login}
     */
    int LOGIN_ID = 51;

    /**
     * {@code minecraft:low_disk_space_warning}
     */
    int LOW_DISK_SPACE_WARNING_ID = 52;

    /**
     * {@code minecraft:map_item_data}
     */
    int MAP_ITEM_DATA_ID = 53;

    /**
     * {@code minecraft:merchant_offers}
     */
    int MERCHANT_OFFERS_ID = 54;

    /**
     * {@code minecraft:mount_screen_open}
     */
    int MOUNT_SCREEN_OPEN_ID = 43;

    /**
     * {@code minecraft:move_entity_pos}
     */
    int MOVE_ENTITY_POS_ID = 55;

    /**
     * {@code minecraft:move_entity_pos_rot}
     */
    int MOVE_ENTITY_POS_ROT_ID = 56;

    /**
     * {@code minecraft:move_entity_rot}
     */
    int MOVE_ENTITY_ROT_ID = 58;

    /**
     * {@code minecraft:move_minecart_along_track}
     */
    int MOVE_MINECART_ALONG_TRACK_ID = 57;

    /**
     * {@code minecraft:move_vehicle}
     */
    int MOVE_VEHICLE_ID = 59;

    /**
     * {@code minecraft:open_book}
     */
    int OPEN_BOOK_ID = 60;

    /**
     * {@code minecraft:open_screen}
     */
    int OPEN_SCREEN_ID = 61;

    /**
     * {@code minecraft:open_sign_editor}
     */
    int OPEN_SIGN_EDITOR_ID = 62;

    /**
     * {@code minecraft:ping}
     */
    int PING_ID = 63;

    /**
     * {@code minecraft:place_ghost_recipe}
     */
    int PLACE_GHOST_RECIPE_ID = 65;

    /**
     * {@code minecraft:player_abilities}
     */
    int PLAYER_ABILITIES_ID = 66;

    /**
     * {@code minecraft:player_chat}
     */
    int PLAYER_CHAT_ID = 67;

    /**
     * {@code minecraft:player_combat_end}
     */
    int PLAYER_COMBAT_END_ID = 68;

    /**
     * {@code minecraft:player_combat_enter}
     */
    int PLAYER_COMBAT_ENTER_ID = 69;

    /**
     * {@code minecraft:player_combat_kill}
     */
    int PLAYER_COMBAT_KILL_ID = 70;

    /**
     * {@code minecraft:player_info_remove}
     */
    int PLAYER_INFO_REMOVE_ID = 71;

    /**
     * {@code minecraft:player_info_update}
     */
    int PLAYER_INFO_UPDATE_ID = 72;

    /**
     * {@code minecraft:player_look_at}
     */
    int PLAYER_LOOK_AT_ID = 73;

    /**
     * {@code minecraft:player_position}
     */
    int PLAYER_POSITION_ID = 74;

    /**
     * {@code minecraft:player_rotation}
     */
    int PLAYER_ROTATION_ID = 75;

    /**
     * {@code minecraft:pong_response}
     */
    int PONG_RESPONSE_ID = 64;

    /**
     * {@code minecraft:post_effects}
     */
    int POST_EFFECTS_ID = 84;

    /**
     * {@code minecraft:projectile_power}
     */
    int PROJECTILE_POWER_ID = 139;

    /**
     * {@code minecraft:recipe_book_add}
     */
    int RECIPE_BOOK_ADD_ID = 76;

    /**
     * {@code minecraft:recipe_book_remove}
     */
    int RECIPE_BOOK_REMOVE_ID = 77;

    /**
     * {@code minecraft:recipe_book_settings}
     */
    int RECIPE_BOOK_SETTINGS_ID = 78;

    /**
     * {@code minecraft:remove_entities}
     */
    int REMOVE_ENTITIES_ID = 79;

    /**
     * {@code minecraft:remove_mob_effect}
     */
    int REMOVE_MOB_EFFECT_ID = 80;

    /**
     * {@code minecraft:reset_score}
     */
    int RESET_SCORE_ID = 81;

    /**
     * {@code minecraft:resource_pack_pop}
     */
    int RESOURCE_PACK_POP_ID = 82;

    /**
     * {@code minecraft:resource_pack_push}
     */
    int RESOURCE_PACK_PUSH_ID = 83;

    /**
     * {@code minecraft:respawn}
     */
    int RESPAWN_ID = 85;

    /**
     * {@code minecraft:rotate_head}
     */
    int ROTATE_HEAD_ID = 86;

    /**
     * {@code minecraft:section_blocks_update}
     */
    int SECTION_BLOCKS_UPDATE_ID = 87;

    /**
     * {@code minecraft:select_advancements_tab}
     */
    int SELECT_ADVANCEMENTS_TAB_ID = 88;

    /**
     * {@code minecraft:server_data}
     */
    int SERVER_DATA_ID = 89;

    /**
     * {@code minecraft:server_links}
     */
    int SERVER_LINKS_ID = 141;

    /**
     * {@code minecraft:set_action_bar_text}
     */
    int SET_ACTION_BAR_TEXT_ID = 90;

    /**
     * {@code minecraft:set_border_center}
     */
    int SET_BORDER_CENTER_ID = 91;

    /**
     * {@code minecraft:set_border_lerp_size}
     */
    int SET_BORDER_LERP_SIZE_ID = 92;

    /**
     * {@code minecraft:set_border_size}
     */
    int SET_BORDER_SIZE_ID = 93;

    /**
     * {@code minecraft:set_border_warning_delay}
     */
    int SET_BORDER_WARNING_DELAY_ID = 94;

    /**
     * {@code minecraft:set_border_warning_distance}
     */
    int SET_BORDER_WARNING_DISTANCE_ID = 95;

    /**
     * {@code minecraft:set_camera}
     */
    int SET_CAMERA_ID = 96;

    /**
     * {@code minecraft:set_chunk_cache_center}
     */
    int SET_CHUNK_CACHE_CENTER_ID = 97;

    /**
     * {@code minecraft:set_chunk_cache_radius}
     */
    int SET_CHUNK_CACHE_RADIUS_ID = 98;

    /**
     * {@code minecraft:set_cursor_item}
     */
    int SET_CURSOR_ITEM_ID = 99;

    /**
     * {@code minecraft:set_default_spawn_position}
     */
    int SET_DEFAULT_SPAWN_POSITION_ID = 100;

    /**
     * {@code minecraft:set_display_objective}
     */
    int SET_DISPLAY_OBJECTIVE_ID = 101;

    /**
     * {@code minecraft:set_entity_data}
     */
    int SET_ENTITY_DATA_ID = 102;

    /**
     * {@code minecraft:set_entity_link}
     */
    int SET_ENTITY_LINK_ID = 103;

    /**
     * {@code minecraft:set_entity_motion}
     */
    int SET_ENTITY_MOTION_ID = 104;

    /**
     * {@code minecraft:set_equipment}
     */
    int SET_EQUIPMENT_ID = 105;

    /**
     * {@code minecraft:set_experience}
     */
    int SET_EXPERIENCE_ID = 106;

    /**
     * {@code minecraft:set_health}
     */
    int SET_HEALTH_ID = 107;

    /**
     * {@code minecraft:set_held_slot}
     */
    int SET_HELD_SLOT_ID = 108;

    /**
     * {@code minecraft:set_objective}
     */
    int SET_OBJECTIVE_ID = 109;

    /**
     * {@code minecraft:set_passengers}
     */
    int SET_PASSENGERS_ID = 110;

    /**
     * {@code minecraft:set_player_inventory}
     */
    int SET_PLAYER_INVENTORY_ID = 111;

    /**
     * {@code minecraft:set_player_team}
     */
    int SET_PLAYER_TEAM_ID = 112;

    /**
     * {@code minecraft:set_score}
     */
    int SET_SCORE_ID = 113;

    /**
     * {@code minecraft:set_simulation_distance}
     */
    int SET_SIMULATION_DISTANCE_ID = 114;

    /**
     * {@code minecraft:set_subtitle_text}
     */
    int SET_SUBTITLE_TEXT_ID = 115;

    /**
     * {@code minecraft:set_time}
     */
    int SET_TIME_ID = 116;

    /**
     * {@code minecraft:set_title_text}
     */
    int SET_TITLE_TEXT_ID = 117;

    /**
     * {@code minecraft:set_titles_animation}
     */
    int SET_TITLES_ANIMATION_ID = 118;

    /**
     * {@code minecraft:show_dialog}
     */
    int SHOW_DIALOG_ID = 144;

    /**
     * {@code minecraft:sound}
     */
    int SOUND_ID = 120;

    /**
     * {@code minecraft:sound_entity}
     */
    int SOUND_ENTITY_ID = 119;

    /**
     * {@code minecraft:start_configuration}
     */
    int START_CONFIGURATION_ID = 121;

    /**
     * {@code minecraft:stop_sound}
     */
    int STOP_SOUND_ID = 122;

    /**
     * {@code minecraft:store_cookie}
     */
    int STORE_COOKIE_ID = 123;

    /**
     * {@code minecraft:swing_animation}
     */
    int SWING_ANIMATION_ID = 124;

    /**
     * {@code minecraft:system_chat}
     */
    int SYSTEM_CHAT_ID = 125;

    /**
     * {@code minecraft:tab_list}
     */
    int TAB_LIST_ID = 126;

    /**
     * {@code minecraft:tag_query}
     */
    int TAG_QUERY_ID = 127;

    /**
     * {@code minecraft:take_item_entity}
     */
    int TAKE_ITEM_ENTITY_ID = 128;

    /**
     * {@code minecraft:teleport_entity}
     */
    int TELEPORT_ENTITY_ID = 129;

    /**
     * {@code minecraft:test_instance_block_status}
     */
    int TEST_INSTANCE_BLOCK_STATUS_ID = 130;

    /**
     * {@code minecraft:ticking_state}
     */
    int TICKING_STATE_ID = 131;

    /**
     * {@code minecraft:ticking_step}
     */
    int TICKING_STEP_ID = 132;

    /**
     * {@code minecraft:transfer}
     */
    int TRANSFER_ID = 133;

    /**
     * {@code minecraft:update_advancements}
     */
    int UPDATE_ADVANCEMENTS_ID = 134;

    /**
     * {@code minecraft:update_attributes}
     */
    int UPDATE_ATTRIBUTES_ID = 135;

    /**
     * {@code minecraft:update_mob_effect}
     */
    int UPDATE_MOB_EFFECT_ID = 136;

    /**
     * {@code minecraft:update_recipes}
     */
    int UPDATE_RECIPES_ID = 137;

    /**
     * {@code minecraft:update_tags}
     */
    int UPDATE_TAGS_ID = 138;

    /**
     * {@code minecraft:waypoint}
     */
    int WAYPOINT_ID = 142;

    /**
     * Immutable identifier to protocol ID lookup table.
     */
    Object2IntMap<Key> BY_IDENTIFIER = Object2IntMaps.unmodifiable(new Object2IntOpenHashMap<>(
        new Key[] {
            Key.key("add_entity"),
            Key.key("add_entity_motion"),
            Key.key("add_transient_block"),
            Key.key("animate"),
            Key.key("award_stats"),
            Key.key("block_changed_ack"),
            Key.key("block_destruction"),
            Key.key("block_entity_data"),
            Key.key("block_event"),
            Key.key("block_update"),
            Key.key("boss_event"),
            Key.key("bundle_delimiter"),
            Key.key("change_difficulty"),
            Key.key("chunk_batch_finished"),
            Key.key("chunk_batch_start"),
            Key.key("chunks_biomes"),
            Key.key("clear_dialog"),
            Key.key("clear_titles"),
            Key.key("command_suggestions"),
            Key.key("commands"),
            Key.key("container_close"),
            Key.key("container_set_content"),
            Key.key("container_set_data"),
            Key.key("container_set_slot"),
            Key.key("cookie_request"),
            Key.key("cooldown"),
            Key.key("custom_chat_completions"),
            Key.key("custom_payload"),
            Key.key("custom_report_details"),
            Key.key("damage_event"),
            Key.key("debug/block_value"),
            Key.key("debug/chunk_value"),
            Key.key("debug/entity_value"),
            Key.key("debug/event"),
            Key.key("debug_sample"),
            Key.key("delete_chat"),
            Key.key("disconnect"),
            Key.key("disguised_chat"),
            Key.key("entity_event"),
            Key.key("entity_position_sync"),
            Key.key("explode"),
            Key.key("forget_level_chunk"),
            Key.key("game_event"),
            Key.key("game_rule_values"),
            Key.key("game_test_highlight_pos"),
            Key.key("hurt_animation"),
            Key.key("initialize_border"),
            Key.key("keep_alive"),
            Key.key("level_chunk_with_light"),
            Key.key("level_event"),
            Key.key("level_particles"),
            Key.key("light_update"),
            Key.key("login"),
            Key.key("low_disk_space_warning"),
            Key.key("map_item_data"),
            Key.key("merchant_offers"),
            Key.key("mount_screen_open"),
            Key.key("move_entity_pos"),
            Key.key("move_entity_pos_rot"),
            Key.key("move_entity_rot"),
            Key.key("move_minecart_along_track"),
            Key.key("move_vehicle"),
            Key.key("open_book"),
            Key.key("open_screen"),
            Key.key("open_sign_editor"),
            Key.key("ping"),
            Key.key("place_ghost_recipe"),
            Key.key("player_abilities"),
            Key.key("player_chat"),
            Key.key("player_combat_end"),
            Key.key("player_combat_enter"),
            Key.key("player_combat_kill"),
            Key.key("player_info_remove"),
            Key.key("player_info_update"),
            Key.key("player_look_at"),
            Key.key("player_position"),
            Key.key("player_rotation"),
            Key.key("pong_response"),
            Key.key("post_effects"),
            Key.key("projectile_power"),
            Key.key("recipe_book_add"),
            Key.key("recipe_book_remove"),
            Key.key("recipe_book_settings"),
            Key.key("remove_entities"),
            Key.key("remove_mob_effect"),
            Key.key("reset_score"),
            Key.key("resource_pack_pop"),
            Key.key("resource_pack_push"),
            Key.key("respawn"),
            Key.key("rotate_head"),
            Key.key("section_blocks_update"),
            Key.key("select_advancements_tab"),
            Key.key("server_data"),
            Key.key("server_links"),
            Key.key("set_action_bar_text"),
            Key.key("set_border_center"),
            Key.key("set_border_lerp_size"),
            Key.key("set_border_size"),
            Key.key("set_border_warning_delay"),
            Key.key("set_border_warning_distance"),
            Key.key("set_camera"),
            Key.key("set_chunk_cache_center"),
            Key.key("set_chunk_cache_radius"),
            Key.key("set_cursor_item"),
            Key.key("set_default_spawn_position"),
            Key.key("set_display_objective"),
            Key.key("set_entity_data"),
            Key.key("set_entity_link"),
            Key.key("set_entity_motion"),
            Key.key("set_equipment"),
            Key.key("set_experience"),
            Key.key("set_health"),
            Key.key("set_held_slot"),
            Key.key("set_objective"),
            Key.key("set_passengers"),
            Key.key("set_player_inventory"),
            Key.key("set_player_team"),
            Key.key("set_score"),
            Key.key("set_simulation_distance"),
            Key.key("set_subtitle_text"),
            Key.key("set_time"),
            Key.key("set_title_text"),
            Key.key("set_titles_animation"),
            Key.key("show_dialog"),
            Key.key("sound"),
            Key.key("sound_entity"),
            Key.key("start_configuration"),
            Key.key("stop_sound"),
            Key.key("store_cookie"),
            Key.key("swing_animation"),
            Key.key("system_chat"),
            Key.key("tab_list"),
            Key.key("tag_query"),
            Key.key("take_item_entity"),
            Key.key("teleport_entity"),
            Key.key("test_instance_block_status"),
            Key.key("ticking_state"),
            Key.key("ticking_step"),
            Key.key("transfer"),
            Key.key("update_advancements"),
            Key.key("update_attributes"),
            Key.key("update_mob_effect"),
            Key.key("update_recipes"),
            Key.key("update_tags"),
            Key.key("waypoint")
        },
        new int[] {
            ADD_ENTITY_ID,
            ADD_ENTITY_MOTION_ID,
            ADD_TRANSIENT_BLOCK_ID,
            ANIMATE_ID,
            AWARD_STATS_ID,
            BLOCK_CHANGED_ACK_ID,
            BLOCK_DESTRUCTION_ID,
            BLOCK_ENTITY_DATA_ID,
            BLOCK_EVENT_ID,
            BLOCK_UPDATE_ID,
            BOSS_EVENT_ID,
            BUNDLE_DELIMITER_ID,
            CHANGE_DIFFICULTY_ID,
            CHUNK_BATCH_FINISHED_ID,
            CHUNK_BATCH_START_ID,
            CHUNKS_BIOMES_ID,
            CLEAR_DIALOG_ID,
            CLEAR_TITLES_ID,
            COMMAND_SUGGESTIONS_ID,
            COMMANDS_ID,
            CONTAINER_CLOSE_ID,
            CONTAINER_SET_CONTENT_ID,
            CONTAINER_SET_DATA_ID,
            CONTAINER_SET_SLOT_ID,
            COOKIE_REQUEST_ID,
            COOLDOWN_ID,
            CUSTOM_CHAT_COMPLETIONS_ID,
            CUSTOM_PAYLOAD_ID,
            CUSTOM_REPORT_DETAILS_ID,
            DAMAGE_EVENT_ID,
            DEBUG_BLOCK_VALUE_ID,
            DEBUG_CHUNK_VALUE_ID,
            DEBUG_ENTITY_VALUE_ID,
            DEBUG_EVENT_ID,
            DEBUG_SAMPLE_ID,
            DELETE_CHAT_ID,
            DISCONNECT_ID,
            DISGUISED_CHAT_ID,
            ENTITY_EVENT_ID,
            ENTITY_POSITION_SYNC_ID,
            EXPLODE_ID,
            FORGET_LEVEL_CHUNK_ID,
            GAME_EVENT_ID,
            GAME_RULE_VALUES_ID,
            GAME_TEST_HIGHLIGHT_POS_ID,
            HURT_ANIMATION_ID,
            INITIALIZE_BORDER_ID,
            KEEP_ALIVE_ID,
            LEVEL_CHUNK_WITH_LIGHT_ID,
            LEVEL_EVENT_ID,
            LEVEL_PARTICLES_ID,
            LIGHT_UPDATE_ID,
            LOGIN_ID,
            LOW_DISK_SPACE_WARNING_ID,
            MAP_ITEM_DATA_ID,
            MERCHANT_OFFERS_ID,
            MOUNT_SCREEN_OPEN_ID,
            MOVE_ENTITY_POS_ID,
            MOVE_ENTITY_POS_ROT_ID,
            MOVE_ENTITY_ROT_ID,
            MOVE_MINECART_ALONG_TRACK_ID,
            MOVE_VEHICLE_ID,
            OPEN_BOOK_ID,
            OPEN_SCREEN_ID,
            OPEN_SIGN_EDITOR_ID,
            PING_ID,
            PLACE_GHOST_RECIPE_ID,
            PLAYER_ABILITIES_ID,
            PLAYER_CHAT_ID,
            PLAYER_COMBAT_END_ID,
            PLAYER_COMBAT_ENTER_ID,
            PLAYER_COMBAT_KILL_ID,
            PLAYER_INFO_REMOVE_ID,
            PLAYER_INFO_UPDATE_ID,
            PLAYER_LOOK_AT_ID,
            PLAYER_POSITION_ID,
            PLAYER_ROTATION_ID,
            PONG_RESPONSE_ID,
            POST_EFFECTS_ID,
            PROJECTILE_POWER_ID,
            RECIPE_BOOK_ADD_ID,
            RECIPE_BOOK_REMOVE_ID,
            RECIPE_BOOK_SETTINGS_ID,
            REMOVE_ENTITIES_ID,
            REMOVE_MOB_EFFECT_ID,
            RESET_SCORE_ID,
            RESOURCE_PACK_POP_ID,
            RESOURCE_PACK_PUSH_ID,
            RESPAWN_ID,
            ROTATE_HEAD_ID,
            SECTION_BLOCKS_UPDATE_ID,
            SELECT_ADVANCEMENTS_TAB_ID,
            SERVER_DATA_ID,
            SERVER_LINKS_ID,
            SET_ACTION_BAR_TEXT_ID,
            SET_BORDER_CENTER_ID,
            SET_BORDER_LERP_SIZE_ID,
            SET_BORDER_SIZE_ID,
            SET_BORDER_WARNING_DELAY_ID,
            SET_BORDER_WARNING_DISTANCE_ID,
            SET_CAMERA_ID,
            SET_CHUNK_CACHE_CENTER_ID,
            SET_CHUNK_CACHE_RADIUS_ID,
            SET_CURSOR_ITEM_ID,
            SET_DEFAULT_SPAWN_POSITION_ID,
            SET_DISPLAY_OBJECTIVE_ID,
            SET_ENTITY_DATA_ID,
            SET_ENTITY_LINK_ID,
            SET_ENTITY_MOTION_ID,
            SET_EQUIPMENT_ID,
            SET_EXPERIENCE_ID,
            SET_HEALTH_ID,
            SET_HELD_SLOT_ID,
            SET_OBJECTIVE_ID,
            SET_PASSENGERS_ID,
            SET_PLAYER_INVENTORY_ID,
            SET_PLAYER_TEAM_ID,
            SET_SCORE_ID,
            SET_SIMULATION_DISTANCE_ID,
            SET_SUBTITLE_TEXT_ID,
            SET_TIME_ID,
            SET_TITLE_TEXT_ID,
            SET_TITLES_ANIMATION_ID,
            SHOW_DIALOG_ID,
            SOUND_ID,
            SOUND_ENTITY_ID,
            START_CONFIGURATION_ID,
            STOP_SOUND_ID,
            STORE_COOKIE_ID,
            SWING_ANIMATION_ID,
            SYSTEM_CHAT_ID,
            TAB_LIST_ID,
            TAG_QUERY_ID,
            TAKE_ITEM_ENTITY_ID,
            TELEPORT_ENTITY_ID,
            TEST_INSTANCE_BLOCK_STATUS_ID,
            TICKING_STATE_ID,
            TICKING_STEP_ID,
            TRANSFER_ID,
            UPDATE_ADVANCEMENTS_ID,
            UPDATE_ATTRIBUTES_ID,
            UPDATE_MOB_EFFECT_ID,
            UPDATE_RECIPES_ID,
            UPDATE_TAGS_ID,
            WAYPOINT_ID
        }
    ));

    /**
     * Resolves the protocol ID for a namespaced identifier.
     *
     * @param identifier namespaced identifier, e.g. {@code Key.key("minecraft", "chest")}
     * @return the protocol ID, or {@link #UNKNOWN} when the identifier is unknown
     */
    static int id(final Key identifier) {
        return BY_IDENTIFIER.getOrDefault(identifier, UNKNOWN);
    }
}
