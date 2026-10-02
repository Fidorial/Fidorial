package fr.fidorial.entity;

import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.ObjectContentsLike;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static net.kyori.adventure.text.object.PlayerHeadObjectContents.property;

/**
 * The identity of a player, with the signed properties carrying their skin.
 *
 * <p>Usable as the skin source of a player head component.</p>
 *
 * @param uuid       the player identity
 * @param name       the player name
 * @param properties the profile properties, usually the signed {@code textures}
 * @since 0.1.0
 */
public record PlayerProfile(UUID uuid, String name, List<Property> properties) implements PlayerHeadObjectContents.SkinSource, ObjectContentsLike {

    /**
     * Copies the properties.
     */
    public PlayerProfile {
        properties = List.copyOf(properties);
    }

    /**
     * Creates a profile without properties, hence without skin.
     *
     * @param uuid the player identity
     * @param name the player name
     * @since 0.1.0
     */
    public PlayerProfile(final UUID uuid, final String name) {
        this(uuid, name, List.of());
    }

    /**
     * Creates a profile without properties from a cached identity.
     *
     * @param meta the cached identity
     * @since 0.1.0
     */
    public PlayerProfile(final PlayerProfileMeta meta) {
        this(meta.id(), meta.name(), List.of());
    }

    @Override
    @SuppressWarnings("UnstableApiUsage") // we are permitted to override
    public void applySkinToPlayerHeadContents(final PlayerHeadObjectContents.Builder builder) {
        if (!this.properties.isEmpty()) {
            builder.profileProperties(
                    this.properties.stream()
                            .map(p -> property(p.name(), p.value(), p.signature()))
                            .toList()
            );
        }
        builder.id(this.uuid)
                .name(this.name);
    }

    @Override
    public ObjectContents asObjectContents() {
        return ObjectContents.playerHead(this);
    }

    /**
     * A profile property, such as the {@code textures} one carrying the skin.
     *
     * @param name      the property name
     * @param value     the value, usually Base64-encoded
     * @param signature the Mojang signature of the value, or {@code null} if unsigned
     * @since 0.1.0
     */
    public record Property(
            String name, String value, @Nullable String signature) {
    }
}
