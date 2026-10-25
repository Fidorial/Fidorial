package fr.euphyllia.fidorial.server.item.enchantment;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.item.FidorialItemRegistry;
import fr.euphyllia.fidorial.server.item.enchantment.effect.EnchantmentEffectSet;
import fr.euphyllia.fidorial.server.registry.RegistryEntry;
import fr.euphyllia.fidorial.server.registry.RegistryHolder;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.component.ItemEnchantments;
import fr.fidorial.item.data.DataComponentType;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.item.enchantment.EnchantmentBehaviour;
import fr.fidorial.item.enchantment.EnchantmentDefinition;
import fr.fidorial.item.enchantment.EnchantmentRegistry;
import fr.fidorial.item.enchantment.EnchantmentTags;
import fr.fidorial.item.enchantment.KeySet;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.Registry;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Enchantment;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Server implementation of {@link EnchantmentRegistry}, backed by the vanilla registry dump.
 */
public final class FidorialEnchantmentRegistry implements EnchantmentRegistry, Registry<Enchantment> {

    public static final Key REGISTRY_NAME = Key.key("enchantment");

    private static final Key ITEM_REGISTRY = Key.key("item");
    private static final Key ENCHANTED_BOOK = Key.key("enchanted_book");

    private static final ComponentLogger LOGGER = ComponentLogger.logger(FidorialEnchantmentRegistry.class);

    /**
     * Once started, changing the registry sends the players back to the configuration phase.
     */
    public final AtomicBoolean started = new AtomicBoolean(false);

    private final Map<Key, Entry> vanilla;
    private final RegistryHolder frozen;
    private final Map<Key, List<Attachment>> attachments = new ConcurrentHashMap<>();

    private volatile Snapshot snapshot;
    private volatile Map<Key, List<EnchantmentBehaviour>> behaviours = Map.of();

    private FidorialEnchantmentRegistry(final Map<Key, Entry> vanilla, final Map<Key, List<Key>> tags,
                                        final RegistryHolder frozen) {
        this.vanilla = Collections.unmodifiableMap(new LinkedHashMap<>(vanilla));
        this.frozen = frozen;
        this.snapshot = Snapshot.of(this.vanilla, tags);
        rebuildBehaviours();
    }

    public static FidorialEnchantmentRegistry bootstrap(final RegistryHolder dynamic, final RegistryHolder frozen) {
        final fr.euphyllia.fidorial.server.registry.Registry source = dynamic.get(REGISTRY_NAME);
        final Map<Key, Entry> vanilla = new LinkedHashMap<>();
        if (source == null) {
            LOGGER.warn("No vanilla enchantment found in the registry dump, starting empty.");
            return new FidorialEnchantmentRegistry(vanilla, Map.of(), frozen);
        }
        int skippedEffects = 0;
        for (final RegistryEntry entry : source.networkEntries()) {
            final BinaryTag data = entry.data();
            if (data == null) {
                LOGGER.warn("The vanilla enchantment {} has no data in the registry dump, it is left out.", entry.key().asString());
                continue;
            }
            try {
                final JsonElement json = EnchantmentJson.fromNbt(data);
                if (!json.isJsonObject()) {
                    throw new IllegalArgumentException("its data is not an object");
                }
                final EnchantmentDefinitionImpl definition = EnchantmentJson.read(entry.key(), json.getAsJsonObject(), null);
                for (final String skipped : definition.effects().skipped()) {
                    LOGGER.debug("Enchantment {}: {} is not run by the server", entry.key().asString(), skipped);
                }
                skippedEffects += definition.effects().skipped().size();
                vanilla.put(entry.key(), new Entry(definition, data, null));
            } catch (final RuntimeException exception) {
                LOGGER.error("Could not read the vanilla enchantment {}, it is left out.", entry.key().asString(), exception);
            }
        }
        LOGGER.info("Loaded {} vanilla enchantments, {} of their effects need gameplay the server does not have yet",
                vanilla.size(), skippedEffects);
        return new FidorialEnchantmentRegistry(vanilla, source.tags(), frozen);
    }

