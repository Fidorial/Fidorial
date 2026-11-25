package fr.euphyllia.fidorial.server.tests.lighting;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.math.Position;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.World;
import fr.fidorial.world.WorldSpec;
import fr.fidorial.world.dimension.DimensionTypeDefinition;
import fr.fidorial.world.dimension.types.VanillaDimensionTypes;
import fr.fidorial.world.generation.GeneratedChunk;
import fr.fidorial.world.generation.WorldGenerator;
import net.kyori.adventure.key.Key;

import java.util.function.Consumer;

import static fr.euphyllia.fidorial.server.tests.lighting.LightScene.AIR;
import static fr.euphyllia.fidorial.server.tests.lighting.LightScene.STONE;
import static fr.euphyllia.fidorial.server.tests.lighting.LightScene.TORCH;

/// Light in real worlds: blocks changed the way players change them, and dimensions of their own.
@SuppressWarnings("unused")
public final class LightWorldTests {

    /// The first layer of air above the floor of the test worlds.
    private static final int Y = -48;

    private static final DimensionTypeDefinition NO_SKY = DimensionTypeDefinition.builder(VanillaDimensionTypes.OVERWORLD)
            .key(TypedKey.withRegistry(RegistryKey.DIMENSION_TYPE, "scenario_test:no_sky"))
            .hasSkylight(false)
            .build();

    private static final DimensionTypeDefinition SHORT = DimensionTypeDefinition.builder(VanillaDimensionTypes.OVERWORLD)
            .key(TypedKey.withRegistry(RegistryKey.DIMENSION_TYPE, "scenario_test:short"))
            .bounds(-32, 64)
            .logicalHeight(64)
            .build();

    private LightWorldTests() {
    }

    private static void set(final World world, final int x, final int y, final int z, final BlockState state) {
        FidorialServer.getInstance().worlds().blockEdits().set((ServerWorld) world, Position.block(x, y, z), state, true);
    }

    private static boolean lit(final World world) {
        final ChunkColumn column = ((ServerWorld) world).loadedColumn(0, 0);
        return column != null && column.lightPopulated();
    }

