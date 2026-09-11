package fr.euphyllia.fidorial.server.world.structure.pool;

import fr.euphyllia.fidorial.server.world.structure.processor.ProcessorList;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

public record ProcessorRef(@Nullable Key id, @Nullable ProcessorList inline) {

    public static final ProcessorRef EMPTY = new ProcessorRef(null, ProcessorList.EMPTY);
}
