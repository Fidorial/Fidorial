package fr.euphyllia.fidorial.server.inventory;

import com.mojang.serialization.Codec;
import fr.euphyllia.fidorial.server.codecs.container.ContainerCodecs;
import fr.euphyllia.fidorial.server.recipe.CraftingInputImpl;
import fr.fidorial.inventory.SimpleContainer;
import fr.fidorial.item.ItemStack;
import fr.fidorial.recipe.CraftingInput;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * The nine slots of a crafter, which ones are disabled, and whether it is triggered, saved in its
 * block entity the way vanilla does ({@code Items}, {@code disabled_slots}, {@code triggered}).
 *
 * <p>A disabled slot stays empty: nothing can be put in it until it is enabled again.</p>
 */
public final class CrafterContainer extends SimpleContainer {

    public static final int GRID_SIZE = 3;
    public static final int SIZE = GRID_SIZE * GRID_SIZE;

    private static final String ITEMS = "Items";
    private static final String DISABLED_SLOTS = "disabled_slots";
    private static final String TRIGGERED = "triggered";
    private static final Codec<CrafterContainer> CODEC = ContainerCodecs.containerCodec(CrafterContainer::new);

    private final boolean[] disabled = new boolean[SIZE];
    private boolean triggered;

    public CrafterContainer() {
        super(SIZE);
    }

    /**
     * Reads a crafter from the data of its block entity; anything unreadable is left empty.
     */
    public static CrafterContainer load(final @Nullable CompoundBinaryTag data) throws IOException {
        final CompoundBinaryTag root = data == null ? CompoundBinaryTag.empty() : data;
        final CrafterContainer crafter = ContainerCodecs.readList(root, ITEMS, CODEC);
        final BinaryTag disabledSlots = root.get(DISABLED_SLOTS);
        if (disabledSlots instanceof final IntArrayBinaryTag slots) {
            for (final int slot : slots.value()) {
                // Like vanilla, a slot holding something stays enabled.
                crafter.setSlotDisabled(slot, true);
            }
        }
        crafter.triggered = root.getInt(TRIGGERED) != 0;
        return crafter;
    }

    /**
     * Writes this crafter into the data of its block entity, keeping whatever else it holds.
     */
    public CompoundBinaryTag save(final @Nullable CompoundBinaryTag data) throws IOException {
        final CompoundBinaryTag root = data == null ? CompoundBinaryTag.empty() : data;
        return ContainerCodecs.writeList(root, ITEMS, CODEC, this)
                .putIntArray(DISABLED_SLOTS, disabledSlots())
                .putInt(TRIGGERED, triggered ? 1 : 0);
    }

    public boolean isSlotDisabled(final int slot) {
        return slot >= 0 && slot < SIZE && disabled[slot];
    }

    /**
     * Disables or enables a slot. A slot holding something cannot be disabled.
     *
     * @return {@code true} when the slot changed
     */
    public boolean setSlotDisabled(final int slot, final boolean disable) {
        if (slot < 0 || slot >= SIZE || disabled[slot] == disable || (disable && !get(slot).isEmpty())) {
            return false;
        }
        disabled[slot] = disable;
        return true;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public void setTriggered(final boolean triggered) {
        this.triggered = triggered;
    }

    /**
     * Like vanilla, putting something into a disabled slot enables it again.
     */
    @Override
    public void set(final int slot, final @Nullable ItemStack stack) {
        if (stack != null && !stack.isEmpty() && isSlotDisabled(slot)) {
            disabled[slot] = false;
        }
        super.set(slot, stack);
    }

    /**
     * {@return the grid, for recipes to match against}
     */
    public CraftingInput asInput() {
        return new CraftingInputImpl(GRID_SIZE, GRID_SIZE, List.of(contents()));
    }

    private int[] disabledSlots() {
        int count = 0;
        final int[] slots = new int[SIZE];
        for (int slot = 0; slot < SIZE; slot++) {
            if (disabled[slot]) {
                slots[count++] = slot;
            }
        }
        return Arrays.copyOf(slots, count);
    }
}
