package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.data.GameEvent;
import net.kyori.adventure.key.KeyPattern;

/**
 * Keys of the vanilla tags of the {@code minecraft:game_event} registry.
 */
public final class GameEventTagKeys {
    /**
     * Tag {@code #minecraft:allay_can_listen}.
     */
    public static final TagKey<GameEvent> ALLAY_CAN_LISTEN = create("allay_can_listen");

    /**
     * Tag {@code #minecraft:ignore_vibrations_sneaking}.
     */
    public static final TagKey<GameEvent> IGNORE_VIBRATIONS_SNEAKING = create("ignore_vibrations_sneaking");

    /**
     * Tag {@code #minecraft:shrieker_can_listen}.
     */
    public static final TagKey<GameEvent> SHRIEKER_CAN_LISTEN = create("shrieker_can_listen");

    /**
     * Tag {@code #minecraft:vibrations}.
     */
    public static final TagKey<GameEvent> VIBRATIONS = create("vibrations");

    /**
     * Tag {@code #minecraft:warden_can_listen}.
     */
    public static final TagKey<GameEvent> WARDEN_CAN_LISTEN = create("warden_can_listen");

    private GameEventTagKeys() {
        throw new UnsupportedOperationException("GameEventTagKeys cannot be instantiated.");
    }

    private static TagKey<GameEvent> create(@KeyPattern final String value) {
        return TagKey.create(RegistryKey.GAME_EVENT, value);
    }
}
