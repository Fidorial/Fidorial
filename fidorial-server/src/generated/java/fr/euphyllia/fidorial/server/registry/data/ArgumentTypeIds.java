package fr.euphyllia.fidorial.server.registry.data;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.kyori.adventure.key.Key;

/**
 * Network IDs for entries in the {@code minecraft:command_argument_type} registry.
 *
 * <p>Generated from Mojang's registry report; do not edit.</p>
 */
public interface ArgumentTypeIds {
    /**
     * Returned by {@link #id(Key)} when the identifier is unknown.
     */
    int UNKNOWN = -1;

    /**
     * {@code minecraft:angle}
     */
    int ANGLE_ARGUMENT_ID = 0;

    /**
     * {@code minecraft:block_pos}
     */
    int BLOCK_POS_ARGUMENT_ID = 1;

    /**
     * {@code minecraft:block_predicate}
     */
    int BLOCK_PREDICATE_ARGUMENT_ID = 2;

    /**
     * {@code minecraft:block_state}
     */
    int BLOCK_STATE_ARGUMENT_ID = 3;

    /**
     * {@code minecraft:bool}
     */
    int BOOL_ARGUMENT_ID = 4;

    /**
     * {@code minecraft:column_pos}
     */
    int COLUMN_POS_ARGUMENT_ID = 5;

    /**
     * {@code minecraft:component}
     */
    int COMPONENT_ARGUMENT_ID = 6;

    /**
     * {@code minecraft:context_float_provider}
     */
    int CONTEXT_FLOAT_PROVIDER_ARGUMENT_ID = 7;

    /**
     * {@code minecraft:context_int_provider}
     */
    int CONTEXT_INT_PROVIDER_ARGUMENT_ID = 8;

    /**
     * {@code minecraft:dialog}
     */
    int DIALOG_ARGUMENT_ID = 9;

    /**
     * {@code minecraft:dimension}
     */
    int DIMENSION_ARGUMENT_ID = 10;

    /**
     * {@code minecraft:double}
     */
    int DOUBLE_ARGUMENT_ID = 11;

    /**
     * {@code minecraft:entity}
     */
    int ENTITY_ARGUMENT_ID = 12;

    /**
     * {@code minecraft:entity_anchor}
     */
    int ENTITY_ANCHOR_ARGUMENT_ID = 13;

    /**
     * {@code minecraft:feature}
     */
    int FEATURE_ARGUMENT_ID = 14;

    /**
     * {@code minecraft:float}
     */
    int FLOAT_ARGUMENT_ID = 15;

    /**
     * {@code minecraft:float_range}
     */
    int FLOAT_RANGE_ARGUMENT_ID = 16;

    /**
     * {@code minecraft:function}
     */
    int FUNCTION_ARGUMENT_ID = 17;

    /**
     * {@code minecraft:game_profile}
     */
    int GAME_PROFILE_ARGUMENT_ID = 18;

    /**
     * {@code minecraft:gamemode}
     */
    int GAMEMODE_ARGUMENT_ID = 19;

    /**
     * {@code minecraft:heightmap}
     */
    int HEIGHTMAP_ARGUMENT_ID = 20;

    /**
     * {@code minecraft:hex_color}
     */
    int HEX_COLOR_ARGUMENT_ID = 21;

    /**
     * {@code minecraft:int_range}
     */
    int INT_RANGE_ARGUMENT_ID = 22;

    /**
     * {@code minecraft:integer}
     */
    int INTEGER_ARGUMENT_ID = 23;

    /**
     * {@code minecraft:item_predicate}
     */
    int ITEM_PREDICATE_ARGUMENT_ID = 24;

    /**
     * {@code minecraft:item_slot}
     */
    int ITEM_SLOT_ARGUMENT_ID = 25;

    /**
     * {@code minecraft:item_slots}
     */
    int ITEM_SLOTS_ARGUMENT_ID = 26;

    /**
     * {@code minecraft:item_stack}
     */
    int ITEM_STACK_ARGUMENT_ID = 27;

    /**
     * {@code minecraft:long}
     */
    int LONG_ARGUMENT_ID = 28;

    /**
     * {@code minecraft:loot_modifier}
     */
    int LOOT_MODIFIER_ARGUMENT_ID = 29;

    /**
     * {@code minecraft:loot_predicate}
     */
    int LOOT_PREDICATE_ARGUMENT_ID = 30;

    /**
     * {@code minecraft:loot_table}
     */
    int LOOT_TABLE_ARGUMENT_ID = 31;

