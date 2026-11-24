package fr.euphyllia.fidorial.server.entity.projectile;

import fr.euphyllia.fidorial.server.combat.DamageSourceFactoryImpl;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.projectile.SpectralArrow;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.MobEffectKeys;
import org.jspecify.annotations.Nullable;

/**
 * A spectral arrow ({@code minecraft:spectral_arrow}): what it hurts glows for ten seconds.
 */
public final class SpectralArrowEntity extends ArrowProjectileEntity implements SpectralArrow {

    private static final int GLOWING_TICKS = 200;

    public SpectralArrowEntity(final int entityId, final Location location, final @Nullable AbstractLivingEntity owner,
                               final ItemStack weapon, final ItemStack pickupItem) {
        super(entityId, EntityTypes.SPECTRAL_ARROW, location, owner, weapon, pickupItem);
    }

    @Override
    public int glowingTicks() {
        return GLOWING_TICKS;
    }

    @Override
    protected DamageSource damageSource() {
        return DamageSourceFactoryImpl.INSTANCE.arrow(this, owner()).withWeapon(weapon());
    }

    @Override
    protected void afterHurt(final AbstractLivingEntity victim) {
        if (server().registries().effects().isEffect(MobEffectKeys.GLOWING)) {
            victim.addEffect(server().registries().effects().builder(MobEffectKeys.GLOWING)
                    .duration(GLOWING_TICKS)
                    .build());
        }
    }
}
