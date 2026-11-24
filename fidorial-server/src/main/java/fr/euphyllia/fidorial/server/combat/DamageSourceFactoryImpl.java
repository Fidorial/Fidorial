package fr.euphyllia.fidorial.server.combat;

import com.google.common.base.Preconditions;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.combat.DamageSourceFactory;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.player.Player;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.DamageType;
import fr.fidorial.registry.keys.DamageTypeKeys;
import org.jspecify.annotations.Nullable;

/**
 * Creates the damage sources of the server. Stateless: one instance serves every thread.
 */
public final class DamageSourceFactoryImpl implements DamageSourceFactory {

    public static final DamageSourceFactoryImpl INSTANCE = new DamageSourceFactoryImpl();

    private final DamageSource fall = environmental(DamageTypeKeys.FALL);
    private final DamageSource drown = environmental(DamageTypeKeys.DROWN);
    private final DamageSource lava = environmental(DamageTypeKeys.LAVA);
    private final DamageSource inFire = environmental(DamageTypeKeys.IN_FIRE);
    private final DamageSource onFire = environmental(DamageTypeKeys.ON_FIRE);
    private final DamageSource cactus = environmental(DamageTypeKeys.CACTUS);
    private final DamageSource starve = environmental(DamageTypeKeys.STARVE);
    private final DamageSource outOfWorld = environmental(DamageTypeKeys.OUT_OF_WORLD);
    private final DamageSource freeze = environmental(DamageTypeKeys.FREEZE);
    private final DamageSource magic = environmental(DamageTypeKeys.MAGIC);
    private final DamageSource wither = environmental(DamageTypeKeys.WITHER);
    private final DamageSource generic = environmental(DamageTypeKeys.GENERIC);
    private final DamageSource genericKill = environmental(DamageTypeKeys.GENERIC_KILL);

    private DamageSourceFactoryImpl() {
    }

    private static DamageSource environmental(final TypedKey<DamageType> type) {
        return new DamageSourceImpl(type, null, null, null, ItemStack.EMPTY);
    }

    private static ItemStack mainHand(final @Nullable Entity entity) {
        return entity instanceof final LivingEntity living ? living.equipment(EquipmentSlot.MAIN_HAND) : ItemStack.EMPTY;
    }

    @Override
    public DamageSource of(final TypedKey<DamageType> type) {
        return environmental(type);
    }

    @Override
    public DamageSource of(final TypedKey<DamageType> type, final @Nullable Entity source) {
        return new DamageSourceImpl(type, source, source, null, ItemStack.EMPTY);
    }

    @Override
    public DamageSource of(final TypedKey<DamageType> type, final @Nullable Entity causing, final @Nullable Entity direct) {
        return new DamageSourceImpl(type, causing, direct, null, ItemStack.EMPTY);
    }

    @Override
    public DamageSource at(final TypedKey<DamageType> type, final Location position) {
        Preconditions.checkArgument(position != null, "The position must not be null");
        return new DamageSourceImpl(type, null, null, position, ItemStack.EMPTY);
    }

    @Override
    public DamageSource playerAttack(final Player attacker) {
        Preconditions.checkArgument(attacker != null, "The attacker must not be null");
        return new DamageSourceImpl(DamageTypeKeys.PLAYER_ATTACK, attacker, attacker, null, mainHand(attacker));
    }

    @Override
    public DamageSource mobAttack(final @Nullable LivingEntity attacker) {
        return new DamageSourceImpl(DamageTypeKeys.MOB_ATTACK, attacker, attacker, null, mainHand(attacker));
    }

    @Override
    public DamageSource arrow(final Entity arrow, final @Nullable Entity shooter) {
        Preconditions.checkArgument(arrow != null, "The arrow must not be null");
        return new DamageSourceImpl(DamageTypeKeys.ARROW, shooter, arrow, null, ItemStack.EMPTY);
    }

    @Override
    public DamageSource trident(final Entity trident, final @Nullable Entity thrower) {
        Preconditions.checkArgument(trident != null, "The trident must not be null");
        return new DamageSourceImpl(DamageTypeKeys.TRIDENT, thrower, trident, null, ItemStack.EMPTY);
    }

    @Override
    public DamageSource maceSmash(final LivingEntity attacker) {
        Preconditions.checkArgument(attacker != null, "The attacker must not be null");
        return new DamageSourceImpl(DamageTypeKeys.MACE_SMASH, attacker, attacker, null, mainHand(attacker));
    }

    @Override
    public DamageSource thorns(final Entity wearer) {
        Preconditions.checkArgument(wearer != null, "The wearer must not be null");
        return new DamageSourceImpl(DamageTypeKeys.THORNS, wearer, wearer, null, ItemStack.EMPTY);
    }

    @Override
    public DamageSource thrown(final @Nullable Entity projectile, final @Nullable Entity thrower) {
        return new DamageSourceImpl(DamageTypeKeys.THROWN, thrower, projectile, null, ItemStack.EMPTY);
    }

    @Override
    public DamageSource fall() {
        return fall;
    }

    @Override
    public DamageSource drown() {
        return drown;
    }

    @Override
    public DamageSource lava() {
        return lava;
    }

    @Override
    public DamageSource inFire() {
        return inFire;
    }

    @Override
    public DamageSource onFire() {
        return onFire;
    }

    @Override
    public DamageSource cactus() {
        return cactus;
    }

    @Override
    public DamageSource starve() {
        return starve;
    }

    @Override
    public DamageSource outOfWorld() {
        return outOfWorld;
    }

    @Override
    public DamageSource freeze() {
        return freeze;
    }

    @Override
    public DamageSource magic() {
        return magic;
    }

    @Override
    public DamageSource wither() {
        return wither;
    }

    @Override
    public DamageSource generic() {
        return generic;
    }

    @Override
    public DamageSource genericKill() {
        return genericKill;
    }
}
