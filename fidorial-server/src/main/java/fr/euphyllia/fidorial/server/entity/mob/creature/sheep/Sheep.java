package fr.euphyllia.fidorial.server.entity.mob.creature.sheep;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.ai.PathPenalty;
import fr.euphyllia.fidorial.server.entity.ai.goal.BreedGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.EatBlockGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.FloatGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.FollowParentGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.LookAtPlayerGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.PanicGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.RandomLookAroundGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.RandomStrollGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.TemptGoal;
import fr.euphyllia.fidorial.server.entity.mob.AbstractAnimal;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.item.DyeColors;
import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.ItemEntity;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.item.DyeColor;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.keys.BiomeTagKeys;
import fr.fidorial.registry.keys.DamageTypeTagKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.ItemTagKeys;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.biome.BiomeDefinition;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The sheep, as in Vanilla: it strolls, grazes, looks at players, follows wheat, panics when hurt
 * and breeds when fed wheat. Its wool is sheared and grows back when it grazes, takes the colour of
 * a dye, and comes in the colours of the biome it spawned in.
 */
public final class Sheep extends AbstractAnimal implements Category.Neutral {

    public static final float MAX_HEALTH = 8f;

    private static final double ADULT_HEIGHT = 1.3;
    private static final double ADULT_WIDTH = 0.9;
    private static final double ADULT_EYE_HEIGHT = 1.235;
    private static final double BABY_HEIGHT = 0.65;
    private static final double BABY_WIDTH = 0.45;
    private static final double BABY_EYE_HEIGHT = 0.65625;

    /**
     * Vanilla's {@code movement_speed} attribute of the sheep; the goals multiply it by their
     * modifier.
     */
    private static final double MOVEMENT_SPEED = 0.23;
    private static final double PANIC_SPEED = MOVEMENT_SPEED * 1.25;
    private static final double BREED_SPEED = MOVEMENT_SPEED;
    private static final double TEMPT_SPEED = MOVEMENT_SPEED * 1.1;
    private static final double FOLLOW_PARENT_SPEED = MOVEMENT_SPEED * 1.1;
    private static final double STROLL_SPEED = MOVEMENT_SPEED;

    private static final double LOOK_AT_PLAYER_RANGE = 6.0;
    private static final float STEP_VOLUME = 0.15f;

    /** Sheep entity data, after the 18 entries every ageable mob has: its colour, and whether it is sheared. */
    private static final int MD_WOOL = 18;
    private static final int COLOR_MASK = 0x0F;
    private static final int SHEARED_FLAG = 0x10;

    private static final int MIN_WOOL_WHEN_SHEARED = 1;
    private static final int MAX_WOOL_WHEN_SHEARED = 3;
    private static final int MIN_MUTTON = 1;
    private static final int MAX_MUTTON = 2;
    /** How much older a lamb gets when it grazes, in seconds. */
    private static final int GRAZE_GROWTH_SECONDS = 60;

    private final PanicGoal panicGoal;
    private final EatBlockGoal eatBlockGoal;

    private DyeColor color = DyeColor.WHITE;
    private boolean sheared;

    /**
     * Spawns a sheep as vanilla does out of nowhere, with a spawn egg or {@code /summon}: an adult,
     * of one of the colours of the biome.
     */
    public Sheep(final int entityId, final Location location) {
        this(entityId, location, false);
        this.color = randomColor(BlockView.biomeAt(serverWorld(),
                (int) Math.floor(location.x()), (int) Math.floor(location.y()), (int) Math.floor(location.z())));
    }

    /**
     * Creates a white sheep, as vanilla does before it sets up a sheep.
     */
    public Sheep(final int entityId, final Location location, final boolean baby) {
        super(entityId, UUID.randomUUID(), EntityTypes.SHEEP, location, MAX_HEALTH, baby);
        navigation.setPathPenalty(PathPenalty.LAND_ANIMAL);

        this.panicGoal = new PanicGoal(this, 1, PANIC_SPEED, DamageTypeTagKeys.PANIC_CAUSES);
        this.eatBlockGoal = new EatBlockGoal(this, 5);
        goals.add(new FloatGoal(this, 0));
        goals.add(panicGoal);
        goals.add(new BreedGoal(this, 2, BREED_SPEED));
        goals.add(new TemptGoal(this, 3, TEMPT_SPEED, this::isFood));
        goals.add(new FollowParentGoal(this, 4, FOLLOW_PARENT_SPEED));
        goals.add(eatBlockGoal);
        goals.add(new RandomStrollGoal(this, 6, STROLL_SPEED));
        goals.add(new LookAtPlayerGoal(this, 7, LOOK_AT_PLAYER_RANGE));
        goals.add(new RandomLookAroundGoal(this, 8));
    }

