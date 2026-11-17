package fr.euphyllia.fidorial.server.world;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMovingMob;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundAddEntityMotionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundExplodePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.LocationPositionData;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.world.block.InfestedBlocks;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.GameMode;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.AttributeKeys;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.registry.keys.DamageTypeKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.registry.keys.ParticleTypeKeys;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.block.BlockType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Source used: <a href="https://minecraft.wiki/w/Explosion">Explosion Wiki</a>
 */
public final class Explosion {

    private static final float STEP = 0.3f;
    private static final float AIR_ATTENUATION = 0.22500001f;
    private static final float EMITTER_PARTICLE_MIN_POWER = 2.0f;

    private static final float DEFAULT_BLAST_RESISTANCE = 0.5f;
    private static final float FLUID_BLAST_RESISTANCE = 100f;

    // TODO : Add the `BLAST_RESISTANCE` method to BlockState.
    private static final Map<TypedKey<BlockType>, Float> BLAST_RESISTANCE = Map.ofEntries(
            Map.entry(BlockTypeKeys.BEDROCK, 3_600_000f),
            Map.entry(BlockTypeKeys.BARRIER, 3_600_000f),
            Map.entry(BlockTypeKeys.COMMAND_BLOCK, 3_600_000f),
            Map.entry(BlockTypeKeys.END_PORTAL_FRAME, 3_600_000f),
            Map.entry(BlockTypeKeys.OBSIDIAN, 1_200f),
            Map.entry(BlockTypeKeys.CRYING_OBSIDIAN, 1_200f),
            Map.entry(BlockTypeKeys.RESPAWN_ANCHOR, 1_200f),
            Map.entry(BlockTypeKeys.REINFORCED_DEEPSLATE, 1_200f),
            Map.entry(BlockTypeKeys.ANCIENT_DEBRIS, 1_200f),
            Map.entry(BlockTypeKeys.NETHERITE_BLOCK, 1_200f),
            Map.entry(BlockTypeKeys.ENCHANTING_TABLE, 1_200f),
            Map.entry(BlockTypeKeys.ENDER_CHEST, 600f),
            Map.entry(BlockTypeKeys.END_STONE, 9f),
            Map.entry(BlockTypeKeys.STONE, 6f),
            Map.entry(BlockTypeKeys.COBBLESTONE, 6f),
            Map.entry(BlockTypeKeys.DEEPSLATE, 6f),
            Map.entry(BlockTypeKeys.GRANITE, 6f),
            Map.entry(BlockTypeKeys.DIORITE, 6f),
            Map.entry(BlockTypeKeys.ANDESITE, 6f),
            Map.entry(BlockTypeKeys.BRICKS, 6f),
            Map.entry(BlockTypeKeys.GLASS, 0.3f));

    private static final double SNEAKING_PLAYER_HEIGHT = 1.5;

    private Explosion() {
    }

    public static void explode(final ServerWorld world, final Location center, final float power, final AbstractEntity source) {
        final FidorialServer server = FidorialServer.getInstance();
        final List<BlockPosition> toDestroy = collectExplodedBlocks(world, center, power);
        damageEntities(server, world, center, power, source);
        final boolean breaksBlocks = breaksBlocks(world, source);
        if (breaksBlocks) {
            destroyBlocks(server, world, toDestroy, power);
        }
        final boolean emitterParticle = power >= EMITTER_PARTICLE_MIN_POWER && breaksBlocks;
        server.players().broadcastNear(world, center.x(), center.y(), center.z(),
                new ClientboundExplodePacket(
                        LocationPositionData.vec3(center),
                        power,
                        toDestroy.size(),
                        emitterParticle ? ParticleTypeKeys.EXPLOSION_EMITTER : ParticleTypeKeys.EXPLOSION,
                        SoundEvents.GENERIC_EXPLODE,
                        List.of()), null);
    }

    private static boolean breaksBlocks(final ServerWorld world, final AbstractEntity source) {
        return !(source instanceof AbstractMob) || world.gameRuleValues().getBoolean(GameRuleKeys.MOB_GRIEFING);
    }

    private static void destroyBlocks(final FidorialServer server, final ServerWorld world, final List<BlockPosition> toDestroy, final float power) {
        for (final BlockPosition pos : toDestroy) {
            final BlockState state = BlockView.blockAt(world, pos.blockX(), pos.blockY(), pos.blockZ());
            if (server.worlds().blockEdits().set(world, pos, BlockState.withoutProperties(BlockTypeKeys.AIR), true)) {
                if (state != null) {
                    // an infested block lets its silverfish out whatever happens to its drops
                    InfestedBlocks.afterBreak(world, pos, state, ItemStack.EMPTY);
                }
                if (ThreadLocalRandom.current().nextFloat() < 1.0f / power) {
                    // TODO : drop item
                }
            }
        }
    }

