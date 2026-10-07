package fr.euphyllia.fidorial.testplugin.effect;

import fr.fidorial.entity.Player;
import fr.fidorial.entity.effect.MobEffectDefinition;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.entity.effect.MobEffectRegistry;
import fr.fidorial.event.EventBus;
import fr.fidorial.event.entity.EntityEffectEvent;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MobEffect;
import fr.fidorial.registry.keys.MobEffectKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

public final class TestEffects {

    public static final TypedKey<MobEffect> BLEEDING =
            TypedKey.create(RegistryKey.MOB_EFFECT, Key.key("fidorialtest", "bleeding"));

    private static final int MAX_AMPLIFIER = 4;

    private TestEffects() {
    }

    public static void registerAll(final MobEffectRegistry effects, final Plugin owner, final ComponentLogger logger) {
        effects.register(effects.define(BLEEDING)
                .category(MobEffectDefinition.Category.HARMFUL)
                // A vanilla client does not know fidorialtest:bleeding: it sees the Poison icon instead.
                .fallback(MobEffectKeys.POISON)
                .knownBy(TestEffects::hasEffectMod)
                .behaviour(new BleedingBehaviour())
                .build(), owner);
        logger.info("[TestPlugin] Effect {} registered (network ID {})",
                BLEEDING.key(), effects.networkId(BLEEDING).orElse(-1));

        effects.attach(MobEffectKeys.REGENERATION, new RegenerationBehaviour(), owner);
        logger.info("[TestPlugin] Regeneration now heals");
    }

    public static void capAmplifier(final EventBus events, final ComponentLogger logger) {
        events.subscribe(EntityEffectEvent.class, event -> {
            final MobEffectInstance effect = event.effect();
            if (effect == null || effect.amplifier() <= MAX_AMPLIFIER) {
                return;
            }
            event.setEffect(effect.toBuilder().amplifier(MAX_AMPLIFIER).build());
            logger.info("[TestPlugin] {} on {} capped at level {}",
                    effect.type().key(), event.entity().uuid(), MAX_AMPLIFIER + 1);
        });
    }

    public static void unregisterAll(final MobEffectRegistry effects, final Plugin owner) {
        effects.unregisterAll(owner);
    }

    private static boolean hasEffectMod(final Player player) {
        return false;
    }
}
