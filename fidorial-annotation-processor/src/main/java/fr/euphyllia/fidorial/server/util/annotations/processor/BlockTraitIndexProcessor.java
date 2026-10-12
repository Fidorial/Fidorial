package fr.euphyllia.fidorial.server.util.annotations.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Generates {@code fr.fidorial.world.block.BlockTraitIndex}: the list of every interface annotated with
 * {@code @BlockTrait}, so that adding a block trait only means annotating its interface.
 *
 * <p>Traits are listed parents first (a trait after every annotated trait it extends), then by name, which keeps
 * the order block states implement them stable from one build to the next.</p>
 */
@SupportedAnnotationTypes(BlockTraitIndexProcessor.ANNOTATION_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public final class BlockTraitIndexProcessor extends AbstractProcessor {

    static final String ANNOTATION_NAME = "fr.fidorial.world.block.BlockTrait";
    private static final String BLOCK_DATA_NAME = "fr.fidorial.world.block.BlockData";
    private static final String INDEX_PACKAGE = "fr.fidorial.world.block";
    private static final String INDEX_NAME = "BlockTraitIndex";

    private boolean written;

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        final Map<String, TypeElement> traits = new TreeMap<>();
        for (final TypeElement annotation : annotations) {
            for (final Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (isValidTrait(element)) {
                    final TypeElement trait = (TypeElement) element;
                    traits.put(trait.getQualifiedName().toString(), trait);
                }
            }
        }
        if (traits.isEmpty()) {
            return false;
        }
        if (written) {
            // Every trait lives in the API sources, so they all show up in the first round
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "@BlockTrait found in a later processing round, after " + INDEX_NAME + " was generated: "
                            + traits.keySet());
            return false;
        }
        write(sortParentsFirst(traits));
        written = true;
        return false;
    }

    private boolean isValidTrait(final Element element) {
        final Messager messager = processingEnv.getMessager();
        if (element.getKind() != ElementKind.INTERFACE) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@BlockTrait only applies to interfaces", element);
            return false;
        }
        if (!element.getModifiers().contains(Modifier.PUBLIC)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "A @BlockTrait interface must be public", element);
            return false;
        }
        final TypeElement blockData = processingEnv.getElementUtils().getTypeElement(BLOCK_DATA_NAME);
        final Types types = processingEnv.getTypeUtils();
        if (blockData == null || !types.isAssignable(types.erasure(element.asType()), types.erasure(blockData.asType()))) {
            messager.printMessage(Diagnostic.Kind.ERROR, "A @BlockTrait interface must extend " + BLOCK_DATA_NAME, element);
            return false;
        }
        return true;
    }

    /**
     * Orders the traits so that each one comes after the annotated traits it extends, ties broken by name.
     */
    private List<TypeElement> sortParentsFirst(final Map<String, TypeElement> traits) {
        final Set<TypeElement> ordered = new LinkedHashSet<>();
        for (final TypeElement trait : traits.values()) {
            visit(trait, traits, ordered);
        }
        return new ArrayList<>(ordered);
    }

    private void visit(final TypeElement trait, final Map<String, TypeElement> traits, final Set<TypeElement> ordered) {
        if (ordered.contains(trait)) {
            return;
        }
        final List<TypeElement> parents = new ArrayList<>();
        for (final TypeMirror parent : trait.getInterfaces()) {
            if (parent instanceof final DeclaredType declared
                    && declared.asElement() instanceof final TypeElement element
                    && traits.containsKey(element.getQualifiedName().toString())) {
                parents.add(element);
            }
        }
        parents.sort(Comparator.comparing(element -> element.getQualifiedName().toString()));
        for (final TypeElement parent : parents) {
            visit(parent, traits, ordered);
        }
        ordered.add(trait);
    }

    private void write(final List<TypeElement> traits) {
        final StringBuilder source = new StringBuilder()
                .append("package ").append(INDEX_PACKAGE).append(";\n\n")
                .append("import java.util.List;\n\n")
                .append("/**\n")
                .append(" * Every interface annotated with {@link BlockTrait}, parents first.\n")
                .append(" *\n")
                .append(" * <p>Generated by the {@code BlockTraitIndexProcessor} of fidorial-annotation-processor; do not edit.</p>\n")
                .append(" */\n")
                .append("final class ").append(INDEX_NAME).append(" {\n\n")
                .append("    static final List<Class<? extends BlockData>> TRAITS = List.of(\n");
        for (int i = 0; i < traits.size(); i++) {
            source.append("            ").append(traits.get(i).getQualifiedName()).append(".class")
                    .append(i + 1 < traits.size() ? ",\n" : "\n");
        }
        source.append("    );\n\n")
                .append("    private ").append(INDEX_NAME).append("() {\n")
                .append("    }\n")
                .append("}\n");

        try {
            final JavaFileObject file = processingEnv.getFiler().createSourceFile(
                    INDEX_PACKAGE + "." + INDEX_NAME, traits.toArray(Element[]::new));
            try (final Writer writer = file.openWriter()) {
                writer.write(source.toString());
            }
        } catch (final IOException e) {
            throw new UncheckedIOException("Could not write " + INDEX_NAME, e);
        }
    }
}
