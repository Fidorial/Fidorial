package fr.euphyllia.fidorial.server.network.listener;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.ServerConfig;
import fr.euphyllia.fidorial.server.adventure.ClickCallbackManager;
import fr.euphyllia.fidorial.server.chat.ChatSigning;
import fr.euphyllia.fidorial.server.chat.IdentifiedSignedMessage;
import fr.euphyllia.fidorial.server.chat.LastSeenMessages;
import fr.euphyllia.fidorial.server.chat.SignedChatSession;
import fr.euphyllia.fidorial.server.chat.SignedMessageChain;
import fr.euphyllia.fidorial.server.chat.SignedMessageHelper;
import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.euphyllia.fidorial.server.entity.player.InventorySlots;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.inventory.ContainerMenu;
import fr.euphyllia.fidorial.server.inventory.EnderChestMenu;
import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.network.ConnectionState;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundBlockChangedAckPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundCommandSuggestionsPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundContainerSetContentPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundEntityPositionSyncPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundGameEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundInitializeChatPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundLoginPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundPlayerAbilitiesPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundPlayerInfoRemovePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundPlayerInfoUpdatePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundPlayerPositionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundRespawnPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundRotateHeadPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket.Entry;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetHealthPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundStartConfigurationPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSwingAnimationPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSystemChatPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.utils.LocationPositionData;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.utils.PositionData;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.common.ServerboundClientInformationPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundAcceptTeleportationPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundAcknowledgeConfigurationPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundAttackPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundChatAckPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundChatCommandPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundChatPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundChatSessionUpdatePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundClientCommandPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundClientTickEndPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundCommandSuggestionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundContainerClickPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundContainerClosePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundCustomClickActionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundInteractPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundKeepAlivePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundMovePlayerPosPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundMovePlayerPosRotPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundMovePlayerRotPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundPlayerAbilitiesPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundPlayerActionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundPlayerInputPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundPlayerLoadedPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundPunchPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundResourcePackPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundSetCarriedItemPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundSetCreativeModeSlotPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundUseItemOnPacket;
import fr.euphyllia.fidorial.server.network.session.ChunkViewTracker;
import fr.euphyllia.fidorial.server.registry.RegistryHolder;
import fr.euphyllia.fidorial.server.util.annotations.NeedsToBeRevisited;
import fr.euphyllia.fidorial.server.world.ChunkGeneratorConfig;
import fr.euphyllia.fidorial.server.world.ChunkNetworkSerializer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.WorldManager;
import fr.euphyllia.fidorial.server.world.block.ChestBlocks;
import fr.euphyllia.fidorial.server.world.block.interaction.FidorialBlockInteractionContext;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.dialog.DialogResponse;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.PlayerProfile;
import fr.fidorial.entity.RespawnPoint;
import fr.fidorial.event.player.BlockBreakEvent;
import fr.fidorial.event.player.BlockPlaceEvent;
import fr.fidorial.event.player.PlayerChatEvent;
import fr.fidorial.event.player.PlayerDialogActionEvent;
import fr.fidorial.event.player.PlayerInteractBlockEvent;
import fr.fidorial.event.player.PlayerJoinEvent;
import fr.fidorial.event.player.PlayerQuitEvent;
import fr.fidorial.event.player.PlayerRespawnEvent;
import fr.fidorial.event.player.PlayerSignedChatEvent;
import fr.fidorial.inventory.EnderChestInventory;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemDefaults;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.component.SwingAnimation;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.storage.player.PlayerDataStorage;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.Location;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.interaction.InteractionHand;
import fr.fidorial.world.block.interaction.InteractionResult;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class PlayPacketHandler implements PlayPacketListener {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(PlayPacketHandler.class);

    private static final int OFFHAND_SLOT = 40;

    private final ClientConnection connection;
    private final FidorialServer server;
    private final ServerConfig config;

    private @Nullable ServerPlayer player;
    private @Nullable ChunkViewTracker chunkView;
    private @Nullable ChunkPos ticket;

    public PlayPacketHandler(final ClientConnection connection) {
        this.connection = connection;
        this.server = connection.server();
        this.config = server.config();
    }

    @Override
    public void onEnter() {
        final RegistryHolder dynamic = server.dynamicRegistries();
        if (dynamic.isEmpty()) {
            LOGGER.error("Missing dynamic registries (GeneratedRegistryData empty): unable to join the game");
            connection.close();
            return;
        }

        bindPlayer(createPlayer());
        final ServerWorld world = (ServerWorld) player.world();
        final Location spawn = player.location();

        connection.setPlayer(player);

        world.scheduler().execute(world.key(), spawn.chunk(), () -> {
            world.addEntity(player);

            sendLoginSequence();
            openChunkView(world, spawn.chunk());
            spawnPlayer(spawn);

            connection.flushPendingMessages();
            connection.startKeepAlive();
            server.addPlayerConnection(connection);
            for (final ServerPlayer other : server.players()) {
                if (other == player) continue;
                connection.send(new ClientboundPlayerInfoUpdatePacket(
                        other.profile(), other.gameMode().id(), other.ping(), other.connection().chatSession()));
                other.connection().send(new ClientboundPlayerInfoUpdatePacket(
                        player.profile(), player.gameMode().id(), player.ping(), connection.chatSession()));
            }
            server.events().post(new PlayerJoinEvent(player));
            LOGGER.info("{} logged with uuid {}", player.name(), player.uuid());
        });
    }

    @Override
    public void onDisconnect() {
        if (chunkView != null) {
            chunkView.close();
            chunkView.world().removeViewer(chunkView);
            chunkView = null;
        }
        if (ticket != null) {
            server.regionizer().removeTicket(worldId(), ticket);
            ticket = null;
        }
        if (player != null) {
            closeOpenMenu(false);
            server.events().post(new PlayerQuitEvent(player));
            schedulePlayerRemoval(player);
        }
    }

    private void schedulePlayerRemoval(final ServerPlayer leaving) {
        final World targetWorld = leaving.world();
        final ChunkPos targetChunk = leaving.chunk();

        leaving.execute(() -> {
            if (leaving.world() != targetWorld || !leaving.chunk().equals(targetChunk)) {
                // we moved since the execute call, so reschedule
                schedulePlayerRemoval(leaving);
                return;
            }

            final ServerWorld world = (ServerWorld) leaving.world();
            world.removeEntity(leaving);
            leaving.permissions().revokeAll();
            leaving.remove();
            server.entityTracker().untrack(leaving);
            for (final ServerPlayer other : server.players()) {
                if (other == leaving) continue;
                other.connection().send(new ClientboundPlayerInfoRemovePacket(leaving.uuid()));
            }
        });
    }

    private ServerPlayer createPlayer() {
        final PlayerProfile profile = connection.profile();
        if (profile == null) {
            throw new IllegalStateException(
                    "Attempt to create a player without an authenticated profile (incomplete login)");
        }
        final PlayerDataStorage.PlayerData data = loadPlayerData(profile);

        final ServerWorld defaultWorld = server.worldManager().overworld();
        final Location defaultSpawn = new Location(config.spawnX(), config.spawnY(), config.spawnZ(), 0f, 0f);

        ServerWorld world = defaultWorld;
        Location spawn = defaultSpawn;

        if (data.hasLastLocation()) {
            final ServerWorld saved = server.worldManager().world(data.world());
            if (saved != null) {
                world = saved;
                spawn = data.location();
            } else {
                LOGGER.warn("{} last played in the unloaded world {}, world spawn used instead", profile.name(), data.world());
            }
        }

        final ServerPlayer created = new ServerPlayer(
                server.entityIds().allocate(),
                profile,
                loadInventory(profile),
                loadEnderChest(profile),
                data.gameMode(),
                connection,
                world,
                spawn);
        created.setRespawnPoint(restoreRespawnPoint(profile, data));
        return created;
    }

    private @Nullable RespawnPoint restoreRespawnPoint(
            final PlayerProfile profile, final PlayerDataStorage.PlayerData data) {
        final Key worldKey = data.respawnWorld();
        final Location location = data.respawnLocation();
        if (worldKey == null || location == null) {
            return null;
        }
        final ServerWorld world = server.worldManager().world(worldKey);
        if (world == null) {
            LOGGER.warn("Respawn point of {} targets the unknown world {}, dropped", profile.name(), worldKey);
            return null;
        }
        return new RespawnPoint(world, location);
    }

    private EnderChestInventory loadEnderChest(final PlayerProfile profile) {
        try {
            return server.playerEnderChestStorage().load(profile.uuid());
        } catch (final Exception e) {
            LOGGER.error("Chargement de l'ender chest de {} impossible, conteneur vide utilise", profile.name(), e);
            return new EnderChestInventory();
        }
    }

    private PlayerInventory loadInventory(final PlayerProfile profile) {
        try {
            final PlayerInventory inventory = server.playerInventoryStorage().load(profile.uuid());
            if (!inventory.isEmpty()) {
                LOGGER.debug("Inventaire de {} recharge", profile.name());
            }
            return inventory;
        } catch (final Exception e) {
            LOGGER.error("Chargement de l'inventaire de {} impossible, inventaire vide utilise", profile.name(), e);
            return new PlayerInventory();
        }
    }

    private PlayerDataStorage.PlayerData loadPlayerData(final PlayerProfile profile) {
        final PlayerDataStorage.PlayerData defaults = new PlayerDataStorage.PlayerData(config.defaultGameMode(), null, null, null, null);
        try {
            return server.playerDataStorage().load(profile.uuid(), defaults);
        } catch (final Exception e) {
            LOGGER.error("Chargement des donnees de {} impossible, valeurs par defaut utilisees", profile.name(), e);
            return defaults;
        }
    }

    private void sendLoginSequence() {
        final int dimensionType = server.dimensionTypes().networkId(serverWorld().generator.dimensionType().key());
        final Key[] dimensions = worldManager().worlds().stream().map(ServerWorld::key).toArray(Key[]::new);
        connection.send(new ClientboundLoginPacket(
                player.entityId(),
                worldManager().levelData().hardcore,
                dimensions,
                worldId(),
                dimensionType,
                worldManager().levelData().hashedSeed(),
                config.viewDistance(),
                config.viewDistance(),
                player.gameMode().id(),
                describeGenerator(serverWorld()) instanceof ChunkGeneratorConfig.Debug,
                describeGenerator(serverWorld()) instanceof ChunkGeneratorConfig.Flat,
                server.config().onlineMode(),
                server.config().enforcesSecureChat()));
        connection.send(new ClientboundPlayerInfoUpdatePacket(
                player.profile(), player.gameMode().id(), player.ping()));
        connection.send(ClientboundPlayerAbilitiesPacket.forGameMode(player.gameMode()));
        connection.send(ClientboundSetEntityMetadataPacket.of(
                player.entityId(),
                Entry.ofByte(ServerPlayer.MD_DISPLAYED_SKIN_PARTS, connection.displayedSkinParts())));
        player.invalidatePermissions();
        connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_WAITING_FOR_CHUNKS, 0f));
        server.weatherEngine().syncTo(connection::send);
        server.dayNightEngine().syncTo(serverWorld(), connection::send);
        server.bossBarRegistry().syncTo(player);
    }

    public void openChunkView(final ServerWorld world, final ChunkPos spawnChunk) {
        this.chunkView = new ChunkViewTracker(
                connection,
                server.chunkWorker(),
                world,
                new ChunkNetworkSerializer(server.blockStateRegistry(), server.biomeRegistry()),
                connection.effectiveViewDistance()
        );
        this.ticket = spawnChunk;
        world.addViewer(chunkView);
        server.regionizer().addTicket(worldId(), ticket);
        chunkView.init(spawnChunk);
    }

    private void spawnPlayer(final Location spawn) {
        connection.send(new ClientboundPlayerPositionPacket(
                player.nextTeleportId(),
                new PositionData.PositionMoveRotationData(
                        LocationPositionData.vec3((spawn)),
                        new PositionData.Vec3D(0.0, 0.0, 0.0),
                        LocationPositionData.floatRotation(spawn))));
        connection.send(ClientboundContainerSetContentPacket.ofPlayerInventory(
                player.inventory(), 0, ItemStack.EMPTY, server.registries().frozen()));
    }

    @Override
    public void handlePlayerLoaded(final ServerboundPlayerLoadedPacket packet) {
        LOGGER.debug("{} a fini de charger le terrain", player.name());
    }

    @Override
    public void handleAcceptTeleportation(final ServerboundAcceptTeleportationPacket packet) {
        // Confirmation du client : rien a faire tant que l'anti-cheat n'existe pas.
    }

    public void enterConfiguration() {
        LOGGER.debug("Switching player {}, uuid {} from PLAY -> CONFIGURATION phase", player.name(), player.uuid());
        connection.addExemptPacket(ServerboundAcknowledgeConfigurationPacket.class);
        connection.setRejectedState(ConnectionState.PLAY);
        // vanilla removes the player fully and creates a new one, so we do the same
        onDisconnect();
        connection.pauseKeepAlive();
        connection.saveInventoryOnDisconnect().thenRunAsync(() -> {
            connection.send(new ClientboundStartConfigurationPacket(), true);
        }, connection::execute);
    }

    @Override
    public void handleAcknowledgeConfiguration(final ServerboundAcknowledgeConfigurationPacket packet) {
        connection.setState(ConnectionState.CONFIGURATION);
        connection.resetChatSession();
    }

    @Override
    public void handleKeepAlive(final ServerboundKeepAlivePacket packet) {
        connection.acknowledgeKeepAlive(packet.id());
    }

    @Override
    public void handleClientInformation(final ServerboundClientInformationPacket packet) {
        connection.setLocale(Locale.forLanguageTag(packet.language().replace('_', '-')));
        connection.setDisplayedSkinParts(packet.displayedSkinParts());
        connection.setViewDistance(packet.viewDistance());
        if (player != null) {
            player.setLocale(packet.language());
            connection.send(ClientboundSetEntityMetadataPacket.of(
                    player.entityId(),
                    Entry.ofByte(ServerPlayer.MD_DISPLAYED_SKIN_PARTS, packet.displayedSkinParts())));
        }
        if (chunkView != null) {
            chunkView.updateViewDistance(connection.effectiveViewDistance());
        }
    }

    @Override
    public void handleSetCarriedItem(final ServerboundSetCarriedItemPacket packet) {
        final int slot = packet.slot();
        if (slot < 0 || slot > 8) {
            LOGGER.debug("{} annonce un slot de hotbar invalide : {}", player.name(), slot);
            return;
        }
        player.setSelectedSlot(slot);
    }

    @Override
    public void handleSetCreativeModeSlot(final ServerboundSetCreativeModeSlotPacket packet) {
        if (player == null) {
            return;
        }
        if (player.gameMode() != GameMode.CREATIVE) {
            LOGGER.debug("{} sends a creative packet out of creative mode (ignore)", player.name());
            return;
        }
        final int slot = InventorySlots.fromWindow(packet.slot());
        if (slot == InventorySlots.INVALID || slot >= player.inventory().size()) {
            return;
        }

        final ItemStack stack = packet.stack();

        if (stack.isEmpty()) {
            player.inventory().set(slot, ItemStack.EMPTY);
            return;
        }

        final int maxCount = Math.max(1, ItemDefaults.maxStackSize(stack.id(), stack));
        player.inventory().set(slot, stack.count() > maxCount ? stack.withCount(maxCount) : stack);
    }

    @Override
    public void handleChatCommand(final ServerboundChatCommandPacket packet) {
        server.commandManager().dispatchAsync(player, packet.command());
    }

    @Override
    public void handleChat(final ServerboundChatPacket packet) {
        if (player == null) {
            return;
        }

        final String content = packet.message();
        if (content.isEmpty()) {
            return;
        }

        final SignedChatSession session = connection.chatSession();
        final SignedMessageChain chain = connection.chatChain();

        SignedMessage verified = null;

        if (session != null && chain != null) {
            final List<SignedMessage.Signature> lastSeen = connection.lastSeen().resolve(packet.acknowledged(), packet.checksum());

            if (lastSeen == null) {
                chain.markBroken();
            } else {
                final SignedMessageChain.Outcome outcome = chain.verify(
                        session, packet.salt(), Instant.ofEpochMilli(packet.timestamp()), content, lastSeen, packet.signature());

                if (outcome.verified()) {
                    verified = new IdentifiedSignedMessage(
                            player.identity(),
                            content,
                            Instant.ofEpochMilli(packet.timestamp()),
                            packet.salt(),
                            outcome.signature(),
                            null,
                            session.sessionId(),
                            outcome.index(),
                            lastSeen);
                } else if (config.enforcesSecureChat()) {
                    disconnectForChat(outcome.reason());
                    return;
                }
            }
        } else if (config.enforcesSecureChat()) {
            disconnectForChat(SignedMessageChain.Reason.MISSING_SIGNATURE);
            return;
        }

        final PlayerChatEvent event = verified != null
                ? server.events().post(new PlayerSignedChatEvent(player, verified))
                : server.events().post(new PlayerChatEvent(player, Component.text(content)));

        if (event.isCancelled()) {
            return;
        }

        if (event instanceof final PlayerSignedChatEvent signedEvent) {
            final SignedMessage decorated = SignedMessageHelper.withUnsignedContent(signedEvent.signedMessage(), signedEvent.message());
            broadcastSigned(decorated);
        } else {
            broadcastUnsigned(event.message());
        }
    }

    private void disconnectForChat(final SignedMessageChain.Reason reason) {
        connection.disconnect(Component.translatable(switch (reason) {
            case MISSING_SIGNATURE -> "multiplayer.disconnect.unsigned_chat";
            case EXPIRED_KEY -> "multiplayer.disconnect.expired_public_key";
            case INVALID_SIGNATURE, CHAIN_BROKEN -> "multiplayer.disconnect.chat_validation_failed";
            case OUT_OF_ORDER -> "multiplayer.disconnect.out_of_order_chat";
        }));
    }

    private void broadcastSigned(final SignedMessage message) {
        final ChatType.Bound chatType = ChatType.CHAT.bind(player.displayName());
        for (final ServerPlayer viewer : server.players()) {
            viewer.sendMessage(message, chatType);
        }
        final Component content = message.unsignedContent() != null
                ? message.unsignedContent()
                : Component.text(message.message());
        final Component formatted = Component.text("<" + player.name() + "> ").append(content);
        server.getConsole().sendMessage(formatted);
    }

    private void broadcastUnsigned(final Component message) {
        final Component formatted = Component.text("<" + player.name() + "> ").append(message);
        server.broadcast(new ClientboundSystemChatPacket(formatted, false));
    }

    @Override
    public void handleChatSessionUpdate(final ServerboundChatSessionUpdatePacket packet) {
        if (player == null) {
            return;
        }
        final UUID playerUuid = player.uuid();

        server.chatSigningKeys().keys().thenAccept(keys -> connection.execute(() -> {
            boolean trusted = false;
            for (final PublicKey mojangKey : keys) {
                try {
                    if (ChatSigning.verifySessionSignature(
                            mojangKey, playerUuid, packet.publicKeyExpiresAt(), packet.publicKey(), packet.keySignature())) {
                        trusted = true;
                        break;
                    }
                } catch (final GeneralSecurityException ignored) {
                    // try the next key
                }
            }

            if (!trusted) {
                LOGGER.warn("{} sent a chat session key with an invalid Mojang signature", player.name());
                if (config.enforcesSecureChat()) {
                    connection.disconnect(Component.translatable("multiplayer.disconnect.invalid_public_key_signature"));
                }
                return;
            }

            final PublicKey sessionKey;
            try {
                sessionKey = ChatSigning.decodePublicKey(packet.publicKey());
            } catch (final GeneralSecurityException e) {
                LOGGER.warn("{} sent a malformed chat session public key", player.name(), e);
                return;
            }

            connection.setChatSession(new SignedChatSession(
                    packet.sessionId(), sessionKey, packet.publicKey(), packet.keySignature(),
                    Instant.ofEpochMilli(packet.publicKeyExpiresAt())));
            LOGGER.debug("{} established chat session {}", player.name(), packet.sessionId());

            final SignedChatSession established = connection.chatSession();
            for (final ServerPlayer viewer : server.players()) {
                viewer.connection().send(new ClientboundInitializeChatPacket(playerUuid, established));
            }
        }));
    }

    @Override
    public void handleUseItemOn(final ServerboundUseItemOnPacket packet) {
        if (player == null) {
            return;
        }
        if (player.gameMode() == GameMode.SPECTATOR) {
            connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
            return;
        }

        final ServerPlayer acting = player;
        final ServerWorld world = serverWorld();
        final BlockPos clicked = packet.target();
        final ChunkPos chunkPos = ChunkPos.fromBlock(clicked.x(), clicked.z());

        world.scheduler().execute(world.key(), chunkPos, () -> {
            final BlockFace clickedFace = BlockFace.byId(packet.face());
            final ItemStack held = acting.inventory().get(acting.selectedSlot());
            final SwingAnimation interactAnimation = held.getOrDefault(DataComponentTypes.INTERACT_ANIMATION, SwingAnimation.DEFAULT);

            final FidorialBlockInteractionContext context = interactionContext(acting, world, clicked, clickedFace, packet);
            if (context == null) {
                connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
                return;
            }

            final PlayerInteractBlockEvent interactEvent = server.events().post(new PlayerInteractBlockEvent(
                    acting,
                    world,
                    clicked,
                    context.block(),
                    clickedFace,
                    context.hand(),
                    context.heldItem(),
                    packet.cursor(),
                    packet.insideBlock()));

            if (interactEvent.useInteractedBlock()) {
                InteractionResult interaction = server.blockInteractions().use(context);
                if (!interaction.handled()) {
                    interaction = server.blockUpdates().use(context);
                }
                if (interaction.handled()) {
                    if (interaction.shouldSwing()) {
                        acting.sendToTrackers(new ClientboundSwingAnimationPacket(acting.entityId(), packet.hand() == 0, interactAnimation));
                    }
                    connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
                    return;
                }
            }

            if (!interactEvent.useItemInHand()) {
                connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
                return;
            }

            final BlockPos target = clicked.relative(clickedFace);
            final BlockState state = held.isEmpty() ? null : blockToPlace(held, target, clickedFace, packet.cursor().y());

            if (state != null && server.blockUpdates().canSurvive(world, target, state)) {
                final BlockPlaceEvent event = server.events()
                        .post(new BlockPlaceEvent(acting, target, server.blockStateRegistry().networkId(state)));
                if (!event.isCancelled() && server.blockUpdates().place(world, target, state, acting, context)) {
                    if (server.blockRegistry().blockForItem(held.id()).isPresent()) {
                        // Todo : every placement should use up the item; only declared block items do for now.
                        context.consumeHeldItem();
                    }
                    acting.sendToTrackers(new ClientboundSwingAnimationPacket(acting.entityId(), packet.hand() == 0, interactAnimation));
                }
            }
            connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
        });
    }

    private @Nullable BlockState blockToPlace(
            final ItemStack held, final BlockPos target, final BlockFace clickedFace, final float cursorY) {
        final BlockState state = server.blockStateRegistry().blockForItem(held.id());
        if (state == null) {
            return null;
        }
        final ServerWorld world = serverWorld();
        final BlockPlaceContext context = new BlockPlaceContext(
                target, clickedFace, player.location(), server.blockStateRegistry().view(world), cursorY);
        return server.blockStateRegistry().placementState(state, context);
    }

    private @Nullable FidorialBlockInteractionContext interactionContext(
            final ServerPlayer acting,
            final ServerWorld world,
            final BlockPos pos,
            final BlockFace clickedFace,
            final ServerboundUseItemOnPacket packet) {
        final BlockState state;
        try {
            state = world.getBlock(pos.x(), pos.y(), pos.z());
        } catch (final IOException e) {
            LOGGER.debug("Lecture du bloc {} impossible", pos, e);
            return null;
        }

        return new FidorialBlockInteractionContext(
                server,
                world,
                acting,
                pos,
                state,
                clickedFace,
                InteractionHand.byId(packet.hand()),
                heldItem(acting, packet.hand()),
                packet.cursor(),
                packet.insideBlock());
    }

    private ItemStack heldItem(final ServerPlayer acting, final int hand) {
        final int slot = hand == 0 ? acting.selectedSlot() : OFFHAND_SLOT;
        return acting.inventory().get(slot);
    }

    private void closeOpenMenu(final boolean notifyClient) {
        final ContainerMenu menu = player.openMenu();
        if (menu == null) {
            return;
        }
        player.closeMenu(notifyClient);

        if (menu instanceof final EnderChestMenu enderChest) {
            final ServerWorld world = serverWorld();
            final BlockPos position = enderChest.position();
            server.chestViewers().close(
                    position, (closed, viewers) -> ChestBlocks.broadcastLid(server, world, closed, viewers));
            ChestBlocks.broadcastSound(server, world, position, SoundEvents.ENDER_CHEST_CLOSE);
        }
        connection.send(ClientboundContainerSetContentPacket.ofPlayerInventory(
                player.inventory(), 0, ItemStack.EMPTY, server.registries().frozen()));
    }

    @Override
    public void handleContainerClick(final ServerboundContainerClickPacket packet) {
        final ContainerMenu menu = player.openMenu();
        if (menu == null || menu.windowId() != packet.windowId()) {
            connection.send(ClientboundContainerSetContentPacket.ofPlayerInventory(
                    player.inventory(), 0, ItemStack.EMPTY, server.registries().frozen()));
            return;
        }
        menu.click(packet);
        connection.send(menu.buildSyncPacket(server.registries().frozen()));
    }

    @Override
    public void handleContainerClose(final ServerboundContainerClosePacket packet) {
        closeOpenMenu(false);
    }

    @Override
    public void handleCustomClickAction(final ServerboundCustomClickActionPacket packet) {
        if (player == null) {
            return;
        }

        if (!ClickCallbackManager.KEY.equals(packet.id())) {
            final DialogResponse response = packet.payload() instanceof final CompoundBinaryTag values
                    ? new DialogResponse(values)
                    : DialogResponse.EMPTY;
            server.events().post(new PlayerDialogActionEvent(player, packet.id(), response));
            return;
        }

        if (!(packet.payload() instanceof final CompoundBinaryTag nbt)) {
            LOGGER.debug("{} sent a non-compound click callback payload for {}: {}",
                    player.name(), packet.id(), packet.payload());
            return;
        }

        final UUID uuid;
        try {
            uuid = ClickCallbackManager.uuidFromPayload(nbt);
        } catch (final IllegalArgumentException e) {
            LOGGER.debug("{} sent an invalid click callback payload for {}", player.name(), packet.id(), e);
            return;
        }
        server.clickCallbacksManager().handleClick(player, packet.id(), uuid);
    }

    @Override
    public void handlePlayerAction(final ServerboundPlayerActionPacket packet) {
        if (player == null) {
            return;
        }
        final ServerPlayer acting = player;
        final int status = packet.status();
        final boolean breaking =
                switch (acting.gameMode()) {
                    case CREATIVE -> status == ServerboundPlayerActionPacket.START_DESTROY_BLOCK;
                    case SURVIVAL -> status == ServerboundPlayerActionPacket.START_DESTROY_BLOCK
                            || status == ServerboundPlayerActionPacket.FINISH_DESTROY_BLOCK;
                    case ADVENTURE, SPECTATOR -> false;
                };
        if (!breaking) {
            connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
            return;
        }

        final ServerWorld world = serverWorld();
        final ChunkPos chunkPos = ChunkPos.fromBlock(packet.position().x(), packet.position().z());

        world.scheduler().execute(world.key(), chunkPos, () -> {
            if (acting.gameMode() == GameMode.SURVIVAL
                    && status == ServerboundPlayerActionPacket.START_DESTROY_BLOCK
                    && !instantMine(world, packet.position())) {
                connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
                return;
            }

            final BlockBreakEvent event = server.events().post(new BlockBreakEvent(acting, packet.position()));
            if (!event.isCancelled()) {
                onBlockDestroyed(packet.position());
                server.blockUpdates().destroy(world, packet.position(), true, acting);
            }
            connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
        });
    }

    private void onBlockDestroyed(final BlockPos position) {
        final ContainerMenu menu = player.openMenu();
        if (menu instanceof final EnderChestMenu enderChest && enderChest.position().equals(position)) {
            closeOpenMenu(true);
        }
        server.chestViewers().forget(position);
    }

    @Override
    public void handleCommandSuggestion(final ServerboundCommandSuggestionPacket packet) {
        String input = packet.text();
        final boolean slash = input.startsWith("/");

        if (slash) {
            input = input.substring(1);
        }

        final int offset = slash ? 1 : 0;

        server.commandManager().offerSuggestions(player, input).thenAccept(suggestions -> {
            final var entries = suggestions.getList().stream()
                    .map(suggestion -> new ClientboundCommandSuggestionsPacket.Entry(
                            suggestion.getText(), suggestion.getTooltip()))
                    .toList();

            connection.send(new ClientboundCommandSuggestionsPacket(
                    packet.id(),
                    suggestions.getRange().getStart() + offset,
                    suggestions.getRange().getLength(),
                    entries));
        });
    }

    @Override
    public void handlePlayerAbilities(final ServerboundPlayerAbilitiesPacket packet) {
    }

    @NeedsToBeRevisited("Only blocks whose behaviour says so give way at once; this wants a real block hardness table.")
    private boolean instantMine(final ServerWorld world, final BlockPos position) {
        final ChunkColumn column = world.loadedColumn(position.chunkX(), position.chunkZ());
        if (column == null) {
            return false;
        }
        final BlockState state = column.getBlock(position.x() & 15, position.y(), position.z() & 15);
        return server.blockUpdates().breaksInstantly(state);
    }

    @Override
    public void handleMovePlayerPos(final ServerboundMovePlayerPosPacket packet) {
        final Location old = player.location();
        onMoved(packet.position().x(), packet.position().y(), packet.position().z(), old.yaw(), old.pitch(), packet.flags());
    }

    @Override
    public void handleMovePlayerPosRot(final ServerboundMovePlayerPosRotPacket packet) {
        onMoved(packet.position().x(), packet.position().y(), packet.position().z(), packet.rotation().yaw(), packet.rotation().pitch(), packet.flags());
    }

    private void onMoved(final double x, final double y, final double z, final float yaw, final float pitch, final int flags) {
        if (player == null) {
            return;
        }

        final ServerPlayer moving = player;
        final ServerWorld world = serverWorld();
        final Location previous = moving.location();
        final ChunkPos fromChunk = previous.chunk();
        final boolean wasOnGround = moving.onGround();
        final boolean isOnGround = (flags & 0x01) != 0;

        world.scheduler().execute(world.key(), fromChunk, () -> {
            final Location current = new Location(x, y, z, yaw, pitch);
            trackFall(previous, current, wasOnGround, isOnGround);
            moving.setLocation(current);
            moving.setOnGround(isOnGround);

            world.entityMoved(moving, fromChunk, current.chunk());

            moving.sendToTrackers(new ClientboundEntityPositionSyncPacket(
                    moving.entityId(),
                    new PositionData.LinearPositionPath(LocationPositionData.vec3(current)),
                    LocationPositionData.floatRotation(current),
                    moving.onGround()));
            moving.sendToTrackers(new ClientboundRotateHeadPacket(moving.entityId(), yaw));
            server.entityTracker().update(moving, server.players());

            final ChunkPos chunk = current.chunk();
            if (chunkView == null || !chunkView.moveTo(chunk.x(), chunk.z())) {
                return;
            }
            server.regionizer().moveTicket(worldId(), ticket, chunk);
            ticket = chunk;
        });
    }

    public boolean teleport(final ServerWorld target, final Location location) {
        if (player == null) {
            return false;
        }
        final ServerPlayer teleporting = player;
        final ServerWorld from = (ServerWorld) teleporting.world();
        final ChunkPos destChunk = location.chunk();

        if (from == target) {
            final Location previous = teleporting.location();
            final ChunkPos fromChunk = previous.chunk();
            return from.scheduler().execute(from.key(), fromChunk, () -> {
                teleporting.setLocation(location);
                from.entityMoved(teleporting, fromChunk, destChunk);
                connection.send(new ClientboundPlayerPositionPacket(
                        teleporting.nextTeleportId(),
                        new PositionData.PositionMoveRotationData(
                                LocationPositionData.vec3(location),
                                new PositionData.Vec3D(0.0, 0.0, 0.0),
                                LocationPositionData.floatRotation(location))));

                teleporting.sendToTrackers(new ClientboundEntityPositionSyncPacket(
                        teleporting.entityId(),
                        new PositionData.LinearPositionPath(LocationPositionData.vec3(location)),
                        LocationPositionData.floatRotation(location),
                        teleporting.onGround()));

                teleporting.sendToTrackers(new ClientboundRotateHeadPacket(teleporting.entityId(), location.yaw()));

                if (chunkView != null && chunkView.moveTo(destChunk.x(), destChunk.z()) && ticket != null) {
                    server.regionizer().moveTicket(from.dimension().id(), ticket, destChunk);
                    ticket = destChunk;
                }
                server.entityTracker().update(teleporting, server.players());
            });
        }

        final CrossWorldTeleport attempt = teleportCrossWorld(from, target, location, destChunk);
        attempt.arrival().thenAccept(succeeded -> {
            if (!succeeded) {
                LOGGER.warn("{} did not fully complete a cross-world teleport into {}", teleporting.name(), target.key());
            }
        });
        return attempt.departureScheduled();
    }

    private record CrossWorldTeleport(boolean departureScheduled, CompletableFuture<Boolean> arrival) {
    }

    private CrossWorldTeleport teleportCrossWorld(final ServerWorld from, final ServerWorld target, final Location location, final ChunkPos destChunk) {
        if (player == null) {
            throw new RuntimeException("Attempt to teleport a player who does not exist!");
        }

        final ServerPlayer teleporting = player;
        final ChunkPos fromChunk = teleporting.chunk();
        final CompletableFuture<Boolean> arrival = new CompletableFuture<>();

        final boolean departureScheduled = from.scheduler().execute(from.key(), fromChunk, () -> {
            if (chunkView != null) {
                chunkView.close();
                from.removeViewer(chunkView);
                chunkView = null;
            }
            if (ticket != null) {
                server.regionizer().removeTicket(from.dimension().id(), ticket);
                ticket = null;
            }
            from.removeEntity(teleporting);
            server.entityTracker().untrack(teleporting);

            final boolean arrivalScheduled = target.scheduler().execute(target.key(), destChunk, () -> {
                teleporting.setWorld(target);
                teleporting.setLocation(location);
                target.addEntity(teleporting);

                final int dimensionType = server.dimensionTypes().networkId(target.generator.dimensionType().key());
                connection.send(new ClientboundRespawnPacket(
                        target.dimension().id(),
                        dimensionType,
                        worldManager().levelData().hashedSeed(),
                        teleporting.gameMode().id(),
                        ClientboundRespawnPacket.KEEP_ALL,
                        describeGenerator(target) instanceof ChunkGeneratorConfig.Debug,
                        describeGenerator(target) instanceof ChunkGeneratorConfig.Flat));
                connection.send(ClientboundPlayerAbilitiesPacket.forGameMode(teleporting.gameMode()));
                connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_WAITING_FOR_CHUNKS, 0f));
                openChunkView(target, destChunk);
                connection.send(new ClientboundPlayerPositionPacket(
                        teleporting.nextTeleportId(),
                        new PositionData.PositionMoveRotationData(
                                LocationPositionData.vec3(location),
                                new PositionData.Vec3D(0.0, 0.0, 0.0),
                                LocationPositionData.floatRotation(location))));
                server.dayNightEngine().syncTo(target, connection::send);
                server.entityTracker().update(teleporting, server.players());
                server.regionizer().addTicket(target.dimension().id(), destChunk);
                arrival.complete(true);
            });

            if (!arrivalScheduled) {
                LOGGER.warn("{} was removed from {} but could not be scheduled to arrive in {}",
                        teleporting.name(), from.key(), target.key());
                arrival.complete(false);
            }
        });

        if (!departureScheduled) {
            arrival.complete(false);
        }

        return new CrossWorldTeleport(departureScheduled, arrival);
    }

    @Override
    public void handleAttack(final ServerboundAttackPacket packet) {
        if (player == null || player.isDead()) {
            return;
        }
        final AbstractEntity target = serverWorld().entityManager().byId(packet.entityId());
        if (target == null) {
            LOGGER.debug("{} is attacking the entity {} which does not exist or no longer exists.", player.name(), packet.entityId());
            return;
        }
        server.combat().attack(player, target);
    }

    @Override
    public void handleInteract(final ServerboundInteractPacket packet) {
        if (player == null || player.isDead()) {
            return;
        }
        final AbstractEntity target = serverWorld().entityManager().byId(packet.entityId());
        if (!(target instanceof final AbstractMob mob) || mob.isRemoved()) {
            LOGGER.debug("{} interacts with the entity {} which does not exist or no longer exists.", player.name(), packet.entityId());
            return;
        }
        mob.onInteract(player, packet.isOffHand() ? EquipmentSlotGroup.OFF_HAND : EquipmentSlotGroup.MAIN_HAND);
    }

    @Override
    public void handlePunch(final ServerboundPunchPacket packet) {
        if (player == null) {
            return;
        }
        player.resetAttackCooldown();
        final SwingAnimation attackAnimation = player.heldItem().getOrDefault(DataComponentTypes.ATTACK_ANIMATION, SwingAnimation.DEFAULT);
        player.sendToTrackers(new ClientboundSwingAnimationPacket(player.entityId(), true, attackAnimation));
    }

    @Override
    public void handlePlayerInput(final ServerboundPlayerInputPacket packet) {
        if (player == null) {
            return;
        }
        player.setSprinting(packet.sprinting());
        player.setSneaking(packet.sneaking());
    }

    @Override
    public void handleChatAck(final ServerboundChatAckPacket packet) {
        if (packet.messageCount() < 0 || packet.messageCount() > LastSeenMessages.SIZE) {
            LOGGER.warn("{} sent an implausible chat ack count: {}", player.name(), packet.messageCount());
            connection.disconnect(Component.translatable("multiplayer.disconnect.chat_validation_failed"));
        }
    }

    @Override
    public void handleClientTickEnd(final ServerboundClientTickEndPacket packet) {
        // confirmation du client
    }

    @Override
    public void handleMovePlayerRot(final ServerboundMovePlayerRotPacket packet) {
        final Location old = player.location();
        onMoved(old.x(), old.y(), old.z(), packet.rotation().yaw(), packet.rotation().pitch(), packet.flags());
    }

    @Override
    public void handleClientCommand(final ServerboundClientCommandPacket packet) {
        LOGGER.debug("{} sent client_command action={}", player == null ? "?" : player.name(), packet.action());
        if (packet.action() == ServerboundClientCommandPacket.PERFORM_RESPAWN) {
            respawn(PlayerRespawnEvent.Cause.DEATH_SCREEN);
        }
    }

    @Override
    public void handleResourcePackResponse(final ServerboundResourcePackPacket packet) {
        connection.notifyResourcePackResponse(packet.id(), packet.status());
    }

    public boolean respawn(final PlayerRespawnEvent.Cause cause) {
        if (player == null) {
            LOGGER.debug("Respawn requested without a player");
            return false;
        }
        if (player.isRemoved() || (!player.isDead() && !player.isAwaitingRespawn())) {
            LOGGER.debug("{} requested a respawn while alive (health={})", player.name(), player.health());
            return false;
        }
        final ServerWorld defaultWorld = server.worldManager().overworld(); // FIXME: dont hardcode
        final Location defaultSpawn =
                new Location(config.spawnX(), config.spawnY(), config.spawnZ(), 0f, 0f);

        ServerWorld requestedWorld = defaultWorld;
        Location requestedSpawn = defaultSpawn;
        boolean usedRespawnPoint = false;

        final RespawnPoint point = player.respawnPoint();
        if (point != null) {
            final ServerWorld target = server.worldManager().world(point.world().key());
            if (target != null) {
                requestedWorld = target;
                requestedSpawn = point.location();
                usedRespawnPoint = true;
            } else {
                LOGGER.warn(
                        "Respawn point of {} targets the unloaded world {}, world spawn used instead",
                        player.name(),
                        point.world().key());
                player.setRespawnPoint((RespawnPoint) null);
            }
        }

        final PlayerRespawnEvent event = server.events()
                .post(new PlayerRespawnEvent(player, requestedWorld, requestedSpawn, cause, usedRespawnPoint));
        final ServerWorld world =
                event.world() instanceof final ServerWorld target ? target : defaultWorld;
        final Location spawn = event.location();

        player.resetOnRespawn();

        final int dimensionType = server.dimensionTypes().networkId(world.generator.dimensionType().key());
        connection.send(new ClientboundRespawnPacket(
                world.dimension().id(),
                dimensionType,
                worldManager().levelData().hashedSeed(),
                player.gameMode().id(),
                ClientboundRespawnPacket.KEEP_NOTHING,
                describeGenerator(world) instanceof ChunkGeneratorConfig.Debug,
                describeGenerator(world) instanceof ChunkGeneratorConfig.Flat));
        connection.send(ClientboundPlayerAbilitiesPacket.forGameMode(player.gameMode()));
        connection.send(new ClientboundSetHealthPacket(player.health(), 20, 5.0f));
        connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_WAITING_FOR_CHUNKS, 0f));

        moveToRespawnPoint(world, spawn);

        connection.send(new ClientboundPlayerPositionPacket(
                player.nextTeleportId(),
                new PositionData.PositionMoveRotationData(
                        LocationPositionData.vec3(spawn),
                        new PositionData.Vec3D(0.0, 0.0, 0.0),
                        LocationPositionData.floatRotation(spawn))));
        server.dayNightEngine().syncTo(world, connection::send);
        server.entityTracker().update(player, server.players());
        LOGGER.debug("{} respawned at {}", player.name(), spawn);
        return true;
    }


    private void moveToRespawnPoint(final ServerWorld world, final Location spawn) {
        final ServerPlayer respawning = player;
        final ServerWorld from = (ServerWorld) respawning.world();
        final ChunkPos destination = spawn.chunk();

        if (from == world) {
            final Location previous = respawning.location();
            final ChunkPos fromChunk = previous.chunk();

            from.scheduler().execute(from.key(), fromChunk, () -> {
                respawning.setLocation(spawn);
                from.entityMoved(respawning, fromChunk, destination);
                if (chunkView != null) {
                    chunkView.resend(destination);
                }
                if (ticket != null && !ticket.equals(destination)) {
                    server.regionizer().moveTicket(from.dimension().id(), ticket, destination);
                    ticket = destination;
                }
            });
            return;
        }

        final ChunkPos fromChunk = respawning.chunk();
        from.scheduler().execute(from.key(), fromChunk, () -> {
            if (chunkView != null) {
                chunkView.close();
                from.removeViewer(chunkView);
                chunkView = null;
            }
            if (ticket != null) {
                server.regionizer().removeTicket(from.dimension().id(), ticket);
                ticket = null;
            }
            from.removeEntity(respawning);
            server.entityTracker().untrack(respawning);

            world.scheduler().execute(world.key(), destination, () -> {
                respawning.setWorld(world);
                respawning.setLocation(spawn);
                world.addEntity(respawning);
                openChunkView(world, destination);
            });
        });
    }

    private void trackFall(final Location previous, final Location current, final boolean wasOnGround, final boolean isOnGround) {
        if (wasOnGround && isOnGround) return;

        if (player.gameMode() == GameMode.CREATIVE || player.gameMode() == GameMode.SPECTATOR) {
            player.setFallDistance(0.0);
            player.setFalling(false);
            return;
        }

        if (isOnGround) {
            if (player.fallDistance() > 0.0) {
                player.landAfterFall();
            }
            player.setFalling(false);
            return;
        }

        final double dy = current.y() - previous.y();
        if (dy < 0.0) {
            player.setFallDistance(player.fallDistance() - dy);
            player.setFalling(true);
        }
    }

    public void bindPlayer(final ServerPlayer player) {
        this.player = player;
    }

    private Key worldId() {
        return player != null ? player.world().key() : server.worldManager().overworld().dimension().id();
    }

    private ServerWorld serverWorld() {
        return player != null ? server.worldManager().world(player.world().key()) : server.worldManager().overworld();
    }

    private WorldManager worldManager() {
        return server.worldManager();
    }

    private ChunkGeneratorConfig describeGenerator(final ServerWorld world) {
        return world.generator.describeForSave();
    }
}
