package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.world.gamerule.FidorialGameRules;
import fr.euphyllia.fidorial.server.world.gamerule.GameRuleValues;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.Player;
import fr.fidorial.event.Subscription;
import fr.fidorial.event.server.GameRuleChangeEvent;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.Location;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unused")
public final class GameRuleTests {

    @ScenarioTest(timeoutTicks = 20)
    public static void valuesSurviveASaveRoundTrip(final ScenarioTestHelper helper) {
        final GameRuleValues original = new GameRuleValues();
        original.load(CompoundBinaryTag.builder()
                .putBoolean("minecraft:keep_inventory", true)
                .putInt("minecraft:random_tick_speed", 10)
                .build(), Set.of());

        final CompoundBinaryTag.Builder saved = CompoundBinaryTag.builder();
        original.save(saved);
        final CompoundBinaryTag tag = saved.build();
        helper.assertTrue(tag.getBoolean("minecraft:keep_inventory"), "Expected keep_inventory to be saved as a boolean");
        helper.assertTrue(tag.getInt("minecraft:random_tick_speed") == 10, "Expected random_tick_speed to be saved as an int");

        final GameRuleValues reloaded = new GameRuleValues();
        reloaded.load(tag, Set.of());
        helper.assertTrue(reloaded.getBoolean(GameRuleKeys.KEEP_INVENTORY), "Expected keep_inventory=true after reload");
        helper.assertTrue(reloaded.getInt(GameRuleKeys.RANDOM_TICK_SPEED) == 10, "Expected random_tick_speed=10 after reload");
        helper.assertTrue(reloaded.getBoolean(GameRuleKeys.ADVANCE_TIME), "Expected untouched rules to keep their default");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void legacyCamelCaseRulesAreImported(final ScenarioTestHelper helper) {
        final CompoundBinaryTag legacy = CompoundBinaryTag.builder()
                .putString("keepInventory", "true")
                .putString("disableRaids", "true")
                .putString("doFireTick", "false")
                .putString("randomTickSpeed", "7")
                .put("minecraft:rule_from_the_future", ByteBinaryTag.byteBinaryTag((byte) 1))
                .build();

        final GameRuleValues rules = new GameRuleValues();
        rules.load(legacy, Set.of());
        helper.assertTrue(rules.getBoolean(GameRuleKeys.KEEP_INVENTORY), "keepInventory should map to keep_inventory");
        helper.assertTrue(!rules.getBoolean(GameRuleKeys.RAIDS), "disableRaids=true should map to raids=false");
        helper.assertTrue(rules.getInt(GameRuleKeys.FIRE_SPREAD_RADIUS_AROUND_PLAYER) == 0,
                "doFireTick=false should map to fire_spread_radius_around_player=0");
        helper.assertTrue(rules.getInt(GameRuleKeys.RANDOM_TICK_SPEED) == 7, "randomTickSpeed should map to random_tick_speed");

        final CompoundBinaryTag.Builder saved = CompoundBinaryTag.builder();
        rules.save(saved);
        helper.assertTrue(saved.build().get("minecraft:rule_from_the_future") != null,
                "Rules unknown to this build should be written back untouched");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void invalidValuesAreRejected(final ScenarioTestHelper helper) {
        final FidorialGameRules rules = FidorialServer.getInstance().gameRules();
        helper.assertTrue(rejects(() -> rules.set(GameRuleKeys.MAX_SNOW_ACCUMULATION_HEIGHT, "9")), "9 is above the maximum of 8");
        helper.assertTrue(rejects(() -> rules.set(GameRuleKeys.RANDOM_TICK_SPEED, "fast")), "Non-numeric values must be rejected");
        helper.assertTrue(rejects(() -> rules.set(GameRuleKeys.PVP, "yes")), "Only true/false are valid booleans");
        helper.assertTrue(rejects(() -> rules.getInt(GameRuleKeys.PVP)), "pvp is not an integer rule");
        helper.assertTrue(rules.definition("keep_inventory").equals(rules.definition("minecraft:keep_inventory")),
                "The namespace should be optional");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void changeEventDescribesTheChange(final ScenarioTestHelper helper) {
        final FidorialGameRules rules = FidorialServer.getInstance().gameRules();
        final List<GameRuleChangeEvent> seen = new ArrayList<>();
        final Subscription subscription = FidorialServer.getInstance().events().subscribe(GameRuleChangeEvent.class, event -> {
            if (event.rule().key().equals(GameRuleKeys.SPAWN_PATROLS)) {
                seen.add(event);
            }
        });
        try {
            rules.setBoolean(GameRuleKeys.SPAWN_PATROLS, false);
            rules.setBoolean(GameRuleKeys.SPAWN_PATROLS, false);
        } finally {
            subscription.unsubscribe();
            rules.reset(GameRuleKeys.SPAWN_PATROLS);
        }
        helper.assertTrue(seen.size() == 1, "Expected one event, a no-op change fires none, got " + seen.size());
        final GameRuleChangeEvent event = seen.getFirst();
        helper.assertTrue(event.previousValue().equals("true") && event.newValue().equals("false"),
                "Expected true -> false, got " + event.previousValue() + " -> " + event.newValue());
        helper.assertTrue(event.cause() == GameRuleChangeEvent.Cause.API && event.source().isEmpty(),
                "A plugin change should have the API cause and no source");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void listenersCanCancelOrAlterAChange(final ScenarioTestHelper helper) {
        final FidorialGameRules rules = FidorialServer.getInstance().gameRules();
        final Subscription subscription = FidorialServer.getInstance().events().subscribe(GameRuleChangeEvent.class, event -> {
            if (event.rule().key().equals(GameRuleKeys.SPAWN_WARDENS)) {
                event.setCancelled(true);
            } else if (event.rule().key().equals(GameRuleKeys.RANDOM_TICK_SPEED)) {
                event.setNewValue(Math.min(Integer.parseInt(event.newValue()), 20));
            }
        });
        try {
            helper.assertTrue(!rules.setBoolean(GameRuleKeys.SPAWN_WARDENS, false), "A cancelled change should report no change");
            helper.assertTrue(rules.getBoolean(GameRuleKeys.SPAWN_WARDENS), "A cancelled change should leave the rule untouched");

            helper.assertTrue(rules.setInt(GameRuleKeys.RANDOM_TICK_SPEED, 1_000), "An altered change should still apply");
            helper.assertTrue(rules.getInt(GameRuleKeys.RANDOM_TICK_SPEED) == 20, "Expected the value set by the listener");
        } finally {
            subscription.unsubscribe();
            rules.reset(GameRuleKeys.SPAWN_WARDENS);
            rules.reset(GameRuleKeys.RANDOM_TICK_SPEED);
        }
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void fallDamageRuleProtectsPlayers(final ScenarioTestHelper helper) {
        final FidorialGameRules rules = FidorialServer.getInstance().gameRules();
        final Player player = helper.summonPlayer("NoFallDamage", new Location(0.5, 65, 0.5, 0f, 0f), GameMode.SURVIVAL);
        final AtomicBoolean hurtWhileDisabled = new AtomicBoolean(true);
        final AtomicBoolean hurtWhileEnabled = new AtomicBoolean(false);

        helper.sequence()
                .execute(() -> {
                    rules.setBoolean(GameRuleKeys.FALL_DAMAGE, false);
                    try {
                        hurtWhileDisabled.set(player.damage(DamageSource.fall(), 4f));
                    } finally {
                        rules.reset(GameRuleKeys.FALL_DAMAGE);
                    }
                    hurtWhileEnabled.set(player.damage(DamageSource.fall(), 4f));
                })
                .execute(() -> helper.assertTrue(!hurtWhileDisabled.get(), "Expected no fall damage with fall_damage=false"))
                .execute(() -> helper.assertTrue(hurtWhileEnabled.get(), "Expected fall damage with fall_damage=true"))
                .build();
    }

    private static boolean rejects(final Runnable action) {
        try {
            action.run();
            return false;
        } catch (final IllegalArgumentException e) {
            return true;
        }
    }
}
