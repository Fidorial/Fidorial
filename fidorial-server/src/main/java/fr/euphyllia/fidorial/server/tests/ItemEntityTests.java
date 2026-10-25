package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.entity.item.ServerItemEntity;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.inventory.ContainerMenu;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundContainerClickPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.entity.AnvilEntitySerializer;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.ItemEntity;
import fr.fidorial.event.Subscription;
import fr.fidorial.event.player.PlayerDeathEvent;
import fr.fidorial.event.player.PlayerPickupItemEvent;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.World;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public final class ItemEntityTests {

    /** Items rest on a stone floor whose top is at this height. */
    private static final double FLOOR_TOP = 65.0;
    private static final int FLOOR_RADIUS = 4;

    private static FidorialServer server() {
        return FidorialServer.getInstance();
    }

    private static ServerPlayer player(final ScenarioTestHelper helper, final String name) {
        return (ServerPlayer) helper.summonPlayer(name, Location.of(helper.world(), 0.5, FLOOR_TOP, 0.5, 0f, 0f), GameMode.SURVIVAL);
    }

    private static void buildFloor(final World world) {
        final int stone = server().registries().blockStates().networkId(BlockState.of(BlockTypeKeys.STONE.key()));
        for (int x = -FLOOR_RADIUS; x <= FLOOR_RADIUS; x++) {
            for (int z = -FLOOR_RADIUS; z <= FLOOR_RADIUS; z++) {
                world.setBlockStateId(Position.block(x, (int) FLOOR_TOP - 1, z), stone);
            }
        }
    }

    private static void assertFloorBuilt(final ScenarioTestHelper helper) {
        helper.assertBlockAt(Position.block(-FLOOR_RADIUS, (int) FLOOR_TOP - 1, -FLOOR_RADIUS), BlockTypeKeys.STONE.key());
        helper.assertBlockAt(Position.block(FLOOR_RADIUS, (int) FLOOR_TOP - 1, FLOOR_RADIUS), BlockTypeKeys.STONE.key());
    }

    /**
     * Drops a stack at an exact spot, without the random push of {@link World#dropItem}.
     */
    private static ServerItemEntity dropStill(final World world, final double x, final double y, final double z,
                                              final ItemStack stack, final int pickupDelay) {
        return server().entities().dropItem((ServerWorld) world, x, y, z, stack, 0.0, 0.0, 0.0, pickupDelay, null);
    }

    private static List<ServerItemEntity> items(final World world) {
        final List<ServerItemEntity> items = new ArrayList<>();
        for (final var entity : world.entities()) {
            if (entity instanceof final ServerItemEntity item && !item.isRemoved()) {
                items.add(item);
            }
        }
        return items;
    }

    private static int countOnGround(final World world, final Key item) {
        int total = 0;
        for (final ServerItemEntity entity : items(world)) {
            if (entity.item().id().equals(item)) {
                total += entity.item().count();
            }
        }
        return total;
    }

    private static int count(final PlayerInventory inventory, final Key item) {
        int total = 0;
        for (int slot = 0; slot < inventory.size(); slot++) {
            final ItemStack stack = inventory.get(slot);
            if (stack.id().equals(item)) {
                total += stack.count();
            }
        }
        return total;
    }

    @ScenarioTest(timeoutTicks = 100)
    public static void droppedItemFallsAndIsPickedUp(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicReference<ItemEntity> dropped = new AtomicReference<>();
        final AtomicReference<ServerPlayer> player = new AtomicReference<>();

        helper.sequence()
                .execute(() -> buildFloor(world))
                .waitUntil(() -> assertFloorBuilt(helper))
                .execute(() -> {
                    player.set(player(helper, "Collector"));
                    dropped.set(world.dropItem(Location.of(world, 0.5, FLOOR_TOP + 1.5, 0.5, 0f, 0f),
                            ItemStack.of(ItemKeys.DIAMOND.key(), 3)));
                    helper.assertTrue(dropped.get().pickupDelay() == ItemEntity.DEFAULT_PICKUP_DELAY,
                            "A dropped item should wait before it can be picked up");
                })
                .waitUntil(() -> count(player.get().inventory(), ItemKeys.DIAMOND.key()) == 3,
                        "The player standing on the item should pick the 3 diamonds up")
                .execute(() -> {
                    helper.assertTrue(dropped.get().isRemoved(), "A fully picked up item should be gone");
                    helper.assertTrue(items(world).isEmpty(), "No item should be left on the ground");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 100)
    public static void itemRestsOnTheFloor(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicReference<ServerItemEntity> dropped = new AtomicReference<>();
        final AtomicInteger stillTicks = new AtomicInteger();

        helper.sequence()
                .execute(() -> buildFloor(world))
                .waitUntil(() -> assertFloorBuilt(helper))
                .execute(() -> dropped.set(dropStill(world, 2.5, FLOOR_TOP + 4.0, 2.5, ItemStack.of(ItemKeys.STICK.key(), 1), 0)))
                .waitUntil(() -> dropped.get().onGround(), "The item should land")
                .waitUntil(() -> stillTicks.incrementAndGet() >= 10, "Let it sit for a while")
                .execute(() -> {
                    final Location at = dropped.get().location();
                    helper.assertTrue(Math.abs(at.y() - FLOOR_TOP) < 1.0E-3,
                            "The item should rest on top of the floor at y=" + FLOOR_TOP + ", got " + at.y());
                    helper.assertTrue(!dropped.get().isRemoved(), "Nobody is around: the item should still be there");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 120)
    public static void similarStacksMerge(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicReference<ServerItemEntity> diamond = new AtomicReference<>();

        helper.sequence()
                .execute(() -> buildFloor(world))
                .waitUntil(() -> assertFloorBuilt(helper))
                .execute(() -> {
                    dropStill(world, -2.5, FLOOR_TOP, -2.5, ItemStack.of(ItemKeys.COBBLESTONE.key(), 10), 0);
                    dropStill(world, -2.2, FLOOR_TOP, -2.5, ItemStack.of(ItemKeys.COBBLESTONE.key(), 20), 0);
                    diamond.set(dropStill(world, -2.5, FLOOR_TOP, -2.2, ItemStack.of(ItemKeys.DIAMOND.key(), 1), 0));
                })
                .waitUntil(() -> items(world).size() == 2, "The two cobblestone stacks should merge into one")
                .execute(() -> {
                    helper.assertTrue(countOnGround(world, ItemKeys.COBBLESTONE.key()) == 30,
                            "Merging must neither lose nor make items");
                    helper.assertTrue(!diamond.get().isRemoved() && diamond.get().item().count() == 1,
                            "A different item must not merge");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 100)
    public static void fullInventoryTakesOnlyWhatFits(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicReference<ServerPlayer> player = new AtomicReference<>();
        final AtomicReference<ServerItemEntity> dropped = new AtomicReference<>();

        helper.sequence()
                .execute(() -> buildFloor(world))
                .waitUntil(() -> assertFloorBuilt(helper))
                .execute(() -> {
                    player.set(player(helper, "FullPockets"));
                    final PlayerInventory inventory = player.get().inventory();
                    for (int slot = 0; slot < PlayerInventory.STORAGE_SIZE; slot++) {
                        inventory.set(slot, ItemStack.of(ItemKeys.STONE.key(), 64));
                    }
                    inventory.set(PlayerInventory.MAIN_START, ItemStack.of(ItemKeys.DIRT.key(), 60));
                    dropped.set(dropStill(world, 0.5, FLOOR_TOP, 0.5, ItemStack.of(ItemKeys.DIRT.key(), 10), 0));
                })
                .waitUntil(() -> count(player.get().inventory(), ItemKeys.DIRT.key()) == 64,
                        "The player should take the 4 dirt that fit")
                .execute(() -> helper.assertTrue(!dropped.get().isRemoved() && dropped.get().item().count() == 6,
                        "The 6 dirt that do not fit should stay on the ground, got " + dropped.get().item()))
                .build();
    }

    @ScenarioTest(timeoutTicks = 100)
    public static void cancelledPickupLeavesTheItem(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicReference<ServerPlayer> player = new AtomicReference<>();
        final AtomicReference<Subscription> subscription = new AtomicReference<>();
        final AtomicInteger attempts = new AtomicInteger();
        final AtomicReference<ServerItemEntity> dropped = new AtomicReference<>();

        helper.sequence()
                .execute(() -> buildFloor(world))
                .waitUntil(() -> assertFloorBuilt(helper))
                .execute(() -> {
                    player.set(player(helper, "Refused"));
                    subscription.set(server().events().subscribe(PlayerPickupItemEvent.class, event -> {
                        if (event.player() == player.get()) {
                            attempts.incrementAndGet();
                            event.setCancelled(true);
                        }
                    }));
                    dropped.set(dropStill(world, 0.5, FLOOR_TOP, 0.5, ItemStack.of(ItemKeys.APPLE.key(), 2), 0));
                })
                .waitUntil(() -> attempts.get() >= 5, "The pickup event should fire while the player stands on the item")
                .execute(() -> {
                    subscription.get().unsubscribe();
                    helper.assertTrue(count(player.get().inventory(), ItemKeys.APPLE.key()) == 0,
                            "A cancelled pickup must give nothing");
                    helper.assertTrue(!dropped.get().isRemoved(), "A cancelled pickup must leave the item");
                })
                .waitUntil(() -> count(player.get().inventory(), ItemKeys.APPLE.key()) == 2,
                        "Once allowed again, the pickup should go through")
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void ownedItemIgnoresOtherPlayers(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicReference<ServerPlayer> player = new AtomicReference<>();
        final AtomicReference<ServerItemEntity> dropped = new AtomicReference<>();
        final AtomicInteger waited = new AtomicInteger();

        helper.sequence()
                .execute(() -> buildFloor(world))
                .waitUntil(() -> assertFloorBuilt(helper))
                .execute(() -> {
                    player.set(player(helper, "NotTheOwner"));
                    dropped.set(dropStill(world, 0.5, FLOOR_TOP, 0.5, ItemStack.of(ItemKeys.STICK.key(), 4), 0));
                    dropped.get().setOwner(UUID.randomUUID());
                })
                .waitUntil(() -> waited.incrementAndGet() >= 20, "Give the player time to try")
                .execute(() -> {
                    helper.assertTrue(count(player.get().inventory(), ItemKeys.STICK.key()) == 0,
                            "Only the owner may pick an owned item up");
                    dropped.get().setOwner(player.get().uuid());
                })
                .waitUntil(() -> count(player.get().inventory(), ItemKeys.STICK.key()) == 4,
                        "The owner should pick the item up")
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void throwingOutOfAWindowDropsTheItem(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final ServerPlayer player = player(helper, "Thrower");

        helper.sequence()
                .execute(() -> {
                    player.inventory().set(PlayerInventory.MAIN_START, ItemStack.of(ItemKeys.STICK.key(), 5));
                    final ContainerMenu menu = player.inventoryMenu();
                    // window slot 9 is the first slot of the main inventory; button 0 throws one item
                    menu.click(new ServerboundContainerClickPacket(menu.windowId(), menu.stateId(), (short) 9, (byte) 0,
                            ServerboundContainerClickPacket.MODE_THROW));
                    helper.assertTrue(player.inventory().get(PlayerInventory.MAIN_START).count() == 4,
                            "The drop key over a slot should take one item out");
                    // button 1 throws the whole stack
                    menu.click(new ServerboundContainerClickPacket(menu.windowId(), menu.stateId(), (short) 9, (byte) 1,
                            ServerboundContainerClickPacket.MODE_THROW));
                    helper.assertTrue(player.inventory().get(PlayerInventory.MAIN_START).isEmpty(),
                            "Ctrl + the drop key should take the whole stack out");
                })
                .waitUntil(() -> countOnGround(world, ItemKeys.STICK.key()) == 5, "The 5 sticks should be on the ground")
                .execute(() -> {
                    for (final ServerItemEntity item : items(world)) {
                        helper.assertTrue(player.uuid().equals(item.thrower()), "The player should be the thrower");
                        helper.assertTrue(item.pickupDelay() > ItemEntity.DEFAULT_PICKUP_DELAY,
                                "A thrown item should not land straight back in the inventory");
                    }
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void deadPlayerDropsTheirInventory(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final ServerPlayer player = player(helper, "Unlucky");

        helper.sequence()
                .execute(() -> {
                    player.inventory().set(3, ItemStack.of(ItemKeys.APPLE.key(), 7));
                    player.inventory().set(PlayerInventory.HEAD, ItemStack.of(ItemKeys.IRON_HELMET.key(), 1));
                    player.kill(player);
                    helper.assertTrue(player.inventory().isEmpty(), "A dead player should lose their inventory");
                })
                .waitUntil(() -> countOnGround(world, ItemKeys.APPLE.key()) == 7
                                && countOnGround(world, ItemKeys.IRON_HELMET.key()) == 1,
                        "The apples and the helmet should be on the ground")
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void deathEventCanKeepTheInventory(final ScenarioTestHelper helper) {
        final ServerPlayer player = player(helper, "Lucky");
        try (final Subscription _ = server().events().subscribe(PlayerDeathEvent.class, event -> {
            if (event.player() == player) {
                event.setKeepInventory(true);
            }
        })) {
            player.inventory().set(0, ItemStack.of(ItemKeys.DIAMOND.key(), 2));
            player.kill(player);
            helper.assertTrue(count(player.inventory(), ItemKeys.DIAMOND.key()) == 2,
                    "keepInventory should keep the diamonds");
            helper.assertTrue(items(helper.world()).isEmpty(), "Nothing should be dropped");
        }
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void itemEntitySurvivesSaveAndLoad(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final UUID owner = UUID.randomUUID();
        final UUID thrower = UUID.randomUUID();
        final ServerItemEntity original = new ServerItemEntity(server().entities().ids().allocate(),
                Location.of(world, 1.25, 70.0, -3.75, 0f, 0f), ItemStack.of(ItemKeys.DIAMOND.key(), 17));
        original.setAge(1234);
        original.setPickupDelay(25);
        original.setOwner(owner);
        original.setThrower(thrower);

        final AnvilEntitySerializer serializer = new AnvilEntitySerializer();
        final CompoundBinaryTag nbt = serializer.toNbt(original);
        helper.assertTrue(nbt.getString("id").equals("minecraft:item"), "Saved with the wrong id: " + nbt.getString("id"));

        final AtomicInteger ids = new AtomicInteger(1_000_000);
        final AbstractEntity loaded = serializer.fromNbt(nbt, world, ids::incrementAndGet);
        helper.assertTrue(loaded instanceof ServerItemEntity, "An item entity should load back as one, got " + loaded);
        final ServerItemEntity item = (ServerItemEntity) loaded;
        helper.assertTrue(item.item().equals(original.item()), "The stack changed: " + item.item());
        helper.assertTrue(item.uuid().equals(original.uuid()), "The UUID changed");
        helper.assertTrue(item.age() == 1234 && item.pickupDelay() == 25, "Age or pickup delay changed");
        helper.assertTrue(owner.equals(item.owner()) && thrower.equals(item.thrower()), "Owner or thrower changed");
        helper.assertTrue(item.location().x() == 1.25 && item.location().y() == 70.0 && item.location().z() == -3.75,
                "The position changed: " + item.location());

        original.setUnlimitedLifetime(true);
        final ServerItemEntity forever = (ServerItemEntity) serializer.fromNbt(serializer.toNbt(original), world, ids::incrementAndGet);
        helper.assertTrue(forever != null && forever.hasUnlimitedLifetime(), "An unlimited lifetime should be saved");
    }
}