    /// A world of the given dimension type with a floor four blocks thick, and the scene in chunk 0,0.
    private static World world(final String name, final DimensionTypeDefinition type, final Consumer<GeneratedChunk> scene) {
        final FidorialServer server = FidorialServer.getInstance();
        final Key key = Key.key("scenario_test", "lighting/" + name);
        final World existing = server.worlds().manager().world(key);
        if (existing != null) {
            return existing;
        }
        final ServerRegistry<DimensionTypeDefinition> types = server.registries().registry(RegistryKey.DIMENSION_TYPE);
        if (!types.contains(type.key())) {
            types.registerCode(type.key(), type);
        }
        final WorldGenerator generator = new WorldGenerator() {
            @Override
            public void generate(final GeneratedChunk chunk) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = chunk.minY(); y < chunk.minY() + 4; y++) {
                            chunk.setBlock(x, y, z, BlockTypeKeys.STONE);
                        }
                    }
                }
                if (chunk.chunkX() == 0 && chunk.chunkZ() == 0) {
                    scene.accept(chunk);
                }
            }

            @Override
            public DimensionTypeDefinition dimensionType() {
                return type;
            }

            @Override
            public boolean generatesStructures() {
                return false;
            }
        };
        final World world = server.worlds().createWorldSilent(WorldSpec.builder(key).generator(generator).build());
        world.setChunkForceLoaded(0, 0, true);
        return world;
    }

    @ScenarioTest
    public static void placingAndBreakingATorch(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final ServerWorld server = (ServerWorld) world;
        world.chunkAsync(0, 0);
        helper.sequence()
                .waitUntil(() -> lit(world), "The chunk of the test should get lit")
                .execute(() -> set(world, 8, Y, 8, TORCH))
                .waitUntil(() -> server.blockLightAt(8, Y, 11) == 11, "Block light 3 blocks from a placed torch should be 11")
                .execute(() -> set(world, 8, Y, 8, AIR))
                .waitUntil(() -> server.blockLightAt(8, Y, 11) == 0, "Block light should go once the torch is broken")
                .execute(() -> helper.assertTrue(server.blockLightAt(8, Y, 8) == 0, "Block light should go where the torch was"))
                .build();
    }

    @ScenarioTest
    public static void placingAndBreakingABlockOverTheGround(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final ServerWorld server = (ServerWorld) world;
        world.chunkAsync(0, 0);
        helper.sequence()
                .waitUntil(() -> lit(world), "The chunk of the test should get lit")
                .execute(() -> set(world, 4, Y + 2, 4, STONE))
                .waitUntil(() -> server.skyLightAt(4, Y + 1, 4) == 14, "Sky light under a floating block should be 14")
                .execute(() -> helper.assertTrue(server.skyLightAt(4, Y, 4) == 14, "Sky light on the ground under it should be 14"))
                .execute(() -> set(world, 4, Y + 2, 4, AIR))
                .waitUntil(() -> server.skyLightAt(4, Y + 1, 4) == 15, "Sky light should be 15 again once the block is broken")
                .build();
    }

    @ScenarioTest(timeoutTicks = 400)
    public static void dimensionWithoutSkyLight(final ScenarioTestHelper helper) {
        final World world = world("no_sky", NO_SKY, chunk -> chunk.setBlock(8, chunk.minY() + 4, 8, BlockTypeKeys.GLOWSTONE));
        final ServerWorld server = (ServerWorld) world;
        final int floor = NO_SKY.minY() + 4;
        helper.sequence()
                .waitUntil(() -> lit(world), "The chunk should get lit")
                .execute(() -> {
                    helper.assertTrue(server.skyLightAt(2, floor, 2) == 0, "Sky light on open ground should be 0");
                    helper.assertTrue(server.skyLightAt(8, NO_SKY.minY() + NO_SKY.height() - 1, 8) == 0, "Sky light at the top of the world should be 0");
                    helper.assertTrue(server.blockLightAt(8, floor + 1, 8) == 14, "Block light over the glowstone should be 14");
                    helper.assertTrue(server.lightLevelAt(8, floor + 1, 8) == 14, "Light over the glowstone should come from it only");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 400)
    public static void shortDimensionWithASealedRoom(final ScenarioTestHelper helper) {
        final int floor = SHORT.minY() + 4;
        final World world = world("short", SHORT, chunk -> {
            for (int x = 4; x <= 10; x++) {
                for (int y = floor; y <= floor + 4; y++) {
                    for (int z = 4; z <= 10; z++) {
                        final boolean inside = x > 4 && x < 10 && z > 4 && z < 10 && y < floor + 4;
                        if (!inside) {
                            chunk.setBlock(x, y, z, BlockTypeKeys.STONE);
                        }
                    }
                }
            }
            chunk.setBlock(7, floor, 7, BlockTypeKeys.TORCH);
        });
        final ServerWorld server = (ServerWorld) world;
        final int top = SHORT.minY() + SHORT.height() - 1;
        helper.sequence()
                .waitUntil(() -> lit(world), "The chunk should get lit")
                .execute(() -> {
                    helper.assertTrue(server.skyLightAt(8, top, 8) == 15, "Sky light at the top of the world should be 15");
                    helper.assertTrue(server.skyLightAt(2, floor, 2) == 15, "Sky light on open ground should be 15");
                    helper.assertTrue(server.skyLightAt(7, floor + 1, 7) == 0, "Sky light inside the sealed room should be 0");
                    helper.assertTrue(server.blockLightAt(7, floor + 1, 7) == 13, "Block light over the torch should be 13");
                    helper.assertTrue(server.blockLightAt(3, floor, 7) == 0, "Block light outside the room should be 0");
                })
                .build();
    }
}
