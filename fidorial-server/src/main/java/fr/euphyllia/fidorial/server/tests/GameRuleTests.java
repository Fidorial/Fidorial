package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.world.gamerule.FidorialGameRules;
import fr.euphyllia.fidorial.server.world.gamerule.GameRuleValues;
import fr.euphyllia.fidorial.server.world.storage.datafixers.minecraft.V1_21_11.level.V4660;
import fr.euphyllia.fidorial.server.world.storage.datafixers.util.nbt.NbtMapType;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.Player;
import fr.fidorial.event.Subscription;
import fr.fidorial.event.server.GameRuleChangeEvent;
import fr.fidorial.gamerule.WorldGameRules;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.Location;
import fr.fidorial.world.World;
import fr.fidorial.world.WorldBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public final class GameRuleTests {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(GameRuleTests.class);

    @ScenarioTest(timeoutTicks = 20)
    public static void valuesSurviveASaveRoundTrip(final ScenarioTestHelper helper) {
        final GameRuleValues original = new GameRuleValues();
        original.load(CompoundBinaryTag.builder()
                .putBoolean("minecraft:keep_inventory", true)
                .putInt("minecraft:random_tick_speed", 10)
                .build());

        final CompoundBinaryTag.Builder saved = CompoundBinaryTag.builder();
        original.save(saved);
        final CompoundBinaryTag tag = saved.build();
        helper.assertTrue(tag.getBoolean("minecraft:keep_inventory"), "Expected keep_inventory to be saved as a boolean");
        helper.assertTrue(tag.getInt("minecraft:random_tick_speed") == 10, "Expected random_tick_speed to be saved as an int");

        final GameRuleValues reloaded = new GameRuleValues();
        reloaded.load(tag);
        helper.assertTrue(reloaded.getBoolean(GameRuleKeys.KEEP_INVENTORY), "Expected keep_inventory=true after reload");
        helper.assertTrue(reloaded.getInt(GameRuleKeys.RANDOM_TICK_SPEED) == 10, "Expected random_tick_speed=10 after reload");
        helper.assertTrue(reloaded.getBoolean(GameRuleKeys.ADVANCE_TIME), "Expected untouched rules to keep their default");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void legacyCamelCaseRulesAreUpgraded(final ScenarioTestHelper helper) {
        final CompoundBinaryTag level = CompoundBinaryTag.builder()
                .put("GameRules", CompoundBinaryTag.builder()
                        .putString("keepInventory", "true")
                        .putString("disableRaids", "true")
                        .putString("doFireTick", "false")
                        .putString("randomTickSpeed", "7")
                        .putString("snowAccumulationHeight", "12")
                        .putString("spawnChunkRadius", "2")
                        .build())
                .build();
        LOGGER.info("[legacyCamelCaseRulesAreUpgraded] before V4660: {}", snbt(level));

        final CompoundBinaryTag upgraded = ((NbtMapType) new V4660().convert(NbtMapType.of(level), 4658, 4660)).toCompound();
        LOGGER.info("[legacyCamelCaseRulesAreUpgraded] after V4660:  {}", snbt(upgraded));

        helper.assertTrue(!upgraded.contains("GameRules"), "GameRules should be renamed to game_rules");
        final CompoundBinaryTag stored = upgraded.getCompound("game_rules");
        helper.assertTrue(stored.get("spawnChunkRadius") == null, "Removed rules should be dropped");

        final GameRuleValues rules = new GameRuleValues();
        rules.load(stored);
        helper.assertTrue(rules.getBoolean(GameRuleKeys.KEEP_INVENTORY), "keepInventory should map to keep_inventory");
        helper.assertTrue(!rules.getBoolean(GameRuleKeys.RAIDS), "disableRaids=true should map to raids=false");
        helper.assertTrue(rules.getInt(GameRuleKeys.FIRE_SPREAD_RADIUS_AROUND_PLAYER) == 0,
                "doFireTick=false should map to fire_spread_radius_around_player=0");
        helper.assertTrue(rules.getInt(GameRuleKeys.RANDOM_TICK_SPEED) == 7, "randomTickSpeed should map to random_tick_speed");
        helper.assertTrue(rules.getInt(GameRuleKeys.MAX_SNOW_ACCUMULATION_HEIGHT) == 8, "Snow height should be clamped to 8");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void unknownRulesAreWrittenBack(final ScenarioTestHelper helper) {
        final GameRuleValues rules = new GameRuleValues();
        rules.load(CompoundBinaryTag.builder()
                .put("minecraft:rule_from_the_future", ByteBinaryTag.byteBinaryTag((byte) 1))
                .build());
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

    private static String snbt(final CompoundBinaryTag tag) {
        try {
            return TagStringIO.tagStringIO().asString(tag);
        } catch (final IOException e) {
            return tag.toString();
        }
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void worldsFollowTheBaseValueUnlessTheyOverrideIt(final ScenarioTestHelper helper) {
        final FidorialGameRules base = FidorialServer.getInstance().gameRules();
        final World other = overridingWorld();
        final WorldGameRules otherRules = other.gameRules();
        final List<GameRuleChangeEvent> seen = new ArrayList<>();
        final Subscription subscription = FidorialServer.getInstance().events().subscribe(GameRuleChangeEvent.class, event -> {
            if (event.rule().key().equals(GameRuleKeys.SPAWN_PATROLS)) {
                seen.add(event);
            }
        });
        try {
            helper.assertTrue(otherRules.setBoolean(GameRuleKeys.SPAWN_PATROLS, false), "Expected the override to be stored");
            helper.assertTrue(otherRules.isOverridden(GameRuleKeys.SPAWN_PATROLS), "Expected the world to override spawn_patrols");
            helper.assertTrue(!otherRules.getBoolean(GameRuleKeys.SPAWN_PATROLS), "Expected the override in effect in the world");
            helper.assertTrue(base.getBoolean(GameRuleKeys.SPAWN_PATROLS), "An override must not change the base value");

            base.setBoolean(GameRuleKeys.SPAWN_PATROLS, false);
            base.setBoolean(GameRuleKeys.SPAWN_PATROLS, true);
            helper.assertTrue(!otherRules.getBoolean(GameRuleKeys.SPAWN_PATROLS), "A base change must not reach an overriding world");

            helper.assertTrue(otherRules.removeOverride(GameRuleKeys.SPAWN_PATROLS), "Expected the override to be removed");
            helper.assertTrue(otherRules.getBoolean(GameRuleKeys.SPAWN_PATROLS), "Without override, the world follows the base value");
            helper.assertTrue(!otherRules.removeOverride(GameRuleKeys.SPAWN_PATROLS), "There is no override left to remove");

            helper.assertTrue(seen.size() == 4, "Expected 4 events (override, 2 base changes, removal), got " + seen.size());
            helper.assertTrue(seen.get(0).world().isPresent() && seen.get(1).world().isEmpty(),
                    "Override events carry their world, base events none");
            helper.assertTrue(seen.get(3).removesOverride() && seen.get(3).newValue().equals("true"),
                    "The removal event should announce the inherited value");
        } finally {
            subscription.unsubscribe();
            otherRules.removeOverride(GameRuleKeys.SPAWN_PATROLS);
            base.reset(GameRuleKeys.SPAWN_PATROLS);
        }
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void overriddenDamageRuleOnlyAppliesInItsWorld(final ScenarioTestHelper helper) {
        final World other = overridingWorld();
        final Player player = helper.summonPlayer("NoFallInOtherWorld", new Location(0.5, 65, 0.5, 0f, 0f), GameMode.SURVIVAL);
        final Location destination = new Location(0.5, 65, 0.5, 0f, 0f);
        final AtomicReference<CompletableFuture<Boolean>> teleport = new AtomicReference<>();
        final CompletableFuture<Boolean> hurt = new CompletableFuture<>();
        final AtomicBoolean hurtWhenOverriddenToTrue = new AtomicBoolean(false);

        helper.sequence()
                .execute(() -> teleport.set(player.teleport(other, destination)))
                .waitUntil(() -> teleport.get().isDone() && player.world().equals(other),
                        "Expected the player to reach the world overriding fall_damage")
                .execute(() -> helper.assertTrue(other.scheduler().execute(other.key(), new ChunkPos(0, 0), () -> {
                    other.gameRules().setBoolean(GameRuleKeys.FALL_DAMAGE, false);
                    try {
                        final boolean hurtWhenDisabled = player.damage(DamageSource.fall(), 4f);
                        other.gameRules().setBoolean(GameRuleKeys.FALL_DAMAGE, true);
                        hurtWhenOverriddenToTrue.set(player.damage(DamageSource.fall(), 4f));
                        hurt.complete(hurtWhenDisabled);
                    } catch (final RuntimeException e) {
                        hurt.completeExceptionally(e);
                    } finally {
                        other.gameRules().removeOverride(GameRuleKeys.FALL_DAMAGE);
                    }
                }), "Expected the damage to be scheduled in the other world"))
                .waitUntil(hurt::isDone, "Expected the fall damage to be applied in the other world")
                .execute(() -> helper.assertTrue(!hurt.join(), "Expected no fall damage where fall_damage is overridden to false"))
                .execute(() -> helper.assertTrue(hurtWhenOverriddenToTrue.get(), "Expected fall damage where fall_damage is overridden to true"))
                .execute(() -> helper.assertTrue(!other.gameRules().isOverridden(GameRuleKeys.FALL_DAMAGE),
                        "The override should be removed once the test is over"))
                .build();
    }

    private static World overridingWorld() {
        final FidorialServer server = FidorialServer.getInstance();
        final Key key = Key.key("scenario_test", "game_rule_overrides");
        final World world = server.worldManager().world(key);
        return world != null ? world : server.createWorld(WorldBuilder.builder(key).build());
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
