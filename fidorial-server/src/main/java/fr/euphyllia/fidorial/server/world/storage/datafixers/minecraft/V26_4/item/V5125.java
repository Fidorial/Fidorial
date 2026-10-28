package fr.euphyllia.fidorial.server.world.storage.datafixers.minecraft.V26_4.item;

import ca.spottedleaf.converter.DataConverter;
import ca.spottedleaf.converter.types.ListType;
import ca.spottedleaf.converter.types.MapType;
import org.jspecify.annotations.Nullable;

/**
 * Writes the default sounds of the {@code minecraft:equippable} and {@code minecraft:consumable}
 * components when a stored item leaves them out, so that items upgraded from an older world keep
 * them (MC-312783, fixed in 26.4 Snapshot 6 at data version 5125).
 *
 * <p>Items nested in a {@code minecraft:container} or a {@code minecraft:bundle_contents} are
 * upgraded too.</p>
 */
public final class V5125 extends DataConverter<MapType, MapType> {

    public static final int TO_VERSION = 5125;

    private static final String COMPONENTS = "components";
    private static final String EQUIPPABLE = "minecraft:equippable";
    private static final String CONSUMABLE = "minecraft:consumable";
    private static final String CONTAINER = "minecraft:container";
    private static final String BUNDLE_CONTENTS = "minecraft:bundle_contents";

    private static final String EQUIP_SOUND = "equip_sound";
    private static final String DEFAULT_EQUIP_SOUND = "minecraft:item.armor.equip_generic";
    private static final String SHEARING_SOUND = "shearing_sound";
    private static final String DEFAULT_SHEARING_SOUND = "minecraft:item.shears.snip";
    private static final String CONSUME_SOUND = "sound";
    private static final String DEFAULT_CONSUME_SOUND = "minecraft:entity.generic.eat";

    public V5125() {
        super(TO_VERSION);
    }

    @Override
    @Nullable public MapType convert(final @Nullable MapType itemStack, final long sourceVersion, final long toVersion) {
        upgrade(itemStack);
        return itemStack;
    }

    /**
     * Upgrades a stored item stack in place.
     *
     * @param itemStack the stack, {@code null} is ignored
     */
    public static void upgrade(final @Nullable MapType itemStack) {
        if (itemStack == null) {
            return;
        }
        final MapType components = itemStack.getMap(COMPONENTS, null);
        if (components == null) {
            return;
        }

        final MapType equippable = components.getMap(EQUIPPABLE, null);
        if (equippable != null) {
            setIfAbsent(equippable, EQUIP_SOUND, DEFAULT_EQUIP_SOUND);
            setIfAbsent(equippable, SHEARING_SOUND, DEFAULT_SHEARING_SOUND);
        }
        final MapType consumable = components.getMap(CONSUMABLE, null);
        if (consumable != null) {
            setIfAbsent(consumable, CONSUME_SOUND, DEFAULT_CONSUME_SOUND);
        }

        // [{slot, item}, ...]
        final ListType container = components.getListUnchecked(CONTAINER, null);
        if (container != null) {
            for (int i = 0, len = container.size(); i < len; ++i) {
                final MapType slot = container.getMap(i, null);
                if (slot != null) {
                    upgrade(slot.getMap("item", null));
                }
            }
        }
        // [stack, ...]
        final ListType bundle = components.getListUnchecked(BUNDLE_CONTENTS, null);
        if (bundle != null) {
            for (int i = 0, len = bundle.size(); i < len; ++i) {
                upgrade(bundle.getMap(i, null));
            }
        }
    }

    private static void setIfAbsent(final MapType component, final String field, final String sound) {
        if (!component.hasKey(field)) {
            component.setString(field, sound);
        }
    }
}