    @Override
    public EnchantmentDefinition.Builder define(final TypedKey<Enchantment> key) {
        return new EnchantmentDefinitionImpl.BuilderImpl(key);
    }

    @Override
    public EnchantmentDefinition register(final EnchantmentDefinition definition, final Plugin owner) {
        final EnchantmentDefinitionImpl impl = impl(definition);
        Preconditions.checkArgument(owner != null, "The owner of %s must not be null", impl.key());
        validate(impl);
        synchronized (this) {
            Preconditions.checkArgument(!snapshot.entries.containsKey(impl.key()),
                    "An enchantment is already registered under %s; use overwrite() to replace it", impl.key().asString());
            final Map<Key, Entry> next = new LinkedHashMap<>(snapshot.entries);
            next.put(impl.key(), new Entry(impl, null, owner));
            publish(Snapshot.of(next, withExclusiveTag(snapshot.tags, impl)), change(impl.key(), "registered"));
        }
        return impl;
    }

    @Override
    public EnchantmentDefinition registerFromJson(final TypedKey<Enchantment> key, final String json, final Plugin owner) {
        Preconditions.checkArgument(key != null, "The key of an enchantment must not be null");
        Preconditions.checkArgument(json != null, "The JSON of %s must not be null", key.key());
        final JsonObject object = EnchantmentJson.parse(key.key(), json);
        return register(EnchantmentJson.read(key.key(), object, null), owner);
    }

    @Override
    public Optional<EnchantmentDefinition> overwrite(final EnchantmentDefinition definition, final Plugin owner) {
        final EnchantmentDefinitionImpl impl = impl(definition);
        Preconditions.checkArgument(owner != null, "The owner of %s must not be null", impl.key());
        validate(impl);
        synchronized (this) {
            final Entry previous = snapshot.entries.get(impl.key());
            final Map<Key, Entry> next = new LinkedHashMap<>(snapshot.entries);
            next.put(impl.key(), new Entry(impl, null, owner));
            publish(Snapshot.of(next, withExclusiveTag(snapshot.tags, impl)),
                    change(impl.key(), previous == null ? "registered" : "redefined"));
            return previous == null ? Optional.empty() : Optional.of(previous.definition());
        }
    }

    @Override
    public boolean unregister(final TypedKey<Enchantment> key) {
        Preconditions.checkArgument(key != null, "The key of an enchantment must not be null");
        synchronized (this) {
            final Entry current = snapshot.entries.get(key.key());
            if (current == null || !current.isCustom()) {
                return false;
            }
            final Map<Key, Entry> next = new LinkedHashMap<>(snapshot.entries);
            final Entry stock = vanilla.get(key.key());
            if (stock != null) {
                next.put(key.key(), stock);
                publish(Snapshot.of(next, snapshot.tags), change(key.key(), "restored to vanilla"));
            } else {
                next.remove(key.key());
                publish(Snapshot.of(next, withoutMember(snapshot.tags, key.key())), change(key.key(), "unregistered"));
            }
            return true;
        }
    }

    @Override
    public void unregisterAll(final Plugin owner) {
        Preconditions.checkArgument(owner != null, "The owner must not be null");
        synchronized (this) {
            boolean changed = false;
            for (final List<Attachment> list : attachments.values()) {
                changed |= list.removeIf(attachment -> attachment.owner() == owner);
            }
            final Map<Key, Entry> next = new LinkedHashMap<>(snapshot.entries);
            Map<Key, List<Key>> tags = snapshot.tags;
            boolean entriesChanged = false;
            for (final Entry entry : snapshot.entries.values()) {
                if (entry.owner() != owner) {
                    continue;
                }
                final Key key = entry.definition().key();
                final Entry stock = vanilla.get(key);
                if (stock != null) {
                    next.put(key, stock);
                } else {
                    next.remove(key);
                    tags = withoutMember(tags, key);
                }
                entriesChanged = true;
            }
            if (entriesChanged) {
                publish(Snapshot.of(next, tags), "Enchantments of " + owner.getClass().getSimpleName() + " unregistered");
            } else if (changed) {
                rebuildBehaviours();
            }
        }
    }

