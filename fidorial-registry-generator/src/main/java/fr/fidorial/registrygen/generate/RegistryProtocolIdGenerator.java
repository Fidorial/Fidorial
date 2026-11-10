package fr.fidorial.registrygen.generate;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import fr.fidorial.registrygen.GenerationUtils;
import fr.fidorial.registrygen.model.ProtocolIdTarget;
import fr.fidorial.registrygen.model.ProtocolIdValueKind;
import fr.fidorial.registrygen.model.RegistryDefinition;
import fr.fidorial.registrygen.model.RegistryEntryDefinition;
import net.kyori.adventure.key.Key;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Generates a holder of {@code int} constants for every {@link RegistryEntryDefinition}
 * of a registry, e.g. {@code CHEST_BLOCK_ENTITY_ID = 8}.
 *
 * <p>In addition to the constants, an identifier &rarr; protocol ID lookup table is
 * emitted so callers can resolve an ID at runtime without depending on a
 * compile-time constant name that may disappear in a future Minecraft version.</p>
 *
 * <p>Since the addition of packet catalogs, this generator also supports emitting
 * a {@link Key}-constant holder (see {@link ProtocolIdValueKind#IDENTIFIER}),
 * for registries whose entries are looked up by namespaced identifier rather than
 * by network ID. The {@code minecraft} namespace is stripped when emitting these
 * keys, so {@code minecraft:core} becomes {@code Key.key("core")} while
 * {@code brigadier:boolean} becomes {@code Key.key("brigadier", "boolean")}.</p>
 *
 * @since 0.1.0
 */
public final class RegistryProtocolIdGenerator {

    private static final String LOOKUP_FIELD = "BY_IDENTIFIER";
    private static final String UNKNOWN_FIELD = "UNKNOWN";

    private static final String FASTUTIL_OBJECTS = "it.unimi.dsi.fastutil.objects";
    private static final ClassName OBJECT_2_INT_MAP = ClassName.get(FASTUTIL_OBJECTS, "Object2IntMap");
    private static final ClassName OBJECT_2_INT_MAPS = ClassName.get(FASTUTIL_OBJECTS, "Object2IntMaps");
    private static final ClassName OBJECT_2_INT_OPEN_HASH_MAP = ClassName.get(FASTUTIL_OBJECTS, "Object2IntOpenHashMap");

    /**
     * Generates the protocol ID holder for a registry.
     *
     * @param registry        parsed Mojang registry definition
     * @param target          generation target (package, class name, constant suffix, value kind)
     * @param outputDirectory generated Java source root
     * @throws IOException if the source file cannot be written
     */
    public void generate(final RegistryDefinition registry,
                         final ProtocolIdTarget target,
                         final Path outputDirectory) throws IOException {

        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(outputDirectory, "outputDirectory");

        if (!registry.identifier().equals(target.registryIdentifier())) {
            throw new IllegalArgumentException("Protocol ID target '" + target.registryIdentifier()
                    + "' does not match parsed registry identifier '"
                    + registry.identifier() + "'.");
        }

        final boolean isProtocolId = target.valueKind() == ProtocolIdValueKind.PROTOCOL_ID;
        final TypeName fieldType = isProtocolId ? TypeName.get(int.class) : TypeName.get(Key.class);

        final TypeSpec.Builder typeBuilder = (isProtocolId
                ? TypeSpec.interfaceBuilder(target.className())
                : TypeSpec.classBuilder(target.className()).addModifiers(Modifier.FINAL))
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(target.classJavadoc())
                .addJavadoc("\n<p>$L</p>\n", target.registryDescription());

        if (isProtocolId) {
            typeBuilder.addField(FieldSpec.builder(int.class, UNKNOWN_FIELD, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$L", -1)
                    .addJavadoc("Returned by {@link #id($T)} when the identifier is unknown.\n", Key.class)
                    .build());
        }

        final Map<String, String> constantsByIdentifier = new LinkedHashMap<>();

        for (final RegistryEntryDefinition entry : registry.entries()) {

            final String fieldName = GenerationUtils.constantName(entry.identifier()) + target.constantSuffix();

            if (constantsByIdentifier.putIfAbsent(entry.identifier(), fieldName) != null) {
                throw new IllegalStateException("Duplicate entry '" + entry.identifier()
                        + "' in '" + registry.identifier() + "'.");
            }

            final FieldSpec.Builder field = FieldSpec.builder(fieldType, fieldName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .addJavadoc("{@code $L}\n", entry.identifier());

            if (isProtocolId) {
                field.initializer("$L", entry.protocolId());
            } else {
                field.initializer(createKeyInitializer(entry.identifier()));
            }

            typeBuilder.addField(field.build());
        }

        if (isProtocolId) {
            typeBuilder.addField(createLookupField(constantsByIdentifier));
            typeBuilder.addMethod(createLookupMethod());
        } else {
            typeBuilder.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build());
        }

        JavaFile.builder(target.packageName(), typeBuilder.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .build()
                .writeTo(outputDirectory);
    }

    private static FieldSpec createLookupField(final Map<String, String> constantsByIdentifier) {

        final ParameterizedTypeName mapType = ParameterizedTypeName.get(OBJECT_2_INT_MAP, ClassName.get(Key.class));

        final CodeBlock.Builder keys = CodeBlock.builder();
        final CodeBlock.Builder ids = CodeBlock.builder();

        int index = 0;
        final int last = constantsByIdentifier.size() - 1;

        for (final Map.Entry<String, String> entry : constantsByIdentifier.entrySet()) {

            final String separator = index < last ? "," : "";
            keys.add("        $L$L\n", createKeyInitializer(entry.getKey()), separator);
            ids.add("        $N$L\n", entry.getValue(), separator);
            index++;
        }

        final CodeBlock initializer = CodeBlock.builder()
                .add("$T.unmodifiable(new $T<>(\n", OBJECT_2_INT_MAPS, OBJECT_2_INT_OPEN_HASH_MAP)
                .add("    new $T[] {\n", Key.class)
                .add(keys.build())
                .add("    },\n")
                .add("    new int[] {\n")
                .add(ids.build())
                .add("    }\n")
                .add("))")
                .build();

        return FieldSpec.builder(mapType, LOOKUP_FIELD, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(initializer)
                .addJavadoc("Immutable identifier to protocol ID lookup table.\n")
                .build();
    }

    private static MethodSpec createLookupMethod() {

        final ParameterSpec identifier = ParameterSpec.builder(Key.class, "identifier", Modifier.FINAL).build();

        return MethodSpec.methodBuilder("id")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(int.class)
                .addParameter(identifier)
                .addJavadoc("Resolves the protocol ID for a namespaced identifier.\n\n")
                .addJavadoc("@param identifier namespaced identifier, e.g. {@code Key.key(\"minecraft\", \"chest\")}\n")
                .addJavadoc("@return the protocol ID, or {@link #$L} when the identifier is unknown\n", UNKNOWN_FIELD)
                .addStatement("return $N.getOrDefault($N, $L)", LOOKUP_FIELD, "identifier", UNKNOWN_FIELD)
                .build();
    }

    private static CodeBlock createKeyInitializer(final String identifier) {

        final int separator = identifier.indexOf(':');

        if (separator < 0) {
            return CodeBlock.of("$T.key($S)", Key.class, identifier);
        }

        final String namespace = identifier.substring(0, separator);
        final String path = identifier.substring(separator + 1);

        if (namespace.equalsIgnoreCase(Key.MINECRAFT_NAMESPACE)) {
            return CodeBlock.of("$T.key($S)", Key.class, path);
        }

        return CodeBlock.of("$T.key($S, $S)", Key.class, namespace, path);
    }
}
