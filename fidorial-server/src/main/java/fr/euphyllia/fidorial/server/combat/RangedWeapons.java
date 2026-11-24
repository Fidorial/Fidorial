package fr.euphyllia.fidorial.server.combat;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.entity.projectile.ArrowEntity;
import fr.euphyllia.fidorial.server.entity.projectile.ArrowProjectileEntity;
import fr.euphyllia.fidorial.server.entity.projectile.SpectralArrowEntity;
import fr.euphyllia.fidorial.server.entity.projectile.TridentEntity;
import fr.euphyllia.fidorial.server.events.entity.EntityShootProjectileEventImpl;
import fr.euphyllia.fidorial.server.item.enchantment.EnchantmentEngine;
import fr.fidorial.entity.GameMode;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemDefaults;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.component.ItemEnchantments;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.item.enchantment.EnchantmentDefinition;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.EnchantmentKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.ItemTagKeys;
import fr.fidorial.registry.keys.SoundEventKeys;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The items a player uses rather than swings: a shield they raise, a bow they draw, a crossbow
 * they load then shoot, a trident they ready then throw.
 *
 * <p>Using starts when the player presses the use key and lasts as long as they hold it; their
 * client then tells the server they let go. Other players see the item raised from the hand
 * states of the player.</p>
 */
public final class RangedWeapons {

    private static final int MAX_USE_TICKS = 72_000;

    private static final double BOW_SPEED = 3.0;
    private static final float BOW_MIN_POWER = 0.1f;
    private static final double CROSSBOW_ARROW_SPEED = 3.15;
    private static final float CROSSBOW_CHARGE_SECONDS = 1.25f;
    private static final int CROSSBOW_EXTRA_USE_TICKS = 3;
    private static final float CROSSBOW_START_SOUND_AT = 0.2f;
    private static final float CROSSBOW_MIDDLE_SOUND_AT = 0.5f;
    private static final double TRIDENT_SPEED = 2.5;
    private static final int TRIDENT_MIN_USE_TICKS = 10;
    private static final double INACCURACY = 1.0;
    private static final double SHOT_BELOW_EYES = 0.1;

    private final FidorialServer server;
    private final CombatEngine combat;

    RangedWeapons(final FidorialServer server, final CombatEngine combat) {
        this.server = server;
        this.combat = combat;
    }

    /**
     * {@return the cooldown group of a stack: the one its {@code minecraft:use_cooldown} names, its
     * item otherwise}
     */
    public static Key cooldownGroup(final ItemStack stack) {
        final ItemCombatData.UseCooldown cooldown = ItemCombatData.of(stack).useCooldown();
        return cooldown != null && cooldown.group() != null ? cooldown.group() : stack.id().key();
    }

    /**
     * Starts using the item of a hand, or shoots a loaded crossbow.
     *
     * @param player the player
     * @param hand   the hand, {@link EquipmentSlot#MAIN_HAND} or {@link EquipmentSlot#OFF_HAND}
     * @return {@code true} if the item is one of those this class handles
     */
    public boolean use(final ServerPlayer player, final EquipmentSlot hand) {
        if (player.isDead() || player.gameMode() == GameMode.SPECTATOR) {
            return false;
        }
        final ItemStack stack = player.equipment(hand);
        if (stack.isEmpty()) {
            return false;
        }
        if (player.isCoolingDown(cooldownGroup(stack))) {
            return true;
        }
        if (ItemCombatData.of(stack).blocksAttacks() != null) {
            start(player, hand, stack);
            return true;
        }
        if (stack.id().equals(ItemKeys.BOW)) {
            if (player.gameMode() == GameMode.CREATIVE || !findAmmo(player).isEmpty()) {
                start(player, hand, stack);
            }
            return true;
        }
        if (stack.id().equals(ItemKeys.CROSSBOW)) {
            final ChargedProjectiles charged = stack.get(ChargedProjectiles.TYPE);
            if (charged != null && !charged.isEmpty()) {
                shootCrossbow(player, hand, stack, charged);
            } else if (player.gameMode() == GameMode.CREATIVE || !findAmmo(player).isEmpty()) {
                start(player, hand, stack);
            }
            return true;
        }
        if (stack.id().equals(ItemKeys.TRIDENT)) {
            if (!isAboutToBreak(stack) && !hasEnchantment(stack, EnchantmentKeys.RIPTIDE)) {
                start(player, hand, stack);
            }
            return true;
        }
        return false;
    }