    private static EnchantmentDefinitionImpl impl(final EnchantmentDefinition definition) {
        Preconditions.checkArgument(definition != null, "The enchantment definition must not be null");
        if (!(definition instanceof final EnchantmentDefinitionImpl impl)) {
            throw new IllegalArgumentException("Enchantment definitions must come from EnchantmentRegistry.define(), got "
                    + definition.getClass().getName());
        }
        return impl;
    }

    /**
     * The client only knows the item tags of the vanilla data: an unknown one would fail its registry.
     */
    private void validate(final EnchantmentDefinitionImpl definition) {
        checkItemTag(definition.key(), definition.supportedItems(), "supported items");
        definition.primaryItems().ifPresent(primary -> checkItemTag(definition.key(), primary, "primary items"));
    }

    private void checkItemTag(final Key key, final KeySet set, final String what) {
        final Optional<Key> tag = set.tag();
        if (tag.isPresent() && !hasItemTag(tag.get())) {
            throw new IllegalArgumentException("The " + what + " of " + key.asString() + " name the unknown item tag #"
                    + tag.get().asString());
        }
    }

    private boolean hasItemTag(final Key tag) {
        final fr.euphyllia.fidorial.server.registry.Registry items = frozen.get(ITEM_REGISTRY);
        return items != null && items.tags().containsKey(tag);
    }

    /**
     * An exclusive set naming a tag that does not exist yet creates it empty, so that clients find it.
     */
    private static Map<Key, List<Key>> withExclusiveTag(final Map<Key, List<Key>> tags, final EnchantmentDefinitionImpl definition) {
        final Optional<Key> tag = definition.exclusiveSet().tag();
        if (tag.isEmpty() || tags.containsKey(tag.get())) {
            return tags;
        }
        final Map<Key, List<Key>> next = new LinkedHashMap<>(tags);
        next.put(tag.get(), List.of());
        return next;
    }

    private static Map<Key, List<Key>> withoutMember(final Map<Key, List<Key>> tags, final Key member) {
        final Map<Key, List<Key>> next = new LinkedHashMap<>();
        tags.forEach((tag, members) -> {
            if (members.contains(member)) {
                final List<Key> kept = new ArrayList<>(members);
                kept.remove(member);
                next.put(tag, List.copyOf(kept));
            } else {
                next.put(tag, members);
            }
        });
        return next;
    }

    @Override
    public void attach(final TypedKey<Enchantment> key, final EnchantmentBehaviour behaviour, final Plugin owner) {
        Preconditions.checkArgument(key != null, "The key of an enchantment must not be null");
        Preconditions.checkArgument(behaviour != null, "The behaviour attached to %s must not be null", key.key());
        Preconditions.checkArgument(owner != null, "The owner of the behaviour attached to %s must not be null", key.key());
        synchronized (this) {
            Preconditions.checkArgument(snapshot.entries.containsKey(key.key()), "Unknown enchantment %s", key.key().asString());
            attachments.computeIfAbsent(key.key(), _ -> new CopyOnWriteArrayList<>()).add(new Attachment(behaviour, owner));
            rebuildBehaviours();
        }
        LOGGER.debug("Behaviour attached to the enchantment {} by {}", key.key().asString(), owner.getClass().getSimpleName());
    }

    @Override
    public boolean detach(final TypedKey<Enchantment> key, final Plugin owner) {
        synchronized (this) {
            final List<Attachment> list = attachments.get(key.key());
            final boolean removed = list != null && list.removeIf(attachment -> attachment.owner() == owner);
            if (removed) {
                rebuildBehaviours();
            }
            return removed;
        }
    }

    /**
     * {@return the behaviours of an enchantment, in the order they run: its vanilla effects, the behaviour
     * of its definition, then the attached ones; empty for an unknown enchantment}
     */
    public List<EnchantmentBehaviour> behaviours(final Key enchantment) {
        return behaviours.getOrDefault(enchantment, List.of());
    }

    /**
     * {@return an object that changes whenever the enchantments or their behaviours do, for those caching
     * what an equipped stack does}
     */
    public Object version() {
        return behaviours;
    }

