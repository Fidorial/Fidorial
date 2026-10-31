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
import fr.fidorial.registry.TypedKey;
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
    private final ServerRegistry<?> items;
    private final Map<Key, List<Attachment>> attachments = new ConcurrentHashMap<>();

    private volatile Map<Key, List<EnchantmentBehaviour>> behaviours = Map.of();

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
        for (final Key key : registry.keyList()) {
            final EnchantmentDefinitionImpl definition;
            try {
                definition = (EnchantmentDefinitionImpl) registry.value(key);
            } catch (final IllegalStateException exception) {
                LOGGER.error("Could not read the vanilla enchantment {}, it has no effect.", key.asString(), exception);
                continue;
            }
            if (definition == null) {
                continue;
            }
            for (final String skipped : definition.effects().skipped()) {
                LOGGER.debug("Enchantment {}: {} is not run by the server", key.asString(), skipped);
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
        Preconditions.checkArgument(behaviour != null, "The behaviour attached to %s must not be null", key.key());
        Preconditions.checkArgument(owner != null, "The owner of the behaviour attached to %s must not be null", key.key());
        synchronized (this) {
            Preconditions.checkArgument(registry.contains(key.key()), "Unknown enchantment %s", key.key().asString());
            attachments.computeIfAbsent(key.key(), _ -> new CopyOnWriteArrayList<>()).add(new Attachment(behaviour, owner));
            rebuildBehaviours();
        }
        LOGGER.debug("Behaviour attached to the enchantment {} by {}", key.key().asString(), owner.getClass().getSimpleName());
    }

    @Override
    public boolean detach(final TypedKey<EnchantmentDefinition> key, final Plugin owner) {
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

    private synchronized void rebuildBehaviours() {
        final Map<Key, List<EnchantmentBehaviour>> next = new HashMap<>();
        for (final Key key : registry.keyList()) {
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
    public @Nullable EnchantmentDefinitionImpl definition(final Key key) {
        try {
            return registry.value(key) instanceof final EnchantmentDefinitionImpl impl ? impl : null;
        } catch (final IllegalStateException unreadable) {
            LOGGER.debug("The enchantment {} cannot be read", key.asString(), unreadable);
            return null;
        }
    }

    public boolean isTagged(final Key enchantment, final Key tag) {
        return registry.isTagged(enchantment, tag);
    }

    @Override
    public Set<Key> supportedEffectComponents() {
        return EnchantmentEffectSet.SUPPORTED;
    }

    @Override
    public boolean isSupportedItem(final TypedKey<EnchantmentDefinition> enchantment, final Key item) {
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
        final FidorialItemRegistry itemRegistry = FidorialServer.getInstance().registries().items();
        if (!itemRegistry.isCustom(item)) {
            return false;
        }
        final Key rendered = itemRegistry.networkType(item);
        return !rendered.equals(item) && containsItem(set, rendered);
    }

    private boolean containsItem(final KeySet set, final Key item) {
        final Optional<Key> tag = set.tag();
        if (tag.isPresent()) {
            return items.isTagged(item, tag.get());
        }
        return set.keys().contains(item);
    }

    @Override
    public boolean areCompatible(final TypedKey<EnchantmentDefinition> first, final TypedKey<EnchantmentDefinition> second) {
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
    public boolean canEnchant(final ItemStack stack, final TypedKey<EnchantmentDefinition> enchantment) {
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
        return stack.id().equals(ItemKeys.ENCHANTED_BOOK.key()) ? DataComponentTypes.STORED_ENCHANTMENTS : DataComponentTypes.ENCHANTMENTS;
    }

    @Override
    public int level(final ItemStack stack, final TypedKey<EnchantmentDefinition> enchantment) {
        if (stack.isEmpty()) {
            return 0;
        }
        return stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.EMPTY).level(enchantment.key());
    }

    @Override
    public int level(final LivingEntity entity, final TypedKey<EnchantmentDefinition> enchantment) {
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
    public ItemStack enchant(final ItemStack stack, final TypedKey<EnchantmentDefinition> enchantment, final int level) {
        Preconditions.checkArgument(stack != null, "The stack to enchant must not be null");
        Preconditions.checkArgument(enchantment != null && registry.contains(enchantment.key()), "Unknown enchantment %s", enchantment);
        Preconditions.checkArgument(level >= 1 && level <= EnchantmentDefinition.MAX_LEVEL,
                "The level of %s must be between 1 and %s, got %s", enchantment.key(), EnchantmentDefinition.MAX_LEVEL, level);
        if (stack.isEmpty()) {
            return stack;
        }
        final DataComponentType<ItemEnchantments> type = componentFor(stack);
        return stack.with(type, stack.getOrDefault(type, ItemEnchantments.EMPTY).with(enchantment.key(), level));
    }

    @Override
    public ItemStack disenchant(final ItemStack stack, final TypedKey<EnchantmentDefinition> enchantment) {
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
    public ItemStack enchantedBook(final TypedKey<EnchantmentDefinition> enchantment, final int level) {
        return enchant(ItemStack.of(ItemKeys.ENCHANTED_BOOK.key()), enchantment, level);
    }

    @Override
    public Component displayName(final TypedKey<EnchantmentDefinition> enchantment, final int level) {
        final EnchantmentDefinitionImpl definition = definition(enchantment.key());
        final Component description = definition == null
                ? Component.translatable(EnchantmentDefinitionImpl.descriptionId(enchantment.key()))
                : definition.description();
        final NamedTextColor color = isTagged(enchantment.key(), EnchantmentTagKeys.CURSE.key()) ? NamedTextColor.RED : NamedTextColor.GRAY;
        Component name = Component.empty().color(color).append(description);
        if (level != 1 || definition == null || definition.maxLevel() != 1) {
            name = name.append(Component.space()).append(Component.translatable("enchantment.level." + level));
        }
        return name;
    }

    private record Attachment(EnchantmentBehaviour behaviour, Object owner) {
    }
}
