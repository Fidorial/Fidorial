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
import fr.fidorial.registrygen.model.BlockPropertyDefinition;
import fr.fidorial.registrygen.model.BlockReportDefinition;
import fr.fidorial.registrygen.model.PrismarineBlockLightPropertiesDefinition;
import fr.fidorial.registrygen.model.SupportedRegistries;
import net.kyori.adventure.key.Key;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Generates block state files from Mojang's {@code reports/blocks.json}.
 *
 * @since 0.1.0
 */
public final class BlockStateGenerator {

    private static final String PROTOCOL_IDS_CLASS_NAME = "BlockStateIds";
    private static final String LIGHT_PROPERTIES_CLASS_NAME = "BlockStateLightProperties";

    private static final int NETWORK_BLOCKS_PER_METHOD = 150; // 64kb limit

    private static final int DEFAULT_EMISSION = 0;
    private static final int DEFAULT_OPACITY = 15; // fully opaque fallback for unmatched blocks

    private static final ClassName LIST = ClassName.get(List.class);
    private static final ClassName OBJECT_2_INT_OPEN_HASH_MAP =
            ClassName.get("it.unimi.dsi.fastutil.objects", "Object2IntOpenHashMap");

    /**
     * Generates {@code BlockStateIds}, and — when Prismarine lighting data is supplied —
     * {@code BlockStateLightProperties}.
     *
     * @param blocks               parsed Mojang block definitions
     * @param lighting             Prismarine light emission/opacity, keyed by plain block name;
     *                             pass {@link Map#of()} to skip {@code BlockStateLightProperties} generation
     * @param blockPackage         package holding the {@code BlockType}, {@code BlockProperty}, and
     *                             {@code BlockRegistry} classes
     * @param registryDataPackage  package for {@code BlockStateIds}/{@code BlockStateLightProperties}
     * @param blockTypeKeysPackage package holding the typed {@code BlockType} keys class (e.g. {@code BlockTypeKeys})
     * @param outputDirectory      generated Java source root
     * @throws IOException if a generated file cannot be written
     */
    public void generate(final List<BlockReportDefinition> blocks,
                         final Map<String, PrismarineBlockLightPropertiesDefinition> lighting,
                         final String blockPackage,
                         final String registryDataPackage,
                         final String blockTypeKeysPackage,
                         final Path outputDirectory) throws IOException {

        Objects.requireNonNull(blocks, "blocks");
        Objects.requireNonNull(lighting, "lighting");
        Objects.requireNonNull(blockPackage, "blockPackage");
        Objects.requireNonNull(registryDataPackage, "registryDataPackage");
        Objects.requireNonNull(blockTypeKeysPackage, "blockTypeKeysPackage");
        Objects.requireNonNull(outputDirectory, "outputDirectory");

        final Packages pkgs = new Packages(blockPackage, registryDataPackage, blockTypeKeysPackage);

        generateProtocolIds(blocks, pkgs, outputDirectory);

        if (!lighting.isEmpty()) {
            generateLightProperties(blocks, lighting, pkgs, outputDirectory);
        }
    }

    /**
     * Bundles every {@link ClassName}/{@link ParameterizedTypeName} the private generation
     * helpers need, resolved once per {@link #generate} call from the configured packages.
     */
    private record Packages(ClassName key, ClassName blockType, ClassName blockProperty, ClassName blockRegistry,
                            ClassName blockTypeKeys, String dataPackage, ParameterizedTypeName lightMapType) {

        Packages(final String blockPackage, final String registryDataPackage, final String blockTypeKeysPackage) {
            this(
                    ClassName.get(Key.class),
                    ClassName.get(blockPackage, "BlockType"),
                    ClassName.get(blockPackage, "BlockProperty"),
                    ClassName.get(blockPackage, "BlockRegistry"),
                    ClassName.get(SupportedRegistries.BLOCK.keysPackage(blockTypeKeysPackage), SupportedRegistries.BLOCK.keysClassName()),
                    registryDataPackage,
                    ParameterizedTypeName.get(OBJECT_2_INT_OPEN_HASH_MAP, ClassName.get(Key.class))
            );
        }
    }

