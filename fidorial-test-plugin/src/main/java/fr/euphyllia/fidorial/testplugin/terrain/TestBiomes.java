package fr.euphyllia.fidorial.testplugin.terrain;

import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.WritableRegistry;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.biome.GrassColorModifier;
import fr.fidorial.world.biome.TemperatureModifier;
import fr.fidorial.world.environment.AdditionsSound;
import fr.fidorial.world.environment.AmbientParticle;
import fr.fidorial.world.environment.AmbientSounds;
import fr.fidorial.world.environment.BackgroundMusic;
import fr.fidorial.world.environment.Modifier;
import fr.fidorial.world.environment.MoodSound;
import fr.fidorial.world.environment.MusicTrack;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.List;

public final class TestBiomes {

    public static final BiomeDefinition VOLCANIC_PLAINS = BiomeDefinition
            .builder(Key.key("fidorial", "volcanic_plains"))
            .temperature(2.0F)
            .downfall(0.0F)
            .hasPrecipitation(false)
            .effects(effects -> effects
                    .waterColor(0x8C2E00)
                    .grassColor(0x6B4423)
                    .foliageColor(0x5A3A1E))
            .skyColor(0x992200)
            .fogColor(0x3F1A0A)
            .waterFogColor(0x3A0F00)
            .addAmbientParticle(AmbientParticle.of(Key.key("ash"), 0.02F))
            .ambientSounds(new AmbientSounds(
                    Key.key("ambient.nether_wastes.loop"),
                    MoodSound.CAVE,
                    AdditionsSound.of(Key.key("ambient.nether_wastes.additions"), 0.0111F)))
            .attributes(attributes -> attributes
                    .increasedFireBurnout(false)
                    .snowGolemMelts(true)
                    .waterEvaporates(true))
            .build();

    public static final BiomeDefinition FROZEN_PEAKS = BiomeDefinition
            .builder(Key.key("fidorial", "frozen_peaks"))
            .temperature(-0.7F)
            .temperatureModifier(TemperatureModifier.FROZEN)
            .downfall(0.9F)
            .hasPrecipitation(true)
            .effects(effects -> effects
                    .waterColor(0x3938C9)
                    .grassColor(0x80B497))
            .vanillaSkyColor()
            .fogColor(0xC0D8FF)
            .backgroundMusic(BackgroundMusic.of(
                    MusicTrack.of(Key.key("music.overworld.frozen_peaks"), 12000, 24000)))
            .build();

    public static final BiomeDefinition TOXIC_RIVER = BiomeDefinition
            .builder(Key.key("fidorial", "toxic_river"))
            .temperature(0.8F)
            .downfall(0.9F)
            .effects(effects -> effects
                    .waterColor(0x4C6314)
                    .grassColor(0x6A7039)
                    .foliageColor(0x6A7039)
                    .grassColorModifier(GrassColorModifier.SWAMP))
            .skyColor(0x78A7FF)
            .waterFogColor(0x232317)
            .attributes(attributes -> attributes
                    .waterFogEndDistance(0.35F, Modifier.MULTIPLY)
                    .increasedFireBurnout(true))
            .build();

    public static final BiomeDefinition CRYSTAL_SHORE = BiomeDefinition
            .builder(Key.key("fidorial", "crystal_shore"))
            .temperature(0.5F)
            .downfall(0.4F)
            .effects(effects -> effects.waterColor(0x5DB7EF))
            .skyColor(0x5DB7EF)
            .build();

    public static final BiomeDefinition ASHEN_WASTELAND = BiomeDefinition
            .builder(Key.key("fidorial", "ashen_wasteland"))
            .temperature(1.6F)
            .downfall(0.0F)
            .hasPrecipitation(false)
            .effects(effects -> effects
                    .waterColor(0x6B6252)
                    .grassColor(0x8A7A5C)
                    .foliageColor(0x746653))
            .skyColor(0x9C9484)
            .fogColor(0xA69D8C)
            .waterFogColor(0x4A4436)
            .addAmbientParticle(AmbientParticle.of(Key.key("ash"), 0.01F))
            .ambientSounds(new AmbientSounds(
                    Key.key("ambient.basalt_deltas.loop"),
                    MoodSound.CAVE,
                    AdditionsSound.of(Key.key("ambient.basalt_deltas.additions"), 0.0111F)))
            .attributes(attributes -> attributes
                    .increasedFireBurnout(false)
                    .snowGolemMelts(true)
                    .waterEvaporates(true))
            .build();

