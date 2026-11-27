package fr.euphyllia.fidorial.server.network.connection.listener;

import com.google.common.base.Preconditions;
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
import fr.euphyllia.fidorial.server.events.block.BlockBreakEventImpl;
import fr.euphyllia.fidorial.server.inventory.ContainerMenu;
import fr.euphyllia.fidorial.server.inventory.CrafterMenu;
import fr.euphyllia.fidorial.server.inventory.PlayerInventories;
import fr.euphyllia.fidorial.server.inventory.ServerEnderChestInventory;
import fr.euphyllia.fidorial.server.inventory.ServerPlayerInventory;
import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.connection.ConnectionState;
import fr.euphyllia.fidorial.server.network.connection.session.ChunkViewTracker;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundBlockChangedAckPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundCommandSuggestionsPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundEntityPositionSyncPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundGameEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundGameRuleValuesPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundInitializeChatPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundLoginPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundPlayerInfoRemovePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundPlayerInfoUpdatePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundPlayerPositionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundRespawnPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundRotateHeadPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket.Entry;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetHeldSlotPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundStartConfigurationPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSwingAnimationPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSystemChatPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.common.ServerboundClientInformationPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.common.ServerboundCookieResponsePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.common.ServerboundCustomClickActionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.common.ServerboundCustomPayloadPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.common.ServerboundResourcePackPacket;
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
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundDebugSubscriptionRequestPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundInteractPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundKeepAlivePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundMovePlayerPosPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundMovePlayerPosRotPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundMovePlayerRotPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundMovePlayerStatusOnlyPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundPlayerAbilitiesPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundPlayerActionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundPlayerCommandPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundPlayerInputPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundPlayerLoadedPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundPunchPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundSetCarriedItemPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundSetCreativeModeSlotPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundSetGameRulePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundUseItemOnPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundUseItemPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.LocationPositionData;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.world.BlockStateRegistry;
import fr.euphyllia.fidorial.server.world.ChunkGeneratorConfig;
import fr.euphyllia.fidorial.server.world.ChunkNetworkSerializer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.ServerWorldManager;
import fr.euphyllia.fidorial.server.world.block.BlockItems;
import fr.euphyllia.fidorial.server.world.block.ContainerBlocks;
import fr.euphyllia.fidorial.server.world.block.InfestedBlocks;
import fr.euphyllia.fidorial.server.world.block.PlayerMining;
import fr.euphyllia.fidorial.server.world.block.ServerBlockPlaceContext;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.BlockStates;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.farming.Harvests;
import fr.euphyllia.fidorial.server.world.gamerule.FidorialGameRules;
import fr.fidorial.dialog.DialogResponse;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.RespawnPoint;
import fr.fidorial.entity.player.PlayerAbilities;
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
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemDefaults;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.component.SwingAnimation;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.messaging.PluginMessenger;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.storage.player.PlayerDataStorage;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.dimension.DimensionTypeDefinition;
import fr.fidorial.world.farming.FarmingRegistry;
import fr.fidorial.world.farming.FarmingWorld;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

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
    /**
     * How long a client may keep moving without confirming a teleport before it is sent again.
     */
    private static final long TELEPORT_RESEND_NANOS = 1_000_000_000L;

    /**
     * How far beyond their block interaction range a player may mine a block, as in vanilla.
     */
    private static final double MINING_REACH_BUFFER = 1.0;
    /**
     * How much of a block a player must have mined, by the server's count, when their client says
     * it is done for the block to break at once, as in vanilla: the rest is the slack between the
     * moment the client started counting and the moment its packet came in.
     */
    private static final double FINISH_PROGRESS = 0.7;
    private static final long TICK_NANOS = 50_000_000L;
    private final ClientConnection connection;
    private final FidorialServer server;
    private final ServerConfiguration config;

    private @Nullable ServerPlayer player;
    private @Nullable ChunkViewTracker chunkView;
    private @Nullable ChunkPos ticket;

    private int creativeDropsLeft = CREATIVE_DROP_BURST;
    private long creativeDropsRefilledAt = System.nanoTime();
    private long teleportIgnoredSince;

    /**
     * The block the player is mining, from the moment the server read that they started.
     */
    private final AtomicReference<@Nullable Mining> mining = new AtomicReference<>();
    /**
     * The block the client said it had mined a little too soon, waiting for the player to have.
     */
    private final AtomicReference<@Nullable Mining> delayed = new AtomicReference<>();

    public PlayPacketHandler(final ClientConnection connection) {
        this.connection = connection;
        this.server = connection.server();
        this.config = server.config();
    }

    @Override
    public void onEnter() {
        if (server.registries().serverRegistries().synced().isEmpty()) {
            LOGGER.error("Missing dynamic registries (GeneratedRegistryData empty): unable to join the game");
            connection.close();
            return;
        }

        final ServerPlayer player = createPlayer();
        bindPlayer(player);
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
            // Before the join event, so that the proxy knows the channels of the plugins answering it.
            connection.sendChannelList(PluginMessenger.REGISTER, server.network().messaging().channels());
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
        leaving.execute(() -> {
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
        final Location defaultSpawn = defaultWorld.spawnLocation();

        final Location spawn = data.lastLocation() != null ? data.lastLocation() : defaultSpawn;

        final ServerPlayer created = new ServerPlayer(
                server.entities().ids().allocate(),
                profile,
                loadInventory(profile),
                loadEnderChest(profile),
                data.gameMode(),
                connection,
                spawn,
                data.abilities());
        if (data.respawnLocation() != null) {
            created.setRespawnPoint(data.respawnLocation());
        }
        final PlayerDataStorage.Experience experience = data.experience();
        created.restoreExperience(experience.level(), experience.progress(), experience.total());
        return created;
    }

    private EnderChestInventory loadEnderChest(final PlayerProfile profile) {
        try {
            final EnderChestInventory enderChest = new ServerEnderChestInventory();
            server.players().enderChestStorage().load(profile.uuid(), enderChest);
            return enderChest;
        } catch (final Exception e) {
            LOGGER.error("Chargement de l'ender chest de {} impossible, conteneur vide utilise", profile.name(), e);
            return new ServerEnderChestInventory();
        }
    }

    private PlayerInventory loadInventory(final PlayerProfile profile) {
        try {
            final PlayerInventory inventory = new ServerPlayerInventory();
            server.players().inventoryStorage().load(profile.uuid(), inventory);
            if (!inventory.isEmpty()) {
                LOGGER.debug("Inventaire de {} recharge", profile.name());
            }
            return inventory;
        } catch (final Exception e) {
            LOGGER.error("Chargement de l'inventaire de {} impossible, inventaire vide utilise", profile.name(), e);
            return new ServerPlayerInventory();
        }
    }

    private PlayerDataStorage.PlayerData loadPlayerData(final PlayerProfile profile) {
        try {
            return server.players().dataStorage().load(profile.uuid());
        } catch (final Exception e) {
            LOGGER.error("Chargement des donnees de {} impossible, valeurs par defaut utilisees", profile.name(), e);
            final GameMode mode = worldOrDisconnect().configuration().gameplay().defaultGameMode();
            final PlayerDataStorage.Experience emptyExperience = new PlayerDataStorage.Experience(0, 0, 0);
            return new PlayerDataStorage.PlayerData(mode, ServerPlayer.withGameModeDefaults(mode, PlayerAbilities.DEFAULT), null, null, emptyExperience);
        }
    }

    private void sendLoginSequence() {
        final ServerPlayer player = playerOrThrow();
        final TypedKey<DimensionTypeDefinition> dimensionType = serverWorld().generator.dimensionType().key();
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
                describeGenerator(serverWorld()) instanceof ChunkGeneratorConfig.Flat,
                config.network().onlineMode(),
                config.network().enforceSecureChat(),
                serverWorld().gameRuleValues().getBoolean(GameRuleKeys.REDUCED_DEBUG_INFO),
                !serverWorld().gameRuleValues().getBoolean(GameRuleKeys.IMMEDIATE_RESPAWN),
                serverWorld().gameRuleValues().getBoolean(GameRuleKeys.LIMITED_CRAFTING)));
        connection.send(new ClientboundPlayerInfoUpdatePacket(
                player.profile(), player.gameMode().id(), player.ping()));
        connection.send(player.abilitiesPacket());
        player.resendAttributeBases();
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
        server.worlds().tickRates().syncTo(connection::send);
        server.worlds().bossBars().syncTo(player);
    }

    public void openChunkView(final ServerWorld world, final ChunkPos spawnChunk) {
        this.chunkView = new ChunkViewTracker(
                connection,
                server.workers().chunkWorker(),
                world,
                new ChunkNetworkSerializer(
                        server.registries().blockStates(), server.registries().registry(RegistryKey.BIOME),
                        server.registries().registry(RegistryKey.BLOCK_ENTITY_TYPE)
                ),
                connection.effectiveViewDistance()
        );
        this.ticket = spawnChunk;
        world.addViewer(chunkView);
        server.workers().regionizer().addTicket(worldId(), ticket);
        chunkView.init(spawnChunk);
    }

    private void spawnPlayer(final Location spawn) {
        final ServerPlayer player = playerOrThrow();
        sendTeleport(player, spawn);
        player.inventoryMenu().sendAllDataToRemote();
    }

    @Override
    public void handlePlayerLoaded(final ServerboundPlayerLoadedPacket packet) {
        LOGGER.debug("{} a fini de charger le terrain", playerOrThrow().name());
    }

    @Override
    public void handleAcceptTeleportation(final ServerboundAcceptTeleportationPacket packet) {
        final ServerPlayer player = playerOrThrow();
        player.execute(() -> player.acceptTeleport(packet.teleportId()));
    }

    public CompletableFuture<Boolean> enterConfiguration() {
        final ServerPlayer player = playerOrThrow();
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
        final ServerPlayer player = playerOrThrow();
        final int slot = packet.slot();
        if (slot < 0 || slot > 8) {
            LOGGER.debug("{} announces an invalid hotbar slot: {}", player.name(), slot);
            return;
        }
        player.execute(() -> {
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
        });
    }

    @Override
    public void handleSetCreativeModeSlot(final ServerboundSetCreativeModeSlotPacket packet) {
        final ServerPlayer player = playerOrThrow();
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
        server.commands().manager().dispatchAsync(playerOrThrow().commandSource(), packet.command());
    }

    @Override
    public void handleChat(final ServerboundChatPacket packet) {
        final ServerPlayer player = playerOrThrow();

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

                if (outcome instanceof final SignedMessageChain.Outcome.Success success) {
                    verified = new IdentifiedSignedMessage(
                            player.identity(),
                            content,
                            Instant.ofEpochMilli(packet.timestamp()),
                            packet.salt(),
                            success.signature(),
                            null,
                            session.sessionId(),
                            success.index(),
                            lastSeen);
                } else if (outcome instanceof final SignedMessageChain.Outcome.Failure failure && config.network().enforceSecureChat()) {
                    disconnectForChat(failure.reason());
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
        final ServerPlayer player = playerOrThrow();
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
        final ServerPlayer player = playerOrThrow();
        final Component formatted = Component.text("<" + player.name() + "> ").append(message);
        server.players().broadcast(new ClientboundSystemChatPacket(formatted, false));
    }

    @Override
    public void handleChatSessionUpdate(final ServerboundChatSessionUpdatePacket packet) {
        final ServerPlayer player = playerOrThrow();
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

            final SignedChatSession established = new SignedChatSession(
                    packet.sessionId(), sessionKey, packet.publicKey(), packet.keySignature(),
                    Instant.ofEpochMilli(packet.publicKeyExpiresAt()));
            connection.setChatSession(established);
            LOGGER.debug("{} established chat session {}", player.name(), packet.sessionId());

            for (final ServerPlayer viewer : server.players().online()) {
                viewer.connection().send(new ClientboundInitializeChatPacket(playerUuid, established));
            }
        }));
    }

    @Override
    public void handleUseItem(final ServerboundUseItemPacket packet) {
        final ServerPlayer acting = playerOrThrow();
        final boolean offHand = packet.isOffHand();

        acting.execute(() -> {
            final ItemStack held = acting.inventory().get(offHand ? PlayerInventory.OFF_HAND : acting.selectedSlot());
            final PlayerUseItemEvent event = server.events().post(new PlayerUseItemEvent(acting,
                    offHand ? EquipmentSlotGroup.OFF_HAND : EquipmentSlotGroup.MAIN_HAND, held));
            if (event.isCancelled()) {
                acting.updateInventory();
                return;
            }
            if (server.entities().combat().items().use(acting, offHand ? EquipmentSlot.OFF_HAND : EquipmentSlot.MAIN_HAND)) {
                return;
            }
            server.maps().useEmptyMap(acting, offHand);
        });
        connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
    }

    @Override
    public void handleUseItemOn(final ServerboundUseItemOnPacket packet) {
        final ServerPlayer player = playerOrThrow();
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

            if (!acting.buildingAllowed()) {
                connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
                return;
            }

            final BlockFace clickedFace = BlockFace.byId(packet.face());
            final int slot = packet.hand() == 0 ? acting.selectedSlot() : PlayerInventory.OFF_HAND;
            final FarmingWorld farming = server.worlds().farming().world(world);
            if (farming.useItem(acting, slot, clicked, clickedFace)) {
                // a hoe tilled or a fertilizer took: the item is worn or used up
                acting.updateInventory();
                acting.sendToTrackers(new ClientboundSwingAnimationPacket(acting.entityId(), packet.hand() == 0, SwingAnimation.DEFAULT));
                connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
                return;
            }
            final ItemStack held = acting.inventory().get(slot);
            final FarmingRegistry farmingItems = server.registries().farming();
            final TypedKey<BlockType> plant = held.isEmpty() ? null : farmingItems.plant(held.id()).orElse(null);
            // a hoe or a fertilizer that found nothing to do is no block to place
            final boolean farmingTool = !held.isEmpty() && plant == null
                    && (farmingItems.isHoe(held.id()) || farmingItems.isFertilizer(held.id()));
            final BlockPosition target = held.isEmpty() || farmingTool
                    ? null : placementTarget(world, clicked, clickedFace, plant != null ? plant : BlockItems.block(held.id()));
            final BlockState state = target == null
                    ? null : blockToPlace(held, plant, farming, target, clickedFace, packet.cursorY());
            final SwingAnimation interactAnimation = held.getOrDefault(DataComponentTypes.INTERACT_ANIMATION, SwingAnimation.DEFAULT);
            final boolean survival = acting.gameMode() != GameMode.CREATIVE;
            // the client takes the item out of the stack as soon as it expects a block to be placed
            final boolean expectsPlacement = !held.isEmpty() && !farmingTool
                    && (plant != null || server.registries().blockStates().blockForItem(held.id()) != null);

            boolean placed = false;
            if (target != null && state != null) {
                final BlockPlaceEvent event = server.events()
                        .post(new BlockPlaceEvent(acting, target, server.registries().blockStates().networkId(state)));
                if (!event.isCancelled()) {
                    server.worlds().blockEdits().set(world, target, state, true);
                    // a chest or a barrel placed from a renamed item takes its name
                    ContainerBlocks.nameFromItem(world, target, held);
                    server.debug().gameEvents().emit(world, GameEventKeys.BLOCK_PLACE, target);
                    acting.sendToTrackers(new ClientboundSwingAnimationPacket(acting.entityId(), packet.hand() == 0, interactAnimation));
                    placed = true;
                }
            }
            if (placed && survival) {
                useUpPlaced(acting, slot, held);
            } else if (!placed && survival && expectsPlacement) {
                // gives the client back what it took out of the stack for nothing
                acting.updateInventory();
            }
            connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
        });
    }

    /**
     * Takes out of the stack a block was placed from what placing it used up: one item, or the
     * fluid of a bucket, which leaves the bucket empty.
     *
     * @param slot the slot of the hand the block was placed with
     * @param held what that hand held
     */
    private static void useUpPlaced(final ServerPlayer player, final int slot, final ItemStack held) {
        if (held.id().equals(ItemKeys.WATER_BUCKET) || held.id().equals(ItemKeys.LAVA_BUCKET)) {
            player.inventory().set(slot, ItemStack.of(ItemKeys.BUCKET));
            // the client does not empty the bucket by itself
            player.updateInventory();
            return;
        }
        PlayerInventories.takeOne(player.inventory(), slot);
    }

    /**
     * Works out the block an item places, {@code null} for none.
     *
     * @param plant   the block the item plants when it is a seed, {@code null} for any other item
     * @param farming the farming of the world, asked whether what is placed can stand there
     */
    private @Nullable BlockState blockToPlace(
            final ItemStack held, final @Nullable TypedKey<BlockType> plant, final FarmingWorld farming,
            final BlockPosition target, final BlockFace clickedFace, final float cursorY) {
        final ServerPlayer player = playerOrThrow();
        final BlockStateRegistry blockStates = server.registries().blockStates();
        final BlockState state = plant != null ? BlockStates.defaultStateOf(plant) : blockStates.blockForItem(held.id());
        if (state == null) {
            return null;
        }
        if (plant != null) {
            // a seed only goes where nothing stands yet, so that it never overwrites another plant
            final BlockData replaced = farming.blockAt(target);
            if (replaced == null || !(replaced.isAir() || farming.isTagged(target, BlockTypeTagKeys.REPLACEABLE))) {
                return null;
            }
        }
        final ServerWorld world = serverWorld();
        final BlockPlaceContext context = new ServerBlockPlaceContext(
                target, clickedFace, player.location(), blockStates.view(world), cursorY, player.isSneaking());
        final BlockState placed = blockStates.placementState(state, context);
        if (placed == null) {
            return null;
        }
        final BlockData data = blockStates.resolve(placed);
        return data == null || farming.canSurvive(target, data) ? placed : null;
    }

    /**
     * Works out where a block placed against a clicked face goes, like vanilla's {@code BlockPlaceContext}: into the
     * clicked block itself when it can be replaced (grass, a single snow layer...), otherwise in front of the clicked
     * face, but only when what stands there can be replaced too. A button or a hopper in front of the face is never
     * overwritten: the click places nothing, as in vanilla.
     *
     * @param placing the block that would be placed
     * @return the position to place at, {@code null} when there is none
     */
    private static @Nullable BlockPosition placementTarget(
            final ServerWorld world, final BlockPosition clicked, final BlockFace clickedFace,
            final TypedKey<BlockType> placing) {
        if (canBeReplaced(blockAt(world, clicked), placing)) {
            return clicked;
        }
        final BlockPosition inFront = clicked.relative(clickedFace);
        return canBeReplaced(blockAt(world, inFront), placing) ? inFront : null;
    }

    /**
     * {@return {@code true} if a block being placed may take the place of an existing one}
     *
     * @param existing the block standing there, {@code null} when its chunk is not loaded
     * @param placing  the block that would be placed
     */
    private static boolean canBeReplaced(final @Nullable BlockState existing, final TypedKey<BlockType> placing) {
        if (existing == null) {
            return false;
        }
        if (existing.isAir()) {
            return true;
        }
        if (!existing.isTagged(BlockTypeTagKeys.REPLACEABLE)) {
            return false;
        }
        if (existing.name().equals(BlockTypeKeys.SNOW)) {
            // like vanilla: only a single layer of snow gives way
            return "1".equals(existing.properties().get("layers"));
        }
        // like vanilla: a replaceable block does not give way to its own kind (grass on grass goes in front)
        return existing.isFluid() || !existing.name().equals(placing);
    }

    private boolean interactWithBlock(final BlockPosition pos) {
        final ServerPlayer player = playerOrThrow();
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
        final ServerPlayer acting = playerOrThrow();
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
        final ServerPlayer acting = playerOrThrow();
        acting.execute(() -> {
            if (acting.gameMode() != GameMode.SPECTATOR && acting.containerMenu() instanceof final CrafterMenu crafter
                    && crafter.windowId() == packet.windowId()) {
                crafter.setSlotState(packet.slot(), packet.enabled());
            }
        });
    }

    @Override
    public void handleContainerClose(final ServerboundContainerClosePacket packet) {
        final ServerPlayer acting = playerOrThrow();
        // Like vanilla, whatever window the packet names: the client only ever has one open.
        acting.execute(acting::doCloseContainer);
    }

    @Override
    public void handleCustomClickAction(final ServerboundCustomClickActionPacket packet) {
        final ServerPlayer player = playerOrThrow();

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
        final ServerPlayer acting = playerOrThrow();
        final int status = packet.status();
        if (status == ServerboundPlayerActionPacket.STAB) {
            acting.execute(() -> server.entities().combat().stab(acting));
            return;
        }
        if (status == ServerboundPlayerActionPacket.RELEASE_USE_ITEM) {
            acting.execute(() -> server.entities().combat().items().release(acting));
            return;
        }
        if (status == ServerboundPlayerActionPacket.CANCEL_USE_ITEM) {
            acting.execute(() -> server.entities().combat().items().cancel(acting));
            return;
        }
        if (status == ServerboundPlayerActionPacket.SWAP_ITEM_WITH_OFFHAND) {
            acting.execute(() -> swapHands(acting));
            return;
        }
        if (status == ServerboundPlayerActionPacket.DROP_ITEM || status == ServerboundPlayerActionPacket.DROP_ALL_ITEMS) {
            acting.execute(() -> dropHeldItem(acting, status == ServerboundPlayerActionPacket.DROP_ALL_ITEMS));
            return;
        }
        if (status == ServerboundPlayerActionPacket.START_DESTROY_BLOCK
                || status == ServerboundPlayerActionPacket.ABORT_DESTROY_BLOCK
                || status == ServerboundPlayerActionPacket.FINISH_DESTROY_BLOCK) {
            handleMining(acting, packet);
            return;
        }
        connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
    }

    /**
     * Mines a block, checking what the client says: a player only breaks a block within their
     * reach; in creative mode as soon as they start, in survival mode once they have been at the
     * very block long enough, with what they hold, to get through it.
     *
     * <p>The block a player starts mining is noted here, on the thread reading their packets, so
     * that the start, the abort and the end of their mining are seen in the order the client sent
     * them, wherever the blocks are. The block itself is then read and broken on the thread of the
     * region owning it.</p>
     *
     * <p>A client and the server do not start counting at the same moment, so that a client may say
     * it is done a little before the server would: like vanilla, the block breaks at once when the
     * player has mined most of it, and otherwise once they have mined all of it, a little later.</p>
     */
    private void handleMining(final ServerPlayer acting, final ServerboundPlayerActionPacket packet) {
        final int status = packet.status();
        final BlockPosition position = packet.position();
        final long now = System.nanoTime();
        final Mining session;
        if (status == ServerboundPlayerActionPacket.START_DESTROY_BLOCK) {
            session = new Mining(position, now);
            mining.set(session);
        } else if (status == ServerboundPlayerActionPacket.ABORT_DESTROY_BLOCK) {
            mining.getAndUpdate(current -> current != null && current.position().equals(position) ? null : current);
            connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
            return;
        } else {
            final Mining started = mining.getAndSet(null);
            if (started == null || !started.position().equals(position)) {
                LOGGER.debug("{} said they mined the block at {} without having started it", acting.name(), position);
                connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
                return;
            }
            session = started;
        }

        final ServerWorld world = serverWorld();
        world.scheduler().execute(world.key(), position.chunk(), () -> {
            if (status == ServerboundPlayerActionPacket.START_DESTROY_BLOCK) {
                startMining(acting, world, session);
            } else {
                finishMining(acting, world, session, now);
            }
            // the client takes back what it predicted, unless the block was broken meanwhile
            connection.send(new ClientboundBlockChangedAckPacket(packet.sequence()));
        });
    }

    /**
     * Starts mining a block: breaks it at once in creative mode, and in survival mode when the
     * player gets through it in a single tick.
     */
    private void startMining(final ServerPlayer acting, final ServerWorld world, final Mining session) {
        final BlockPosition position = session.position();
        if (!inMiningReach(acting, world, position)) {
            LOGGER.debug("{} started mining the block at {}, out of their reach", acting.name(), position);
            mining.compareAndSet(session, null);
            return;
        }
        switch (acting.gameMode()) {
            case CREATIVE -> {
                mining.compareAndSet(session, null);
                destroy(acting, world, position);
            }
            case SURVIVAL -> {
                final BlockData block = blockDataAt(world, position);
                if (block == null || block.isAir()) {
                    mining.compareAndSet(session, null);
                } else if (PlayerMining.progressPerTick(acting, block) >= 1.0f && mining.compareAndSet(session, null)) {
                    destroy(acting, world, position);
                }
            }
            case ADVENTURE, SPECTATOR -> mining.compareAndSet(session, null);
        }
    }

    /**
     * Ends mining a block in survival mode, as the client says it is done: breaks it now when the
     * player has mined most of it since they started, and once they have mined all of it otherwise.
     */
    private void finishMining(final ServerPlayer acting, final ServerWorld world, final Mining session,
                              final long finishedAt) {
        final BlockPosition position = session.position();
        if (acting.gameMode() != GameMode.SURVIVAL) {
            return;
        }
        if (!inMiningReach(acting, world, position)) {
            LOGGER.debug("{} finished mining the block at {}, out of their reach", acting.name(), position);
            return;
        }
        final BlockData block = blockDataAt(world, position);
        if (block == null || block.isAir()) {
            return;
        }
        final float progress = PlayerMining.progressPerTick(acting, block);
        final long ticks = ticksOfMining(session, finishedAt);
        if ((double) progress * ticks >= FINISH_PROGRESS) {
            destroy(acting, world, position);
            return;
        }
        final long needed = PlayerMining.ticksToMine(progress);
        if (needed == Long.MAX_VALUE) {
            LOGGER.debug("{} said they mined the block at {}, which they cannot break", acting.name(), position);
            return;
        }
        LOGGER.debug("{} said they mined the block at {} after {} ticks, it takes them {}: it breaks once they would have",
                acting.name(), position, ticks, needed);
        // like vanilla, one late block at a time: a later one takes its place
        delayed.set(session);
        world.scheduler().executeDelayed(world.key(), position.chunk(),
                () -> finishMiningLater(acting, world, session), Math.max(1L, needed - ticks));
    }

    /**
     * Breaks a block a client said it had mined a little too soon, once the player would have, when
     * they are still within reach and no other block took its place since.
     */
    private void finishMiningLater(final ServerPlayer acting, final ServerWorld world, final Mining session) {
        if (!delayed.compareAndSet(session, null) || acting.isRemoved() || acting.gameMode() != GameMode.SURVIVAL) {
            return;
        }
        final BlockPosition position = session.position();
        if (!inMiningReach(acting, world, position)) {
            return;
        }
        final BlockData block = blockDataAt(world, position);
        if (block == null || block.isAir()) {
            return;
        }
        final float progress = PlayerMining.progressPerTick(acting, block);
        if ((double) progress * ticksOfMining(session, System.nanoTime()) >= FINISH_PROGRESS) {
            destroy(acting, world, position);
        }
    }

    /**
     * {@return {@code true} if a player is close enough to a block to mine it}: in its world, within
     * their block interaction range and the margin vanilla gives
     */
    private static boolean inMiningReach(final ServerPlayer acting, final ServerWorld world, final BlockPosition position) {
        return acting.world() == world && acting.isWithinBlockInteractionRange(position, MINING_REACH_BUFFER);
    }

    /**
     * {@return how many ticks a player has been mining at {@code now}, the tick they started
     * included}: counted on the clock rather than in the ticks of a region, which slow down when it
     * lags, while the client keeps mining at its own pace
     */
    private static long ticksOfMining(final Mining session, final long now) {
        return Math.max(0L, now - session.startedAt()) / TICK_NANOS + 1L;
    }

    private @Nullable BlockData blockDataAt(final ServerWorld world, final BlockPosition position) {
        final BlockState state = blockAt(world, position);
        return state == null ? null : server.registries().blockStates().resolve(state);
    }

    /**
     * Breaks a block for a player allowed to: fires the {@link BlockBreakEvent} with what the block
     * is about to drop, drops what the listeners left, then removes the block.
     */
    private void destroy(final ServerPlayer acting, final ServerWorld world, final BlockPosition position) {
        final BlockState broken = blockAt(world, position);
        final BlockData block = broken != null ? server.registries().blockStates().resolve(broken) : null;
        if (broken == null || block == null || block.isAir()) {
            // nothing left to break: the block is already gone, or its chunk is not loaded
            return;
        }
        final boolean survival = acting.gameMode() != GameMode.CREATIVE;
        final ItemStack tool = acting.heldItem();
        final List<ItemStack> loot = survival ? Harvests.loot(world, block, tool) : List.of();
        final BlockBreakEvent event = server.events().post(new BlockBreakEventImpl(acting, world, position, block, tool, loot));
        if (!event.isCancelled()) {
            // what the block gives is dropped while it still stands
            if (survival) {
                Harvests.harvest(world, position, block, acting, tool);
            }
            Harvests.dropAll(world, position, event.drops());
            server.worlds().blockEdits().set(world, position, BlockState.withoutProperties(BlockTypeKeys.AIR), true);
            server.debug().gameEvents().emit(world, GameEventKeys.BLOCK_DESTROY, position);
            if (survival) {
                InfestedBlocks.afterBreak(world, position, broken, tool);
            }
        }
    }

    /**
     * A block a player mines in survival mode.
     *
     * @param position  where the block is, in the world of the player
     * @param startedAt when the server read that they started, on {@link System#nanoTime()}
     */
    private record Mining(BlockPosition position, long startedAt) {
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

        server.commands().manager().offerSuggestions(playerOrThrow().commandSource(), input).thenAccept(suggestions -> {
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
        final ServerPlayer player = playerOrThrow();
        player.execute(() -> player.setFlying(packet.flying()));
    }

    private static @Nullable BlockState blockAt(final ServerWorld world, final BlockPosition position) {
        final ChunkColumn column = world.loadedColumn(position.chunkX(), position.chunkZ());
        if (column == null || position.blockY() < column.minY() || position.blockY() >= column.minY() + column.height()) {
            return null;
        }
        return column.getBlock(position.blockX() & 15, position.blockY(), position.blockZ() & 15);
    }

    @Override
    public void handleMovePlayerPos(final ServerboundMovePlayerPosPacket packet) {
        onMoved(old -> old.with(packet.position().x(), packet.position().y(), packet.position().z(), old.yaw(), old.pitch()),
                packet.flags());
    }

    @Override
    public void handleMovePlayerPosRot(final ServerboundMovePlayerPosRotPacket packet) {
        onMoved(old -> old.with(packet.position().x(), packet.position().y(), packet.position().z(),
                packet.rotation().yaw(), packet.rotation().pitch()), packet.flags());
    }

    private void onMoved(final UnaryOperator<Location> destination, final int flags) {
        final ServerPlayer moving = playerOrThrow();
        final boolean isOnGround = (flags & 0x01) != 0;

        moving.execute(() -> {
            if (moving.isAwaitingTeleport()) {
                resendUnconfirmedTeleport(moving);
                return; // sent before the client saw the teleport
            }
            teleportIgnoredSince = 0L;
            final ServerWorld world = (ServerWorld) moving.world();
            final Location previous = moving.location();
            final ChunkPos fromChunk = previous.chunk();
            final boolean wasOnGround = moving.onGround();
            final Location current = destination.apply(previous);
            final float yaw = current.yaw();
            if (server.events().post(new PlayerMoveEvent(moving, previous, current)).isCancelled()) {
                sendTeleport(moving, previous);
                return;
            }
            trackFall(previous, current, wasOnGround, isOnGround);
            moving.setKnownMovement(current.x() - previous.x(), current.y() - previous.y(), current.z() - previous.z());
            moving.setLocation(current);
            moving.setOnGround(isOnGround);

            world.entityMoved(moving, fromChunk, current.chunk());
            moving.sendToTrackers(new ClientboundEntityPositionSyncPacket(
                    moving.entityId(),
                    new PositionData.LinearPositionPath(LocationPositionData.vec3(current)),
                    LocationPositionData.floatRotation(current),
                    yaw,
                    moving.onGround()));
            moving.sendToTrackers(new ClientboundRotateHeadPacket(moving.entityId(), yaw));
            server.entities().tracker().update(moving, server.players().online());

            final ChunkPos chunk = current.chunk();
            if (chunkView == null || !chunkView.moveTo(chunk.x(), chunk.z()) || ticket == null) {
                return;
            }
            server.workers().regionizer().moveTicket(worldId(), ticket, chunk);
            ticket = chunk;
        });
    }

    /**
     * Sends the pending teleport again once the client has kept moving for a while without confirming it.
     */
    private void resendUnconfirmedTeleport(final ServerPlayer moving) {
        final long now = System.nanoTime();
        if (teleportIgnoredSince == 0L) {
            teleportIgnoredSince = now;
            return;
        }
        if (now - teleportIgnoredSince < TELEPORT_RESEND_NANOS) {
            return;
        }
        teleportIgnoredSince = now;
        final Location at = moving.location();
        sendTeleport(moving, at);
    }

    public CompletableFuture<Boolean> teleport(final Location location) {
        final ServerPlayer teleporting = playerOrThrow();
        final ServerWorld target = ((ServerWorld) location.world());
        final CompletableFuture<Boolean> result = new CompletableFuture<>();

        final boolean scheduled = teleporting.execute(() -> {
            final ServerWorld from = ((ServerWorld) teleporting.world());
            if (from.equals(target)) {
                teleportWithinWorld(target, location, result);
            } else {
                teleportCrossWorld(from, target, location, result);
            }
        }, () -> result.complete(false));
        if (!scheduled) {
            result.complete(false);
        }
        return result;
    }

    private void teleportWithinWorld(final ServerWorld target, final Location location, final CompletableFuture<Boolean> result) {
        final ServerPlayer teleporting = playerOrThrow();
        final ChunkPos fromChunk = teleporting.chunk();
        final ChunkPos destChunk = location.chunk();
        try {
            teleporting.setLocation(location);
            target.entityMoved(teleporting, fromChunk, destChunk);
            sendTeleport(teleporting, location);

            teleporting.sendToTrackers(new ClientboundEntityPositionSyncPacket(
                    teleporting.entityId(),
                    new PositionData.LinearPositionPath(LocationPositionData.vec3(location)),
                    LocationPositionData.floatRotation(location),
                    location.yaw(),
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
    }

    private void teleportCrossWorld(final ServerWorld from, final ServerWorld target, final Location location, final CompletableFuture<Boolean> result) {
        final ServerPlayer teleporting = playerOrThrow();
        final ChunkPos destChunk = location.chunk();
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

                    final TypedKey<DimensionTypeDefinition> dimensionType = target.generator.dimensionType().key();
                    connection.send(new ClientboundRespawnPacket(
                            target.dimension().id(),
                            dimensionType,
                            teleporting.gameMode().id(),
                            ClientboundRespawnPacket.KEEP_ALL,
                            describeGenerator(target) instanceof ChunkGeneratorConfig.Flat));
                    connection.send(teleporting.abilitiesPacket());
                    connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_WAITING_FOR_CHUNKS, 0f));
                    openChunkView(target, destChunk);
                    sendTeleport(teleporting, location);
                    if (!connection.postEffects().isEmpty()) {
                        connection.sendPostEffects(connection.postEffects());
                    }
                    server.worlds().dayNight().syncTo(target, connection::send);
                    server.worlds().weather().syncTo(target, connection::send);
                    server.worlds().gameRules().syncTo(target, teleporting.entityId(), connection::send);
                    server.entities().tracker().update(teleporting, server.players().online());
                    server.workers().regionizer().addTicket(target.dimension().id(), destChunk);
                    server.debug().gameEvents().emit(target, GameEventKeys.TELEPORT, location);
                    result.complete(true);
                } catch (final Exception exception) {
                    LOGGER.error("An error occurred while teleporting the player : ", exception);
                    result.complete(false);
                }
            });

            if (!arrivalScheduled) {
                LOGGER.warn("{} was removed from {} but could not be scheduled to arrive in {}", teleporting.name(), from.key(), target.key());
                result.complete(false);
                return;
            }
            teleporting.setLocation(location);
        } catch (final Exception exception) {
            LOGGER.error("An error occurred while teleporting the player : ", exception);
            result.complete(false);
        }
    }

    @Override
    public void handleAttack(final ServerboundAttackPacket packet) {
        final ServerPlayer player = playerOrThrow();
        player.execute(() -> {
            if (player.isDead()) {
                return;
            }
            final AbstractEntity target = serverWorld().entityManager().byId(packet.entityId());
            if (target == null) {
                LOGGER.debug("{} is attacking the entity {} which does not exist or no longer exists.", player.name(), packet.entityId());
                return;
            }
            server.entities().combat().attack(player, target);
        });
    }

    @Override
    public void handleInteract(final ServerboundInteractPacket packet) {
        final ServerPlayer player = playerOrThrow();
        player.execute(() -> {
            if (player.isDead()) {
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
        });
    }

    @Override
    public void handlePunch(final ServerboundPunchPacket packet) {
        final ServerPlayer player = playerOrThrow();
        player.execute(() -> {
            player.resetAttackCooldown();
            if (server.events().post(new PlayerSwingEvent(player)).isCancelled()) {
                return;
            }
            final SwingAnimation attackAnimation = player.heldItem().getOrDefault(DataComponentTypes.ATTACK_ANIMATION, SwingAnimation.DEFAULT);
            player.sendToTrackers(new ClientboundSwingAnimationPacket(player.entityId(), true, attackAnimation));
        });
    }

    @Override
    public void handlePlayerInput(final ServerboundPlayerInputPacket packet) {
        final ServerPlayer player = playerOrThrow();
        player.execute(() -> {
            player.setSneaking(packet.sneaking());
            final PlayerInput input = new PlayerInput(packet.forward(), packet.backward(), packet.left(), packet.right(),
                    packet.jumping(), packet.sneaking(), packet.sprinting());
            final PlayerInput previous = player.input();
            if (!previous.equals(input)) {
                player.setInput(input);
                server.events().post(new PlayerInputEvent(player, previous, input));
            }
        });
    }

    @Override
    public void handlePlayerCommand(final ServerboundPlayerCommandPacket packet) {
        final ServerPlayer player = playerOrThrow();
        switch (packet.action()) {
            case START_SPRINTING -> player.execute(() -> player.setSprinting(true));
            case STOP_SPRINTING -> player.execute(() -> player.setSprinting(false));
            case LEAVE_BED, START_HORSE_JUMP, STOP_HORSE_JUMP, OPEN_VEHICLE_INVENTORY, START_FALL_FLYING -> {
            }
        }
    }

    @Override
    public void handleChatAck(final ServerboundChatAckPacket packet) {
        if (packet.messageCount() < 0 || packet.messageCount() > LastSeenMessages.SIZE) {
            LOGGER.warn("{} sent an implausible chat ack count: {}", playerOrThrow().name(), packet.messageCount());
            connection.disconnect(Component.translatable("multiplayer.disconnect.chat_validation_failed"));
        }
    }

    @Override
    public void handleClientTickEnd(final ServerboundClientTickEndPacket packet) {
        // confirmation du client
    }

    @Override
    public void handleMovePlayerRot(final ServerboundMovePlayerRotPacket packet) {
        onMoved(old -> old.with(old.x(), old.y(), old.z(), packet.rotation().yaw(), packet.rotation().pitch()), packet.flags());
    }

    @Override
    public void handleMovePlayerStatusOnly(final ServerboundMovePlayerStatusOnlyPacket packet) {
        onMoved(UnaryOperator.identity(), packet.flags());
    }

    @Override
    public void handleDebugSubscriptionRequest(final ServerboundDebugSubscriptionRequestPacket packet) {
        final ServerPlayer player = playerOrThrow();
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
        LOGGER.debug("{} sent client_command action={}", playerOrThrow().name(), packet.action());
        if (packet.action() == ServerboundClientCommandPacket.PERFORM_RESPAWN) {
            respawn(PlayerRespawnEvent.Cause.DEATH_SCREEN);
        } else if (packet.action() == ServerboundClientCommandPacket.REQUEST_GAMERULE_VALUES) {
            sendGameRuleValues();
        }
    }

    private boolean canEditGameRules() {
        return playerOrThrow().hasPermission(GAMERULE_PERMISSION);
    }

    private void sendGameRuleValues() {
        if (!canEditGameRules()) {
            LOGGER.debug("{} requested the game rule values without permission", playerOrThrow().name());
            return;
        }
        connection.send(new ClientboundGameRuleValuesPacket(server.worlds().gameRules().snapshot()));
    }

    @Override
    public void handleSetGameRule(final ServerboundSetGameRulePacket packet) {
        final ServerPlayer player = playerOrThrow();
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
    public void handleCookieResponse(final ServerboundCookieResponsePacket packet) {
        connection.handleCookieResponse(packet);
    }

    @Override
    public void handleCustomPayload(final ServerboundCustomPayloadPacket packet) {
        connection.handlePluginMessage(packet);
    }

    @Override
    public void handleResourcePackResponse(final ServerboundResourcePackPacket packet) {
        connection.notifyResourcePackResponse(packet.id(), packet.status());
    }

    public CompletableFuture<Boolean> respawn(final PlayerRespawnEvent.Cause cause) {
        final ServerPlayer player = playerOrThrow();
        final CompletableFuture<Boolean> result = new CompletableFuture<>();

        final boolean scheduled = player.execute(() -> {
            try {
                respawnNow(player, cause, result);
            } catch (final Exception exception) {
                LOGGER.error("An error occurred while respawning {}", player.name(), exception);
                result.complete(false);
            }
        }, () -> result.complete(false));
        if (!scheduled) {
            result.complete(false);
        }
        return result;
    }

    private void respawnNow(final ServerPlayer player, final PlayerRespawnEvent.Cause cause, final CompletableFuture<Boolean> result) {
        if (!player.isDead() && !player.isAwaitingRespawn()) {
            LOGGER.debug("{} requested a respawn while alive (health={})", player.name(), player.health());
            result.complete(false);
            return;
        }
        final ServerWorld defaultWorld = worldOrDisconnect();

        Location requestedSpawn = defaultWorld.spawnLocation(); // default spawn
        boolean usedRespawnPoint = false;

        final RespawnPoint point = player.respawnPoint();
        if (point != null) {
            final ServerWorld target = server.worlds().manager().world(point.world().key());
            if (target != null) {
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

        final TypedKey<DimensionTypeDefinition> dimensionType = world.generator.dimensionType().key();
        connection.send(new ClientboundRespawnPacket(
                world.dimension().id(),
                dimensionType,
                player.gameMode().id(),
                ClientboundRespawnPacket.KEEP_NOTHING,
                describeGenerator(world) instanceof ChunkGeneratorConfig.Flat));
        connection.send(player.abilitiesPacket());
        player.sendHealth();
        player.resendAttributeBases();
        connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_WAITING_FOR_CHUNKS, 0f));

        final CompletableFuture<Boolean> moved = new CompletableFuture<>();
        moveToRespawnPoint(world, spawn, moved);

        // completed by the thread owning the player once they are at their respawn point
        moved.thenAccept(succeeded -> {
            try {
                if (!succeeded) {
                    LOGGER.warn("{} could not be moved to their respawn point at {}", player.name(), spawn);
                    result.complete(false);
                    return;
                }
                sendTeleport(player, spawn);
                if (!connection.postEffects().isEmpty()) {
                    connection.sendPostEffects(connection.postEffects());
                }
                server.worlds().dayNight().syncTo(world, connection::send);
                server.worlds().weather().syncTo(world, connection::send);
                server.worlds().gameRules().syncTo(world, player.entityId(), connection::send);
                server.entities().tracker().update(player, server.players().online());
                LOGGER.debug("{} respawned at {}", player.name(), spawn);
                result.complete(true);
            } catch (final Exception exception) {
                LOGGER.error("An error occurred while respawning {}", player.name(), exception);
                result.complete(false);
            }
        });
    }

    private void moveToRespawnPoint(final ServerWorld world, final Location spawn, final CompletableFuture<Boolean> result) {
        final ServerPlayer respawning = playerOrThrow();
        final ChunkPos destination = spawn.chunk();
        try {
            final ServerWorld from = (ServerWorld) respawning.world();
            if (from.equals(world)) {
                final ChunkPos fromChunk = respawning.chunk();
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
                return;
            }

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
                return;
            }
            respawning.setLocation(spawn);
        } catch (final Exception exception) {
            LOGGER.error("An error occurred while moving {} to their respawn point", respawning.name(), exception);
            result.complete(false);
        }
    }

    private void trackFall(final Location previous, final Location current, final boolean wasOnGround, final boolean isOnGround) {
        final ServerPlayer player = playerOrThrow();
        if (wasOnGround && isOnGround) return;

        if (player.isFlying()) {
            player.setFallDistance(0.0);
            player.setFalling(false);
            return;
        }

        player.updateFallDistance(previous, current);

        if (isOnGround) {
            if (player.fallDistance() > 0.0) {
                final double fallen = player.fallDistance();
                player.landAfterFall();
                landOn(player, current, fallen);
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

    /**
     * Tells farming what a player landed on, so that soil gets trampled.
     */
    private void landOn(final ServerPlayer player, final Location landing, final double fallen) {
        if (!(landing.world() instanceof final ServerWorld world)) {
            return;
        }
        // the block under the feet, even when it stands a little lower than a full block
        final BlockPosition ground = Position.block(landing.blockX(), (int) Math.floor(landing.y() - 0.2), landing.blockZ());
        world.scheduler().execute(world.key(), ground.chunk(),
                () -> server.worlds().farming().world(world).entityLanded(player, ground, fallen));
    }

    private ServerPlayer playerOrThrow() {
        final ServerPlayer player = this.player;
        Preconditions.checkState(player != null, "No player is bound to the connection of %s yet", connection.username());
        return player;
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

    private void sendTeleport(final ServerPlayer player, final Location to) {
        connection.send(new ClientboundPlayerPositionPacket(
                player.nextTeleportId(),
                new PositionData.PositionMoveRotationData(
                        LocationPositionData.vec3(to),
                        new PositionData.Vec3D(0.0, 0.0, 0.0),
                        LocationPositionData.floatRotation(to))));
    }
}
