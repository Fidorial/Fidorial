package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.codecs.container.PlayerEnderChestCodec;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.inventory.EnderChestMenu;
import fr.euphyllia.fidorial.server.inventory.EnderChestRows;
import fr.euphyllia.fidorial.server.inventory.HiddenEnderChestRows;
import fr.euphyllia.fidorial.server.inventory.ServerEnderChestInventory;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.GameMode;
import fr.fidorial.inventory.EnderChestInventory;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.permission.PermissionDefinition;
import fr.fidorial.permission.PermissionGrant;
import fr.fidorial.permission.PermissionNode;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.MenuTypeKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import net.kyori.adventure.util.TriState;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

import static fr.euphyllia.fidorial.server.tests.RedstoneTests.defaults;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.load;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.server;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.set;

/**
 * Ender chests of one to six rows, chosen by the {@code fidorial.enderchest.rows.*} permissions,
 * and what becomes of the rows a player no longer sees: cleared, the default, or kept.
 */
@SuppressWarnings("unused")
public final class EnderChestTests {

    private static final int AIR_Y = 65;

    /**
     * What grants the permissions in these tests.
     */
    private static final Plugin OWNER = new Plugin() {
    };

    private EnderChestTests() {
    }

    private static ServerPlayer player(final ScenarioTestHelper helper, final String name, final BlockPosition near) {
        return (ServerPlayer) helper.summonPlayer(name,
                Location.of(helper.world(), near.blockX() + 0.5, near.blockY(), near.blockZ() + 2.5, 180f, 0f),
                GameMode.SURVIVAL);
    }

    private static BlockPosition enderChest(final World world, final int x, final int z) {
        final BlockPosition position = Position.block(x, AIR_Y, z);
        load(world, position);
        set(world, position, defaults(BlockTypeKeys.ENDER_CHEST));
        return position;
    }

