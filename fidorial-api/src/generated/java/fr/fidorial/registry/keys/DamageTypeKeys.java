package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.DamageType;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:damage_type} registry.
 */
public final class DamageTypeKeys {
    /**
     * Key for {@code minecraft:arrow}.
     */
    public static final TypedKey<DamageType> ARROW = create("arrow");

    /**
     * Key for {@code minecraft:bad_respawn_point}.
     */
    public static final TypedKey<DamageType> BAD_RESPAWN_POINT = create("bad_respawn_point");

    /**
     * Key for {@code minecraft:cactus}.
     */
    public static final TypedKey<DamageType> CACTUS = create("cactus");

    /**
     * Key for {@code minecraft:campfire}.
     */
    public static final TypedKey<DamageType> CAMPFIRE = create("campfire");

    /**
     * Key for {@code minecraft:cramming}.
     */
    public static final TypedKey<DamageType> CRAMMING = create("cramming");

    /**
     * Key for {@code minecraft:dragon_breath}.
     */
    public static final TypedKey<DamageType> DRAGON_BREATH = create("dragon_breath");

    /**
     * Key for {@code minecraft:drown}.
     */
    public static final TypedKey<DamageType> DROWN = create("drown");

    /**
     * Key for {@code minecraft:dry_out}.
     */
    public static final TypedKey<DamageType> DRY_OUT = create("dry_out");

    /**
     * Key for {@code minecraft:ender_pearl}.
     */
    public static final TypedKey<DamageType> ENDER_PEARL = create("ender_pearl");

    /**
     * Key for {@code minecraft:explosion}.
     */
    public static final TypedKey<DamageType> EXPLOSION = create("explosion");

    /**
     * Key for {@code minecraft:fall}.
     */
    public static final TypedKey<DamageType> FALL = create("fall");

    /**
     * Key for {@code minecraft:falling_anvil}.
     */
    public static final TypedKey<DamageType> FALLING_ANVIL = create("falling_anvil");

    /**
     * Key for {@code minecraft:falling_block}.
     */
    public static final TypedKey<DamageType> FALLING_BLOCK = create("falling_block");

    /**
     * Key for {@code minecraft:falling_stalactite}.
     */
    public static final TypedKey<DamageType> FALLING_STALACTITE = create("falling_stalactite");

    /**
     * Key for {@code minecraft:fireball}.
     */
    public static final TypedKey<DamageType> FIREBALL = create("fireball");

    /**
     * Key for {@code minecraft:fireworks}.
     */
    public static final TypedKey<DamageType> FIREWORKS = create("fireworks");

    /**
     * Key for {@code minecraft:fly_into_wall}.
     */
    public static final TypedKey<DamageType> FLY_INTO_WALL = create("fly_into_wall");

    /**
     * Key for {@code minecraft:freeze}.
     */
    public static final TypedKey<DamageType> FREEZE = create("freeze");

    /**
     * Key for {@code minecraft:generic}.
     */
    public static final TypedKey<DamageType> GENERIC = create("generic");

    /**
     * Key for {@code minecraft:generic_kill}.
     */
    public static final TypedKey<DamageType> GENERIC_KILL = create("generic_kill");

    /**
     * Key for {@code minecraft:hot_floor}.
     */
    public static final TypedKey<DamageType> HOT_FLOOR = create("hot_floor");

    /**
     * Key for {@code minecraft:in_fire}.
     */
    public static final TypedKey<DamageType> IN_FIRE = create("in_fire");

    /**
     * Key for {@code minecraft:in_wall}.
     */
    public static final TypedKey<DamageType> IN_WALL = create("in_wall");

    /**
     * Key for {@code minecraft:indirect_magic}.
     */
    public static final TypedKey<DamageType> INDIRECT_MAGIC = create("indirect_magic");

    /**
     * Key for {@code minecraft:lava}.
     */
    public static final TypedKey<DamageType> LAVA = create("lava");

    /**
     * Key for {@code minecraft:lightning_bolt}.
     */
    public static final TypedKey<DamageType> LIGHTNING_BOLT = create("lightning_bolt");

    /**
     * Key for {@code minecraft:mace_smash}.
     */
    public static final TypedKey<DamageType> MACE_SMASH = create("mace_smash");

    /**
     * Key for {@code minecraft:magic}.
     */
    public static final TypedKey<DamageType> MAGIC = create("magic");

    /**
     * Key for {@code minecraft:mob_attack}.
     */
    public static final TypedKey<DamageType> MOB_ATTACK = create("mob_attack");

    /**
     * Key for {@code minecraft:mob_attack_no_aggro}.
     */
    public static final TypedKey<DamageType> MOB_ATTACK_NO_AGGRO = create("mob_attack_no_aggro");

    /**
     * Key for {@code minecraft:mob_projectile}.
     */
    public static final TypedKey<DamageType> MOB_PROJECTILE = create("mob_projectile");

