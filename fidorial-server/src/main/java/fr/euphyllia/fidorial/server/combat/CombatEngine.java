package fr.euphyllia.fidorial.server.combat;

import com.google.gson.JsonElement;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMovingMob;
import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.euphyllia.fidorial.server.entity.mob.AngerableMob;
import fr.euphyllia.fidorial.server.entity.physics.BlockCollider;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.entity.projectile.ArrowProjectileEntity;
import fr.euphyllia.fidorial.server.events.entity.EntityBlockAttackEventImpl;
import fr.euphyllia.fidorial.server.events.entity.EntityDamageEventImpl;
import fr.euphyllia.fidorial.server.events.entity.EntityDeathEventImpl;
import fr.euphyllia.fidorial.server.events.entity.EntityResurrectEventImpl;
import fr.euphyllia.fidorial.server.events.player.PlayerAttackEntityEventImpl;
import fr.euphyllia.fidorial.server.events.player.PlayerDeathEventImpl;
import fr.euphyllia.fidorial.server.inventory.CraftingContainer;
import fr.euphyllia.fidorial.server.inventory.InventoryMenu;
import fr.euphyllia.fidorial.server.item.enchantment.EnchantmentEngine;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundAnimatePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundDamageEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundEntityEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundHurtAnimationPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundLevelEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundLevelParticlesPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundPlayerCombatKillPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMotionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.gamerule.GameRuleOverrides;
import fr.fidorial.combat.CombatService;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.combat.DamageSourceFactory;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.EntityType;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.entity.player.Player;
import fr.fidorial.event.entity.EntityBlockAttackEvent;
import fr.fidorial.event.entity.EntityDamageEvent;
import fr.fidorial.event.player.PlayerAttackEntityEvent;
import fr.fidorial.event.player.PlayerDeathEvent;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.DamageType;
import fr.fidorial.registry.keys.AttributeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.registry.keys.DamageTypeTagKeys;
import fr.fidorial.registry.keys.EntityTypeTagKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.ItemTagKeys;
import fr.fidorial.registry.keys.MobEffectKeys;
import fr.fidorial.registry.keys.ParticleTypeKeys;
import fr.fidorial.registry.keys.SoundEventKeys;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.World;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The vanilla combat rules.
 *
 * <p>A melee hit deals the {@code minecraft:attack_damage} of its attacker, which the weapon in hand
 * and the effects change, scaled by how charged the swing is; the enchantments of the weapon add to
 * it. A fully charged swing while falling is a critical hit, one while sprinting knocks back harder,
 * one standing with a sword sweeps the entities around the target, and one with a mace after a fall
 * is a smash attack.</p>
 *
 * <p>A hit then goes through the defences of its victim: a raised shield facing it, the
 * invulnerability left by the last hit, armor, Resistance, protection enchantments and absorption.
 * A lethal hit is stopped by a totem of undying held in a hand.</p>
 */
public final class CombatEngine implements CombatService {

    /**
     * How far beyond its reach the server accepts a hit, for the latency of the attacker.
     */
    private static final double REACH_TOLERANCE = 1.0;

    /**
     * How much farther a player in creative mode reaches.
     */
    private static final double CREATIVE_REACH_BONUS = 2.0;

    /**
     * How hard every hit from somewhere pushes its victim.
     */
    private static final double BASE_KNOCKBACK = 0.4;

    /**
     * How hard a raised shield pushes back the attacker of a melee hit it blocked.
     */
    private static final double BLOCKED_KNOCKBACK = 0.5;

    /**
     * Each point of knockback of a melee hit pushes this much.
     */
    private static final double KNOCKBACK_PER_POINT = 0.5;

    /**
     * How fast an attacker who knocked its victim back keeps going.
     */
    private static final double ATTACKER_SLOWDOWN = 0.6;

    private static final double MAX_UPWARD_KNOCKBACK = 0.4;

    private static final float CRITICAL_MULTIPLIER = 1.5f;

    private static final double SWEEP_REACH_X = 1.0;
    private static final double SWEEP_REACH_Y = 0.25;
    private static final float SWEEP_BASE_DAMAGE = 1.0f;

    private static final double SMASH_RADIUS = 3.5;
    private static final double SMASH_PUSH = 0.7;
    private static final double SMASH_LIFT = 0.7;
    private static final double SMASH_ATTACKER_LIFT = 0.01;
    private static final int SMASH_PARTICLES_EVENT = 2013;
    private static final int SMASH_PARTICLES = 750;

    private static final byte ENTITY_EVENT_DEATH = 3;
    private static final byte ENTITY_EVENT_TOTEM = 35;

    private static final double FORGIVE_RANGE = 32.0;
    private static final double FORGIVE_RANGE_Y = 10.0;

    private static final float FREEZE_HURTS_EXTRA_MULTIPLIER = 5.0f;

    private static final float HELMET_DAMAGE_REDUCTION = 0.75f;

    private static final int INVULNERABILITY_TICKS = AbstractLivingEntity.INVULNERABILITY_TICKS;

    private static final ComponentLogger LOGGER = ComponentLogger.logger(CombatEngine.class);

    private final FidorialServer server;
    private final RangedWeapons items;

    public CombatEngine(final FidorialServer server) {
        this.server = server;
        this.items = new RangedWeapons(server, this);
    }

    /**
     * {@return what makes the items that are used, rather than swung, work: shields, bows,
     * crossbows and tridents}
     */
    public RangedWeapons items() {
        return items;
    }

    @Override
    public DamageSourceFactory sources() {
        return DamageSourceFactoryImpl.INSTANCE;
    }

    // ------------------------------------------------------------------------------------------
    // Melee
    // ------------------------------------------------------------------------------------------

    @Override
    public boolean attack(final LivingEntity attacker, final Entity target) {
        if (attacker instanceof final ServerPlayer player) {
            return playerAttack(player, target);
        }
        if (attacker instanceof final AbstractMob mob && target instanceof final AbstractLivingEntity victim) {
            return mobAttack(mob, victim);
        }
        return false;
    }

