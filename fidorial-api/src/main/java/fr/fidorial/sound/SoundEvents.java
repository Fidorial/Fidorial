package fr.fidorial.sound;

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
