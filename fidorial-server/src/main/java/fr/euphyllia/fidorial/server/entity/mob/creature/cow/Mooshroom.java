package fr.euphyllia.fidorial.server.entity.mob.creature.cow;

import fr.euphyllia.fidorial.server.codecs.item.ItemStackCodecs;
import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.mob.AbstractAnimal;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.item.SuspiciousFlowers;
import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.entity.ItemEntity;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.component.SuspiciousStewEffects;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.math.Location;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.sound.SoundEvents;
import io.papermc.adventurex.nbt.dfu.BinaryTagOps;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The mooshroom, as in Vanilla: what every {@linkplain AbstractCow cow} does, and it fills a bowl
 * with mushroom stew, turns into a cow when sheared, and changes colour when lightning strikes it.
 * A brown one that ate a flower gives a suspicious stew next.
 */
public final class Mooshroom extends AbstractCow implements Category.Neutral {

    /** Mooshroom entity data: its colour, after the 18 entries every ageable mob has. */
    private static final int MD_VARIANT = 18;

    private static final int MUSHROOMS_WHEN_SHEARED = 5;
    /** As in Vanilla, two parents of the same colour have a calf of the other one once in 1024. */
    private static final int MUTATION_CHANCE = 1024;
    private static final float MYCELIUM_WALK_VALUE = 10.0f;
    private static final float LOUD_VOLUME = 2.0f;

    private Variant variant = Variant.RED;
    private @Nullable SuspiciousStewEffects stewEffects;
    private @Nullable UUID lastLightningBolt;

    /**
     * Spawns a mooshroom as vanilla does out of nowhere, with a spawn egg or {@code /summon}: a red
     * adult.
     */

    public Mooshroom(final int entityId, final Location location) {
        this(entityId, location, false);
    }

    public Mooshroom(final int entityId, final Location location, final boolean baby) {
        super(entityId, EntityTypes.MOOSHROOM, location, baby);
    }

    /**
     * {@return {@code true} if mooshrooms may spawn on a block}: as in Vanilla, on a block of the
     * {@code mooshrooms_spawnable_on} tag, mycelium, in a light above 8
     */
    public static boolean canSpawn(final ServerWorld world, final int x, final int y, final int z) {
        final BlockState ground = BlockView.blockAt(world, x, y - 1, z);
        return ground != null
                && BlockView.isTagged(BlockTypeTagKeys.MOOSHROOMS_SPAWNABLE_ON, ground)
                && isBrightEnoughToSpawn(world, x, y, z);
    }

    public Variant variant() {
        return variant;
    }

    public void setVariant(final Variant variant) {
        this.variant = variant;
        sendToTrackers(variantMetadata());
    }

    /**
     * {@return the effects of the flower this mooshroom ate, which its next stew takes; {@code null}
     * if it ate none}
     */
    public @Nullable SuspiciousStewEffects stewEffects() {
        return stewEffects;
    }

    private ClientboundSetEntityMetadataPacket variantMetadata() {
        return ClientboundSetEntityMetadataPacket.of(entityId(),
                ClientboundSetEntityMetadataPacket.Entry.varInt(MD_VARIANT, variant.ordinal()));
    }

    @Override
    public void sendSpawnPackets(final ClientConnection connection) {
        super.sendSpawnPackets(connection);
        connection.send(variantMetadata());
    }

    /**
     * {@return how much a mooshroom likes to walk to a block}: as in Vanilla, onto mycelium above
     * all, otherwise where it is brighter
     */
    @Override
    public float walkTargetValue(final int x, final int y, final int z) {
        final ServerWorld world = serverWorld();
        final BlockState ground = BlockView.blockAt(world, x, y - 1, z);
        if (ground != null && ground.name().equals(BlockTypeKeys.MYCELIUM)) {
            return MYCELIUM_WALK_VALUE;
        }
        return lightWalkValue(world, x, y, z);
    }

    /**
     * Changes the colour of this mooshroom, as when lightning strikes it; a bolt only counts once.
     *
     * @param lightningBolt the bolt that struck
     */
    public void struckByLightning(final UUID lightningBolt) {
        // TODO: call this from lightning, once lightning bolts exist
        if (lightningBolt.equals(lastLightningBolt)) {
            return;
        }
        lastLightningBolt = lightningBolt;
        setVariant(variant == Variant.RED ? Variant.BROWN : Variant.RED);
        playSound(SoundEvents.MOOSHROOM_CONVERT, LOUD_VOLUME, 1.0f);
    }

