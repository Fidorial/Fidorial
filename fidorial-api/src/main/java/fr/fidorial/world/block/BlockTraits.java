package fr.fidorial.world.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Detects the trait interfaces a block type implements from its properties.
 *
 * <p>The rules live on the traits themselves, as {@link BlockTrait} annotations; this class only evaluates them.
 * The list of traits, {@code BlockTraitIndex}, is generated at compile time by the {@code BlockTraitIndexProcessor}
 * of fidorial-annotation-processor: adding a trait only means annotating its interface.</p>
 */
final class BlockTraits {

    private static final List<Rule> RULES = BlockTraitIndex.TRAITS.stream().map(BlockTraits::rule).toList();

    private BlockTraits() {
    }

    static List<Class<? extends BlockData>> detect(final List<BlockProperty> properties) {
        final Map<String, List<String>> declared = new HashMap<>();
        for (final BlockProperty property : properties) {
            declared.put(property.name(), property.values());
        }
        final List<Class<? extends BlockData>> traits = new ArrayList<>();
        for (final Rule rule : RULES) {
            if (rule.matches(declared)) {
                traits.add(rule.trait());
            }
        }
        return traits;
    }

    /**
     * Merges the {@link BlockTrait} of a trait with the ones of every trait it extends.
     */
    private static Rule rule(final Class<? extends BlockData> trait) {
        if (!trait.isAnnotationPresent(BlockTrait.class)) {
            throw new IllegalStateException(trait.getName() + " is listed as a block trait but has no @BlockTrait");
        }
        final Set<String> required = new LinkedHashSet<>();
        final List<BlockTrait.Values> restrictions = new ArrayList<>();
        collect(trait, required, restrictions, new LinkedHashSet<>());
        return new Rule(trait, List.copyOf(required), List.copyOf(restrictions));
    }

    private static void collect(final Class<?> type, final Set<String> required,
                                final List<BlockTrait.Values> restrictions, final Set<Class<?>> visited) {
        if (!visited.add(type)) {
            return;
        }
        final BlockTrait spec = type.getAnnotation(BlockTrait.class);
        if (spec != null) {
            required.addAll(List.of(spec.properties()));
            for (final BlockTrait.Values values : spec.values()) {
                required.add(values.property());
                restrictions.add(values);
            }
        }
        for (final Class<?> parent : type.getInterfaces()) {
            collect(parent, required, restrictions, visited);
        }
    }

    private record Rule(Class<? extends BlockData> trait, List<String> required, List<BlockTrait.Values> restrictions) {

        boolean matches(final Map<String, List<String>> declared) {
            for (final String property : required) {
                if (!declared.containsKey(property)) {
                    return false;
                }
            }
            for (final BlockTrait.Values restriction : restrictions) {
                final List<String> values = declared.get(restriction.property());
                final Set<String> allowed = Set.of(restriction.allowed());
                if (!allowed.containsAll(values)
                        || (restriction.exactly() && !Set.copyOf(values).equals(allowed))) {
                    return false;
                }
            }
            return true;
        }
    }
}
