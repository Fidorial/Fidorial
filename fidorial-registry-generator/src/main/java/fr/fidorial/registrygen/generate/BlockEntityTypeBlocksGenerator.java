package fr.fidorial.registrygen.generate;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeSpec;
import fr.fidorial.registrygen.GenerationUtils;
import fr.fidorial.registrygen.model.PrismarineBlockEntityDefinition;
import fr.fidorial.registrygen.model.RegistryEntryDefinition;
import fr.fidorial.registrygen.model.SupportedRegistries;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generates {@code BlockEntityTypeBlocks} — the block &rarr; block entity type association —
 * by joining Mojang's block and block entity type registries against PrismarineJS's block
 * entities report.
 *
 * @since 0.1.0
 */
public final class BlockEntityTypeBlocksGenerator {

    private static final String CLASS_NAME = "BlockEntityTypeBlocks";
    private static final String BLOCK_ENTITY_TYPE_KEYS_CLASS_NAME = "BlockEntityTypeKeys";

    /**
     * Registrations per generated method, well under the JVM's 64 KB method limit.
     */
    private static final int BLOCKS_PER_METHOD = 200;

    private static final ClassName OBJECT_2_OBJECT_OPEN_HASH_MAP =
            ClassName.get("it.unimi.dsi.fastutil.objects", "Object2ObjectOpenHashMap");
    private static final ClassName NULLABLE = ClassName.get("org.jspecify.annotations", "Nullable");
    private static final ClassName TYPED_KEY = ClassName.get("fr.fidorial.registry", "TypedKey");
    private static final ParameterizedTypeName BLOCK_KEY =
            ParameterizedTypeName.get(TYPED_KEY, ClassName.get("fr.fidorial.world.block", "BlockType"));
    private static final ParameterizedTypeName BLOCK_ENTITY_TYPE_KEY =
            ParameterizedTypeName.get(TYPED_KEY, ClassName.get("fr.fidorial.registry.data", "BlockEntityType"));

