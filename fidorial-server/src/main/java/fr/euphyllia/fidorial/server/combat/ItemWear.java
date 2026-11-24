package fr.euphyllia.fidorial.server.combat;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.item.enchantment.EnchantmentEngine;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundEntityEventPacket;
import fr.fidorial.entity.GameMode;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemDefaults;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.registry.keys.SoundEventKeys;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Wears the stacks a living entity holds or wears: weapons that hit, armor that takes a hit, shields
 * that block one, bows that shoot.
 *
 * <p>Unbreaking may spare some of the wear. A stack worn out breaks: it is gone, and the viewers
 * see and hear it shatter. A player in creative mode wears nothing.</p>
 */
public final class ItemWear {

    private ItemWear() {
        throw new UnsupportedOperationException("ItemWear cannot be instantiated.");
    }

    /**
     * Wears the stack in a slot of an entity.
     *
     * @param owner  the entity holding or wearing it
     * @param slot   the slot
     * @param amount the durability it loses before Unbreaking
     */
    public static void hurt(final AbstractLivingEntity owner, final EquipmentSlot slot, final int amount) {
        if (amount <= 0 || (owner instanceof final ServerPlayer player && player.gameMode() == GameMode.CREATIVE)) {
            return;
        }
        final ItemStack stack = owner.equipment(slot);
        final ItemStack worn = wear(owner, stack, amount);
        if (worn == stack) {
            return;
        }
        setEquipment(owner, slot, worn);
        if (worn.isEmpty()) {
            broke(owner, slot);
        }
    }

    /**
     * {@return a stack once it lost some durability, {@link ItemStack#EMPTY} once it broke, the same
     * stack when it does not wear}
     *
     * @param owner  the entity holding or wearing it
     * @param stack  the stack
     * @param amount the durability it loses before Unbreaking
     */
    public static ItemStack wear(final AbstractLivingEntity owner, final ItemStack stack, final int amount) {
        if (stack.isEmpty() || amount <= 0) {
            return stack;
        }
        final int durability = ItemDefaults.maxDamage(stack.id(), stack);
        if (durability <= 0) {
            return stack;
        }
        final int lost = EnchantmentEngine.itemDamage(owner, stack, amount);
        if (lost <= 0) {
            return stack;
        }
        final int damage = stack.getOrDefault(DataComponentTypes.DAMAGE, 0) + lost;
        return damage >= durability ? ItemStack.EMPTY : stack.with(DataComponentTypes.DAMAGE, damage);
    }

    /**
     * {@return the hand of a player holding a slot of their inventory: the off hand for its slot,
     * the main hand otherwise}
     */
    public static EquipmentSlot handOf(final int inventorySlot) {
        return inventorySlot == PlayerInventory.OFF_HAND ? EquipmentSlot.OFF_HAND : EquipmentSlot.MAIN_HAND;
    }

    /**
     * Puts a stack in a slot of an entity, and lets the player who owns it know.
     */
    public static void setEquipment(final AbstractLivingEntity owner, final EquipmentSlot slot, final ItemStack stack) {
        if (owner instanceof final ServerPlayer player) {
            player.setEquipment(slot, stack);
        }
    }

    /**
     * Shows and plays a stack in a slot of an entity breaking.
     */
    public static void broke(final AbstractLivingEntity owner, final EquipmentSlot slot) {
        final ClientboundEntityEventPacket event = new ClientboundEntityEventPacket(owner.entityId(), breakEvent(slot));
        owner.sendToTrackers(event);
        if (owner instanceof final ServerPlayer player) {
            player.connection().send(event);
        }
        FidorialServer.getInstance().entities().combat().playAt(owner, SoundEventKeys.ENTITY_ITEM_BREAK.key(), 0.8f,
                0.8f + ThreadLocalRandom.current().nextFloat() * 0.4f);
    }

    private static byte breakEvent(final EquipmentSlot slot) {
        return switch (slot) {
            case MAIN_HAND -> 47;
            case OFF_HAND -> 48;
            case HEAD -> 49;
            case CHEST -> 50;
            case LEGS -> 51;
            case FEET -> 52;
            case BODY -> 65;
            case SADDLE -> 68;
        };
    }
}
