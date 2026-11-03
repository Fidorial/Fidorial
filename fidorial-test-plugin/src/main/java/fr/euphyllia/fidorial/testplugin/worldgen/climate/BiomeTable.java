package fr.euphyllia.fidorial.testplugin.worldgen.climate;

import fr.fidorial.registry.keys.BiomeKeys;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

public final class BiomeTable {

    private static final Key FROZEN_OCEAN = BiomeKeys.FROZEN_OCEAN;
    private static final Key COLD_OCEAN = BiomeKeys.COLD_OCEAN;
    private static final Key OCEAN = BiomeKeys.OCEAN;
    private static final Key LUKEWARM_OCEAN = BiomeKeys.LUKEWARM_OCEAN;
    private static final Key WARM_OCEAN = BiomeKeys.WARM_OCEAN;
    private static final Key DEEP_FROZEN_OCEAN = BiomeKeys.DEEP_FROZEN_OCEAN;
    private static final Key DEEP_COLD_OCEAN = BiomeKeys.DEEP_COLD_OCEAN;
    private static final Key DEEP_OCEAN = BiomeKeys.DEEP_OCEAN;
    private static final Key DEEP_LUKEWARM_OCEAN = BiomeKeys.DEEP_LUKEWARM_OCEAN;
    private static final Key MUSHROOM_FIELDS = BiomeKeys.MUSHROOM_FIELDS;

    private static final Key RIVER = BiomeKeys.RIVER;
    private static final Key FROZEN_RIVER = BiomeKeys.FROZEN_RIVER;
    private static final Key BEACH = BiomeKeys.BEACH;
    private static final Key SNOWY_BEACH = BiomeKeys.SNOWY_BEACH;
    private static final Key STONY_SHORE = BiomeKeys.STONY_SHORE;

    private static final Key PLAINS = BiomeKeys.PLAINS;
    private static final Key SUNFLOWER_PLAINS = BiomeKeys.SUNFLOWER_PLAINS;
    private static final Key SNOWY_PLAINS = BiomeKeys.SNOWY_PLAINS;
    private static final Key ICE_SPIKES = BiomeKeys.ICE_SPIKES;
    private static final Key FOREST = BiomeKeys.FOREST;
    private static final Key FLOWER_FOREST = BiomeKeys.FLOWER_FOREST;
    private static final Key BIRCH_FOREST = BiomeKeys.BIRCH_FOREST;
    private static final Key OLD_GROWTH_BIRCH_FOREST = BiomeKeys.OLD_GROWTH_BIRCH_FOREST;
    private static final Key DARK_FOREST = BiomeKeys.DARK_FOREST;
    private static final Key PALE_GARDEN = BiomeKeys.PALE_GARDEN;
    private static final Key TAIGA = BiomeKeys.TAIGA;
    private static final Key SNOWY_TAIGA = BiomeKeys.SNOWY_TAIGA;
    private static final Key OLD_GROWTH_SPRUCE_TAIGA = BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA;
    private static final Key OLD_GROWTH_PINE_TAIGA = BiomeKeys.OLD_GROWTH_PINE_TAIGA;
    private static final Key JUNGLE = BiomeKeys.JUNGLE;
    private static final Key SPARSE_JUNGLE = BiomeKeys.SPARSE_JUNGLE;
    private static final Key BAMBOO_JUNGLE = BiomeKeys.BAMBOO_JUNGLE;
    private static final Key SAVANNA = BiomeKeys.SAVANNA;
    private static final Key SAVANNA_PLATEAU = BiomeKeys.SAVANNA_PLATEAU;
    private static final Key WINDSWEPT_SAVANNA = BiomeKeys.WINDSWEPT_SAVANNA;
    private static final Key DESERT = BiomeKeys.DESERT;
    private static final Key SWAMP = BiomeKeys.SWAMP;
    private static final Key MANGROVE_SWAMP = BiomeKeys.MANGROVE_SWAMP;
    private static final Key MEADOW = BiomeKeys.MEADOW;
    private static final Key CHERRY_GROVE = BiomeKeys.CHERRY_GROVE;
    private static final Key GROVE = BiomeKeys.GROVE;
    private static final Key SNOWY_SLOPES = BiomeKeys.SNOWY_SLOPES;
    private static final Key JAGGED_PEAKS = BiomeKeys.JAGGED_PEAKS;
    private static final Key FROZEN_PEAKS = BiomeKeys.FROZEN_PEAKS;
    private static final Key STONY_PEAKS = BiomeKeys.STONY_PEAKS;
    private static final Key BADLANDS = BiomeKeys.BADLANDS;
    private static final Key ERODED_BADLANDS = BiomeKeys.ERODED_BADLANDS;
    private static final Key WOODED_BADLANDS = BiomeKeys.WOODED_BADLANDS;
    private static final Key WINDSWEPT_HILLS = BiomeKeys.WINDSWEPT_HILLS;
    private static final Key WINDSWEPT_GRAVELLY_HILLS = BiomeKeys.WINDSWEPT_GRAVELLY_HILLS;
    private static final Key WINDSWEPT_FOREST = BiomeKeys.WINDSWEPT_FOREST;