    public static final BiomeDefinition TOXIC_CRATERS = BiomeDefinition
            .builder(Key.key("fidorial", "toxic_craters"))
            .temperature(0.9F)
            .downfall(0.9F)
            .effects(effects -> effects
                    .waterColor(0x8FA83B)
                    .grassColor(0x6B6B34)
                    .foliageColor(0x6B6B34)
                    .grassColorModifier(GrassColorModifier.SWAMP))
            .skyColor(0x746B4A)
            .waterFogColor(0x1F2410)
            .attributes(attributes -> attributes
                    .waterFogEndDistance(0.25F, Modifier.MULTIPLY)
                    .increasedFireBurnout(true))
            .build();

    public static final BiomeDefinition MOLTEN_CRATERS = BiomeDefinition
            .builder(Key.key("fidorial", "molten_craters"))
            .temperature(2.0F)
            .downfall(0.0F)
            .hasPrecipitation(false)
            .effects(effects -> effects
                    .waterColor(0xB33F00)
                    .grassColor(0x5C3A22)
                    .foliageColor(0x4A2E1A))
            .skyColor(0x8C3300)
            .fogColor(0x4A1D08)
            .waterFogColor(0x3A1200)
            .addAmbientParticle(AmbientParticle.of(Key.key("white_ash"), 0.02F))
            .ambientSounds(new AmbientSounds(
                    Key.key("ambient.nether_wastes.loop"),
                    MoodSound.CAVE,
                    AdditionsSound.of(Key.key("ambient.nether_wastes.additions"), 0.0111F)))
            .attributes(attributes -> attributes
                    .increasedFireBurnout(true)
                    .snowGolemMelts(true)
                    .waterEvaporates(true))
            .build();

    public static final BiomeDefinition RUBBLE_FLATS = BiomeDefinition
            .builder(Key.key("fidorial", "rubble_flats"))
            .temperature(1.3F)
            .downfall(0.2F)
            .effects(effects -> effects.waterColor(0x736B5C))
            .skyColor(0x8C8579)
            .build();

    public static final BiomeDefinition IRRADIATED_RIDGES = BiomeDefinition
            .builder(Key.key("fidorial", "irradiated_ridges"))
            .temperature(0.3F)
            .downfall(0.1F)
            .hasPrecipitation(false)
            .effects(effects -> effects
                    .waterColor(0x536655)
                    .grassColor(0x4C5A3E))
            .skyColor(0x554F42)
            .fogColor(0x726A54)
            .addAmbientParticle(AmbientParticle.of(Key.key("ash"), 0.015F))
            .build();

    private static final List<BiomeDefinition> ALL =
            List.of(VOLCANIC_PLAINS, FROZEN_PEAKS, TOXIC_RIVER, CRYSTAL_SHORE,
                    ASHEN_WASTELAND, TOXIC_CRATERS, MOLTEN_CRATERS, RUBBLE_FLATS, IRRADIATED_RIDGES);

    private TestBiomes() {
        throw new UnsupportedOperationException("TestBiomes cannot be instantiated.");
    }

    public static List<BiomeDefinition> all() {
        return ALL;
    }

    public static void registerAll(final WritableRegistry<BiomeDefinition> biomes, final Plugin owner,
                                   final ComponentLogger logger) {
        for (final BiomeDefinition biome : ALL) {
            biomes.overwrite(biome.typedKey(), biome, owner);
            logger.info("[TestPlugin] biome {} registered", biome.key().asString());
        }

        logger.info("[TestPlugin] {} biomes registered", biomes.size());
    }

    public static void unregisterAll(final WritableRegistry<BiomeDefinition> biomes) {
        for (final BiomeDefinition biome : ALL) {
            biomes.unregister(biome.typedKey());
        }
    }
}