    /**
     * {@code minecraft:message}
     */
    int MESSAGE_ARGUMENT_ID = 32;

    /**
     * {@code minecraft:nbt_compound_tag}
     */
    int NBT_COMPOUND_TAG_ARGUMENT_ID = 33;

    /**
     * {@code minecraft:nbt_path}
     */
    int NBT_PATH_ARGUMENT_ID = 34;

    /**
     * {@code minecraft:nbt_tag}
     */
    int NBT_TAG_ARGUMENT_ID = 35;

    /**
     * {@code minecraft:objective}
     */
    int OBJECTIVE_ARGUMENT_ID = 36;

    /**
     * {@code minecraft:objective_criteria}
     */
    int OBJECTIVE_CRITERIA_ARGUMENT_ID = 37;

    /**
     * {@code minecraft:operation}
     */
    int OPERATION_ARGUMENT_ID = 38;

    /**
     * {@code minecraft:particle}
     */
    int PARTICLE_ARGUMENT_ID = 39;

    /**
     * {@code minecraft:pattern}
     */
    int PATTERN_ARGUMENT_ID = 40;

    /**
     * {@code minecraft:resource}
     */
    int RESOURCE_ARGUMENT_ID = 41;

    /**
     * {@code minecraft:resource_location}
     */
    int RESOURCE_LOCATION_ARGUMENT_ID = 42;

    /**
     * {@code minecraft:resource_or_tag}
     */
    int RESOURCE_OR_TAG_ARGUMENT_ID = 43;

    /**
     * {@code minecraft:resource_selector}
     */
    int RESOURCE_SELECTOR_ARGUMENT_ID = 44;

    /**
     * {@code minecraft:rotation}
     */
    int ROTATION_ARGUMENT_ID = 45;

    /**
     * {@code minecraft:score_holder}
     */
    int SCORE_HOLDER_ARGUMENT_ID = 46;

    /**
     * {@code minecraft:scoreboard_slot}
     */
    int SCOREBOARD_SLOT_ARGUMENT_ID = 47;

    /**
     * {@code minecraft:slot_source}
     */
    int SLOT_SOURCE_ARGUMENT_ID = 48;

    /**
     * {@code minecraft:string}
     */
    int STRING_ARGUMENT_ID = 49;

    /**
     * {@code minecraft:style}
     */
    int STYLE_ARGUMENT_ID = 50;

    /**
     * {@code minecraft:swing_animation}
     */
    int SWING_ANIMATION_ARGUMENT_ID = 51;

    /**
     * {@code minecraft:swizzle}
     */
    int SWIZZLE_ARGUMENT_ID = 52;

    /**
     * {@code minecraft:team}
     */
    int TEAM_ARGUMENT_ID = 53;

    /**
     * {@code minecraft:team_color}
     */
    int TEAM_COLOR_ARGUMENT_ID = 54;

    /**
     * {@code minecraft:template_mirror}
     */
    int TEMPLATE_MIRROR_ARGUMENT_ID = 55;

    /**
     * {@code minecraft:template_rotation}
     */
    int TEMPLATE_ROTATION_ARGUMENT_ID = 56;

    /**
     * {@code minecraft:time}
     */
    int TIME_ARGUMENT_ID = 57;

    /**
     * {@code minecraft:uuid}
     */
    int UUID_ARGUMENT_ID = 58;

    /**
     * {@code minecraft:vec2}
     */
    int VEC2_ARGUMENT_ID = 59;

    /**
     * {@code minecraft:vec3}
     */
    int VEC3_ARGUMENT_ID = 60;

