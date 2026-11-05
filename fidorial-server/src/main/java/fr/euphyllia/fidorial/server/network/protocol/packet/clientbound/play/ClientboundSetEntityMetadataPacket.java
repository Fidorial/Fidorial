package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import net.kyori.adventure.key.Key;

import java.util.List;
import java.util.function.Consumer;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Entity_Metadata
public record ClientboundSetEntityMetadataPacket(int entityId, List<Entry> metadata) implements ClientboundPacket<ClientboundSetEntityMetadataPacket> {

    private static final int METADATA_END = 0xFF;

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetEntityMetadataPacket> CODEC = NetworkEncoder.member(ClientboundSetEntityMetadataPacket::encode);

    public static ClientboundSetEntityMetadataPacket of(final int entityId, final Entry... entries) {
        return new ClientboundSetEntityMetadataPacket(entityId, List.of(entries));
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.SET_ENTITY_DATA;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetEntityMetadataPacket> codec() {
        return CODEC;
    }

    private void encode(final CodecAwarePacketBuffer buf) {
        buf.writeVarInt(entityId);
        for (final Entry entry : metadata) {
            entry.write(buf);
        }
        buf.writeByte(METADATA_END);
    }

    public record Entry(int index, int typeId, Consumer<PacketBuffer> valueWriter) {
        private static final int TYPE_BYTE = 0;
        private static final int TYPE_VARINT = 1;
        private static final int TYPE_FLOAT = 3;
        private static final int TYPE_BOOLEAN = 8;

        public static Entry ofByte(final int index, final int value) {
            return new Entry(index, TYPE_BYTE, b -> b.writeByte(value & 0xFF));
        }

        public static Entry varInt(final int index, final int value) {
            return new Entry(index, TYPE_VARINT, b -> b.writeVarInt(value));
        }

        public static Entry ofFloat(final int index, final float value) {
            return new Entry(index, TYPE_FLOAT, b -> b.writeFloat(value));
        }

        public static Entry ofBoolean(final int index, final boolean value) {
            return new Entry(index, TYPE_BOOLEAN, b -> b.writeBoolean(value));
        }

        public static Entry raw(final int index, final int typeId, final Consumer<PacketBuffer> valueWriter) {
            return new Entry(index, typeId, valueWriter);
        }

        void write(final PacketBuffer buf) {
            buf.writeByte(index & 0xFF);
            buf.writeVarInt(typeId);
            valueWriter.accept(buf);
        }
    }
}