    private boolean playerAttack(final ServerPlayer player, final Entity target) {
        if (!(target instanceof final AbstractLivingEntity victim)) {
            return false;
        }
        if (player.gameMode() == GameMode.SPECTATOR || player.isDead() || player.isRemoved()) {
            return false;
        }
        if (!victim.isAlive() || victim == player || !canHarm(player, victim)) {
            return false;
        }
        final ItemStack weapon = player.heldItem();
        final ItemCombatData.Combat combat = ItemCombatData.of(weapon);
        if (!isWithinReach(player, victim, combat.attackRange())) {
            return false;
        }

        final float strength = attackStrengthScale(player);
        if (combat.minimumAttackCharge() > 0f && strength < combat.minimumAttackCharge()) {
            return false;
        }
        final boolean strong = strength > CombatRules.STRONG_HIT_THRESHOLD;
        final boolean smash = isSmashAttack(player, weapon);
        final DamageSource source = meleeSource(player, weapon, combat, smash);

        final float base = attackDamage(player);
        final float enchantmentBonus = (EnchantmentEngine.modifyDamage(player, victim, source, base) - base) * strength;
        float damage = base * CombatRules.attackDamageScale(strength);
        if (smash) {
            damage += smashBonus(player, weapon, victim, source);
        }

        final boolean sprintKnockback = player.isSprinting() && strong;
        final boolean critical = strong && canCriticalHit(player);
        if (critical) {
            damage *= CRITICAL_MULTIPLIER;
        }
        final boolean sweeping = strong && !critical && !sprintKnockback && !smash && canSweep(player, weapon);
        final float sweepBase = damage;
        damage += enchantmentBonus;
        final double knockback = attackKnockback(player)
                + EnchantmentEngine.knockbackBonus(player, victim, source);

        final PlayerAttackEntityEvent event = server.events().post(new PlayerAttackEntityEventImpl(
                player, victim, weapon, damage, knockback, strength, critical, smash, sweeping));
        player.resetAttackCooldown();
        if (event.isCancelled()) {
            return false;
        }

        final float healthBefore = victim.health();
        final boolean hurt = hurt(victim, source, event.damage(), BASE_KNOCKBACK);
        if (!hurt) {
            playAt(player, SoundEventKeys.ENTITY_PLAYER_ATTACK_NODAMAGE.key(), 1.0f, 1.0f);
            return false;
        }

        if (event.knockback() > 0.0) {
            knockbackAlong(victim, event.knockback() * KNOCKBACK_PER_POINT, player.location().yaw());
            player.setSprinting(false);
        }
        if (event.isSweeping()) {
            sweep(player, victim, source, sweepBase, strength);
        }
        if (critical) {
            sendToViewersAndSelf(victim, ClientboundAnimatePacket.criticalHit(victim.entityId()));
        }
        if (enchantmentBonus > 0f) {
            sendToViewersAndSelf(victim, ClientboundAnimatePacket.magicCriticalHit(victim.entityId()));
        }
        playAttackSound(player, strong, critical, sprintKnockback);
        if (smash) {
            afterSmash(player, victim);
        }
        showDamageIndicator(victim, healthBefore - victim.health());
        if (combat.weapon() != null) {
            ItemWear.hurt(player, EquipmentSlot.MAIN_HAND, combat.weapon().itemDamagePerAttack());
        }
        return true;
    }

    /**
     * A thrust of a piercing weapon, such as a spear: every living entity the look of the player
     * crosses, from the least to the most reach of the weapon, takes the hit.
     *
     * @return {@code true} if at least one entity was hurt
     */
    public boolean stab(final ServerPlayer player) {
        if (player.gameMode() == GameMode.SPECTATOR || player.isDead() || player.isRemoved()
                || !(player.world() instanceof final ServerWorld world)) {
            return false;
        }
        final ItemStack weapon = player.heldItem();
        final ItemCombatData.Combat combat = ItemCombatData.of(weapon);
        final ItemCombatData.PiercingWeapon piercing = combat.piercingWeapon();
        if (piercing == null) {
            return false;
        }
        final float strength = attackStrengthScale(player);
        player.resetAttackCooldown();
        if (strength < combat.minimumAttackCharge()) {
            return false;
        }
        if (piercing.sound() != null) {
            playAt(player, piercing.sound(), 1.0f, 1.0f);
        }

        final boolean creative = player.gameMode() == GameMode.CREATIVE;
        final ItemCombatData.AttackRange range = combat.attackRange();
        final double minReach = range == null ? 0.0 : creative ? range.minCreativeReach() : range.minReach();
        final double maxReach = range == null
                ? player.attributeValue(AttributeKeys.ENTITY_INTERACTION_RANGE) + (creative ? CREATIVE_REACH_BONUS : 0.0)
                : creative ? range.maxCreativeReach() : range.maxReach();
        final double margin = range == null ? 0.0 : range.hitboxMargin();
        final Location at = player.location();
        final double eyeY = at.y() + player.eyeHeight();
        final double[] look = Hitboxes.look(at.yaw(), at.pitch());
        final double dx = look[0] * maxReach;
        final double dy = look[1] * maxReach;
        final double dz = look[2] * maxReach;

        final List<AbstractLivingEntity> struck = new ArrayList<>();
        final int chunks = (int) Math.ceil(maxReach / 16.0) + 1;
        world.entityManager().forEachInChunkRange(at.chunk().x(), at.chunk().z(), chunks, entity -> {
            if (!(entity instanceof final AbstractLivingEntity living) || living == player || !living.isAlive()
                    || !living.isOwnedByCurrentThread() || !canHarm(player, living)) {
                return;
            }
            final double enter = Hitboxes.entry(Hitboxes.box(living, margin), at.x(), eyeY, at.z(), dx, dy, dz);
            if (enter >= 0.0 && enter * maxReach >= minReach - REACH_TOLERANCE) {
                struck.add(living);
            }
        });

        final float base = attackDamage(player) * CombatRules.attackDamageScale(strength);
        final TypedKey<DamageType> type = combat.damageType();
        boolean hurtAny = false;
        for (final AbstractLivingEntity victim : struck) {
            final DamageSource source = type == null
                    ? DamageSourceFactoryImpl.INSTANCE.playerAttack(player)
                    : DamageSourceFactoryImpl.INSTANCE.of(type, player).withWeapon(weapon);
            final float damage = base + (EnchantmentEngine.modifyDamage(player, victim, source, attackDamage(player))
                    - attackDamage(player)) * strength;
            if (hurt(victim, source, damage, piercing.dealsKnockback() ? BASE_KNOCKBACK : 0.0)) {
                hurtAny = true;
                if (piercing.hitSound() != null) {
                    playAt(victim, piercing.hitSound(), 1.0f, 1.0f);
                }
            }
        }
        if (hurtAny && combat.weapon() != null) {
            ItemWear.hurt(player, EquipmentSlot.MAIN_HAND, combat.weapon().itemDamagePerAttack());
        }
        return hurtAny;
    }

    private boolean mobAttack(final AbstractMob mob, final AbstractLivingEntity victim) {
        if (mob.isDead() || mob.isRemoved() || !victim.isAlive()) {
            return false;
        }
        final DamageSource source = DamageSourceFactoryImpl.INSTANCE.mobAttack(mob);
        final float damage = EnchantmentEngine.modifyDamage(mob, victim, source, attackDamage(mob));
        if (damage <= 0f) {
            return false;
        }
        final boolean hurt = hurt(victim, source, damage, BASE_KNOCKBACK);
        if (!hurt) {
            return false;
        }
        final double knockback = attackKnockback(mob) + EnchantmentEngine.knockbackBonus(mob, victim, source);
        if (knockback > 0.0) {
            knockbackAlong(victim, knockback * KNOCKBACK_PER_POINT, yawOf(mob));
            if (mob instanceof final AbstractMovingMob moving) {
                moving.setVelocity(moving.velocityX() * ATTACKER_SLOWDOWN, moving.velocityY(),
                        moving.velocityZ() * ATTACKER_SLOWDOWN);
            }
        }
        return true;
    }

    private static DamageSource meleeSource(final ServerPlayer player, final ItemStack weapon,
                                            final ItemCombatData.Combat combat, final boolean smash) {
        if (smash) {
            return DamageSourceFactoryImpl.INSTANCE.maceSmash(player);
        }
        final TypedKey<DamageType> type = combat.damageType();
        return type == null
                ? DamageSourceFactoryImpl.INSTANCE.playerAttack(player)
                : DamageSourceFactoryImpl.INSTANCE.of(type, player).withWeapon(weapon);
    }

