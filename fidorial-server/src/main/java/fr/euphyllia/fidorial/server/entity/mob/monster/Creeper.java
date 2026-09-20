package fr.euphyllia.fidorial.server.entity.mob.monster;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.goal.ChaseTargetGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.LookAtTargetGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.RandomStrollGoal;
import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.world.Explosion;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.math.Location;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.World;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.UUID;

public final class Creeper extends AbstractPathfinderMob implements Category.Monster {

    public static final float MAX_HEALTH = 20f;
    private static final ComponentLogger LOGGER = ComponentLogger.logger(Creeper.class);

    private static final int FUSE_TICKS = 30;

    private static final double SWELL_RANGE_SQ = 3.0 * 3.0;
    private static final double DEFUSE_RANGE_SQ = 7.0 * 7.0;
    private static final float EXPLOSION_POWER = 3.0f;
    private static final double CHASE_SPEED = 0.16;
    private static final double STROLL_SPEED = 0.09;
    private static final int MD_STATE = 16;
    private static final int MD_CHARGED = 17;
    private static final int MD_IGNITED = 18;
    private int swell;
    private boolean primed;

    public Creeper(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.CREEPER, location, MAX_HEALTH);

        goals.add(new SwellGoal());
        goals.add(new ChaseTargetGoal(this, 1, CHASE_SPEED));
        goals.add(new RandomStrollGoal(this, 2, STROLL_SPEED));
        goals.add(new LookAtTargetGoal(this, 3, 8.0));
    }

    @Override
    public void tick(final long currentTick) {
        super.tick(currentTick);
        if (isRemoved()) {
            return;
        }
        if (primed) {
            if (++swell >= FUSE_TICKS) {
                explode();
            }
        } else if (swell > 0) {
            swell--;
        }
    }

    private void explode() {
        final Location center = location();
        setPrimed(false);
        server().despawnEntity(this);
        Explosion.explode(serverWorld(), center, EXPLOSION_POWER, this);
    }

    @Override
    protected void onDeath() {
        setPrimed(false);
        super.onDeath();
    }

    @Override
    public void sendSpawnPackets(final ClientConnection connection) {
        super.sendSpawnPackets(connection);
        if (primed) {
            connection.send(ClientboundSetEntityMetadataPacket.of(
                    entityId(), ClientboundSetEntityMetadataPacket.Entry.varInt(MD_STATE, 1)));
        }
    }

    private void setPrimed(final boolean primed) {
        if (this.primed == primed) {
            return;
        }
        this.primed = primed;
        sendToTrackers(ClientboundSetEntityMetadataPacket.of(
                entityId(), ClientboundSetEntityMetadataPacket.Entry.varInt(MD_STATE, primed ? 1 : -1)));
        if (primed) {
            playSound(SoundEvents.CREEPER_PRIMED, 1.0f, 0.5f);
        }
    }

    @Override
    protected Sound.Type hurtSound() {
        return SoundEvents.CREEPER_HURT;
    }

    @Override
    protected Sound.Type deathSound() {
        return SoundEvents.CREEPER_DEATH;
    }

    @Override
    protected float voicePitch() {
        return 1.0f;
    }

    private final class SwellGoal implements Goal {

        @Override
        public int priority() {
            return 0;
        }

        @Override
        public boolean canStart() {
            final ServerPlayer target = target();
            return target != null && distanceSqTo(target) <= SWELL_RANGE_SQ && hasLineOfSightTo(target);
        }

        @Override
        public boolean shouldContinue() {
            final ServerPlayer target = target();
            return target != null && distanceSqTo(target) <= DEFUSE_RANGE_SQ && hasLineOfSightTo(target);
        }

        @Override
        public void start() {
            navigation().stop();
            setPrimed(true);
        }

        @Override
        public void stop() {
            setPrimed(false);
        }

        @Override
        public void tick() {
            final ServerPlayer target = target();
            if (target != null) {
                lookAt(target);
            }
        }
    }
}
