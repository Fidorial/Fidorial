package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.GameEvent;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:game_event} registry.
 */
public final class GameEventKeys {
    /**
     * Key for {@code minecraft:block_activate}.
     */
    public static final TypedKey<GameEvent> BLOCK_ACTIVATE = create("block_activate");

    /**
     * Key for {@code minecraft:block_attach}.
     */
    public static final TypedKey<GameEvent> BLOCK_ATTACH = create("block_attach");

    /**
     * Key for {@code minecraft:block_change}.
     */
    public static final TypedKey<GameEvent> BLOCK_CHANGE = create("block_change");

    /**
     * Key for {@code minecraft:block_close}.
     */
    public static final TypedKey<GameEvent> BLOCK_CLOSE = create("block_close");

    /**
     * Key for {@code minecraft:block_deactivate}.
     */
    public static final TypedKey<GameEvent> BLOCK_DEACTIVATE = create("block_deactivate");

    /**
     * Key for {@code minecraft:block_destroy}.
     */
    public static final TypedKey<GameEvent> BLOCK_DESTROY = create("block_destroy");

    /**
     * Key for {@code minecraft:block_detach}.
     */
    public static final TypedKey<GameEvent> BLOCK_DETACH = create("block_detach");

    /**
     * Key for {@code minecraft:block_open}.
     */
    public static final TypedKey<GameEvent> BLOCK_OPEN = create("block_open");

    /**
     * Key for {@code minecraft:block_place}.
     */
    public static final TypedKey<GameEvent> BLOCK_PLACE = create("block_place");

    /**
     * Key for {@code minecraft:bounce}.
     */
    public static final TypedKey<GameEvent> BOUNCE = create("bounce");

    /**
     * Key for {@code minecraft:container_close}.
     */
    public static final TypedKey<GameEvent> CONTAINER_CLOSE = create("container_close");

    /**
     * Key for {@code minecraft:container_open}.
     */
    public static final TypedKey<GameEvent> CONTAINER_OPEN = create("container_open");

    /**
     * Key for {@code minecraft:drink}.
     */
    public static final TypedKey<GameEvent> DRINK = create("drink");

    /**
     * Key for {@code minecraft:eat}.
     */
    public static final TypedKey<GameEvent> EAT = create("eat");

    /**
     * Key for {@code minecraft:elytra_glide}.
     */
    public static final TypedKey<GameEvent> ELYTRA_GLIDE = create("elytra_glide");

    /**
     * Key for {@code minecraft:entity_action}.
     */
    public static final TypedKey<GameEvent> ENTITY_ACTION = create("entity_action");

    /**
     * Key for {@code minecraft:entity_damage}.
     */
    public static final TypedKey<GameEvent> ENTITY_DAMAGE = create("entity_damage");

    /**
     * Key for {@code minecraft:entity_die}.
     */
    public static final TypedKey<GameEvent> ENTITY_DIE = create("entity_die");

    /**
     * Key for {@code minecraft:entity_dismount}.
     */
    public static final TypedKey<GameEvent> ENTITY_DISMOUNT = create("entity_dismount");

    /**
     * Key for {@code minecraft:entity_interact}.
     */
    public static final TypedKey<GameEvent> ENTITY_INTERACT = create("entity_interact");

    /**
     * Key for {@code minecraft:entity_mount}.
     */
    public static final TypedKey<GameEvent> ENTITY_MOUNT = create("entity_mount");

    /**
     * Key for {@code minecraft:entity_place}.
     */
    public static final TypedKey<GameEvent> ENTITY_PLACE = create("entity_place");

    /**
     * Key for {@code minecraft:equip}.
     */
    public static final TypedKey<GameEvent> EQUIP = create("equip");

    /**
     * Key for {@code minecraft:explode}.
     */
    public static final TypedKey<GameEvent> EXPLODE = create("explode");

    /**
     * Key for {@code minecraft:flap}.
     */
    public static final TypedKey<GameEvent> FLAP = create("flap");

