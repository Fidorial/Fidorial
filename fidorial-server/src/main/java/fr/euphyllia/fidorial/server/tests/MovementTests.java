package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.AStarPathfinder;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMovingMob;
import fr.euphyllia.fidorial.server.entity.mob.MobFactories;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.BlockCollision;
import fr.euphyllia.fidorial.server.world.block.CollisionShape;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.entity.ai.Path;
import fr.fidorial.entity.mob.Mob;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockType;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * How mobs move among blocks that are not whole: grass they walk through, slabs they step onto,
 * fences they cannot climb. Built on the cobblestone floor of the test worlds, whose surface is the
 * air block at {@value #SURFACE}.
 */
@SuppressWarnings("unused")
public final class MovementTests {

    private static final int SURFACE = -48;
    private static final int FLOOR = SURFACE - 1;
    /**
     * On the scale of vanilla's movement speed: a brisk walk, about 4 blocks per second.
     */
    private static final double WALK_SPEED = 0.3;

    private static FidorialServer server() {
        return FidorialServer.getInstance();
    }

    private static int id(final TypedKey<BlockType> block) {
        return server().registries().blockStates().networkId(BlockState.withoutProperties(block));
    }

    private static int id(final TypedKey<BlockType> block, final Map<String, String> properties) {
        return server().registries().blockStates().networkId(BlockState.withProperties(block, properties));
    }

    private static int bottomSlab() {
        return id(BlockTypeKeys.OAK_SLAB, Map.of("type", "bottom", "waterlogged", "false"));
    }

    private static int fence() {
        return id(BlockTypeKeys.OAK_FENCE,
                Map.of("east", "false", "north", "false", "south", "false", "west", "false", "waterlogged", "false"));
    }

    private static void set(final World world, final int x, final int y, final int z, final int state) {
        world.setBlockStateId(Position.block(x, y, z), state);
    }

    private static AbstractMovingMob cow(final World world, final double x, final double y, final double z) {
        final AbstractMovingMob cow = (AbstractMovingMob) MobFactories.create(EntityTypes.COW, server().entities().ids().allocate(),
                Location.of(world, x, y, z, 0f, 0f));
        cow.goals().clear();
        server().entities().spawn(cow);
        return cow;
    }

    /**
     * Walks a mob to a place, and nowhere else.
     */
    private static void walkTo(final AbstractMovingMob mob, final Location destination) {
        mob.goals().clear();
        mob.goals().add(new WalkTo(mob, destination));
    }

    private static double horizontalDistance(final Location from, final Location to) {
        final double dx = from.x() - to.x();
        final double dz = from.z() - to.z();
        return Math.sqrt(dx * dx + dz * dz);
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void collisionShapesComeFromTheDataset(final ScenarioTestHelper helper) {
        helper.assertTrue(BlockCollision.shape(BlockState.withoutProperties(BlockTypeKeys.STONE)).isFullBlock(),
                "Stone should be a full block");
        helper.assertTrue(BlockCollision.shape(BlockState.withoutProperties(BlockTypeKeys.SHORT_GRASS)).isEmpty(),
                "Short grass should have nothing to bump into");
        helper.assertTrue(BlockCollision.shape(BlockState.withoutProperties(BlockTypeKeys.POPPY)).isEmpty(),
                "Flowers should have nothing to bump into");
        helper.assertTrue(BlockCollision.shape(BlockState.withoutProperties(BlockTypeKeys.WATER)).isEmpty(),
                "Water should have nothing to bump into");

        final CollisionShape slab = BlockCollision.shape(
                BlockState.withProperties(BlockTypeKeys.OAK_SLAB, Map.of("type", "bottom", "waterlogged", "false")));
        helper.assertTrue(slab.top() == 0.5, "A bottom slab should be half a block high, got " + slab);
        final CollisionShape topSlab = BlockCollision.shape(
                BlockState.withProperties(BlockTypeKeys.OAK_SLAB, Map.of("type", "top", "waterlogged", "true")));
        helper.assertTrue(topSlab.minY(0) == 0.5 && topSlab.top() == 1.0, "A top slab should fill the upper half, got " + topSlab);
        final CollisionShape fence = BlockCollision.shape(BlockState.withProperties(BlockTypeKeys.OAK_FENCE,
                Map.of("east", "false", "north", "false", "south", "false", "west", "false", "waterlogged", "false")));
        helper.assertTrue(fence.top() == 1.5, "A fence should be one and a half blocks high, got " + fence);
        final CollisionShape snow = BlockCollision.shape(BlockState.withProperties(BlockTypeKeys.SNOW, Map.of("layers", "1")));
        helper.assertTrue(snow.isEmpty(), "A single layer of snow should have nothing to bump into, got " + snow);
    }

    @ScenarioTest(timeoutTicks = 100)
    public static void aMobFallsThroughGrassOntoTheGround(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicReference<AbstractMovingMob> cow = new AtomicReference<>();
        helper.sequence()
                .execute(() -> {
                    for (int x = 2; x <= 6; x++) {
                        for (int z = 2; z <= 6; z++) {
                            set(world, x, FLOOR, z, id(BlockTypeKeys.GRASS_BLOCK));
                            set(world, x, SURFACE, z, id(BlockTypeKeys.SHORT_GRASS));
                        }
                    }
                })
                .waitUntil(() -> helper.assertBlockAt(Position.block(6, SURFACE, 6), BlockTypeKeys.SHORT_GRASS))
                .execute(() -> cow.set(cow(world, 4.5, SURFACE + 3, 4.5)))
                .waitUntil(() -> cow.get().onGround() && cow.get().location().y() < SURFACE + 1,
                        "The cow should land through the grass")
                .execute(() -> helper.assertTrue(Math.abs(cow.get().location().y() - SURFACE) < 1.0E-6,
                        "The cow should stand on the grass block, not on the grass: " + cow.get().location()))
                .build();
    }

    @ScenarioTest(timeoutTicks = 100)
    public static void aMobStandsOnABottomSlab(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicReference<AbstractMovingMob> cow = new AtomicReference<>();
        helper.sequence()
                .execute(() -> {
                    for (int x = 2; x <= 6; x++) {
                        for (int z = 2; z <= 6; z++) {
                            set(world, x, SURFACE, z, bottomSlab());
                        }
                    }
                })
                .waitUntil(() -> helper.assertBlockAt(Position.block(6, SURFACE, 6), BlockTypeKeys.OAK_SLAB))
                .execute(() -> cow.set(cow(world, 4.5, SURFACE + 3, 4.5)))
                .waitUntil(() -> cow.get().onGround(), "The cow should land on the slabs")
                .execute(() -> helper.assertTrue(Math.abs(cow.get().location().y() - (SURFACE + 0.5)) < 1.0E-6,
                        "The cow should stand at the top of the slabs: " + cow.get().location()))
                .build();
    }

    @ScenarioTest(timeoutTicks = 300)
    public static void aMobWalksThroughTallGrassAndFlowers(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicReference<AbstractMovingMob> cow = new AtomicReference<>();
        final Location destination = Location.of(world, 14.5, SURFACE, 4.5);
        helper.sequence()
                .execute(() -> {
                    for (int x = 0; x <= 16; x++) {
                        for (int z = 2; z <= 6; z++) {
                            set(world, x, FLOOR, z, id(BlockTypeKeys.GRASS_BLOCK));
                            set(world, x, SURFACE, z, id((x + z) % 3 == 0 ? BlockTypeKeys.POPPY : BlockTypeKeys.SHORT_GRASS));
                        }
                    }
                })
                .waitUntil(() -> helper.assertBlockAt(Position.block(16, FLOOR, 6), BlockTypeKeys.GRASS_BLOCK))
                .execute(() -> {
                    cow.set(cow(world, 2.5, SURFACE, 4.5));
                    walkTo(cow.get(), destination);
                })
                .waitUntil(() -> horizontalDistance(cow.get().location(), destination) < 1.0,
                        "The cow should walk across the field")
                .execute(() -> helper.assertTrue(Math.abs(cow.get().location().y() - SURFACE) < 0.01,
                        "The cow should walk on the ground, through the grass: " + cow.get().location()))
                .build();
    }

    @ScenarioTest(timeoutTicks = 300)
    public static void aMobWalksUpSlabsAndJumpsOntoABlock(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicReference<AbstractMovingMob> cow = new AtomicReference<>();
        // the floor, a slab, a block, a block and a slab, two blocks: half a block higher at each
        // step; then three blocks, a whole block higher, to jump onto
        helper.sequence()
                .execute(() -> {
                    for (int z = 2; z <= 6; z++) {
                        set(world, 6, SURFACE, z, bottomSlab());
                        set(world, 7, SURFACE, z, id(BlockTypeKeys.STONE));
                        set(world, 8, SURFACE, z, id(BlockTypeKeys.STONE));
                        set(world, 8, SURFACE + 1, z, bottomSlab());
                        for (int x = 9; x <= 13; x++) {
                            set(world, x, SURFACE, z, id(BlockTypeKeys.STONE));
                            set(world, x, SURFACE + 1, z, id(BlockTypeKeys.STONE));
                        }
                        set(world, 13, SURFACE + 2, z, id(BlockTypeKeys.STONE));
                    }
                })
                .waitUntil(() -> helper.assertBlockAt(Position.block(13, SURFACE + 2, 6), BlockTypeKeys.STONE))
                .execute(() -> {
                    cow.set(cow(world, 2.5, SURFACE, 4.5));
                    walkTo(cow.get(), Location.of(world, 13.5, SURFACE + 3, 4.5));
                })
                .waitUntil(() -> cow.get().location().x() > 13.0 && cow.get().location().y() > SURFACE + 2.9,
                        "The cow should walk up the slabs and jump onto the last block")
                .build();
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void pathsCrossGrassButNotFences(final ScenarioTestHelper helper) {
        final ServerWorld world = (ServerWorld) helper.world();
        helper.sequence()
                .execute(() -> {
                    for (int x = 0; x <= 12; x++) {
                        set(world, x, SURFACE, 2, id(BlockTypeKeys.SHORT_GRASS));
                    }
                    // a pen of fences around (20, 2)
                    for (int x = 18; x <= 22; x++) {
                        for (int z = 0; z <= 4; z++) {
                            if (x == 18 || x == 22 || z == 0 || z == 4) {
                                set(world, x, SURFACE, z, fence());
                            }
                        }
                    }
                })
                .waitUntil(() -> helper.assertBlockAt(Position.block(22, SURFACE, 4), BlockTypeKeys.OAK_FENCE))
                .execute(() -> {
                    final Path acrossGrass = AStarPathfinder.find(world,
                            Position.block(0, SURFACE, 2), Position.block(12, SURFACE, 2), 500);
                    helper.assertTrue(acrossGrass != null && acrossGrass.reachesGoal(),
                            "A path should go straight through the grass");
                    for (final BlockPosition waypoint : acrossGrass.waypoints()) {
                        helper.assertTrue(waypoint.y() == SURFACE && waypoint.z() == 2,
                                "The path should stay in the grass, at ground level: " + acrossGrass.waypoints());
                    }

                    final Path outOfThePen = AStarPathfinder.find(world,
                            Position.block(20, SURFACE, 2), Position.block(26, SURFACE, 2), 500);
                    helper.assertTrue(outOfThePen == null || !outOfThePen.reachesGoal(),
                            "No path should climb over a fence: " + (outOfThePen == null ? null : outOfThePen.waypoints()));
                })
                .build();
    }

    /**
     * Keeps a mob walking towards a place.
     */
    private static final class WalkTo implements Goal {

        private final Mob mob;
        private final Location destination;

        WalkTo(final Mob mob, final Location destination) {
            this.mob = mob;
            this.destination = destination;
        }

        @Override
        public int priority() {
            return 0;
        }

        @Override
        public boolean canStart() {
            return true;
        }

        @Override
        public boolean shouldContinue() {
            return true;
        }

        @Override
        public void tick() {
            mob.navigateTo(destination, WALK_SPEED);
        }
    }
}