    private static void start(final ServerPlayer player, final EquipmentSlot hand, final ItemStack stack) {
        player.itemUse().start(hand, stack);
        player.syncLivingFlags();
    }

    /**
     * Counts one more tick of use of the item of a player: plays the loading sounds of a crossbow,
     * and stops the use once it lasted as long as it can.
     */
    public void tickUse(final ServerPlayer player) {
        final ItemUse use = player.itemUse();
        if (!use.isUsing()) {
            return;
        }
        use.tick();
        final ItemStack stack = use.item();
        if (stack.id().equals(ItemKeys.CROSSBOW)) {
            final int chargeTicks = crossbowChargeTicks(player, stack);
            final float charge = (float) use.ticks() / chargeTicks;
            if (use.stage() == 0 && charge >= CROSSBOW_START_SOUND_AT) {
                use.setStage(1);
                combat.playAt(player, SoundEventKeys.ITEM_CROSSBOW_LOADING_START.key(), 0.5f, 1.0f);
            }
            if (use.stage() == 1 && charge >= CROSSBOW_MIDDLE_SOUND_AT) {
                use.setStage(2);
                combat.playAt(player, SoundEventKeys.ITEM_CROSSBOW_LOADING_MIDDLE.key(), 0.5f, 1.0f);
            }
            if (use.ticks() >= chargeTicks + CROSSBOW_EXTRA_USE_TICKS) {
                release(player);
            }
            return;
        }
        if (use.ticks() >= MAX_USE_TICKS) {
            release(player);
        }
    }

    /**
     * Stops using the item of a player: a drawn bow shoots, a loaded crossbow keeps its
     * projectiles, a readied trident is thrown, a shield is lowered.
     */
    public void release(final ServerPlayer player) {
        final ItemUse use = player.itemUse();
        final EquipmentSlot hand = use.hand();
        if (hand == null) {
            return;
        }
        final int ticks = use.ticks();
        final ItemStack used = use.item();
        final ItemStack stack = player.equipment(hand);
        use.stop();
        player.syncLivingFlags();
        if (player.isDead() || !stack.id().equals(used.id())) {
            return;
        }
        if (stack.id().equals(ItemKeys.BOW)) {
            releaseBow(player, hand, stack, ticks);
        } else if (stack.id().equals(ItemKeys.CROSSBOW)) {
            releaseCrossbow(player, hand, stack, ticks);
        } else if (stack.id().equals(ItemKeys.TRIDENT)) {
            releaseTrident(player, hand, stack, ticks);
        }
    }

    /**
     * Stops using the item of a player without its effect: a drawn bow lowers without shooting, a
     * crossbow being loaded stays empty, a readied trident is not thrown. Since 26.4-pre-1.
     */
    public void cancel(final ServerPlayer player) {
        final ItemUse use = player.itemUse();
        if (!use.isUsing()) {
            return;
        }
        use.stop();
        player.syncLivingFlags();
    }

    /**
     * {@return how hard a bow drawn for some ticks shoots, between {@code 0} and {@code 1}}
     */
    public static float bowPower(final int ticks) {
        final float seconds = ticks / 20.0f;
        final float power = (seconds * seconds + seconds * 2.0f) / 3.0f;
        return Math.min(1.0f, power);
    }

    private void releaseBow(final ServerPlayer player, final EquipmentSlot hand, final ItemStack bow, final int ticks) {
        final float power = bowPower(ticks);
        if (power < BOW_MIN_POWER) {
            return;
        }
        final Ammo ammo = takeAmmo(player, bow);
        if (ammo == null) {
            return;
        }
        final List<ArrowProjectileEntity> shot = shoot(player, bow, ammo, power * BOW_SPEED, power == 1.0f, false);
        if (shot.isEmpty()) {
            return;
        }
        ammo.consume(player);
        ItemWear.hurt(player, hand, shot.size());
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        combat.playAt(player, SoundEventKeys.ENTITY_ARROW_SHOOT.key(), 1.0f,
                1.0f / (random.nextFloat() * 0.4f + 1.2f) + power * 0.5f);
    }