    private static final Key LUSH_CAVES = BiomeKeys.LUSH_CAVES;
    private static final Key DRIPSTONE_CAVES = BiomeKeys.DRIPSTONE_CAVES;
    private static final Key SULFUR_CAVES = BiomeKeys.SULFUR_CAVES;
    private static final Key DEEP_DARK = BiomeKeys.DEEP_DARK;

    private BiomeTable() {
        throw new UnsupportedOperationException("BiomeTable cannot be instantiated.");
    }

    public static Key surface(final ClimatePoint point) {
        final int continent = point.continentLevel();
        final int temperature = point.temperatureLevel();

        if (continent == 0) {
            return MUSHROOM_FIELDS;
        }
        if (continent == 1) {
            return deepOcean(temperature);
        }
        if (continent == 2) {
            return ocean(temperature);
        }
        return inland(point, continent);
    }

    public static @Nullable Key cave(final ClimatePoint point, final int y, final int surfaceY) {
        final double depth = (surfaceY - y) / 128.0;
        if (depth < 0.2) {
            return null;
        }
        if (point.erosion() < -0.375 && y <= -18) {
            return DEEP_DARK;
        }
        if (point.humidity() > 0.62) {
            return LUSH_CAVES;
        }
        if (point.continentalness() > 0.75) {
            return DRIPSTONE_CAVES;
        }
        if (point.weirdness() < -0.7
                && point.continentalness() > -0.19
                && point.continentalness() < 0.55
                && point.erosion() > 0.45) {
            return SULFUR_CAVES;
        }
        return null;
    }

    private static Key ocean(final int temperature) {
        return switch (temperature) {
            case 0 -> FROZEN_OCEAN;
            case 1 -> COLD_OCEAN;
            case 2 -> OCEAN;
            case 3 -> LUKEWARM_OCEAN;
            default -> WARM_OCEAN;
        };
    }

    private static Key deepOcean(final int temperature) {
        return switch (temperature) {
            case 0 -> DEEP_FROZEN_OCEAN;
            case 1 -> DEEP_COLD_OCEAN;
            case 2 -> DEEP_OCEAN;
            default -> DEEP_LUKEWARM_OCEAN;
        };
    }

    private static Key inland(final ClimatePoint point, final int continent) {
        final int erosion = point.erosionLevel();
        final int pv = point.peaksValleysLevel();
        final int temperature = point.temperatureLevel();

        return switch (erosion) {
            case 0 -> erosion0(point, continent, pv, temperature);
            case 1 -> erosion1(point, continent, pv, temperature);
            case 2 -> erosion2(point, continent, pv, temperature);
            case 3 -> erosion3(point, continent, pv, temperature);
            case 4 -> erosion4(point, continent, pv);
            case 5 -> erosion5(point, continent, pv, temperature);
            default -> erosion6(point, continent, pv, temperature);
        };
    }

