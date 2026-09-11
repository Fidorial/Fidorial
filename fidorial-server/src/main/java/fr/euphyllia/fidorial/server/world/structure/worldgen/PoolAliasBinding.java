package fr.euphyllia.fidorial.server.world.structure.worldgen;

import fr.euphyllia.fidorial.server.world.structure.math.LegacyRandom;
import net.kyori.adventure.key.Key;

import java.util.List;
import java.util.Map;

public sealed interface PoolAliasBinding {

    void resolve(LegacyRandom random, Map<Key, Key> into);

    record Direct(Key alias, Key target) implements PoolAliasBinding {
        @Override
        public void resolve(final LegacyRandom random, final Map<Key, Key> into) {
            into.put(alias, target);
        }
    }

    record RandomTarget(Key alias, List<Key> targets, List<Integer> weights) implements PoolAliasBinding {
        @Override
        public void resolve(final LegacyRandom random, final Map<Key, Key> into) {
            final int index = pick(random, weights);
            if (index >= 0) {
                into.put(alias, targets.get(index));
            }
        }
    }

    record RandomGroup(List<List<PoolAliasBinding>> groups, List<Integer> weights) implements PoolAliasBinding {
        @Override
        public void resolve(final LegacyRandom random, final Map<Key, Key> into) {
            final int index = pick(random, weights);
            if (index >= 0) {
                for (final PoolAliasBinding binding : groups.get(index)) {
                    binding.resolve(random, into);
                }
            }
        }
    }

    static int pick(final LegacyRandom random, final List<Integer> weights) {
        int total = 0;
        for (final int weight : weights) {
            total += weight;
        }
        if (total <= 0) {
            return -1;
        }
        int roll = random.nextInt(total);
        for (int i = 0; i < weights.size(); i++) {
            roll -= weights.get(i);
            if (roll < 0) {
                return i;
            }
        }
        return weights.size() - 1;
    }
}