    private static List<BlockPosition> collectExplodedBlocks(final ServerWorld world, final Location center, final float power) {
        final Set<BlockPosition> out = new HashSet<>();
        final var random = ThreadLocalRandom.current();
        final double ox = center.x();
        final double oy = center.y();
        final double oz = center.z();

        for (int j = 0; j < 16; j++) {
            for (int k = 0; k < 16; k++) {
                for (int l = 0; l < 16; l++) {
                    if (j != 0 && j != 15 && k != 0 && k != 15 && l != 0 && l != 15) {
                        continue;
                    }
                    double dx = j / 15.0 * 2.0 - 1.0;
                    double dy = k / 15.0 * 2.0 - 1.0;
                    double dz = l / 15.0 * 2.0 - 1.0;
                    final double norm = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    dx /= norm;
                    dy /= norm;
                    dz /= norm;

                    float intensity = power * (0.7f + random.nextFloat() * 0.6f);
                    double px = ox, py = oy, pz = oz;

                    for (; intensity > 0.0f; intensity -= AIR_ATTENUATION) {
                        final int bx = (int) Math.floor(px);
                        final int by = (int) Math.floor(py);
                        final int bz = (int) Math.floor(pz);
                        final BlockState state = BlockView.blockAt(world, bx, by, bz);
                        if (state == null) {
                            break;
                        }
                        if (!state.isAir()) {
                            intensity -= (resistanceOf(state) + 0.3f) * 0.3f;
                            if (intensity > 0.0f) {
                                out.add(Position.block(bx, by, bz));
                            }
                        }
                        px += dx * STEP;
                        py += dy * STEP;
                        pz += dz * STEP;
                    }
                }
            }
        }
        final List<BlockPosition> shuffled = new ArrayList<>(out);
        Collections.shuffle(shuffled, random);
        return shuffled;
    }

    private static float resistanceOf(final BlockState state) {
        final float resistance = blockResistanceOf(state);
        return state.isFluid() ? Math.max(resistance, FLUID_BLAST_RESISTANCE) : resistance;
    }

    private static float blockResistanceOf(final BlockState state) {
        // Todo : Add the `BLAST_RESISTANCE` method to BlockState.
        final Float resistance = BLAST_RESISTANCE.get(state.name());
        if (resistance != null) {
            return resistance;
        }
        if (BlockView.isTagged(BlockTypeTagKeys.PLANKS, state)) {
            return 3f;
        }
        if (BlockView.isTagged(BlockTypeTagKeys.LEAVES, state)) {
            return 0.2f;
        }
        return DEFAULT_BLAST_RESISTANCE;
    }

    private static void damageEntities(
            final FidorialServer server,
            final ServerWorld world,
            final Location center,
            final float power,
            final AbstractEntity source
    ) {
        final double range = power * 2.0;
        final double rangeSq = range * range;
        final double cx = center.x();
        final double cy = center.y();
        final double cz = center.z();

        final List<AbstractEntity> affected = new ArrayList<>();
        world.entityManager().forEachNear(center.chunk(), range, affected::add);
        final DamageSource damageSource = damageSource(source, center);

        for (final AbstractEntity abstractEntity : affected) {
            if (abstractEntity == source || abstractEntity.isRemoved()) {
                continue;
            }
            final Location pos = abstractEntity.location();

            final double fx = pos.x() - cx;
            final double fy = pos.y() - cy;
            final double fz = pos.z() - cz;
            final double feetDistSq = fx * fx + fy * fy + fz * fz;
            if (feetDistSq > rangeSq) {
                continue;
            }
            final double feetDist = Math.sqrt(feetDistSq);
            final double exposure = getExposure(world, center, abstractEntity);
            final double impact = (1.0 - feetDist / range) * exposure;
            final float damage = (float) ((impact * impact + impact) / 2.0 * 7.0 * range + 1.0);

            final double eye = eyeHeight(abstractEntity);
            final double ex = pos.x() - cx;
            final double ey = pos.y() + eye - cy;
            final double ez = pos.z() - cz;
            final double eyeDist = Math.sqrt(ex * ex + ey * ey + ez * ez);
            double knockX = 0.0, knockY = 0.0, knockZ = 0.0;
            if (eyeDist > 1.0E-4) {
                knockX = ex / eyeDist * impact;
                knockY = ey / eyeDist * impact;
                knockZ = ez / eyeDist * impact;
            }

            if (abstractEntity instanceof final AbstractLivingEntity living) {
                server.entities().combat().damage(living, damageSource, damage);
                pushAway(living, knockX, knockY, knockZ);
            }
        }
    }

