package fr.euphyllia.fidorial.testplugin.mob;

import fr.fidorial.entity.EntityType;
import fr.fidorial.entity.mob.MobDefinition;
import fr.fidorial.entity.mob.MobRegistry;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.EntityTypeKeys;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

public final class BullMobs {

    /**
     *  {@code /summon fidorialtest:bull}
     */
    public static final TypedKey<EntityType> BULL = TypedKey.withRegistry(RegistryKey.ENTITY_TYPE, "fidorialtest:bull");


    public static final TypedKey<EntityType> COW = EntityTypeKeys.COW;

    private static final float BULL_HEALTH = 24f;
    private static final double BULL_SPEED = 0.28;
    private static final double BULL_WIDTH = 0.95;
    private static final double BULL_HEIGHT = 1.45;
    private static final double BULL_SIGHT = 24.0;
    private static final float BULL_DAMAGE = 5f;

    private BullMobs() {
    }

    public static void attachToCows(final MobRegistry mobs, final Object owner, final ComponentLogger logger) {
        mobs.attach(COW, BullBehaviour::new, owner);
        logger.info("[TestPlugin] Les vaches chargent desormais le joueur ({} greffe)", COW);
    }

    /**
     * Declare le taureau comme un mob a part entiere.
     *
     * @param mobs   le registre du serveur
     * @param owner  le plugin, pour que tout se retire d'un coup
     * @param logger de quoi tracer l'enregistrement
     */
    public static void registerBull(final MobRegistry mobs, final Object owner, final ComponentLogger logger) {
        final MobDefinition bull = MobDefinition.builder(BULL, COW)
                .maxHealth(BULL_HEALTH)
                .movementSpeed(BULL_SPEED)
                .size(BULL_WIDTH, BULL_HEIGHT)
                .followRange(BULL_SIGHT)
                .attackDamage(BULL_DAMAGE)
                .soundSource(Sound.Source.HOSTILE)
                .behaviour(BullBehaviour::new)
                .persistent(true)
                .build();

        mobs.register(bull, owner);
        logger.info("[TestPlugin] Mob {} enregistre, invoquable avec /summon {}", BULL, BULL.asString());
    }

    public static void unregisterAll(final MobRegistry mobs, final Object owner) {
        mobs.unregisterAll(owner);
    }
}