    private static Key erosion0(
            final ClimatePoint point, final int continent, final int pv, final int temperature) {
        switch (pv) {
            case 0 -> {
                if (continent <= 4) {
                    return river(temperature);
                }
                return temperature == 4 ? badlands(point) : middle(point, true);
            }
            case 1 -> {
                if (continent == 3) {
                    return STONY_SHORE;
                }
                if (continent == 4) {
                    return temperature == 4 ? badlands(point) : middle(point, false);
                }
                if (temperature == 0) {
                    return point.humidityLevel() <= 1 ? SNOWY_SLOPES : GROVE;
                }
                return temperature == 4 ? badlands(point) : middle(point, false);
            }
            case 2 -> {
                if (continent == 3) {
                    return STONY_SHORE;
                }
                if (temperature < 3) {
                    return point.humidityLevel() <= 1 ? SNOWY_SLOPES : GROVE;
                }
                return plateau(point);
            }
            case 3 -> {
                if (continent == 3) {
                    return temperature == 4 ? badlands(point) : middle(point, false);
                }
                if (continent == 4) {
                    if (temperature < 3) {
                        return point.humidityLevel() <= 1 ? SNOWY_SLOPES : GROVE;
                    }
                    return plateau(point);
                }
                return peaks(point, temperature);
            }
            default -> {
                return peaks(point, temperature);
            }
        }
    }

    private static Key erosion1(
            final ClimatePoint point, final int continent, final int pv, final int temperature) {
        switch (pv) {
            case 0 -> {
                if (continent <= 4) {
                    return river(temperature);
                }
                return temperature == 4 ? badlands(point) : middle(point, true);
            }
            case 1 -> {
                if (continent == 3) {
                    return STONY_SHORE;
                }
                if (continent == 4) {
                    return temperature == 4 ? badlands(point) : middle(point, false);
                }
                if (temperature == 0) {
                    return point.humidityLevel() <= 1 ? SNOWY_SLOPES : GROVE;
                }
                return temperature == 4 ? badlands(point) : middle(point, false);
            }
            case 2 -> {
                if (continent == 3) {
                    return STONY_SHORE;
                }
                if (temperature == 0) {
                    return point.humidityLevel() <= 1 ? SNOWY_SLOPES : GROVE;
                }
                if (continent == 6) {
                    return plateau(point);
                }
                return temperature == 4 ? badlands(point) : middle(point, false);
            }
            case 3 -> {
                if (continent == 3) {
                    return temperature == 4 ? badlands(point) : middle(point, false);
                }
                if (continent == 4) {
                    if (temperature == 0) {
                        return point.humidityLevel() <= 1 ? SNOWY_SLOPES : GROVE;
                    }
                    return temperature == 4 ? badlands(point) : middle(point, false);
                }
                if (temperature < 3) {
                    return point.humidityLevel() <= 1 ? SNOWY_SLOPES : GROVE;
                }
                return plateau(point);
            }
            default -> {
                if (continent <= 4) {
                    if (temperature == 0) {
                        return point.humidityLevel() <= 1 ? SNOWY_SLOPES : GROVE;
                    }
                    return temperature == 4 ? badlands(point) : middle(point, false);
                }
                return peaks(point, temperature);
            }
        }
    }

    private static Key erosion2(
            final ClimatePoint point, final int continent, final int pv, final int temperature) {
        switch (pv) {
            case 0 -> {
                return river(temperature);
            }
            case 1 -> {
                if (continent == 3) {
                    return STONY_SHORE;
                }
                if (continent == 4) {
                    return middle(point, false);
                }
                return temperature == 4 ? badlands(point) : middle(point, false);
            }
            case 2 -> {
                if (continent == 3) {
                    return STONY_SHORE;
                }
                if (continent == 4) {
                    return middle(point, false);
                }
                if (continent == 6) {
                    return plateau(point);
                }
                return temperature == 4 ? badlands(point) : middle(point, false);
            }
            default -> {
                if (continent <= 4) {
                    return middle(point, false);
                }
                return plateau(point);
            }
        }
    }

