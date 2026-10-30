package fr.euphyllia.fidorial.server.network.listener;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.adventure.ClickCallbackManager;
import fr.euphyllia.fidorial.server.datapack.known.KnownPack;
import fr.euphyllia.fidorial.server.datapack.known.KnownPackNegotiation;
import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.network.ConnectionState;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.common.ClientboundResourcePackPushPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration.ClientboundBrandPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration.ClientboundCodeOfConductPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration.ClientboundFinishConfigurationPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration.ClientboundRegistryDataPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration.ClientboundSelectKnownPacksPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration.ClientboundUpdateEnabledFeaturesPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration.ClientboundUpdateTagsPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.ConfigurationPacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.common.ServerboundClientInformationPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.configuration.ServerboundAcceptCodeOfConductPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.configuration.ServerboundCustomClickActionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.configuration.ServerboundFinishConfigurationPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.configuration.ServerboundResourcePackPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.configuration.ServerboundSelectKnownPacksPacket;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class ConfigurationPacketHandler implements ConfigurationPacketListener {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ConfigurationPacketHandler.class);

    private static final KnownPackNegotiation KNOWN_PACKS = KnownPackNegotiation.packs(KnownPack.core());

    private final ClientConnection connection;
    private final FidorialServer server;
    private volatile boolean awaitingResourcePackResponse = false;
    private final Object codeOfConductLock = new Object();
    private boolean clientInformationReceived = false;
    private boolean codeOfConductPending = false;
    private volatile boolean awaitingCodeOfConduct = false;
    private volatile boolean awaitingKnownPacks = false;

    public ConfigurationPacketHandler(final ClientConnection connection) {
        this.connection = connection;
        this.server = connection.server();
    }

    @Override
    public void onEnter() {
        LOGGER.debug("{} enters the Configuration phase", connection.username());
        if (!server.network().protocolMap().isAvailable()) {
            LOGGER.error(
                    "Protocol table missing: unable to configure {}.",
                    connection.username());
            connection.close();
            return;
        }
        connection.send(new ClientboundBrandPacket(server.brandName()));
        if (sendResourcePackIfConfigured()) {
            awaitingResourcePackResponse = true;
        } else {
            proceedToCodeOfConduct();
        }
    }

    private void proceedToKnownPacks() {
        connection.send(new ClientboundUpdateEnabledFeaturesPacket(
                server.worlds().manager().levelData().enabledFeatures.toArray(Key[]::new)));
        awaitingKnownPacks = true;
        connection.send(new ClientboundSelectKnownPacksPacket(KNOWN_PACKS.packs()));
    }

    private void proceedToCodeOfConduct() {
        if (!server.moderation().codeOfConduct().enabled()) {
            proceedToKnownPacks();
            return;
        }
        synchronized (codeOfConductLock) {
            codeOfConductPending = true;
        }
        sendCodeOfConductIfReady();
    }

    private void sendCodeOfConductIfReady() {
        final String contents;
        synchronized (codeOfConductLock) {
            if (!codeOfConductPending || !clientInformationReceived) {
                return;
            }
            contents = server.moderation().codeOfConduct().contentFor(connection.locale());
            codeOfConductPending = false;
            if (contents != null) {
                awaitingCodeOfConduct = true;
            }
        }
        if (contents == null) {
            LOGGER.warn(
                    "No Code of Conduct available for {} ({}); skipping the screen.",
                    connection.username(),
                    connection.locale());
            proceedToKnownPacks();
            return;
        }
        LOGGER.debug("Sending the Code of Conduct to {} ({})", connection.username(), connection.locale());
        connection.send(new ClientboundCodeOfConductPacket(contents));
    }

    @Override
    public void handleAcceptCodeOfConduct(final ServerboundAcceptCodeOfConductPacket packet) {
        if (!awaitingCodeOfConduct) {
            LOGGER.debug("{} accepted a Code of Conduct that was never sent; ignored.", connection.username());
            return;
        }
        awaitingCodeOfConduct = false;
        LOGGER.info("{} acknowledged the Code of Conduct", connection.username());
        proceedToKnownPacks();
    }

    @Override
    public void handleResourcePackResponse(final ServerboundResourcePackPacket packet) {
        connection.notifyResourcePackResponse(packet.id(), packet.status());

        final boolean terminalFailure = switch (packet.status()) {
            case SUCCESSFULLY_LOADED, ACCEPTED, DOWNLOADED -> false;
            default -> true;
        };

        final ResourcePackRequest pack = server.config().resourcePack();
        if (terminalFailure && pack != null && pack.required()) {
            connection.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
            return;
        }

        if (awaitingResourcePackResponse) {
            awaitingResourcePackResponse = false;
            proceedToCodeOfConduct();
        }
    }

    @Override
    public void handleSelectKnownPacks(final ServerboundSelectKnownPacksPacket packet) {
        if (!awaitingKnownPacks) {
            LOGGER.debug("{} sent an out-of-place Known Packs response; ignored.", connection.username());
            return;
        }
        awaitingKnownPacks = false;
        LOGGER.debug("Known packs for {}: offered by server {}, returned by client {}", connection.username(), KNOWN_PACKS.packs(), packet.knownPacks());

        switch (KNOWN_PACKS.negotiate(packet.knownPacks())) {
            case KnownPackNegotiation.Result.Accepted(final Set<KnownPack> negotiated) -> {
                LOGGER.debug("Known Packs negotiated with {}: {}", connection.username(), negotiated);
                sendRegistries(negotiated);
            }
            case KnownPackNegotiation.Result.Rejected(final Set<KnownPack> empty) -> {
                LOGGER.debug("Known Packs negotiation with {} did not match; sending full registries.", connection.username());
                sendRegistries(empty);
            }
        }
        sendTags();
        /* not allowed by the vanilla client in CONFIG phase even though a packet exists for it
        if (!connection.activePostEffects().isEmpty()) {
            connection.sendPostEffects(connection.activePostEffects());
        }
         */
        connection.send(new ClientboundFinishConfigurationPacket());
    }

    /**
     * Vanilla entries are sent without data to a client that has the vanilla data pack; entries added
     * or replaced by plugins are always sent with theirs.
     */
    private void sendRegistries(final Set<KnownPack> negotiatedPacks) {
        final boolean hasKnownPacks = !negotiatedPacks.isEmpty();
        for (final ServerRegistry<?> registry : server.registries().serverRegistries().synced()) {
            connection.send(new ClientboundRegistryDataPacket(registry.name(), registry.networkEntries(hasKnownPacks)));
        }
    }

    private void sendTags() {
        connection.send(new ClientboundUpdateTagsPacket(server.registries().serverRegistries().all()));
    }

    private boolean sendResourcePackIfConfigured() {
        final ResourcePackRequest request = server.config().resourcePack();
        if (request == null) {
            return false;
        }
        for (final ResourcePackInfo pack : request.packs()) {
            connection.send(new ClientboundResourcePackPushPacket(
                    ConfigurationClientboundPackets.RESOURCE_PACK_PUSH,
                    pack.id(),
                    pack.uri().toString(),
                    pack.hash(),
                    request.required(),
                    request.prompt()));
        }
        return true;
    }

    @Override
    public void handleFinishConfiguration(final ServerboundFinishConfigurationPacket packet) {
        connection.setState(ConnectionState.PLAY);
    }

    @Override
    public void handleCustomClickAction(final ServerboundCustomClickActionPacket packet) {
        if (!(packet.payload() instanceof final CompoundBinaryTag nbt)) {
            LOGGER.debug("{} sent a non-compound click callback payload for {}: {}",
                    connection.username(), packet.id(), packet.payload());
            return;
        }
        final UUID uuid;
        try {
            uuid = ClickCallbackManager.uuidFromPayload(nbt);
        } catch (final IllegalArgumentException e) {
            LOGGER.debug("{} sent an invalid click callback payload for {}", connection.username(), packet.id(), e);
            return;
        }
        server.commands().clickCallbacks().handleClick(connection, packet.id(), uuid);
    }

    @Override
    public void handleClientInformation(final ServerboundClientInformationPacket packet) {
        connection.setLocale(Locale.forLanguageTag(packet.language().replace('_', '-')));
        connection.setDisplayedSkinParts(packet.displayedSkinParts());
        connection.setViewDistance(packet.viewDistance());
        synchronized (codeOfConductLock) {
            clientInformationReceived = true;
        }
        sendCodeOfConductIfReady();
    }
}