    @Override
    public float attackDamage(final LivingEntity attacker) {
        return (float) attacker.attributeValue(AttributeKeys.ATTACK_DAMAGE);
    }

    @Override
    public double attackKnockback(final LivingEntity attacker) {
        final double attribute = attacker.attributeValue(AttributeKeys.ATTACK_KNOCKBACK);
        final boolean sprinting = attacker instanceof final ServerPlayer player
                && player.isSprinting() && attackStrengthScale(player) > CombatRules.STRONG_HIT_THRESHOLD;
        return attribute + (sprinting ? 1.0 : 0.0);
    }

    @Override
    public float attackStrengthScale(final Player attacker) {
        if (!(attacker instanceof final ServerPlayer player)) {
            return 1.0f;
        }
        return CombatRules.attackStrength(player.ticksSinceLastAttack(),
                CombatRules.attackCooldown(player.attributeValue(AttributeKeys.ATTACK_SPEED)));
    }

    @Override
    public int attackCooldownTicks(final Player attacker) {
        return Math.max(1, Math.round(CombatRules.attackCooldown(attacker.attributeValue(AttributeKeys.ATTACK_SPEED))));
    }

    @Override
    public boolean pvpEnabled(final World world) {
        return server.worlds().manager().configurations().resolve(world.key()).gameplay().pvp()
                && ((ServerWorld) world).gameRuleValues().getBoolean(GameRuleKeys.PVP);
    }

    /**
     * {@return {@code true} if an entity may hurt another}: players only hurt each other where PvP
     * is allowed, and never one in creative or spectator mode
     */
    public boolean canHarm(final @Nullable Entity attacker, final AbstractLivingEntity victim) {
        if (!(victim instanceof final ServerPlayer other)) {
            return true;
        }
        if (other.isInvulnerableToDamage()) {
            return false;
        }
        return !(attacker instanceof Player) || attacker == victim || pvpEnabled(other.world());
    }

    private boolean isWithinReach(final ServerPlayer attacker, final AbstractLivingEntity victim,
                                  final ItemCombatData.@Nullable AttackRange range) {
        final boolean creative = attacker.gameMode() == GameMode.CREATIVE;
        final double maxReach;
        final double minReach;
        final double margin;
        if (range == null) {
            maxReach = attacker.attributeValue(AttributeKeys.ENTITY_INTERACTION_RANGE) + (creative ? CREATIVE_REACH_BONUS : 0.0);
            minReach = 0.0;
            margin = 0.0;
        } else {
            maxReach = creative ? range.maxCreativeReach() : range.maxReach();
            minReach = creative ? range.minCreativeReach() : range.minReach();
            margin = range.hitboxMargin();
        }
        final Location from = attacker.location();
        final double distance = Hitboxes.distanceTo(Hitboxes.box(victim, margin),
                from.x(), from.y() + attacker.eyeHeight(), from.z());
        return distance <= maxReach + REACH_TOLERANCE && distance >= minReach - REACH_TOLERANCE;
    }

    private boolean canCriticalHit(final ServerPlayer attacker) {
        return attacker.fallDistance() > 0.0
                && !attacker.onGround()
                && !attacker.isSprinting()
                && !attacker.hasEffect(MobEffectKeys.BLINDNESS)
                && !isClimbing(attacker)
                && !isInWater(attacker);
    }

    private boolean canSweep(final ServerPlayer attacker, final ItemStack weapon) {
        return !weapon.isEmpty()
                && attacker.onGround()
                && attacker.knownSpeed() < attacker.attributeValue(AttributeKeys.MOVEMENT_SPEED)
                && server.registries().registry(RegistryKey.ITEM).isTagged(weapon.id(), ItemTagKeys.SWORDS);
    }

    private static boolean isSmashAttack(final ServerPlayer attacker, final ItemStack weapon) {
        return weapon.id().equals(ItemKeys.MACE)
                && attacker.fallDistance() > CombatRules.SMASH_MIN_FALL
                && !attacker.isFlying();
    }

    private static float smashBonus(final ServerPlayer attacker, final ItemStack weapon, final AbstractLivingEntity victim,
                                    final DamageSource source) {
        final double fall = attacker.fallDistance();
        final double perBlock = EnchantmentEngine.smashDamagePerFallenBlock(attacker, weapon, victim, source);
        return (float) (CombatRules.smashBonus(fall) + perBlock * fall);
    }

    /**
     * After a smash attack: the attacker stops falling and takes no fall damage, the entities
     * around the target are thrown away from it, and the ground shakes.
     */
    private void afterSmash(final ServerPlayer attacker, final AbstractLivingEntity target) {
        final double fall = attacker.fallDistance();
        final boolean heavy = fall > CombatRules.HEAVY_SMASH_FALL;
        attacker.setFallDistance(0.0);
        attacker.push(0.0, SMASH_ATTACKER_LIFT, 0.0);

        final Location center = target.location();
        final List<AbstractLivingEntity> around = livingAround(target, SMASH_RADIUS, SMASH_RADIUS, SMASH_RADIUS);
        for (final AbstractLivingEntity entity : around) {
            if (entity == attacker || entity == target || !entity.isAlive()
                    || (entity instanceof final ServerPlayer player
                    && (player.gameMode() == GameMode.SPECTATOR || (player.gameMode() == GameMode.CREATIVE && player.isFlying())))) {
                continue;
            }
            final Location at = entity.location();
            final double dx = at.x() - center.x();
            final double dy = at.y() - center.y();
            final double dz = at.z() - center.z();
            final double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            final double power = (SMASH_RADIUS - distance) * SMASH_PUSH * (heavy ? 2.0 : 1.0)
                    * (1.0 - Math.clamp(entity.knockbackResistance(), 0.0, 1.0));
            if (power <= 0.0 || distance < 1.0E-4) {
                continue;
            }
            addVelocity(entity, dx / distance * power, SMASH_LIFT, dz / distance * power);
        }

        final boolean grounded = target instanceof final AbstractMovingMob mob ? mob.onGround()
                : target instanceof final ServerPlayer player && player.onGround();
        final Key sound = !grounded ? SoundEventKeys.ITEM_MACE_SMASH_AIR.key()
                : heavy ? SoundEventKeys.ITEM_MACE_SMASH_GROUND_HEAVY.key() : SoundEventKeys.ITEM_MACE_SMASH_GROUND.key();
        playAt(attacker, sound, 1.0f, 1.0f);
        if (grounded) {
            final BlockPosition below = Position.block((int) Math.floor(center.x()), (int) Math.floor(center.y() - 0.2),
                    (int) Math.floor(center.z()));
            server.players().broadcastNear(target.world(), center.x(), center.y(), center.z(),
                    new ClientboundLevelEventPacket(SMASH_PARTICLES_EVENT, below, SMASH_PARTICLES, false), null);
        }
    }