    /**
     * {@return {@code true} if sheep may spawn on a block}: as any animal, on grass in the light
     */
    public static boolean canSpawn(final ServerWorld world, final int x, final int y, final int z) {
        return checkAnimalSpawnRules(world, x, y, z);
    }

    /**
     * {@return a colour for a sheep that spawns in a biome}, as in Vanilla: 82% the colour of the
     * biome (white, brown where farm animals are warm, black where they are cold), one of those in
     * 500 pink instead, and the rest shared between black, gray, light gray, brown and white
     */
    public DyeColor randomColor(final @Nullable TypedKey<BiomeDefinition> biome) {
        final ServerRegistry<BiomeDefinition> biomes = server().registries().registry(RegistryKey.BIOME);
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final int roll = random.nextInt(100);
        if (biome != null && biomes.isTagged(biome, BiomeTagKeys.SPAWNS_WARM_VARIANT_FARM_ANIMALS)) {
            return pick(roll, random, DyeColor.GRAY, DyeColor.LIGHT_GRAY, DyeColor.WHITE, DyeColor.BLACK, DyeColor.BROWN);
        }
        if (biome != null && biomes.isTagged(biome, BiomeTagKeys.SPAWNS_COLD_VARIANT_FARM_ANIMALS)) {
            return pick(roll, random, DyeColor.LIGHT_GRAY, DyeColor.GRAY, DyeColor.WHITE, DyeColor.BROWN, DyeColor.BLACK);
        }
        return pick(roll, random, DyeColor.BLACK, DyeColor.GRAY, DyeColor.LIGHT_GRAY, DyeColor.BROWN, DyeColor.WHITE);
    }

    /**
     * Vanilla's weights out of 100: 5, 5, 5 and 3 for the four rare colours, 82 for the common one.
     */
    private static DyeColor pick(final int roll, final ThreadLocalRandom random, final DyeColor five, final DyeColor ten,
                                 final DyeColor fifteen, final DyeColor eighteen, final DyeColor common) {
        if (roll < 5) {
            return five;
        } else if (roll < 10) {
            return ten;
        } else if (roll < 15) {
            return fifteen;
        } else if (roll < 18) {
            return eighteen;
        }
        return random.nextInt(500) == 0 ? DyeColor.PINK : common;
    }

    @Override
    protected double adultHeight() {
        return ADULT_HEIGHT;
    }

    @Override
    protected double adultWidth() {
        return ADULT_WIDTH;
    }

    @Override
    protected double babyHeight() {
        return BABY_HEIGHT;
    }

    @Override
    protected double babyWidth() {
        return BABY_WIDTH;
    }

    @Override
    public double eyeHeight() {
        return isBaby() ? BABY_EYE_HEIGHT : ADULT_EYE_HEIGHT;
    }

    @Override
    public double movementSpeed() {
        return MOVEMENT_SPEED;
    }

    @Override
    protected double defaultFollowRange() {
        return 0.0;
    }

    public DyeColor color() {
        return color;
    }

    public void setColor(final DyeColor color) {
        this.color = color;
        sendToTrackers(woolMetadata());
    }

    public boolean isSheared() {
        return sheared;
    }

    public void setSheared(final boolean sheared) {
        this.sheared = sheared;
        sendToTrackers(woolMetadata());
    }

    /**
     * {@return {@code true} while this sheep has its head down, grazing}
     */
    public boolean isGrazing() {
        return eatBlockGoal.eatAnimationTick() > 0;
    }

    private ClientboundSetEntityMetadataPacket woolMetadata() {
        final int wool = color.id() & COLOR_MASK | (sheared ? SHEARED_FLAG : 0);
        return ClientboundSetEntityMetadataPacket.of(entityId(),
                ClientboundSetEntityMetadataPacket.Entry.ofByte(MD_WOOL, (byte) wool));
    }

    @Override
    public void sendSpawnPackets(final ClientConnection connection) {
        super.sendSpawnPackets(connection);
        connection.send(woolMetadata());
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return server().registries().registry(RegistryKey.ITEM).isTagged(stack.id(), ItemTagKeys.SHEEP_FOOD);
    }

