package fr.euphyllia.fidorial.server.entity.mob;

import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.ai.Targeting;
import fr.euphyllia.fidorial.server.entity.mob.monster.LocalDifficulty;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.world.entity.AnvilEntitySerializer;
import fr.fidorial.entity.GameMode;
import fr.fidorial.registry.keys.GameRuleKeys;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The grudge of a mob that is not hostile by nature: once provoked it stays angry at whoever
 * provoked it until a time of the world, and keeps it when saved. With the {@code universal_anger}
 * game rule, a mob angry at no one in particular is angry at every player.
 *
 * <p>The mob calls {@link #update(boolean)} once per tick: its target becomes the one it is angry
 * at, and the anger lasts as long as it keeps after it.</p>
 */
public final class PersistentAnger {

    /**
     * The end time of a mob that is not angry.
     */
    public static final long NO_END_TIME = -1L;

    private static final String TAG_END_TIME = "anger_end_time";
    private static final String TAG_ANGRY_AT = "angry_at";
    private static final String TAG_OLD_ANGER_TIME = "AngerTime";
    private static final int UUID_INTS = 4;

    private final AbstractPathfinderMob mob;
    private final int minTicks;
    private final int maxTicks;
    private long endTime = NO_END_TIME;
    private @Nullable UUID angryAt;

    /**
     * @param mob      the mob that holds the grudge
     * @param minTicks how long, at least, the mob stays angry once provoked
     * @param maxTicks how long, at most, the mob stays angry once provoked
     */
    public PersistentAnger(final AbstractPathfinderMob mob, final int minTicks, final int maxTicks) {
        this.mob = mob;
        this.minTicks = minTicks;
        this.maxTicks = maxTicks;
    }

    /**
     * {@return the time of the world at which the anger ends}, {@link #NO_END_TIME} when the mob is
     * not angry
     */
    public long endTime() {
        return endTime;
    }

    /**
     * {@return the one the mob is angry at, {@code null} if it is angry at no one in particular}
     */
    public @Nullable UUID angryAt() {
        return angryAt;
    }

    /**
     * {@return {@code true} until the anger runs out}
     */
    public boolean isAngry() {
        return endTime > 0L && endTime - gameTime() > 0L;
    }

    /**
     * Makes the anger last a random time from now, between the bounds given at creation.
     */
    public void start() {
        remainAngryFor(ThreadLocalRandom.current().nextInt(minTicks, maxTicks + 1));
    }

    /**
     * Makes the anger last a given time from now.
     *
     * @param ticks how long the mob stays angry
     */
    public void remainAngryFor(final long ticks) {
        endTime = gameTime() + ticks;
    }

    /**
     * {@return {@code true} if the mob is angry at a living entity}: it may attack it, and it is the
     * one the mob holds a grudge against, or a player while the mob is angry at every player
     */
    public boolean isAngryAt(final AbstractLivingEntity entity) {
        if (!Targeting.canAttack(mob, entity)) {
            return false;
        }
        if (isValidPlayerTarget(entity) && isAngryAtAllPlayers()) {
            return true;
        }
        return angryAt != null && angryAt.equals(entity.uuid());
    }

    /**
     * {@return {@code true} if the mob is angry at every player}: with the {@code universal_anger}
     * game rule, while it is angry at no one in particular
     */
    public boolean isAngryAtAllPlayers() {
        return mob.serverWorld().gameRuleValues().getBoolean(GameRuleKeys.UNIVERSAL_ANGER) && isAngry() && angryAt == null;
    }

    /**
     * Follows the target of the mob, once per tick: the mob becomes angry at a new target, and stays
     * angry while it has one if asked to. The anger ends when the target it was about died as a mob,
     * when it ran out and the mob lost its target, or when the player it was about may no longer be
     * attacked.
     *
     * @param stayAngryIfTargetPresent {@code true} to start the anger over each tick the mob has a
     *                                 target
     */
    public void update(final boolean stayAngryIfTargetPresent) {
        final AbstractLivingEntity previous = mob.target();
        if (previous != null && previous.isDead() && previous instanceof AbstractMob && matches(previous)) {
            stop();
            return;
        }
        final AbstractLivingEntity target = previous != null && Targeting.canAttack(mob, previous) ? previous : null;
        if (target != null) {
            final boolean newTarget = !matches(target);
            if (newTarget) {
                angryAt = target.uuid();
            }
            if (newTarget || stayAngryIfTargetPresent) {
                start();
            }
        }
        if (angryAt != null && !isAngry()
                && (target == null || !isValidPlayerTarget(target) || !stayAngryIfTargetPresent)) {
            stop();
        }
        if (angryAt != null && mob.serverWorld().entityManager().byUuid(angryAt) instanceof final ServerPlayer player
                && (player.gameMode() == GameMode.CREATIVE || player.gameMode() == GameMode.SPECTATOR || isPeaceful())) {
            stop();
        }
    }

    /**
     * Ends the anger: the mob forgets who hurt it and gives up its target.
     */
    public void stop() {
        mob.forgetLastHurtByMob();
        angryAt = null;
        mob.setTarget(null);
        endTime = NO_END_TIME;
    }

    /**
     * Ends the anger and starts it over at no one in particular: with the {@code universal_anger}
     * game rule, the mob is then angry at every player.
     */
    public void forgetCurrentTargetAndRefreshUniversalAnger() {
        stop();
        start();
    }

    /**
     * Forgives a player that died, with the {@code forgive_dead_players} game rule.
     *
     * @param player the player that died
     */
    public void playerDied(final ServerPlayer player) {
        if (player.world().equals(mob.world())
                && mob.serverWorld().gameRuleValues().getBoolean(GameRuleKeys.FORGIVE_DEAD_PLAYERS)
                && matches(player)) {
            stop();
        }
    }

    /**
     * Writes the anger of the mob.
     */
    public void save(final CompoundBinaryTag.Builder tag) {
        tag.putLong(TAG_END_TIME, endTime);
        if (angryAt != null) {
            tag.putIntArray(TAG_ANGRY_AT, AnvilEntitySerializer.uuidToInts(angryAt));
        }
    }

    /**
     * Reads the anger of the mob back; it goes after the one it is angry at again if that one is
     * around. An anger written as a number of ticks left is read too.
     */
    public void load(final CompoundBinaryTag tag) {
        if (tag.get(TAG_END_TIME) != null) {
            endTime = tag.getLong(TAG_END_TIME);
        } else if (tag.get(TAG_OLD_ANGER_TIME) != null) {
            remainAngryFor(tag.getInt(TAG_OLD_ANGER_TIME));
        } else {
            endTime = NO_END_TIME;
        }
        angryAt = tag.get(TAG_ANGRY_AT) instanceof final IntArrayBinaryTag ints && ints.size() == UUID_INTS
                ? AnvilEntitySerializer.uuidFromInts(ints.value())
                : null;
        if (angryAt != null) {
            final AbstractEntity found = mob.serverWorld().entityManager().byUuid(angryAt);
            mob.setTarget(found instanceof final AbstractLivingEntity living ? living : null);
        }
    }

    private boolean matches(final AbstractLivingEntity entity) {
        return angryAt != null && angryAt.equals(entity.uuid());
    }

    private boolean isValidPlayerTarget(final AbstractLivingEntity entity) {
        return entity instanceof final ServerPlayer player
                && player.gameMode() != GameMode.CREATIVE && player.gameMode() != GameMode.SPECTATOR
                && !isPeaceful();
    }

    private boolean isPeaceful() {
        return mob.server().worlds().manager().levelData().difficulty == LocalDifficulty.PEACEFUL;
    }

    private long gameTime() {
        return mob.world().dayNightCycle().worldAge();
    }
}
