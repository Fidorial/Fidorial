package fr.euphyllia.fidorial.server.entity.player;

public final class InventorySlots {

    public static final int INVALID = -1;

    public static final int HOTBAR_SIZE = 9;
    public static final int MAIN_SIZE = 36;
    public static final int FEET = 36;
    public static final int LEGS = 37;
    public static final int CHEST = 38;
    public static final int HEAD = 39;
    public static final int OFFHAND = 40;

    private static final int WINDOW_ARMOR_START = 5;
    private static final int WINDOW_ARMOR_END = 8;
    private static final int WINDOW_MAIN_START = 9;
    private static final int WINDOW_MAIN_END = 35;
    private static final int WINDOW_HOTBAR_START = 36;
    private static final int WINDOW_HOTBAR_END = WINDOW_HOTBAR_START + HOTBAR_SIZE - 1;
    private static final int WINDOW_OFFHAND = 45;

    private InventorySlots() {
    }

    public static int fromWindow(final int windowSlot) {
        if (windowSlot >= WINDOW_HOTBAR_START && windowSlot <= WINDOW_HOTBAR_END) {
            return windowSlot - WINDOW_HOTBAR_START;
        }
        if (windowSlot >= WINDOW_MAIN_START && windowSlot <= WINDOW_MAIN_END) {
            return windowSlot;
        }
        if (windowSlot >= WINDOW_ARMOR_START && windowSlot <= WINDOW_ARMOR_END) {
            return HEAD - (windowSlot - WINDOW_ARMOR_START);
        }
        if (windowSlot == WINDOW_OFFHAND) {
            return OFFHAND;
        }
        return INVALID;
    }
}
