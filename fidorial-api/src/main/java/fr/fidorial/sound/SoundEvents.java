package fr.fidorial.sound;

import fr.fidorial.annotation.SinceMinecraft;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.sound.Sound;

/**
 * Sound events played by the server itself, mostly by the built-in mobs.
 *
 * <p>For the complete vanilla list, see {@link fr.fidorial.registry.keys.SoundEventKeys}.</p>
 *
 * @since 0.1.0
 */
public final class SoundEvents {

    // --- Creeper ---
    /**
     * The {@code minecraft:entity.creeper.primed} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type CREEPER_PRIMED = of("entity.creeper.primed");

    /**
     * The {@code minecraft:entity.creeper.hurt} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type CREEPER_HURT = of("entity.creeper.hurt");

    /**
     * The {@code minecraft:entity.creeper.death} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type CREEPER_DEATH = of("entity.creeper.death");

    // --- Chicken ---
    /**
     * The {@code minecraft:entity.chicken.ambient} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type CHICKEN_AMBIENT = of("entity.chicken.ambient");

    /**
     * The {@code minecraft:entity.chicken.hurt} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type CHICKEN_HURT = of("entity.chicken.hurt");

    /**
     * The {@code minecraft:entity.chicken.death} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type CHICKEN_DEATH = of("entity.chicken.death");

    /**
     * The {@code minecraft:entity.chicken.step} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type CHICKEN_STEP = of("entity.chicken.step");

    /**
     * The {@code minecraft:entity.chicken.egg} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type CHICKEN_EGG = of("entity.chicken.egg");

    // --- Chicken ("picky" sound variant) ---
    /**
     * The {@code minecraft:entity.chicken_picky.ambient} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type CHICKEN_PICKY_AMBIENT = of("entity.chicken_picky.ambient");

    /**
     * The {@code minecraft:entity.chicken_picky.hurt} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type CHICKEN_PICKY_HURT = of("entity.chicken_picky.hurt");

    /**
     * The {@code minecraft:entity.chicken_picky.death} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type CHICKEN_PICKY_DEATH = of("entity.chicken_picky.death");

    /**
     * The {@code minecraft:entity.chicken_picky.step} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type CHICKEN_PICKY_STEP = of("entity.chicken_picky.step");

    // --- Baby chicken ---
    /**
     * The {@code minecraft:entity.baby_chicken.ambient} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type BABY_CHICKEN_AMBIENT = of("entity.baby_chicken.ambient");

    /**
     * The {@code minecraft:entity.baby_chicken.hurt} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type BABY_CHICKEN_HURT = of("entity.baby_chicken.hurt");

    /**
     * The {@code minecraft:entity.baby_chicken.death} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type BABY_CHICKEN_DEATH = of("entity.baby_chicken.death");

    /**
     * The {@code minecraft:entity.baby_chicken.step} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type BABY_CHICKEN_STEP = of("entity.baby_chicken.step");

    // --- Cow ---
    /**
     * The {@code minecraft:entity.cow.ambient} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type COW_AMBIENT = of("entity.cow.ambient");

    /**
     * The {@code minecraft:entity.cow.hurt} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type COW_HURT = of("entity.cow.hurt");

    /**
     * The {@code minecraft:entity.cow.death} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type COW_DEATH = of("entity.cow.death");

    /**
     * The {@code minecraft:entity.cow.step} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type COW_STEP = of("entity.cow.step");

    /**
     * The {@code minecraft:entity.cow.milk} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type COW_MILK = of("entity.cow.milk");

    // --- Cow ("moody" sound variant) ---
    /**
     * The {@code minecraft:entity.cow_moody.ambient} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type COW_MOODY_AMBIENT = of("entity.cow_moody.ambient");

    /**
     * The {@code minecraft:entity.cow_moody.hurt} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type COW_MOODY_HURT = of("entity.cow_moody.hurt");

    /**
     * The {@code minecraft:entity.cow_moody.death} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type COW_MOODY_DEATH = of("entity.cow_moody.death");

    /**
     * The {@code minecraft:entity.cow_moody.step} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type COW_MOODY_STEP = of("entity.cow_moody.step");

    // --- Pig ---
    /**
     * The {@code minecraft:entity.pig.ambient} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PIG_AMBIENT = of("entity.pig.ambient");

    /**
     * The {@code minecraft:entity.pig.death} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PIG_DEATH = of("entity.pig.death");

    /**
     * The {@code minecraft:entity.pig.eat} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PIG_EAT = of("entity.pig.eat");

    /**
     * The {@code minecraft:entity.pig.hurt} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PIG_HURT = of("entity.pig.hurt");

    /**
     * The {@code minecraft:entity.pig.step} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PIG_STEP = of("entity.pig.step");

    // --- Pig ("mini" sound variant) ---
    /**
     * The {@code minecraft:entity.pig_mini.ambient} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PIG_MINI_AMBIENT = of("entity.pig_mini.ambient");

    /**
     * The {@code minecraft:entity.pig_mini.death} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PIG_MINI_DEATH = of("entity.pig_mini.death");

    /**
     * The {@code minecraft:entity.pig_mini.eat} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PIG_MINI_EAT = of("entity.pig_mini.eat");

    /**
     * The {@code minecraft:entity.pig_mini.hurt} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PIG_MINI_HURT = of("entity.pig_mini.hurt");

    // --- Pig ("big" sound variant) ---
    /**
     * The {@code minecraft:entity.pig_big.ambient} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PIG_BIG_AMBIENT = of("entity.pig_big.ambient");

    /**
     * The {@code minecraft:entity.pig_big.death} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PIG_BIG_DEATH = of("entity.pig_big.death");

    /**
     * The {@code minecraft:entity.pig_big.eat} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PIG_BIG_EAT = of("entity.pig_big.eat");

    /**
     * The {@code minecraft:entity.pig_big.hurt} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PIG_BIG_HURT = of("entity.pig_big.hurt");

    // --- Baby pig ---
    /**
     * The {@code minecraft:entity.baby_pig.ambient} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type BABY_PIG_AMBIENT = of("entity.baby_pig.ambient");

    /**
     * The {@code minecraft:entity.baby_pig.death} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type BABY_PIG_DEATH = of("entity.baby_pig.death");

    /**
     * The {@code minecraft:entity.baby_pig.eat} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type BABY_PIG_EAT = of("entity.baby_pig.eat");

    /**
     * The {@code minecraft:entity.baby_pig.hurt} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type BABY_PIG_HURT = of("entity.baby_pig.hurt");

    /**
     * The {@code minecraft:entity.baby_pig.step} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type BABY_PIG_STEP = of("entity.baby_pig.step");

    // --- Mooshroom ---
    /**
     * The {@code minecraft:entity.mooshroom.convert} sound event, played when lightning changes the
     * colour of a mooshroom.
     *
     * @since 0.1.0
     */
    public static final Sound.Type MOOSHROOM_CONVERT = of("entity.mooshroom.convert");

