package fr.euphyllia.fidorial.server.codecs.world;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.euphyllia.fidorial.server.codecs.DispatchCodecs;
import fr.euphyllia.fidorial.server.codecs.RecordCodec;
import fr.fidorial.world.dimension.IntProvider;

import java.util.List;

public final class IntProviderCodecs {

    public static final Codec<IntProvider> INT_PROVIDER =
            Codec.recursive("fidorial:int_provider", IntProviderCodecs::createCodec);

    private IntProviderCodecs() {
        throw new UnsupportedOperationException("IntProviderCodecs cannot be instantiated.");
    }

    private static Codec<IntProvider> createCodec(final Codec<IntProvider> self) {
        final MapCodec<IntProvider.Constant> constantCodec = RecordCodec.builder(IntProvider.Constant.class)
                .required("value", IntProvider.Constant::value, Codec.INT)
                .buildMap();

        final MapCodec<IntProvider.Uniform> uniformCodec = RecordCodec.builder(IntProvider.Uniform.class)
                .required("min_inclusive", IntProvider.Uniform::minInclusive, Codec.INT)
                .required("max_inclusive", IntProvider.Uniform::maxInclusive, Codec.INT)
                .buildMap();

        final MapCodec<IntProvider.BiasedToBottom> biasedCodec = RecordCodec.builder(IntProvider.BiasedToBottom.class)
                .required("min_inclusive", IntProvider.BiasedToBottom::minInclusive, Codec.INT)
                .required("max_inclusive", IntProvider.BiasedToBottom::maxInclusive, Codec.INT)
                .buildMap();

        final MapCodec<IntProvider.Clamped> clampedCodec = RecordCodec.builder(IntProvider.Clamped.class)
                .required("min_inclusive", IntProvider.Clamped::minInclusive, Codec.INT)
                .required("max_inclusive", IntProvider.Clamped::maxInclusive, Codec.INT)
                .required("source", IntProvider.Clamped::source, self)
                .buildMap();

        final MapCodec<IntProvider.ClampedNormal> clampedNormalCodec = RecordCodec.builder(IntProvider.ClampedNormal.class)
                .required("mean", IntProvider.ClampedNormal::mean, Codec.FLOAT)
                .required("deviation", IntProvider.ClampedNormal::deviation, Codec.FLOAT)
                .required("min_inclusive", IntProvider.ClampedNormal::minInclusive, Codec.INT)
                .required("max_inclusive", IntProvider.ClampedNormal::maxInclusive, Codec.INT)
                .buildMap();

        final Codec<IntProvider.WeightedList.Entry> entryCodec = RecordCodec.builder(IntProvider.WeightedList.Entry.class)
                .required("data", IntProvider.WeightedList.Entry::data, self)
                .required("weight", IntProvider.WeightedList.Entry::weight, Codec.INT)
                .build();

        final MapCodec<IntProvider.WeightedList> weightedListCodec = RecordCodec.builder(IntProvider.WeightedList.class)
                .required("distribution", IntProvider.WeightedList::distribution, entryCodec.listOf())
                .buildMap();

        final Codec<IntProvider> dispatch = DispatchCodecs.<IntProvider>matcher("type", List.of(
                DispatchCodecs.Variant.of("constant", null, true, IntProvider.Constant.class, constantCodec),
                DispatchCodecs.Variant.of("uniform", null, true, IntProvider.Uniform.class, uniformCodec),
                DispatchCodecs.Variant.of(
                        "biased_to_bottom", null, true, IntProvider.BiasedToBottom.class, biasedCodec),
                DispatchCodecs.Variant.of("clamped", null, true, IntProvider.Clamped.class, clampedCodec),
                DispatchCodecs.Variant.of(
                        "clamped_normal", null, true, IntProvider.ClampedNormal.class, clampedNormalCodec),
                DispatchCodecs.Variant.of(
                        "weighted_list", null, true, IntProvider.WeightedList.class, weightedListCodec)
        )).codec();

        return Codec.either(Codec.INT, dispatch).xmap(
                either -> either.map(IntProvider.Constant::new, provider -> provider),
                provider -> provider instanceof IntProvider.Constant(final int value)
                        ? Either.left(value)
                        : Either.right(provider));
    }
}
