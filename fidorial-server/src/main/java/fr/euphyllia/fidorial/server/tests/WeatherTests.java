package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.World;
import fr.fidorial.world.WorldBuilder;
import fr.fidorial.world.weather.Weather;
import fr.fidorial.world.weather.WeatherManager;
import net.kyori.adventure.key.Key;

@SuppressWarnings("unused")
public final class WeatherTests {

    @ScenarioTest(timeoutTicks = 20)
    public static void eachWorldHasItsOwnWeather(final ScenarioTestHelper helper) {
        final WeatherManager overworld = FidorialServer.getInstance().weatherEngine().primaryWorld().weather();
        final Weather previous = overworld.weather();
        final WeatherManager other = world("weather_per_world").weather();
        try {
            overworld.setWeather(Weather.CLEAR);
            other.setWeather(Weather.THUNDER);

            helper.assertTrue(overworld.weather() == Weather.CLEAR, "Rain elsewhere must not reach the overworld");
            helper.assertTrue(other.isRaining() && other.isThundering(), "Expected a thunderstorm in the other world");
            helper.assertTrue(FidorialServer.getInstance().weatherEngine().weather() == Weather.CLEAR,
                    "The WeatherManager service should follow the overworld");
        } finally {
            other.setWeather(Weather.CLEAR);
            overworld.setWeather(previous);
        }
    }

    @ScenarioTest(timeoutTicks = 100)
    public static void advanceWeatherOverrideOnlyFreezesItsWorld(final ScenarioTestHelper helper) {
        final World frozen = world("weather_frozen");
        final World cycling = world("weather_cycling");

        helper.sequence()
                .execute(() -> {
                    frozen.gameRules().setBoolean(GameRuleKeys.ADVANCE_WEATHER, false);
                    cycling.gameRules().setBoolean(GameRuleKeys.ADVANCE_WEATHER, true);
                    frozen.weather().setWeather(Weather.RAIN, 1);
                    cycling.weather().setWeather(Weather.RAIN, 1);
                })
                .waitUntil(() -> !cycling.weather().isRaining(), "The rain should stop where the weather advances")
                .execute(() -> helper.assertTrue(frozen.weather().isRaining(),
                        "The rain should go on where advance_weather is overridden to false"))
                .execute(() -> {
                    frozen.gameRules().removeOverride(GameRuleKeys.ADVANCE_WEATHER);
                    cycling.gameRules().removeOverride(GameRuleKeys.ADVANCE_WEATHER);
                    frozen.weather().setWeather(Weather.CLEAR);
                })
                .build();
    }

    private static World world(final String name) {
        final FidorialServer server = FidorialServer.getInstance();
        final Key key = Key.key("scenario_test", name);
        final World world = server.worldManager().world(key);
        return world != null ? world : server.createWorld(WorldBuilder.builder(key).build());
    }
}
