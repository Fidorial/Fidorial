package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundPlayerActionPacket;
import fr.euphyllia.fidorial.server.world.BlockStateRegistry;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.BlockMining;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.ItemEntity;
import fr.fidorial.event.Subscription;
import fr.fidorial.event.block.BlockBreakEvent;
import fr.fidorial.event.farming.HarvestEvent;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.component.ItemEnchantments;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.EnchantmentKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.Blocks;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * What a block broken by a player drops: its loot table rolled with the tool in hand, and nothing
 * when the block needs a tool the player does not hold, in creative mode, or with the
 * {@code block_drops} game rule off; and what the listeners of the {@link BlockBreakEvent} make of
 * it.
 *
 * <p>The blocks are broken through the packets a client sends, so that the whole path a player
 * takes is tested. They stand on the floor of the test world, in the middle of the chunk at its
 * origin, so that the thread running the test owns them; the player stands within reach of them,
 * but far enough away not to pick up what drops, and mines fast, so that a bare hand does not take
 * seconds to get through stone: how long mining takes is what {@link MiningTests} looks at.</p>
 */
@SuppressWarnings("unused")
public final class BlockDropTests {

    /**
     * The first layer of air above the floor of the test worlds.
     */
    static final int Y = -48;

    static final BlockPosition TARGET = Position.block(8, Y, 8);
    private static final BlockPosition OTHER_TARGET = Position.block(12, Y, 8);

    /**
     * The network id of the top face, the face the client says it mines: breaking ignores it.
     */
    private static final int FACE_UP = 1;

    /**
     * How many ticks to wait after a break before checking that nothing dropped: an item dropped
     * would have shown up long before.
     */
    static final int SETTLE_TICKS = 10;

    /**
     * The block break speed of the players of these tests, as a plugin may raise it: a bare hand
     * gets through stone in half a second, a pickaxe at once.
     */
    private static final double FAST_BREAK_SPEED = 16.0;

    private BlockDropTests() {
    }

    static FidorialServer server() {
        return FidorialServer.getInstance();
    }

    static BlockData state(final String state) {
        return Objects.requireNonNull(Blocks.data(state), () -> "unknown block " + state);
    }

    static void set(final World world, final BlockPosition position, final String state) {
        final BlockStateRegistry states = server().registries().blockStates();
        server().worlds().blockEdits().set((ServerWorld) world, position, states.toBlockState(state(state)), true);
    }

    static ServerPlayer player(final ScenarioTestHelper helper, final String name, final GameMode gameMode,
                               final ItemStack held) {
        helper.world().blockStateId(TARGET);
        final ServerPlayer player = (ServerPlayer) helper.summonPlayer(name,
                Location.of(helper.world(), 8.5, Y, 4.5, 0f, 0f), gameMode);
        player.inventory().set(player.selectedSlot(), held);
        return player;
    }

