package fr.fidorial.world.weather;

import fr.fidorial.world.World;

/**
 * The weather of a world, obtained with {@link World#weather()}. The instance registered
 * as a service manages the weather of the overworld.
 *
 * @since 0.1.0
 */
public interface WeatherManager {

    /**
     * {@return the current weather}
     *
     * @since 0.1.0
     */
    Weather weather();

    /**
     * {@return {@code true} while it rains, thunderstorms included}
     *
     * @since 0.1.0
     */
    boolean isRaining();

    /**
     * {@return {@code true} during a thunderstorm}
     *
     * @since 0.1.0
     */
    boolean isThundering();

    /**
     * Changes the weather.
     *
     * @param weather       the new weather
     * @param durationTicks how long it lasts before changing on its own; {@code 0} for a random duration
     * @since 0.1.0
     */
    void setWeather(Weather weather, int durationTicks);

    /**
     * Changes the weather for a random duration.
     *
     * @param weather the new weather
     * @since 0.1.0
     */
    default void setWeather(final Weather weather) {
        setWeather(weather, 0);
    }
}