    /**
     * Key for {@code minecraft:fluid_pickup}.
     */
    public static final TypedKey<GameEvent> FLUID_PICKUP = create("fluid_pickup");

    /**
     * Key for {@code minecraft:fluid_place}.
     */
    public static final TypedKey<GameEvent> FLUID_PLACE = create("fluid_place");

    /**
     * Key for {@code minecraft:hit_ground}.
     */
    public static final TypedKey<GameEvent> HIT_GROUND = create("hit_ground");

    /**
     * Key for {@code minecraft:instrument_play}.
     */
    public static final TypedKey<GameEvent> INSTRUMENT_PLAY = create("instrument_play");

    /**
     * Key for {@code minecraft:item_interact_finish}.
     */
    public static final TypedKey<GameEvent> ITEM_INTERACT_FINISH = create("item_interact_finish");

    /**
     * Key for {@code minecraft:item_interact_start}.
     */
    public static final TypedKey<GameEvent> ITEM_INTERACT_START = create("item_interact_start");

    /**
     * Key for {@code minecraft:jukebox_play}.
     */
    public static final TypedKey<GameEvent> JUKEBOX_PLAY = create("jukebox_play");

    /**
     * Key for {@code minecraft:jukebox_stop_play}.
     */
    public static final TypedKey<GameEvent> JUKEBOX_STOP_PLAY = create("jukebox_stop_play");

    /**
     * Key for {@code minecraft:lightning_strike}.
     */
    public static final TypedKey<GameEvent> LIGHTNING_STRIKE = create("lightning_strike");

    /**
     * Key for {@code minecraft:note_block_play}.
     */
    public static final TypedKey<GameEvent> NOTE_BLOCK_PLAY = create("note_block_play");

    /**
     * Key for {@code minecraft:prime_fuse}.
     */
    public static final TypedKey<GameEvent> PRIME_FUSE = create("prime_fuse");

    /**
     * Key for {@code minecraft:projectile_land}.
     */
    public static final TypedKey<GameEvent> PROJECTILE_LAND = create("projectile_land");

    /**
     * Key for {@code minecraft:projectile_shoot}.
     */
    public static final TypedKey<GameEvent> PROJECTILE_SHOOT = create("projectile_shoot");

    /**
     * Key for {@code minecraft:resonate_1}.
     */
    public static final TypedKey<GameEvent> RESONATE_1 = create("resonate_1");

    /**
     * Key for {@code minecraft:resonate_10}.
     */
    public static final TypedKey<GameEvent> RESONATE_10 = create("resonate_10");

    /**
     * Key for {@code minecraft:resonate_11}.
     */
    public static final TypedKey<GameEvent> RESONATE_11 = create("resonate_11");

    /**
     * Key for {@code minecraft:resonate_12}.
     */
    public static final TypedKey<GameEvent> RESONATE_12 = create("resonate_12");

    /**
     * Key for {@code minecraft:resonate_13}.
     */
    public static final TypedKey<GameEvent> RESONATE_13 = create("resonate_13");

    /**
     * Key for {@code minecraft:resonate_14}.
     */
    public static final TypedKey<GameEvent> RESONATE_14 = create("resonate_14");

    /**
     * Key for {@code minecraft:resonate_15}.
     */
    public static final TypedKey<GameEvent> RESONATE_15 = create("resonate_15");

    /**
     * Key for {@code minecraft:resonate_2}.
     */
    public static final TypedKey<GameEvent> RESONATE_2 = create("resonate_2");

    /**
     * Key for {@code minecraft:resonate_3}.
     */
    public static final TypedKey<GameEvent> RESONATE_3 = create("resonate_3");

    /**
     * Key for {@code minecraft:resonate_4}.
     */
    public static final TypedKey<GameEvent> RESONATE_4 = create("resonate_4");

    /**
     * Key for {@code minecraft:resonate_5}.
     */
    public static final TypedKey<GameEvent> RESONATE_5 = create("resonate_5");

