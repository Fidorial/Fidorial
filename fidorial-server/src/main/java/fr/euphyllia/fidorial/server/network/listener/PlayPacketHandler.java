package fr.euphyllia.fidorial.server.network.listener;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.adventure.ClickCallbackManager;
import fr.euphyllia.fidorial.server.chat.ChatSigning;
import fr.euphyllia.fidorial.server.chat.IdentifiedSignedMessage;
import fr.euphyllia.fidorial.server.chat.LastSeenMessages;
import fr.euphyllia.fidorial.server.chat.SignedChatSession;
import fr.euphyllia.fidorial.server.chat.SignedMessageChain;
import fr.euphyllia.fidorial.server.chat.SignedMessageHelper;
import fr.euphyllia.fidorial.server.configuration.ServerConfiguration;
import fr.euphyllia.fidorial.server.debug.ChunkDebugValues;
import fr.euphyllia.fidorial.server.debug.DebugChannels;
import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.inventory.ContainerMenu;
import fr.euphyllia.fidorial.server.inventory.CrafterMenu;
import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.network.ConnectionState;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundBlockChangedAckPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundCommandSuggestionsPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundEntityPositionSyncPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundGameEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundGameRuleValuesPacket;
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
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetHeldSlotPacket;
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
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundContainerSlotStateChangedPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundCustomClickActionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundDebugSubscriptionRequestPacket;
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
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundSetGameRulePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundUseItemOnPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundUseItemPacket;
import fr.euphyllia.fidorial.server.network.session.ChunkViewTracker;
import fr.euphyllia.fidorial.server.registry.RegistryHolder;
import fr.euphyllia.fidorial.server.world.ChunkGeneratorConfig;
import fr.euphyllia.fidorial.server.world.ChunkNetworkSerializer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.ServerWorldManager;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.gamerule.FidorialGameRules;
import fr.fidorial.dialog.DialogResponse;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.RespawnPoint;
import fr.fidorial.entity.player.PlayerInput;
import fr.fidorial.entity.player.profile.PlayerProfile;
import fr.fidorial.event.block.BlockBreakEvent;
import fr.fidorial.event.block.BlockPlaceEvent;
import fr.fidorial.event.player.PlayerChatEvent;
import fr.fidorial.event.player.PlayerDialogActionEvent;
import fr.fidorial.event.player.PlayerDropItemEvent;
import fr.fidorial.event.player.PlayerInputEvent;
import fr.fidorial.event.player.PlayerItemHeldEvent;
import fr.fidorial.event.player.PlayerJoinEvent;
import fr.fidorial.event.player.PlayerMoveEvent;
import fr.fidorial.event.player.PlayerQuitEvent;
import fr.fidorial.event.player.PlayerRespawnEvent;
import fr.fidorial.event.player.PlayerSignedChatEvent;
import fr.fidorial.event.player.PlayerSwapHandItemsEvent;
import fr.fidorial.event.player.PlayerSwingEvent;
import fr.fidorial.event.player.PlayerUseItemEvent;
import fr.fidorial.event.server.GameRuleChangeEvent;
import fr.fidorial.gamerule.GameRuleDefinition;
import fr.fidorial.inventory.EnderChestInventory;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemDefaults;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.component.SwingAnimation;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.storage.player.PlayerDataStorage;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class PlayPacketHandler implements PlayPacketListener {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(PlayPacketHandler.class);

    private static final String GAMERULE_PERMISSION = "fidorial.command.gamerule";

    /**
     * How many stacks a creative player may throw out of the creative inventory in a burst.
     */
    private static final int CREATIVE_DROP_BURST = 10;
    /**
     * How long it takes to earn one more creative drop back: one per tick.
     */
    private static final long CREATIVE_DROP_REFILL_NANOS = 50_000_000L;

    private final ClientConnection connection;
    private final FidorialServer server;
    private final ServerConfiguration config;

    private @Nullable ServerPlayer player;
    private @Nullable ChunkViewTracker chunkView;
    private @Nullable ChunkPos ticket;

    private int creativeDropsLeft = CREATIVE_DROP_BURST;
    private long creativeDropsRefilledAt = System.nanoTime();

    public PlayPacketHandler(final ClientConnection connection) {
        this.connection = connection;
        this.server = connection.server();
        this.config = server.config();
    }

    @Override
    public void onEnter() {
        final RegistryHolder dynamic = server.registries().dynamic();
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
            server.players().connect(connection);
            for (final ServerPlayer other : server.players().online()) {
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
            server.workers().regionizer().removeTicket(worldId(), ticket);
            ticket = null;
        }
        if (player != null) {
            server.debug().subscribers().clear(player);
            player.doCloseContainer();
            server.events().post(new PlayerQuitEvent(player));
            schedulePlayerRemoval(player);
        }
    }

    private void schedulePlayerRemoval(final ServerPlayer leaving) {
        final World targetWorld = leaving.world();
        final ChunkPos targetChunk = leaving.chunk();

        leaving.execute(() -> {
            if (!leaving.world().equals(targetWorld) || !leaving.chunk().equals(targetChunk)) {
                // we moved since the execute call, so reschedule
                schedulePlayerRemoval(leaving);
                return;
            }

            final ServerWorld world = (ServerWorld) leaving.world();
            world.removeEntity(leaving);
            leaving.permissions().revokeAll();
            leaving.remove();
            server.entities().tracker().untrack(leaving);
            for (final ServerPlayer other : server.players().online()) {
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

        final ServerWorld defaultWorld = worldOrDisconnect();
        final Location defaultSpawn = defaultWorld.configuration().gameplay().spawn().location(defaultWorld);

        final Location spawn = data.lastLocation() != null ? data.lastLocation() : defaultSpawn;

        final ServerPlayer created = new ServerPlayer(
                server.entities().ids().allocate(),
                profile,
                loadInventory(profile),
                loadEnderChest(profile),
                data.gameMode(),
                connection,
                spawn);
        if (data.respawnLocation() != null) {
            created.setRespawnPoint(data.respawnLocation());
        }
        return created;
    }

    private EnderChestInventory loadEnderChest(final PlayerProfile profile) {
        try {
            return server.players().enderChestStorage().load(profile.uuid());
        } catch (final Exception e) {
            LOGGER.error("Chargement de l'ender chest de {} impossible, conteneur vide utilise", profile.name(), e);
            return new EnderChestInventory();
        }
    }

    private PlayerInventory loadInventory(final PlayerProfile profile) {
        try {
            final PlayerInventory inventory = server.players().inventoryStorage().load(profile.uuid());
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
        try {
            return server.players().dataStorage().load(profile.uuid());
        } catch (final Exception e) {
            LOGGER.error("Chargement des donnees de {} impossible, valeurs par defaut utilisees", profile.name(), e);
            return new PlayerDataStorage.PlayerData(worldOrDisconnect().configuration().gameplay().defaultGameMode(), null, null);
        }
    }

    private void sendLoginSequence() {
        final int dimensionType = server.registries().dimensionTypes().networkId(serverWorld().generator.dimensionType().key());
        final Key[] dimensions = worldManager().worlds().stream().map(ServerWorld::key).toArray(Key[]::new);
        connection.send(new ClientboundLoginPacket(
                player.entityId(),
                worldManager().levelData().hardcore,
                dimensions,
                worldId(),
                dimensionType,
                config.general().viewDistance(),
                config.general().viewDistance(),
                player.gameMode().id(),
                describeGenerator(serverWorld()) instanceof ChunkGeneratorConfig.Debug,
                describeGenerator(serverWorld()) instanceof ChunkGeneratorConfig.Flat,
                config.network().onlineMode(),
                config.network().enforceSecureChat(),
                serverWorld().gameRuleValues().getBoolean(GameRuleKeys.REDUCED_DEBUG_INFO),
                !serverWorld().gameRuleValues().getBoolean(GameRuleKeys.IMMEDIATE_RESPAWN),
                serverWorld().gameRuleValues().getBoolean(GameRuleKeys.LIMITED_CRAFTING)));
        connection.send(new ClientboundPlayerInfoUpdatePacket(
                player.profile(), player.gameMode().id(), player.ping()));
        connection.send(ClientboundPlayerAbilitiesPacket.forGameMode(player.gameMode()));
        connection.send(ClientboundSetEntityMetadataPacket.of(
                player.entityId(),
                Entry.ofByte(ServerPlayer.MD_DISPLAYED_SKIN_PARTS, connection.displayedSkinParts())));
        player.invalidatePermissions();
        connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_WAITING_FOR_CHUNKS, 0f));
        if (!connection.postEffects().isEmpty()) {
            connection.sendPostEffects(connection.postEffects());
        }
        server.worlds().weather().syncTo(serverWorld(), connection::send);
        server.worlds().dayNight().syncTo(serverWorld(), connection::send);
        server.worlds().bossBars().syncTo(player);
    }

    public void openChunkView(final ServerWorld world, final ChunkPos spawnChunk) {
        this.chunkView = new ChunkViewTracker(
                connection,
                server.workers().chunkWorker(),
                world,
                new ChunkNetworkSerializer(server.registries().blockStates(), server.registries().biomes()),
                connection.effectiveViewDistance()
        );
        this.ticket = spawnChunk;
        world.addViewer(chunkView);
        server.workers().regionizer().addTicket(worldId(), ticket);
        chunkView.init(spawnChunk);
    }

    private void spawnPlayer(final Location spawn) {
        connection.send(new ClientboundPlayerPositionPacket(
                player.nextTeleportId(),
                new PositionData.PositionMoveRotationData(
                        LocationPositionData.vec3((spawn)),
                        new PositionData.Vec3D(0.0, 0.0, 0.0),
                        LocationPositionData.floatRotation(spawn))));
        player.inventoryMenu().sendAllDataToRemote();
    }

    @Override
    public void handlePlayerLoaded(final ServerboundPlayerLoadedPacket packet) {
        LOGGER.debug("{} a fini de charger le terrain", player.name());
    }

    @Override
    public void handleAcceptTeleportation(final ServerboundAcceptTeleportationPacket packet) {
        // Confirmation du client : rien a faire tant que l'anti-cheat n'existe pas.
    }

    public CompletableFuture<Boolean> enterConfiguration() {
        if (player == null) {
            return CompletableFuture.completedFuture(false);
        }
        LOGGER.debug("Switching player {}, uuid {} from PLAY -> CONFIGURATION phase", player.name(), player.uuid());
        connection.addExemptPacket(ServerboundAcknowledgeConfigurationPacket.class);
        connection.setRejectedState(ConnectionState.PLAY);
        // vanilla removes the player fully and creates a new one, so we do the same
        onDisconnect();
        connection.pauseKeepAlive();
        return connection.saveInventoryOnDisconnect().thenApplyAsync(_ -> {
            connection.send(new ClientboundStartConfigurationPacket(), true);
            return connection.isActive(); // check if the client disconnected while saving
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
        if (player == null) return;
        final int slot = packet.slot();
        if (slot < 0 || slot > 8) {
            LOGGER.debug("{} announces an invalid hotbar slot: {}", player.name(), slot);
            return;
        }
        final int previous = player.selectedSlot();
        if (previous == slot) {
            return;
        }
        final PlayerItemHeldEvent event = server.events().post(new PlayerItemHeldEvent(player, previous, slot));
        if (event.isCancelled()) {
            connection.send(new ClientboundSetHeldSlotPacket(previous));
            return;
        }
        player.acceptSelectedSlot(slot);
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
        final ServerPlayer acting = player;
        if (packet.slot() < 0) {
            // A stack thrown out of the creative inventory.
            acting.execute(() -> {
                final ItemStack stack = packet.stack();
                if (!stack.isEmpty() && stack.count() <= ItemDefaults.maxStackSize(stack.id(), stack)
                        && takeCreativeDrop()) {
                    server.entities().dropFromPlayer(acting, stack);
                }
            });
            return;
        }
        acting.execute(() -> acting.inventoryMenu().setCreativeSlot(packet.slot(), packet.stack()));
    }

    /**
     * {@return whether the player may throw one more stack out of the creative inventory}, so that a
     * modified client cannot flood the world with items
     */
    private boolean takeCreativeDrop() {
        final long now = System.nanoTime();
        final long earned = (now - creativeDropsRefilledAt) / CREATIVE_DROP_REFILL_NANOS;
        if (earned > 0) {
            creativeDropsLeft = (int) Math.min(CREATIVE_DROP_BURST, creativeDropsLeft + earned);
            creativeDropsRefilledAt += earned * CREATIVE_DROP_REFILL_NANOS;
        }
        if (creativeDropsLeft <= 0) {
            return false;
        }
        creativeDropsLeft--;
        return true;
    }

    @Override
    public void handleChatCommand(final ServerboundChatCommandPacket packet) {
        server.commands().manager().dispatchAsync(player.commandSource(), packet.command());
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
                } else if (config.network().enforceSecureChat()) {
                    disconnectForChat(outcome.reason());
                    return;
                }
            }
        } else if (config.network().enforceSecureChat()) {
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
        for (final ServerPlayer viewer : server.players().online()) {
            viewer.sendMessage(message, chatType);
        }
        final Component content = message.unsignedContent() != null
                ? message.unsignedContent()
                : Component.text(message.message());
        final Component formatted = Component.text("<" + player.name() + "> ").append(content);
        server.commands().console().sendMessage(formatted);
    }

    private void broadcastUnsigned(final Component message) {
        final Component formatted = Component.text("<" + player.name() + "> ").append(message);
        server.players().broadcast(new ClientboundSystemChatPacket(formatted, false));
    }

    @Override
    public void handleChatSessionUpdate(final ServerboundChatSessionUpdatePacket packet) {
        if (player == null) {
            return;
        }
        final UUID playerUuid = player.uuid();

        server.network().chatSigningKeys().keys().thenAccept(keys -> connection.execute(() -> {
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
                if (config.network().enforceSecureChat()) {
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
            for (final ServerPlayer viewer : server.players().online()) {
                viewer.connection().send(new ClientboundInitializeChatPacket(playerUuid, established));
            }
        }));
    }

    @Override
    public void handleUseItem(final ServerboundUseItemPacket packet) {
        if (player == null) {
            return;
        }
        final ServerPlayer acting = player;
        final boolean offHand = packet.isOffHand();

        acting.execute(() -> {
            final ItemStack held = acting.inventory().get(offHand ? PlayerInventory.OFF_HAND : acting.selectedSlot());
            final PlayerUseItemEvent event = server.events().post(new PlayerUseItemEvent(acting,
                    offHand ? EquipmentSlotGroup.OFF_HAND : EquipmentSlotGroup.MAIN_HAND, held));
            if (event.isCancelled()) {
                acting.updateInventory();
                return;
            }
            server.maps().useEmptyMap(acting, offHand);
        });
        connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
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
        final BlockPosition clicked = packet.target();
        final ChunkPos chunkPos = clicked.chunk();

        world.scheduler().execute(world.key(), chunkPos, () -> {
            if (interactWithBlock(clicked)) {
                connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
                return;
            }

            if (packet.hand() == 0 && server.maps().useMapOn(acting, clicked)) {
                connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
                return;
            }

            final BlockFace clickedFace = BlockFace.byId(packet.face());
            final BlockPosition target = clicked.relative(clickedFace);
            final ItemStack held = acting.inventory().get(acting.selectedSlot());
            final BlockState state = held.isEmpty() ? null : blockToPlace(held, target, clickedFace, packet.cursorY());
            final SwingAnimation interactAnimation = held.getOrDefault(DataComponentTypes.INTERACT_ANIMATION, SwingAnimation.DEFAULT);

            if (state != null) {
                final BlockPlaceEvent event = server.events()
                        .post(new BlockPlaceEvent(acting, target, server.registries().blockStates().networkId(state)));
                if (!event.isCancelled()) {
                    server.worlds().blockEdits().set(world, target, state);
                    server.debug().gameEvents().emit(world, GameEventKeys.BLOCK_PLACE, target);
                    acting.sendToTrackers(new ClientboundSwingAnimationPacket(acting.entityId(), packet.hand() == 0, interactAnimation));
                }
            }
            connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
        });
    }

    private @Nullable BlockState blockToPlace(
            final ItemStack held, final BlockPosition target, final BlockFace clickedFace, final float cursorY) {
        final BlockState state = server.registries().blockStates().blockForItem(held.id());
        if (state == null) {
            return null;
        }
        final ServerWorld world = serverWorld();
        final BlockPlaceContext context = new BlockPlaceContext(
                target, clickedFace, player.location(), server.registries().blockStates().view(world), cursorY);
        return server.registries().blockStates().placementState(state, context);
    }

    private boolean interactWithBlock(final BlockPosition pos) {
        if (player.isSneaking() && (!player.inventory().get(player.selectedSlot()).isEmpty()
                || !player.inventory().get(PlayerInventory.OFF_HAND).isEmpty())) {
            // Like vanilla: sneaking with something in hand uses the item rather than the block.
            return false;
        }
        final BlockState state;
        try {
            state = serverWorld().getBlock(pos.blockX(), pos.blockY(), pos.blockZ());
        } catch (final IOException e) {
            LOGGER.debug("Lecture du bloc {} impossible", pos, e);
            return false;
        }
        final BlockData data = server.registries().blockStates().resolve(state);
        final BlockBehaviour behaviour = server.registries().blocks().behaviour(state.name()).orElse(null);
        return data != null && behaviour != null && behaviour.use(player, pos, data);
    }

    @Override
    public void handleContainerClick(final ServerboundContainerClickPacket packet) {
        final ServerPlayer acting = player;
        // On the player's thread, like vanilla, where the window is also ticked.
        acting.execute(() -> {
            final ContainerMenu menu = acting.containerMenu();
            if (menu.windowId() != packet.windowId()) {
                return;
            }
            if (acting.gameMode() == GameMode.SPECTATOR) {
                menu.sendAllDataToRemote();
                return;
            }
            if (!menu.stillValid()) {
                LOGGER.debug("{} clicked in a window that is no longer valid", acting.name());
                return;
            }
            menu.click(packet);
            // The client's prediction is not read (see ServerboundContainerClickPacket): it gets
            // the whole window back, as vanilla does when the state ID does not match.
            menu.sendAllDataToRemote();
        });
    }

    @Override
    public void handleContainerSlotStateChanged(final ServerboundContainerSlotStateChangedPacket packet) {
        final ServerPlayer acting = player;
        acting.execute(() -> {
            if (acting.gameMode() != GameMode.SPECTATOR && acting.containerMenu() instanceof final CrafterMenu crafter
                    && crafter.windowId() == packet.windowId()) {
                crafter.setSlotState(packet.slot(), packet.enabled());
            }
        });
    }

    @Override
    public void handleContainerClose(final ServerboundContainerClosePacket packet) {
        final ServerPlayer acting = player;
        // Like vanilla, whatever window the packet names: the client only ever has one open.
        acting.execute(acting::doCloseContainer);
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
        server.commands().clickCallbacks().handleClick(player, packet.id(), uuid);
    }

    @Override
    public void handlePlayerAction(final ServerboundPlayerActionPacket packet) {
        if (player == null) {
            return;
        }
        final ServerPlayer acting = player;
        final int status = packet.status();
        if (status == ServerboundPlayerActionPacket.SWAP_ITEM_WITH_OFFHAND) {
            acting.execute(() -> swapHands(acting));
            return;
        }
        if (status == ServerboundPlayerActionPacket.DROP_ITEM || status == ServerboundPlayerActionPacket.DROP_ALL_ITEMS) {
            acting.execute(() -> dropHeldItem(acting, status == ServerboundPlayerActionPacket.DROP_ALL_ITEMS));
            return;
        }
        final boolean breaking =
                switch (acting.gameMode()) {
                    case CREATIVE -> status == ServerboundPlayerActionPacket.START_DESTROY_BLOCK;
                    case SURVIVAL ->
                            status == ServerboundPlayerActionPacket.START_DESTROY_BLOCK && instantMine(packet.position())
                                    || status == ServerboundPlayerActionPacket.FINISH_DESTROY_BLOCK;
                    case ADVENTURE, SPECTATOR -> false;
                };
        if (!breaking) {
            connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
            return;
        }

        final ServerWorld world = serverWorld();
        final ChunkPos chunkPos = packet.position().chunk();

        world.scheduler().execute(world.key(), chunkPos, () -> {
            final BlockBreakEvent event = server.events().post(new BlockBreakEvent(acting, packet.position()));
            if (!event.isCancelled()) {
                server.worlds().blockEdits().set(world, packet.position(), BlockState.of(BlockTypeKeys.AIR.key()));
                server.debug().gameEvents().emit(world, GameEventKeys.BLOCK_DESTROY, packet.position());
            }
            connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
        });
    }

    private void swapHands(final ServerPlayer acting) {
        final PlayerInventory inventory = acting.inventory();
        final int slot = acting.selectedSlot();
        final ItemStack main = inventory.get(slot);
        final ItemStack off = inventory.get(PlayerInventory.OFF_HAND);
        if (!server.events().post(new PlayerSwapHandItemsEvent(acting, main, off)).isCancelled()) {
            inventory.set(slot, off);
            inventory.set(PlayerInventory.OFF_HAND, main);
        }
        acting.updateInventory();
    }

    private void dropHeldItem(final ServerPlayer acting, final boolean wholeStack) {
        final int slot = acting.selectedSlot();
        final ItemStack held = acting.inventory().get(slot);
        if (!held.isEmpty() && acting.gameMode() != GameMode.SPECTATOR && !acting.isDead()) {
            final PlayerDropItemEvent event = server.events().post(new PlayerDropItemEvent(acting, held, wholeStack));
            if (!event.isCancelled()) {
                final int count = wholeStack ? held.count() : 1;
                acting.inventory().set(slot, count >= held.count() ? ItemStack.EMPTY : held.withCount(held.count() - count));
                server.entities().dropFromPlayer(acting, held.withCount(count));
            }
        }
        // The client already took the item out: confirm, or put it back when the drop was refused.
        acting.updateInventory();
    }

    @Override
    public void handleCommandSuggestion(final ServerboundCommandSuggestionPacket packet) {
        String input = packet.text();
        final boolean slash = input.startsWith("/");

        if (slash) {
            input = input.substring(1);
        }

        final int offset = slash ? 1 : 0;

        server.commands().manager().offerSuggestions(player.commandSource(), input).thenAccept(suggestions -> {
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

    private boolean instantMine(final BlockPosition position) {
        return false;
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
            final Location current = Location.of(world, x, y, z, yaw, pitch);
            if (server.events().post(new PlayerMoveEvent(moving, previous, current)).isCancelled()) {
                connection.send(new ClientboundPlayerPositionPacket(
                        moving.nextTeleportId(),
                        new PositionData.PositionMoveRotationData(
                                LocationPositionData.vec3(previous),
                                new PositionData.Vec3D(0.0, 0.0, 0.0),
                                LocationPositionData.floatRotation(previous))));
                return;
            }
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
            server.entities().tracker().update(moving, server.players().online());

            final ChunkPos chunk = current.chunk();
            if (chunkView == null || !chunkView.moveTo(chunk.x(), chunk.z())) {
                return;
            }
            server.workers().regionizer().moveTicket(worldId(), ticket, chunk);
            ticket = chunk;
        });
    }

    public CompletableFuture<Boolean> teleport(final Location location) {
        if (player == null) {
            return CompletableFuture.completedFuture(false);
        }
        final ServerPlayer teleporting = player;
        final ServerWorld from = ((ServerWorld) teleporting.world());
        final ChunkPos destChunk = location.chunk();

        final ServerWorld target = ((ServerWorld) location.world());
        if (from.equals(target)) {
            final Location previous = teleporting.location();
            final ChunkPos fromChunk = previous.chunk();
            final CompletableFuture<Boolean> result = new CompletableFuture<>();
            final boolean scheduled = target.scheduler().execute(target.key(), fromChunk, () -> {
                try {
                    teleporting.setLocation(location);
                    target.entityMoved(teleporting, fromChunk, destChunk);
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
                        server.workers().regionizer().moveTicket(target.dimension().id(), ticket, destChunk);
                        ticket = destChunk;
                    }
                    server.entities().tracker().update(teleporting, server.players().online());
                    server.debug().gameEvents().emit(target, GameEventKeys.TELEPORT, location);
                    result.complete(true);
                } catch (final Exception exception) {
                    LOGGER.error("An error occurred while teleporting the player : ", exception);
                    result.complete(false);
                }
            });
            if (!scheduled) {
                result.complete(false);
            }
            return result;
        }

        final CompletableFuture<Boolean> arrival = teleportCrossWorld(from, target, location, destChunk);
        arrival.thenAccept(succeeded -> {
            if (!succeeded) {
                LOGGER.warn("{} did not fully complete a cross-world teleport into {}", teleporting.name(), target.key());
            }
        });
        return arrival;
    }

    private CompletableFuture<Boolean> teleportCrossWorld(final ServerWorld from, final ServerWorld target, final Location location, final ChunkPos destChunk) {
        if (player == null) {
            throw new RuntimeException("Attempt to teleport a player who does not exist!");
        }

        final ServerPlayer teleporting = player;
        final ChunkPos fromChunk = teleporting.chunk();
        final CompletableFuture<Boolean> arrival = new CompletableFuture<>();

        final boolean departureScheduled = from.scheduler().execute(from.key(), fromChunk, () -> {
            try {
                if (chunkView != null) {
                    chunkView.close();
                    from.removeViewer(chunkView);
                    chunkView = null;
                }
                if (ticket != null) {
                    server.workers().regionizer().removeTicket(from.dimension().id(), ticket);
                    ticket = null;
                }
                from.removeEntity(teleporting);
                server.entities().tracker().untrack(teleporting);

                final boolean arrivalScheduled = target.scheduler().execute(target.key(), destChunk, () -> {
                    try {
                        teleporting.setLocation(location);
                        target.addEntity(teleporting);

                        final int dimensionType = server.registries().dimensionTypes().networkId(target.generator.dimensionType().key());
                        connection.send(new ClientboundRespawnPacket(
                                target.dimension().id(),
                                dimensionType,
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
                        if (!connection.postEffects().isEmpty()) {
                            connection.sendPostEffects(connection.postEffects());
                        }
                        server.worlds().dayNight().syncTo(target, connection::send);
                        server.worlds().weather().syncTo(target, connection::send);
                        server.worlds().gameRules().syncTo(target, teleporting.entityId(), connection::send);
                        server.entities().tracker().update(teleporting, server.players().online());
                        server.workers().regionizer().addTicket(target.dimension().id(), destChunk);
                        server.debug().gameEvents().emit(target, GameEventKeys.TELEPORT, location);
                        arrival.complete(true);
                    } catch (final Exception exception) {
                        LOGGER.error("An error occurred while teleporting the player : ", exception);
                        arrival.complete(false);
                    }
                });

                if (!arrivalScheduled) {
                    LOGGER.warn("{} was removed from {} but could not be scheduled to arrive in {}", teleporting.name(), from.key(), target.key());
                    arrival.complete(false);
                }
            } catch (final Exception exception) {
                LOGGER.error("An error occurred while teleporting the player : ", exception);
                arrival.complete(false);
            }
        });

        if (!departureScheduled) {
            arrival.complete(false);
        }

        return arrival;
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
        server.entities().combat().attack(player, target);
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
        if (mob.onInteract(player, packet.isOffHand() ? EquipmentSlotGroup.OFF_HAND : EquipmentSlotGroup.MAIN_HAND)) {
            server.debug().gameEvents().emit(target.world(), GameEventKeys.ENTITY_INTERACT, target.location());
        }
    }

    @Override
    public void handlePunch(final ServerboundPunchPacket packet) {
        if (player == null) {
            return;
        }
        player.resetAttackCooldown();
        if (server.events().post(new PlayerSwingEvent(player)).isCancelled()) {
            return;
        }
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
        final PlayerInput input = new PlayerInput(packet.forward(), packet.backward(), packet.left(), packet.right(),
                packet.jumping(), packet.sneaking(), packet.sprinting());
        final PlayerInput previous = player.input();
        if (!previous.equals(input)) {
            player.setInput(input);
            server.events().post(new PlayerInputEvent(player, previous, input));
        }
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
    public void handleDebugSubscriptionRequest(final ServerboundDebugSubscriptionRequestPacket packet) {
        if (player == null) {
            return;
        }
        final int gained = server.debug().subscribers().update(player, packet.requested());
        final ChunkViewTracker view = chunkView;
        if ((gained & DebugChannels.STRUCTURES.bit()) != 0 && view != null) {
            final ServerWorld world = view.world();
            view.forEachSent(key -> server.workers().chunkWorker().execute(
                    () -> ChunkDebugValues.sendStructures(connection, world, (int) (key >> 32), (int) key)));
        }
    }

    @Override
    public void handleClientCommand(final ServerboundClientCommandPacket packet) {
        LOGGER.debug("{} sent client_command action={}", player == null ? "?" : player.name(), packet.action());
        if (packet.action() == ServerboundClientCommandPacket.PERFORM_RESPAWN) {
            respawn(PlayerRespawnEvent.Cause.DEATH_SCREEN);
        } else if (packet.action() == ServerboundClientCommandPacket.REQUEST_GAMERULE_VALUES) {
            sendGameRuleValues();
        }
    }

    private boolean canEditGameRules() {
        return player != null && player.hasPermission(GAMERULE_PERMISSION);
    }

    private void sendGameRuleValues() {
        if (!canEditGameRules()) {
            LOGGER.debug("{} requested the game rule values without permission", player == null ? "?" : player.name());
            return;
        }
        connection.send(new ClientboundGameRuleValuesPacket(server.worlds().gameRules().snapshot()));
    }

    @Override
    public void handleSetGameRule(final ServerboundSetGameRulePacket packet) {
        if (player == null) {
            return;
        }
        if (!canEditGameRules()) {
            LOGGER.warn("{} tried to change game rules without permission", player.name());
            return;
        }
        final FidorialGameRules rules = server.worlds().gameRules();
        for (final ServerboundSetGameRulePacket.Entry entry : packet.entries()) {
            final Optional<GameRuleDefinition> found = rules.definition(entry.rule());
            if (found.isEmpty()) {
                LOGGER.warn("{} sent an unknown game rule: {}", player.name(), entry.rule());
                continue;
            }
            final GameRuleDefinition rule = found.get();
            if (!rule.implemented()) {
                player.sendMessage(Component.translatable("commands.gamerule.unsupported", Component.text(rule.id())));
                continue;
            }
            final Integer value = rule.parse(entry.value());
            if (value == null) {
                LOGGER.warn("{} sent an invalid value for game rule {}: {}", player.name(), rule.id(), entry.value());
                continue;
            }
            switch (rules.apply(null, rule, value, GameRuleChangeEvent.Cause.GAME_RULE_SCREEN, player)) {
                case CHANGED -> {
                    final String applied = rule.format(rules.get(rule));
                    LOGGER.info("{} set game rule {} to {} from the game rule screen", player.name(), rule.id(), applied);
                    player.sendMessage(Component.translatable(
                            "commands.gamerule.set", Component.text(rule.id()), Component.text(applied)));
                }
                case CANCELLED -> player.sendMessage(
                        Component.translatable("commands.gamerule.cancelled", Component.text(rule.id())));
                case UNCHANGED -> {
                }
            }
        }
    }

    @Override
    public void handleResourcePackResponse(final ServerboundResourcePackPacket packet) {
        connection.notifyResourcePackResponse(packet.id(), packet.status());
    }

    public CompletableFuture<Boolean> respawn(final PlayerRespawnEvent.Cause cause) {
        if (player == null) {
            LOGGER.debug("Respawn requested without a player");
            return CompletableFuture.completedFuture(false);
        }
        if (player.isRemoved() || (!player.isDead() && !player.isAwaitingRespawn())) {
            LOGGER.debug("{} requested a respawn while alive (health={})", player.name(), player.health());
            return CompletableFuture.completedFuture(false);
        }
        final ServerWorld defaultWorld = worldOrDisconnect();
        final Location defaultSpawn = defaultWorld.configuration().gameplay().spawn().location(defaultWorld);

        ServerWorld requestedWorld = defaultWorld;
        Location requestedSpawn = defaultSpawn;
        boolean usedRespawnPoint = false;

        final RespawnPoint point = player.respawnPoint();
        if (point != null) {
            final ServerWorld target = server.worlds().manager().world(point.world().key());
            if (target != null) {
                requestedWorld = target;
                requestedSpawn = point.position().toLocation(point.world());
                usedRespawnPoint = true;
            } else {
                LOGGER.warn(
                        "Respawn point of {} targets the unloaded world {}, world spawn used instead",
                        player.name(),
                        point.world().key());
                connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0f));
                player.setRespawnPoint((RespawnPoint) null);
            }
        }

        final PlayerRespawnEvent event = server.events()
                .post(new PlayerRespawnEvent(player, requestedSpawn, cause, usedRespawnPoint));
        final Location spawn = event.location();
        final ServerWorld world = ((ServerWorld) spawn.world());

        player.resetOnRespawn();

        final int dimensionType = server.registries().dimensionTypes().networkId(world.generator.dimensionType().key());
        connection.send(new ClientboundRespawnPacket(
                world.dimension().id(),
                dimensionType,
                player.gameMode().id(),
                ClientboundRespawnPacket.KEEP_NOTHING,
                describeGenerator(world) instanceof ChunkGeneratorConfig.Debug,
                describeGenerator(world) instanceof ChunkGeneratorConfig.Flat));
        connection.send(ClientboundPlayerAbilitiesPacket.forGameMode(player.gameMode()));
        connection.send(new ClientboundSetHealthPacket(player.health(), 20, 5.0f));
        player.resendAttributeBases();
        connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_WAITING_FOR_CHUNKS, 0f));

        final CompletableFuture<Boolean> moved = moveToRespawnPoint(world, spawn);

        connection.send(new ClientboundPlayerPositionPacket(
                player.nextTeleportId(),
                new PositionData.PositionMoveRotationData(
                        LocationPositionData.vec3(spawn),
                        new PositionData.Vec3D(0.0, 0.0, 0.0),
                        LocationPositionData.floatRotation(spawn))));
        if (!connection.postEffects().isEmpty()) {
            connection.sendPostEffects(connection.postEffects());
        }
        server.worlds().dayNight().syncTo(world, connection::send);
        server.worlds().weather().syncTo(world, connection::send);
        server.worlds().gameRules().syncTo(world, player.entityId(), connection::send);
        server.entities().tracker().update(player, server.players().online());

        return moved.handle((succeeded, throwable) -> {
            if (throwable != null) {
                LOGGER.error("An error occurred while respawning {}", player.name(), throwable);
                return false;
            }
            if (succeeded) {
                LOGGER.debug("{} respawned at {}", player.name(), spawn);
            } else {
                LOGGER.warn("{} could not be moved to their respawn point at {}", player.name(), spawn);
            }
            return succeeded;
        });
    }

    private CompletableFuture<Boolean> moveToRespawnPoint(final ServerWorld world, final Location spawn) {
        final ServerPlayer respawning = player;
        final ServerWorld from = (ServerWorld) respawning.world();
        final ChunkPos destination = spawn.chunk();
        final CompletableFuture<Boolean> result = new CompletableFuture<>();

        if (from.equals(world)) {
            final Location previous = respawning.location();
            final ChunkPos fromChunk = previous.chunk();

            final boolean scheduled = from.scheduler().execute(from.key(), fromChunk, () -> {
                try {
                    respawning.setLocation(spawn);
                    from.entityMoved(respawning, fromChunk, destination);
                    if (chunkView != null) {
                        chunkView.resend(destination);
                    }
                    if (ticket != null && !ticket.equals(destination)) {
                        server.workers().regionizer().moveTicket(from.dimension().id(), ticket, destination);
                        ticket = destination;
                    }
                    result.complete(true);
                } catch (final Exception exception) {
                    LOGGER.error("An error occurred while moving {} to their respawn point", respawning.name(), exception);
                    result.complete(false);
                }
            });
            if (!scheduled) {
                result.complete(false);
            }
            return result;
        }

        final ChunkPos fromChunk = respawning.chunk();
        final boolean departureScheduled = from.scheduler().execute(from.key(), fromChunk, () -> {
            try {
                if (chunkView != null) {
                    chunkView.close();
                    from.removeViewer(chunkView);
                    chunkView = null;
                }
                if (ticket != null) {
                    server.workers().regionizer().removeTicket(from.dimension().id(), ticket);
                    ticket = null;
                }
                from.removeEntity(respawning);
                server.entities().tracker().untrack(respawning);

                final boolean arrivalScheduled = world.scheduler().execute(world.key(), destination, () -> {
                    try {
                        respawning.setLocation(spawn);
                        world.addEntity(respawning);
                        openChunkView(world, destination);
                        result.complete(true);
                    } catch (final Exception exception) {
                        LOGGER.error("An error occurred while moving {} to their respawn point", respawning.name(), exception);
                        result.complete(false);
                    }
                });

                if (!arrivalScheduled) {
                    LOGGER.warn("{} was removed from {} but could not be scheduled to arrive in {}", respawning.name(), from.key(), world.key());
                    result.complete(false);
                }
            } catch (final Exception exception) {
                LOGGER.error("An error occurred while moving {} to their respawn point", respawning.name(), exception);
                result.complete(false);
            }
        });

        if (!departureScheduled) {
            result.complete(false);
        }

        return result;
    }

    private void trackFall(final Location previous, final Location current, final boolean wasOnGround, final boolean isOnGround) {
        if (player == null || wasOnGround && isOnGround) return;

        if (player.gameMode() == GameMode.CREATIVE || player.gameMode() == GameMode.SPECTATOR) {
            player.setFallDistance(0.0);
            player.setFalling(false);
            return;
        }

        player.updateFallDistance(previous, current);

        if (isOnGround) {
            if (player.fallDistance() > 0.0) {
                player.landAfterFall();
            }
            player.setFalling(false);
            return;
        }

        if (current.y() < previous.y()) {
            player.setFalling(true);
        }
    }

    public void bindPlayer(final ServerPlayer player) {
        this.player = player;
    }

    private ServerWorld worldOrDisconnect() {
        return server.worlds().manager().defaultWorld().orElseThrow(this::disconnectForMissingWorld);
    }

    private RuntimeException disconnectForMissingWorld() {
        LOGGER.error("No default world is currently resolvable; disconnecting {}", connection.username());
        connection.disconnect(Component.translatable("multiplayer.disconnect.generic"));
        return new IllegalStateException("No world is currently loaded");
    }

    private Key worldId() {
        return player != null ? player.world().key() : worldOrDisconnect().key();
    }

    private ServerWorld serverWorld() {
        return player != null ? (ServerWorld) player.world() : worldOrDisconnect();
    }

    private ServerWorldManager worldManager() {
        return server.worlds().manager();
    }

    private ChunkGeneratorConfig describeGenerator(final ServerWorld world) {
        return world.generator.describeForSave();
    }
}
