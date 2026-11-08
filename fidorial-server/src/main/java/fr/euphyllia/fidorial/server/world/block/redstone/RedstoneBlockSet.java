package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.SoundEvent;
import fr.fidorial.registry.keys.SoundEventKeys;

public enum RedstoneBlockSet {
    WOOD(SoundEventKeys.BLOCK_WOODEN_DOOR_OPEN, SoundEventKeys.BLOCK_WOODEN_DOOR_CLOSE,
            SoundEventKeys.BLOCK_WOODEN_TRAPDOOR_OPEN, SoundEventKeys.BLOCK_WOODEN_TRAPDOOR_CLOSE,
            SoundEventKeys.BLOCK_WOODEN_BUTTON_CLICK_ON, SoundEventKeys.BLOCK_WOODEN_BUTTON_CLICK_OFF),
    CHERRY(SoundEventKeys.BLOCK_CHERRY_WOOD_DOOR_OPEN, SoundEventKeys.BLOCK_CHERRY_WOOD_DOOR_CLOSE,
            SoundEventKeys.BLOCK_CHERRY_WOOD_TRAPDOOR_OPEN, SoundEventKeys.BLOCK_CHERRY_WOOD_TRAPDOOR_CLOSE,
            SoundEventKeys.BLOCK_CHERRY_WOOD_BUTTON_CLICK_ON, SoundEventKeys.BLOCK_CHERRY_WOOD_BUTTON_CLICK_OFF),
    NETHER_WOOD(SoundEventKeys.BLOCK_NETHER_WOOD_DOOR_OPEN, SoundEventKeys.BLOCK_NETHER_WOOD_DOOR_CLOSE,
            SoundEventKeys.BLOCK_NETHER_WOOD_TRAPDOOR_OPEN, SoundEventKeys.BLOCK_NETHER_WOOD_TRAPDOOR_CLOSE,
            SoundEventKeys.BLOCK_NETHER_WOOD_BUTTON_CLICK_ON, SoundEventKeys.BLOCK_NETHER_WOOD_BUTTON_CLICK_OFF),
    BAMBOO(SoundEventKeys.BLOCK_BAMBOO_WOOD_DOOR_OPEN, SoundEventKeys.BLOCK_BAMBOO_WOOD_DOOR_CLOSE,
            SoundEventKeys.BLOCK_BAMBOO_WOOD_TRAPDOOR_OPEN, SoundEventKeys.BLOCK_BAMBOO_WOOD_TRAPDOOR_CLOSE,
            SoundEventKeys.BLOCK_BAMBOO_WOOD_BUTTON_CLICK_ON, SoundEventKeys.BLOCK_BAMBOO_WOOD_BUTTON_CLICK_OFF),
    IRON(SoundEventKeys.BLOCK_IRON_DOOR_OPEN, SoundEventKeys.BLOCK_IRON_DOOR_CLOSE,
            SoundEventKeys.BLOCK_IRON_TRAPDOOR_OPEN, SoundEventKeys.BLOCK_IRON_TRAPDOOR_CLOSE,
            SoundEventKeys.BLOCK_STONE_BUTTON_CLICK_ON, SoundEventKeys.BLOCK_STONE_BUTTON_CLICK_OFF),
    COPPER(SoundEventKeys.BLOCK_COPPER_DOOR_OPEN, SoundEventKeys.BLOCK_COPPER_DOOR_CLOSE,
            SoundEventKeys.BLOCK_COPPER_TRAPDOOR_OPEN, SoundEventKeys.BLOCK_COPPER_TRAPDOOR_CLOSE,
            SoundEventKeys.BLOCK_STONE_BUTTON_CLICK_ON, SoundEventKeys.BLOCK_STONE_BUTTON_CLICK_OFF),
    STONE(SoundEventKeys.BLOCK_IRON_DOOR_OPEN, SoundEventKeys.BLOCK_IRON_DOOR_CLOSE,
            SoundEventKeys.BLOCK_IRON_TRAPDOOR_OPEN, SoundEventKeys.BLOCK_IRON_TRAPDOOR_CLOSE,
            SoundEventKeys.BLOCK_STONE_BUTTON_CLICK_ON, SoundEventKeys.BLOCK_STONE_BUTTON_CLICK_OFF);

    private final TypedKey<SoundEvent> doorOpen;
    private final TypedKey<SoundEvent> doorClose;
    private final TypedKey<SoundEvent> trapdoorOpen;
    private final TypedKey<SoundEvent> trapdoorClose;
    private final TypedKey<SoundEvent> buttonOn;
    private final TypedKey<SoundEvent> buttonOff;

    RedstoneBlockSet(final TypedKey<SoundEvent> doorOpen, final TypedKey<SoundEvent> doorClose,
                     final TypedKey<SoundEvent> trapdoorOpen, final TypedKey<SoundEvent> trapdoorClose,
                     final TypedKey<SoundEvent> buttonOn, final TypedKey<SoundEvent> buttonOff) {
        this.doorOpen = doorOpen;
        this.doorClose = doorClose;
        this.trapdoorOpen = trapdoorOpen;
        this.trapdoorClose = trapdoorClose;
        this.buttonOn = buttonOn;
        this.buttonOff = buttonOff;
    }

    TypedKey<SoundEvent> door(final boolean open) {
        return open ? doorOpen : doorClose;
    }

    TypedKey<SoundEvent> trapdoor(final boolean open) {
        return open ? trapdoorOpen : trapdoorClose;
    }

    TypedKey<SoundEvent> button(final boolean pressed) {
        return pressed ? buttonOn : buttonOff;
    }
}
