package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.player.Player;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.map.MapCanvas;
import fr.fidorial.map.MapColor;
import fr.fidorial.map.MapFont;
import fr.fidorial.map.MapImage;
import fr.fidorial.map.MapLayer;
import fr.fidorial.map.MapManager;
import fr.fidorial.map.MapPalette;
import fr.fidorial.map.MapScale;
import fr.fidorial.map.MapShade;
import fr.fidorial.map.MapView;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.World;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public final class MapTests {

    private static MapManager maps() {
        return FidorialServer.getInstance().maps();
    }

    private static MapPalette palette() {
        return maps().palette();
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void createdMapsAreFoundByIdAndItem(final ScenarioTestHelper helper) {
        final MapView view = maps().create(helper.world(), 0, 0, MapScale.ONE_TO_TWO);
        helper.assertTrue(maps().get(view.id()).orElse(null) == view, "The new map is not found by its id");
        final ItemStack item = view.item();
        helper.assertTrue(item.id().equals(ItemKeys.FILLED_MAP.key()), "The item of a map is not a filled map");
        helper.assertTrue(maps().of(item).orElse(null) == view, "The item does not lead back to its map");
        helper.assertTrue(view.world().orElseThrow().equals(helper.world().key()), "The map does not show its world");
        helper.assertTrue(maps().createBlank().id() > view.id(), "Ids are not allocated in order");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void drawnMapsAreSavedInTheVanillaFolder(final ScenarioTestHelper helper) throws Exception {
        final MapView view = maps().createBlank();
        view.canvas().fill(10, 10, 20, 20, palette().pixel(MapColor.COLOR_RED));
        view.canvas().text(2, 2, "Fidorial", palette().pixel(MapColor.COLOR_BLACK));
        FidorialServer.getInstance().maps().autoSave();
        final Path file = FidorialServer.getInstance().worlds().manager().paths().dataDir()
                .resolve("minecraft").resolve("maps").resolve(view.id() + ".dat");
        helper.assertTrue(Files.isRegularFile(file), "The map was not saved to " + file);
        helper.assertTrue(palette().color(view.canvas().pixel(15, 15)) == MapColor.COLOR_RED, "The canvas lost a pixel");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void temporaryMapsAreNotSaved(final ScenarioTestHelper helper) throws Exception {
        final MapView view = maps().createTemporary();
        view.canvas().fill(palette().pixel(MapColor.COLOR_BLUE));
        FidorialServer.getInstance().maps().autoSave();
        final Path file = FidorialServer.getInstance().worlds().manager().paths().dataDir()
                .resolve("minecraft").resolve("maps").resolve(view.id() + ".dat");
        helper.assertTrue(view.temporary() && !Files.exists(file), "A temporary map was saved");
        maps().delete(view);
        helper.assertTrue(maps().get(view.id()).isEmpty(), "A deleted temporary map is still found");
    }

    @ScenarioTest(timeoutTicks = 400)
    public static void terrainIsRendered(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final MapView view = maps().create(world, 8, 8, MapScale.ONE_TO_ONE);
        final AtomicReference<CompletableFuture<Void>> render = new AtomicReference<>();
        helper.sequence()
                .execute(() -> render.set(view.renderTerrain()))
                .waitUntil(() -> helper.assertTrue(render.get().isDone(), "The terrain is still rendering"))
                .execute(() -> {
                    render.get().join();
                    helper.assertTrue(!palette().isTransparent(view.canvas().pixel(64, 64)),
                            "The center of a rendered terrain map is still empty");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void usingAnEmptyMapCreatesOne(final ScenarioTestHelper helper) {
        final Player player = helper.summonPlayer("Mapper", helper.world(),
                Location.of(helper.world(), 100.5, -60, -30.5, 0f, 0f), GameMode.SURVIVAL);
        player.inventory().set(player.selectedSlot(), ItemStack.of(ItemKeys.MAP.key(), 1));
        helper.sequence()
                .execute(() -> player.execute(() ->
                        FidorialServer.getInstance().maps().useEmptyMap((ServerPlayer) player, false)))
                .waitUntil(() -> helper.assertTrue(
                        player.inventory().get(player.selectedSlot()).id().equals(ItemKeys.FILLED_MAP.key()),
                        "The empty map was not turned into a map"))
                .execute(() -> {
                    final ItemStack held = player.inventory().get(player.selectedSlot());
                    final MapView view = maps().of(held).orElseThrow();
                    helper.assertTrue(view.centerX() == 128 && view.centerZ() == 0,
                            "The new map is not aligned on the grid: " + view.centerX() + ", " + view.centerZ());
                    helper.assertTrue(view.tracksPlayers(), "A new map does not track players");
                    helper.assertTrue(held.get(DataComponentTypes.MAP_ID) == view.id(), "Wrong map id on the item");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void carriersSeeTheMapAndItsLayers(final ScenarioTestHelper helper) {
        final Player player = helper.summonPlayer("Viewer", helper.world(),
                Location.of(helper.world(), 0.5, -60, 0.5, 0f, 0f), GameMode.SURVIVAL);
        final MapView view = maps().create(helper.world(), 0, 0, MapScale.ONE_TO_ONE);
        final int[] paints = {0};
        view.addLayer(maps().layer(MapLayer.Scope.PER_VIEWER, 0, context -> {
            paints[0]++;
            context.canvas().text(1, 1, context.viewer().orElseThrow().name(), palette().pixel(MapColor.COLOR_BLACK));
        }));
        player.inventory().set(player.selectedSlot(), view.item());
        helper.sequence()
                .waitUntil(() -> helper.assertTrue(view.viewers().contains(player), "The carrier does not see the map"))
                .execute(() -> helper.assertTrue(paints[0] >= 1, "The per-viewer layer was not painted"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void managerMakesDrawingTools(final ScenarioTestHelper helper) {
        final MapPalette palette = palette();
        helper.assertTrue(palette.color(palette.pixel(MapColor.GRASS, MapShade.LIGHT)) == MapColor.GRASS,
                "A pixel does not keep its color");
        helper.assertTrue(palette.color(palette.closest(0x7FB238)) == MapColor.GRASS, "The closest color is wrong");
        helper.assertTrue(maps().alignedCenter(100, MapScale.ONE_TO_ONE) == 128, "Grid alignment is wrong");
        helper.assertTrue(maps().directionOf(180f) == 0, "A player facing north does not point up");
        final MapImage image = maps().image(2, 1, new int[] {0xFFFF0000, 0x00000000}, MapImage.Dithering.NONE);
        helper.assertTrue(palette.isTransparent(image.pixel(1, 0)) && !palette.isTransparent(image.pixel(0, 0)),
                "Image conversion lost transparency");
        final MapCanvas canvas = maps().createCanvas();
        final int width = canvas.text(0, 0, "Hi", palette.pixel(MapColor.COLOR_BLACK));
        helper.assertTrue(width == maps().standardFont().width("Hi") && width > 0, "Text width is inconsistent");
        final MapFont font = maps().fontBuilder(8, 0).glyph('x', 0, "#.#", ".#.", "#.#").build();
        helper.assertTrue(font.glyph('x') != null && font.glyph('x').width() == 3, "A custom glyph was lost");
    }
}