    /**
     * Immutable identifier to protocol ID lookup table.
     */
    Object2IntMap<Key> BY_IDENTIFIER = Object2IntMaps.unmodifiable(new Object2IntOpenHashMap<>(
        new Key[] {
            Key.key("angle"),
            Key.key("block_pos"),
            Key.key("block_predicate"),
            Key.key("block_state"),
            Key.key("bool"),
            Key.key("column_pos"),
            Key.key("component"),
            Key.key("context_float_provider"),
            Key.key("context_int_provider"),
            Key.key("dialog"),
            Key.key("dimension"),
            Key.key("double"),
            Key.key("entity"),
            Key.key("entity_anchor"),
            Key.key("feature"),
            Key.key("float"),
            Key.key("float_range"),
            Key.key("function"),
            Key.key("game_profile"),
            Key.key("gamemode"),
            Key.key("heightmap"),
            Key.key("hex_color"),
            Key.key("int_range"),
            Key.key("integer"),
            Key.key("item_predicate"),
            Key.key("item_slot"),
            Key.key("item_slots"),
            Key.key("item_stack"),
            Key.key("long"),
            Key.key("loot_modifier"),
            Key.key("loot_predicate"),
            Key.key("loot_table"),
            Key.key("message"),
            Key.key("nbt_compound_tag"),
            Key.key("nbt_path"),
            Key.key("nbt_tag"),
            Key.key("objective"),
            Key.key("objective_criteria"),
            Key.key("operation"),
            Key.key("particle"),
            Key.key("pattern"),
            Key.key("resource"),
            Key.key("resource_location"),
            Key.key("resource_or_tag"),
            Key.key("resource_selector"),
            Key.key("rotation"),
            Key.key("score_holder"),
            Key.key("scoreboard_slot"),
            Key.key("slot_source"),
            Key.key("string"),
            Key.key("style"),
            Key.key("swing_animation"),
            Key.key("swizzle"),
            Key.key("team"),
            Key.key("team_color"),
            Key.key("template_mirror"),
            Key.key("template_rotation"),
            Key.key("time"),
            Key.key("uuid"),
            Key.key("vec2"),
            Key.key("vec3")
        },
        new int[] {
            ANGLE_ARGUMENT_ID,
            BLOCK_POS_ARGUMENT_ID,
            BLOCK_PREDICATE_ARGUMENT_ID,
            BLOCK_STATE_ARGUMENT_ID,
            BOOL_ARGUMENT_ID,
            COLUMN_POS_ARGUMENT_ID,
            COMPONENT_ARGUMENT_ID,
            CONTEXT_FLOAT_PROVIDER_ARGUMENT_ID,
            CONTEXT_INT_PROVIDER_ARGUMENT_ID,
            DIALOG_ARGUMENT_ID,
            DIMENSION_ARGUMENT_ID,
            DOUBLE_ARGUMENT_ID,
            ENTITY_ARGUMENT_ID,
            ENTITY_ANCHOR_ARGUMENT_ID,
            FEATURE_ARGUMENT_ID,
            FLOAT_ARGUMENT_ID,
            FLOAT_RANGE_ARGUMENT_ID,
            FUNCTION_ARGUMENT_ID,
            GAME_PROFILE_ARGUMENT_ID,
            GAMEMODE_ARGUMENT_ID,
            HEIGHTMAP_ARGUMENT_ID,
            HEX_COLOR_ARGUMENT_ID,
            INT_RANGE_ARGUMENT_ID,
            INTEGER_ARGUMENT_ID,
            ITEM_PREDICATE_ARGUMENT_ID,
            ITEM_SLOT_ARGUMENT_ID,
            ITEM_SLOTS_ARGUMENT_ID,
            ITEM_STACK_ARGUMENT_ID,
            LONG_ARGUMENT_ID,
            LOOT_MODIFIER_ARGUMENT_ID,
            LOOT_PREDICATE_ARGUMENT_ID,
            LOOT_TABLE_ARGUMENT_ID,
            MESSAGE_ARGUMENT_ID,
            NBT_COMPOUND_TAG_ARGUMENT_ID,
            NBT_PATH_ARGUMENT_ID,
            NBT_TAG_ARGUMENT_ID,
            OBJECTIVE_ARGUMENT_ID,
            OBJECTIVE_CRITERIA_ARGUMENT_ID,
            OPERATION_ARGUMENT_ID,
            PARTICLE_ARGUMENT_ID,
            PATTERN_ARGUMENT_ID,
            RESOURCE_ARGUMENT_ID,
            RESOURCE_LOCATION_ARGUMENT_ID,
            RESOURCE_OR_TAG_ARGUMENT_ID,
            RESOURCE_SELECTOR_ARGUMENT_ID,
            ROTATION_ARGUMENT_ID,
            SCORE_HOLDER_ARGUMENT_ID,
            SCOREBOARD_SLOT_ARGUMENT_ID,
            SLOT_SOURCE_ARGUMENT_ID,
            STRING_ARGUMENT_ID,
            STYLE_ARGUMENT_ID,
            SWING_ANIMATION_ARGUMENT_ID,
            SWIZZLE_ARGUMENT_ID,
            TEAM_ARGUMENT_ID,
            TEAM_COLOR_ARGUMENT_ID,
            TEMPLATE_MIRROR_ARGUMENT_ID,
            TEMPLATE_ROTATION_ARGUMENT_ID,
            TIME_ARGUMENT_ID,
            UUID_ARGUMENT_ID,
            VEC2_ARGUMENT_ID,
            VEC3_ARGUMENT_ID
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