    /**
     * The {@code minecraft:entity.mooshroom.eat} sound event, played when a brown mooshroom eats a
     * flower.
     *
     * @since 0.1.0
     */
    public static final Sound.Type MOOSHROOM_EAT = of("entity.mooshroom.eat");

    /**
     * The {@code minecraft:entity.mooshroom.milk} sound event, played when a bowl is filled with
     * mushroom stew.
     *
     * @since 0.1.0
     */
    public static final Sound.Type MOOSHROOM_MILK = of("entity.mooshroom.milk");

    /**
     * The {@code minecraft:entity.mooshroom.suspicious_milk} sound event, played when a bowl is
     * filled with suspicious stew.
     *
     * @since 0.1.0
     */
    public static final Sound.Type MOOSHROOM_SUSPICIOUS_MILK = of("entity.mooshroom.suspicious_milk");

    /**
     * The {@code minecraft:entity.mooshroom.shear} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type MOOSHROOM_SHEAR = of("entity.mooshroom.shear");

    // --- Experience ---
    /**
     * The {@code minecraft:entity.player.levelup} sound event, played every fifth level a player
     * reaches.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PLAYER_LEVELUP = of("entity.player.levelup");

    // --- Golden dandelion ---
    /**
     * The {@code minecraft:item.golden_dandelion.use} sound event, played when a baby stops growing.
     *
     * @since 0.1.0
     */
    public static final Sound.Type GOLDEN_DANDELION_USE = of("item.golden_dandelion.use");

