package fr.euphyllia.fidorial.server.entity.mob.monster.cube;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.ai.Brightness;
import fr.euphyllia.fidorial.server.entity.ai.goal.NearestAttackableTargetGoal;
import fr.euphyllia.fidorial.server.entity.mob.misc.IronGolem;
import fr.euphyllia.fidorial.server.entity.mob.monster.LocalDifficulty;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.mob.monster.Slime;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BiomeTagKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.TimelineKeys;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.dimension.TimelineReference;
import net.kyori.adventure.sound.Sound;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The slime: a green cube that goes after the players around, as long as they are no more than 4
 * blocks above or below it, and the iron golems. It hits as hard as it is big, but a tiny one does
 * not hurt; only a tiny one drops slime balls.
 *
 * <p>It spawns in the slime chunks below a height of 40, and in the swamps at night between the
 * heights of 50 and 70, the more often the fuller the moon.</p>
 */
public final class SlimeEntity extends AbstractCubeMob implements Slime {

    private static final double BASE_EYE_HEIGHT = 0.325;
    private static final double TARGET_HEIGHT_RANGE = 4.0;
    private static final int TARGET_INTERVAL = 10;
    private static final int MAX_SLIME_BALLS = 2;

    private static final int SURFACE_MIN_Y = 50;
    private static final int SURFACE_MAX_Y = 70;
    private static final int SLIME_CHUNK_MAX_Y = 40;
    private static final int SLIME_CHUNK_CHANCE = 10;
    private static final long SLIME_CHUNK_SALT = 987_234_911L;
    /**
     * How likely a slime is to spawn in a swamp for each phase of the moon, from the full moon on.
     */
    private static final float[] SURFACE_SPAWN_CHANCE = {0.5f, 0.375f, 0.25f, 0.125f, 0.0f, 0.125f, 0.25f, 0.375f};
    private static final int SURFACE_MAX_LIGHT = 8;

    /**
     * Creates a slime as one that spawns, with the random traits of one and a random size.
     */
    public SlimeEntity(final int entityId, final Location location) {
        this(entityId, location, true);
    }

    private SlimeEntity(final int entityId, final Location location, final boolean spawned) {
        super(entityId, EntityTypes.SLIME, location, spawned);
        goals.add(new AttackGoal(2));
        goals.add(new NearestAttackableTargetGoal<>(this, 1, ServerPlayer.class, TARGET_INTERVAL, true,
                player -> Math.abs(player.location().y() - location().y()) <= TARGET_HEIGHT_RANGE));
        goals.add(new NearestAttackableTargetGoal<>(this, 3, IronGolem.class, true));
    }

    @Override
    protected AbstractCubeMob newSplitCube(final int entityId, final Location location) {
        return new SlimeEntity(entityId, location, false);
    }

    /**
     * {@return {@code true} if a slime may spawn on a block}, unless the world is peaceful: in a
     * biome of the {@code minecraft:allows_surface_slime_spawns} tag between the heights of 50 and
     * 70, depending on the moon and in a light of 7 at most, or else once in ten tries in a slime
     * chunk below a height of 40; on a block it can stand on
     */
    public static boolean canSpawn(final ServerWorld world, final int x, final int y, final int z) {
        if (FidorialServer.getInstance().worlds().manager().levelData().difficulty == LocalDifficulty.PEACEFUL) {
            return false;
        }
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final TypedKey<BiomeDefinition> biome = BlockView.biomeAt(world, x, y, z);
        if (biome != null && y > SURFACE_MIN_Y && y < SURFACE_MAX_Y
                && FidorialServer.getInstance().registries().registry(RegistryKey.BIOME).isTagged(biome, BiomeTagKeys.ALLOWS_SURFACE_SLIME_SPAWNS)
                && random.nextFloat() < surfaceSpawnChance(world)
                && Brightness.light(world, x, y, z) <= random.nextInt(SURFACE_MAX_LIGHT)) {
            return BlockView.isSolidGround(world, x, y - 1, z);
        }
        if (random.nextInt(SLIME_CHUNK_CHANCE) == 0 && isSlimeChunk(x >> 4, z >> 4) && y < SLIME_CHUNK_MAX_Y) {
            return BlockView.isSolidGround(world, x, y - 1, z);
        }
        return false;
    }

    /**
     * {@return {@code true} if a chunk is a slime chunk}: one chunk in ten, picked by the seed of the
     * worlds
     */
    @SuppressFBWarnings(
            value = "DMI_RANDOM_USED_ONLY_ONCE",
            justification = "The first number of a Random seeded by the chunk is what makes it a slime chunk."
    )
    public static boolean isSlimeChunk(final int chunkX, final int chunkZ) {
        final long seed = FidorialServer.getInstance().worlds().manager().levelData().seed;
        // the products of ints wrap around on purpose: the slime chunks depend on it
        final int xSquared = chunkX * chunkX * 4_987_142;
        final int xLinear = chunkX * 5_947_611;
        final int zSquare = chunkZ * chunkZ;
        final long zSquared = zSquare * 4_392_871L;
        final int zLinear = chunkZ * 389_711;
        final long chunkSeed = (seed + xSquared + xLinear + zSquared + zLinear) ^ SLIME_CHUNK_SALT;
        return new Random(chunkSeed).nextInt(SLIME_CHUNK_CHANCE) == 0;
    }

    /**
     * {@return how likely a slime is to spawn in a swamp of a world}: in a world where the moon
     * rises, from one in two at full moon to never at new moon; never elsewhere
     */
    private static float surfaceSpawnChance(final ServerWorld world) {
        return hasMoon(world) ? SURFACE_SPAWN_CHANCE[world.dayNightCycle().moonPhase()] : 0.0f;
    }

    private static boolean hasMoon(final ServerWorld world) {
        for (final TimelineReference timeline : world.dimensionType().timelines()) {
            switch (timeline) {
                case final TimelineReference.Id id -> {
                    if (id.key().equals(TimelineKeys.MOON)) {
                        return true;
                    }
                }
                case final TimelineReference.Tag tag -> {
                    if (FidorialServer.getInstance().registries().registry(RegistryKey.TIMELINE).isTagged(TimelineKeys.MOON, tag.key())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public float attackDamage() {
        return size();
    }

    @Override
    protected int experienceReward() {
        return size();
    }

    @Override
    protected double baseEyeHeight() {
        return BASE_EYE_HEIGHT;
    }

    @Override
    protected void dropLoot() {
        if (!isTiny()) {
            return;
        }
        // TODO: Looting adds up to one more per level; a slime a frog eats leaves exactly one
        final int count = ThreadLocalRandom.current().nextInt(MAX_SLIME_BALLS + 1);
        if (count > 0) {
            dropLootItem(ItemStack.of(ItemKeys.SLIME_BALL, count));
        }
    }

    @Override
    protected Sound.Type jumpSound() {
        return isTiny() ? SoundEvents.SLIME_JUMP_SMALL : SoundEvents.SLIME_JUMP;
    }

    @Override
    protected Sound.Type squishSound() {
        return isTiny() ? SoundEvents.SLIME_SQUISH_SMALL : SoundEvents.SLIME_SQUISH;
    }

    @Override
    protected Sound.Type hurtSound() {
        return isTiny() ? SoundEvents.SLIME_HURT_SMALL : SoundEvents.SLIME_HURT;
    }

    @Override
    protected Sound.Type deathSound() {
        return isTiny() ? SoundEvents.SLIME_DEATH_SMALL : SoundEvents.SLIME_DEATH;
    }
}
