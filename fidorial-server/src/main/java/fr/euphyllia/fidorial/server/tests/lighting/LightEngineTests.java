package fr.euphyllia.fidorial.server.tests.lighting;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;

import java.util.Random;

import static fr.euphyllia.fidorial.server.tests.lighting.LightScene.AIR;
import static fr.euphyllia.fidorial.server.tests.lighting.LightScene.GLASS;
import static fr.euphyllia.fidorial.server.tests.lighting.LightScene.GLOWSTONE;
import static fr.euphyllia.fidorial.server.tests.lighting.LightScene.STONE;
import static fr.euphyllia.fidorial.server.tests.lighting.LightScene.TORCH;
import static fr.euphyllia.fidorial.server.tests.lighting.LightScene.WATER;

@SuppressWarnings("unused")
public final class LightEngineTests {

    private LightEngineTests() {
    }

    /// A stone floor four blocks thick, in a world of the given bounds.
    private static LightScene ground(final int minY, final int height, final boolean skylight, final int chunksX, final int chunksZ) {
        final LightScene scene = new LightScene(minY, height, skylight, chunksX, chunksZ);
        return scene.fill(0, minY, 0, scene.width() - 1, minY + 3, scene.depth() - 1, STONE);
    }

    private static void expect(final ScenarioTestHelper helper, final int actual, final int expected, final String what) {
        helper.assertTrue(actual == expected, what + " is " + actual + ", expected " + expected);
    }

