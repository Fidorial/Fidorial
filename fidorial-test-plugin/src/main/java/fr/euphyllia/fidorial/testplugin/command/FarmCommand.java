package fr.euphyllia.fidorial.testplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.testplugin.TestPlugin;
import fr.euphyllia.fidorial.testplugin.farming.TestCrops;
import fr.fidorial.command.CommandSender;
import fr.fidorial.command.CommandSource;
import fr.fidorial.entity.Player;
import fr.fidorial.item.ItemStack;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.Location;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockAccess;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.block.plant.CropBlock;
import net.kyori.adventure.key.Key;

import java.util.Map;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

/**
 * {@code /farm}: hands out sixteen of each custom crop's seed.
 * <br>{@code /farm plant <crop> [stage]}: puts the crop block right at the
 * player's feet, without going through items.
 */
public final class FarmCommand {

    private static final int STACK = 16;
    private static final int MAIN_INVENTORY_SLOTS = 36;

    private static final Map<String, Key> PLANTS = Map.of(
            "rice", TestCrops.RICE_PLANT,
            "tomato", TestCrops.TOMATO_PLANT,
            "hemp", TestCrops.HEMP_PLANT,
            "truffle", TestCrops.TRUFFLE_PLANT,
            "mint", TestCrops.MINT_PLANT);

    private final TestPlugin plugin;

    public FarmCommand(final TestPlugin plugin) {
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSource> create() {
        final LiteralArgumentBuilder<CommandSource> plant = literal("plant");
        for (final Map.Entry<String, Key> entry : PLANTS.entrySet()) {
            final Key block = entry.getValue();
            plant.then(literal(entry.getKey())
                    .executes(ctx -> plant(ctx, block, 0))
                    .then(argument("stage", IntegerArgumentType.integer(0, 15))
                            .executes(ctx -> plant(ctx, block, IntegerArgumentType.getInteger(ctx, "stage")))));
        }
        return literal("farm")
                .executes(this::give)
                .then(plant)
                .build();
    }

    private int plant(final CommandContext<CommandSource> ctx, final Key block, final int stage) {
        final CommandSender sender = ctx.getSource().sender();
        if (!(sender instanceof final Player player)) {
            plugin.msg(sender, "<red>[TestPlugin] Run this command in-game.</red>");
            return Command.SINGLE_SUCCESS;
        }

        final World world = player.world();
        final Location location = player.location();
        final BlockPos pos = new BlockPos(
                (int) Math.floor(location.x()),
                (int) Math.floor(location.y()),
                (int) Math.floor(location.z()));

        // Blocks can only be written from the thread of the region that owns them.
        world.scheduler().execute(world.key(), ChunkPos.fromBlock(pos.x(), pos.z()), () -> {
            final BlockBehaviour behaviour = Blocks.registry().behaviour(block).orElse(null);
            if (!(behaviour instanceof final CropBlock crop)) {
                plugin.msg(player, "<red>[TestPlugin] " + block.asString() + " is not a registered crop.</red>");
                return;
            }
            final BlockData seedling = crop.type().defaultData();
            if (seedling == null) {
                plugin.msg(player, "<red>[TestPlugin] " + block.asString() + " has no default state.</red>");
                return;
            }
            final BlockData data = crop.withAge(seedling, stage);
            final BlockAccess blocks = world.blocks();

            if (!blocks.setBlock(pos, data)) {
                plugin.msg(player, "<red>[TestPlugin] Could not place " + data.asString() + " at "
                        + pos.x() + " " + pos.y() + " " + pos.z() + ".</red>");
                return;
            }
            plugin.msg(player, "<green>[TestPlugin] Placed " + data.asString() + " at "
                    + pos.x() + " " + pos.y() + " " + pos.z() + ".</green>");
            if (!crop.canSurvive(data, blocks, pos)) {
                plugin.msg(player, "<yellow>[TestPlugin] Wrong soil under it: it will pop off as soon as a neighbour changes.</yellow>");
            }
        });
        return Command.SINGLE_SUCCESS;
    }

    private int give(final CommandContext<CommandSource> ctx) {
        final CommandSender sender = ctx.getSource().sender();
        if (!(sender instanceof final Player player)) {
            plugin.msg(sender, "<red>[TestPlugin] Run this command in-game.</red>");
            return Command.SINGLE_SUCCESS;
        }

        int given = 0;
        for (final Key seed : TestCrops.STARTER_KIT) {
            final int slot = firstEmptySlot(player);
            if (slot < 0) {
                plugin.msg(player, "<red>[TestPlugin] Your inventory is full.</red>");
                break;
            }
            final ItemStack stack = plugin.server().items().create(seed, STACK);
            player.inventory().set(slot, stack);
            given++;
        }
        player.updateInventory();

        plugin.msg(player, "<green>[TestPlugin] " + given + " kinds of seeds given.</green>");
        plugin.msg(player, "<gray> - <white>Rice</white>: farmland or mud, with water right next to the soil.</gray>");
        plugin.msg(player, "<gray> - <red>Tomatoes</red>: farmland; right-click when ripe, the plant stays.</gray>");
        plugin.msg(player, "<gray> - <green>Hemp</green>: farmland; climbs up to 3 blocks, break the bottom.</gray>");
        plugin.msg(player, "<gray> - <gold>Truffles</gold>: podzol, mycelium or rooted dirt, in the dark (light 7 or less).</gray>");
        plugin.msg(player, "<gray> - <aqua>Mint</aqua>: farmland, dirt or grass; spreads once ripe.</gray>");
        plugin.msg(player, "<gray>Or skip the seeds: /farm plant rice|tomato|hemp|truffle|mint [stage] puts one at your feet.</gray>");
        return Command.SINGLE_SUCCESS;
    }

    private static int firstEmptySlot(final Player player) {
        for (int slot = 0; slot < MAIN_INVENTORY_SLOTS; slot++) {
            if (player.inventory().get(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }
}
