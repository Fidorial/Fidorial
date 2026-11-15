package fr.euphyllia.fidorial.server.entity.ai;

import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.entity.GameMode;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.keys.MobEffectKeys;
import org.jspecify.annotations.Nullable;

/**
 * Tells whom a mob may go after, as vanilla's targeting conditions for combat: a living entity it
 * is allowed to attack, close enough to be noticed, and in sight if the mob has to see it.
 */
public final class Targeting {

    /**
     * A mob always notices what stands this close, however well hidden, as in Vanilla.
     */
    private static final double MIN_RANGE = 2.0;
    private static final double SNEAKING_VISIBILITY = 0.8;
    private static final double INVISIBLE_VISIBILITY = 0.7;
    private static final float MIN_ARMOR_COVER = 0.1f;
    private static final double DISGUISED_VISIBILITY = 0.5;
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final int DIFFICULTY_PEACEFUL = 0;

    private Targeting() {
    }

    /**
     * {@return {@code true} if a mob may attack a living entity at all}: as in Vanilla, anything
     * alive but itself, and players only out of peaceful, creative and spectator mode
     */
    public static boolean canAttack(final AbstractPathfinderMob mob, final @Nullable AbstractLivingEntity target) {
        if (target == null || target == mob || target.isRemoved() || target.isDead()
                || !target.world().equals(mob.world())) {
            return false;
        }
        if (target instanceof final ServerPlayer player) {
            final GameMode mode = player.gameMode();
            return mode != GameMode.CREATIVE && mode != GameMode.SPECTATOR
                    && mob.server().worlds().manager().levelData().difficulty != DIFFICULTY_PEACEFUL;
        }
        return true;
    }

    /**
     * Tells whether a mob picks a living entity as its target.
     *
     * @param range        how far the mob notices its targets, in blocks; a target that hides is
     *                     noticed from closer, see {@link #visibility}
     * @param lineOfSight  {@code true} if the mob has to see the target
     * @param testsStealth {@code false} to notice a target that hides as easily as any other
     * @return {@code true} if the mob may go after the target
     */
    public static boolean test(final AbstractPathfinderMob mob, final @Nullable AbstractLivingEntity target,
                               final double range, final boolean lineOfSight, final boolean testsStealth) {
        if (target == null || !canAttack(mob, target)) {
            return false;
        }
        if (range > 0.0) {
            final double visible = Math.max(range * (testsStealth ? visibility(target, mob) : 1.0), MIN_RANGE);
            final Location self = mob.location();
            final Location other = target.location();
            final double dx = self.x() - other.x();
            final double dy = self.y() - other.y();
            final double dz = self.z() - other.z();
            if (dx * dx + dy * dy + dz * dz > visible * visible) {
                return false;
            }
        }
        return !lineOfSight || mob.hasLineOfSightTo(target);
    }

    /**
     * {@return how much of the range of a mob a living entity is noticed from, between {@code 0} and
     * {@code 1}}. As in Vanilla, sneaking takes a fifth off; being invisible leaves 70% of the share
     * of armour worn, a tenth at least; wearing the head of the kind of the mob halves it.
     */
    public static double visibility(final AbstractLivingEntity target, final AbstractPathfinderMob mob) {
        if (!(target instanceof final ServerPlayer player)) {
            return target.hasEffect(MobEffectKeys.INVISIBILITY) ? INVISIBLE_VISIBILITY * MIN_ARMOR_COVER : 1.0;
        }
        double visibility = 1.0;
        if (player.isSneaking()) {
            visibility *= SNEAKING_VISIBILITY;
        }
        if (player.hasEffect(MobEffectKeys.INVISIBILITY)) {
            int worn = 0;
            for (final EquipmentSlot slot : ARMOR_SLOTS) {
                if (!player.equipment(slot).isEmpty()) {
                    worn++;
                }
            }
            visibility *= INVISIBLE_VISIBILITY * Math.max((float) worn / ARMOR_SLOTS.length, MIN_ARMOR_COVER);
        }
        final TypedKey<Item> disguise = mob.disguiseHead();
        if (disguise != null) {
            final ItemStack head = player.equipment(EquipmentSlot.HEAD);
            if (!head.isEmpty() && head.id().equals(disguise)) {
                visibility *= DISGUISED_VISIBILITY;
            }
        }
        return visibility;
    }
}
