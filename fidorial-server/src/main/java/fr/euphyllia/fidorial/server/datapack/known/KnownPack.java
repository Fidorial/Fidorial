package fr.euphyllia.fidorial.server.datapack.known;

import fr.euphyllia.fidorial.server.VersionConstants;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkCodec;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkRecordCodec;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import net.kyori.adventure.key.Key;

import static fr.euphyllia.fidorial.server.VersionConstants.MINECRAFT_VERSION_ID;

/**
 * Represents a datapack whose presence is negotiated between server and client during configuration.
 *
 * @param identifier the key identifier of the pack, consisting of namespace and value
 * @param version the version of the pack
 * @apiNote the vanilla client only recognizes known packs under the {@linkplain Key#MINECRAFT_NAMESPACE} namespace.
 */
public record KnownPack(Key identifier, String version) {

    public static final NetworkCodec<PacketBuffer, KnownPack> NETWORK_CODEC = NetworkRecordCodec.builder(KnownPack.class, PacketBuffer.class)
            .required("identifier", KnownPack::identifier, CommonNetworkCodecs.StringLikeCodecs.SPLIT_KEY)
            .required("version", KnownPack::version, CommonNetworkCodecs.StringLikeCodecs.STRING)
            .build();

    /**
     * The default vanilla datapack; versioned by the MC version
     *
     * @return the core vanilla datapack
     */
    public static KnownPack core() {
        return minecraftVersioned(Key.key("core"));
    }

    /**
     * Creates a new known pack from an identifier, with version {@linkplain VersionConstants#MINECRAFT_VERSION_ID}.
     *
     * @param identifier the known pack's key identifier
     * @return the known pack, versioned by the current MC version
     * @apiNote the vanilla client only recognizes known packs under the {@linkplain Key#MINECRAFT_NAMESPACE} namespace.
     */
    public static KnownPack minecraftVersioned(final Key identifier) {
        return new KnownPack(identifier, MINECRAFT_VERSION_ID);
    }

    @Override
    public String toString() {
        return identifier.asString() + "@" + version;
    }
}
