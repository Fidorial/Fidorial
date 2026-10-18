package fr.euphyllia.fidorial.testplugin.mob;

import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.player.Player;
import fr.fidorial.entity.mob.Mob;
import fr.fidorial.entity.mob.MobBehaviour;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

/**
 * Transforme un herbivore en taureau.
 */
public final class BullBehaviour implements MobBehaviour {

    private static final double SIGHT_RANGE = 20.0;
    private static final double CHARGE_SPEED = 0.62;

    private static final int CHARGE_PRIORITY = -1;
    private static final int WANDER_PRIORITY = 5;

    private static final Sound.Type SNORT = SoundEvents.of("entity.ravager.ambient");

    private final Mob mob;

    private @Nullable ChargeGoal charge;

    public BullBehaviour(final Mob mob) {
        this.mob = mob;
    }

    @Override
    public void onSpawn() {
        mob.setFollowRange(SIGHT_RANGE);

        final ChargeGoal chargeGoal = new ChargeGoal(mob, CHARGE_PRIORITY, CHARGE_SPEED);
        this.charge = chargeGoal;
        mob.goals().add(chargeGoal);

        if (mob.definition().isPresent()) {
            mob.goals().add(new WanderGoal(mob, WANDER_PRIORITY, mob.movementSpeed()));
        }
    }

    @Override
    public void onTick(final long currentTick) {
        final ChargeGoal chargeGoal = charge;
        if (chargeGoal == null) {
            return;
        }

        if (chargeGoal.isCharging() && currentTick % 10 == 0) {
            mob.playSound(SNORT, 0.7f, 1.4f);
        }
    }

    @Override
    public void onHurt(final DamageSource source, final float amount) {
        if (source.causingEntity() instanceof final Player attacker) {
            mob.setTarget(attacker);
            mob.playSound(SNORT, 1.0f, 0.8f);
        }
    }

    @Override
    public boolean onInteract(final Player player, final EquipmentSlotGroup hand) {
        mob.setTarget(player);
        mob.playSound(SNORT, 1.0f, 0.7f);
        return false;
    }
}
