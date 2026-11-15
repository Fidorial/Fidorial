package fr.euphyllia.fidorial.server.entity.mob;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.ai.Brightness;
import fr.euphyllia.fidorial.server.entity.mob.monster.LocalDifficulty;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.entity.EntityType;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.GameRuleKeys;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A hostile mob: it leaves 5 experience points and its loot whatever its age, likes the dark,
 * goes idle sooner in the light, and does not stay in a peaceful world nor far from every player.
 */
public abstract class AbstractMonster extends AbstractPathfinderMob implements Category.Monster {

    private static final int EXPERIENCE = 5;

    /**
     * Further than this from every player a monster goes away at once, as in Vanilla.
     */
    private static final double DESPAWN_DISTANCE = 128.0;

    /**
     * Closer than this to a player a monster never goes away, as in Vanilla.
     */
    private static final double NO_DESPAWN_DISTANCE = 32.0;
    private static final int IDLE_TICKS_BEFORE_DESPAWN = 600;
    private static final int DESPAWN_CHANCE = 800;
    private static final float BRIGHT = 0.5f;

    protected AbstractMonster(final int entityId, final UUID uuid, final EntityType type,
                              final Location location, final float maxHealth) {
        super(entityId, uuid, type, location, maxHealth);
    }

    @Override
    public void tick(final long currentTick) {
        super.tick(currentTick);
        if (isRemoved() || isDead()) {
            return;
        }
        if (brightness() > BRIGHT) {
            addNoActionTime(2);
        }
        checkDespawn();
    }

    /**
     * Removes this monster from a peaceful world, and, unless it is
     * {@linkplain #isPersistenceRequired() persistent}, once every player is far enough: at once
     * beyond 128 blocks, now and then beyond 32 blocks after 30 seconds without anything to do.
     */
    private void checkDespawn() {
        if (difficulty() == LocalDifficulty.PEACEFUL) {
            server().entities().despawn(this);
            return;
        }
        if (isPersistenceRequired()) {
            resetNoActionTime();
            return;
        }
        final ServerPlayer nearest = nearestPlayer(-1.0);
        if (nearest == null) {
            return;
        }
        final double distanceSq = distanceSqTo(nearest);
        if (distanceSq > DESPAWN_DISTANCE * DESPAWN_DISTANCE) {
            server().entities().despawn(this);
        } else if (distanceSq > NO_DESPAWN_DISTANCE * NO_DESPAWN_DISTANCE) {
            if (noActionTime() > IDLE_TICKS_BEFORE_DESPAWN
                    && ThreadLocalRandom.current().nextInt(DESPAWN_CHANCE) == 0) {
                server().entities().despawn(this);
            }
        } else {
            resetNoActionTime();
        }
    }

    /**
     * {@return the difficulty of the world, from {@code 0} for peaceful to {@code 3} for hard}
     */
    protected final int difficulty() {
        return server().worlds().manager().levelData().difficulty;
    }

    /**
     * {@return the difficulty where this monster stands}
     */
    protected final LocalDifficulty localDifficulty() {
        final Location at = location();
        return LocalDifficulty.at(serverWorld(), (int) Math.floor(at.x()), (int) Math.floor(at.z()));
    }

    /**
     * {@return how bright it is at the eyes of this monster, from {@code 0} to {@code 1}}
     */
    protected final float brightness() {
        final Location at = location();
        return Brightness.at(serverWorld(), (int) Math.floor(at.x()),
                (int) Math.floor(at.y() + eyeHeight()), (int) Math.floor(at.z()));
    }

    /**
     * {@return {@code true} if the sun burns this monster this tick, were it one that burns}: by day,
     * in full light under the open sky, out of the water, the rain and the powder snow, and then
     * only now and then, 4 ticks out of 100 at noon
     */
    protected final boolean isSunBurnTick() {
        if (!Brightness.isDay(serverWorld())) {
            return false;
        }
        final float brightness = brightness();
        if (brightness <= BRIGHT
                || ThreadLocalRandom.current().nextFloat() * 30.0f >= (brightness - 0.4f) * 2.0f) {
            return false;
        }
        if (isInWater() || isInPowderSnow()) {
            return false;
        }
        final Location at = location();
        return Brightness.canSeeSky(serverWorld(), (int) Math.floor(at.x()),
                (int) Math.floor(at.y() + eyeHeight()), (int) Math.floor(at.z()))
                && !serverWorld().weather().isRaining();
    }

    @Override
    public float walkTargetValue(final int x, final int y, final int z) {
        return BRIGHT - Brightness.at(serverWorld(), x, y, z);
    }

    @Override
    protected int experienceReward() {
        return EXPERIENCE;
    }

    @Override
    protected boolean shouldDropExperience() {
        return true;
    }

    @Override
    protected boolean shouldDropLoot() {
        return serverWorld().gameRuleValues().getBoolean(GameRuleKeys.MOB_DROPS);
    }
}