    private static void matchesFullRelight(final ScenarioTestHelper helper, final LightScene scene, final String after) {
        final String difference = scene.differenceFromFullRelight();
        helper.assertTrue(difference == null, "After " + after + ", " + difference);
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void torchLightFadesByOnePerBlock(final ScenarioTestHelper helper) {
        final LightScene scene = ground(-32, 64, true, 2, 1);
        final int y = -28;
        scene.set(8, y, 8, TORCH).light();

        expect(helper, scene.block(8, y, 8), 14, "Block light at the torch");
        expect(helper, scene.block(9, y, 8), 13, "Block light next to the torch");
        expect(helper, scene.block(8, y + 5, 8), 9, "Block light 5 blocks above the torch");
        expect(helper, scene.block(11, y, 12), 7, "Block light 7 blocks away around corners");
        expect(helper, scene.block(21, y, 8), 1, "Block light 13 blocks away");
        expect(helper, scene.block(22, y, 8), 0, "Block light 14 blocks away");
        expect(helper, scene.block(8, y - 1, 8), 0, "Block light inside the floor");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void breakingATorchTakesItsLightAway(final ScenarioTestHelper helper) {
        final LightScene scene = ground(-32, 64, true, 2, 2).light();
        final int y = -28;

        scene.edit(15, y, 15, TORCH);
        expect(helper, scene.block(16, y, 16), 12, "Block light across the corner of four chunks");
        matchesFullRelight(helper, scene, "placing a torch");

        scene.edit(15, y, 15, AIR);
        expect(helper, scene.block(15, y, 15), 0, "Block light where the torch was");
        expect(helper, scene.block(16, y, 16), 0, "Block light across the corner of four chunks");
        matchesFullRelight(helper, scene, "breaking the torch");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void breakingOneOfTwoTorchesKeepsTheOther(final ScenarioTestHelper helper) {
        final int y = -28;
        final LightScene scene = ground(-32, 64, true, 1, 1)
                .set(4, y, 8, TORCH)
                .set(12, y, 8, TORCH)
                .light();

        scene.edit(4, y, 8, AIR);
        expect(helper, scene.block(12, y, 8), 14, "Block light at the torch left alone");
        expect(helper, scene.block(4, y, 8), 6, "Block light where the other torch was");
        matchesFullRelight(helper, scene, "breaking one of two torches");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void torchNextToBrokenGlowstoneStaysLit(final ScenarioTestHelper helper) {
        final int y = -28;
        // the torch sits in light brighter than its own, which goes with the glowstone
        final LightScene scene = ground(-32, 64, true, 1, 1)
                .set(8, y, 8, GLOWSTONE)
                .set(9, y, 8, TORCH)
                .light();

        scene.edit(8, y, 8, AIR);
        expect(helper, scene.block(9, y, 8), 14, "Block light at the torch");
        expect(helper, scene.block(8, y, 8), 13, "Block light where the glowstone was");
        matchesFullRelight(helper, scene, "breaking glowstone next to a torch");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void swappingALightForAnother(final ScenarioTestHelper helper) {
        final int y = -28;
        final LightScene scene = ground(-32, 64, true, 1, 1).set(8, y, 8, TORCH).light();

        scene.edit(8, y, 8, GLOWSTONE);
        expect(helper, scene.block(9, y, 8), 14, "Block light next to glowstone that replaced a torch");
        matchesFullRelight(helper, scene, "replacing a torch with glowstone");

        scene.edit(8, y, 8, TORCH);
        expect(helper, scene.block(9, y, 8), 13, "Block light next to a torch that replaced glowstone");
        matchesFullRelight(helper, scene, "replacing glowstone with a torch");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void sealedRoomKeepsItsLightIn(final ScenarioTestHelper helper) {
        final int y = -28;
        final LightScene scene = ground(-32, 64, true, 1, 1)
                .fill(4, y, 4, 10, y + 4, 10, STONE)
                .fill(5, y, 5, 9, y + 3, 9, AIR)
                .set(7, y, 7, TORCH)
                .light();

        expect(helper, scene.block(3, y, 7), 0, "Block light outside the walls");
        expect(helper, scene.sky(7, y, 7), 0, "Sky light inside the room");

        scene.edit(4, y, 7, AIR);
        expect(helper, scene.block(3, y, 7), 10, "Block light through the opened wall");
        expect(helper, scene.sky(5, y, 7), 13, "Sky light coming in through the opened wall");
        matchesFullRelight(helper, scene, "opening the wall");

        scene.edit(4, y, 7, STONE);
        expect(helper, scene.block(3, y, 7), 0, "Block light outside the closed wall");
        expect(helper, scene.sky(5, y, 7), 0, "Sky light inside the closed room");
        matchesFullRelight(helper, scene, "closing the wall again");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void skyLightDimsSidewaysUnderARoof(final ScenarioTestHelper helper) {
        final int y = -28;
        final LightScene scene = ground(-32, 64, true, 1, 1).light();
        expect(helper, scene.sky(8, y, 8), 15, "Sky light on open ground");

        for (int x = 4; x <= 12; x++) {
            for (int z = 4; z <= 12; z++) {
                scene.edit(x, y + 5, z, STONE);
            }
        }
        // only straight down from the open sky does light stay at full strength
        expect(helper, scene.sky(4, y, 8), 14, "Sky light under the edge of the roof");
        expect(helper, scene.sky(6, y + 4, 8), 12, "Sky light right under the roof, 3 blocks in");
        expect(helper, scene.sky(8, y, 8), 10, "Sky light on the ground under the middle of the roof");
        expect(helper, scene.sky(8, y + 6, 8), 15, "Sky light on top of the roof");
        matchesFullRelight(helper, scene, "building a roof");

        for (int x = 4; x <= 12; x++) {
            for (int z = 4; z <= 12; z++) {
                scene.edit(x, y + 5, z, AIR);
            }
        }
        expect(helper, scene.sky(8, y, 8), 15, "Sky light where the roof was");
        matchesFullRelight(helper, scene, "taking the roof down");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void tunnelIsLitFromItsMouth(final ScenarioTestHelper helper) {
        final int y = -28;
        // a hill covering the first chunk, with a tunnel running into it from the second
        final LightScene scene = ground(-32, 64, true, 2, 1)
                .fill(0, y, 0, 15, y + 8, 15, STONE)
                .fill(4, y, 8, 15, y + 1, 8, AIR)
                .light();

        expect(helper, scene.sky(15, y, 8), 14, "Sky light just inside the tunnel");
        expect(helper, scene.sky(10, y, 8), 9, "Sky light 6 blocks into the tunnel");
        expect(helper, scene.sky(4, y + 1, 8), 3, "Sky light at the end of the tunnel");
        matchesFullRelight(helper, scene, "lighting both chunks together");

        scene.edit(16, y, 8, STONE);
        expect(helper, scene.sky(15, y, 8), 13, "Sky light just inside the tunnel, half blocked");
        matchesFullRelight(helper, scene, "blocking half of the tunnel mouth");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void skyLightLosesOnePerBlockOfWater(final ScenarioTestHelper helper) {
        final int y = -28;
        final LightScene scene = ground(-32, 64, true, 1, 1)
                .fill(0, y, 0, 15, y + 4, 15, WATER)
                .light();

        expect(helper, scene.sky(8, y + 5, 8), 15, "Sky light over the water");
        expect(helper, scene.sky(8, y + 4, 8), 14, "Sky light in the top layer of water");
        expect(helper, scene.sky(8, y, 8), 10, "Sky light at the bottom of 5 blocks of water");

        scene.edit(8, y + 4, 8, GLASS);
        expect(helper, scene.sky(8, y + 3, 8), 14, "Sky light in the water under glass");
        matchesFullRelight(helper, scene, "putting glass on the water");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void noSkyLightWhereTheDimensionHasNone(final ScenarioTestHelper helper) {
        final int y = -28;
        final LightScene scene = ground(-32, 64, false, 1, 1).set(8, y, 8, TORCH).light();

        expect(helper, scene.sky(8, 31, 8), 0, "Sky light at the top of the world");
        expect(helper, scene.sky(2, y, 2), 0, "Sky light on the ground");
        expect(helper, scene.block(9, y, 8), 13, "Block light next to the torch");

        scene.edit(8, y + 6, 8, STONE).edit(8, y + 6, 8, AIR);
        expect(helper, scene.sky(8, y + 5, 8), 0, "Sky light under where a block was");
        matchesFullRelight(helper, scene, "placing and breaking a block");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void oneSectionTallWorldHasNoWrapAround(final ScenarioTestHelper helper) {
        final LightScene scene = new LightScene(0, 16, true, 1, 1)
                .set(2, 0, 2, TORCH)
                .set(13, 15, 13, TORCH)
                .light();

        // too far for the torches to reach, so only light wrapping past the bottom or top of the world could get there
        expect(helper, scene.block(2, 15, 2), 0, "Block light on the top layer, over a torch on the bottom layer");
        expect(helper, scene.block(13, 0, 13), 0, "Block light on the bottom layer, under a torch on the top layer");

        // a block on the top layer has nothing above it to take light from
        scene.edit(4, 15, 4, STONE);
        expect(helper, scene.sky(4, 14, 4), 14, "Sky light under a block on the top layer");
        matchesFullRelight(helper, scene, "placing a block on the top layer");

        scene.edit(4, 15, 4, AIR);
        matchesFullRelight(helper, scene, "breaking the block on the top layer");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void lightCrossesChunksLitOneAtATime(final ScenarioTestHelper helper) {
        final int y = -44;
        final LightScene scene = ground(-48, 80, true, 2, 1)
                .fill(0, y, 0, 31, y + 3, 15, STONE)
                .fill(1, y, 8, 30, y, 8, AIR)
                .set(14, y, 8, TORCH);

        // the chunk without the torch first, as when it loads before its neighbor
        scene.light(1, 0).light(0, 0).checkSeams();
        expect(helper, scene.block(16, y, 8), 12, "Block light across the seam");
        expect(helper, scene.block(26, y, 8), 2, "Block light 12 blocks away, in the next chunk");
        matchesFullRelight(helper, scene, "lighting the chunks one at a time and checking the seam");
    }

    @ScenarioTest(timeoutTicks = 100)
    public static void randomEditsMatchAFullRelight(final ScenarioTestHelper helper) {
        randomEdits(helper, -64, 96, true, 1);
    }

    @ScenarioTest(timeoutTicks = 100)
    public static void randomEditsMatchAFullRelightWithoutSkyLight(final ScenarioTestHelper helper) {
        randomEdits(helper, -64, 96, false, 2);
    }

    @ScenarioTest(timeoutTicks = 100)
    public static void randomEditsMatchAFullRelightInAShallowWorld(final ScenarioTestHelper helper) {
        randomEdits(helper, 32, 32, true, 3);
    }

    private static void randomEdits(final ScenarioTestHelper helper, final int minY, final int height, final boolean skylight, final long seed) {
        final BlockState[] palette = {AIR, AIR, AIR, STONE, STONE, STONE, GLASS, WATER, TORCH, GLOWSTONE};
        final Random random = new Random(seed);
        final LightScene scene = ground(minY, height, skylight, 2, 2);
        final int floor = minY + 4;
        for (int i = 0; i < 200; i++) {
            scene.set(random.nextInt(32), floor + random.nextInt(12), random.nextInt(32), palette[random.nextInt(palette.length)]);
        }
        scene.light();

        for (int i = 0; i < 150; i++) {
            final int x = random.nextInt(32);
            final int y = floor + random.nextInt(Math.min(16, height - 4));
            final int z = random.nextInt(32);
            final BlockState state = palette[random.nextInt(palette.length)];
            scene.edit(x, y, z, state);
            final String difference = scene.differenceFromFullRelight();
            if (difference != null) {
                helper.fail("After setting " + state.name().key().value() + " at " + x + "," + y + "," + z
                        + " (edit " + (i + 1) + "), " + difference);
                return;
            }
        }
    }
}