    /**
     * A sweep: the living entities around the target take {@code 1 + sweeping_damage_ratio × damage},
     * changed by the enchantments of the weapon, and are pushed along the look of the attacker.
     */
    private void sweep(final ServerPlayer attacker, final AbstractLivingEntity primary, final DamageSource source,
                       final float damage, final float strength) {
        final float sweepDamage = SWEEP_BASE_DAMAGE
                + (float) attacker.attributeValue(AttributeKeys.SWEEPING_DAMAGE_RATIO) * damage;
        final double reach = attacker.attributeValue(AttributeKeys.ENTITY_INTERACTION_RANGE);
        final Location from = attacker.location();
        for (final AbstractLivingEntity nearby : livingAround(primary, SWEEP_REACH_X, SWEEP_REACH_Y, SWEEP_REACH_X)) {
            if (nearby == primary || nearby == attacker || !nearby.isAlive() || !canHarm(attacker, nearby)) {
                continue;
            }
            final Location at = nearby.location();
            final double dx = at.x() - from.x();
            final double dy = at.y() - from.y();
            final double dz = at.z() - from.z();
            if (dx * dx + dy * dy + dz * dz >= reach * reach) {
                continue;
            }
            knockbackAlong(nearby, BASE_KNOCKBACK, attacker.location().yaw());
            hurt(nearby, source, EnchantmentEngine.modifyDamage(attacker, nearby, source, sweepDamage) * strength,
                    BASE_KNOCKBACK);
        }
        playAt(attacker, SoundEventKeys.ENTITY_PLAYER_ATTACK_SWEEP.key(), 1.0f, 1.0f);
        final double[] look = Hitboxes.look(from.yaw(), 0f);
        final double x = from.x() + look[0];
        final double z = from.z() + look[2];
        final double y = from.y() + Hitboxes.height(attacker) * 0.5;
        server.players().broadcastNear(attacker.world(), x, y, z,
                new ClientboundLevelParticlesPacket(ParticleTypeKeys.SWEEP_ATTACK, x, y, z, 0f, 0f, 0f, 0f, 1), null);
    }

    /**
     * {@return the living entities whose box touches the box of {@code center} grown by the given
     * margins}, owned by the current thread
     */
    private List<AbstractLivingEntity> livingAround(final AbstractLivingEntity center, final double marginX,
                                                    final double marginY, final double marginZ) {
        final List<AbstractLivingEntity> found = new ArrayList<>();
        if (!(center.world() instanceof final ServerWorld world)) {
            return found;
        }
        final BlockCollider.Box centerBox = Hitboxes.box(center, 0.0);
        final BlockCollider.Box area = new BlockCollider.Box(
                centerBox.minX() - marginX, centerBox.minY() - marginY, centerBox.minZ() - marginZ,
                centerBox.maxX() + marginX, centerBox.maxY() + marginY, centerBox.maxZ() + marginZ);
        final int range = (int) Math.ceil(Math.max(marginX, marginZ) / 16.0) + 1;
        world.entityManager().forEachInChunkRange(center.location().chunk().x(), center.location().chunk().z(), range,
                entity -> {
                    if (entity instanceof final AbstractLivingEntity living && !living.isRemoved()
                            && living.isOwnedByCurrentThread()
                            && Hitboxes.overlap(area, Hitboxes.box(living, 0.0))) {
                        found.add(living);
                    }
                });
        return found;
    }

    private static boolean isClimbing(final ServerPlayer player) {
        if (!(player.world() instanceof final ServerWorld world)) {
            return false;
        }
        final Location at = player.location();
        final BlockState state = BlockView.blockAt(world, (int) Math.floor(at.x()), (int) Math.floor(at.y()),
                (int) Math.floor(at.z()));
        return state != null && state.isTagged(BlockTypeTagKeys.CLIMBABLE);
    }

    private static boolean isInWater(final ServerPlayer player) {
        final Location at = player.location();
        return player.world() instanceof final ServerWorld world && BlockView.isInWater(world, at.x(), at.y(), at.z());
    }

    private void playAttackSound(final ServerPlayer attacker, final boolean strong, final boolean critical,
                                 final boolean sprintKnockback) {
        final Key sound;
        if (sprintKnockback) {
            sound = SoundEventKeys.ENTITY_PLAYER_ATTACK_KNOCKBACK.key();
        } else if (critical) {
            sound = SoundEventKeys.ENTITY_PLAYER_ATTACK_CRIT.key();
        } else if (strong) {
            sound = SoundEventKeys.ENTITY_PLAYER_ATTACK_STRONG.key();
        } else {
            sound = SoundEventKeys.ENTITY_PLAYER_ATTACK_WEAK.key();
        }
        playAt(attacker, sound, 1.0f, 1.0f);
    }

    private void showDamageIndicator(final AbstractLivingEntity victim, final float dealt) {
        final int count = (int) (dealt * 0.5f);
        if (count <= 0) {
            return;
        }
        final Location at = victim.location();
        final double y = at.y() + Hitboxes.height(victim) * 0.5;
        server.players().broadcastNear(victim.world(), at.x(), y, at.z(),
                new ClientboundLevelParticlesPacket(ParticleTypeKeys.DAMAGE_INDICATOR, at.x(), y, at.z(),
                        0.1f, 0f, 0.1f, 0.2f, count), null);
    }

    // ------------------------------------------------------------------------------------------
    // Damage
    // ------------------------------------------------------------------------------------------

    @Override
    public boolean damage(final LivingEntity target, final DamageSource source, final float amount) {
        if (!(target instanceof final AbstractLivingEntity victim)) {
            return false;
        }
        return hurt(victim, source, amount, BASE_KNOCKBACK);
    }

    /**
     * Hurts an entity, through every defence it has.
     *
     * @param knockback how hard the hit pushes the victim away from where it comes from
     * @return {@code true} if the hit hurt the victim: {@code false} when it was refused, absorbed
     *         by the invulnerability of the last hit or blocked by a shield
     */
    // FIXME: the caller may not own the victim: a hit across a region border changes it from another thread
    public boolean hurt(final AbstractLivingEntity victim, final DamageSource source, final float amount,
                        final double knockback) {
        if (victim instanceof final AbstractMob mob) {
            // a mob may dodge the hit, or act whether it hurt or not
            return mob.takeHit(source, amount, knockback, () -> hurtUnchecked(victim, source, amount, knockback));
        }
        return hurtUnchecked(victim, source, amount, knockback);
    }

