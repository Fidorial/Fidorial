package fr.euphyllia.fidorial.server.codecs.item.component;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.registry.ServerRegistries;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import fr.fidorial.item.component.ItemEnchantments;
import fr.fidorial.item.component.ItemLore;
import fr.fidorial.item.component.SwingAnimation;
import fr.fidorial.item.component.WrittenBookContent;
import fr.fidorial.item.data.DataComponentType;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.item.enchantment.EnchantmentDefinition;
import fr.fidorial.registry.RegistryKey;
import io.netty.handler.codec.DecoderException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DataComponentNetworkCodecs {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(DataComponentNetworkCodecs.class);

    private static final int MAX_COMPONENT_TEXT_LENGTH = 262_144;

    private static final int MAX_ENCHANTMENTS = 1024;

    public interface Codec<T> {

        void write(PacketBuffer buf, ServerRegistries registries, T value);

        T read(PacketBuffer buf, ServerRegistries registries);
    }

    private static final Map<DataComponentType<?>, Codec<?>> CODECS = new LinkedHashMap<>();

    static {
        register(DataComponentTypes.MAX_STACK_SIZE, varIntCodec());
        register(DataComponentTypes.MAX_DAMAGE, varIntCodec());
        register(DataComponentTypes.DAMAGE, varIntCodec());
        register(DataComponentTypes.ITEM_MODEL, identifierCodec());
        register(DataComponentTypes.CUSTOM_NAME, textCodec());
        register(DataComponentTypes.ITEM_NAME, textCodec());
        register(DataComponentTypes.LORE, loreCodec());
        register(DataComponentTypes.ATTACK_ANIMATION, swingAnimationCodec());
        register(DataComponentTypes.INTERACT_ANIMATION, swingAnimationCodec());
        register(DataComponentTypes.MAP_ID, varIntCodec());
        register(DataComponentTypes.ENCHANTMENTS, enchantmentsCodec());
        register(DataComponentTypes.STORED_ENCHANTMENTS, enchantmentsCodec());
        register(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, booleanCodec());
        register(DataComponentTypes.WRITTEN_BOOK_CONTENT, writtenBookContentCodec());
    }

    private DataComponentNetworkCodecs() {
        throw new UnsupportedOperationException("DataComponentNetworkCodecs cannot be instantiated.");
    }

    private static <T> void register(final DataComponentType<T> type, final Codec<T> codec) {
        CODECS.put(type, codec);
    }

    @SuppressWarnings("unchecked")
    public static <T> @Nullable Codec<T> codec(final DataComponentType<T> type) {
        return (Codec<T>) CODECS.get(Objects.requireNonNull(type, "type"));
    }

    public static boolean hasCodec(final DataComponentType<?> type) {
        return CODECS.containsKey(Objects.requireNonNull(type, "type"));
    }

    public static <T> void writeErased(final PacketBuffer buf,
                                       final ServerRegistries registries,
                                       final DataComponentType<T> type,
                                       final Object value) {

        final Codec<T> codec = codec(type);
        if (codec == null) {
            throw new IllegalStateException("No wire codec for component " + type);
        }
        codec.write(buf, registries, type.valueType().cast(value));
    }

    private static Codec<Integer> varIntCodec() {
        return new Codec<>() {
            @Override
            public void write(final PacketBuffer buf, final ServerRegistries registries, final Integer value) {
                buf.writeVarInt(value);
            }

            @Override
            public Integer read(final PacketBuffer buf, final ServerRegistries registries) {
                return buf.readVarInt();
            }
        };
    }

    private static Codec<Boolean> booleanCodec() {
        return new Codec<>() {
            @Override
            public void write(final PacketBuffer buf, final ServerRegistries registries, final Boolean value) {
                buf.writeBoolean(value);
            }

            @Override
            public Boolean read(final PacketBuffer buf, final ServerRegistries registries) {
                return buf.readBoolean();
            }
        };
    }

    private static Codec<Key> identifierCodec() {
        return new Codec<>() {
            @Override
            public void write(final PacketBuffer buf, final ServerRegistries registries, final Key value) {
                buf.writeKey(value);
            }

            @Override
            public Key read(final PacketBuffer buf, final ServerRegistries registries) {
                return buf.readKey();
            }
        };
    }

    private static Codec<Component> textCodec() {
        return new Codec<>() {
            @Override
            public void write(final PacketBuffer buf, final ServerRegistries registries, final Component value) {
                buf.writeComponent(value);
            }

            @Override
            public Component read(final PacketBuffer buf, final ServerRegistries registries) {
                return buf.readComponent(MAX_COMPONENT_TEXT_LENGTH);
            }
        };
    }

    private static Codec<ItemLore> loreCodec() {
        return new Codec<>() {
            @Override
            public void write(final PacketBuffer buf, final ServerRegistries registries, final ItemLore value) {
                buf.writeVarInt(value.size());
                for (final Component line : value.lines()) {
                    buf.writeComponent(line);
                }
            }

            @Override
            public ItemLore read(final PacketBuffer buf, final ServerRegistries registries) {
                final int size = readBoundedLength(buf, ItemLore.MAX_LINES, "lore");

                final List<Component> lines = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    lines.add(buf.readComponent(MAX_COMPONENT_TEXT_LENGTH));
                }

                return new ItemLore(lines);
            }
        };
    }

    private static Codec<ItemEnchantments> enchantmentsCodec() {
        return new Codec<>() {
            @Override
            public void write(final PacketBuffer buf, final ServerRegistries registries, final ItemEnchantments value) {
                final ServerRegistry<EnchantmentDefinition> registry = registries.registry(RegistryKey.ENCHANTMENT);
                final Map<Integer, Integer> known = new LinkedHashMap<>();
                for (final Map.Entry<Key, Integer> level : value.levels().entrySet()) {
                    final int id = registry.networkId(level.getKey());
                    if (id < 0) {
                        LOGGER.debug("Dropping the enchantment {}: absent from the registry", level.getKey().asString());
                        continue;
                    }
                    known.put(id, level.getValue());
                }
                buf.writeVarInt(known.size());
                for (final Map.Entry<Integer, Integer> level : known.entrySet()) {
                    buf.writeVarInt(level.getKey());
                    buf.writeVarInt(level.getValue());
                }
            }

            @Override
            public ItemEnchantments read(final PacketBuffer buf, final ServerRegistries registries) {
                final ServerRegistry<EnchantmentDefinition> registry = registries.registry(RegistryKey.ENCHANTMENT);
                final int size = readBoundedLength(buf, MAX_ENCHANTMENTS, "enchantments");
                final Map<Key, Integer> levels = new LinkedHashMap<>();
                for (int i = 0; i < size; i++) {
                    final Key enchantment = registry.byNetworkId(buf.readVarInt());
                    final int level = buf.readVarInt();
                    if (enchantment == null) {
                        throw new DecoderException("Unknown enchantment network id");
                    }
                    if (level < 1 || level > ItemEnchantments.MAX_LEVEL) {
                        throw new DecoderException("Implausible level " + level + " for " + enchantment.asString());
                    }
                    levels.put(enchantment, level);
                }
                return new ItemEnchantments(levels);
            }
        };
    }

    private static Codec<WrittenBookContent> writtenBookContentCodec() {
        return new Codec<>() {
            @Override
            public void write(final PacketBuffer buf, final ServerRegistries registries, final WrittenBookContent value) {
                buf.writeString(WrittenBookTexts.title(value.title()));
                buf.writeBoolean(false);
                buf.writeString(WrittenBookTexts.author(value.author()));
                buf.writeVarInt(value.generation());
                buf.writeVarInt(value.pages().size());
                for (final Component page : value.pages()) {
                    buf.writeComponent(page);
                    buf.writeBoolean(false);
                }
                buf.writeBoolean(value.resolved());
            }

            @Override
            public WrittenBookContent read(final PacketBuffer buf, final ServerRegistries registries) {
                final Component title = Component.text(buf.readString(WrittenBookContent.MAX_TITLE_LENGTH));
                if (buf.readBoolean()) {
                    buf.readString(WrittenBookContent.MAX_TITLE_LENGTH);
                }
                final Component author = Component.text(buf.readString(Short.MAX_VALUE));
                final int generation = buf.readVarInt();
                if (generation < 0 || generation > WrittenBookContent.MAX_GENERATION) {
                    throw new DecoderException("Implausible book generation: " + generation);
                }
                final int size = readBoundedLength(buf, WrittenBookContent.MAX_PAGES, "book");
                final List<Component> pages = new ArrayList<>(Math.min(size, 16));
                for (int i = 0; i < size; i++) {
                    pages.add(buf.readComponent(MAX_COMPONENT_TEXT_LENGTH));
                    if (buf.readBoolean()) {
                        buf.readComponent(MAX_COMPONENT_TEXT_LENGTH);
                    }
                }
                return new WrittenBookContent(title, author, generation, pages, buf.readBoolean());
            }
        };
    }

    private static Codec<SwingAnimation> swingAnimationCodec() {
        return new Codec<>() {
            @Override
            public void write(final PacketBuffer buf, final ServerRegistries registries, final SwingAnimation value) {
                buf.writeVarInt(networkId(value.type()));
                buf.writeVarInt(value.duration());
            }

            @Override
            public SwingAnimation read(final PacketBuffer buf, final ServerRegistries registries) {
                final SwingAnimation.SwingAnimationType type = fromNetworkId(buf.readVarInt());
                final int duration = buf.readVarInt();
                return new SwingAnimation(type, duration);
            }
        };
    }

    private static int networkId(final SwingAnimation.SwingAnimationType type) {
        return switch (type) {
            case WHACK -> 1;
            case STAB -> 2;
        };
    }

    private static SwingAnimation.SwingAnimationType fromNetworkId(final int id) {
        return switch (id) {
            case 1 -> SwingAnimation.SwingAnimationType.WHACK;
            case 2 -> SwingAnimation.SwingAnimationType.STAB;
            default -> throw new DecoderException("Unknown swing animation type id: " + id);
        };
    }

    private static int readBoundedLength(final PacketBuffer buf, final int max, final String what) {
        final int size = buf.readVarInt();
        if (size < 0 || size > max) {
            throw new DecoderException("Implausible " + what + " length: " + size);
        }
        return size;
    }
}
