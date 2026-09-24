package fr.euphyllia.fidorial.server.entity.player;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.inventory.ContainerMenu;
import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.network.nbt.ComponentResolver;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundAddEntityPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundBossEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundClearTitlesPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundContainerClosePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundContainerSetContentPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundDisguisedChatPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundEntityEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundGameEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundOpenScreenPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundPlayerAbilitiesPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundPlayerInfoGameModePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundPlayerInfoUpdatePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundRotateHeadPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetActionBarTextPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetHealthPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetSubtitleTextPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetTitleTextPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetTitlesAnimationPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundEntityPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundStopSoundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSystemChatPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundTabListPacket;
import fr.euphyllia.fidorial.server.util.annotations.NeedsToBeRevisited;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.command.CommandSender;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.Player;
import fr.fidorial.entity.PlayerProfile;
import fr.fidorial.entity.RespawnPoint;
import fr.fidorial.event.player.PlayerRespawnEvent;
import fr.fidorial.inventory.EnderChestInventory;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemStack;
import fr.fidorial.permission.PermissionResolver;
import fr.fidorial.permission.PermissionState;
import fr.fidorial.permission.PermissionStateHolder;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.translation.TranslationStore;
import fr.fidorial.world.Location;
import fr.fidorial.world.World;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.identity.Identity;
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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToDoubleFunction;

public final class ServerPlayer extends AbstractLivingEntity implements Player, PermissionStateHolder {

    public static final float MAX_HEALTH = 20f;

    // https://minecraft.wiki/w/Java_Edition_protocol/Entity_metadata#Avatar

    public static final int MAX_AIR_SUPPLY = 300;
    public static final int MD_MAIN_HAND = 15; // Main hand (0: left, 1: right)
    public static final int MD_DISPLAYED_SKIN_PARTS =
            16; // The Displayed Skin Parts bit mask that is sent in Client Information
    private static final int MAX_TRACKED_ATTACK_TICKS = 100;
    private static final int[] ARMOR_SLOTS = {36, 37, 38, 39};
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
    private volatile boolean sprinting;
    private volatile boolean sneaking;
    private volatile boolean falling;
    private volatile boolean awaitingRespawn;
    private volatile double fallDistance;
    private final AtomicInteger ticksSinceLastAttack = new AtomicInteger(MAX_TRACKED_ATTACK_TICKS);
    private volatile int airSupply = MAX_AIR_SUPPLY;
    private volatile int lastTeleportId;
    private volatile boolean onGround;
    private volatile @Nullable ContainerMenu openMenu;
    private volatile @Nullable RespawnPoint respawnPoint;
    private int nextWindowId = 1;
    private Locale locale;

    public ServerPlayer(
            final int entityId,
            final PlayerProfile profile,
            final PlayerInventory inventory,
            final EnderChestInventory enderChest,
            final GameMode gameMode,
            final ClientConnection connection,
            final World world,
            final Location location
    ) {
        super(entityId, profile.uuid(), EntityTypes.PLAYER, world, location, MAX_HEALTH);
        this.profile = profile;
        this.inventory = inventory;
        this.enderChest = enderChest;
        this.gameMode = gameMode;
        this.connection = connection;
        this.locale = connection.locale();
        this.permissions = new PermissionState(
                this,
                FidorialServer.getInstance().permissions(),
                () -> FidorialServer.getInstance()
                        .services()
                        .find(PermissionResolver.class)
                        .map(List::of)
                        .orElseGet(List::of));
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
        return FidorialServer.getInstance().operators().isOp(profile.uuid());
    }

    @Override
    public void setOperator(final boolean operator) {
        FidorialServer.getInstance().operators().setOp(profile.uuid(), profile.name(), operator);
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
    }

    private void updateClientPermissionLevel() {
        final int level = isOperator() ? 4 : 0;
        connection.send(new ClientboundEntityEventPacket(entityId(), (byte) (24 + level)));
    }

    @Override
    public void refreshCommands() {
        connection.send(connection.server().commandManager().createCommandsPacket(this));
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
     * The currently open container window, or {@code null}.
     */
    public @Nullable ContainerMenu openMenu() {
        return openMenu;
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
     * Opens a window on the client and keeps it as the current window. Any previously open window
     * is cleanly closed beforehand.
     */
    public void openMenu(final ContainerMenu menu) {
        closeMenu(true);
        this.openMenu = menu;
        connection.send(new ClientboundOpenScreenPacket(
                menu.windowId(),
                menu.menuTypeId(connection.server().registries().frozen()),
                menu.title()));
        connection.send(menu.buildSyncPacket(connection.server().registries().frozen()));
    }

    /**
     * Closes the current window, if any.
     *
     * @param notifyClient {@code true} to also send a {@code container_close} to the client;
     *                     unnecessary when it is precisely the client that just closed the window.
     */
    public void closeMenu(final boolean notifyClient) {
        final ContainerMenu menu = this.openMenu;
        if (menu == null) {
            return;
        }
        this.openMenu = null;
        menu.returnCarried();
        menu.onClosed();
        if (notifyClient) {
            connection.send(new ClientboundContainerClosePacket(menu.windowId()));
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
        this.ticksSinceLastAttack.set(MAX_TRACKED_ATTACK_TICKS);
    }

    @Override
    public void tick(final long currentTick) {
        if (isRemoved()) {
            return;
        }
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
    }

    public float landAfterFall() {
        final double distance = fallDistance;
        setFallDistance(0.0);
        setFalling(false);
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
        connection.server().broadcast(new ClientboundPlayerInfoGameModePacket(uuid(), gameMode.id()));
    }

    @Override
    public @UnmodifiableView Iterable<? extends BossBar> activeBossBars() {
        return Collections.unmodifiableSet(activeBossBars.keySet());
    }

    public int selectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(final int selectedSlot) {
        if (selectedSlot < 0 || selectedSlot > 8) {
            throw new IllegalArgumentException("slot de hotbar invalide : " + selectedSlot);
        }
        this.selectedSlot = selectedSlot;
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
    public void enterConfigurationPhase() {
        connection.enterConfiguration();
    }

    @Override
    public void updateInventory() {
        final ContainerMenu menu = openMenu;
        if (menu == null) {
            connection.send(ClientboundContainerSetContentPacket.ofPlayerInventory(
                    inventory,
                    0,
                    ItemStack.EMPTY,
                    connection.server().registries().network()));
            return;
        }
        connection.send(menu.buildSyncPacket(connection.server().registries().network()));
    }

    public int nextTeleportId() {
        final var id = lastTeleportId;
        lastTeleportId = id + 1;
        return lastTeleportId;
    }

    @Override
    public CompletableFuture<Boolean> teleport(final World destination, final Location location) {
        if (isRemoved() || !(destination instanceof final ServerWorld target)) {
            return CompletableFuture.completedFuture(false);
        }
        return connection.teleport(target, location);
    }

    @Override
    public CommandSender sender() {
        return this;
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
        if (!connection.server().gameRules().getBoolean(GameRuleKeys.NATURAL_HEALTH_REGENERATION)) {
            return;
        }
        heal(REGENERATION_AMOUNT);
        connection.send(new ClientboundSetHealthPacket(health(), 20, 5.0f));
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
        final Component resolved = ComponentResolver.resolve(message, this);
        return TranslationStore.render(resolved, locale());
    }

    private record BossBarEntry(UUID id, BossBar.Listener listener) {
    }

}
