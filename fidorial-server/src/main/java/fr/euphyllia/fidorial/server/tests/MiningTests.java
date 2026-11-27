package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundPlayerActionPacket;
import fr.fidorial.entity.GameMode;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Position;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.World;

import java.util.concurrent.atomic.AtomicInteger;

import static fr.euphyllia.fidorial.server.tests.BlockDropTests.SETTLE_TICKS;
import static fr.euphyllia.fidorial.server.tests.BlockDropTests.TARGET;
import static fr.euphyllia.fidorial.server.tests.BlockDropTests.Y;
import static fr.euphyllia.fidorial.server.tests.BlockDropTests.dropped;
import static fr.euphyllia.fidorial.server.tests.BlockDropTests.player;
import static fr.euphyllia.fidorial.server.tests.BlockDropTests.set;

/**
 * What the server checks of the mining a client says it did: a block only breaks within the reach
 * of the player, once they started mining that very block, and once they have mined it long enough
 * with what they hold; a block no player in survival mode breaks never does.
 *
 * <p>The blocks are mined through the packets a client sends, where {@link BlockDropTests} breaks
 * its own, by its players.</p>
 */
@SuppressWarnings("unused")
public final class MiningTests {

    /**
     * The network id of the top face, the face the client says it mines.
     */
    private static final int FACE_UP = 1;

    /**
     * In the chunk the test owns, but out of the reach of the players of {@link BlockDropTests}.
     */
    private static final BlockPosition OUT_OF_REACH = Position.block(8, Y, 14);

    /**
     * How many ticks a client may say it mined stone with a wooden pickaxe while the block is still
     * there: the player needs more than a second.
     */
    private static final int TOO_SOON_TICKS = 5;

    private MiningTests() {
    }

    private static void send(final ServerPlayer player, final int action, final BlockPosition position) {
        player.connection().mockReceive(new ServerboundPlayerActionPacket(action, position, FACE_UP, 0));
    }

    private static void start(final ServerPlayer player, final BlockPosition position) {
        send(player, ServerboundPlayerActionPacket.START_DESTROY_BLOCK, position);
    }

    private static void finish(final ServerPlayer player, final BlockPosition position) {
        send(player, ServerboundPlayerActionPacket.FINISH_DESTROY_BLOCK, position);
    }

    @ScenarioTest
    public static void blockOutOfReachIsNotMined(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicInteger settled = new AtomicInteger();
        helper.sequence()
                .execute(() -> {
                    set(world, OUT_OF_REACH, "minecraft:stone");
                    final ServerPlayer player = player(helper, "mining_far", GameMode.SURVIVAL, ItemStack.of(ItemKeys.DIAMOND_PICKAXE));
                    start(player, OUT_OF_REACH);
                    finish(player, OUT_OF_REACH);
                })
                .waitUntil(() -> settled.incrementAndGet() >= SETTLE_TICKS, "Leave the time for the block to break")
                .execute(() -> {
                    helper.assertBlockAt(OUT_OF_REACH, BlockTypeKeys.STONE);
                    helper.assertTrue(dropped(world) == 0, "A block out of reach should drop nothing, dropped " + dropped(world));
                })
                .build();
    }

    @ScenarioTest
    public static void blockNeverStartedIsNotMined(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicInteger settled = new AtomicInteger();
        helper.sequence()
                .execute(() -> {
                    set(world, TARGET, "minecraft:stone");
                    finish(player(helper, "mining_unstarted", GameMode.SURVIVAL, ItemStack.of(ItemKeys.DIAMOND_PICKAXE)), TARGET);
                })
                .waitUntil(() -> settled.incrementAndGet() >= SETTLE_TICKS, "Leave the time for the block to break")
                .execute(() -> helper.assertBlockAt(TARGET, BlockTypeKeys.STONE))
                .build();
    }

    @ScenarioTest
    public static void abortedMiningIsNotFinished(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicInteger settled = new AtomicInteger();
        helper.sequence()
                .execute(() -> {
                    set(world, TARGET, "minecraft:stone");
                    // a wooden pickaxe takes 23 ticks to get through stone
                    final ServerPlayer player = player(helper, "mining_quitter", GameMode.SURVIVAL, ItemStack.of(ItemKeys.WOODEN_PICKAXE));
                    start(player, TARGET);
                    send(player, ServerboundPlayerActionPacket.ABORT_DESTROY_BLOCK, TARGET);
                    finish(player, TARGET);
                })
                .waitUntil(() -> settled.incrementAndGet() >= SETTLE_TICKS, "Leave the time for the block to break")
                .execute(() -> helper.assertBlockAt(TARGET, BlockTypeKeys.STONE))
                .build();
    }

    @ScenarioTest
    public static void blockMinedTooSoonBreaksOnceThePlayerWouldHaveMinedIt(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicInteger waited = new AtomicInteger();
        helper.sequence()
                .execute(() -> {
                    set(world, TARGET, "minecraft:stone");
                    final ServerPlayer player = player(helper, "mining_hasty", GameMode.SURVIVAL, ItemStack.of(ItemKeys.WOODEN_PICKAXE));
                    // a wooden pickaxe takes 23 ticks to get through stone
                    start(player, TARGET);
                    finish(player, TARGET);
                })
                .waitUntil(() -> waited.incrementAndGet() >= TOO_SOON_TICKS, "Let a few ticks go by")
                .execute(() -> helper.assertBlockAt(TARGET, BlockTypeKeys.STONE))
                .waitUntil(() -> helper.assertBlockAt(TARGET, BlockTypeKeys.AIR))
                .waitUntil(() -> dropped(world, ItemKeys.COBBLESTONE) == 1, "The stone should drop its cobblestone once mined")
                .build();
    }

    @ScenarioTest
    public static void blockMinedInOneTickBreaksAsSoonAsStarted(final ScenarioTestHelper helper) {
        helper.sequence()
                .execute(() -> {
                    // a diamond hoe gets through leaves in a single tick: the client only says it starts
                    set(helper.world(), TARGET, "minecraft:oak_leaves[persistent=true]");
                    start(player(helper, "mining_swift", GameMode.SURVIVAL, ItemStack.of(ItemKeys.DIAMOND_HOE)), TARGET);
                })
                .waitUntil(() -> helper.assertBlockAt(TARGET, BlockTypeKeys.AIR))
                .build();
    }

    @ScenarioTest
    public static void bedrockStaysInSurvivalMode(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicInteger settled = new AtomicInteger();
        helper.sequence()
                .execute(() -> {
                    set(world, TARGET, "minecraft:bedrock");
                    final ServerPlayer player = player(helper, "mining_bedrock", GameMode.SURVIVAL, ItemStack.of(ItemKeys.NETHERITE_PICKAXE));
                    start(player, TARGET);
                    finish(player, TARGET);
                })
                .waitUntil(() -> settled.incrementAndGet() >= SETTLE_TICKS, "Leave the time for the block to break")
                .execute(() -> helper.assertBlockAt(TARGET, BlockTypeKeys.BEDROCK))
                .build();
    }
}
