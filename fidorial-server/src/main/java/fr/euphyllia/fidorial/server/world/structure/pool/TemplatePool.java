package fr.euphyllia.fidorial.server.world.structure.pool;

import fr.euphyllia.fidorial.server.world.structure.math.Box;
import fr.euphyllia.fidorial.server.world.structure.math.LegacyRandom;
import fr.fidorial.world.structure.StructureRotation;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.List;

public final class TemplatePool {

    private final Key id;
    private final Key fallback;
    private final List<PoolElement> templates;
    private volatile int maxSize = -1;

    public TemplatePool(final Key id, final Key fallback, final List<PoolElement> templates) {
        this.id = id;
        this.fallback = fallback;
        this.templates = List.copyOf(templates);
    }

    public Key id() {
        return id;
    }

    public Key fallback() {
        return fallback;
    }

    public List<PoolElement> templates() {
        return templates;
    }

    public int size() {
        return templates.size();
    }

    public PoolElement randomTemplate(final LegacyRandom random) {
        return templates.isEmpty() ? PoolElement.Empty.INSTANCE : templates.get(random.nextInt(templates.size()));
    }

    public List<PoolElement> shuffledTemplates(final LegacyRandom random) {
        final List<PoolElement> copy = new ArrayList<>(templates);
        random.shuffle(copy);
        return copy;
    }

    public int maxSize(final TemplateSource source) {
        int cached = maxSize;
        if (cached < 0) {
            cached = 0;
            for (final PoolElement element : templates) {
                final Box box = element.boundingBox(source, 0, 0, 0, StructureRotation.NONE);
                if (box != null) {
                    cached = Math.max(cached, box.ySpan());
                }
            }
            maxSize = cached;
        }
        return cached;
    }
}