    private void rebuildBehaviours() {
        final Map<Key, List<EnchantmentBehaviour>> next = new HashMap<>();
        for (final Entry entry : snapshot.entries.values()) {
            final EnchantmentDefinitionImpl definition = entry.definition();
            final List<EnchantmentBehaviour> list = new ArrayList<>(2);
            if (definition.effects() != EnchantmentEffectSet.EMPTY) {
                list.add(definition.effects());
            }
            definition.behaviour().ifPresent(list::add);
            for (final Attachment attachment : attachments.getOrDefault(definition.key(), List.of())) {
                list.add(attachment.behaviour());
            }
            if (!list.isEmpty()) {
                next.put(definition.key(), List.copyOf(list));
            }
        }
        this.behaviours = Map.copyOf(next);
    }

    /**
     * {@return the definition registered under {@code key}, {@code null} for an unknown enchantment}
     */
    public @Nullable EnchantmentDefinitionImpl definition(final Key key) {
        final Entry entry = snapshot.entries.get(key);
        return entry == null ? null : entry.definition();
    }

    @Override
    public Optional<EnchantmentDefinition> definition(final TypedKey<Enchantment> key) {
        return Optional.ofNullable(definition(key.key()));
    }

    @Override
    public Collection<EnchantmentDefinition> definitions() {
        return snapshot.definitions;
    }

    @Override
    public Collection<TypedKey<Enchantment>> keys() {
        return snapshot.typedKeys;
    }

    @Override
    public boolean contains(final TypedKey<Enchantment> key) {
        return snapshot.entries.containsKey(key.key());
    }

    @Override
    public boolean isCustom(final TypedKey<Enchantment> key) {
        return isCustom(key.key());
    }

    public boolean isCustom(final Key key) {
        final Entry entry = snapshot.entries.get(key);
        return entry != null && entry.isCustom();
    }

    @Override
    public int networkId(final TypedKey<Enchantment> key) {
        return networkId(key.key());
    }

    public int networkId(final Key key) {
        final Integer id = snapshot.ids.get(key);
        return id == null ? -1 : id;
    }

    /**
     * {@return the enchantment sent under {@code networkId}, {@code null} when out of range}
     */
    public @Nullable Key byNetworkId(final int networkId) {
        final List<Key> order = snapshot.order;
        return networkId < 0 || networkId >= order.size() ? null : order.get(networkId);
    }

    public int size() {
        return snapshot.order.size();
    }

    @Override
    public Set<Key> supportedEffectComponents() {
        return EnchantmentEffectSet.SUPPORTED;
    }

    @Override
    public Collection<Key> tags() {
        return snapshot.tags.keySet();
    }

    @Override
    public Collection<TypedKey<Enchantment>> tag(final Key tag) {
        final List<Key> members = snapshot.tags.getOrDefault(tag, List.of());
        final List<TypedKey<Enchantment>> typed = new ArrayList<>(members.size());
        for (final Key member : members) {
            typed.add(TypedKey.create(RegistryKey.ENCHANTMENT, member));
        }
        return Collections.unmodifiableList(typed);
    }

    @Override
    public boolean isTagged(final TypedKey<Enchantment> enchantment, final Key tag) {
        return isTagged(enchantment.key(), tag);
    }

    public boolean isTagged(final Key enchantment, final Key tag) {
        return snapshot.tags.getOrDefault(tag, List.of()).contains(enchantment);
    }

    @Override
    public boolean addToTag(final Key tag, final TypedKey<Enchantment> enchantment) {
        Preconditions.checkArgument(tag != null, "The tag must not be null");
        Preconditions.checkArgument(enchantment != null, "The enchantment must not be null");
        synchronized (this) {
            Preconditions.checkArgument(snapshot.entries.containsKey(enchantment.key()),
                    "Unknown enchantment %s", enchantment.key().asString());
            final List<Key> members = snapshot.tags.getOrDefault(tag, List.of());
            if (members.contains(enchantment.key())) {
                return false;
            }
            final List<Key> grown = new ArrayList<>(members);
            grown.add(enchantment.key());
            final Map<Key, List<Key>> next = new LinkedHashMap<>(snapshot.tags);
            next.put(tag, List.copyOf(grown));
            publish(Snapshot.of(snapshot.entries, next), change(enchantment.key(), "added to #" + tag.asString()));
            return true;
        }
    }

