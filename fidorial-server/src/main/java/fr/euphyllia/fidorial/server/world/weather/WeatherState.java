package fr.euphyllia.fidorial.server.world.weather;

import net.kyori.adventure.nbt.CompoundBinaryTag;

public final class WeatherState {

    private static final String RAINING = "raining";
    private static final String RAIN_TIME = "rain_time";
    private static final String THUNDERING = "thundering";
    private static final String THUNDER_TIME = "thunder_time";
    private static final String CLEAR_WEATHER_TIME = "clear_weather_time";

    public boolean raining;
    public int rainTime;
    public boolean thundering;
    public int thunderTime;
    public int clearWeatherTime;

    public void load(final CompoundBinaryTag tag) {
        raining = tag.getBoolean(RAINING);
        rainTime = tag.getInt(RAIN_TIME);
        thundering = tag.getBoolean(THUNDERING);
        thunderTime = tag.getInt(THUNDER_TIME);
        clearWeatherTime = tag.getInt(CLEAR_WEATHER_TIME);
    }

    public void save(final CompoundBinaryTag.Builder builder) {
        builder.putBoolean(RAINING, raining);
        builder.putInt(RAIN_TIME, rainTime);
        builder.putBoolean(THUNDERING, thundering);
        builder.putInt(THUNDER_TIME, thunderTime);
        builder.putInt(CLEAR_WEATHER_TIME, clearWeatherTime);
    }
}
