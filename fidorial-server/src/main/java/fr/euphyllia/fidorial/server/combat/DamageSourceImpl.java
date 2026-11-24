package fr.euphyllia.fidorial.server.combat;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.DamageType;
import org.jspecify.annotations.Nullable;

/**
 * The server's damage source: a damage type, the entities involved, where the hit comes from and
 * the item it was dealt with.
 *
 * @param type          the {@code minecraft:damage_type} reported to the client
 * @param causingEntity the entity credited with the hit
 * @param directEntity  the entity that touched the victim
 * @param position      the point the hit comes from, {@code null} to use {@code directEntity}
 * @param weapon        the item the hit was dealt with, {@link ItemStack#EMPTY} for none
 */
public record DamageSourceImpl(
        TypedKey<DamageType> type,
        @Nullable Entity causingEntity,
        @Nullable Entity directEntity,
        @Nullable Location position,
        ItemStack weapon
) implements DamageSource {

    public DamageSourceImpl {
        Preconditions.checkArgument(type != null, "The type of a damage source must not be null");
        Preconditions.checkArgument(weapon != null, "The weapon of a damage source must not be null, use ItemStack.EMPTY");
    }

    @Override
    public boolean is(final TagKey<DamageType> tag) {
        return FidorialServer.getInstance().registries().registry(RegistryKey.DAMAGE_TYPE).isTagged(type, tag);
    }

    @Override
    public boolean isDirect() {
        return causingEntity != null && causingEntity == directEntity;
    }

    @Override
    public DamageSource withPosition(final Location position) {
        Preconditions.checkArgument(position != null, "The position must not be null");
        return new DamageSourceImpl(type, causingEntity, directEntity, position, weapon);
    }

    @Override
    public DamageSource withWeapon(final ItemStack weapon) {
        return new DamageSourceImpl(type, causingEntity, directEntity, position, weapon);
    }

    @Override
    public String toString() {
        return "DamageSource[" + type.key() + (causingEntity == null ? "" : " by " + causingEntity) + "]";
    }
}