    private boolean hurtUnchecked(final AbstractLivingEntity victim, final DamageSource source, final float rawAmount,
                                  final double knockback) {
        if (rawAmount <= 0f || victim.isRemoved() || victim.isDead()) {
            return false;
        }
        if (victim instanceof final ServerPlayer player
                && (player.isAwaitingRespawn()
                || (player.isInvulnerableToDamage() && !source.is(DamageTypeTagKeys.BYPASSES_INVULNERABILITY)))) {
            return false;
        }
        if (victim instanceof ServerPlayer && isDisabledByGameRule(victim, source)) {
            return false;
        }
        if (EnchantmentEngine.isImmune(victim, source)) {
            return false;
        }
        if (source.is(DamageTypeTagKeys.IS_FIRE)
                && (victim.isFireImmune() || victim.hasEffect(MobEffectKeys.FIRE_RESISTANCE))) {
            return false;
        }
        float amount = rawAmount;
        if (source.is(DamageTypeTagKeys.IS_FREEZING)) {
            final ServerRegistry<EntityType> entityTypes = server.registries().registry(RegistryKey.ENTITY_TYPE);
            final TypedKey<EntityType> victimType = victim.type().key();
            if (entityTypes.isTagged(victimType, EntityTypeTagKeys.IMMUNE_TO_FREEZING)) {
                return false;
            }
            if (entityTypes.isTagged(victimType, EntityTypeTagKeys.FREEZE_HURTS_EXTRA_TYPES)) {
                amount *= FREEZE_HURTS_EXTRA_MULTIPLIER;
            }
        }
        if (victim instanceof ServerPlayer) {
            amount = scaleWithDifficulty(source, amount);
            if (amount <= 0f) {
                return false;
            }
        }

        final EntityDamageEvent event = server.events().post(new EntityDamageEventImpl(victim, source, amount, knockback));
        if (event.isCancelled() || event.damage() <= 0f) {
            return false;
        }
        float damage = event.damage();

        // a raised shield facing the hit takes its share first
        final float blocked = block(victim, source, damage);
        final boolean shielded = blocked > 0f;
        damage -= blocked;

        // the invulnerability left by the last hit only lets through what a stronger hit adds
        final boolean withinInvulnerability = !source.is(DamageTypeTagKeys.BYPASSES_COOLDOWN)
                && victim.invulnerableTicks() > AbstractLivingEntity.INVULNERABILITY_OVERRIDE_THRESHOLD;
        final float incoming;
        if (withinInvulnerability) {
            if (damage <= victim.lastDamage()) {
                return false;
            }
            incoming = damage - victim.lastDamage();
        } else {
            incoming = damage;
            victim.setInvulnerableTicks(INVULNERABILITY_TICKS);
        }
        victim.setLastDamage(damage);

        final float reduced = incoming > 0f ? reduce(victim, source, incoming) : 0f;
        final float dealt = soakWithAbsorption(victim, reduced);

        if (!withinInvulnerability) {
            if (shielded) {
                // a blocked hit does not flash the victim red
                LOGGER.debug("{} blocked {} of a hit of {}", victim, blocked, source);
            } else {
                broadcastHurt(victim, source);
            }
        }
        if (victim instanceof final AbstractMob mob) {
            mob.rememberDamage(source);
        }
        if (dealt > 0f) {
            victim.setHealth(victim.health() - dealt);
        }
        syncHealth(victim);
        if (reduced > 0f) {
            server.debug().gameEvents().emit(victim.world(), GameEventKeys.ENTITY_DAMAGE, victim.location());
        }

        if (!withinInvulnerability && event.knockback() > 0.0 && !source.is(DamageTypeTagKeys.NO_KNOCKBACK)
                && !(shielded && damage <= 0f && victim instanceof ServerPlayer)
                && !(victim instanceof final AbstractMob mob && mob.knockBack(source, amount, event.knockback()))) {
            knockbackFrom(victim, source, event.knockback());
        }

        final Entity causing = source.causingEntity();
        if (causing != null && causing != victim && !(shielded && damage <= 0f)) {
            final boolean melee = source.isDirect() && causing instanceof LivingEntity;
            if (melee || source.is(DamageTypeTagKeys.IS_PROJECTILE)) {
                // Fire Aspect, Thorns, Bane of Arthropods... the effects of the attacker only follow
                // a melee hit, those of the victim any attack.
                EnchantmentEngine.postAttack(victim, source, dealt, melee);
            }
        }

        if (victim.isDead()) {
            if (!source.is(DamageTypeTagKeys.BYPASSES_INVULNERABILITY) && resurrect(victim, source)) {
                return !shielded;
            }
            if (!withinInvulnerability && victim instanceof final AbstractMob mob) {
                mob.playDeathSound();
            }
            die(victim, causing);
        } else if (victim instanceof final AbstractMob mob) {
            // a hit within the cooldown only deals what it adds to the last one: it makes no sound
            if (!withinInvulnerability && !shielded && !source.is(DamageTypeTagKeys.NO_HURT_SOUND)) {
                mob.playHurtSound();
            }
            mob.notifyHurt(source, dealt);
            aggro(mob, causing);
        }
        return !shielded;
    }

    private float scaleWithDifficulty(final DamageSource source, final float amount) {
        if (!scalesWithDifficulty(source)) {
            return amount;
        }
        return switch (server.worlds().manager().levelData().difficulty) {
            case 0 -> 0f;
            case 1 -> Math.min(amount / 2f + 1f, amount);
            case 3 -> amount * 1.5f;
            default -> amount;
        };
    }

    private boolean isDisabledByGameRule(final AbstractLivingEntity victim, final DamageSource source) {
        final GameRuleOverrides rules = gameRulesOf(victim);
        if (source.is(DamageTypeTagKeys.IS_DROWNING)) {
            return !rules.getBoolean(GameRuleKeys.DROWNING_DAMAGE);
        }
        if (source.is(DamageTypeTagKeys.IS_FALL)) {
            return !rules.getBoolean(GameRuleKeys.FALL_DAMAGE);
        }
        if (source.is(DamageTypeTagKeys.IS_FIRE)) {
            return !rules.getBoolean(GameRuleKeys.FIRE_DAMAGE);
        }
        if (source.is(DamageTypeTagKeys.IS_FREEZING)) {
            return !rules.getBoolean(GameRuleKeys.FREEZE_DAMAGE);
        }
        return false;
    }

    private GameRuleOverrides gameRulesOf(final AbstractLivingEntity entity) {
        return ((ServerWorld) entity.world()).gameRuleValues();
    }

    /**
     * {@return the damage left once the defences of the victim took their share}: a helmet against
     * falling blocks, armor, which wears, then Resistance, then protection enchantments
     */
    private float reduce(final AbstractLivingEntity victim, final DamageSource source, final float amount) {
        float damage = amount;
        if (source.is(DamageTypeTagKeys.DAMAGES_HELMET) && !victim.equipment(EquipmentSlot.HEAD).isEmpty()) {
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            ItemWear.hurt(victim, EquipmentSlot.HEAD, (int) (damage * 4.0f + random.nextFloat() * damage * 2.0f));
            damage *= HELMET_DAMAGE_REDUCTION;
        }
        if (!source.is(DamageTypeTagKeys.BYPASSES_ARMOR)) {
            wearArmor(victim, damage);
            damage = CombatRules.damageAfterArmor(damage, (float) victim.armor(), (float) victim.armorToughness(),
                    share -> EnchantmentEngine.armorEffectiveness(victim, source, share));
        }
        if (source.is(DamageTypeTagKeys.BYPASSES_EFFECTS)) {
            return Math.max(0f, damage);
        }
        if (!source.is(DamageTypeTagKeys.BYPASSES_RESISTANCE)) {
            final MobEffectInstance resistance = victim.effect(MobEffectKeys.RESISTANCE).orElse(null);
            if (resistance != null) {
                damage *= CombatRules.resistanceMultiplier(resistance.amplifier());
            }
        }
        if (damage <= 0f) {
            return 0f;
        }
        if (!source.is(DamageTypeTagKeys.BYPASSES_ENCHANTMENTS)) {
            damage = CombatRules.damageAfterProtection(damage, EnchantmentEngine.protection(victim, source));
        }
        return Math.max(0f, damage);
    }