    /**
     * Generates the {@code BlockEntityTypeBlocks} class.
     *
     * <p>Pairs whose block or block entity type is missing from Mojang's registries are
     * skipped, so the generated code only references keys that exist.</p>
     *
     * @param blocks                     Mojang's block registry entries
     * @param blockEntityTypes           Mojang's block entity type registry entries
     * @param prismarineBlockEntities    Prismarine's block entities report
     * @param registryDataPackage        package the class is written into
     * @param blockTypeKeysPackage       package holding the generated {@code BlockTypeKeys} class
     * @param blockEntityTypeKeysPackage package holding the generated {@code BlockEntityTypeKeys} class
     * @param outputDirectory            generated Java source root
     *
     * @throws IOException if the generated file cannot be written
     */
    public void generate(final List<RegistryEntryDefinition> blocks,
                         final List<RegistryEntryDefinition> blockEntityTypes,
                         final List<PrismarineBlockEntityDefinition> prismarineBlockEntities,
                         final String registryDataPackage,
                         final String blockTypeKeysPackage,
                         final String blockEntityTypeKeysPackage,
                         final Path outputDirectory) throws IOException {

        Objects.requireNonNull(blocks, "blocks");
        Objects.requireNonNull(blockEntityTypes, "blockEntityTypes");
        Objects.requireNonNull(prismarineBlockEntities, "prismarineBlockEntities");
        Objects.requireNonNull(registryDataPackage, "registryDataPackage");
        Objects.requireNonNull(blockTypeKeysPackage, "blockTypeKeysPackage");
        Objects.requireNonNull(blockEntityTypeKeysPackage, "blockEntityTypeKeysPackage");
        Objects.requireNonNull(outputDirectory, "outputDirectory");

        final ClassName blockTypeKeys = ClassName.get(
                SupportedRegistries.BLOCK.keysPackage(blockTypeKeysPackage),
                SupportedRegistries.BLOCK.keysClassName());
        final ClassName blockEntityTypeKeys = ClassName.get(blockEntityTypeKeysPackage, BLOCK_ENTITY_TYPE_KEYS_CLASS_NAME);

        final ParameterizedTypeName mapType =
                ParameterizedTypeName.get(OBJECT_2_OBJECT_OPEN_HASH_MAP, BLOCK_KEY, BLOCK_ENTITY_TYPE_KEY);

        final TypeSpec.Builder type = TypeSpec.classBuilder(CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(MethodSpec.methodBuilder("blockEntityType")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(BLOCK_ENTITY_TYPE_KEY.annotated(AnnotationSpec.builder(NULLABLE).build()))
                        .addParameter(ParameterSpec.builder(BLOCK_KEY, "block", Modifier.FINAL).build())
                        .addJavadoc("@param block the block\n")
                        .addJavadoc("@return the block entity type it carries, {@code null} when it carries none\n")
                        .addStatement("return BLOCK_ENTITY_TYPE.get($N)", "block")
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PRIVATE)
                        .addStatement("throw new $T($S)", UnsupportedOperationException.class,
                                CLASS_NAME + " cannot be instantiated.")
                        .build());
        final int registrationCount = addRegistrationMethods(
                type, blocks, blockEntityTypes, prismarineBlockEntities,
                blockTypeKeys, blockEntityTypeKeys
        );
        type.addJavadoc("The block entity type each block carries.\n\n")
                .addJavadoc("<p>Joined from Mojang's registry report and PrismarineJS's\n")
                .addJavadoc("{@code minecraft-data} block entities report; do not edit.</p>\n")
                .addField(FieldSpec.builder(mapType, "BLOCK_ENTITY_TYPE", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T<>($L)", OBJECT_2_OBJECT_OPEN_HASH_MAP, registrationCount)
                        .build());

        JavaFile.builder(registryDataPackage, type.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .build()
                .writeTo(outputDirectory);
    }

    private static int addRegistrationMethods(final TypeSpec.Builder type,
                                               final List<RegistryEntryDefinition> blocks,
                                               final List<RegistryEntryDefinition> blockEntityTypes,
                                               final List<PrismarineBlockEntityDefinition> prismarineBlockEntities,
                                               final ClassName blockTypeKeys,
                                               final ClassName blockEntityTypeKeys) {

        final Set<String> knownBlocks = paths(blocks);
        final Set<String> knownBlockEntityTypes = paths(blockEntityTypes);

        final List<MethodSpec> methods = new ArrayList<>();

        MethodSpec.Builder current = null;
        int inCurrent = 0;
        int registrationCount = 0;

        for (final PrismarineBlockEntityDefinition blockEntity : prismarineBlockEntities) {

            if (!knownBlockEntityTypes.contains(blockEntity.name())) {
                continue;
            }

            for (final String block : blockEntity.blocks()) {

                if (!knownBlocks.contains(block)) {
                    continue;
                }

                if (current == null) {
                    current = MethodSpec.methodBuilder("registerBlockEntityTypes" + methods.size())
                            .addModifiers(Modifier.PRIVATE, Modifier.STATIC);
                }

                current.addStatement("BLOCK_ENTITY_TYPE.put($T.$L, $T.$L)",
                        blockTypeKeys, GenerationUtils.constantName(block),
                        blockEntityTypeKeys, GenerationUtils.constantName(blockEntity.name()));

                if (++inCurrent >= BLOCKS_PER_METHOD) {
                    methods.add(current.build());
                    current = null;
                    inCurrent = 0;
                }

                registrationCount++;
            }
        }

        if (current != null) {
            methods.add(current.build());
        }

        methods.forEach(type::addMethod);

        final MethodSpec.Builder bootstrap = MethodSpec.methodBuilder("bootstrap")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addJavadoc("Fills the lookup table. Called once during server start-up;\n")
                .addJavadoc("calling it again is harmless but pointless.\n");

        for (final MethodSpec method : methods) {
            bootstrap.addStatement("$N()", method.name());
        }

        type.addMethod(bootstrap.build());
        return registrationCount;
    }

    private static Set<String> paths(final List<RegistryEntryDefinition> entries) {
        return entries.stream()
                .map(entry -> GenerationUtils.path(entry.identifier()))
                .collect(Collectors.toUnmodifiableSet());
    }
}
