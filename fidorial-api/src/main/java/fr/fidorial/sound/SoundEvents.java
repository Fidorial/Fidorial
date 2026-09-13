package fr.fidorial.sound;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.sound.Sound;

public class SoundEvents {

    // --- Creeper ---
    public static final Sound.Type CREEPER_PRIMED = of("entity.creeper.primed");
    public static final Sound.Type CREEPER_HURT = of("entity.creeper.hurt");
    public static final Sound.Type CREEPER_DEATH = of("entity.creeper.death");

    // --- Chicken ---
    public static final Sound.Type CHICKEN_AMBIENT = of("entity.chicken.ambient");
    public static final Sound.Type CHICKEN_HURT = of("entity.chicken.hurt");
    public static final Sound.Type CHICKEN_DEATH = of("entity.chicken.death");
    public static final Sound.Type CHICKEN_STEP = of("entity.chicken.step");
    public static final Sound.Type CHICKEN_EGG = of("entity.chicken.egg");

    // --- Cow ---
    public static final Sound.Type COW_AMBIENT = of("entity.cow.ambient");
    public static final Sound.Type COW_HURT = of("entity.cow.hurt");
    public static final Sound.Type COW_DEATH = of("entity.cow.death");
    public static final Sound.Type COW_STEP = of("entity.cow.step");
    public static final Sound.Type COW_MILK = of("entity.cow.milk");

    // --- Cow (variante sonore "moody") ---
    public static final Sound.Type COW_MOODY_AMBIENT = of("entity.cow_moody.ambient");
    public static final Sound.Type COW_MOODY_HURT = of("entity.cow_moody.hurt");
    public static final Sound.Type COW_MOODY_DEATH = of("entity.cow_moody.death");


    // --- Bat ---
    public static final Sound.Type BAT_AMBIENT = of("entity.bat.ambient");
    public static final Sound.Type BAT_HURT = of("entity.bat.hurt");
    public static final Sound.Type BAT_DEATH = of("entity.bat.death");
    public static final Sound.Type BAT_TAKEOFF = of("entity.bat.takeoff");

    // --- Zombie ---
    public static final Sound.Type ZOMBIE_AMBIENT = of("entity.zombie.ambient");
    public static final Sound.Type ZOMBIE_HURT = of("entity.zombie.hurt");
    public static final Sound.Type ZOMBIE_DEATH = of("entity.zombie.death");
    public static final Sound.Type ZOMBIE_STEP = of("entity.zombie.step");
    public static final Sound.Type ZOMBIE_INFECT = of("entity.zombie.infect");
    public static final Sound.Type ZOMBIE_ATTACK_WOODEN_DOOR = of("entity.zombie.attack_wooden_door");
    public static final Sound.Type ZOMBIE_BREAK_WOODEN_DOOR = of("entity.zombie.break_wooden_door");
    public static final Sound.Type ZOMBIE_DESTROY_EGG = of("entity.zombie.destroy_egg");
    public static final Sound.Type ZOMBIE_CONVERTED_TO_DROWNED = of("entity.zombie.converted_to_drowned");

    // --- Generic ---
    public static final Sound.Type GENERIC_EXPLODE = of("entity.generic.explode");
    public static final Sound.Type GENERIC_BURN = of("entity.generic.burn");

    // -- Player ---
    public static final Sound.Type PLAYER_ATTACK_STRONG = of("entity.player.attack.strong");
    public static final Sound.Type PLAYER_ATTACK_WEAK = of("entity.player.attack.weak");
    public static final Sound.Type PLAYER_ATTACK_NODAMAGE = of("entity.player.attack.nodamage");
    public static final Sound.Type PLAYER_ATTACK_KNOCKBACK = of("entity.player.attack.knockback");
    public static final Sound.Type PLAYER_ATTACK_CRIT = of("entity.player.attack.crit");
    public static final Sound.Type PLAYER_ATTACK_SWEEP = of("entity.player.attack.sweep");
    public static final Sound.Type PLAYER_BIG_FALL = of("entity.player.big_fall");
    public static final Sound.Type PLAYER_SMALL_FALL = of("entity.player.small_fall");
    public static final Sound.Type PLAYER_BURP = of("entity.player.burp");
    public static final Sound.Type PLAYER_SPLASH = of("entity.player.splash");
    public static final Sound.Type PLAYER_HURT = of("entity.player.hurt");
    public static final Sound.Type PLAYER_DEATH = of("entity.player.death");

    // --- Blocks ---
    public static final Sound.Type ENDER_CHEST_OPEN = of("block.ender_chest.open");
    public static final Sound.Type ENDER_CHEST_CLOSE = of("block.ender_chest.close");

    // --- Items ---
    public static final Sound.Type HOE_TILL = of("item.hoe.till");
    public static final Sound.Type CROP_PLANT = of("item.crop.plant");

    private SoundEvents() {
    }

    public static Sound.Type of(@KeyPattern final String path) {
        final Key key = Key.key(path);
        return () -> key;
    }
}
