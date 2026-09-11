package fr.euphyllia.fidorial.server.world.structure.pool;

import fr.euphyllia.fidorial.server.world.structure.processor.ProcessorList;
import fr.euphyllia.fidorial.server.world.structure.template.StructureTemplateImpl;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

public interface TemplateSource {

    @Nullable StructureTemplateImpl template(Key key);

    ProcessorList processorList(ProcessorRef ref);

    @Nullable TemplatePool pool(Key key);
}