    private static Key erosion3(
            final ClimatePoint point, final int continent, final int pv, final int temperature) {
        switch (pv) {
            case 0 -> {
                return river(temperature);
            }
            case 1 -> {
                if (continent == 3) {
                    return beach(temperature);
                }
                if (continent == 4) {
                    return middle(point, false);
                }
                return temperature == 4 ? badlands(point) : middle(point, false);
            }
            case 2 -> {
                return middle(point, false);
            }
            default -> {
                if (continent == 6) {
                    return plateau(point);
                }
                if (continent == 3) {
                    return middle(point, false);
                }
                return temperature == 4 ? badlands(point) : middle(point, false);
            }
        }
    }

    private static Key erosion4(final ClimatePoint point, final int continent, final int pv) {
        final int temperature = point.temperatureLevel();
        switch (pv) {
            case 0 -> {
                return river(temperature);
            }
            case 1 -> {
                return continent == 3 ? beach(temperature) : middle(point, false);
            }
            case 2 -> {
                if (continent == 3) {
                    return point.weirdness() < 0 ? beach(temperature) : middle(point, false);
                }
                return middle(point, false);
            }
            default -> {
                return middle(point, false);
            }
        }
    }

    private static Key erosion5(
            final ClimatePoint point, final int continent, final int pv, final int temperature) {
        final int humidity = point.humidityLevel();
        final boolean windswept = point.weirdness() > 0 && temperature >= 2 && humidity <= 3;

        switch (pv) {
            case 0 -> {
                return river(temperature);
            }
            case 1 -> {
                if (continent == 3) {
                    if (point.weirdness() < 0) {
                        return beach(temperature);
                    }
                    return windswept ? WINDSWEPT_SAVANNA : middle(point, false);
                }
                if (continent == 4) {
                    return windswept ? WINDSWEPT_SAVANNA : middle(point, false);
                }
                return middle(point, false);
            }
            case 2, 3 -> {
                if (continent <= 4) {
                    return windswept ? WINDSWEPT_SAVANNA : middle(point, false);
                }
                return shattered(point);
            }
            default -> {
                if (continent <= 4) {
                    return windswept ? WINDSWEPT_SAVANNA : shattered(point);
                }
                return shattered(point);
            }
        }
    }

    private static Key erosion6(
            final ClimatePoint point, final int continent, final int pv, final int temperature) {
        switch (pv) {
            case 0 -> {
                if (continent == 3) {
                    return river(temperature);
                }
                return swamp(temperature);
            }
            case 1 -> {
                if (continent == 3) {
                    return beach(temperature);
                }
                return temperature == 0 ? middle(point, false) : swamp(temperature);
            }
            case 2 -> {
                if (continent == 3) {
                    return point.weirdness() < 0 ? beach(temperature) : middle(point, false);
                }
                return middle(point, false);
            }
            default -> {
                return middle(point, false);
            }
        }
    }

    private static Key swamp(final int temperature) {
        if (temperature == 0) {
            return FROZEN_RIVER;
        }
        return temperature >= 3 ? MANGROVE_SWAMP : SWAMP;
    }

    private static Key river(final int temperature) {
        return temperature == 0 ? FROZEN_RIVER : RIVER;
    }

    private static Key beach(final int temperature) {
        if (temperature == 0) {
            return SNOWY_BEACH;
        }
        return temperature == 4 ? DESERT : BEACH;
    }

    private static Key peaks(final ClimatePoint point, final int temperature) {
        if (temperature <= 2) {
            return point.weirdness() < 0 ? JAGGED_PEAKS : FROZEN_PEAKS;
        }
        return temperature == 3 ? STONY_PEAKS : badlands(point);
    }

    private static Key badlands(final ClimatePoint point) {
        final int humidity = point.humidityLevel();
        if (humidity <= 1) {
            return point.weirdness() < 0 ? BADLANDS : ERODED_BADLANDS;
        }
        return humidity == 2 ? BADLANDS : WOODED_BADLANDS;
    }

