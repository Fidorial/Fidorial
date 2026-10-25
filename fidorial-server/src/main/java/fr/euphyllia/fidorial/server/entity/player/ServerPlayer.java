package fr.euphyllia.fidorial.server.entity.player;

import fr.euphyllia.fidorial.server.debug.DebugSubscriptionState;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.events.player.PlayerOpenInventoryEventImpl;
import fr.euphyllia.fidorial.server.inventory.ContainerMenu;
import fr.euphyllia.fidorial.server.inventory.InventoryMenu;
import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.network.nbt.ComponentResolver;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundAddEntityPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundBossEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundClearTitlesPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundContainerClosePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundDisguisedChatPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundEntityEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundGameEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundOpenScreenPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundPlayerAbilitiesPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundPlayerInfoGameModePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundPlayerInfoUpdatePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundRemoveMobEffectPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundRotateHeadPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetActionBarTextPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetHealthPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetHeldSlotPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetSubtitleTextPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetTitleTextPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetTitlesAnimationPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundEntityPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundStopSoundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSystemChatPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundTabListPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundUpdateAttributesPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundUpdateMobEffectPacket;
import fr.euphyllia.fidorial.server.util.annotations.NeedsToBeRevisited;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.attribute.Attribute;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.RespawnPoint;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.entity.player.Player;
import fr.fidorial.entity.player.PlayerInput;
import fr.fidorial.entity.player.profile.PlayerProfile;
import fr.fidorial.event.player.PlayerOpenInventoryEvent;
import fr.fidorial.event.player.PlayerPostEffectsModifyEvent;
import fr.fidorial.event.player.PlayerRespawnEvent;
import fr.fidorial.inventory.EnderChestInventory;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.permission.PermissionResolver;
import fr.fidorial.permission.PermissionState;
import fr.fidorial.permission.PermissionStateHolder;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.GameEvent;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.translation.TranslationStore;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.permission.PermissionChecker;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.pointer.PointersSupplier;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import java.net.InetAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public final class ServerPlayer extends AbstractLivingEntity implements Player, PermissionStateHolder {

    public static final float MAX_HEALTH = 20f;

    // https://minecraft.wiki/w/Java_Edition_protocol/Entity_metadata#Avatar

    public static final int MAX_AIR_SUPPLY = 300;
    public static final int MD_MAIN_HAND = 15; // Main hand (0: left, 1: right)
    public static final int MD_DISPLAYED_SKIN_PARTS =
            16; // The Displayed Skin Parts bit mask that is sent in Client Information
    private static final int MAX_TRACKED_ATTACK_TICKS = 100;
    private static final Key ATTRIBUTE_REGISTRY = RegistryKey.ATTRIBUTE.key();
    private static final Key MOB_EFFECT_REGISTRY = RegistryKey.MOB_EFFECT.key();
    private static final double EYE_HEIGHT = 1.62;
    private static final double SNEAKING_EYE_HEIGHT = 1.27;
    private static final int[] ARMOR_SLOTS = {
            PlayerInventory.FEET, PlayerInventory.LEGS, PlayerInventory.CHEST, PlayerInventory.HEAD
    };
    private static final int VOID_MARGIN = 64;
    private static final float VOID_DAMAGE = 4.0f;
    private static final int BURN_INTERVAL_TICKS = 20;
    private static final float BURN_DAMAGE = 1.0f;

    private static final int REGENERATION_INTERVAL_TICKS = 80;
    private static final float REGENERATION_AMOUNT = 1.0f;
    private static final double SAFE_FALL_DISTANCE = 3.0;
    private static final float SMALL_FALL_THRESHOLD = 4.0f;
    private static final PointersSupplier<ServerPlayer> pointers = PointersSupplier.<ServerPlayer>builder()
            .resolving(Identity.DISPLAY_NAME, ServerPlayer::displayName)
            .resolving(Identity.NAME, ServerPlayer::name)
            .resolving(Identity.UUID, ServerPlayer::uuid)
            .resolving(Identity.LOCALE, ServerPlayer::locale)
            .resolving(PermissionChecker.POINTER, player -> player::permissionState)
            .build();
    private final PlayerProfile profile;
    private final PlayerInventory inventory;
    private final EnderChestInventory enderChest;
    private final ClientConnection connection;
    private final PermissionState permissions;
    private final Map<BossBar, BossBarEntry> activeBossBars = new ConcurrentHashMap<>();
    private volatile GameMode gameMode;
    private volatile int selectedSlot;
    private volatile PlayerInput input = PlayerInput.NONE;
    private final Map<Key, Double> attributeBases = new ConcurrentHashMap<>();
    private volatile boolean sprinting;
    private volatile boolean sneaking;
    private volatile boolean falling;
    private volatile boolean awaitingRespawn;
    private volatile double fallDistance;
    private final AtomicInteger ticksSinceLastAttack = new AtomicInteger(MAX_TRACKED_ATTACK_TICKS);
    private volatile int airSupply = MAX_AIR_SUPPLY;
    private volatile int lastTeleportId;
    private volatile boolean onGround;
    private final InventoryMenu inventoryMenu;
    private volatile ContainerMenu containerMenu;
    private volatile @Nullable RespawnPoint respawnPoint;
    private int nextWindowId = 1;
    private Locale locale;
    private final AtomicReference<DebugSubscriptionState> debugSubscriptions = new AtomicReference<>(DebugSubscriptionState.EMPTY);

    public ServerPlayer(
            final int entityId,
            final PlayerProfile profile,
            final PlayerInventory inventory,
            final EnderChestInventory enderChest,
            final GameMode gameMode,
            final ClientConnection connection,
            final Location location
    ) {
        super(entityId, profile.uuid(), EntityTypes.PLAYER, location, MAX_HEALTH);
        this.profile = profile;
        this.inventory = inventory;
        this.enderChest = enderChest;
        this.gameMode = gameMode;
        this.connection = connection;
        this.locale = connection.locale();
        this.permissions = new PermissionState(
                this,
                connection.server().registries().permissions(),
                () -> connection.server()
                        .services()
                        .find(PermissionResolver.class)
                        .map(List::of)
                        .orElseGet(List::of));

        this.inventoryMenu = new InventoryMenu(this);
        this.containerMenu = inventoryMenu;
    }

    private static int encodeFlags(final Set<BossBar.Flag> flags) {
        int result = 0;
        if (flags.contains(BossBar.Flag.DARKEN_SCREEN)) result |= 1;
        if (flags.contains(BossBar.Flag.PLAY_BOSS_MUSIC)) result |= 2;
        if (flags.contains(BossBar.Flag.CREATE_WORLD_FOG)) result |= 4;
        return result;
    }

    @Override
    public PermissionState permissions() {
        return permissions;
    }

    @Override
    public boolean isOperator() {
        return connection.server().moderation().operators().isOp(profile.uuid());
    }

    @Override
    public void setOperator(final boolean operator) {
        connection.server().moderation().operators().setOp(profile.uuid(), profile.name(), operator);
        invalidatePermissions();
    }

    @Override
    public Component displayName() {
        return Component.text(profile().name());
    }

    @Override
    public void invalidatePermissions() {
        permissions.invalidate();
        updateClientPermissionLevel();
        refreshCommands();
        connection.server().debug().subscribers().refresh(this);
    }

    private void updateClientPermissionLevel() {
        final int level = isOperator() ? 4 : 0;
        connection.send(new ClientboundEntityEventPacket(entityId(), (byte) (24 + level)));
    }

    @Override
    public void refreshCommands() {
        connection.send(connection.server().commands().manager().createCommandsPacket(this));
    }

    @Override
    public PlayerProfile profile() {
        return profile;
    }

    public PlayerInventory inventory() {
        return inventory;
    }

    @Override
    public EnderChestInventory enderChest() {
        return enderChest;
    }

    /**
     * Window 0: the player inventory, with its 2x2 crafting grid. Always there, even while another
     * window is open.
     */
    public InventoryMenu inventoryMenu() {
        return inventoryMenu;
    }

    /**
     * {@return the window the client has open: the {@linkplain #inventoryMenu() inventory} when it
     * has no other, as vanilla's {@code containerMenu}}
     */
    public ContainerMenu containerMenu() {
        return containerMenu;
    }

    /**
     * Allocates the next Window ID. IDs stay within 1..99: beyond that, several protocol packets
     * still encode the window on a single byte.
     */
    public int allocateWindowId() {
        final int id = nextWindowId;
        nextWindowId = id >= 99 ? 1 : id + 1;
        return id;
    }

    /**
     * Opens a window on the client and keeps it as the current window, unless a
     * {@link PlayerOpenInventoryEvent} listener cancels it. Any previously open window is cleanly
     * closed beforehand.
     *
     * @return {@code true} when the window opened
     */
    public boolean openMenu(final ContainerMenu menu) {
        final PlayerOpenInventoryEvent event = connection.server().events().post(new PlayerOpenInventoryEventImpl(
                this, menu.menuType(), menu.title(), menu.blockPosition(), menu.container()));
        if (event.isCancelled()) {
            return false;
        }
        if (containerMenu != inventoryMenu) {
            closeContainer();
        }
        this.containerMenu = menu;
        connection.send(new ClientboundOpenScreenPacket(
                menu.windowId(),
                connection.server().registries().frozen().networkId(RegistryKey.MENU.key(), menu.menuType().key()),
                menu.title()));
        menu.sendAllDataToRemote();
        menu.onOpened();
        emitContainerEvent(GameEventKeys.CONTAINER_OPEN, menu);
        return true;
    }

    /**
     * Like vanilla, every tick: the client hears of what changed in its window, then a window that
     * is no longer valid (its block gone, or out of reach) closes.
     */
    private void tickMenu() {
        final ContainerMenu menu = containerMenu;
        menu.broadcastChanges();
        if (!menu.stillValid()) {
            closeContainer();
        }
    }

    /**
     * Vanilla's reach check: whether the block at {@code pos} is within this player's block
     * interaction range, plus {@code buffer}, from their eyes.
     */
    public boolean isWithinBlockInteractionRange(final BlockPosition pos, final double buffer) {
        final double range = attributeBase(Attribute.BLOCK_INTERACTION_RANGE)
                .orElse(PlayerAttributeDefaults.base(Attribute.BLOCK_INTERACTION_RANGE)) + buffer;
        final Location at = location();
        final double eyeY = at.y() + (isSneaking() ? SNEAKING_EYE_HEIGHT : EYE_HEIGHT);
        final double dx = Math.max(Math.max(pos.blockX() - at.x(), at.x() - (pos.blockX() + 1)), 0.0);
        final double dy = Math.max(Math.max(pos.blockY() - eyeY, eyeY - (pos.blockY() + 1)), 0.0);
        final double dz = Math.max(Math.max(pos.blockZ() - at.z(), at.z() - (pos.blockZ() + 1)), 0.0);
        return dx * dx + dy * dy + dz * dz < range * range;
    }

    /**
     * Closes the current window from the server side: tells the client, then
     * {@linkplain #doCloseContainer() closes it}.
     */
    public void closeContainer() {
        connection.send(new ClientboundContainerClosePacket(containerMenu.windowId()));
        doCloseContainer();
    }

    /**
     * Vanilla's {@code doCloseContainer}, also what the client closing its window does: the window
     * is {@linkplain ContainerMenu#removed() removed}, then the inventory takes over what the client
     * knows of the slots both show and becomes the current window again. Closing the inventory
     * itself gives back its cursor and its 2x2 grid.
     */
    public void doCloseContainer() {
        final ContainerMenu menu = containerMenu;
        menu.removed();
        if (menu == inventoryMenu) {
            return;
        }
        inventoryMenu.transferState(menu);
        containerMenu = inventoryMenu;
        emitContainerEvent(GameEventKeys.CONTAINER_CLOSE, menu);
    }

    private void emitContainerEvent(final TypedKey<GameEvent> event, final ContainerMenu menu) {
        final var events = connection.server().debug().gameEvents();
        final BlockPosition block = menu.blockPosition();
        if (block != null) {
            events.emit(world(), event, block);
        } else {
            events.emit(world(), event, location());
        }
    }

    public ClientConnection connection() {
        return connection;
    }

    @NeedsToBeRevisited("This currently relies fully on the bits the client sends; without any server-side validation")
    public boolean onGround() {
        // FIXME: This currently relies fully on the bits the client sends; without any server-side validation.
        // Needs to be revisited in the future.
        return this.onGround;
    }

    public void setOnGround(final boolean onGround) {
        this.onGround = onGround;
    }

    public void setLocale(final String language) {
        this.locale = Locale.forLanguageTag(language.replace('_', '-'));
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    public Locale locale() {
        return this.locale;
    }

    public ItemStack heldItem() {
        return inventory.get(selectedSlot);
    }

    public boolean isSprinting() {
        return sprinting;
    }

    public void setSprinting(final boolean sprinting) {
        this.sprinting = sprinting;
    }

    public boolean isSneaking() {
        return sneaking;
    }

    public void setSneaking(final boolean sneaking) {
        this.sneaking = sneaking;
    }

    public double fallDistance() {
        return fallDistance;
    }

    public void setFallDistance(final double fallDistance) {
        this.fallDistance = Math.max(0.0, fallDistance);
    }

    public boolean isFalling() {
        return falling;
    }

    public void setFalling(final boolean falling) {
        this.falling = falling;
    }

    public int ticksSinceLastAttack() {
        return ticksSinceLastAttack.get();
    }

    public void resetAttackCooldown() {
        this.ticksSinceLastAttack.set(0);
    }

    public int airSupply() {
        return airSupply;
    }

    public void setAirSupply(final int airSupply) {
        this.airSupply = Math.clamp(airSupply, 0, MAX_AIR_SUPPLY);
    }

    @Override
    public boolean isAwaitingRespawn() {
        return awaitingRespawn;
    }

    public void setAwaitingRespawn(final boolean awaitingRespawn) {
        this.awaitingRespawn = awaitingRespawn;
    }

    @Override
    public @Nullable RespawnPoint respawnPoint() {
        return respawnPoint;
    }

    @Override
    public void setRespawnPoint(final @Nullable RespawnPoint point) {
        this.respawnPoint = point;
    }

    @Override
    public CompletableFuture<Boolean> respawn() {
        if (isRemoved() || (!isDead() && !awaitingRespawn)) {
            return CompletableFuture.completedFuture(false);
        }
        return connection.respawn(PlayerRespawnEvent.Cause.API);
    }

    @Override
    public double armor() {
        return 0;
    }

    @Override
    public double armorToughness() {
        return 0;
    }

    @Override
    public double knockbackResistance() {
        return 0;
    }

    public boolean isInvulnerableToDamage() {
        return gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR;
    }

    public void resetOnRespawn() {
        setHealth(maxHealth());
        setAbsorptionAmount(0f);
        setFireTicks(0);
        setLastDamage(0f);
        setInvulnerableTicks(0);
        setAirSupply(MAX_AIR_SUPPLY);
        setFallDistance(0.0);
        setAwaitingRespawn(false);
        clearEffectsSilently();
        this.ticksSinceLastAttack.set(MAX_TRACKED_ATTACK_TICKS);
    }

    @Override
    protected void onEffectUpdated(final MobEffectInstance effect) {
        final int id = effectNetworkId(effect);
        if (id >= 0) {
            connection.send(ClientboundUpdateMobEffectPacket.of(entityId(), id, effect));
        }
    }

    @Override
    protected void onEffectRemoved(final MobEffectInstance effect) {
        final int id = effectNetworkId(effect);
        if (id < 0) {
            return;
        }
        for (final MobEffectInstance other : activeEffects()) {
            if (effectNetworkId(other) == id) {
                connection.send(ClientboundUpdateMobEffectPacket.of(entityId(), id, other));
                return;
            }
        }
        connection.send(new ClientboundRemoveMobEffectPacket(entityId(), id));
    }

    private int effectNetworkId(final MobEffectInstance effect) {
        return connection.server().registries().effects().clientNetworkId(this, effect.type().key());
    }

    @Override
    public void tick(final long currentTick) {
        if (isRemoved()) {
            return;
        }
        tickMenu();
        tickLiving(currentTick);
        if (ticksSinceLastAttack.get() < MAX_TRACKED_ATTACK_TICKS) {
            ticksSinceLastAttack.incrementAndGet();
        }
        if (isDead() || awaitingRespawn || isInvulnerableToDamage()) {
            return;
        }
        tickVoid();
        tickFire();
        tickRegeneration(currentTick);
        connection.server().maps().tickPlayer(this);
    }

    public float landAfterFall() {
        final double distance = fallDistance;
        setFallDistance(0.0);
        setFalling(false);
        if (distance > 0.0) {
            connection.server().debug().gameEvents().emit(world(), GameEventKeys.HIT_GROUND, location());
        }
        if (distance <= SAFE_FALL_DISTANCE || isInvulnerableToDamage()) {
            return 0f;
        }
        final float damage = (float) Math.floor(distance - SAFE_FALL_DISTANCE);
        if (damage <= 0f) {
            return 0f;
        }
        playSound(Sound.sound(
                damage > SMALL_FALL_THRESHOLD ? SoundEvents.PLAYER_BIG_FALL : SoundEvents.PLAYER_SMALL_FALL,
                Sound.Source.PLAYER, 1.0f, 1.0f));
        damage(DamageSource.fall(), damage);
        return damage;
    }

    @Override
    public Pointers pointers() {
        return pointers.view(this);
    }

    @Override
    public void sendMessage(final Component message) {
        connection.send(new ClientboundSystemChatPacket(prepareMessageForSend(message), false));
    }

    @Override
    public void sendMessage(final Component message, final ChatType.Bound chatType) {
        connection.send(new ClientboundDisguisedChatPacket(prepareMessageForSend(message), chatType, this));
    }

    @Override
    public void sendMessage(final SignedMessage signedMessage, final ChatType.Bound chatType) {
        connection.sendSignedMessage(signedMessage, chatType);
    }

    @Override
    public void deleteMessage(final SignedMessage.Signature signature) {
        connection.deleteSignedMessage(signature);
    }

    @Override
    public void sendActionBar(final Component message) {
        connection.send(new ClientboundSetActionBarTextPacket(prepareMessageForSend(message)));
    }

    @Override
    public void sendPlayerListHeaderAndFooter(final Component header, final Component footer) {
        connection.send(new ClientboundTabListPacket(header, footer));
    }

    @Override
    public void showTitle(final Title title) {
        final Title.Times titleTimes = title.times();
        if (titleTimes != null) connection.send(new ClientboundSetTitlesAnimationPacket(
                convertDurationToTicks(titleTimes.fadeIn()),
                convertDurationToTicks(titleTimes.stay()),
                convertDurationToTicks(titleTimes.fadeOut())));
        connection.send(new ClientboundSetTitleTextPacket(prepareMessageForSend(title.title())));
        connection.send(new ClientboundSetSubtitleTextPacket(prepareMessageForSend(title.subtitle())));
    }

    @Override
    public <T> void sendTitlePart(final TitlePart<T> titlePart, final T value) {
        switch (value) {
            case final Component message -> connection.send(titlePart == TitlePart.TITLE
                    ? new ClientboundSetTitleTextPacket(prepareMessageForSend(message))
                    : new ClientboundSetSubtitleTextPacket(prepareMessageForSend(message)));
            case final Title.Times times -> connection.send(new ClientboundSetTitlesAnimationPacket(
                    convertDurationToTicks(times.fadeIn()),
                    convertDurationToTicks(times.stay()),
                    convertDurationToTicks(times.fadeOut())));
            default -> throw new IllegalStateException("Unexpected value: " + value);
        }
    }

    @Override
    public void clearTitle() {
        connection.send(new ClientboundClearTitlesPacket(false));
    }

    @Override
    public void resetTitle() {
        connection.send(new ClientboundClearTitlesPacket(true));
    }

    private int convertDurationToTicks(final Duration duration) {
        return (int) (duration.toMillis() / 50L);
    }

    @Override
    public void playSound(final Sound sound) {
        final Location loc = location();
        connection.send(new ClientboundSoundPacket(sound, loc.x(), loc.y(), loc.z()));
    }

    @Override
    public void playSound(final Sound sound, final double x, final double y, final double z) {
        connection.send(new ClientboundSoundPacket(sound, x, y, z));
    }

    @Override
    public void playSound(final Sound sound, final Sound.Emitter emitter) {
        if (emitter == Sound.Emitter.self()) {
            connection.send(new ClientboundSoundEntityPacket(sound, entityId()));
        } else if (emitter instanceof final Entity entity) {
            connection.send(new ClientboundSoundEntityPacket(sound, entity.entityId()));
        } else {
            throw new IllegalArgumentException("Sound emitter must be an Entity or self(), but was: " + emitter);
        }
    }

    @Override
    public void stopSound(final SoundStop stop) {
        connection.send(new ClientboundStopSoundPacket(stop.source(), stop.sound()));
    }

    @Override
    public Sound.Source soundSource() {
        return Sound.Source.PLAYER;
    }

    @Override
    public void showBossBar(final BossBar bar) {
        if (activeBossBars.containsKey(bar)) return;
        final UUID id = UUID.randomUUID();
        final BossBar.Listener listener = new BossBar.Listener() {
            @Override
            public void bossBarProgressChanged(final BossBar bar, final float old, final float now) {
                connection.send(new ClientboundBossEventPacket.UpdateProgress(id, now));
            }

            @Override
            public void bossBarNameChanged(final BossBar bar, final Component old, final Component now) {
                connection.send(new ClientboundBossEventPacket.UpdateName(id, now));
            }

            @Override
            public void bossBarColorChanged(final BossBar bar, final BossBar.Color old, final BossBar.Color now) {
                connection.send(new ClientboundBossEventPacket.UpdateStyle(id, now, bar.overlay()));
            }

            @Override
            public void bossBarOverlayChanged(final BossBar bar, final BossBar.Overlay old, final BossBar.Overlay now) {
                connection.send(new ClientboundBossEventPacket.UpdateStyle(id, bar.color(), now));
            }

            @Override
            public void bossBarFlagsChanged(final BossBar ba, final Set<BossBar.Flag> added, final Set<BossBar.Flag> removed) {
                connection.send(new ClientboundBossEventPacket.UpdateProperties(id, encodeFlags(ba.flags())));
            }
        };
        activeBossBars.put(bar, new BossBarEntry(id, listener));
        bar.addListener(listener);
        connection.send(new ClientboundBossEventPacket.Add(id, bar.name(), bar.progress(),
                bar.color(), bar.overlay(), encodeFlags(bar.flags())));
    }

    @Override
    public void hideBossBar(final BossBar bar) {
        final BossBarEntry entry = activeBossBars.remove(bar);
        if (entry == null) return;
        bar.removeListener(entry.listener());
        connection.send(new ClientboundBossEventPacket.Remove(entry.id()));
    }

    public void clearActiveBossBars() {
        for (final Map.Entry<BossBar, BossBarEntry> entry : activeBossBars.entrySet()) {
            entry.getKey().removeListener(entry.getValue().listener());
        }
        activeBossBars.clear();
    }

    @Override
    public InetAddress address() {
        return connection.remoteInetAddress();
    }

    @Override
    public int ping() {
        return connection.ping();
    }

    @Override
    public void sendResourcePacks(final ResourcePackRequest request) {
        connection.sendResourcePacks(request);
    }

    @Override
    public void removeResourcePacks(final UUID id, final UUID... others) {
        connection.removeResourcePacks(id, others);
    }

    @Override
    public void clearResourcePacks() {
        connection.clearResourcePacks();
    }

    @Override
    public void showDialog(final DialogLike dialog) {
        connection.showDialog(dialog);
    }

    @Override
    public void closeDialog() {
        connection.closeDialog();
    }

    @Override
    public void kick(final Component reason) {
        connection.disconnect(reason);
    }

    @Override
    public GameMode gameMode() {
        return gameMode;
    }

    @Override
    public void setGameMode(final GameMode gameMode) {
        if (gameMode == this.gameMode) {
            return;
        }
        this.gameMode = gameMode;
        connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, gameMode.id()));
        connection.send(ClientboundPlayerAbilitiesPacket.forGameMode(gameMode));
        connection.server().players().broadcast(new ClientboundPlayerInfoGameModePacket(uuid(), gameMode.id()));
    }

    @Override
    public @UnmodifiableView Iterable<? extends BossBar> activeBossBars() {
        return Collections.unmodifiableSet(activeBossBars.keySet());
    }

    public int selectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(final int selectedSlot) {
        acceptSelectedSlot(selectedSlot);
        connection.send(new ClientboundSetHeldSlotPacket(selectedSlot));
    }

    public void acceptSelectedSlot(final int selectedSlot) {
        if (selectedSlot < 0 || selectedSlot > 8) {
            throw new IllegalArgumentException("slot de hotbar invalide : " + selectedSlot);
        }
        this.selectedSlot = selectedSlot;
    }

    @Override
    public PlayerInput input() {
        return input;
    }

    @Override
    public void setAttributeBase(final Key attribute, final double base) {
        final int id = attributeId(attribute);
        if (!Double.isFinite(base)) {
            throw new IllegalArgumentException("Attribute base value must be finite: " + base);
        }
        attributeBases.put(attribute, base);
        connection.send(new ClientboundUpdateAttributesPacket(entityId(),
                List.of(new ClientboundUpdateAttributesPacket.Value(id, base))));
    }

    @Override
    public void resetAttributeBase(final Key attribute) {
        final int id = attributeId(attribute);
        if (attributeBases.remove(attribute) != null) {
            connection.send(new ClientboundUpdateAttributesPacket(entityId(),
                    List.of(new ClientboundUpdateAttributesPacket.Value(id, PlayerAttributeDefaults.base(attribute)))));
        }
    }

    @Override
    public OptionalDouble attributeBase(final Key attribute) {
        final Double base = attributeBases.get(attribute);
        return base == null ? OptionalDouble.empty() : OptionalDouble.of(base);
    }

    public void resendAttributeBases() {
        final List<ClientboundUpdateAttributesPacket.Value> values = new ArrayList<>(attributeBases.size());
        for (final Map.Entry<Key, Double> entry : attributeBases.entrySet()) {
            values.add(new ClientboundUpdateAttributesPacket.Value(attributeId(entry.getKey()), entry.getValue()));
        }
        if (!values.isEmpty()) {
            connection.send(new ClientboundUpdateAttributesPacket(entityId(), values));
        }
    }

    private int attributeId(final Key attribute) {
        final int id = connection.server().registries().frozen().networkId(ATTRIBUTE_REGISTRY, attribute);
        if (id < 0) {
            throw new IllegalArgumentException("Unknown attribute: " + attribute.asString());
        }
        return id;
    }

    public void setInput(final PlayerInput input) {
        this.input = input;
    }

    @Override
    public void sendSpawnPackets(final ClientConnection viewer) {
        viewer.send(new ClientboundPlayerInfoUpdatePacket(profile(), gameMode().id(), ping()));
        viewer.send(ClientboundAddEntityPacket.of(this));
        viewer.send(new ClientboundRotateHeadPacket(entityId(), location().yaw()));
        viewer.send(ClientboundSetEntityMetadataPacket.of(
                entityId(),
                ClientboundSetEntityMetadataPacket.Entry.ofByte(MD_DISPLAYED_SKIN_PARTS, connection().displayedSkinParts())));
    }

    @Override
    public CompletableFuture<Boolean> enterConfigurationPhase() {
        return connection.enterConfiguration();
    }

    @Override
    public void updateInventory() {
        containerMenu.sendAllDataToRemote();
    }

    @Override
    public List<Key> activePostEffects() {
        return connection.postEffects();
    }

    @Override
    public CompletableFuture<Boolean> setActivePostEffects(final SequencedCollection<Key> effects) {
        final List<Key> requested = List.copyOf(effects);
        return modifyPostEffects(PlayerPostEffectsModifyEvent.Cause.API, _ -> requested);
    }

    @Override
    public CompletableFuture<Boolean> activatePostEffect(final Key effect) {
        Objects.requireNonNull(effect, "effect");
        return modifyPostEffects(PlayerPostEffectsModifyEvent.Cause.API,
                current -> Stream.concat(current.stream(), Stream.of(effect)).toList());
    }

    @Override
    public CompletableFuture<Boolean> deactivatePostEffect(final Key effect) {
        Objects.requireNonNull(effect, "effect");
        return modifyPostEffects(PlayerPostEffectsModifyEvent.Cause.API,
                current -> current.stream().filter(active -> !active.equals(effect)).toList());
    }

    @Override
    public CompletableFuture<Boolean> clearActivePostEffects() {
        return modifyPostEffects(PlayerPostEffectsModifyEvent.Cause.API, _ -> List.of());
    }

    public CompletableFuture<Boolean> modifyPostEffects(final PlayerPostEffectsModifyEvent.Cause cause, final UnaryOperator<List<Key>> change) {
        final CompletableFuture<Boolean> result = new CompletableFuture<>();
        final boolean scheduled = execute(() -> {
            try {
                if (isRemoved()) {
                    result.complete(false);
                    return;
                }
                final List<Key> current = connection.postEffects();
                final List<Key> next = List.copyOf(new LinkedHashSet<>(change.apply(current)));
                if (next.equals(current)) {
                    result.complete(false);
                    return;
                }
                final PlayerPostEffectsModifyEvent event = connection.server().events().post(new PlayerPostEffectsModifyEvent(this, cause, current, next));
                if (event.isCancelled()) {
                    result.complete(false);
                    return;
                }
                connection.updatePostEffects(next);
                result.complete(true);
            } catch (final Throwable t) {
                result.completeExceptionally(t);
            }
        });
        if (!scheduled) {
            result.complete(false);
        }
        return result;
    }

    public int nextTeleportId() {
        final var id = lastTeleportId;
        lastTeleportId = id + 1;
        return lastTeleportId;
    }

    @Override
    public CompletableFuture<Boolean> teleport(final Location location) {
        if (isRemoved()) {
            return CompletableFuture.completedFuture(false);
        }
        return connection.teleport(location);
    }

    public DebugSubscriptionState debugSubscriptions() {
        return debugSubscriptions.get();
    }

    public DebugSubscriptionState.Transition updateDebugSubscriptions(final UnaryOperator<DebugSubscriptionState> update) {
        while (true) {
            final DebugSubscriptionState previous = debugSubscriptions.get();
            final DebugSubscriptionState current = update.apply(previous);
            if (debugSubscriptions.compareAndSet(previous, current)) {
                return new DebugSubscriptionState.Transition(previous, current);
            }
        }
    }

    private double armorFromInventory(final ToDoubleFunction<ItemStack> value) {
        double total = 0.0;
        for (final int slot : ARMOR_SLOTS) {
            if (slot < inventory.size()) {
                total += value.applyAsDouble(inventory.get(slot));
            }
        }
        return total;
    }

    private void tickVoid() {
        final int floor = world() instanceof final ServerWorld serverWorld
                ? serverWorld.minY() - VOID_MARGIN
                : -VOID_MARGIN;
        if (location().y() < floor) {
            damage(DamageSource.outOfWorld(), VOID_DAMAGE);
        }
    }

    private void tickFire() {
        if (fireTicks() <= 0) {
            return;
        }
        if (fireTicks() % BURN_INTERVAL_TICKS == 0) {
            damage(DamageSource.onFire(), BURN_DAMAGE);
        }
        setFireTicks(fireTicks() - 1);
    }

    private void tickRegeneration(final long currentTick) {
        if (health() >= maxHealth() || currentTick % REGENERATION_INTERVAL_TICKS != 0) {
            return;
        }
        if (!(world() instanceof final ServerWorld serverWorld)
                || !serverWorld.gameRuleValues().getBoolean(GameRuleKeys.NATURAL_HEALTH_REGENERATION)) {
            return;
        }
        heal(REGENERATION_AMOUNT);
    }

    @Override
    public void heal(final float amount) {
        final float before = health();
        Player.super.heal(amount);
        if (health() != before) {
            connection.send(new ClientboundSetHealthPacket(health(), 20, 5.0f));
        }
    }

    @Override
    public ObjectContents asObjectContents() {
        return this.profile().asObjectContents();
    }

    @Contract("null -> null; !null -> new")
    public @Nullable Component prepareMessageForSend(final @Nullable Component message) {
        if (message == null) {
            return null;
        }
        final Component resolved = ComponentResolver.resolve(message, this.commandSource());
        return TranslationStore.render(resolved, locale());
    }

    private record BossBarEntry(UUID id, BossBar.Listener listener) {
    }

}
