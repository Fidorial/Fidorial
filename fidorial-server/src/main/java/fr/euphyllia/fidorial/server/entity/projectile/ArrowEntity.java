package fr.euphyllia.fidorial.server.entity.projectile;

import fr.euphyllia.fidorial.server.combat.DamageSourceFactoryImpl;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.projectile.Arrow;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * An arrow shot by a bow or a crossbow ({@code minecraft:arrow}).
 */
public final class ArrowEntity extends ArrowProjectileEntity implements Arrow {

    private static final int MD_COLOR = 11;
    private static final int NO_COLOR = -1;

    public ArrowEntity(final int entityId, final Location location, final @Nullable AbstractLivingEntity owner,
                       final ItemStack weapon, final ItemStack pickupItem) {
        super(entityId, EntityTypes.ARROW, location, owner, weapon, pickupItem);
    }

    @Override
    protected DamageSource damageSource() {
        return DamageSourceFactoryImpl.INSTANCE.arrow(this, owner()).withWeapon(weapon());
    }

    @Override
    protected List<ClientboundSetEntityMetadataPacket.Entry> metadataEntries() {
        final List<ClientboundSetEntityMetadataPacket.Entry> entries = super.metadataEntries();
        entries.add(ClientboundSetEntityMetadataPacket.Entry.varInt(MD_COLOR, NO_COLOR));
        return entries;
    }
}
