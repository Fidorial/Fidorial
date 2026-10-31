package fr.euphyllia.fidorial.server.command.brigadier.packet.registry;

import com.mojang.brigadier.arguments.ArgumentType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;


public final class NetworkArgumentIds {

    private static final Int2ObjectOpenHashMap<ArgumentTypeRegistrar<?, ?>> BY_ID = new Int2ObjectOpenHashMap<>();
    private static final Object2IntOpenHashMap<ArgumentTypeRegistrar<?, ?>> IDS = new Object2IntOpenHashMap<>();

    static {
        IDS.defaultReturnValue(-1);
    }

    private NetworkArgumentIds() {
    }

    public static void register(final int id, final ArgumentTypeRegistrar<?, ?> registrar) {
        if (BY_ID.containsKey(id)) {
            throw new IllegalStateException("Duplicate network id: " + id);
        }
        BY_ID.put(id, registrar);
        if (IDS.put(registrar, id) != IDS.defaultReturnValue()) {
            throw new IllegalStateException("Registrar already registered: " + registrar);
        }
    }

    public static int getId(final ArgumentTypeRegistrar<?, ?> registrar) {
        final int id = IDS.getInt(registrar);
        if (id == -1) {
            throw new IllegalArgumentException("Unknown registrar: " + registrar.getClass().getName());
        }
        return id;
    }

    public static ArgumentTypeRegistrar<?, ?> byId(final int id) {
        final ArgumentTypeRegistrar<?, ?> registrar = BY_ID.get(id);

        if (registrar == null) {
            throw new IllegalArgumentException("Unknown network argument id: " + id);
        }

        return registrar;
    }

    public static boolean hasId(final ArgumentTypeRegistrar<? extends ArgumentType<?>,?> registrar) {
        return IDS.containsKey(registrar);
    }
}
