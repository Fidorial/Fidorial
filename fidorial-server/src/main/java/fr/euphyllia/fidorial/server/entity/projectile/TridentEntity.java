package fr.euphyllia.fidorial.server.entity.projectile;

import fr.euphyllia.fidorial.server.combat.DamageSourceFactoryImpl;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.events.entity.ProjectileHitEventImpl;
import fr.euphyllia.fidorial.server.item.enchantment.EnchantmentEngine;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.ItemEntity;
import fr.fidorial.entity.projectile.Trident;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.component.ItemEnchantments;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.SoundEventKeys;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A thrown trident ({@code minecraft:trident}).
 *
 * <p>It deals the same damage whatever its speed, to the first entity it hits only, then drops.
 * With Loyalty, once it hit something it flies back to its thrower, through blocks.</p>
 */
public final class TridentEntity extends ArrowProjectileEntity implements Trident {

    private static final double TRIDENT_DAMAGE = 8.0;
    private static final double WATER_DRAG = 0.99;
    private static final int RETURN_AFTER_GROUND_TICKS = 4;
    private static final double RETURN_SPEED_PER_LEVEL = 0.05;
    private static final double RETURN_LIFT_PER_LEVEL = 0.015;
    private static final double RETURN_KEPT_SPEED = 0.95;

    private static final int MD_LOYALTY = 11;
    private static final int MD_FOIL = 12;

    private final int loyalty;
    private boolean hit;
    private boolean returning;

    public TridentEntity(final int entityId, final Location location, final @Nullable AbstractLivingEntity owner,
                         final ItemStack trident, final int loyalty) {
        super(entityId, EntityTypes.TRIDENT, location, owner, trident, trident);
        this.loyalty = Math.max(0, loyalty);
        setBaseDamage(TRIDENT_DAMAGE);
    }

    @Override
    public boolean hasHit() {
        return hit;
    }

    @Override
    public int loyalty() {
        return loyalty;
    }

    @Override
    protected DamageSource damageSource() {
        return DamageSourceFactoryImpl.INSTANCE.trident(this, owner()).withWeapon(pickupItem());
    }

    @Override
    protected double waterDrag() {
        return WATER_DRAG;
    }

    @Override
    protected Key hitSound() {
        return SoundEventKeys.ITEM_TRIDENT_HIT.key();
    }

    @Override
    protected Key blockHitSound() {
        return SoundEventKeys.ITEM_TRIDENT_HIT_GROUND.key();
    }

    @Override
    protected void whileStuck() {
        if (stuckTicks() > RETURN_AFTER_GROUND_TICKS) {
            hit = true;
        }
        if (loyalty <= 0 || pickup() != PickupRule.ANY_PLAYER) {
            super.whileStuck();
        }
    }

    @Override
    protected void belowWorld() {
        if (loyalty > 0 && livingOwner() != null) {
            hit = true;
            return;
        }
        super.belowWorld();
    }

    @Override
    protected boolean beforeMove(final ServerWorld world) {
        final AbstractLivingEntity owner = livingOwner();
        if (loyalty <= 0 || owner == null || (!hit && !isThroughBlocks())) {
            return false;
        }
        if (!canReturnTo(owner)) {
            if (pickup() == PickupRule.ANY_PLAYER) {
                final Location at = location();
                server().entities().dropItem(world, at.x(), at.y(), at.z(), pickupItem(), 0.0, 0.0, 0.0,
                        ItemEntity.DEFAULT_PICKUP_DELAY, null);
            }
            discard();
            return true;
        }
        setThroughBlocks(true);
        final Location at = location();
        final Location target = owner.location();
        final double eyeY = target.y() + (owner instanceof final ServerPlayer player ? player.eyeHeight() : 1.0);
        final double dx = target.x() - at.x();
        final double dy = eyeY - at.y();
        final double dz = target.z() - at.z();
        final double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        setLocation(at.with(at.x(), at.y() + dy * RETURN_LIFT_PER_LEVEL * loyalty, at.z(), at.yaw(), at.pitch()));
        if (length > 1.0E-6) {
            final double pull = RETURN_SPEED_PER_LEVEL * loyalty;
            setVelocity(velocityX() * RETURN_KEPT_SPEED + dx / length * pull,
                    velocityY() * RETURN_KEPT_SPEED + dy / length * pull,
                    velocityZ() * RETURN_KEPT_SPEED + dz / length * pull);
        }
        if (!returning) {
            returning = true;
            playSound(SoundEventKeys.ITEM_TRIDENT_RETURN.key(), 10.0f, 1.0f);
        }
        return false;
    }

    private static boolean canReturnTo(final AbstractLivingEntity owner) {
        if (!owner.isAlive()) {
            return false;
        }
        return !(owner instanceof final ServerPlayer player) || player.gameMode() != GameMode.SPECTATOR;
    }

    @Override
    protected boolean canBePickedUpBy(final ServerPlayer player) {
        if (isThroughBlocks()) {
            return player == livingOwner();
        }
        return super.canBePickedUpBy(player);
    }

    @Override
    protected Outcome strike(final AbstractLivingEntity victim) {
        if (hit) {
            return Outcome.PASS_THROUGH;
        }
        if (server().events().post(ProjectileHitEventImpl.entity(this, victim)).isCancelled()) {
            return Outcome.PASS_THROUGH;
        }
        final DamageSource source = damageSource();
        final AbstractLivingEntity owner = livingOwner();
        float damage = (float) baseDamage();
        if (owner != null) {
            damage = EnchantmentEngine.modifyDamage(owner, pickupItem(), victim, source, damage);
        }
        hit = true;
        if (combat().canHarm(owner, victim)) {
            combat().hurt(victim, source, damage, 0.4);
        }
        setVelocity(velocityX() * -0.01, velocityY() * -0.1, velocityZ() * -0.01);
        playSound(hitSound(), 1.0f, 1.0f);
        return Outcome.HALT;
    }

    @Override
    protected List<ClientboundSetEntityMetadataPacket.Entry> metadataEntries() {
        final List<ClientboundSetEntityMetadataPacket.Entry> entries = super.metadataEntries();
        entries.add(ClientboundSetEntityMetadataPacket.Entry.ofByte(MD_LOYALTY, loyalty));
        final ItemEnchantments enchantments = pickupItem().getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.EMPTY);
        final boolean foil = pickupItem().getOrDefault(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, !enchantments.isEmpty());
        entries.add(ClientboundSetEntityMetadataPacket.Entry.ofBoolean(MD_FOIL, foil));
        return entries;
    }
}
