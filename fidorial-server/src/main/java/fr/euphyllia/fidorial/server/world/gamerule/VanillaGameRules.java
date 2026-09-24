package fr.euphyllia.fidorial.server.world.gamerule;

import fr.fidorial.gamerule.GameRuleDefinition;
import fr.fidorial.registry.keys.GameRuleKeys;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static fr.fidorial.gamerule.GameRuleDefinition.ofBoolean;
import static fr.fidorial.gamerule.GameRuleDefinition.ofInteger;

/**
 * Types, vanilla defaults and bounds of every entry of the {@code minecraft:game_rule} registry
 * (Minecraft 26.3).
 *
 * <p>The {@code implemented} flag says whether Fidorial wires the rule into its gameplay. Flip it
 * to {@code true} here once the matching mechanic exists: the rule then shows up in
 * {@code /gamerule} and becomes editable from the client's game rule screen.</p>
 */
public final class VanillaGameRules {

    private static final int MAX = Integer.MAX_VALUE;

    /**
     * Every known rule, sorted by identifier. The index of a definition in this list is its slot
     * in {@link FidorialGameRules}.
     */
    public static final List<GameRuleDefinition> ALL;

    private static final Map<Key, Integer> INDEX_BY_KEY = new LinkedHashMap<>();

    static {
        final List<GameRuleDefinition> rules = new ArrayList<>(List.of(
                ofBoolean(GameRuleKeys.ADVANCE_TIME, true, true),
                ofBoolean(GameRuleKeys.ADVANCE_WEATHER, true, true),
                ofBoolean(GameRuleKeys.DROWNING_DAMAGE, true, true),
                ofBoolean(GameRuleKeys.FALL_DAMAGE, true, true),
                ofBoolean(GameRuleKeys.FIRE_DAMAGE, true, true),
                ofBoolean(GameRuleKeys.FREEZE_DAMAGE, true, true),
                ofBoolean(GameRuleKeys.IMMEDIATE_RESPAWN, false, true),
                ofBoolean(GameRuleKeys.LAVA_SOURCE_CONVERSION, false, true),
                ofInteger(GameRuleKeys.MAX_BLOCK_MODIFICATIONS, 32_768, 1, MAX, true),
                ofBoolean(GameRuleKeys.MOB_GRIEFING, true, true),
                ofBoolean(GameRuleKeys.NATURAL_HEALTH_REGENERATION, true, true),
                ofBoolean(GameRuleKeys.PVP, true, true),
                ofBoolean(GameRuleKeys.REDUCED_DEBUG_INFO, false, true),
                ofBoolean(GameRuleKeys.SHOW_DEATH_MESSAGES, true, true),
                ofBoolean(GameRuleKeys.WATER_SOURCE_CONVERSION, true, true),


                ofBoolean(GameRuleKeys.ALLOW_ENTERING_NETHER_USING_PORTALS, true, false), // no portals
                ofBoolean(GameRuleKeys.BLOCK_DROPS, true, false), // no item entities
                ofBoolean(GameRuleKeys.BLOCK_EXPLOSION_DROP_DECAY, true, false), // no item entities
                ofBoolean(GameRuleKeys.COMMAND_BLOCK_OUTPUT, true, false), // no command blocks
                ofBoolean(GameRuleKeys.COMMAND_BLOCKS_WORK, true, false), // no command blocks
                ofBoolean(GameRuleKeys.ELYTRA_MOVEMENT_CHECK, true, false), // no movement checks
                ofBoolean(GameRuleKeys.ENDER_PEARLS_VANISH_ON_DEATH, true, false), // no ender pearls
                ofBoolean(GameRuleKeys.ENTITY_DROPS, true, false), // no item entities
                ofInteger(GameRuleKeys.FIRE_SPREAD_RADIUS_AROUND_PLAYER, 128, -1, MAX, false), // no fire ticks
                ofBoolean(GameRuleKeys.FORGIVE_DEAD_PLAYERS, true, false), // no neutral mob anger
                ofBoolean(GameRuleKeys.GLOBAL_SOUND_EVENTS, true, false), // no boss spawn sounds
                ofBoolean(GameRuleKeys.KEEP_INVENTORY, false, false), // nothing is dropped on death yet
                ofBoolean(GameRuleKeys.LIMITED_CRAFTING, false, false), // no crafting
                ofBoolean(GameRuleKeys.LOCATOR_BAR, true, false), // no waypoints
                ofBoolean(GameRuleKeys.LOG_ADMIN_COMMANDS, true, false), // no admin broadcast
                ofInteger(GameRuleKeys.MAX_COMMAND_FORKS, 65_536, 0, MAX, false), // no /execute
                ofInteger(GameRuleKeys.MAX_COMMAND_SEQUENCE_LENGTH, 65_536, 0, MAX, false), // no functions
                ofInteger(GameRuleKeys.MAX_ENTITY_CRAMMING, 24, 0, MAX, false), // no entity collisions
                ofInteger(GameRuleKeys.MAX_MINECART_SPEED, 8, 1, 1_000, false), // no minecarts (experimental)
                ofInteger(GameRuleKeys.MAX_SNOW_ACCUMULATION_HEIGHT, 1, 0, 8, false), // no snowfall
                ofBoolean(GameRuleKeys.MOB_DROPS, true, false), // no item entities
                ofBoolean(GameRuleKeys.MOB_EXPLOSION_DROP_DECAY, true, false), // no item entities
                ofBoolean(GameRuleKeys.PLAYER_MOVEMENT_CHECK, true, false), // no movement checks
                ofInteger(GameRuleKeys.PLAYERS_NETHER_PORTAL_CREATIVE_DELAY, 0, 0, MAX, false), // no portals
                ofInteger(GameRuleKeys.PLAYERS_NETHER_PORTAL_DEFAULT_DELAY, 80, 0, MAX, false), // no portals
                ofInteger(GameRuleKeys.PLAYERS_SLEEPING_PERCENTAGE, 100, 0, MAX, false), // no beds
                ofBoolean(GameRuleKeys.PROJECTILES_CAN_BREAK_BLOCKS, true, false), // no projectiles
                ofBoolean(GameRuleKeys.RAIDS, true, false), // no raids
                ofInteger(GameRuleKeys.RANDOM_TICK_SPEED, 3, 0, MAX, false), // no random ticks
                ofInteger(GameRuleKeys.RESPAWN_RADIUS, 10, 0, MAX, false), // no safe spawn search
                ofBoolean(GameRuleKeys.SEND_COMMAND_FEEDBACK, true, false), // commands reply directly
                ofBoolean(GameRuleKeys.SHOW_ADVANCEMENT_MESSAGES, true, false), // no advancements
                ofBoolean(GameRuleKeys.SPAWN_MOBS, true, false), // no natural spawning
                ofBoolean(GameRuleKeys.SPAWN_MONSTERS, true, false), // no natural spawning
                ofBoolean(GameRuleKeys.SPAWN_PATROLS, true, false), // no natural spawning
                ofBoolean(GameRuleKeys.SPAWN_PHANTOMS, true, false), // no natural spawning
                ofBoolean(GameRuleKeys.SPAWN_WANDERING_TRADERS, true, false), // no natural spawning
                ofBoolean(GameRuleKeys.SPAWN_WARDENS, true, false), // no natural spawning
                ofBoolean(GameRuleKeys.SPAWNER_BLOCKS_WORK, true, false), // no spawners
                ofBoolean(GameRuleKeys.SPECTATORS_GENERATE_CHUNKS, true, false), // no per-mode chunk loading
                ofBoolean(GameRuleKeys.SPREAD_VINES, true, false), // no random ticks
                ofBoolean(GameRuleKeys.TNT_EXPLODES, true, false), // no TNT
                ofBoolean(GameRuleKeys.TNT_EXPLOSION_DROP_DECAY, false, false), // no TNT
                ofBoolean(GameRuleKeys.UNIVERSAL_ANGER, false, false) // no neutral mob anger
        ));
        rules.sort(Comparator.comparing(rule -> rule.key().key().asString()));
        ALL = List.copyOf(rules);
        for (int i = 0; i < ALL.size(); i++) {
            final Integer previous = INDEX_BY_KEY.put(ALL.get(i).key().key(), i);
            if (previous != null) {
                throw new IllegalStateException("Duplicate game rule definition: " + ALL.get(i).key().key());
            }
        }
    }

    private VanillaGameRules() {
    }

    public static int indexOf(final Key key) {
        final Integer index = INDEX_BY_KEY.get(key);
        return index == null ? -1 : index;
    }

    public static int indexOf(final String id) {
        if (!Key.parseable(id)) {
            return -1;
        }
        return indexOf(Key.key(id));
    }
}
