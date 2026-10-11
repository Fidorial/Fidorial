package fr.euphyllia.fidorial.server.codecs.container;

import com.mojang.serialization.Codec;
import fr.euphyllia.fidorial.server.VersionConstants;
import fr.euphyllia.fidorial.server.world.storage.datafixers.DataFixerType;
import fr.fidorial.inventory.EnderChestInventory;
import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.io.IOException;

public final class PlayerEnderChestCodec {

    private static final String ROOT_NAME = "";
    private static final String LIST_NAME = "EnderItems";
    private static final Codec<EnderChestInventory> CODEC = ContainerCodecs.containerCodec(EnderChestInventory::new);

    private PlayerEnderChestCodec() {
    }

    public static byte[] encode(final byte[] existing, final EnderChestInventory enderChest) throws IOException {
        final CompoundBinaryTag root = ContainerCodecs.readRoot(existing, DataFixerType.PLAYER);
        return ContainerCodecs.writeRoot(ROOT_NAME, ContainerCodecs.writeList(root, LIST_NAME, CODEC, enderChest),
                VersionConstants.DATA_VERSION);
    }

    public static EnderChestInventory decode(final byte[] payload) throws IOException {
        return ContainerCodecs.readList(ContainerCodecs.readRoot(payload, DataFixerType.PLAYER), LIST_NAME, CODEC);
    }
}
