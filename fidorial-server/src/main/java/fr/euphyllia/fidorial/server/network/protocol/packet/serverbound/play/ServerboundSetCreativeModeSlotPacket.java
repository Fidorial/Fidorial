package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.ItemStackReader;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.fidorial.item.ItemStack;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

public record ServerboundSetCreativeModeSlotPacket(short slot, ItemStack stack)
        implements ServerboundPacket {

    private static final ComponentLogger LOGGER =
            ComponentLogger.logger(ServerboundSetCreativeModeSlotPacket.class);

    public static final NetworkDecoder<CodecAwarePacketBuffer, ServerboundSetCreativeModeSlotPacket> DECODER = ServerboundSetCreativeModeSlotPacket::decode;

    private static ServerboundSetCreativeModeSlotPacket decode(final CodecAwarePacketBuffer buf) {

        final short slot = buf.readShort();

        try {
            final ItemStack stack = ItemStackReader.readSlot(buf, buf.registries());
            return new ServerboundSetCreativeModeSlotPacket(slot, stack);
        } catch (final RuntimeException e) {
            LOGGER.warn("Unreadable creative slot {}, treating it as empty: {}", slot, e.getMessage());
            return new ServerboundSetCreativeModeSlotPacket(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleSetCreativeModeSlot(this);
    }
}