    /**
     * The {@code minecraft:item.golden_dandelion.unuse} sound event, played when a baby grows again.
     *
     * @since 0.1.0
     */
    public static final Sound.Type GOLDEN_DANDELION_UNUSE = of("item.golden_dandelion.unuse");

    // --- Bat ---
    /**
     * The {@code minecraft:entity.bat.ambient} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type BAT_AMBIENT = of("entity.bat.ambient");

    /**
     * The {@code minecraft:entity.bat.hurt} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type BAT_HURT = of("entity.bat.hurt");

    /**
     * The {@code minecraft:entity.bat.death} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type BAT_DEATH = of("entity.bat.death");

    /**
     * The {@code minecraft:entity.bat.takeoff} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type BAT_TAKEOFF = of("entity.bat.takeoff");

    // --- Zombie ---
    /**
     * The {@code minecraft:entity.zombie.ambient} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type ZOMBIE_AMBIENT = of("entity.zombie.ambient");

    /**
     * The {@code minecraft:entity.zombie.hurt} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type ZOMBIE_HURT = of("entity.zombie.hurt");

    /**
     * The {@code minecraft:entity.zombie.death} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type ZOMBIE_DEATH = of("entity.zombie.death");

    /**
     * The {@code minecraft:entity.zombie.step} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type ZOMBIE_STEP = of("entity.zombie.step");

    /**
     * The {@code minecraft:entity.zombie.infect} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type ZOMBIE_INFECT = of("entity.zombie.infect");

    /**
     * The {@code minecraft:entity.zombie.attack_wooden_door} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type ZOMBIE_ATTACK_WOODEN_DOOR = of("entity.zombie.attack_wooden_door");

    /**
     * The {@code minecraft:entity.zombie.break_wooden_door} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type ZOMBIE_BREAK_WOODEN_DOOR = of("entity.zombie.break_wooden_door");

    /**
     * The {@code minecraft:entity.zombie.destroy_egg} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type ZOMBIE_DESTROY_EGG = of("entity.zombie.destroy_egg");

    /**
     * The {@code minecraft:entity.zombie.converted_to_drowned} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type ZOMBIE_CONVERTED_TO_DROWNED = of("entity.zombie.converted_to_drowned");

    /**
     * The {@code minecraft:entity.zombie.converted_to_frostbite} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type ZOMBIE_CONVERTED_TO_FROSTBITE = of("entity.zombie.converted_to_frostbite");

    // --- Husk ---
    /**
     * The {@code minecraft:entity.husk.ambient} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type HUSK_AMBIENT = of("entity.husk.ambient");

    /**
     * The {@code minecraft:entity.husk.hurt} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type HUSK_HURT = of("entity.husk.hurt");

    /**
     * The {@code minecraft:entity.husk.death} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type HUSK_DEATH = of("entity.husk.death");

    /**
     * The {@code minecraft:entity.husk.step} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type HUSK_STEP = of("entity.husk.step");

    /**
     * The {@code minecraft:entity.husk.converted_to_zombie} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type HUSK_CONVERTED_TO_ZOMBIE = of("entity.husk.converted_to_zombie");

    // --- Drowned ---
    /**
     * The {@code minecraft:entity.drowned.ambient} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type DROWNED_AMBIENT = of("entity.drowned.ambient");

    /**
     * The {@code minecraft:entity.drowned.hurt} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type DROWNED_HURT = of("entity.drowned.hurt");

    /**
     * The {@code minecraft:entity.drowned.death} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type DROWNED_DEATH = of("entity.drowned.death");

    /**
     * The {@code minecraft:entity.drowned.step} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type DROWNED_STEP = of("entity.drowned.step");

    /**
     * The {@code minecraft:entity.drowned.converted_to_zombie} sound event.
     *
     * @since 0.1.0
     */
    @SinceMinecraft("26.4")
    public static final Sound.Type DROWNED_CONVERTED_TO_ZOMBIE = of("entity.drowned.converted_to_zombie");

    // --- Frostbite ---
    /**
     * The {@code minecraft:entity.frostbite.ambient} sound event.
     *
     * @since 0.1.0
     */
    @SinceMinecraft("26.4")
    public static final Sound.Type FROSTBITE_AMBIENT = of("entity.frostbite.ambient");

