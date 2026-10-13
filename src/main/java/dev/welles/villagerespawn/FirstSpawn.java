package dev.welles.villagerespawn;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers players that were placed in a village on their first join, between choosing the spawn
 * position and the player actually entering the world.
 */
public final class FirstSpawn {
    private static final Set<UUID> PENDING = ConcurrentHashMap.newKeySet();

    private FirstSpawn() {}

    public static void mark(UUID player) {
        PENDING.add(player);
    }

    /** Returns whether {@code player} was placed in a village, and forgets about it. */
    public static boolean consume(UUID player) {
        return PENDING.remove(player);
    }
}
