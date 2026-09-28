package fr.euphyllia.fidorial.server.codecs.world;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.euphyllia.fidorial.server.codecs.CommonCodecs;
import fr.euphyllia.fidorial.server.codecs.RecordCodec;
import fr.euphyllia.fidorial.server.codecs.adventure.ComponentCodecs;
import fr.fidorial.world.environment.AdditionsSound;
import fr.fidorial.world.environment.AmbientParticle;
import fr.fidorial.world.environment.AmbientSounds;
import fr.fidorial.world.environment.Attribute;
import fr.fidorial.world.environment.BackgroundMusic;
import fr.fidorial.world.environment.BedRule;
import fr.fidorial.world.environment.EnvironmentAttributes;
import fr.fidorial.world.environment.Modifier;
import fr.fidorial.world.environment.MoodSound;
import fr.fidorial.world.environment.MusicTrack;

import java.util.Arrays;
import java.util.List;

public class EnvironmentAttributeCodecs {

    public static final Codec<Modifier> MODIFIER = Codec.STRING.comapFlatMap(
            name -> Arrays.stream(Modifier.values())
                    .filter(modifier -> modifier.id().equals(name))
                    .findFirst()
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown attribute modifier: " + name)),
            Modifier::id);

    public static final Codec<BedRule.AccessCondition> SLEEP_ACCESS = Codec.STRING.comapFlatMap(
            id -> BedRule.AccessCondition.fromId(id)
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown bed rule value: " + id)),
            BedRule.AccessCondition::id);

    public static final Codec<BedRule> BED_RULE = RecordCodec.builder(BedRule.class)
            .keys(RecordCodec.KeyStyle.UNCHECKED)
            .required("can_sleep", BedRule::sleepAllowed, SLEEP_ACCESS)
            .required("can_set_spawn", BedRule::spawnAllowed, SLEEP_ACCESS)
            .field("destroy_on_use", BedRule::destroysOnUse, Codec.BOOL, false)
            .field("destroy_on_leave", BedRule::destroysOnLeave, Codec.BOOL, false)
            .optional("error_message", BedRule::failureMessage, ComponentCodecs.COMPONENT_CODEC)
            .build();

    public static final Codec<AmbientParticle> AMBIENT_PARTICLE = RecordCodec.builder(AmbientParticle.class)
            .keys(RecordCodec.KeyStyle.UNCHECKED)
            .required("particle", AmbientParticle::type, BiomeCodecs.PARTICLE_OPTIONS)
            .required("probability", AmbientParticle::probability, Codec.FLOAT)
            .build();

    public static final Codec<MoodSound> MOOD_SOUND = RecordCodec.builder(MoodSound.class)
            .required("sound", MoodSound::sound, CommonCodecs.KEY_CODEC)
            .required("tick_delay", MoodSound::tickDelay, Codec.INT)
            .required("block_search_extent", MoodSound::blockSearchExtent, Codec.INT)
            .required("offset", MoodSound::offset, Codec.DOUBLE)
            .build();

    public static final Codec<AdditionsSound> ADDITIONS_SOUND = RecordCodec.builder(AdditionsSound.class)
            .required("sound", AdditionsSound::sound, CommonCodecs.KEY_CODEC)
            .required("tick_chance", AdditionsSound::tickChance, Codec.FLOAT)
            .build();

    public static final Codec<AmbientSounds> AMBIENT_SOUNDS = RecordCodec.builder(AmbientSounds.class)
            .nullable("loop", AmbientSounds::loop, CommonCodecs.KEY_CODEC)
            .nullable("mood", AmbientSounds::mood, MOOD_SOUND)
            .nullable("additions", AmbientSounds::additions, ADDITIONS_SOUND)
            .build();

    public static final Codec<MusicTrack> MUSIC_TRACK = RecordCodec.builder(MusicTrack.class)
            .required("sound", MusicTrack::sound, CommonCodecs.KEY_CODEC)
            .required("min_delay", MusicTrack::minDelay, Codec.INT)
            .required("max_delay", MusicTrack::maxDelay, Codec.INT)
            .field("replace_current_music", MusicTrack::replaceCurrentMusic, Codec.BOOL, false)
            .build();

