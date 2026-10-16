package fr.euphyllia.fidorial.testplugin.mob;

import fr.fidorial.combat.CombatService;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.Player;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.entity.mob.Mob;
import fr.fidorial.math.Location;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class ChargeGoal implements Goal {

    private static final int WARMUP_TICKS = 20;

    private static final int CHARGE_TICKS = 60;

    private static final int COOLDOWN_TICKS = 70;

    private static final double TRIGGER_RANGE = 12.0;

    private static final double GIVE_UP_RANGE = 24.0;

    private static final double CONTACT_MARGIN = 1.1;

    private static final double GORE_KNOCKBACK = 1.35;

    private static final double GORE_LIFT = 0.55;

    private static final int REPATH_INTERVAL = 4;

    private static final Sound.Type WARMUP_SOUND = SoundEvents.of("entity.ravager.step");
    private static final Sound.Type CHARGE_SOUND = SoundEvents.of("entity.ravager.roar");
    private static final Sound.Type IMPACT_SOUND = SoundEvents.of("entity.ravager.attack");

    private final Mob mob;
    private final int priority;
    private final double chargeSpeed;

    private Phase phase = Phase.IDLE;
    private int phaseTicks;
    private int cooldown;
    private @Nullable Player victim;

    public ChargeGoal(final Mob mob, final int priority, final double chargeSpeed) {
        this.mob = mob;
        this.priority = priority;
        this.chargeSpeed = chargeSpeed;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public boolean canStart() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        final Player target = pickTarget();
        if (target == null) {
            return false;
        }
        victim = target;
        return true;
    }

    @Override
    public boolean shouldContinue() {
        final Player target = victim;
        if (target == null || !target.isAlive() || target.isRemoved() || mob.isDead()) {
            return false;
        }
        if (mob.distanceSqTo(target) > GIVE_UP_RANGE * GIVE_UP_RANGE) {
            return false;
        }
        return phase != Phase.IDLE;
    }

    @Override
    public void start() {
        phase = Phase.WARMUP;
        phaseTicks = 0;
        mob.navigation().stop();
        mob.playSound(WARMUP_SOUND, 1.0f, 0.7f);
    }

    @Override
    public void tick() {
        final Player target = victim;
        if (target == null) {
            return;
        }

        phaseTicks++;
        mob.lookAt(target);

        switch (phase) {
            case WARMUP -> tickWarmup();
            case CHARGE -> tickCharge(target);
            case IDLE -> {
            }
        }
    }

    @Override
    public void stop() {
        phase = Phase.IDLE;
        phaseTicks = 0;
        victim = null;
        mob.navigation().stop();
        mob.setMoveSpeed(0.0);
        if (cooldown <= 0) {
            cooldown = COOLDOWN_TICKS;
        }
    }

    public boolean isCharging() {
        return phase == Phase.CHARGE;
    }

    private void tickWarmup() {
        mob.setMoveSpeed(0.0);
        if (phaseTicks % 6 == 0) {
            mob.playSound(WARMUP_SOUND, 0.8f, 0.6f);
        }
        if (phaseTicks >= WARMUP_TICKS) {
            phase = Phase.CHARGE;
            phaseTicks = 0;
            mob.playSound(CHARGE_SOUND, 1.2f, 1.0f);
        }
    }

    private void tickCharge(final Player target) {
        if (phaseTicks >= CHARGE_TICKS) {
            phase = Phase.IDLE;
            return;
        }

        if (phaseTicks % REPATH_INTERVAL == 1) {
            mob.navigation().moveTo(mob.location(), target.location());
        }
        mob.setMoveSpeed(chargeSpeed);

        if (touches(target)) {
            gore(target);
            phase = Phase.IDLE;
        }
    }

    private boolean touches(final Player target) {
        final double reach = mob.width() * 0.5 + CONTACT_MARGIN;
        return mob.distanceSqTo(target) <= reach * reach;
    }

    private void gore(final Player target) {
        mob.playSound(IMPACT_SOUND, 1.0f, 1.0f);

        final float damage = Math.max(1f, mob.attackDamage());
        if (!target.damage(DamageSource.mobAttack(mob), damage)) {
            return;
        }

        final Location origin = mob.location();
        combat().ifPresent(service -> service.knockback(target, GORE_KNOCKBACK, origin.x(), origin.z()));
        mob.setVelocity(mob.velocityX() * -0.4, GORE_LIFT * 0.5, mob.velocityZ() * -0.4);
    }

    private @Nullable Player pickTarget() {
        final Player target = mob.target();
        if (target != null && target.isAlive() && mob.distanceSqTo(target) <= TRIGGER_RANGE * TRIGGER_RANGE) {
            return target;
        }
        final Player nearest = mob.nearestPlayer(TRIGGER_RANGE);
        return nearest != null && nearest.isAlive() ? nearest : null;
    }

    private Optional<CombatService> combat() {
        return mob.server().services().find(CombatService.class);
    }

    private enum Phase {
        IDLE,
        WARMUP,
        CHARGE
    }
}
