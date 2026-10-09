package fr.euphyllia.fidorial.server.entity.mob;

import fr.euphyllia.fidorial.server.entity.ai.PathPenalty;
import fr.fidorial.entity.EntityType;
import fr.fidorial.entity.mob.MobDefinition;
import fr.fidorial.math.Location;
import net.kyori.adventure.sound.Sound;

import java.util.Optional;
import java.util.UUID;

public final class PluginMob extends AbstractPathfinderMob {

    private final MobDefinition definition;

    public PluginMob(final MobDefinition definition, final EntityType type, final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), type, location, definition.maxHealth());
        this.definition = definition;
        navigation.setPathPenalty(PathPenalty.LAND_ANIMAL);
    }

    @Override
    public Optional<MobDefinition> definition() {
        return Optional.of(definition);
    }

    @Override
    public Sound.Source soundSource() {
        return definition.soundSource();
    }

    @Override
    public double movementSpeed() {
        return definition.movementSpeed();
    }

    @Override
    public double width() {
        return definition.width();
    }

    @Override
    public double height() {
        return definition.height();
    }

    @Override
    protected double halfWidth() {
        return definition.width() * 0.5;
    }

    @Override
    protected double defaultFollowRange() {
        return definition.followRange();
    }

    @Override
    public float attackDamage() {
        return definition.attackDamage();
    }

    public boolean isPersistent() {
        return definition.persistent();
    }
}
