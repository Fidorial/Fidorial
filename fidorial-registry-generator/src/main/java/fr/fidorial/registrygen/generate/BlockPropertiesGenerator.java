package fr.fidorial.registrygen.generate;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import fr.fidorial.registrygen.model.BlockPropertyCatalog;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import javax.lang.model.element.Modifier;

/**
 * Generates {@code BlockProperties} from Mojang's block report.
 *
 * @since 0.1.0
 */
public final class BlockPropertiesGenerator {

    static final String CLASS_NAME = "BlockProperties";
    private static final String NAMES = "Names";

    public void generate(final BlockPropertyCatalog catalog, final String blockPackage, final Path outputDirectory) throws IOException {
        final ClassName property = ClassName.get(blockPackage, "BlockProperty");
        final ClassName names = ClassName.get(blockPackage, CLASS_NAME, NAMES);
        final TypeSpec.Builder type = TypeSpec.classBuilder(CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("The properties of the vanilla blocks.\n\n")
                .addJavadoc("<p>Generated from Mojang's block report; do not edit.</p>\n\n")
                .addJavadoc("@since 0.1.0\n")
                .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build());

        for (final BlockPropertyCatalog.Entry entry : catalog.entries()) {
            type.addField(FieldSpec.builder(fieldType(property, entry), entry.constant(), Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer(initializer(property, names, entry))
                    .build());
        }
        type.addType(names(catalog));

        JavaFile.builder(blockPackage, type.build()).indent("    ").skipJavaLangImports(true).build().writeTo(outputDirectory);
    }

    private static TypeSpec names(final BlockPropertyCatalog catalog) {
        final TypeSpec.Builder names = TypeSpec.classBuilder(NAMES)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addJavadoc("The names of the vanilla block properties, each shared by every property of that name.\n\n")
                .addJavadoc("@since 0.1.0\n")
                .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build());
        catalog.entries().stream()
                .map(entry -> entry.definition().name())
                .distinct()
                .sorted()
                .forEach(name -> names.addField(FieldSpec.builder(String.class, constant(name), Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$S", name)
                        .build()));
        return names.build();
    }

    private static String constant(final String name) {
        return name.toUpperCase(Locale.ROOT);
    }

    private static TypeName fieldType(final ClassName property, final BlockPropertyCatalog.Entry entry) {
        return switch (entry.kind()) {
            case FLAG -> property.nestedClass("Flag");
            case RANGE -> property.nestedClass("Range");
            case CHOICE -> ParameterizedTypeName.get(property.nestedClass("Choice"), entry.enumType());
        };
    }

    private static CodeBlock initializer(final ClassName property, final ClassName names, final BlockPropertyCatalog.Entry entry) {
        final CodeBlock name = CodeBlock.of("$T.$N", names, constant(entry.definition().name()));
        return switch (entry.kind()) {
            case FLAG -> CodeBlock.of("new $T($L)", property.nestedClass("Flag"), name);
            case RANGE -> CodeBlock.of("new $T($L, $L, $L)", property.nestedClass("Range"), name,
                    entry.definition().values().getFirst(), entry.definition().values().getLast());
            case CHOICE -> {
                final CodeBlock.Builder values = CodeBlock.builder();
                for (final String value : entry.definition().values()) {
                    values.add(", $T.$N", entry.enumType(), value.toUpperCase(Locale.ROOT));
                }
                yield CodeBlock.of("$T.of($L, $T.class$L)", property.nestedClass("Choice"), name, entry.enumType(), values.build());
            }
        };
    }
}
