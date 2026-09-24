package fr.fidorial.world.weather;

import fr.fidorial.world.World;

/**
 * The weather of a world, obtained with {@link World#weather()}. The instance registered
 * as a service manages the weather of the overworld.
 *
 * @since 0.1.0
 */
public interface WeatherManager {

    Weather weather();

    boolean isRaining();

    boolean isThundering();

    void setWeather(Weather weather, int durationTicks);

    default void setWeather(final Weather weather) {
        setWeather(weather, 0);
    }
}
