package fr.euphyllia.fidorial.testplugin.mob;

import fr.fidorial.entity.EntityType;
import fr.fidorial.entity.mob.MobDefinition;
import fr.fidorial.entity.mob.MobRegistry;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.EntityTypeKeys;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

public final class CompanionMobs {

    /** {@code /summon fidorialtest:companion}. */
    public static final TypedKey<EntityType> COMPANION = TypedKey.withRegistry(RegistryKey.ENTITY_TYPE, "fidorialtest:companion");

    public static final TypedKey<EntityType> AXOLOTL = EntityTypeKeys.AXOLOTL;

    private static final float COMPANION_HEALTH = 14f;
    private static final double COMPANION_SPEED = 0.24;

    private static final double COMPANION_WIDTH = 0.75;
    private static final double COMPANION_HEIGHT = 0.42;

    private static final double COMPANION_SIGHT = 16.0;

    private CompanionMobs() {
    }

    public static void register(final MobRegistry mobs, final Object owner, final ComponentLogger logger) {
        final MobDefinition companion = MobDefinition.builder(COMPANION, AXOLOTL)
                .maxHealth(COMPANION_HEALTH)
                .movementSpeed(COMPANION_SPEED)
                .size(COMPANION_WIDTH, COMPANION_HEIGHT)
                .followRange(COMPANION_SIGHT)
                .soundSource(Sound.Source.NEUTRAL)
                .behaviour(CompanionBehaviour::new)
                .persistent(true)
                .build();

        mobs.register(companion, owner);
        logger.info("[TestPlugin] Mob {} enregistre, invoquable avec /summon {}", COMPANION, COMPANION.asString());
    }
}
