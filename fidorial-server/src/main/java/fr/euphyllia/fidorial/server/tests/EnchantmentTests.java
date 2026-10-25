package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.codecs.adventure.ComponentCodecs;
import fr.euphyllia.fidorial.server.codecs.container.ContainerCodecs;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.item.ServerItemEntity;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.euphyllia.fidorial.server.entity.mob.MobFactories;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.inventory.ContainerMenu;
import fr.euphyllia.fidorial.server.inventory.InventoryMenu;
import fr.euphyllia.fidorial.server.item.enchantment.FidorialEnchantmentRegistry;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundContainerClickPacket;
import fr.fidorial.attribute.Attribute;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.GameMode;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.item.data.TypedDataComponentValue;
import fr.fidorial.item.enchantment.EnchantmentDefinition;
import fr.fidorial.item.enchantment.EnchantmentEffects;
import fr.fidorial.item.enchantment.EnchantmentTags;
import fr.fidorial.math.Location;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Enchantment;
import fr.fidorial.registry.keys.EnchantmentKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.World;
import io.papermc.adventurex.nbt.dfu.BinaryTagOps;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.io.IOException;
import java.util.Optional;

@SuppressWarnings("unused")
public final class EnchantmentTests {

    private static final double FLOOR = 65.0;

    private static FidorialServer server() {
        return FidorialServer.getInstance();
    }

    private static FidorialEnchantmentRegistry enchantments() {
        return server().registries().enchantments();
    }

    private static ServerPlayer player(final ScenarioTestHelper helper, final String name, final double x) {
        return (ServerPlayer) helper.summonPlayer(name, Location.of(helper.world(), x, FLOOR, 0.5, 0f, 0f), GameMode.SURVIVAL);
    }

    private static AbstractMob pig(final ScenarioTestHelper helper, final double x) {
        final AbstractMob pig = MobFactories.create(EntityTypes.PIG, server().entities().ids().allocate(),
                Location.of(helper.world(), x, FLOOR, 0.5, 0f, 0f));
        server().entities().spawn(pig);
        return pig;
    }

    private static ItemStack enchanted(final Key item, final TypedKey<Enchantment> enchantment, final int level) {
        return enchantments().enchant(ItemStack.of(item), enchantment, level);
    }

