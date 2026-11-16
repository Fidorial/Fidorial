package fr.euphyllia.fidorial.server.entity.mob;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.ai.Brightness;
import fr.euphyllia.fidorial.server.entity.mob.monster.LocalDifficulty;
import fr.euphyllia.fidorial.server.util.annotations.NeedsToBeRevisited;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.EntityType;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.world.dimension.IntProvider;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A hostile mob: it leaves 5 experience points and its loot whatever its age, likes the dark,
 * goes idle sooner in the light, and does not stay in a peaceful world nor far from every player.
 */
public abstract class AbstractMonster extends AbstractPathfinderMob implements Category.Monster {

    private static final int EXPERIENCE = 5;

    private static final float BRIGHT = 0.5f;

    /**
     * How much light the sky loses under a thunderstorm when monsters look for a place to spawn.
     */
    private static final int THUNDER_SKY_DARKENING = 10;

    protected AbstractMonster(final int entityId, final UUID uuid, final EntityType type,
                              final Location location, final float maxHealth) {
        super(entityId, uuid, type, location, maxHealth);
    }

    /**
     * {@return {@code true} if a monster may spawn on a block}: on solid ground, where it is
     * {@linkplain #isDarkEnoughToSpawn dark enough}
     */
    public static boolean checkMonsterSpawnRules(final ServerWorld world, final int x, final int y, final int z) {
        return checkAnyLightMonsterSpawnRules(world, x, y, z) && isDarkEnoughToSpawn(world, x, y, z);
    }

    /**
     * {@return {@code true} if a monster that minds no light may spawn on a block}: on solid ground,
     * unless the world is peaceful
     */
    @NeedsToBeRevisited("Difficulty per world")
    public static boolean checkAnyLightMonsterSpawnRules(final ServerWorld world, final int x, final int y, final int z) {
        return FidorialServer.getInstance().worlds().manager().levelData().difficulty != LocalDifficulty.PEACEFUL
                && BlockView.isSolidGround(world, x, y - 1, z);
    }

    /**
     * {@return {@code true} if a monster of the surface may spawn on a block}: where
     * {@linkplain #checkMonsterSpawnRules any monster may}, under the open sky
     */
    public static boolean checkSurfaceMonsterSpawnRules(final ServerWorld world, final int x, final int y, final int z) {
        return checkMonsterSpawnRules(world, x, y, z) && Brightness.canSeeSky(world, x, y, z);
    }

    /**
     * {@return {@code true} if a block is dark enough for a monster to spawn}: the more sky light it
     * gets the less likely, never in the light of a block where the dimension forbids it, and in a
     * light that does not go above what the dimension draws. A thunderstorm counts as the night.
     */
    public static boolean isDarkEnoughToSpawn(final ServerWorld world, final int x, final int y, final int z) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        if (world.dimensionType().hasSkylight() && world.skyLightAt(x, y, z) > random.nextInt(32)) {
            return false;
        }
        final int blockLightLimit = world.dimensionType().monsterSpawnBlockLightLimit();
        if (blockLightLimit < 15 && world.blockLightAt(x, y, z) > blockLightLimit) {
            return false;
        }
        final int light = world.weather().isThundering()
                ? Brightness.light(world, x, y, z, THUNDER_SKY_DARKENING)
                : Brightness.light(world, x, y, z);
        return light <= sample(world.dimensionType().monsterSpawnLightLevel(), random);
    }

    private static int sample(final IntProvider provider, final ThreadLocalRandom random) {
        return switch (provider) {
            case final IntProvider.Constant constant -> constant.value();
            case final IntProvider.Uniform uniform ->
                    random.nextInt(uniform.minInclusive(), uniform.maxInclusive() + 1);
            default -> 0;
        };
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

    @Override
    protected final boolean despawnsInPeaceful() {
        return true;
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
