package fr.euphyllia.fidorial.testplugin.mob;

import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.mob.Mob;
import fr.fidorial.entity.mob.MobBehaviour;
import fr.fidorial.entity.player.Player;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.sound.Sound;

import java.util.concurrent.ThreadLocalRandom;

public final class CompanionBehaviour implements MobBehaviour {

    private static final int FOLLOW_PRIORITY = 1;

    private static final int WANDER_PRIORITY = 5;

    private static final int IDLE_SOUND_CHANCE = 200;

    private static final Sound.Type IDLE = SoundEvents.of("entity.axolotl.idle_air");
    private static final Sound.Type SPLASH = SoundEvents.of("entity.axolotl.splash");

    private final Mob mob;

    public CompanionBehaviour(final Mob mob) {
        this.mob = mob;
    }

    @Override
    public void onSpawn() {
        mob.goals().add(new FollowPlayerGoal(mob, FOLLOW_PRIORITY, mob.movementSpeed()));
        mob.goals().add(new WanderGoal(mob, WANDER_PRIORITY, mob.movementSpeed() * 0.6));
    }

    @Override
    public void onTick(final long currentTick) {
        if (ThreadLocalRandom.current().nextInt(IDLE_SOUND_CHANCE) == 0) {
            mob.playSound(IDLE, 0.6f, 1.0f);
        }
    }

    @Override
    public void onHurt(final DamageSource source, final float amount) {
        mob.playSound(SPLASH, 1.0f, 1.4f);
    }

    @Override
    public boolean onInteract(final Player player, final EquipmentSlotGroup hand) {
        mob.playSound(IDLE, 1.0f, 1.5f);
        mob.lookAt(player);
        return false;
    }
}