    /**
     * Breaks a block the way a client does: at once in creative mode; in survival mode, by starting
     * then finishing to mine it, the block breaking once the player has mined it.
     */
    private static void breakBlock(final ServerPlayer player, final BlockPosition position) {
        player.connection().mockReceive(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.START_DESTROY_BLOCK, position, FACE_UP, 0));
        if (player.gameMode() != GameMode.CREATIVE) {
            player.connection().mockReceive(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.FINISH_DESTROY_BLOCK, position, FACE_UP, 0));
        }
    }

    static int dropped(final World world, final TypedKey<Item> item) {
        int count = 0;
        for (final Entity entity : world.entities()) {
            if (entity instanceof final ItemEntity drop && !drop.isRemoved() && drop.item().id().equals(item)) {
                count += drop.item().count();
            }
        }
        return count;
    }

    static int dropped(final World world) {
        int count = 0;
        for (final Entity entity : world.entities()) {
            if (entity instanceof final ItemEntity drop && !drop.isRemoved()) {
                count += drop.item().count();
            }
        }
        return count;
    }

    private static ItemStack silkTouch(final TypedKey<Item> tool) {
        return ItemStack.of(tool).with(DataComponentTypes.ENCHANTMENTS,
                ItemEnchantments.EMPTY.with(EnchantmentKeys.SILK_TOUCH, 1));
    }

    /**
     * Listens to the block breaks of one world only: every test has its own, and the bus is shared.
     */
    private static Subscription listenToBreaks(final World world, final Consumer<BlockBreakEvent> listener) {
        return server().events().subscribe(BlockBreakEvent.class, event -> {
            if (event.world() == world) {
                listener.accept(event);
            }
        });
    }

    @ScenarioTest
    public static void toolRulesFollowTheDataset(final ScenarioTestHelper helper) {
        helper.sequence()
                .execute(() -> {
                    final var blocks = server().registries().registry(RegistryKey.BLOCK);
                    helper.assertTrue(BlockMining.requiresTool(BlockTypeKeys.STONE), "Stone needs a tool to drop anything");
                    helper.assertFalse(BlockMining.requiresTool(BlockTypeKeys.OAK_LOG), "A log drops whatever breaks it");
                    helper.assertTrue(BlockMining.canHarvest(BlockTypeKeys.OAK_LOG, ItemStack.EMPTY, blocks::isTagged),
                            "A log drops when broken by hand");
                    helper.assertFalse(BlockMining.canHarvest(BlockTypeKeys.STONE, ItemStack.EMPTY, blocks::isTagged),
                            "Stone gives nothing to a bare hand");
                    helper.assertFalse(BlockMining.canHarvest(BlockTypeKeys.STONE, ItemStack.of(ItemKeys.DIAMOND_SHOVEL), blocks::isTagged),
                            "Stone gives nothing to a shovel");
                    helper.assertTrue(BlockMining.canHarvest(BlockTypeKeys.STONE, ItemStack.of(ItemKeys.WOODEN_PICKAXE), blocks::isTagged),
                            "Any pickaxe gets stone");
                    helper.assertFalse(BlockMining.canHarvest(BlockTypeKeys.IRON_ORE, ItemStack.of(ItemKeys.WOODEN_PICKAXE), blocks::isTagged),
                            "Iron ore needs at least a stone pickaxe");
                    helper.assertTrue(BlockMining.canHarvest(BlockTypeKeys.IRON_ORE, ItemStack.of(ItemKeys.STONE_PICKAXE), blocks::isTagged),
                            "A stone pickaxe gets iron ore");
                    helper.assertFalse(BlockMining.canHarvest(BlockTypeKeys.DIAMOND_ORE, ItemStack.of(ItemKeys.STONE_PICKAXE), blocks::isTagged),
                            "Diamond ore needs at least an iron pickaxe");
                    helper.assertFalse(BlockMining.canHarvest(BlockTypeKeys.OBSIDIAN, ItemStack.of(ItemKeys.IRON_PICKAXE), blocks::isTagged),
                            "Obsidian needs a diamond pickaxe");
                    helper.assertTrue(BlockMining.canHarvest(BlockTypeKeys.OBSIDIAN, ItemStack.of(ItemKeys.DIAMOND_PICKAXE), blocks::isTagged),
                            "A diamond pickaxe gets obsidian");
                    helper.assertTrue(BlockMining.canHarvest(BlockTypeKeys.COBWEB, ItemStack.of(ItemKeys.SHEARS), blocks::isTagged),
                            "Shears get a cobweb");
                    helper.assertTrue(BlockMining.canHarvest(BlockTypeKeys.COBWEB, ItemStack.of(ItemKeys.WOODEN_SWORD), blocks::isTagged),
                            "A sword gets the string of a cobweb");
                    helper.assertFalse(BlockMining.canHarvest(BlockTypeKeys.COBWEB, ItemStack.of(ItemKeys.STICK), blocks::isTagged),
                            "A stick is no tool");
                })
                .build();
    }

    @ScenarioTest
    public static void pickaxeMinesCobblestoneOutOfStone(final ScenarioTestHelper helper) {
        final World world = helper.world();
        helper.sequence()
                .execute(() -> {
                    set(world, TARGET, "minecraft:stone");
                    breakBlock(player(helper, "drops_miner", GameMode.SURVIVAL, ItemStack.of(ItemKeys.WOODEN_PICKAXE)), TARGET);
                })
                .waitUntil(() -> dropped(world, ItemKeys.COBBLESTONE) == 1, "Stone mined with a pickaxe should drop one cobblestone")
                .execute(() -> {
                    helper.assertBlockAt(TARGET, BlockTypeKeys.AIR);
                    helper.assertTrue(dropped(world) == 1, "Only the cobblestone should drop, dropped " + dropped(world) + " items");
                })
                .build();
    }

    @ScenarioTest
    public static void handBreaksStoneWithoutDrops(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicInteger settled = new AtomicInteger();
        helper.sequence()
                .execute(() -> {
                    set(world, TARGET, "minecraft:stone");
                    breakBlock(player(helper, "drops_puncher", GameMode.SURVIVAL, ItemStack.EMPTY), TARGET);
                })
                .waitUntil(() -> helper.assertBlockAt(TARGET, BlockTypeKeys.AIR))
                .waitUntil(() -> settled.incrementAndGet() >= SETTLE_TICKS, "Leave the time for a drop to show up")
                .execute(() -> helper.assertTrue(dropped(world) == 0,
                        "Stone broken by hand should drop nothing, dropped " + dropped(world) + " items"))
                .build();
    }

    @ScenarioTest
    public static void silkTouchKeepsTheBlock(final ScenarioTestHelper helper) {
        final World world = helper.world();
        helper.sequence()
                .execute(() -> {
                    set(world, TARGET, "minecraft:stone");
                    breakBlock(player(helper, "drops_silk", GameMode.SURVIVAL, silkTouch(ItemKeys.IRON_PICKAXE)), TARGET);
                })
                .waitUntil(() -> dropped(world, ItemKeys.STONE) == 1, "Stone mined with Silk Touch should drop itself")
                .execute(() -> helper.assertTrue(dropped(world, ItemKeys.COBBLESTONE) == 0, "Silk Touch should give no cobblestone"))
                .build();
    }

    @ScenarioTest
    public static void pickaxeTierDecidesOreDrops(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicInteger settled = new AtomicInteger();
        final AtomicReference<ServerPlayer> player = new AtomicReference<>();
        helper.sequence()
                .execute(() -> {
                    set(world, TARGET, "minecraft:iron_ore");
                    set(world, OTHER_TARGET, "minecraft:iron_ore");
                    player.set(player(helper, "drops_prospector", GameMode.SURVIVAL, ItemStack.of(ItemKeys.WOODEN_PICKAXE)));
                    breakBlock(player.get(), TARGET);
                })
                .waitUntil(() -> helper.assertBlockAt(TARGET, BlockTypeKeys.AIR))
                .waitUntil(() -> settled.incrementAndGet() >= SETTLE_TICKS, "Leave the time for a drop to show up")
                .execute(() -> {
                    helper.assertTrue(dropped(world) == 0, "A wooden pickaxe should get nothing out of iron ore");
                    player.get().inventory().set(player.get().selectedSlot(), ItemStack.of(ItemKeys.STONE_PICKAXE));
                    breakBlock(player.get(), OTHER_TARGET);
                })
                .waitUntil(() -> dropped(world, ItemKeys.RAW_IRON) >= 1, "A stone pickaxe should get raw iron out of iron ore")
                .execute(() -> helper.assertTrue(dropped(world, ItemKeys.IRON_ORE) == 0, "Without Silk Touch the ore itself should not drop"))
                .build();
    }

    @ScenarioTest
    public static void logsDropWhenBrokenByHand(final ScenarioTestHelper helper) {
        final World world = helper.world();
        helper.sequence()
                .execute(() -> {
                    set(world, TARGET, "minecraft:oak_log[axis=y]");
                    breakBlock(player(helper, "drops_lumberjack", GameMode.SURVIVAL, ItemStack.EMPTY), TARGET);
                })
                .waitUntil(() -> dropped(world, ItemKeys.OAK_LOG) == 1, "A log broken by hand should drop itself")
                .build();
    }

    @ScenarioTest
    public static void cropsStillGiveTheirHarvest(final ScenarioTestHelper helper) {
        final World world = helper.world();
        helper.sequence()
                .execute(() -> {
                    set(world, TARGET.relative(BlockFace.DOWN), "minecraft:farmland");
                    set(world, TARGET, "minecraft:wheat[age=7]");
                    breakBlock(player(helper, "drops_reaper", GameMode.SURVIVAL, ItemStack.EMPTY), TARGET);
                })
                .waitUntil(() -> dropped(world, ItemKeys.WHEAT) == 1, "Ripe wheat broken by hand should drop one wheat")
                .execute(() -> {
                    final int seeds = dropped(world, ItemKeys.WHEAT_SEEDS);
                    helper.assertTrue(seeds >= 1 && seeds <= 4, "Ripe wheat drops one to four seeds, dropped " + seeds);
                })
                .build();
    }

    @ScenarioTest
    public static void creativeBreaksWithoutDrops(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicInteger settled = new AtomicInteger();
        helper.sequence()
                .execute(() -> {
                    set(world, TARGET, "minecraft:oak_log[axis=y]");
                    breakBlock(player(helper, "drops_builder", GameMode.CREATIVE, ItemStack.of(ItemKeys.DIAMOND_AXE)), TARGET);
                })
                .waitUntil(() -> helper.assertBlockAt(TARGET, BlockTypeKeys.AIR))
                .waitUntil(() -> settled.incrementAndGet() >= SETTLE_TICKS, "Leave the time for a drop to show up")
                .execute(() -> helper.assertTrue(dropped(world) == 0, "A block broken in creative mode should drop nothing"))
                .build();
    }

    @ScenarioTest
    public static void blockDropsGameRuleStopsDrops(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicInteger settled = new AtomicInteger();
        helper.sequence()
                .execute(() -> {
                    world.gameRules().setBoolean(GameRuleKeys.BLOCK_DROPS, false);
                    set(world, TARGET, "minecraft:oak_log[axis=y]");
                    breakBlock(player(helper, "drops_ruled", GameMode.SURVIVAL, ItemStack.EMPTY), TARGET);
                })
                .waitUntil(() -> helper.assertBlockAt(TARGET, BlockTypeKeys.AIR))
                .waitUntil(() -> settled.incrementAndGet() >= SETTLE_TICKS, "Leave the time for a drop to show up")
                .execute(() -> {
                    world.gameRules().setBoolean(GameRuleKeys.BLOCK_DROPS, true);
                    helper.assertTrue(dropped(world) == 0, "Nothing should drop with block drops off");
                })
                .build();
    }

    @ScenarioTest
    public static void listenersReshapeTheDrops(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicReference<Subscription> subscription = new AtomicReference<>();
        final AtomicInteger events = new AtomicInteger();
        helper.sequence()
                .execute(() -> {
                    set(world, TARGET, "minecraft:stone");
                    final ServerPlayer player = player(helper, "drops_alchemist", GameMode.SURVIVAL, ItemStack.of(ItemKeys.WOODEN_PICKAXE));
                    subscription.set(listenToBreaks(world, event -> {
                        events.incrementAndGet();
                        helper.assertTrue(event.player() == player, "The player breaking the block is the event's");
                        helper.assertTrue(event.position().equals(TARGET), "The event is about the broken block, got " + event.position());
                        helper.assertTrue(event.block().key().equals(BlockTypeKeys.STONE), "The block of the event is stone, got " + event.block().key());
                        helper.assertTrue(event.tool().id().equals(ItemKeys.WOODEN_PICKAXE), "The tool of the event is the pickaxe in hand");
                        final List<ItemStack> drops = event.drops();
                        helper.assertTrue(drops.size() == 1 && drops.getFirst().id().equals(ItemKeys.COBBLESTONE),
                                "The event should hold the cobblestone about to drop, holds " + drops);
                        drops.clear();
                        drops.add(ItemStack.of(ItemKeys.DIAMOND, 2));
                    }));
                    breakBlock(player, TARGET);
                })
                .waitUntil(() -> dropped(world, ItemKeys.DIAMOND) == 2, "What the listener left in the drops should drop")
                .execute(() -> {
                    subscription.get().unsubscribe();
                    helper.assertTrue(events.get() == 1, "One break is one event, got " + events.get());
                    helper.assertTrue(dropped(world, ItemKeys.COBBLESTONE) == 0, "What the listener removed should not drop");
                    helper.assertBlockAt(TARGET, BlockTypeKeys.AIR);
                })
                .build();
    }

    @ScenarioTest
    public static void cancelledBreakKeepsTheBlockAndDropsNothing(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicReference<Subscription> subscription = new AtomicReference<>();
        final AtomicInteger events = new AtomicInteger();
        final AtomicInteger settled = new AtomicInteger();
        helper.sequence()
                .execute(() -> {
                    set(world, TARGET, "minecraft:stone");
                    subscription.set(listenToBreaks(world, event -> {
                        events.incrementAndGet();
                        event.setCancelled(true);
                    }));
                    breakBlock(player(helper, "drops_vandal", GameMode.SURVIVAL, ItemStack.of(ItemKeys.WOODEN_PICKAXE)), TARGET);
                })
                .waitUntil(() -> events.get() == 1, "The break should fire its event")
                .waitUntil(() -> settled.incrementAndGet() >= SETTLE_TICKS, "Leave the time for a drop to show up")
                .execute(() -> {
                    subscription.get().unsubscribe();
                    helper.assertBlockAt(TARGET, BlockTypeKeys.STONE);
                    helper.assertTrue(dropped(world) == 0, "A cancelled break should drop nothing, dropped " + dropped(world) + " items");
                })
                .build();
    }

    @ScenarioTest
    public static void cropsLeaveTheirHarvestToTheHarvestEvent(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicReference<Subscription> breaks = new AtomicReference<>();
        final AtomicReference<Subscription> harvests = new AtomicReference<>();
        final AtomicInteger harvested = new AtomicInteger();
        helper.sequence()
                .execute(() -> {
                    set(world, TARGET.relative(BlockFace.DOWN), "minecraft:farmland");
                    set(world, TARGET, "minecraft:wheat[age=7]");
                    breaks.set(listenToBreaks(world, event -> {
                        helper.assertTrue(event.drops().isEmpty(), "The harvest of a crop is not in the break event, got " + event.drops());
                        event.drops().add(ItemStack.of(ItemKeys.STICK, 1));
                    }));
                    harvests.set(server().events().subscribe(HarvestEvent.class, event -> {
                        if (event.world() == world) {
                            harvested.incrementAndGet();
                        }
                    }));
                    breakBlock(player(helper, "drops_gleaner", GameMode.SURVIVAL, ItemStack.EMPTY), TARGET);
                })
                .waitUntil(() -> dropped(world, ItemKeys.WHEAT) == 1 && dropped(world, ItemKeys.STICK) == 1,
                        "The crop should give its harvest and what the listener added")
                .execute(() -> {
                    breaks.get().unsubscribe();
                    harvests.get().unsubscribe();
                    helper.assertTrue(harvested.get() == 1, "Breaking a crop should fire one harvest event, fired " + harvested.get());
                })
                .build();
    }

    @ScenarioTest
    public static void whatListenersAddDropsEvenInCreative(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicReference<Subscription> subscription = new AtomicReference<>();
        helper.sequence()
                .execute(() -> {
                    set(world, TARGET, "minecraft:oak_log[axis=y]");
                    subscription.set(listenToBreaks(world, event -> {
                        helper.assertTrue(event.drops().isEmpty(), "Nothing drops in creative mode, got " + event.drops());
                        event.drops().add(ItemStack.of(ItemKeys.EMERALD, 1));
                    }));
                    breakBlock(player(helper, "drops_gifted", GameMode.CREATIVE, ItemStack.EMPTY), TARGET);
                })
                .waitUntil(() -> dropped(world, ItemKeys.EMERALD) == 1, "What a listener adds should drop in creative mode too")
                .execute(() -> {
                    subscription.get().unsubscribe();
                    helper.assertTrue(dropped(world, ItemKeys.OAK_LOG) == 0, "The log itself should not drop in creative mode");
                })
                .build();
    }
}
