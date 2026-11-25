package fr.euphyllia.fidorial.server.datapack;

import com.mojang.serialization.Codec;
import fr.euphyllia.fidorial.server.codecs.RecordCodec;
import net.kyori.adventure.text.Component;

import java.util.List;

import static fr.euphyllia.fidorial.server.codecs.adventure.ComponentCodecs.COMPONENT_CODEC;

public record DatapackManifest(Info pack, List<Overlay> overlays) {
    public static final Codec<DatapackManifest> CODEC = RecordCodec.builder(DatapackManifest.class)
            .required("pack", DatapackManifest::pack, Info.CODEC)
            .field("overlays", DatapackManifest::overlays, Overlay.CODEC.listOf().fieldOf("entries").codec(), List.of())
            .build();

    public record Info(Component description) {
        public static final Codec<Info> CODEC = RecordCodec.builder(Info.class)
                .required("description", Info::description, COMPONENT_CODEC)
                .build();
    }

    /// @param directory the folder of the pack whose content goes over the base content
    public record Overlay(String directory) {
        public static final Codec<Overlay> CODEC = RecordCodec.builder(Overlay.class)
                .required("directory", Overlay::directory, Codec.STRING)
                .build();
    }
}