    /**
     * Pushes a living entity away from an explosion, less if it resists the blast; a spectator and a
     * creative player who flies are not pushed.
     */
    private static void pushAway(final AbstractLivingEntity entity, final double x, final double y, final double z) {
        switch (entity) {
            case final ServerPlayer player -> {
                if (player.gameMode() == GameMode.SPECTATOR || (player.gameMode() == GameMode.CREATIVE && player.isFlying())) {
                    return;
                }
                final double kept = 1.0 - Math.clamp(player.attributeValue(AttributeKeys.EXPLOSION_KNOCKBACK_RESISTANCE), 0.0, 1.0);
                // the client moves its player itself
                player.connection().send(new ClientboundAddEntityMotionPacket(player.entityId(),
                        new PositionData.VelocityVec3D(x * kept, y * kept, z * kept)));
            }
            case final AbstractMovingMob mob -> {
                if (!mob.isRemoved() && !mob.isDead()) {
                    mob.setVelocity(mob.velocityX() + x, mob.velocityY() + y, mob.velocityZ() + z);
                }
            }
            default -> {

            }
        }
    }

    /**
     * {@return what the victims of an explosion are hurt by}: an explosion with an attacker when a
     * living entity set it off, a plain one otherwise
     */
    private static DamageSource damageSource(final AbstractEntity source, final Location center) {
        return source instanceof final AbstractLivingEntity living
                ? DamageSource.of(DamageTypeKeys.PLAYER_EXPLOSION, living, source).withPosition(center)
                : DamageSource.of(DamageTypeKeys.EXPLOSION, null, source).withPosition(center);
    }

    private static float getExposure(final ServerWorld world, final Location center, final AbstractEntity entity) {
        final double[] box = boundingBox(entity);
        final double width = box[3] - box[0];
        final double height = box[4] - box[1];

        final double stepX = 1.0 / (width * 2.0 + 1.0);
        final double stepY = 1.0 / (height * 2.0 + 1.0);
        final double stepZ = stepX;
        if (stepX <= 0.0 || stepY <= 0.0) {
            return 0.0f;
        }
        final double offX = (1.0 - Math.floor(1.0 / stepX) * stepX) / 2.0;
        final double offZ = (1.0 - Math.floor(1.0 / stepZ) * stepZ) / 2.0;

        int clear = 0, total = 0;
        for (double fx = 0.0; fx <= 1.0; fx += stepX) {
            for (double fy = 0.0; fy <= 1.0; fy += stepY) {
                for (double fz = 0.0; fz <= 1.0; fz += stepZ) {
                    final double sx = lerp(fx, box[0], box[3]) + offX;
                    final double sy = lerp(fy, box[1], box[4]);
                    final double sz = lerp(fz, box[2], box[5]) + offZ;
                    if (clearLineOfSight(world, sx, sy, sz, center.x(), center.y(), center.z())) {
                        clear++;
                    }
                    total++;
                }
            }
        }
        return total == 0 ? 0.0f : (float) clear / total;
    }

    private static boolean clearLineOfSight(
            final ServerWorld world,
            final double sx,
            final double sy,
            final double sz,
            final double cx,
            final double cy,
            final double cz
    ) {

        final double dx = cx - sx;
        final double dy = cy - sy;
        final double dz = cz - sz;
        final double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist < 1.0E-6) {
            return true;
        }
        final int steps = (int) Math.ceil(dist / 0.3);
        final double stepX = dx / steps;
        final double stepY = dy / steps;
        final double stepZ = dz / steps;
        double x = sx, y = sy, z = sz;
        for (int i = 0; i < steps; i++) {
            x += stepX;
            y += stepY;
            z += stepZ;
            final BlockState state = BlockView.blockAt(world, (int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
            if (state != null && !state.isAir() && (!state.isFluid() || state.isWaterlogged())) {
                return false;
            }
        }
        return true;
    }

    private static double eyeHeight(final AbstractEntity entity) {
        final double eyes = AbstractMovingMob.eyeHeightOf(entity);
        if (eyes > 0.0) {
            return eyes;
        }
        final double[] box = boundingBox(entity);
        return (box[4] - box[1]) * 0.85;
    }

    private static double[] boundingBox(final AbstractEntity entity) {
        double width = 0.6;
        double height = 1.8;
        if (entity instanceof final AbstractMovingMob mob) {
            width = mob.width();
            height = mob.height();
        } else if (entity instanceof final ServerPlayer player && player.isSneaking()) {
            height = SNEAKING_PLAYER_HEIGHT;
        } else if (!(entity instanceof ServerPlayer)) {
            width = entity.type().width();
            height = entity.type().height();
        }
        final Location loc = entity.location();
        final double half = width / 2.0;
        return new double[]{loc.x() - half, loc.y(), loc.z() - half, loc.x() + half, loc.y() + height, loc.z() + half};
    }

    private static double lerp(final double t, final double a, final double b) {
        return a + (b - a) * t;
    }
}
