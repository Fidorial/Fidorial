package fr.euphyllia.fidorial.server.world.structure.processor;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.structure.math.LegacyRandom;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.block.BlockType;

import java.util.Set;

public interface RuleTest {

    RuleTest ALWAYS_TRUE = (state, random) -> true;

    boolean test(BlockState state, LegacyRandom random);

    static RuleTest block(final TypedKey<BlockType> block) {
        return (state, random) -> state.name().equals(block);
    }

    static RuleTest blockState(final BlockState expected) {
        return (state, random) -> state.equals(expected);
    }

    static RuleTest tag(final Set<TypedKey<BlockType>> blocks) {
        return (state, random) -> blocks.contains(state.name());
    }

    static RuleTest randomBlock(final TypedKey<BlockType> block, final float probability) {
        return (state, random) -> state.name().equals(block) && random.nextFloat() < probability;
    }

    static RuleTest randomBlockState(final BlockState expected, final float probability) {
        return (state, random) -> state.equals(expected) && random.nextFloat() < probability;
    }
}
