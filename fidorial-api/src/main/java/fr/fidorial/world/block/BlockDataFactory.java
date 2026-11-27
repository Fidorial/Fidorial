package fr.fidorial.world.block;

import com.google.common.base.Preconditions;

import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class BlockDataFactory {

    private static final ClassDesc BASE = desc(BlockDataImpl.class);
    private static final MethodType CONSTRUCTOR_TYPE = MethodType.methodType(void.class, BlockType.class, int.class);
    private static final MethodTypeDesc CONSTRUCTOR = CONSTRUCTOR_TYPE.describeConstable().orElseThrow();
    private static final Map<List<Class<?>>, MethodHandle> CONSTRUCTORS = new ConcurrentHashMap<>();

    private BlockDataFactory() {
    }

    static MethodHandle constructor(final List<Class<?>> traits) {
        return CONSTRUCTORS.computeIfAbsent(List.copyOf(traits), BlockDataFactory::define);
    }

    private static MethodHandle define(final List<Class<?>> traits) {
        final Module module = BlockDataFactory.class.getModule();
        for (final Class<?> trait : traits) {
            Preconditions.checkArgument(trait.isInterface() && BlockData.class.isAssignableFrom(trait),
                    "%s is not an interface extending BlockData", trait.getName());
            Preconditions.checkArgument(trait.getModule().isExported(trait.getPackageName(), module),
                    "The package of the block trait %s must be exported to %s", trait.getName(), module.getName());
            module.addReads(trait.getModule());
        }
        checkUnambiguous(traits);
        final ClassDesc name = ClassDesc.of(BlockDataImpl.class.getPackageName(), "GeneratedBlockData$" + traits.getLast().getSimpleName());
        final byte[] bytes = ClassFile.of().build(name, builder -> builder
                .withFlags(ClassFile.ACC_FINAL | ClassFile.ACC_SYNTHETIC)
                .withSuperclass(BASE)
                .withInterfaceSymbols(traits.stream().map(BlockDataFactory::desc).toList())
                .withMethodBody(ConstantDescs.INIT_NAME, CONSTRUCTOR, ClassFile.ACC_PUBLIC, code -> code
                        .aload(0)
                        .aload(1)
                        .iload(2)
                        .invokespecial(BASE, ConstantDescs.INIT_NAME, CONSTRUCTOR)
                        .return_()));
        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup().defineHiddenClass(bytes, true);
            return lookup.findConstructor(lookup.lookupClass(), CONSTRUCTOR_TYPE).asType(MethodType.methodType(BlockData.class, BlockType.class, int.class));
        } catch (final IllegalAccessException | NoSuchMethodException exception) {
            throw new IllegalStateException("Cannot define the block data class of " + traits, exception);
        }
    }

    private static void checkUnambiguous(final List<Class<?>> traits) {
        final Map<String, List<Class<?>>> providers = new HashMap<>();
        for (final Class<?> trait : traits) {
            for (final Method method : trait.getMethods()) {
                if (method.isDefault()) {
                    final List<Class<?>> declaring = providers.computeIfAbsent(
                            method.getName() + Arrays.toString(method.getParameterTypes()), _ -> new ArrayList<>());
                    if (!declaring.contains(method.getDeclaringClass())) {
                        declaring.add(method.getDeclaringClass());
                    }
                }
            }
        }
        providers.forEach((signature, declaring) -> {
            final long mostSpecific = declaring.stream()
                    .filter(candidate -> declaring.stream().noneMatch(other -> other != candidate && candidate.isAssignableFrom(other)))
                    .count();
            Preconditions.checkArgument(mostSpecific == 1, "The block traits %s give %s more than once", traits, signature);
        });
    }

    private static ClassDesc desc(final Class<?> type) {
        return type.describeConstable().orElseThrow();
    }
}