    @Override
    public boolean removeFromTag(final Key tag, final TypedKey<Enchantment> enchantment) {
        Preconditions.checkArgument(tag != null, "The tag must not be null");
        Preconditions.checkArgument(enchantment != null, "The enchantment must not be null");
        synchronized (this) {
            final List<Key> members = snapshot.tags.get(tag);
            if (members == null || !members.contains(enchantment.key())) {
                return false;
            }
            final List<Key> shrunk = new ArrayList<>(members);
            shrunk.remove(enchantment.key());
            final Map<Key, List<Key>> next = new LinkedHashMap<>(snapshot.tags);
            next.put(tag, List.copyOf(shrunk));
            publish(Snapshot.of(snapshot.entries, next), change(enchantment.key(), "removed from #" + tag.asString()));
            return true;
        }
    }

    @Override
    public boolean isSupportedItem(final TypedKey<Enchantment> enchantment, final Key item) {
        final EnchantmentDefinitionImpl definition = definition(enchantment.key());
        return definition != null && matchesItem(definition.supportedItems(), item);
    }

    /**
     * {@return {@code true} if {@code item}, or the vanilla item a plugin item is rendered as, belongs to {@code set}}
     */
    public boolean matchesItem(final KeySet set, final Key item) {
        if (containsItem(set, item)) {
            return true;
        }
        final FidorialItemRegistry items = FidorialServer.getInstance().registries().items();
        if (!items.isCustom(item)) {
            return false;
        }
        final Key rendered = items.networkType(item);
        return !rendered.equals(item) && containsItem(set, rendered);
    }

    private boolean containsItem(final KeySet set, final Key item) {
        final Optional<Key> tag = set.tag();
        if (tag.isPresent()) {
            return frozen.isTagged(ITEM_REGISTRY, tag.get(), item);
        }
        return set.keys().contains(item);
    }

    @Override
    public boolean areCompatible(final TypedKey<Enchantment> first, final TypedKey<Enchantment> second) {
        return areCompatible(first.key(), second.key());
    }

    public boolean areCompatible(final Key first, final Key second) {
        if (first.equals(second)) {
            return false;
        }
        final EnchantmentDefinitionImpl a = definition(first);
        final EnchantmentDefinitionImpl b = definition(second);
        return (a == null || !inSet(a.exclusiveSet(), second)) && (b == null || !inSet(b.exclusiveSet(), first));
    }

    private boolean inSet(final KeySet set, final Key enchantment) {
        final Optional<Key> tag = set.tag();
        return tag.isPresent() ? isTagged(enchantment, tag.get()) : set.keys().contains(enchantment);
    }

    @Override
    public boolean canEnchant(final ItemStack stack, final TypedKey<Enchantment> enchantment) {
        if (stack.isEmpty() || !isSupportedItem(enchantment, stack.id())) {
            return false;
        }
        for (final Key existing : enchantments(stack).levels().keySet()) {
            if (!areCompatible(existing, enchantment.key())) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@return the enchantments a stack carries, its stored ones for an enchanted book}
     */
    public static ItemEnchantments enchantments(final ItemStack stack) {
        return stack.getOrDefault(componentFor(stack), ItemEnchantments.EMPTY);
    }

    private static DataComponentType<ItemEnchantments> componentFor(final ItemStack stack) {
        return stack.id().equals(ENCHANTED_BOOK) ? DataComponentTypes.STORED_ENCHANTMENTS : DataComponentTypes.ENCHANTMENTS;
    }

    @Override
    public int level(final ItemStack stack, final TypedKey<Enchantment> enchantment) {
        if (stack.isEmpty()) {
            return 0;
        }
        return stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.EMPTY).level(enchantment.key());
    }

