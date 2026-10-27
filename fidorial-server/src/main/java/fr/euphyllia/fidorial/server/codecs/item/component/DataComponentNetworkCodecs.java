package fr.euphyllia.fidorial.server.codecs.item.component;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.item.enchantment.FidorialEnchantmentRegistry;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.registry.RegistryHolder;
import fr.fidorial.item.component.ItemEnchantments;
import fr.fidorial.item.component.ItemLore;
import fr.fidorial.item.component.SwingAnimation;
import fr.fidorial.item.component.WrittenBookContent;
import fr.fidorial.item.data.DataComponentType;
import fr.fidorial.item.data.DataComponentTypes;
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

        void write(PacketBuffer buf, RegistryHolder frozen, T value);

        T read(PacketBuffer buf, RegistryHolder frozen);
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
                                       final RegistryHolder frozen,
                                       final DataComponentType<T> type,
                                       final Object value) {

        final Codec<T> codec = codec(type);
        if (codec == null) {
            throw new IllegalStateException("No wire codec for component " + type);
        }
        codec.write(buf, frozen, type.valueType().cast(value));
    }

    private static Codec<Integer> varIntCodec() {
        return new Codec<>() {
            @Override
            public void write(final PacketBuffer buf, final RegistryHolder frozen, final Integer value) {
                buf.writeVarInt(value);
            }

            @Override
            public Integer read(final PacketBuffer buf, final RegistryHolder frozen) {
                return buf.readVarInt();
            }
        };
    }

    private static Codec<Boolean> booleanCodec() {
        return new Codec<>() {
            @Override
            public void write(final PacketBuffer buf, final RegistryHolder frozen, final Boolean value) {
                buf.writeBoolean(value);
            }

            @Override
            public Boolean read(final PacketBuffer buf, final RegistryHolder frozen) {
                return buf.readBoolean();
            }
        };
    }

    private static Codec<Key> identifierCodec() {
        return new Codec<>() {
            @Override
            public void write(final PacketBuffer buf, final RegistryHolder frozen, final Key value) {
                buf.writeKey(value);
            }

            @Override
            public Key read(final PacketBuffer buf, final RegistryHolder frozen) {
                return buf.readKey();
            }
        };
    }

    private static Codec<Component> textCodec() {
        return new Codec<>() {
            @Override
            public void write(final PacketBuffer buf, final RegistryHolder frozen, final Component value) {
                buf.writeComponent(value);
            }

            @Override
            public Component read(final PacketBuffer buf, final RegistryHolder frozen) {
                return buf.readComponent(MAX_COMPONENT_TEXT_LENGTH);
            }
        };
    }

    private static Codec<ItemLore> loreCodec() {
        return new Codec<>() {
            @Override
            public void write(final PacketBuffer buf, final RegistryHolder frozen, final ItemLore value) {
                buf.writeVarInt(value.size());
                for (final Component line : value.lines()) {
                    buf.writeComponent(line);
                }
            }

            @Override
            public ItemLore read(final PacketBuffer buf, final RegistryHolder frozen) {
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
            public void write(final PacketBuffer buf, final RegistryHolder frozen, final ItemEnchantments value) {
                final FidorialEnchantmentRegistry registry = FidorialServer.getInstance().registries().enchantments();
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
            public ItemEnchantments read(final PacketBuffer buf, final RegistryHolder frozen) {
                final FidorialEnchantmentRegistry registry = FidorialServer.getInstance().registries().enchantments();
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
            public void write(final PacketBuffer buf, final RegistryHolder frozen, final WrittenBookContent value) {
                buf.writeString(value.title());
                buf.writeBoolean(false);
                buf.writeString(value.author());
                buf.writeVarInt(value.generation());
                buf.writeVarInt(value.pages().size());
                for (final Component page : value.pages()) {
                    buf.writeComponent(page);
                    buf.writeBoolean(false);
                }
                buf.writeBoolean(value.resolved());
            }

            @Override
            public WrittenBookContent read(final PacketBuffer buf, final RegistryHolder frozen) {
                final String title = buf.readString(WrittenBookContent.MAX_TITLE_LENGTH);
                if (buf.readBoolean()) {
                    buf.readString(WrittenBookContent.MAX_TITLE_LENGTH);
                }
                final String author = buf.readString(Short.MAX_VALUE);
                final int generation = buf.readVarInt();
                if (generation < 0 || generation > WrittenBookContent.MAX_GENERATION) {
                    throw new DecoderException("Implausible book generation: " + generation);
                }
                final int size = readBoundedLength(buf, WrittenBookContent.MAX_PAGES, "book");
                final List<Component> pages = new ArrayList<>(size);
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
            public void write(final PacketBuffer buf, final RegistryHolder frozen, final SwingAnimation value) {
                buf.writeVarInt(networkId(value.type()));
                buf.writeVarInt(value.duration());
            }

            @Override
            public SwingAnimation read(final PacketBuffer buf, final RegistryHolder frozen) {
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
