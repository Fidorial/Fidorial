package fr.euphyllia.fidorial.server.entity;

import fr.euphyllia.fidorial.server.entity.effect.FidorialMobEffectRegistry;
import fr.euphyllia.fidorial.server.entity.effect.MobEffectInstanceImpl;
import fr.fidorial.entity.EntityType;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.effect.MobEffectBehaviour;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.event.entity.EntityEffectEvent;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MobEffect;
import net.kyori.adventure.key.Key;
import fr.fidorial.math.Location;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractLivingEntity extends AbstractEntity implements LivingEntity {

    public static final int DEATH_TICKS = 20;
    public static final int INVULNERABILITY_TICKS = 20;
    public static final int INVULNERABILITY_OVERRIDE_THRESHOLD = 10;

    private volatile float maxHealth;
    private volatile float health;
    private volatile float absorption;
    private volatile float lastDamage;
    private volatile int fireTicks;
    private final AtomicInteger invulnerableTicks = new AtomicInteger();
    private final AtomicInteger deathTicks = new AtomicInteger(-1);
    private final Map<Key, MobEffectInstanceImpl> effects = new ConcurrentHashMap<>();

    protected AbstractLivingEntity(
            final int entityId,
            final UUID uuid,
            final EntityType type,
            final Location location,
            final float maxHealth) {
        super(entityId, uuid, type, location);
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    @Override
    public final float health() {
        return health;
    }

    @Override
    public void setHealth(final float health) {
        this.health = Math.clamp(health, 0f, maxHealth);
    }

    @Override
    public final float maxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(final float maxHealth) {
        this.maxHealth = Math.max(1f, maxHealth);
        setHealth(health);
    }

    @Override
    public final float absorptionAmount() {
        return absorption;
    }

    @Override
    public void setAbsorptionAmount(final float absorption) {
        this.absorption = Math.max(0f, absorption);
    }

    @Override
    public final float headYaw() {
        // TODO: implement this
        return 0.0f;
    }

    @Override
    public final void setHeadYaw(final float headYaw) {
        // TODO: implement this
    }

    @Override
    public final int fireTicks() {
        return fireTicks;
    }

    @Override
    public void setFireTicks(final int ticks) {
        this.fireTicks = Math.max(0, ticks);
    }

    public final float lastDamage() {
        return lastDamage;
    }

    public final void setLastDamage(final float lastDamage) {
        this.lastDamage = lastDamage;
    }

    public final int invulnerableTicks() {
        return invulnerableTicks.get();
    }

    public final void setInvulnerableTicks(final int ticks) {
        this.invulnerableTicks.set(Math.max(0, ticks));
    }


    public final boolean isDying() {
        return deathTicks.get() >= 0;
    }

    public final void startDeathAnimation() {
        if (deathTicks.get() < 0) {
            deathTicks.set(DEATH_TICKS);
        }
    }

    protected void tickLiving(final long currentTick) {
        invulnerableTicks.updateAndGet(ticks -> ticks > 0 ? ticks - 1 : 0);
        if (deathTicks.get() > 0 && deathTicks.decrementAndGet() == 0) {
            onDeathAnimationFinished();
        }
        if (!isRemoved() && !isDead()) {
            tickEffects();
        }
    }

    @Override
    public final boolean addEffect(final MobEffectInstance effect) {
        if (!server().effects().isEffect(effect.type())) {
            throw new IllegalArgumentException("Unknown effect " + effect.type().key().asString());
        }
        if (isRemoved() || isDead() || !canBeAffected(effect)) {
            return false;
        }
        final MobEffectInstanceImpl incoming = MobEffectInstanceImpl.of(effect);
        final MobEffectInstanceImpl previous = effects.get(incoming.type().key());
        final MobEffectInstanceImpl merged = previous == null ? incoming : previous.merge(incoming);
        if (merged.equals(previous)) {
            return false;
        }
        final EntityEffectEvent event = server().events().post(new EntityEffectEvent(this,
                previous == null ? EntityEffectEvent.Action.ADDED : EntityEffectEvent.Action.CHANGED,
                previous, merged));
        if (event.isCancelled() || event.effect() == null) {
            return false;
        }
        final MobEffectInstanceImpl applied = MobEffectInstanceImpl.of(event.effect());
        effects.put(applied.type().key(), applied);
        onEffectUpdated(applied);
        dispatch(applied, MobEffectBehaviour::onApplied, "onApplied");
        return true;
    }

    @Override
    public final Optional<MobEffectInstance> effect(final TypedKey<MobEffect> type) {
        return Optional.ofNullable(effects.get(type.key()));
    }

    @Override
    public final boolean hasEffect(final TypedKey<MobEffect> type) {
        return effects.containsKey(type.key());
    }

    @Override
    public final Collection<MobEffectInstance> activeEffects() {
        return List.copyOf(effects.values());
    }

    @Override
    public final boolean clearEffects() {
        boolean removed = false;
        for (final MobEffectInstanceImpl effect : effects.values()) {
            removed |= removeEffect(effect.type().key(), EntityEffectEvent.Action.REMOVED);
        }
        return removed;
    }

    @Override
    public final boolean removeEffect(final TypedKey<MobEffect> type) {
        return removeEffect(type.key(), EntityEffectEvent.Action.REMOVED);
    }

    private boolean removeEffect(final Key type, final EntityEffectEvent.Action action) {
        final MobEffectInstanceImpl current = effects.get(type);
        if (current == null) {
            return false;
        }
        final EntityEffectEvent event = server().events().post(new EntityEffectEvent(this, action, current, null));
        if (event.isCancelled() && action != EntityEffectEvent.Action.EXPIRED) {
            return false;
        }
        if (!effects.remove(type, current)) {
            return false;
        }
        onEffectRemoved(current);
        dispatch(current, MobEffectBehaviour::onRemoved, "onRemoved");
        return true;
    }

    protected final void clearEffectsSilently() {
        effects.clear();
    }
    protected boolean canBeAffected(final MobEffectInstance effect) {
        return true;
    }

    protected void onEffectUpdated(final MobEffectInstance effect) {
    }

    protected void onEffectRemoved(final MobEffectInstance effect) {
    }

    private void dispatch(final MobEffectInstance effect, final BehaviourHook hook, final String name) {
        for (final MobEffectBehaviour behaviour : server().effects().behaviours(effect.type().key())) {
            try {
                hook.call(behaviour, this, effect);
            } catch (final Throwable throwable) {
                LOGGER.error("Error in the {} of {} for {} on {}",
                        name, behaviour.getClass().getName(), effect.type().key(), this, throwable);
            }
        }
    }

    @FunctionalInterface
    private interface BehaviourHook {
        void call(MobEffectBehaviour behaviour, LivingEntity entity, MobEffectInstance effect);
    }

    private void tickEffects() {
        if (effects.isEmpty()) {
            return;
        }
        final FidorialMobEffectRegistry registry = server().effects();
        for (final MobEffectInstanceImpl effect : effects.values()) {
            if (!registry.isKnown(effect.type().key())) {
                removeEffect(effect.type().key(), EntityEffectEvent.Action.REMOVED);
                continue;
            }
            dispatch(effect, MobEffectBehaviour::onTick, "onTick");
            if (isRemoved() || isDead()) {
                return;
            }
            final MobEffectInstanceImpl next = effect.tickDown();
            if (next.hasExpired()) {
                removeEffect(effect.type().key(), EntityEffectEvent.Action.EXPIRED);
            } else if (next != effect) {
                effects.replace(effect.type().key(), effect, next);
            }
        }
    }

    protected void onDeathAnimationFinished() {
        remove();
    }
}
