package fr.euphyllia.fidorial.server.world.structure.processor;

import java.util.List;

public record ProcessorList(List<StructureProcessor> processors) {

    public static final ProcessorList EMPTY = new ProcessorList(List.of());

    public ProcessorList {
        processors = List.copyOf(processors);
    }

    public boolean needsWholePiece() {
        for (final StructureProcessor processor : processors) {
            if (processor.needsWholePiece()) {
                return true;
            }
        }
        return false;
    }
}
