package fr.euphyllia.fidorial.server.item.enchantment;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.item.FidorialItemRegistry;
import fr.euphyllia.fidorial.server.item.enchantment.effect.EnchantmentEffectSet;
import fr.euphyllia.fidorial.server.registry.ServerRegistries;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import fr.euphyllia.fidorial.server.registry.WritableServerRegistry;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.component.ItemEnchantments;
import fr.fidorial.item.data.DataComponentType;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.item.enchantment.EnchantmentBehaviour;
import fr.fidorial.item.enchantment.EnchantmentDefinition;
import fr.fidorial.item.enchantment.Enchantments;
import fr.fidorial.item.enchantment.KeySet;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.keys.EnchantmentTagKeys;
import fr.fidorial.registry.keys.ItemKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server implementation of {@link Enchantments}: the behaviours of the enchantments of the
 * {@code minecraft:enchantment} registry, rebuilt whenever it changes.
 */
public final class FidorialEnchantments implements Enchantments {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(FidorialEnchantments.class);

    private final WritableServerRegistry<EnchantmentDefinition> registry;
    private final ServerRegistry<Item> items;
    private final Map<TypedKey<EnchantmentDefinition>, List<Attachment>> attachments = new ConcurrentHashMap<>();

    private volatile Map<TypedKey<EnchantmentDefinition>, List<EnchantmentBehaviour>> behaviours = Map.of();

    public FidorialEnchantments(final ServerRegistries registries) {
        this.registry = registries.writable(RegistryKey.ENCHANTMENT);
        this.items = registries.registry(RegistryKey.ITEM);
        registries.addListener(changed -> {
            if (changed == registry) {
                rebuildBehaviours();
            }
        });
        logVanilla();
        rebuildBehaviours();
    }

    private void logVanilla() {
        int loaded = 0;
        int skippedEffects = 0;
        for (final TypedKey<EnchantmentDefinition> key : registry.keys()) {
            final EnchantmentDefinitionImpl definition;
            try {
                definition = (EnchantmentDefinitionImpl) registry.get(key);
            } catch (final IllegalStateException exception) {
                LOGGER.error("Could not read the vanilla enchantment {}, it has no effect.", key, exception);
                continue;
            }
            for (final String skipped : definition.effects().skipped()) {
                LOGGER.debug("Enchantment {}: {} is not run by the server", key, skipped);
            }
            skippedEffects += definition.effects().skipped().size();
            loaded++;
        }
        LOGGER.info("Loaded {} vanilla enchantments, {} of their effects need gameplay the server does not have yet",
                loaded, skippedEffects);
    }

    public WritableServerRegistry<EnchantmentDefinition> registry() {
        return registry;
    }

    @Override
    public EnchantmentDefinition.Builder define(final TypedKey<EnchantmentDefinition> key) {
        return new EnchantmentDefinitionImpl.BuilderImpl(key);
    }

    @Override
    public void attach(final TypedKey<EnchantmentDefinition> key, final EnchantmentBehaviour behaviour, final Plugin owner) {
        Preconditions.checkArgument(key != null, "The key of an enchantment must not be null");
        Preconditions.checkArgument(behaviour != null, "The behaviour attached to %s must not be null", key);
        Preconditions.checkArgument(owner != null, "The owner of the behaviour attached to %s must not be null", key);
        synchronized (this) {
            Preconditions.checkArgument(registry.contains(key), "Unknown enchantment %s", key);
            attachments.computeIfAbsent(key, _ -> new CopyOnWriteArrayList<>()).add(new Attachment(behaviour, owner));
            rebuildBehaviours();
        }
        LOGGER.debug("Behaviour attached to the enchantment {} by {}", key, owner.getClass().getSimpleName());
    }

    @Override
    public boolean detach(final TypedKey<EnchantmentDefinition> key, final Plugin owner) {
        synchronized (this) {
            final List<Attachment> list = attachments.get(key);
            final boolean removed = list != null && list.removeIf(attachment -> attachment.owner() == owner);
            if (removed) {
                rebuildBehaviours();
            }
            return removed;
        }
    }

    /**
     * Removes every behaviour a plugin attached.
     */
    public void detachAll(final Plugin owner) {
        synchronized (this) {
            boolean removed = false;
            for (final List<Attachment> list : attachments.values()) {
                removed |= list.removeIf(attachment -> attachment.owner() == owner);
            }
            if (removed) {
                rebuildBehaviours();
            }
        }
    }

    /**
     * {@return the behaviours of an enchantment, in the order they run: its vanilla effects, the behaviour
     * of its definition, then the attached ones; empty for an unknown enchantment}
     */
    public List<EnchantmentBehaviour> behaviours(final TypedKey<EnchantmentDefinition> enchantment) {
        return behaviours.getOrDefault(enchantment, List.of());
    }

    /**
     * {@return an object that changes whenever the enchantments or their behaviours do, for those caching
     * what an equipped stack does}
     */
    public Object version() {
        return behaviours;
    }

    private synchronized void rebuildBehaviours() {
        final Map<TypedKey<EnchantmentDefinition>, List<EnchantmentBehaviour>> next = new HashMap<>();
        for (final TypedKey<EnchantmentDefinition> key : registry.keys()) {
            final EnchantmentDefinitionImpl definition = definition(key);
            if (definition == null) {
                continue;
            }
            final List<EnchantmentBehaviour> list = new ArrayList<>(2);
            if (definition.effects() != EnchantmentEffectSet.EMPTY) {
                list.add(definition.effects());
            }
            definition.behaviour().ifPresent(list::add);
            for (final Attachment attachment : attachments.getOrDefault(key, List.of())) {
                list.add(attachment.behaviour());
            }
            if (!list.isEmpty()) {
                next.put(key, List.copyOf(list));
            }
        }
        this.behaviours = Map.copyOf(next);
    }