    @Override
    protected boolean interact(final ServerPlayer player, final EquipmentSlotGroup hand) {
        final int slot = handSlot(player, hand);
        final ItemStack held = player.inventory().get(slot);
        if (held.isEmpty()) {
            return super.interact(player, hand);
        }
        final TypedKey<Item> item = held.id();
        if (item.equals(ItemKeys.SHEARS)) {
            if (readyForShearing()) {
                shear();
                server().debug().gameEvents().emit(world(), GameEventKeys.SHEAR, location());
                wearItem(player, slot);
            }
            // as in Vanilla, shears on a sheep without wool do nothing else either
            return true;
        }
        if (!sheared && server().registries().registry(RegistryKey.ITEM).isTagged(item, ItemTagKeys.SHEEP_WOOL_DYES)) {
            final DyeColor dye = DyeColors.colorOfDye(item);
            if (dye != null && dye != color) {
                final Location at = location();
                // the player hears the sound from their own client, which predicts the click
                server().players().broadcastNear(world(), at.x(), at.y(), at.z(), new ClientboundSoundPacket(
                        Sound.sound(SoundEvents.DYE_USE, Sound.Source.PLAYER, 1.0f, 1.0f), at.x(), at.y(), at.z()), player);
                setColor(dye);
                consumeItem(player, slot);
            }
            return true;
        }
        return super.interact(player, hand);
    }

    public boolean readyForShearing() {
        return !sheared && !isBaby();
    }

    /**
     * Shears this sheep, as in Vanilla: one to three blocks of its wool fall from its back one by
     * one, and it stays bare until it grazes.
     */
    public void shear() {
        final Location at = location();
        sendToTrackers(new ClientboundSoundPacket(
                Sound.sound(SoundEvents.SHEEP_SHEAR, Sound.Source.PLAYER, 1.0f, 1.0f), at.x(), at.y(), at.z()));
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final ServerWorld world = serverWorld();
        final int wool = random.nextInt(MIN_WOOL_WHEN_SHEARED, MAX_WOOL_WHEN_SHEARED + 1);
        for (int i = 0; i < wool; i++) {
            // thrown as any item a mob leaves, then pushed a little further
            server().entities().dropItem(world, at.x(), at.y() + 1.0, at.z(), ItemStack.of(DyeColors.wool(color), 1),
                    random.nextDouble() * 0.2 - 0.1 + spread(random),
                    0.2 + random.nextFloat() * 0.05f,
                    random.nextDouble() * 0.2 - 0.1 + spread(random),
                    ItemEntity.DEFAULT_PICKUP_DELAY, null);
        }
        setSheared(true);
    }

    /**
     * {@return the extra push of a block of sheared wool along one axis}: as in Vanilla, the
     * difference of two random numbers, a tenth of a block per tick at most
     */
    private static double spread(final ThreadLocalRandom random) {
        final float first = random.nextFloat();
        final float second = random.nextFloat();
        return (first - second) * 0.1f;
    }

    /**
     * Grows the wool back, and ages a lamb by a minute, as in Vanilla.
     */
    @Override
    public void ate() {
        super.ate();
        setSheared(false);
        if (canAgeUp()) {
            ageUp(GRAZE_GROWTH_SECONDS, false);
        }
    }

    @Override
    protected @Nullable Sheep createOffspring(final AbstractAnimal partner) {
        final Sheep baby = new Sheep(server().entities().ids().allocate(), birthLocation(), true);
        if (partner instanceof final Sheep other) {
            baby.color = DyeColors.mix(color, other.color);
        }
        return baby;
    }

    @Override
    public boolean isPanicking() {
        return panicGoal.isPanicking();
    }

    @Override
    protected void dropLoot() {
        // TODO: Looting adds up to one more mutton per level, once loot knows what killed the mob
        final int mutton = ThreadLocalRandom.current().nextInt(MIN_MUTTON, MAX_MUTTON + 1);
        dropLootItem(ItemStack.of(fireTicks() > 0 ? ItemKeys.COOKED_MUTTON : ItemKeys.MUTTON, mutton));
        if (!sheared) {
            dropLootItem(ItemStack.of(DyeColors.wool(color), 1));
        }
    }

    @Override
    protected void onStep() {
        playSound(SoundEvents.SHEEP_STEP, STEP_VOLUME, 1.0f);
    }

    @Override
    protected Sound.Type ambientSound() {
        return SoundEvents.SHEEP_AMBIENT;
    }

    @Override
    protected Sound.Type hurtSound() {
        return SoundEvents.SHEEP_HURT;
    }

    @Override
    protected Sound.Type deathSound() {
        return SoundEvents.SHEEP_DEATH;
    }

    @Override
    public void saveAdditional(final CompoundBinaryTag.Builder tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Sheared", sheared);
        tag.putByte("Color", (byte) color.id());
    }

    @Override
    public void loadAdditional(final CompoundBinaryTag tag) {
        super.loadAdditional(tag);
        this.sheared = tag.getBoolean("Sheared", false);
        this.color = DyeColor.byId(tag.getByte("Color", (byte) 0));
    }
}
