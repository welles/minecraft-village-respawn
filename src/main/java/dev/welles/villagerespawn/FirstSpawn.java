package dev.welles.villagerespawn;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers players that were placed in a village on their first join, between choosing the spawn
 * position and the player actually entering the world.
 */
public final class FirstSpawn {
    private static final Map<UUID, VillageLocator.Spot> PENDING = new ConcurrentHashMap<>();

    private FirstSpawn() {}

    public static void mark(UUID player, VillageLocator.Spot spot) {
        PENDING.put(player, spot);
    }

    @Nullable
    public static VillageLocator.Spot consume(UUID player) {
        return PENDING.remove(player);
    }
}
