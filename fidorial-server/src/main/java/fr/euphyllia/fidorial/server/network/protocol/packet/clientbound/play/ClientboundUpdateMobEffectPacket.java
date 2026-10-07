package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.fidorial.entity.effect.MobEffectInstance;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Entity_Effect
public record ClientboundUpdateMobEffectPacket(int entityId, int effectId, int amplifier, int duration, int flags)
        implements ClientboundPacket {

    public static final int FLAG_AMBIENT = 0x01;
    public static final int FLAG_VISIBLE = 0x02;
    public static final int FLAG_SHOW_ICON = 0x04;
    public static final int FLAG_BLEND = 0x08;

    /**
     * @param entityId the entity the effect runs on
     * @param effectId the network ID of the {@code minecraft:mob_effect} entry
     * @param effect   the running effect
     * @return the packet showing that effect to the client
     */
    public static ClientboundUpdateMobEffectPacket of(final int entityId, final int effectId,
                                                      final MobEffectInstance effect) {
        int flags = 0;
        if (effect.ambient()) {
            flags |= FLAG_AMBIENT;
        }
        if (effect.visible()) {
            flags |= FLAG_VISIBLE;
        }
        if (effect.showIcon()) {
            flags |= FLAG_SHOW_ICON;
        }
        return new ClientboundUpdateMobEffectPacket(entityId, effectId, effect.amplifier(), effect.duration(), flags);
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.UPDATE_MOB_EFFECT;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(effectId);
        buf.writeVarInt(amplifier);
        buf.writeVarInt(duration);
        buf.writeByte(flags);
    }
}