    @Override
    protected boolean interact(final ServerPlayer player, final EquipmentSlotGroup hand) {
        final int slot = handSlot(player, hand);
        final ItemStack held = player.inventory().get(slot);
        if (held.isEmpty()) {
            return super.interact(player, hand);
        }
        final TypedKey<Item> item = held.id();
        if (item.equals(ItemKeys.BOWL) && !isBaby()) {
            fillBowl(player, slot);
            return true;
        }
        if (item.equals(ItemKeys.SHEARS) && readyForShearing()) {
            shear();
            server().debug().gameEvents().emit(world(), GameEventKeys.SHEAR, location());
            wearItem(player, slot);
            return true;
        }
        if (variant == Variant.BROWN && !isBaby()) {
            final SuspiciousStewEffects effects = SuspiciousFlowers.effectsOf(item);
            if (effects != null) {
                // TODO: smoke when it already ate a flower, effect particles when it eats one
                if (stewEffects == null) {
                    consumeItem(player, slot);
                    stewEffects = effects;
                    playSound(SoundEvents.MOOSHROOM_EAT, LOUD_VOLUME, 1.0f);
                }
                return true;
            }
        }
        return super.interact(player, hand);
    }

    /**
     * Fills a bowl with mushroom stew, or with the suspicious stew of the flower this mooshroom ate.
     */
    private void fillBowl(final ServerPlayer player, final int slot) {
        final SuspiciousStewEffects effects = stewEffects;
        final ItemStack stew;
        if (effects != null) {
            stew = ItemStack.of(ItemKeys.SUSPICIOUS_STEW, 1).with(DataComponentTypes.SUSPICIOUS_STEW_EFFECTS, effects);
            stewEffects = null;
        } else {
            stew = ItemStack.of(ItemKeys.MUSHROOM_STEW, 1);
        }
        fillItem(player, slot, stew, false);
        playSound(effects != null ? SoundEvents.MOOSHROOM_SUSPICIOUS_MILK : SoundEvents.MOOSHROOM_MILK, 1.0f, 1.0f);
    }

    public boolean readyForShearing() {
        return !isBaby();
    }

    /**
     * Shears this mooshroom, as in Vanilla: it becomes a cow of the same age, and its five
     * mushrooms fall from its back one by one.
     */
    public void shear() {
        final Location at = location();
        sendToTrackers(new ClientboundSoundPacket(
                Sound.sound(SoundEvents.MOOSHROOM_SHEAR, Sound.Source.PLAYER, 1.0f, 1.0f), at.x(), at.y(), at.z()));
        // TODO: the explosion particle, once particles exist
        final ServerWorld world = serverWorld();
        final double top = at.y() + height();
        final Variant sheared = variant;

        convertTo(new Cow(server().entities().ids().allocate(), at, false));

        final ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < MUSHROOMS_WHEN_SHEARED; i++) {
            server().entities().dropItem(world, at.x(), top, at.z(), ItemStack.of(sheared.mushroom(), 1),
                    random.nextDouble() * 0.2 - 0.1, 0.2, random.nextDouble() * 0.2 - 0.1,
                    ItemEntity.DEFAULT_PICKUP_DELAY, null);
        }
    }

    @Override
    protected @Nullable Mooshroom createOffspring(final AbstractAnimal partner) {
        final Mooshroom baby = new Mooshroom(server().entities().ids().allocate(), birthLocation(), true);
        if (partner instanceof final Mooshroom other) {
            baby.variant = offspringVariant(other);
        }
        return baby;
    }

    private Variant offspringVariant(final Mooshroom partner) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        if (variant == partner.variant && random.nextInt(MUTATION_CHANCE) == 0) {
            return variant == Variant.BROWN ? Variant.RED : Variant.BROWN;
        }
        return random.nextBoolean() ? variant : partner.variant;
    }

    @Override
    public void saveAdditional(final CompoundBinaryTag.Builder tag) {
        super.saveAdditional(tag);
        tag.putString("Type", variant.savedName());
        final SuspiciousStewEffects effects = stewEffects;
        if (effects != null) {
            ItemStackCodecs.SUSPICIOUS_STEW_EFFECTS_CODEC.encodeStart(BinaryTagOps.binaryTagOps(), effects)
                    .result().ifPresent(saved -> tag.put("stew_effects", saved));
        }
    }

    @Override
    public void loadAdditional(final CompoundBinaryTag tag) {
        super.loadAdditional(tag);
        this.variant = Variant.bySavedName(tag.getString("Type"));
        final BinaryTag saved = tag.get("stew_effects");
        this.stewEffects = saved == null
                ? null
                : ItemStackCodecs.SUSPICIOUS_STEW_EFFECTS_CODEC.parse(BinaryTagOps.binaryTagOps(), saved).result().orElse(null);
    }

    /**
     * The colour of a mooshroom, in the order of vanilla's ids.
     */
    public enum Variant {
        RED(ItemKeys.RED_MUSHROOM),
        BROWN(ItemKeys.BROWN_MUSHROOM);

        private final TypedKey<Item> mushroom;

        Variant(final TypedKey<Item> mushroom) {
            this.mushroom = mushroom;
        }

        /**
         * {@return the mushroom that grows on a mooshroom of this colour}
         */
        public TypedKey<Item> mushroom() {
            return mushroom;
        }

        /**
         * {@return the name vanilla saves this colour under}
         */
        public String savedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        /**
         * {@return the colour saved under a name, red if the name is unknown}
         */
        public static Variant bySavedName(final String name) {
            return BROWN.savedName().equals(name) ? BROWN : RED;
        }
    }
}