    /**
     * Generates {@code BlockStateIds}, registering every block type and its
     * full network state table with a {@code BlockRegistry}.
     */
    private void generateProtocolIds(final List<BlockReportDefinition> blocks, final Packages pkgs, final Path outputDirectory) throws IOException {

        final TypeSpec.Builder protocolIds = TypeSpec.classBuilder(PROTOCOL_IDS_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("Registers every block type and its full network state table.\n\n")
                .addJavadoc("<p>Generated from Mojang's registry report; do not edit.</p>\n")
                .addMethod(createPrivateConstructor(PROTOCOL_IDS_CLASS_NAME));

        addProtocolIdRegistrationMethods(protocolIds, blocks, pkgs);

        JavaFile.builder(pkgs.dataPackage(), protocolIds.build()).indent("    ").skipJavaLangImports(true).build().writeTo(outputDirectory);
    }

    private static void addProtocolIdRegistrationMethods(final TypeSpec.Builder protocolIds,
                                                         final List<BlockReportDefinition> blocks,
                                                         final Packages pkgs) {

        final ParameterSpec registryParameter = ParameterSpec.builder(pkgs.blockRegistry(), "registry", Modifier.FINAL).build();

        final MethodSpec.Builder registerAll = MethodSpec.methodBuilder("registerAll")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(registryParameter)
                .addJavadoc("Registers every generated block type with the given registry.\n\n")
                .addJavadoc("@param registry the registry to populate\n");

        int chunkIndex = 0;
        for (int start = 0; start < blocks.size(); start += NETWORK_BLOCKS_PER_METHOD) {

            final int end = Math.min(start + NETWORK_BLOCKS_PER_METHOD, blocks.size());
            final String chunkMethodName = "register" + chunkIndex;

            protocolIds.addMethod(createRegistrationChunkMethod(chunkMethodName, registryParameter, blocks.subList(start, end), pkgs));
            registerAll.addStatement("$N($N)", chunkMethodName, registryParameter);

            chunkIndex++;
        }

        protocolIds.addMethod(registerAll.build());
    }

    private static MethodSpec createRegistrationChunkMethod(final String methodName,
                                                            final ParameterSpec registryParameter,
                                                            final List<BlockReportDefinition> blocks,
                                                            final Packages pkgs) {

        final MethodSpec.Builder chunkMethod = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(registryParameter);

        for (final BlockReportDefinition block : blocks) {
            chunkMethod.addStatement("$N.register($L)", registryParameter, createBlockTypeInitializer(block, pkgs));
        }

        return chunkMethod.build();
    }

    private static CodeBlock createBlockTypeInitializer(final BlockReportDefinition block, final Packages pkgs) {

        return CodeBlock.of("$T.of($T.$N.key(), $L, $L, $L)",
                pkgs.blockType(),
                pkgs.blockTypeKeys(),
                keysFieldName(block.identifier()),
                createPropertiesInitializer(block.properties(), pkgs),
                createStateIdsInitializer(block.stateIdsInOrder()),
                block.defaultOrdinal());
    }

    /**
     * Generates {@code BlockStateLightProperties}, registering per-block light emission/opacity
     * sourced from PrismarineJS's {@code minecraft-data} (Mojang's own report doesn't expose this).
     */
    private void generateLightProperties(final List<BlockReportDefinition> blocks,
                                         final Map<String, PrismarineBlockLightPropertiesDefinition> lighting,
                                         final Packages pkgs,
                                         final Path outputDirectory) throws IOException {

        final TypeSpec.Builder lightProperties = TypeSpec.classBuilder(LIGHT_PROPERTIES_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("Per-block light emission/opacity, sourced from PrismarineJS's {@code minecraft-data}.\n\n")
                .addJavadoc("<p>Generated from Prismarine's blocks report; do not edit.</p>\n")
                .addField(createLightMapField("EMISSION", pkgs))
                .addField(createLightMapField("OPACITY", pkgs))
                .addStaticBlock(createLightDefaultsInitializer())
                .addMethod(createPrivateConstructor(LIGHT_PROPERTIES_CLASS_NAME))
                .addMethod(createLightAccessor("emission", "EMISSION", DEFAULT_EMISSION, pkgs))
                .addMethod(createLightAccessor("opacity", "OPACITY", DEFAULT_OPACITY, pkgs))
                .addMethod(createLightRegisterHelper(pkgs));

        addLightRegistrationMethods(lightProperties, blocks, lighting, pkgs);

        JavaFile.builder(pkgs.dataPackage(), lightProperties.build()).indent("    ").skipJavaLangImports(true).build().writeTo(outputDirectory);
    }

    private static FieldSpec createLightMapField(final String name, final Packages pkgs) {
        return FieldSpec.builder(pkgs.lightMapType(), name, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T<>()", OBJECT_2_INT_OPEN_HASH_MAP)
                .build();
    }

    private static CodeBlock createLightDefaultsInitializer() {
        return CodeBlock.builder()
                .addStatement("OPACITY.defaultReturnValue($L)", DEFAULT_OPACITY)
                .build();
    }

    private static MethodSpec createLightAccessor(final String methodName, final String mapFieldName, final int defaultValue, final Packages pkgs) {

        final ParameterSpec keyParameter = ParameterSpec.builder(pkgs.key(), "key", Modifier.FINAL).build();

        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.INT)
                .addParameter(keyParameter)
                .addJavadoc("@param key namespaced block identifier\n")
                .addJavadoc("@return {@code " + defaultValue + "} if the block has no recorded data\n")
                .addStatement("return $N.getInt($N)", mapFieldName, "key")
                .build();
    }

    private static MethodSpec createLightRegisterHelper(final Packages pkgs) {

        final ParameterSpec keyParameter = ParameterSpec.builder(pkgs.key(), "key", Modifier.FINAL).build();
        final ParameterSpec emissionParameter = ParameterSpec.builder(int.class, "emission", Modifier.FINAL).build();
        final ParameterSpec opacityParameter = ParameterSpec.builder(int.class, "opacity", Modifier.FINAL).build();

        return MethodSpec.methodBuilder("register")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(keyParameter)
                .addParameter(emissionParameter)
                .addParameter(opacityParameter)
                .addStatement("EMISSION.put($N, $N)", "key", "emission")
                .addStatement("OPACITY.put($N, $N)", "key", "opacity")
                .build();
    }

    private static void addLightRegistrationMethods(final TypeSpec.Builder lightProperties,
                                                    final List<BlockReportDefinition> blocks,
                                                    final Map<String, PrismarineBlockLightPropertiesDefinition> lighting,
                                                    final Packages pkgs) {

        final List<BlockReportDefinition> known = new ArrayList<>();
        for (final BlockReportDefinition block : blocks) {
            if (lighting.containsKey(GenerationUtils.path(block.identifier()))) {
                known.add(block);
            } else {
                System.out.println("No Prismarine lighting data for: " + block.identifier());
            }
        }

        final MethodSpec.Builder bootstrap = MethodSpec.methodBuilder("bootstrap")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        int chunkIndex = 0;
        for (int start = 0; start < known.size(); start += NETWORK_BLOCKS_PER_METHOD) {

            final int end = Math.min(start + NETWORK_BLOCKS_PER_METHOD, known.size());
            final String chunkMethodName = "registerLight" + chunkIndex;

            lightProperties.addMethod(createLightChunkMethod(chunkMethodName, known.subList(start, end), lighting, pkgs));
            bootstrap.addStatement("$N()", chunkMethodName);

            chunkIndex++;
        }

        lightProperties.addMethod(bootstrap.build());
    }

    private static MethodSpec createLightChunkMethod(final String methodName,
                                                     final List<BlockReportDefinition> blocks,
                                                     final Map<String, PrismarineBlockLightPropertiesDefinition> lighting,
                                                     final Packages pkgs) {

        final MethodSpec.Builder chunkMethod = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC);

        for (final BlockReportDefinition block : blocks) {
            final PrismarineBlockLightPropertiesDefinition entry = lighting.get(GenerationUtils.path(block.identifier()));
            chunkMethod.addStatement("register($T.$N.key(), $L, $L)",
                    pkgs.blockTypeKeys(), keysFieldName(block.identifier()), entry.emitLight(), entry.filterLight());
        }

        return chunkMethod.build();
    }

    private static String keysFieldName(final String identifier) {
        return GenerationUtils.constantName(identifier);
    }

    private static MethodSpec createPrivateConstructor(final String className) {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addStatement("throw new $T($S)", UnsupportedOperationException.class, className + " cannot be instantiated.")
                .build();
    }

    private static CodeBlock createPropertiesInitializer(final List<BlockPropertyDefinition> properties, final Packages pkgs) {

        if (properties.isEmpty()) {
            return CodeBlock.of("$T.of()", LIST);
        }

        final CodeBlock.Builder initializer = CodeBlock.builder().add("$T.of(\n", LIST).indent();

        for (int index = 0; index < properties.size(); index++) {

            final BlockPropertyDefinition property = properties.get(index);
            initializer.add("new $T($S, $L)", pkgs.blockProperty(), property.name(), createValuesInitializer(property.values()));

            initializer.add(index < properties.size() - 1 ? ",\n" : "\n");
        }

        return initializer.unindent().add(")").build();
    }

    private static CodeBlock createValuesInitializer(final List<String> values) {

        final CodeBlock.Builder initializer = CodeBlock.builder().add("$T.of(", LIST);

        for (int index = 0; index < values.size(); index++) {
            initializer.add("$S", values.get(index));
            if (index < values.size() - 1) {
                initializer.add(", ");
            }
        }

        return initializer.add(")").build();
    }

    private static CodeBlock createStateIdsInitializer(final int[] stateIds) {

        final CodeBlock.Builder initializer = CodeBlock.builder().add("new int[] {");

        for (int index = 0; index < stateIds.length; index++) {
            initializer.add("$L", stateIds[index]);
            if (index < stateIds.length - 1) {
                initializer.add(", ");
            }
        }

        return initializer.add("}").build();
    }
}
