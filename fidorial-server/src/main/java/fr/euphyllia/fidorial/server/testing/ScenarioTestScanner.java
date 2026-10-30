package fr.euphyllia.fidorial.server.testing;

import fr.fidorial.testing.ScenarioTestInstance;
import fr.fidorial.testing.annotation.ScenarioTest;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class ScenarioTestScanner {

    private ScenarioTestScanner() {
    }

    static List<ScenarioTestInstance> scanPackages(final Collection<ClassLoader> loaders, final String... packages) {
        final List<ScenarioTestInstance> result = new ArrayList<>();
        try (final ScanResult scan = new ClassGraph()
                .enableClassInfo()
                .enableMethodInfo()
                .enableAnnotationInfo()
                .enableClassLoaders(loaders.toArray(ClassLoader[]::new))
                .acceptPackages(packages) // recursive
                .scan()) {

            for (final ClassInfo classInfo : scan.getClassesWithMethodAnnotation(ScenarioTest.class)) {
                final Class<?> clazz = loadClass(classInfo.getName(), loaders);

                for (final Method method : clazz.getDeclaredMethods()) {
                    final ScenarioTest annotation = method.getAnnotation(ScenarioTest.class);
                    if (annotation != null) {
                        result.add(toInstance(method, annotation));
                    }
                }
            }
        }
        return result;
    }

    private static ScenarioTestInstance toInstance(final Method method, final ScenarioTest annotation) {
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalStateException("@ScenarioTest method must be static: " + method);
        }
        try {
            method.setAccessible(true);
        } catch (final InaccessibleObjectException e) {
            throw new IllegalStateException(
                    "Cannot reflectively access " + method + ". The module declaring "
                            + method.getDeclaringClass().getPackageName()
                            + " needs to open that package to the fidorial-api module.", e);
        }
        return new ScenarioTestInstance(method, annotation.group(), annotation.timeoutTicks(), annotation.required());
    }

    private static Class<?> loadClass(final String name, final Collection<ClassLoader> loaders) {
        for (final ClassLoader loader : loaders) {
            try {
                return Class.forName(name, false, loader);
            } catch (final ClassNotFoundException _) {}
        }

        throw new IllegalStateException("Cannot load scanned class " + name + " with any supplied classloader.");
    }
}