    /**
     * {@return the definition registered under {@code key}, {@code null} for an unknown or unreadable enchantment}
     */
    public @Nullable EnchantmentDefinitionImpl definition(final TypedKey<EnchantmentDefinition> key) {
        try {
            return registry.find(key).orElse(null) instanceof final EnchantmentDefinitionImpl impl ? impl : null;
        } catch (final IllegalStateException unreadable) {
            LOGGER.debug("The enchantment {} cannot be read", key, unreadable);
            return null;
        }
    }

    @Override
    public Set<Key> supportedEffectComponents() {
        return EnchantmentEffectSet.SUPPORTED;
    }

    @Override
    public boolean isSupportedItem(final TypedKey<EnchantmentDefinition> enchantment, final TypedKey<Item> item) {
        final EnchantmentDefinitionImpl definition = definition(enchantment);
        return definition != null && matchesItem(definition.supportedItems(), item);
    }

    /**
     * {@return {@code true} if {@code item}, or the vanilla item a plugin item is rendered as, belongs to {@code set}}
     */
    public boolean matchesItem(final KeySet<Item> set, final TypedKey<Item> item) {
        if (contains(items, set, item)) {
            return true;
        }
        final FidorialItemRegistry itemRegistry = FidorialServer.getInstance().registries().items();
        if (!itemRegistry.isCustom(item)) {
            return false;
        }
        final TypedKey<Item> rendered = itemRegistry.networkType(item);
        return !rendered.equals(item) && contains(items, set, rendered);
    }

    private static <T> boolean contains(final ServerRegistry<T> registry, final KeySet<T> set, final TypedKey<T> key) {
        final Optional<TagKey<T>> tag = set.tag();
        return tag.isPresent() ? registry.isTagged(key, tag.get()) : set.keys().contains(key);
    }

    @Override
    public boolean areCompatible(final TypedKey<EnchantmentDefinition> first, final TypedKey<EnchantmentDefinition> second) {
        if (first.equals(second)) {
            return false;
        }
        final EnchantmentDefinitionImpl a = definition(first);
        final EnchantmentDefinitionImpl b = definition(second);
        return (a == null || !contains(registry, a.exclusiveSet(), second))
                && (b == null || !contains(registry, b.exclusiveSet(), first));
    }

    @Override
    public boolean canEnchant(final ItemStack stack, final TypedKey<EnchantmentDefinition> enchantment) {
        if (stack.isEmpty() || !isSupportedItem(enchantment, stack.id())) {
            return false;
        }
        for (final TypedKey<EnchantmentDefinition> existing : enchantments(stack).levels().keySet()) {
            if (!areCompatible(existing, enchantment)) {
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
        return stack.id().equals(ItemKeys.ENCHANTED_BOOK) ? DataComponentTypes.STORED_ENCHANTMENTS : DataComponentTypes.ENCHANTMENTS;
    }

    @Override
    public int level(final ItemStack stack, final TypedKey<EnchantmentDefinition> enchantment) {
        if (stack.isEmpty()) {
            return 0;
        }
        return stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.EMPTY).level(enchantment);
    }

    @Override
    public int level(final LivingEntity entity, final TypedKey<EnchantmentDefinition> enchantment) {
        final EnchantmentDefinitionImpl definition = definition(enchantment);
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
    public ItemStack enchant(final ItemStack stack, final TypedKey<EnchantmentDefinition> enchantment, final int level) {
        Preconditions.checkArgument(stack != null, "The stack to enchant must not be null");
        Preconditions.checkArgument(enchantment != null && registry.contains(enchantment), "Unknown enchantment %s", enchantment);
        Preconditions.checkArgument(level >= 1 && level <= EnchantmentDefinition.MAX_LEVEL,
                "The level of %s must be between 1 and %s, got %s", enchantment, EnchantmentDefinition.MAX_LEVEL, level);
        if (stack.isEmpty()) {
            return stack;
        }
        final DataComponentType<ItemEnchantments> type = componentFor(stack);
        return stack.with(type, stack.getOrDefault(type, ItemEnchantments.EMPTY).with(enchantment, level));
    }

    @Override
    public ItemStack disenchant(final ItemStack stack, final TypedKey<EnchantmentDefinition> enchantment) {
        Preconditions.checkArgument(stack != null, "The stack must not be null");
        Preconditions.checkArgument(enchantment != null, "The enchantment must not be null");
        final DataComponentType<ItemEnchantments> type = componentFor(stack);
        final ItemEnchantments current = stack.get(type);
        if (current == null || !current.has(enchantment)) {
            return stack;
        }
        return stack.with(type, current.without(enchantment));
    }

    @Override
    public ItemStack enchantedBook(final TypedKey<EnchantmentDefinition> enchantment, final int level) {
        return enchant(ItemStack.of(ItemKeys.ENCHANTED_BOOK), enchantment, level);
    }

    @Override
    public Component displayName(final TypedKey<EnchantmentDefinition> enchantment, final int level) {
        final EnchantmentDefinitionImpl definition = definition(enchantment);
        final Component description = definition == null
                ? Component.translatable(EnchantmentDefinitionImpl.descriptionId(enchantment))
                : definition.description();
        final NamedTextColor color = registry.isTagged(enchantment, EnchantmentTagKeys.CURSE) ? NamedTextColor.RED : NamedTextColor.GRAY;
        Component name = Component.empty().color(color).append(description);
        if (level != 1 || definition == null || definition.maxLevel() != 1) {
            name = name.append(Component.space()).append(Component.translatable("enchantment.level." + level));
        }
        return name;
    }

    private record Attachment(EnchantmentBehaviour behaviour, Object owner) {
    }
}