    /**
     * {@return the ticks a crossbow takes to load: a second and a quarter, less with Quick Charge}
     */
    public int crossbowChargeTicks(final ServerPlayer player, final ItemStack crossbow) {
        final float seconds = EnchantmentEngine.processStack(player, crossbow, CROSSBOW_CHARGE_SECONDS, "crossbowChargeTime",
                (effects, item, value) -> effects.modifyCrossbowChargeTime(item, value));
        return Math.max(1, (int) Math.floor(Math.max(0f, seconds) * 20.0f));
    }

    private void releaseCrossbow(final ServerPlayer player, final EquipmentSlot hand, final ItemStack crossbow,
                                 final int ticks) {
        final ChargedProjectiles charged = crossbow.get(ChargedProjectiles.TYPE);
        if ((charged != null && !charged.isEmpty()) || ticks < crossbowChargeTicks(player, crossbow)) {
            return;
        }
        final Ammo ammo = takeAmmo(player, crossbow);
        if (ammo == null) {
            return;
        }
        final int count = Math.max(1, Math.round(EnchantmentEngine.processStack(player, crossbow, 1f, "projectileCount",
                (effects, item, value) -> effects.modifyProjectileCount(item, value))));
        final List<ItemStack> loaded = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            loaded.add(ammo.stack().withCount(1));
        }
        ammo.consume(player);
        ItemWear.setEquipment(player, hand, crossbow.with(ChargedProjectiles.TYPE,
                new ChargedProjectiles(loaded)));
        combat.playAt(player, SoundEventKeys.ITEM_CROSSBOW_LOADING_END.key(), 1.0f,
                1.0f / (ThreadLocalRandom.current().nextFloat() * 0.5f + 1.0f) + 0.2f);
    }

    private void shootCrossbow(final ServerPlayer player, final EquipmentSlot hand, final ItemStack crossbow,
                               final ChargedProjectiles charged) {
        final List<ItemStack> projectiles = charged.items();
        final float spread = projectiles.size() <= 1 ? 0f : EnchantmentEngine.processStack(player, crossbow, 0f,
                "projectileSpread", (effects, item, value) -> effects.modifyProjectileSpread(item, value));
        final float step = projectiles.size() <= 1 ? 0f : 2.0f * spread / (projectiles.size() - 1);
        final float first = (projectiles.size() - 1) % 2 * step / 2.0f;
        float side = 1.0f;
        int fired = 0;
        for (int index = 0; index < projectiles.size(); index++) {
            final int pair = (index + 1) / 2;
            final float angle = first + side * pair * step;
            side = -side;
            final Ammo ammo = new Ammo(projectiles.get(index), -1, index > 0 || player.gameMode() == GameMode.CREATIVE);
            final ArrowProjectileEntity arrow = spawnArrow(player, crossbow, ammo, CROSSBOW_ARROW_SPEED, true, true, angle);
            if (arrow != null) {
                fired++;
                combat.playAt(player, SoundEventKeys.ITEM_CROSSBOW_SHOOT.key(), 1.0f, crossbowPitch(index));
            }
        }
        ItemWear.setEquipment(player, hand, crossbow.reset(ChargedProjectiles.TYPE));
        ItemWear.hurt(player, hand, fired);
    }

    private static float crossbowPitch(final int index) {
        if (index == 0) {
            return 1.0f;
        }
        final float high = index % 2 == 1 ? 0.43f : 0.53f;
        return 1.0f / (ThreadLocalRandom.current().nextFloat() * 0.5f + 1.8f) + high;
    }

    private void releaseTrident(final ServerPlayer player, final EquipmentSlot hand, final ItemStack trident,
                                final int ticks) {
        if (ticks < TRIDENT_MIN_USE_TICKS || isAboutToBreak(trident) || hasEnchantment(trident, EnchantmentKeys.RIPTIDE)) {
            return;
        }
        final boolean creative = player.gameMode() == GameMode.CREATIVE;
        final ItemStack worn = creative ? trident : ItemWear.wear(player, trident, 1);
        if (worn.isEmpty()) {
            return;
        }
        final int loyalty = Math.round(EnchantmentEngine.processStack(player, worn, 0f, "tridentReturnAcceleration",
                (effects, item, value) -> effects.modifyTridentReturnAcceleration(item, value)));
        final TridentEntity thrown = new TridentEntity(server.entities().ids().allocate(),
                shotFrom(player), player, worn.withCount(1), loyalty);
        aim(thrown, player, 0f, TRIDENT_SPEED);
        thrown.setPickup(creative ? ArrowProjectileEntity.PickupRule.CREATIVE_PLAYERS : ArrowProjectileEntity.PickupRule.ANY_PLAYER);
        if (server.events().post(new EntityShootProjectileEventImpl(player, worn, thrown, 1.0f)).isCancelled()) {
            player.updateInventory();
            return;
        }
        server.entities().spawn(thrown);
        if (!creative) {
            ItemWear.setEquipment(player, hand, worn.count() <= 1 ? ItemStack.EMPTY : worn.withCount(worn.count() - 1));
        }
        combat.playAt(thrown, SoundEventKeys.ITEM_TRIDENT_THROW.key(), 1.0f, 1.0f);
    }

    private static boolean isAboutToBreak(final ItemStack stack) {
        final int durability = ItemDefaults.maxDamage(stack.id(), stack);
        return durability > 0 && stack.getOrDefault(DataComponentTypes.DAMAGE, 0) >= durability - 1;
    }

    private static boolean hasEnchantment(final ItemStack stack, final TypedKey<EnchantmentDefinition> enchantment) {
        final ItemEnchantments enchantments = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.EMPTY);
        return enchantments.levels().containsKey(enchantment);
    }

    /**
     * The ammunition of a shot.
     *
     * @param stack the stack it comes from
     * @param slot  the slot of the inventory it is taken from, {@code -1} if none
     * @param free  {@code true} when shooting it uses nothing up: creative mode, Infinity, a copy of
     *              Multishot
     */
    private record Ammo(ItemStack stack, int slot, boolean free) {

        void consume(final ServerPlayer player) {
            if (free || slot < 0) {
                return;
            }
            final PlayerInventory inventory = player.inventory();
            final ItemStack current = inventory.get(slot);
            inventory.set(slot, current.count() <= 1 ? ItemStack.EMPTY : current.withCount(current.count() - 1));
        }
    }

    /**
     * {@return the ammunition a player shoots with a weapon, {@code null} if they have none}: the
     * arrows in the off hand first, then those of the main hand, then the first ones of the
     * inventory; a player in creative mode always has a plain arrow
     */
    private @Nullable Ammo takeAmmo(final ServerPlayer player, final ItemStack weapon) {
        final int slot = findAmmoSlot(player);
        final boolean creative = player.gameMode() == GameMode.CREATIVE;
        if (slot < 0) {
            return creative ? new Ammo(ItemStack.of(ItemKeys.ARROW), -1, true) : null;
        }
        final ItemStack stack = player.inventory().get(slot);
        final boolean free = creative || EnchantmentEngine.ammoUse(player, weapon, stack, 1) <= 0;
        return new Ammo(stack, slot, free);
    }

    private ItemStack findAmmo(final ServerPlayer player) {
        final int slot = findAmmoSlot(player);
        return slot < 0 ? ItemStack.EMPTY : player.inventory().get(slot);
    }

    private int findAmmoSlot(final ServerPlayer player) {
        final PlayerInventory inventory = player.inventory();
        if (isArrow(inventory.get(PlayerInventory.OFF_HAND))) {
            return PlayerInventory.OFF_HAND;
        }
        if (isArrow(inventory.get(player.selectedSlot()))) {
            return player.selectedSlot();
        }
        for (int slot = 0; slot < PlayerInventory.OFF_HAND && slot < inventory.size(); slot++) {
            if (isArrow(inventory.get(slot))) {
                return slot;
            }
        }
        return -1;
    }

    private boolean isArrow(final ItemStack stack) {
        return !stack.isEmpty() && server.registries().registry(RegistryKey.ITEM).isTagged(stack.id(), ItemTagKeys.ARROWS);
    }

    /**
     * Shoots the projectiles of one draw of a bow: one, or more with Multishot, spread around the
     * look of the shooter.
     */
    private List<ArrowProjectileEntity> shoot(final ServerPlayer player, final ItemStack weapon, final Ammo ammo,
                                              final double speed, final boolean critical, final boolean crossbow) {
        final int count = Math.max(1, Math.round(EnchantmentEngine.processStack(player, weapon, 1f, "projectileCount",
                (effects, item, value) -> effects.modifyProjectileCount(item, value))));
        final float spread = count <= 1 ? 0f : EnchantmentEngine.processStack(player, weapon, 0f, "projectileSpread",
                (effects, item, value) -> effects.modifyProjectileSpread(item, value));
        final float step = count <= 1 ? 0f : 2.0f * spread / (count - 1);
        final float first = (count - 1) % 2 * step / 2.0f;
        float side = 1.0f;
        final List<ArrowProjectileEntity> shot = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            final int pair = (index + 1) / 2;
            final float angle = first + side * pair * step;
            side = -side;
            final Ammo projectile = index == 0 ? ammo : new Ammo(ammo.stack(), -1, true);
            final ArrowProjectileEntity arrow = spawnArrow(player, weapon, projectile, speed, critical, crossbow, angle);
            if (arrow != null) {
                shot.add(arrow);
            }
        }
        return shot;
    }

    private @Nullable ArrowProjectileEntity spawnArrow(final ServerPlayer player, final ItemStack weapon, final Ammo ammo,
                                                       final double speed, final boolean critical, final boolean crossbow,
                                                       final float yawOffset) {
        final Location from = shotFrom(player);
        final ItemStack item = ammo.stack().withCount(1);
        final int id = server.entities().ids().allocate();
        final ArrowProjectileEntity arrow = item.id().equals(ItemKeys.SPECTRAL_ARROW)
                ? new SpectralArrowEntity(id, from, player, weapon, item)
                : new ArrowEntity(id, from, player, weapon, item);
        arrow.setCritical(critical);
        arrow.setFromCrossbow(crossbow);
        arrow.setPiercing(Math.round(EnchantmentEngine.processStack(player, weapon, 0f, "projectilePiercing",
                (effects, value, current) -> effects.modifyProjectilePiercing(value, current))));
        arrow.setPickup(ammo.free() ? ArrowProjectileEntity.PickupRule.CREATIVE_PLAYERS : ArrowProjectileEntity.PickupRule.ANY_PLAYER);
        aim(arrow, player, yawOffset, speed);
        EnchantmentEngine.projectileSpawned(player, weapon, arrow);
        if (server.events().post(new EntityShootProjectileEventImpl(player, weapon, arrow,
                (float) Math.min(1.0, speed / BOW_SPEED))).isCancelled()) {
            return null;
        }
        server.entities().spawn(arrow);
        return arrow;
    }

    private static Location shotFrom(final ServerPlayer player) {
        final Location at = player.location();
        return Location.of(at.world(), at.x(), at.y() + player.eyeHeight() - SHOT_BELOW_EYES, at.z(), at.yaw(), at.pitch());
    }

    /**
     * Sends a projectile where a player looks, turned by {@code yawOffset} degrees.
     */
    private static void aim(final ArrowProjectileEntity projectile, final ServerPlayer player, final float yawOffset,
                            final double speed) {
        final Location at = player.location();
        final double[] look = Hitboxes.look(at.yaw() + yawOffset, at.pitch());
        projectile.launch(look[0], look[1], look[2], speed, INACCURACY);
    }
}