    public static final Codec<BackgroundMusic> BACKGROUND_MUSIC = RecordCodec.builder(BackgroundMusic.class)
            .keys(RecordCodec.KeyStyle.UNCHECKED)
            .nullable("default", BackgroundMusic::normal, MUSIC_TRACK)
            .nullable("underwater", BackgroundMusic::underwater, MUSIC_TRACK)
            .nullable("creative", BackgroundMusic::creative, MUSIC_TRACK)
            .build();

    public static final Codec<EnvironmentAttributes> ATTRIBUTES = RecordCodec.builder(EnvironmentAttributes.class)
            .nullable("visual/fog_color", EnvironmentAttributes::fogColor, modifiable(CommonCodecs.RGB_COLOR))
            .nullable("visual/fog_start_distance", EnvironmentAttributes::fogStartDistance, modifiable(Codec.FLOAT))
            .nullable("visual/fog_end_distance", EnvironmentAttributes::fogEndDistance, modifiable(Codec.FLOAT))
            .nullable("visual/sky_fog_end_distance", EnvironmentAttributes::skyFogEndDistance, modifiable(Codec.FLOAT))
            .nullable("visual/cloud_fog_end_distance", EnvironmentAttributes::cloudFogEndDistance, modifiable(Codec.FLOAT))
            .nullable("visual/sky_color", EnvironmentAttributes::skyColor, modifiable(CommonCodecs.RGB_COLOR))
            .nullable("visual/sunrise_sunset_color", EnvironmentAttributes::sunriseSunsetColor, modifiable(CommonCodecs.ARGB_COLOR))
            .nullable("visual/cloud_color", EnvironmentAttributes::cloudColor, modifiable(CommonCodecs.ARGB_COLOR))
            .nullable("visual/cloud_height", EnvironmentAttributes::cloudHeight, modifiable(Codec.FLOAT))
            .nullable("visual/sun_angle", EnvironmentAttributes::sunAngle, modifiable(Codec.FLOAT))
            .nullable("visual/moon_angle", EnvironmentAttributes::moonAngle, modifiable(Codec.FLOAT))
            .nullable("visual/star_angle", EnvironmentAttributes::starAngle, modifiable(Codec.FLOAT))
            .nullable("visual/moon_phase", EnvironmentAttributes::moonPhase, modifiable(Codec.INT))
            .nullable("visual/star_brightness", EnvironmentAttributes::starBrightness, modifiable(Codec.FLOAT))
            .nullable("visual/block_light_tint", EnvironmentAttributes::blockLightTint, modifiable(CommonCodecs.RGB_COLOR))
            .nullable("visual/sky_light_color", EnvironmentAttributes::skyLightColor, modifiable(CommonCodecs.RGB_COLOR))
            .nullable("visual/sky_light_factor", EnvironmentAttributes::skyLightFactor, modifiable(Codec.FLOAT))
            .nullable("visual/night_vision_color", EnvironmentAttributes::nightVisionColor, modifiable(CommonCodecs.RGB_COLOR))
            .nullable("visual/ambient_light_color", EnvironmentAttributes::ambientLightColor, modifiable(CommonCodecs.RGB_COLOR))
            .nullable("visual/default_dripstone_particle", EnvironmentAttributes::defaultDripstoneParticle, modifiable(BiomeCodecs.PARTICLE_OPTIONS))
            .nullable("visual/water_fog_color", EnvironmentAttributes::waterFogColor, modifiable(CommonCodecs.RGB_COLOR))
            .nullable("visual/water_fog_start_distance", EnvironmentAttributes::waterFogStartDistance, modifiable(Codec.FLOAT))
            .nullable("visual/water_fog_end_distance", EnvironmentAttributes::waterFogEndDistance, modifiable(Codec.FLOAT))
            .nullable("audio/music_volume", EnvironmentAttributes::musicVolume, modifiable(Codec.FLOAT))
            .nullable("audio/firefly_bush_sounds", EnvironmentAttributes::fireflyBushSounds, modifiable(Codec.BOOL))
            .nullable("gameplay/can_start_raid", EnvironmentAttributes::canStartRaid, modifiable(Codec.BOOL))
            .nullable("gameplay/can_pillager_patrol_spawn", EnvironmentAttributes::canPillagerPatrolSpawn, modifiable(Codec.BOOL))
            .nullable("gameplay/water_evaporates", EnvironmentAttributes::waterEvaporates, modifiable(Codec.BOOL))
            .nullable("gameplay/bed_rule", EnvironmentAttributes::bedRule, modifiable(BED_RULE))
            .nullable("gameplay/straw_bed_rule", EnvironmentAttributes::strawBedRule, modifiable(BED_RULE))
            .nullable("gameplay/respawn_anchor_works", EnvironmentAttributes::respawnAnchorWorks, modifiable(Codec.BOOL))
            .nullable("gameplay/nether_portal_spawns_piglin", EnvironmentAttributes::netherPortalSpawnsPiglin, modifiable(Codec.BOOL))
            .nullable("gameplay/fast_lava", EnvironmentAttributes::fastLava, modifiable(Codec.BOOL))
            .nullable("gameplay/increased_fire_burnout", EnvironmentAttributes::increasedFireBurnout, modifiable(Codec.BOOL))
            .nullable("gameplay/eyeblossom_open", EnvironmentAttributes::eyeblossomOpen, modifiable(CommonCodecs.TRI_STATE))
            .nullable("gameplay/turtle_egg_hatch_chance", EnvironmentAttributes::turtleEggHatchChance, modifiable(Codec.FLOAT))
            .nullable("gameplay/piglins_zombify", EnvironmentAttributes::piglinsZombify, modifiable(Codec.BOOL))
            .nullable("gameplay/snow_golem_melts", EnvironmentAttributes::snowGolemMelts, modifiable(Codec.BOOL))
            .nullable("gameplay/creaking_active", EnvironmentAttributes::creakingActive, modifiable(Codec.BOOL))
            .nullable("gameplay/surface_slime_spawn_chance", EnvironmentAttributes::surfaceSlimeSpawnChance, modifiable(Codec.FLOAT))
            .nullable("gameplay/cat_waking_up_gift_chance", EnvironmentAttributes::catWakingUpGiftChance, modifiable(Codec.FLOAT))
            .nullable("gameplay/bees_stay_in_hive", EnvironmentAttributes::beesStayInHive, modifiable(Codec.BOOL))
            .nullable("gameplay/monsters_burn", EnvironmentAttributes::monstersBurn, modifiable(Codec.BOOL))
            .nullable("gameplay/creature_world_gen_spawn_probability", EnvironmentAttributes::creatureWorldGenSpawnProbability, modifiable(Codec.FLOAT))
            .nullable("gameplay/villager_activity", EnvironmentAttributes::villagerActivity, modifiable(CommonCodecs.KEY_CODEC))
            .nullable("gameplay/baby_villager_activity", EnvironmentAttributes::babyVillagerActivity, modifiable(CommonCodecs.KEY_CODEC))
            .nullable("gameplay/sky_light_level", EnvironmentAttributes::skyLightLevel, modifiable(Codec.FLOAT))
            .field("visual/ambient_particles", EnvironmentAttributes::ambientParticles, AMBIENT_PARTICLE.listOf(), List.of())
            .nullable("audio/ambient_sounds", EnvironmentAttributes::ambientSounds, AMBIENT_SOUNDS)
            .nullable("audio/background_music", EnvironmentAttributes::backgroundMusic, BACKGROUND_MUSIC)
            .build();

    private EnvironmentAttributeCodecs() {
        throw new UnsupportedOperationException("EnvironmentAttributeCodecs cannot be instantiated.");
    }

    public static <T> Codec<Attribute<T>> modifiable(final Codec<T> value) {
        final Codec<Attribute<T>> expanded = RecordCodecBuilder.create(instance -> instance.group(
                MODIFIER.fieldOf("modifier").forGetter(Attribute::modifier),
                value.fieldOf("argument").forGetter(Attribute::value)
        ).apply(instance, (modifier, argument) -> new Attribute<>(argument, modifier)));

        return Codec.either(value, expanded).xmap(
                either -> either.map(Attribute::of, attribute -> attribute),
                attribute -> attribute.modifier() == Modifier.OVERRIDE
                        ? Either.left(attribute.value())
                        : Either.right(attribute));
    }
}
