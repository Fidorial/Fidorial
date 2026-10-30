package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.data.BlockSoundSet;
import net.kyori.adventure.key.KeyPattern;

/**
 * Keys of the vanilla tags of the {@code minecraft:block_sound_set} registry.
 */
public final class BlockSoundSetTagKeys {
    /**
     * Tag {@code #minecraft:sounds_wooden}.
     */
    public static final TagKey<BlockSoundSet> SOUNDS_WOODEN = create("sounds_wooden");

    private BlockSoundSetTagKeys() {
        throw new UnsupportedOperationException("BlockSoundSetTagKeys cannot be instantiated.");
    }

    private static TagKey<BlockSoundSet> create(@KeyPattern final String value) {
        return TagKey.create(RegistryKey.BLOCK_SOUND_SET, value);
    }
}