    private static Key middle(final ClimatePoint point, final boolean forceWeird) {
        final int temperature = point.temperatureLevel();
        final int humidity = point.humidityLevel();
        final boolean weird = forceWeird || point.weirdness() > 0;

        return switch (temperature) {
            case 0 -> switch (humidity) {
                case 0 -> weird ? ICE_SPIKES : SNOWY_PLAINS;
                case 1 -> SNOWY_PLAINS;
                case 2 -> weird ? SNOWY_TAIGA : SNOWY_PLAINS;
                case 3 -> SNOWY_TAIGA;
                default -> TAIGA;
            };
            case 1 -> switch (humidity) {
                case 0, 1 -> PLAINS;
                case 2 -> FOREST;
                case 3 -> TAIGA;
                default -> weird ? OLD_GROWTH_PINE_TAIGA : OLD_GROWTH_SPRUCE_TAIGA;
            };
            case 2 -> switch (humidity) {
                case 0 -> weird ? SUNFLOWER_PLAINS : FLOWER_FOREST;
                case 1 -> PLAINS;
                case 2 -> FOREST;
                case 3 -> weird ? OLD_GROWTH_BIRCH_FOREST : BIRCH_FOREST;
                default -> DARK_FOREST;
            };
            case 3 -> switch (humidity) {
                case 0, 1 -> SAVANNA;
                case 2 -> weird ? PLAINS : FOREST;
                case 3 -> weird ? SPARSE_JUNGLE : JUNGLE;
                default -> weird ? BAMBOO_JUNGLE : JUNGLE;
            };
            default -> DESERT;
        };
    }

    private static Key plateau(final ClimatePoint point) {
        final int temperature = point.temperatureLevel();
        final int humidity = point.humidityLevel();
        final boolean weird = point.weirdness() > 0;

        return switch (temperature) {
            case 0 -> switch (humidity) {
                case 0 -> weird ? ICE_SPIKES : SNOWY_PLAINS;
                case 1, 2 -> SNOWY_PLAINS;
                default -> SNOWY_TAIGA;
            };
            case 1 -> switch (humidity) {
                case 0 -> weird ? CHERRY_GROVE : MEADOW;
                case 1 -> MEADOW;
                case 2 -> weird ? MEADOW : FOREST;
                case 3 -> weird ? MEADOW : TAIGA;
                default -> weird ? OLD_GROWTH_PINE_TAIGA : OLD_GROWTH_SPRUCE_TAIGA;
            };
            case 2 -> switch (humidity) {
                case 0 -> weird ? CHERRY_GROVE : MEADOW;
                case 1 -> MEADOW;
                case 2 -> weird ? FOREST : MEADOW;
                case 3 -> weird ? BIRCH_FOREST : MEADOW;
                default -> PALE_GARDEN;
            };
            case 3 -> switch (humidity) {
                case 0, 1 -> SAVANNA_PLATEAU;
                case 2, 3 -> FOREST;
                default -> JUNGLE;
            };
            default -> switch (humidity) {
                case 0, 1 -> weird ? ERODED_BADLANDS : BADLANDS;
                case 2 -> BADLANDS;
                default -> WOODED_BADLANDS;
            };
        };
    }

    private static Key shattered(final ClimatePoint point) {
        final int temperature = point.temperatureLevel();
        final int humidity = point.humidityLevel();
        final boolean weird = point.weirdness() > 0;

        if (temperature == 4) {
            return DESERT;
        }
        return switch (humidity) {
            case 0, 1 -> switch (temperature) {
                case 0, 1 -> WINDSWEPT_GRAVELLY_HILLS;
                case 2 -> WINDSWEPT_HILLS;
                default -> SAVANNA;
            };
            case 2 -> temperature <= 2 ? WINDSWEPT_HILLS : (weird ? PLAINS : FOREST);
            case 3 -> temperature <= 2 ? WINDSWEPT_FOREST : (weird ? SPARSE_JUNGLE : JUNGLE);
            default -> switch (temperature) {
                case 0, 1 -> WINDSWEPT_FOREST;
                case 2 -> weird ? BAMBOO_JUNGLE : JUNGLE;
                default -> JUNGLE;
            };
        };
    }
}