    /**
     * Wears every armor piece of the victim of a hit: a quarter of the damage, one at least.
     */
    private static void wearArmor(final AbstractLivingEntity victim, final float damage) {
        if (damage <= 0f) {
            return;
        }
        final int wear = (int) Math.max(1.0f, damage / 4.0f);
        for (final EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isHumanoidArmor() && !ItemCombatData.of(victim.equipment(slot)).modifiers().isEmpty()) {
                ItemWear.hurt(victim, slot, wear);
            }
        }
    }

    private boolean scalesWithDifficulty(final DamageSource source) {
        final JsonElement data = server.registries().registry(RegistryKey.DAMAGE_TYPE).data(source.type());
        final JsonElement scaling = data != null && data.isJsonObject() ? data.getAsJsonObject().get("scaling") : null;
        if (scaling == null || !scaling.isJsonPrimitive()) {
            return false;
        }
        return switch (scaling.getAsString()) {
            case "always" -> true;
            case "when_caused_by_living_non_player" ->
                    source.causingEntity() instanceof LivingEntity && !(source.causingEntity() instanceof Player);
            default -> false;
        };
    }

    private static float soakWithAbsorption(final AbstractLivingEntity victim, final float damage) {
        final float absorption = victim.absorptionAmount();
        if (absorption <= 0f) {
            return damage;
        }
        final float soaked = Math.min(absorption, damage);
        victim.setAbsorptionAmount(absorption - soaked);
        return damage - soaked;
    }

    // ------------------------------------------------------------------------------------------
    // Shields
    // ------------------------------------------------------------------------------------------

    @Override
    public boolean isBlocking(final LivingEntity entity) {
        return blockingItem(entity) != null;
    }

    /**
     * {@return the hand holding a shield raised long enough to block, {@code null} if there is none}
     */
    private @Nullable EquipmentSlot blockingItem(final LivingEntity entity) {
        if (!(entity instanceof final ServerPlayer player)) {
            return null;
        }
        final ItemUse use = player.itemUse();
        final EquipmentSlot hand = use.hand();
        if (hand == null) {
            return null;
        }
        final ItemStack stack = player.equipment(hand);
        final ItemCombatData.BlocksAttacks blocks = ItemCombatData.of(stack).blocksAttacks();
        if (blocks == null || player.isCoolingDown(RangedWeapons.cooldownGroup(stack))) {
            return null;
        }
        return use.ticks() >= (int) (blocks.blockDelaySeconds() * 20.0f) ? hand : null;
    }

    @Override
    public void disableBlocking(final LivingEntity entity, final float seconds) {
        if (seconds <= 0f || !(entity instanceof final ServerPlayer player)) {
            return;
        }
        final EquipmentSlot hand = player.itemUse().hand();
        if (hand == null) {
            return;
        }
        final ItemStack stack = player.equipment(hand);
        final ItemCombatData.BlocksAttacks blocks = ItemCombatData.of(stack).blocksAttacks();
        if (blocks == null) {
            return;
        }
        final int ticks = (int) (seconds * blocks.disableCooldownScale() * 20.0f);
        if (ticks <= 0) {
            return;
        }
        player.itemUse().stop();
        player.syncLivingFlags();
        player.startCooldown(RangedWeapons.cooldownGroup(stack), ticks);
        if (blocks.disabledSound() != null) {
            playAt(player, blocks.disabledSound(), 0.8f, 0.8f + ThreadLocalRandom.current().nextFloat() * 0.4f);
        }
    }

    /**
     * Blocks a hit with a raised shield facing it.
     *
     * @return the damage the shield took away, {@code 0} when it did not block
     */
    private float block(final AbstractLivingEntity victim, final DamageSource source, final float damage) {
        final EquipmentSlot hand = blockingItem(victim);
        if (hand == null) {
            return 0f;
        }
        final ItemStack shield = victim.equipment(hand);
        final ItemCombatData.BlocksAttacks blocks = ItemCombatData.of(shield).blocksAttacks();
        if (blocks == null || (blocks.bypassedBy() != null && source.is(blocks.bypassedBy()))) {
            return 0f;
        }
        if (source.directEntity() instanceof final ArrowProjectileEntity arrow && arrow.piercing() > 0) {
            return 0f;
        }
        final Location origin = source.position() != null ? source.position()
                : source.directEntity() != null ? source.directEntity().location() : null;
        if (origin == null) {
            return 0f;
        }
        final double angle = angleFromLook(victim, origin);
        float blocked = 0f;
        for (final ItemCombatData.DamageReduction reduction : blocks.damageReductions()) {
            if (angle > reduction.horizontalBlockingAngle() || !matches(reduction.types(), source)) {
                continue;
            }
            blocked += Math.clamp(reduction.base() + reduction.factor() * damage, 0f, damage);
        }
        blocked = Math.clamp(blocked, 0f, damage);
        if (blocked <= 0f) {
            return 0f;
        }
        final EntityBlockAttackEvent event = server.events().post(
                new EntityBlockAttackEventImpl(victim, source, shield, damage, blocked));
        if (event.isCancelled() || event.blockedDamage() <= 0f) {
            return 0f;
        }
        blocked = event.blockedDamage();

        if (blocks.blockSound() != null) {
            playAt(victim, blocks.blockSound(), 1.0f, 0.8f + ThreadLocalRandom.current().nextFloat() * 0.4f);
        }
        ItemWear.hurt(victim, hand, CombatRules.blockingItemDamage(blocks.itemDamage(), blocked));

        if (source.isDirect() && source.causingEntity() instanceof final AbstractLivingEntity attacker) {
            final Location attackerAt = attacker.location();
            final Location victimAt = victim.location();
            applyKnockback(attacker, BLOCKED_KNOCKBACK, victimAt.x() - attackerAt.x(), victimAt.z() - attackerAt.z());
            final ItemCombatData.Weapon weapon = ItemCombatData.of(attacker.equipment(EquipmentSlot.MAIN_HAND)).weapon();
            if (weapon != null && weapon.disableBlockingForSeconds() > 0f) {
                disableBlocking(victim, weapon.disableBlockingForSeconds());
            }
        }
        return blocked;
    }

    private static boolean matches(final @Nullable List<ItemCombatData.TypeFilter> types, final DamageSource source) {
        if (types == null) {
            return true;
        }
        for (final ItemCombatData.TypeFilter filter : types) {
            if ((filter.type() != null && filter.type().equals(source.type()))
                    || (filter.tag() != null && source.is(filter.tag()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@return the horizontal angle, in degrees, between where an entity looks and a point}
     */
    private static double angleFromLook(final AbstractLivingEntity entity, final Location point) {
        final Location at = entity.location();
        final double[] look = Hitboxes.look(yawOf(entity), 0f);
        final double lookX = look[0];
        final double lookZ = look[2];
        final double toX = point.x() - at.x();
        final double toZ = point.z() - at.z();
        final double length = Math.sqrt(toX * toX + toZ * toZ);
        if (length < 1.0E-6) {
            return 0.0;
        }
        final double cos = Math.clamp((lookX * toX + lookZ * toZ) / length, -1.0, 1.0);
        return Math.toDegrees(Math.acos(cos));
    }

    // ------------------------------------------------------------------------------------------
    // Death protection
    // ------------------------------------------------------------------------------------------

    /**
     * Saves an entity from a lethal hit with an item that protects from death held in a hand: the
     * item is used up, the entity is left with one health point and the effects of the item.
     *
     * @return {@code true} if the entity was saved
     */
    private boolean resurrect(final AbstractLivingEntity victim, final DamageSource source) {
        for (final EquipmentSlot hand : new EquipmentSlot[]{EquipmentSlot.MAIN_HAND, EquipmentSlot.OFF_HAND}) {
            final ItemStack stack = victim.equipment(hand);
            final ItemCombatData.DeathProtection protection = ItemCombatData.of(stack).deathProtection();
            if (protection == null) {
                continue;
            }
            if (server.events().post(new EntityResurrectEventImpl(victim, source, stack)).isCancelled()) {
                return false;
            }
            ItemWear.setEquipment(victim, hand, stack.count() <= 1 ? ItemStack.EMPTY : stack.withCount(stack.count() - 1));
            victim.setHealth(1.0f);
            applyDeathEffects(victim, protection);
            sendToViewersAndSelf(victim, new ClientboundEntityEventPacket(victim.entityId(), ENTITY_EVENT_TOTEM));
            syncHealth(victim);
            return true;
        }
        return false;
    }

    private void applyDeathEffects(final AbstractLivingEntity victim, final ItemCombatData.DeathProtection protection) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        for (final ItemCombatData.DeathEffect effect : protection.deathEffects()) {
            switch (effect) {
                case final ItemCombatData.DeathEffect.ClearAll ignored -> victim.clearEffects();
                case final ItemCombatData.DeathEffect.Apply apply -> {
                    if (random.nextFloat() >= apply.probability()) {
                        continue;
                    }
                    for (final ItemCombatData.AppliedEffect applied : apply.effects()) {
                        if (!server.registries().effects().isEffect(applied.type())) {
                            continue;
                        }
                        victim.addEffect(server.registries().effects().builder(applied.type())
                                .duration(applied.duration())
                                .amplifier(applied.amplifier())
                                .ambient(applied.ambient())
                                .visible(applied.showParticles())
                                .showIcon(applied.showIcon())
                                .build());
                    }
                }
                case final ItemCombatData.DeathEffect.Remove remove -> remove.effects().forEach(victim::removeEffect);
                case final ItemCombatData.DeathEffect.PlaySound sound -> playAt(victim, sound.sound(), 1.0f, 1.0f);
            }
        }
    }

    // ------------------------------------------------------------------------------------------
    // Knockback
    // ------------------------------------------------------------------------------------------

    @Override
    public void knockback(final LivingEntity target, final double strength, final double fromX, final double fromZ) {
        if (target instanceof final AbstractLivingEntity victim) {
            final Location at = victim.location();
            applyKnockback(victim, strength, fromX - at.x(), fromZ - at.z());
        }
    }

    /**
     * Pushes the victim of a hit away from where the hit came from: along the flight of a
     * projectile, away from the point of an explosion, away from an attacker.
     */
    private void knockbackFrom(final AbstractLivingEntity victim, final DamageSource source, final double strength) {
        if (source.directEntity() instanceof final ArrowProjectileEntity arrow) {
            final double vx = arrow.velocityX();
            final double vz = arrow.velocityZ();
            if (vx * vx + vz * vz > 1.0E-7) {
                applyKnockback(victim, strength, -vx, -vz);
                return;
            }
        }
        final Location origin = knockbackOrigin(source, victim);
        if (origin != null) {
            final Location at = victim.location();
            applyKnockback(victim, strength, origin.x() - at.x(), origin.z() - at.z());
        }
    }

    /**
     * Pushes an entity along the look of its attacker.
     */
    private void knockbackAlong(final AbstractLivingEntity victim, final double strength, final float yaw) {
        final double[] look = Hitboxes.look(yaw, 0f);
        applyKnockback(victim, strength, -look[0], -look[2]);
    }

    /**
     * Pushes an entity away from a direction: half of its velocity is kept, the push is added
     * against {@code (towardX, towardZ)}, and one on the ground is lifted a little.
     *
     * @param towardX the x component of the direction the push comes from
     * @param towardZ the z component of the direction the push comes from
     */
    public void applyKnockback(final AbstractLivingEntity victim, final double strength,
                               final double towardX, final double towardZ) {
        final double scaled = strength * (1.0 - Math.clamp(victim.knockbackResistance(), 0.0, 1.0));
        if (scaled <= 0.0) {
            return;
        }
        double dx = towardX;
        double dz = towardZ;
        final double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 1.0E-5) {
            final double random = ThreadLocalRandom.current().nextDouble() * Math.PI * 2.0;
            dx = Math.cos(random) * 0.01;
            dz = Math.sin(random) * 0.01;
        }
        final double norm = Math.sqrt(dx * dx + dz * dz);
        final double pushX = dx / norm * scaled;
        final double pushZ = dz / norm * scaled;

        if (victim instanceof final AbstractMovingMob mob) {
            final double newX = mob.velocityX() / 2.0 - pushX;
            final double newZ = mob.velocityZ() / 2.0 - pushZ;
            final double newY = mob.onGround()
                    ? Math.min(MAX_UPWARD_KNOCKBACK, mob.velocityY() / 2.0 + scaled)
                    : mob.velocityY();
            mob.setVelocity(newX, newY, newZ);
            mob.sendToTrackers(new ClientboundSetEntityMotionPacket(mob.entityId(), new PositionData.VelocityVec3D(newX, newY, newZ)));
            return;
        }
        if (victim instanceof final ServerPlayer player) {
            final double newX = player.impulseX() / 2.0 - pushX;
            final double newZ = player.impulseZ() / 2.0 - pushZ;
            final double newY = player.onGround()
                    ? Math.min(MAX_UPWARD_KNOCKBACK, player.impulseY() / 2.0 + scaled)
                    : player.impulseY();
            player.push(newX, newY, newZ);
        }
    }

    /**
     * Adds a velocity to an entity, as an explosion or a smash attack throws it.
     */
    public void addVelocity(final AbstractLivingEntity entity, final double x, final double y, final double z) {
        if (entity instanceof final AbstractMovingMob mob) {
            if (!mob.isRemoved() && !mob.isDead()) {
                mob.setVelocity(mob.velocityX() + x, mob.velocityY() + y, mob.velocityZ() + z);
            }
        } else if (entity instanceof final ServerPlayer player) {
            player.addImpulse(x, y, z);
        }
    }

    private static float yawOf(final AbstractLivingEntity entity) {
        return entity instanceof final AbstractMovingMob mob ? mob.yaw() : entity.location().yaw();
    }

    // ------------------------------------------------------------------------------------------
    // Death
    // ------------------------------------------------------------------------------------------

    // FIXME: the caller may not own the victim: a hit across a region border changes it from another thread
    @Override
    public void kill(final LivingEntity target, final @Nullable Entity killer) {
        if (!(target instanceof final AbstractLivingEntity victim) || victim.isDead()) {
            return;
        }
        victim.setAbsorptionAmount(0f);
        victim.setHealth(0f);
        syncHealth(victim);
        if (victim instanceof final AbstractMob mob) {
            mob.playDeathSound();
        }
        die(victim, killer);
    }

    private void die(final AbstractLivingEntity victim, final @Nullable Entity killer) {
        if (victim instanceof final ServerPlayer player) {
            player.itemUse().stop();
            killPlayer(player, killer);
            return;
        }
        if (!(victim instanceof AbstractMob)) {
            sendToViewersAndSelf(victim, new ClientboundEntityEventPacket(victim.entityId(), ENTITY_EVENT_DEATH));
        }
        server.events().post(new EntityDeathEventImpl(victim, killer));
        if (killer instanceof final AbstractMob mob && !mob.isRemoved()) {
            mob.onKilledEntity(victim);
        }
    }

    private void killPlayer(final ServerPlayer player, final @Nullable Entity killer) {
        if (player.isAwaitingRespawn()) {
            return;
        }
        player.setAwaitingRespawn(true);
        if (gameRulesOf(player).getBoolean(GameRuleKeys.FORGIVE_DEAD_PLAYERS)) {
            forgive(player);
        }

        final Component message = deathMessage(player, killer);
        final boolean keepInventory = gameRulesOf(player).getBoolean(GameRuleKeys.KEEP_INVENTORY);
        final PlayerDeathEvent event = server.events().post(new PlayerDeathEventImpl(player, killer, message, keepInventory));

        sendToViewersAndSelf(player, new ClientboundEntityEventPacket(player.entityId(), ENTITY_EVENT_DEATH));
        player.playSound(Sound.sound(SoundEvents.PLAYER_DEATH, Sound.Source.PLAYER, 1.0f, 1.0f));

        final Component shown = gameRulesOf(player).getBoolean(GameRuleKeys.SHOW_DEATH_MESSAGES)
                ? event.deathMessage()
                : null;
        player.connection().send(new ClientboundPlayerCombatKillPacket(
                player.entityId(), shown == null ? Component.empty() : shown));

        if (shown != null) {
            server.sendMessage(shown);
        }

        if (!event.keepInventory()) {
            player.runOrExecute(() -> {
                dropInventory(player);
                dropExperience(player);
            });
        }
    }

    /**
     * Makes the mobs that hold a grudge around a dead player forgive them: those within 32 blocks
     * across and 10 up or down of the block they died in.
     */
    private void forgive(final ServerPlayer player) {
        if (!(player.world() instanceof final ServerWorld world)) {
            return;
        }
        final Location at = player.location();
        final double blockX = Math.floor(at.x());
        final double blockY = Math.floor(at.y());
        final double blockZ = Math.floor(at.z());
        world.entityManager().forEachNear(at.chunk(), FORGIVE_RANGE + 1.0, entity -> {
            if (!(entity instanceof final AngerableMob angerable) || entity.isRemoved()
                    || !server.workers().regionizer().isOwnedByCurrentThread(world.key(), entity.location().chunk())) {
                return;
            }
            final Location there = entity.location();
            if (there.x() >= blockX - FORGIVE_RANGE && there.x() <= blockX + 1.0 + FORGIVE_RANGE
                    && there.y() >= blockY - FORGIVE_RANGE_Y && there.y() <= blockY + 1.0 + FORGIVE_RANGE_Y
                    && there.z() >= blockZ - FORGIVE_RANGE && there.z() <= blockZ + 1.0 + FORGIVE_RANGE) {
                angerable.anger().playerDied(player);
            }
        });
    }

    /**
     * Leaves the experience of a dead player where they died, as vanilla does: 7 points per level,
     * 100 at most; the rest is lost.
     */
    private void dropExperience(final ServerPlayer player) {
        final int dropped = player.deathExperience();
        player.clearExperience();
        if (dropped > 0 && player.world() instanceof final ServerWorld world) {
            final Location at = player.location();
            server.entities().awardExperience(world, at.x(), at.y(), at.z(), dropped);
        }
    }

    /**
     * Spills everything a dead player carried around them: the inventory, the cursor and the 2x2
     * crafting grid, after closing whatever window was open so that it gives its contents back first.
     */
    private void dropInventory(final ServerPlayer player) {
        if (player.containerMenu() != player.inventoryMenu()) {
            player.closeContainer();
        }
        final InventoryMenu menu = player.inventoryMenu();
        final List<ItemStack> lost = new ArrayList<>();
        lost.add(menu.takeCarried());
        final CraftingContainer grid = menu.grid();
        for (int slot = 0; slot < grid.size(); slot++) {
            lost.add(grid.get(slot));
            grid.set(slot, ItemStack.EMPTY);
        }
        menu.refreshResult();
        final PlayerInventory inventory = player.inventory();
        for (int slot = 0; slot < inventory.size(); slot++) {
            lost.add(inventory.get(slot));
        }
        inventory.clear();
        for (final ItemStack stack : lost) {
            // Curse of Vanishing: the stack is destroyed rather than dropped.
            if (!stack.isEmpty() && !EnchantmentEngine.preventsDrop(player, stack)) {
                server.entities().scatterFromPlayer(player, stack);
            }
        }
        player.updateInventory();
    }

    private Component deathMessage(final ServerPlayer player, final @Nullable Entity killer) {
        final Component victim = player.displayName().hoverEvent(player);
        if (killer == null) {
            return Component.translatable("death.attack.generic", victim);
        }
        final String key = killer instanceof Player ? "death.attack.player" : "death.attack.mob";
        return Component.translatable(key, victim, killer.displayName().hoverEvent(killer));
    }

    private void aggro(final AbstractMob mob, final @Nullable Entity attacker) {
        if (mob instanceof final AbstractPathfinderMob pathfinder && pathfinder.picksPlayersByItself()
                && attacker instanceof final ServerPlayer player) {
            pathfinder.setTarget(player);
        }
    }

    // ------------------------------------------------------------------------------------------
    // Packets
    // ------------------------------------------------------------------------------------------

    private void broadcastHurt(final AbstractLivingEntity victim, final DamageSource source) {
        if (server.registries().registry(RegistryKey.DAMAGE_TYPE).contains(source.type())) {
            sendToViewersAndSelf(victim, new ClientboundDamageEventPacket(
                    victim.entityId(),
                    source.type(),
                    source.causingEntity() == null ? null : source.causingEntity().entityId(),
                    source.directEntity() == null ? null : source.directEntity().entityId(),
                    null));
        } else {
            LOGGER.debug("Unknown damage type: {}", source.type().key());
        }
        sendToViewersAndSelf(victim, new ClientboundHurtAnimationPacket(victim.entityId(), hurtYaw(victim, source)));
    }

    private float hurtYaw(final AbstractLivingEntity victim, final DamageSource source) {
        final Location origin = knockbackOrigin(source, victim);
        if (origin == null) {
            return 0f;
        }
        final Location self = victim.location();
        return (float) (Math.toDegrees(Math.atan2(origin.z() - self.z(), origin.x() - self.x())) - 90.0);
    }

    private static @Nullable Location knockbackOrigin(final DamageSource source, final AbstractLivingEntity victim) {
        if (source.position() != null) {
            return source.position();
        }
        final Entity direct = source.directEntity() != null ? source.directEntity() : source.causingEntity();
        return direct == null || direct == victim ? null : direct.location();
    }

    private static void sendToViewersAndSelf(final AbstractLivingEntity victim, final ClientboundPacket<?> packet) {
        victim.sendToTrackers(packet);
        if (victim instanceof final ServerPlayer player) {
            player.connection().send(packet);
        }
    }

    /**
     * Plays a sound at an entity, to everyone near it.
     */
    public void playAt(final AbstractEntity entity, final Key sound, final float volume, final float pitch) {
        final Location at = entity.location();
        server.players().broadcastNear(entity.world(), at.x(), at.y(), at.z(),
                new ClientboundSoundPacket(Sound.sound(sound, entity.soundSource(), volume, pitch), at.x(), at.y(), at.z()),
                null);
    }

    /**
     * Sends the health of a player to their client.
     */
    public static void syncHealth(final AbstractLivingEntity victim) {
        if (victim instanceof final ServerPlayer player) {
            player.sendHealth();
        }
    }

}