    private static void approx(final ScenarioTestHelper helper, final float actual, final float expected, final String what) {
        helper.assertTrue(Math.abs(actual - expected) < 1.0e-3f, what + ": expected " + expected + ", got " + actual);
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void vanillaEnchantmentsAreLoaded(final ScenarioTestHelper helper) {
        final FidorialEnchantmentRegistry registry = enchantments();
        helper.assertTrue(registry.keys().size() >= 43, "Expected the 43 vanilla enchantments, got " + registry.keys().size());
        final EnchantmentDefinition sharpness = registry.definition(EnchantmentKeys.SHARPNESS).orElseThrow();
        helper.assertTrue(sharpness.maxLevel() == 5, "Sharpness should go up to V, got " + sharpness.maxLevel());
        helper.assertTrue(sharpness.hasEffect(EnchantmentEffects.DAMAGE), "Sharpness should carry minecraft:damage");
        helper.assertTrue(!registry.isCustom(EnchantmentKeys.SHARPNESS), "Sharpness is a stock enchantment");
        helper.assertTrue(registry.isTagged(EnchantmentKeys.VANISHING_CURSE, EnchantmentTags.CURSE), "Vanishing is a curse");
        helper.assertTrue(!registry.areCompatible(EnchantmentKeys.SHARPNESS, EnchantmentKeys.SMITE),
                "Sharpness and Smite share an exclusive set");
        helper.assertTrue(registry.isSupportedItem(EnchantmentKeys.SHARPNESS, ItemKeys.DIAMOND_SWORD.key()),
                "Sharpness goes on swords");
        helper.assertTrue(!registry.isSupportedItem(EnchantmentKeys.SHARPNESS, ItemKeys.DIAMOND_BOOTS.key()),
                "Sharpness does not go on boots");
        helper.assertTrue(registry.supportedEffectComponents().contains(EnchantmentEffects.DAMAGE_PROTECTION),
                "The server runs minecraft:damage_protection");
        helper.assertTrue(registry.networkEntries().size() == registry.keys().size(), "Every enchantment is sent");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void enchantingAStack(final ScenarioTestHelper helper) {
        final FidorialEnchantmentRegistry registry = enchantments();
        ItemStack sword = ItemStack.of(ItemKeys.DIAMOND_SWORD.key());
        helper.assertTrue(registry.canEnchant(sword, EnchantmentKeys.SHARPNESS), "A sword takes Sharpness");
        sword = registry.enchant(sword, EnchantmentKeys.SHARPNESS, 4);
        helper.assertTrue(registry.level(sword, EnchantmentKeys.SHARPNESS) == 4, "The sword should have Sharpness IV");
        helper.assertTrue(!registry.canEnchant(sword, EnchantmentKeys.SMITE), "Smite cannot join Sharpness");
        helper.assertTrue(registry.canEnchant(sword, EnchantmentKeys.LOOTING), "Looting can join Sharpness");
        sword = registry.disenchant(sword, EnchantmentKeys.SHARPNESS);
        helper.assertTrue(registry.level(sword, EnchantmentKeys.SHARPNESS) == 0, "Sharpness should be gone");

        final ItemStack book = registry.enchantedBook(EnchantmentKeys.MENDING, 1);
        helper.assertTrue(book.has(DataComponentTypes.STORED_ENCHANTMENTS), "A book stores its enchantments");
        helper.assertTrue(registry.level(book, EnchantmentKeys.MENDING) == 0, "Stored enchantments do not act");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void enchantmentsSurviveSaveAndLoad(final ScenarioTestHelper helper) throws IOException {
        final ItemStack sword = enchanted(ItemKeys.DIAMOND_SWORD.key(), EnchantmentKeys.SHARPNESS, 5)
                .with(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false);
        final Optional<ItemStack> loadedSword = ContainerCodecs.readItem(ContainerCodecs.writeItem(sword));
        helper.assertTrue(loadedSword.isPresent() && loadedSword.get().equals(sword), "The sword changed: " + loadedSword);
        final ItemStack book = enchantments().enchantedBook(EnchantmentKeys.FORTUNE, 3);
        final Optional<ItemStack> loadedBook = ContainerCodecs.readItem(ContainerCodecs.writeItem(book));
        helper.assertTrue(loadedBook.isPresent() && loadedBook.get().equals(book), "The book changed: " + loadedBook);
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void enchantedBooksShowTheirEnchantmentsOnHover(final ScenarioTestHelper helper) {
        final ItemStack book = enchantments().enchantedBook(EnchantmentKeys.SHARPNESS, 5);
        final HoverEvent.ShowItem hover = book.asHoverEvent().value();
        helper.assertTrue(hover.dataComponents().get(DataComponentTypes.STORED_ENCHANTMENTS.key())
                        instanceof TypedDataComponentValue<?>,
                "The hover event should hold the stored enchantments: " + hover.dataComponents());

        final Component text = Component.translatable(book).hoverEvent(book);
        final BinaryTag encoded = ComponentCodecs.COMPONENT_CODEC.encodeStart(BinaryTagOps.binaryTagOps(), text).getOrThrow();
        final CompoundBinaryTag stored = ((CompoundBinaryTag) encoded).getCompound("hover_event")
                .getCompound("components").getCompound("minecraft:stored_enchantments");
        helper.assertTrue(stored.getInt("minecraft:sharpness") == 5, "Clients should be sent Sharpness V, got " + encoded);

        final Component decoded = ComponentCodecs.COMPONENT_CODEC.parse(BinaryTagOps.binaryTagOps(), encoded).getOrThrow();
        final ItemStack shown = ItemStack.of((HoverEvent.ShowItem) decoded.hoverEvent().value());
        helper.assertTrue(shown.equals(book), "The book should come back from the network unchanged: " + shown);
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void enchantedBooksSurviveAdventureSerializers(final ScenarioTestHelper helper) {
        final ItemStack book = enchantments().enchantedBook(EnchantmentKeys.MENDING, 1)
                .with(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false);
        final Component text = Component.text("Book").hoverEvent(book);

        final Component fromMiniMessage = MiniMessage.miniMessage().deserialize(MiniMessage.miniMessage().serialize(text));
        final ItemStack miniMessageBook = ItemStack.of((HoverEvent.ShowItem) fromMiniMessage.hoverEvent().value());
        helper.assertTrue(miniMessageBook.equals(book), "MiniMessage changed the book: " + miniMessageBook);

        final Component fromGson = GsonComponentSerializer.gson().deserialize(GsonComponentSerializer.gson().serialize(text));
        final ItemStack gsonBook = ItemStack.of((HoverEvent.ShowItem) fromGson.hoverEvent().value());
        helper.assertTrue(gsonBook.equals(book), "The Gson serializer changed the book: " + gsonBook);
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void sharpnessAndSmiteChangeMeleeDamage(final ScenarioTestHelper helper) {
        final ServerPlayer plain = player(helper, "PlainSword", 0.5);
        final ServerPlayer sharp = player(helper, "SharpSword", 3.5);
        final ServerPlayer holy = player(helper, "HolySword", 6.5);
        plain.setHeldItem(ItemStack.of(ItemKeys.DIAMOND_SWORD.key()));
        sharp.setHeldItem(enchanted(ItemKeys.DIAMOND_SWORD.key(), EnchantmentKeys.SHARPNESS, 5));
        holy.setHeldItem(enchanted(ItemKeys.DIAMOND_SWORD.key(), EnchantmentKeys.SMITE, 5));
        final AbstractMob first = pig(helper, 1.5);
        final AbstractMob second = pig(helper, 4.5);
        final AbstractMob third = pig(helper, 7.5);

        helper.assertTrue(server().entities().combat().attack(plain, first), "The plain hit should land");
        helper.assertTrue(server().entities().combat().attack(sharp, second), "The sharp hit should land");
        helper.assertTrue(server().entities().combat().attack(holy, third), "The holy hit should land");
        final float base = first.maxHealth() - first.health();
        approx(helper, second.maxHealth() - second.health() - base, 3.0f, "Sharpness V adds 3 damage");
        approx(helper, third.maxHealth() - third.health() - base, 0.0f, "Smite does nothing to a pig");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void protectionEnchantmentsReduceDamage(final ScenarioTestHelper helper) {
        final ServerPlayer armored = player(helper, "Armored", 0.5);
        final ServerPlayer falling = player(helper, "Falling", 3.5);
        final ServerPlayer bare = player(helper, "Bare", 6.5);
        armored.inventory().set(PlayerInventory.CHEST, enchanted(ItemKeys.IRON_CHESTPLATE.key(), EnchantmentKeys.PROTECTION, 4));
        falling.inventory().set(PlayerInventory.FEET, enchanted(ItemKeys.IRON_BOOTS.key(), EnchantmentKeys.FEATHER_FALLING, 4));

        armored.damage(DamageSource.mobAttack(null), 10f);
        falling.damage(DamageSource.fall(), 10f);
        bare.damage(DamageSource.fall(), 10f);
        approx(helper, armored.maxHealth() - armored.health(), 8.4f, "Protection IV takes 16% off");
        approx(helper, falling.maxHealth() - falling.health(), 5.2f, "Feather Falling IV takes 48% off a fall");
        approx(helper, bare.maxHealth() - bare.health(), 10f, "No enchantment, no protection");
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void efficiencyGivesItsAttributeWhileHeld(final ScenarioTestHelper helper) {
        final ServerPlayer player = player(helper, "Miner", 0.5);
        helper.sequence()
                .execute(() -> player.setHeldItem(enchanted(ItemKeys.DIAMOND_PICKAXE.key(), EnchantmentKeys.EFFICIENCY, 5)))
                .waitUntil(() -> player.attributeValue(Attribute.MINING_EFFICIENCY) == 26.0,
                        "Efficiency V should give 26 mining efficiency, got " + player.attributeValue(Attribute.MINING_EFFICIENCY))
                .execute(() -> player.setHeldItem(ItemStack.EMPTY))
                .waitUntil(() -> player.attributeValue(Attribute.MINING_EFFICIENCY) == 0.0,
                        "Putting the pickaxe away should remove the modifier")
                .build();
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void fireAspectSetsTheVictimOnFire(final ScenarioTestHelper helper) {
        final ServerPlayer player = player(helper, "Burner", 0.5);
        player.setHeldItem(enchanted(ItemKeys.DIAMOND_SWORD.key(), EnchantmentKeys.FIRE_ASPECT, 2));
        final AbstractMob pig = pig(helper, 1.5);
        helper.assertTrue(server().entities().combat().attack(player, pig), "The hit should land");
        helper.assertTrue(pig.fireTicks() >= 150, "Fire Aspect II burns for 8 seconds, got " + pig.fireTicks() + " ticks");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void thornsHurtsTheAttacker(final ScenarioTestHelper helper) {
        final ServerPlayer player = player(helper, "Spiky", 0.5);
        // Level VII: 15% per level, so every hit is returned.
        player.inventory().set(PlayerInventory.CHEST, enchanted(ItemKeys.IRON_CHESTPLATE.key(), EnchantmentKeys.THORNS, 7));
        final AbstractMob pig = pig(helper, 1.5);
        server().entities().combat().damage(player, DamageSource.mobAttack(pig), 1f);
        helper.assertTrue(pig.health() < pig.maxHealth(), "Thorns should have hurt the pig, it has " + pig.health());
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void curseOfBindingKeepsTheArmorOn(final ScenarioTestHelper helper) {
        final ServerPlayer survival = player(helper, "Bound", 0.5);
        final ItemStack helmet = enchanted(ItemKeys.IRON_HELMET.key(), EnchantmentKeys.BINDING_CURSE, 1);
        survival.inventory().set(PlayerInventory.HEAD, helmet);
        final ContainerMenu menu = survival.inventoryMenu();
        menu.click(new ServerboundContainerClickPacket(menu.windowId(), menu.stateId(), (short) InventoryMenu.ARMOR_FROM,
                (byte) 0, ServerboundContainerClickPacket.MODE_PICKUP));
        helper.assertTrue(menu.carried().isEmpty() && survival.inventory().get(PlayerInventory.HEAD).equals(helmet),
                "A cursed helmet should stay on");

        final ServerPlayer creative = player(helper, "Creative", 3.5);
        creative.setGameMode(GameMode.CREATIVE);
        creative.inventory().set(PlayerInventory.HEAD, helmet);
        final ContainerMenu creativeMenu = creative.inventoryMenu();
        creativeMenu.click(new ServerboundContainerClickPacket(creativeMenu.windowId(), creativeMenu.stateId(),
                (short) InventoryMenu.ARMOR_FROM, (byte) 0, ServerboundContainerClickPacket.MODE_PICKUP));
        helper.assertTrue(creativeMenu.carried().equals(helmet), "A creative player takes it off anyway");
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void curseOfVanishingDestroysOnDeath(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final ServerPlayer player = player(helper, "Vanishing", 8.5);
        helper.sequence()
                .execute(() -> {
                    player.inventory().set(0, enchanted(ItemKeys.DIAMOND_SWORD.key(), EnchantmentKeys.VANISHING_CURSE, 1));
                    player.inventory().set(1, ItemStack.of(ItemKeys.APPLE.key(), 3));
                    player.kill(player);
                })
                .waitUntil(() -> count(world, ItemKeys.APPLE.key()) == 3, "The apples should be dropped")
                .execute(() -> helper.assertTrue(count(world, ItemKeys.DIAMOND_SWORD.key()) == 0,
                        "The cursed sword should vanish"))
                .build();
    }

    private static int count(final World world, final Key item) {
        int total = 0;
        for (final var entity : world.entities()) {
            if (entity instanceof final ServerItemEntity dropped && !dropped.isRemoved() && dropped.item().id().equals(item)) {
                total += dropped.item().count();
            }
        }
        return total;
    }
}