    /**
     * The {@code minecraft:entity.frostbite.hurt} sound event.
     *
     * @since 0.1.0
     */
    @SinceMinecraft("26.4")
    public static final Sound.Type FROSTBITE_HURT = of("entity.frostbite.hurt");

    /**
     * The {@code minecraft:entity.frostbite.death} sound event.
     *
     * @since 0.1.0
     */
    @SinceMinecraft("26.4")
    public static final Sound.Type FROSTBITE_DEATH = of("entity.frostbite.death");

    /**
     * The {@code minecraft:entity.frostbite.step} sound event.
     *
     * @since 0.1.0
     */
    @SinceMinecraft("26.4")
    public static final Sound.Type FROSTBITE_STEP = of("entity.frostbite.step");

    /**
     * The {@code minecraft:entity.frostbite.throw} sound event.
     *
     * @since 0.1.0
     */
    @SinceMinecraft("26.4")
    public static final Sound.Type FROSTBITE_THROW = of("entity.frostbite.throw");

    /**
     * The {@code minecraft:entity.frostbite.converted_to_zombie} sound event.
     *
     * @since 0.1.0
     */
    @SinceMinecraft("26.4")
    public static final Sound.Type FROSTBITE_CONVERTED_TO_ZOMBIE = of("entity.frostbite.converted_to_zombie");

    // --- Ice Ball ---
    /**
     * The {@code minecraft:entity.ice_ball.throw} sound event.
     *
     * @since 0.1.0
     */
    @SinceMinecraft("26.4")
    public static final Sound.Type ICE_BALL_THROW = of("entity.ice_ball.throw");

    /**
     * The {@code minecraft:entity.ice_ball.break} sound event.
     *
     * @since 0.1.0
     */
    @SinceMinecraft("26.4")
    public static final Sound.Type ICE_BALL_BREAK = of("entity.ice_ball.break");

    // --- Generic ---
    /**
     * The {@code minecraft:entity.generic.explode} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type GENERIC_EXPLODE = of("entity.generic.explode");

    /**
     * The {@code minecraft:entity.generic.burn} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type GENERIC_BURN = of("entity.generic.burn");

    // -- Player ---
    /**
     * The {@code minecraft:entity.player.attack.strong} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PLAYER_ATTACK_STRONG = of("entity.player.attack.strong");

    /**
     * The {@code minecraft:entity.player.attack.weak} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PLAYER_ATTACK_WEAK = of("entity.player.attack.weak");

    /**
     * The {@code minecraft:entity.player.attack.nodamage} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PLAYER_ATTACK_NODAMAGE = of("entity.player.attack.nodamage");

    /**
     * The {@code minecraft:entity.player.attack.knockback} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PLAYER_ATTACK_KNOCKBACK = of("entity.player.attack.knockback");

    /**
     * The {@code minecraft:entity.player.attack.crit} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PLAYER_ATTACK_CRIT = of("entity.player.attack.crit");

    /**
     * The {@code minecraft:entity.player.attack.sweep} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PLAYER_ATTACK_SWEEP = of("entity.player.attack.sweep");

    /**
     * The {@code minecraft:entity.player.big_fall} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PLAYER_BIG_FALL = of("entity.player.big_fall");

    /**
     * The {@code minecraft:entity.player.small_fall} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PLAYER_SMALL_FALL = of("entity.player.small_fall");

    /**
     * The {@code minecraft:entity.player.burp} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PLAYER_BURP = of("entity.player.burp");

    /**
     * The {@code minecraft:entity.player.splash} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PLAYER_SPLASH = of("entity.player.splash");

    /**
     * The {@code minecraft:entity.player.hurt} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PLAYER_HURT = of("entity.player.hurt");

    /**
     * The {@code minecraft:entity.player.death} sound event.
     *
     * @since 0.1.0
     */
    public static final Sound.Type PLAYER_DEATH = of("entity.player.death");

    private SoundEvents() {
    }

    /**
     * {@return the sound type of a sound event}
     *
     * @param path the sound event key, for instance {@code entity.cow.ambient}
     * @since 0.1.0
     */
    public static Sound.Type of(@KeyPattern final String path) {
        final Key key = Key.key(path);
        return () -> key;
    }
}