    /**
     * Uses the ender chest the way a right click does: through its behaviour.
     */
    private static EnderChestMenu open(final ServerPlayer player, final BlockPosition position) {
        final FidorialServer server = FidorialServer.getInstance();
        final BlockData data = server.registries().blocks().fromNetworkId(player.world().blockStateId(position));
        if (data == null || !server.registries().blocks().behaviour(data.key()).orElseThrow().use(player, position, data)
                || !(player.containerMenu() instanceof final EnderChestMenu menu)) {
            throw new AssertionError("Using the ender chest at " + position + " should open it");
        }
        return menu;
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void enderChestShowsTheMostRowsThePermissionsGive(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition position = Position.block(2, AIR_Y, 2);
        final ServerPlayer player = player(helper, "EnderRowsUser", position);
        helper.sequence()
                .execute(() -> {
                    enderChest(world, 2, 2);
                    helper.assertTrue(player.enderChest().size() == EnderChestInventory.SIZE,
                            "An ender chest should hold the slots of six rows, holds " + player.enderChest().size());
                    helper.assertTrue(player.enderChestRows() == EnderChestInventory.DEFAULT_ROWS,
                            "A player granted no row should see the default rows, sees " + player.enderChestRows());
                    final EnderChestMenu plain = open(player, position);
                    helper.assertTrue(plain.rows() == 3 && plain.menuType().equals(MenuTypeKeys.GENERIC_9X3)
                                    && plain.slotCount() == 27 + 36,
                            "By default the ender chest should open three rows");
                    player.closeContainer();

                    final PermissionGrant grant = player.newGrant(OWNER)
                            .allow(EnderChestRows.permission(6))
                            .allow(EnderChestRows.permission(1));
                    try {
                        helper.assertTrue(player.enderChestRows() == 6,
                                "The most rows granted should win, the player sees " + player.enderChestRows());
                        final EnderChestMenu large = open(player, position);
                        helper.assertTrue(large.rows() == 6 && large.menuType().equals(MenuTypeKeys.GENERIC_9X6)
                                        && large.slotCount() == 54 + 36,
                                "Six rows granted should open six rows");
                        player.closeContainer();
                    } finally {
                        grant.revoke();
                    }

                    final PermissionGrant fewer = player.newGrant(OWNER).allow(EnderChestRows.permission(1));
                    try {
                        helper.assertTrue(player.enderChestRows() == 1,
                                "A single row granted should shrink the ender chest below the default, sees "
                                        + player.enderChestRows());
                        helper.assertTrue(open(player, position).menuType().equals(MenuTypeKeys.GENERIC_9X1),
                                "One row granted should open one row");
                        player.closeContainer();
                    } finally {
                        fewer.revoke();
                    }

                    final PermissionGrant all = player.newGrant(OWNER).allow(EnderChestRows.PERMISSION_ROOT + ".*");
                    try {
                        helper.assertTrue(player.enderChestRows() == 6,
                                "The wildcard should give every row, the player sees " + player.enderChestRows());
                    } finally {
                        all.revoke();
                    }
                    helper.assertTrue(player.enderChestRows() == 3, "Once the grants are gone, the default rows should be back");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void rowsNoLongerSeenAreClearedByDefault(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition position = Position.block(2, AIR_Y, 6);
        final ServerPlayer player = player(helper, "EnderRowsCleared", position);
        helper.sequence()
                .execute(() -> {
                    enderChest(world, 2, 6);
                    final EnderChestInventory enderChest = player.enderChest();
                    enderChest.set(4, ItemStack.of(ItemKeys.DIAMOND, 4));
                    enderChest.set(20, ItemStack.of(ItemKeys.EMERALD, 2));
                    enderChest.set(50, ItemStack.of(ItemKeys.GOLD_INGOT, 3));

                    final PermissionGrant grant = player.newGrant(OWNER).allow(EnderChestRows.permission(6));
                    try {
                        open(player, position);
                        player.closeContainer();
                        helper.assertTrue(enderChest.get(50).equals(ItemStack.of(ItemKeys.GOLD_INGOT, 3)),
                                "Opening six rows should clear nothing");
                    } finally {
                        grant.revoke();
                    }

                    final EnderChestMenu menu = open(player, position);
                    player.closeContainer();
                    helper.assertTrue(menu.rows() == 3, "The grant revoked, the ender chest should be back to three rows");
                    helper.assertTrue(enderChest.get(50).isEmpty(), "The rows no longer seen should be cleared, got " + enderChest.get(50));
                    helper.assertTrue(enderChest.get(4).equals(ItemStack.of(ItemKeys.DIAMOND, 4))
                                    && enderChest.get(20).equals(ItemStack.of(ItemKeys.EMERALD, 2)),
                            "The rows still seen should keep their items");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void rowsNoLongerSeenAreKeptWhenConfiguredSo(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition position = Position.block(2, AIR_Y, 10);
        final ServerPlayer player = player(helper, "EnderRowsKept", position);
        helper.sequence()
                .execute(() -> {
                    enderChest(world, 2, 10);
                    final EnderChestInventory enderChest = player.enderChest();
                    enderChest.set(0, ItemStack.of(ItemKeys.DIAMOND, 1));
                    enderChest.set(40, ItemStack.of(ItemKeys.EMERALD, 7));

                    final EnderChestMenu kept = new EnderChestMenu(player, player.allocateWindowId(), (ServerWorld) world,
                            position, 2, HiddenEnderChestRows.KEEP);
                    helper.assertTrue(player.openMenu(kept) && kept.menuType().equals(MenuTypeKeys.GENERIC_9X2),
                            "A window of two rows should open");
                    player.closeContainer();
                    helper.assertTrue(enderChest.get(40).equals(ItemStack.of(ItemKeys.EMERALD, 7)),
                            "With hidden-rows: keep, the rows no longer seen should keep their items");

                    final EnderChestMenu cleared = new EnderChestMenu(player, player.allocateWindowId(), (ServerWorld) world,
                            position, 2, HiddenEnderChestRows.CLEAR);
                    player.openMenu(cleared);
                    player.closeContainer();
                    helper.assertTrue(enderChest.get(40).isEmpty() && enderChest.get(0).equals(ItemStack.of(ItemKeys.DIAMOND, 1)),
                            "With hidden-rows: clear, only the rows no longer seen should be emptied");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void hiddenRowsAreSavedAndRowPermissionsGrantedToNobody(final ScenarioTestHelper helper) {
        helper.sequence()
                .execute(() -> {
                    final EnderChestInventory saved = new ServerEnderChestInventory();
                    saved.set(53, ItemStack.of(ItemKeys.NETHERITE_INGOT, 2));
                    final EnderChestInventory read;
                    try {
                        read = PlayerEnderChestCodec.decode(PlayerEnderChestCodec.encode(null, saved));
                    } catch (final IOException exception) {
                        throw new UncheckedIOException(exception);
                    }
                    helper.assertTrue(read.get(53).equals(ItemStack.of(ItemKeys.NETHERITE_INGOT, 2)),
                            "The last slot of the sixth row should be saved, read " + read.get(53));

                    for (int rows = 1; rows <= EnderChestInventory.MAX_ROWS; rows++) {
                        final Optional<PermissionDefinition> definition = server().registries().permissions()
                                .definition(PermissionNode.of(EnderChestRows.permission(rows)));
                        helper.assertTrue(definition.isPresent()
                                        && definition.get().defaultFor(true) == TriState.NOT_SET
                                        && definition.get().defaultFor(false) == TriState.NOT_SET,
                                "The permission of " + rows + " rows should be declared and granted to nobody, operators included");
                    }
                })
                .build();
    }
}