    @Override
    public int level(final LivingEntity entity, final TypedKey<Enchantment> enchantment) {
        final EnchantmentDefinitionImpl definition = definition(enchantment.key());
        if (definition == null) {
            return 0;
        }
        int highest = 0;
        for (final EquipmentSlot slot : EquipmentSlot.values()) {
            if (definition.matchesSlot(slot)) {
                highest = Math.max(highest, level(entity.equipment(slot), enchantment));
            }
        }
        return highest;
    }

    @Override
    public ItemStack enchant(final ItemStack stack, final TypedKey<Enchantment> enchantment, final int level) {
        Preconditions.checkArgument(stack != null, "The stack to enchant must not be null");
        Preconditions.checkArgument(enchantment != null && contains(enchantment), "Unknown enchantment %s", enchantment);
        Preconditions.checkArgument(level >= 1 && level <= EnchantmentDefinition.MAX_LEVEL,
                "The level of %s must be between 1 and %s, got %s", enchantment.key(), EnchantmentDefinition.MAX_LEVEL, level);
        if (stack.isEmpty()) {
            return stack;
        }
        final DataComponentType<ItemEnchantments> type = componentFor(stack);
        return stack.with(type, stack.getOrDefault(type, ItemEnchantments.EMPTY).with(enchantment.key(), level));
    }

    @Override
    public ItemStack disenchant(final ItemStack stack, final TypedKey<Enchantment> enchantment) {
        Preconditions.checkArgument(stack != null, "The stack must not be null");
        Preconditions.checkArgument(enchantment != null, "The enchantment must not be null");
        final DataComponentType<ItemEnchantments> type = componentFor(stack);
        final ItemEnchantments current = stack.get(type);
        if (current == null || !current.has(enchantment.key())) {
            return stack;
        }
        return stack.with(type, current.without(enchantment.key()));
    }

    @Override
    public ItemStack enchantedBook(final TypedKey<Enchantment> enchantment, final int level) {
        return enchant(ItemStack.of(ENCHANTED_BOOK), enchantment, level);
    }

    @Override
    public Component displayName(final TypedKey<Enchantment> enchantment, final int level) {
        final EnchantmentDefinitionImpl definition = definition(enchantment.key());
        final Component description = definition == null
                ? Component.translatable(EnchantmentDefinitionImpl.descriptionId(enchantment.key()))
                : definition.description();
        final NamedTextColor color = isTagged(enchantment.key(), EnchantmentTags.CURSE) ? NamedTextColor.RED : NamedTextColor.GRAY;
        Component name = Component.empty().color(color).append(description);
        if (level != 1 || definition == null || definition.maxLevel() != 1) {
            name = name.append(Component.space()).append(Component.translatable("enchantment.level." + level));
        }
        return name;
    }

    /**
     * {@return the entries to send in the registry data: the vanilla data of stock enchantments, the
     * definition of the others, with plugin items replaced by the vanilla items clients render}
     */
    public List<RegistryEntry> networkEntries() {
        final Snapshot current = snapshot;
        final List<RegistryEntry> entries = new ArrayList<>(current.order.size());
        for (final Key key : current.order) {
            final Entry entry = current.entries.get(key);
            entries.add(new RegistryEntry(key, entry.isCustom() || entry.vanillaPayload() == null
                    ? payload(entry.definition())
                    : entry.vanillaPayload()));
        }
        return entries;
    }

    private BinaryTag payload(final EnchantmentDefinitionImpl definition) {
        final FidorialItemRegistry items = FidorialServer.getInstance().registries().items();
        try {
            return EnchantmentJson.toNbt(EnchantmentJson.write(definition,
                    item -> items.isItem(item) ? items.networkType(item) : null,
                    enchantment -> snapshot.entries.containsKey(enchantment) ? enchantment : null));
        } catch (final RuntimeException exception) {
            LOGGER.error("Could not encode the enchantment {} for clients, sending a bare one instead",
                    definition.key().asString(), exception);
            return EnchantmentJson.toNbt(EnchantmentJson.write(new EnchantmentDefinitionImpl(definition.key(),
                            definition.description(), KeySetImpl.of(List.of(ENCHANTED_BOOK)), Optional.empty(), KeySetImpl.EMPTY,
                            definition.maxLevel(), definition.weight(), definition.anvilCost(), definition.minCost(),
                            definition.maxCost(), definition.slots(), null, EnchantmentEffectSet.EMPTY, Optional.empty()),
                    item -> item, enchantment -> null));
        }
    }