    /**
     * Key for {@code minecraft:on_fire}.
     */
    public static final TypedKey<DamageType> ON_FIRE = create("on_fire");

    /**
     * Key for {@code minecraft:out_of_world}.
     */
    public static final TypedKey<DamageType> OUT_OF_WORLD = create("out_of_world");

    /**
     * Key for {@code minecraft:outside_border}.
     */
    public static final TypedKey<DamageType> OUTSIDE_BORDER = create("outside_border");

    /**
     * Key for {@code minecraft:player_attack}.
     */
    public static final TypedKey<DamageType> PLAYER_ATTACK = create("player_attack");

    /**
     * Key for {@code minecraft:player_explosion}.
     */
    public static final TypedKey<DamageType> PLAYER_EXPLOSION = create("player_explosion");

    /**
     * Key for {@code minecraft:sonic_boom}.
     */
    public static final TypedKey<DamageType> SONIC_BOOM = create("sonic_boom");

    /**
     * Key for {@code minecraft:spear}.
     */
    public static final TypedKey<DamageType> SPEAR = create("spear");

    /**
     * Key for {@code minecraft:spit}.
     */
    public static final TypedKey<DamageType> SPIT = create("spit");

    /**
     * Key for {@code minecraft:stalagmite}.
     */
    public static final TypedKey<DamageType> STALAGMITE = create("stalagmite");

    /**
     * Key for {@code minecraft:starve}.
     */
    public static final TypedKey<DamageType> STARVE = create("starve");

    /**
     * Key for {@code minecraft:sting}.
     */
    public static final TypedKey<DamageType> STING = create("sting");

    /**
     * Key for {@code minecraft:sulfur_cube_hot}.
     */
    public static final TypedKey<DamageType> SULFUR_CUBE_HOT = create("sulfur_cube_hot");

    /**
     * Key for {@code minecraft:sweet_berry_bush}.
     */
    public static final TypedKey<DamageType> SWEET_BERRY_BUSH = create("sweet_berry_bush");

    /**
     * Key for {@code minecraft:thorns}.
     */
    public static final TypedKey<DamageType> THORNS = create("thorns");

    /**
     * Key for {@code minecraft:thrown}.
     */
    public static final TypedKey<DamageType> THROWN = create("thrown");

    /**
     * Key for {@code minecraft:trident}.
     */
    public static final TypedKey<DamageType> TRIDENT = create("trident");

    /**
     * Key for {@code minecraft:unattributed_fireball}.
     */
    public static final TypedKey<DamageType> UNATTRIBUTED_FIREBALL = create("unattributed_fireball");

    /**
     * Key for {@code minecraft:wind_charge}.
     */
    public static final TypedKey<DamageType> WIND_CHARGE = create("wind_charge");

    /**
     * Key for {@code minecraft:wither}.
     */
    public static final TypedKey<DamageType> WITHER = create("wither");

    /**
     * Key for {@code minecraft:wither_skull}.
     */
    public static final TypedKey<DamageType> WITHER_SKULL = create("wither_skull");

    private static final List<TypedKey<DamageType>> VALUES = List.of(
        ARROW,
        BAD_RESPAWN_POINT,
        CACTUS,
        CAMPFIRE,
        CRAMMING,
        DRAGON_BREATH,
        DROWN,
        DRY_OUT,
        ENDER_PEARL,
        EXPLOSION,
        FALL,
        FALLING_ANVIL,
        FALLING_BLOCK,
        FALLING_STALACTITE,
        FIREBALL,
        FIREWORKS,
        FLY_INTO_WALL,
        FREEZE,
        GENERIC,
        GENERIC_KILL,
        HOT_FLOOR,
        IN_FIRE,
        IN_WALL,
        INDIRECT_MAGIC,
        LAVA,
        LIGHTNING_BOLT,
        MACE_SMASH,
        MAGIC,
        MOB_ATTACK,
        MOB_ATTACK_NO_AGGRO,
        MOB_PROJECTILE,
        ON_FIRE,
        OUT_OF_WORLD,
        OUTSIDE_BORDER,
        PLAYER_ATTACK,
        PLAYER_EXPLOSION,
        SONIC_BOOM,
        SPEAR,
        SPIT,
        STALAGMITE,
        STARVE,
        STING,
        SULFUR_CUBE_HOT,
        SWEET_BERRY_BUSH,
        THORNS,
        THROWN,
        TRIDENT,
        UNATTRIBUTED_FIREBALL,
        WIND_CHARGE,
        WITHER,
        WITHER_SKULL
    );

    private DamageTypeKeys() {
        throw new UnsupportedOperationException("DamageTypeKeys cannot be instantiated.");
    }

    private static TypedKey<DamageType> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.DAMAGE_TYPE, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<DamageType>> values() {
        return VALUES.stream();
    }
}