    /**
     * Key for {@code minecraft:resonate_6}.
     */
    public static final TypedKey<GameEvent> RESONATE_6 = create("resonate_6");

    /**
     * Key for {@code minecraft:resonate_7}.
     */
    public static final TypedKey<GameEvent> RESONATE_7 = create("resonate_7");

    /**
     * Key for {@code minecraft:resonate_8}.
     */
    public static final TypedKey<GameEvent> RESONATE_8 = create("resonate_8");

    /**
     * Key for {@code minecraft:resonate_9}.
     */
    public static final TypedKey<GameEvent> RESONATE_9 = create("resonate_9");

    /**
     * Key for {@code minecraft:sculk_sensor_tendrils_clicking}.
     */
    public static final TypedKey<GameEvent> SCULK_SENSOR_TENDRILS_CLICKING = create("sculk_sensor_tendrils_clicking");

    /**
     * Key for {@code minecraft:shear}.
     */
    public static final TypedKey<GameEvent> SHEAR = create("shear");

    /**
     * Key for {@code minecraft:shriek}.
     */
    public static final TypedKey<GameEvent> SHRIEK = create("shriek");

    /**
     * Key for {@code minecraft:splash}.
     */
    public static final TypedKey<GameEvent> SPLASH = create("splash");

    /**
     * Key for {@code minecraft:step}.
     */
    public static final TypedKey<GameEvent> STEP = create("step");

    /**
     * Key for {@code minecraft:swim}.
     */
    public static final TypedKey<GameEvent> SWIM = create("swim");

    /**
     * Key for {@code minecraft:teleport}.
     */
    public static final TypedKey<GameEvent> TELEPORT = create("teleport");

    /**
     * Key for {@code minecraft:unequip}.
     */
    public static final TypedKey<GameEvent> UNEQUIP = create("unequip");

    private static final List<TypedKey<GameEvent>> VALUES = List.of(
        BLOCK_ACTIVATE,
        BLOCK_ATTACH,
        BLOCK_CHANGE,
        BLOCK_CLOSE,
        BLOCK_DEACTIVATE,
        BLOCK_DESTROY,
        BLOCK_DETACH,
        BLOCK_OPEN,
        BLOCK_PLACE,
        BOUNCE,
        CONTAINER_CLOSE,
        CONTAINER_OPEN,
        DRINK,
        EAT,
        ELYTRA_GLIDE,
        ENTITY_ACTION,
        ENTITY_DAMAGE,
        ENTITY_DIE,
        ENTITY_DISMOUNT,
        ENTITY_INTERACT,
        ENTITY_MOUNT,
        ENTITY_PLACE,
        EQUIP,
        EXPLODE,
        FLAP,
        FLUID_PICKUP,
        FLUID_PLACE,
        HIT_GROUND,
        INSTRUMENT_PLAY,
        ITEM_INTERACT_FINISH,
        ITEM_INTERACT_START,
        JUKEBOX_PLAY,
        JUKEBOX_STOP_PLAY,
        LIGHTNING_STRIKE,
        NOTE_BLOCK_PLAY,
        PRIME_FUSE,
        PROJECTILE_LAND,
        PROJECTILE_SHOOT,
        RESONATE_1,
        RESONATE_10,
        RESONATE_11,
        RESONATE_12,
        RESONATE_13,
        RESONATE_14,
        RESONATE_15,
        RESONATE_2,
        RESONATE_3,
        RESONATE_4,
        RESONATE_5,
        RESONATE_6,
        RESONATE_7,
        RESONATE_8,
        RESONATE_9,
        SCULK_SENSOR_TENDRILS_CLICKING,
        SHEAR,
        SHRIEK,
        SPLASH,
        STEP,
        SWIM,
        TELEPORT,
        UNEQUIP
    );

    private GameEventKeys() {
        throw new UnsupportedOperationException("GameEventKeys cannot be instantiated.");
    }

    private static TypedKey<GameEvent> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.GAME_EVENT, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<GameEvent>> values() {
        return VALUES.stream();
    }
}