    /**
     * {@return the tags to send, by tag}
     */
    public Map<Key, List<Key>> networkTags() {
        return snapshot.tags;
    }

    @Override
    public RegistryKey<Enchantment> registryKey() {
        return RegistryKey.ENCHANTMENT;
    }

    @Override
    public Enchantment get(final TypedKey<Enchantment> key) {
        final EnchantmentDefinitionImpl definition = definition(key.key());
        if (definition == null) {
            throw new NoSuchElementException("No enchantment is registered under " + key.key().asString());
        }
        return definition;
    }

    @Override
    public Optional<Enchantment> find(final TypedKey<Enchantment> key) {
        return Optional.ofNullable(definition(key.key()));
    }

    @Override
    public TypedKey<Enchantment> key(final Enchantment value) {
        return TypedKey.create(RegistryKey.ENCHANTMENT, value.key());
    }

    @Override
    public Collection<Enchantment> values() {
        return Collections.unmodifiableCollection(snapshot.definitions);
    }

    @Override
    public Stream<Enchantment> stream() {
        return snapshot.definitions.stream().map(Enchantment.class::cast);
    }

    private void publish(final Snapshot next, final String change) {
        this.snapshot = next;
        rebuildBehaviours();
        if (started.get()) {
            LOGGER.warn("{} after startup: clients will be sent to the configuration phase to see the change.", change);
            FidorialServer.getInstance().players().online().forEach(ServerPlayer::enterConfigurationPhase);
        } else {
            LOGGER.debug("{}.", change);
        }
    }

    private static String change(final Key key, final String action) {
        return "Enchantment " + key.asString() + " " + action;
    }

    /**
     * @param definition     the enchantment
     * @param vanillaPayload the vanilla data of a stock enchantment, {@code null} otherwise
     * @param owner          the plugin that registered or overwrote it, {@code null} for a stock enchantment
     */
    private record Entry(EnchantmentDefinitionImpl definition, @Nullable BinaryTag vanillaPayload, @Nullable Object owner) {

        boolean isCustom() {
            return owner != null;
        }
    }

    private record Attachment(EnchantmentBehaviour behaviour, Object owner) {
    }

    private static final class Snapshot {

        private final Map<Key, Entry> entries;
        private final List<Key> order;
        private final Map<Key, Integer> ids;
        private final Map<Key, List<Key>> tags;
        private final List<EnchantmentDefinition> definitions;
        private final List<TypedKey<Enchantment>> typedKeys;

        private Snapshot(final Map<Key, Entry> entries, final Map<Key, List<Key>> tags) {
            this.entries = Collections.unmodifiableMap(new LinkedHashMap<>(entries));
            this.order = List.copyOf(entries.keySet());
            final Map<Key, Integer> ids = new HashMap<>(order.size());
            final List<EnchantmentDefinition> definitions = new ArrayList<>(order.size());
            final List<TypedKey<Enchantment>> typedKeys = new ArrayList<>(order.size());
            for (int id = 0; id < order.size(); id++) {
                final Key key = order.get(id);
                ids.put(key, id);
                definitions.add(entries.get(key).definition());
                typedKeys.add(TypedKey.create(RegistryKey.ENCHANTMENT, key));
            }
            this.ids = Map.copyOf(ids);
            this.definitions = List.copyOf(definitions);
            this.typedKeys = List.copyOf(typedKeys);
            final Map<Key, List<Key>> copied = new LinkedHashMap<>();
            tags.forEach((tag, members) -> copied.put(tag, List.copyOf(members.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList())));
            this.tags = Collections.unmodifiableMap(copied);
        }

        static Snapshot of(final Map<Key, Entry> entries, final Map<Key, List<Key>> tags) {
            return new Snapshot(entries, tags);
        }
    }
}
