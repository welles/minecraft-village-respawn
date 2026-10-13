package dev.welles.villagerespawn;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Rings the village bell after a player respawned in a village. The echo is simulated by quieter,
 * delayed repetitions, so it works with vanilla clients.
 */
public final class RespawnBell {
    /** Delay before the first ring, so the client has finished loading the terrain. */
    private static final int INITIAL_DELAY_TICKS = 20;
    /** Delays (relative to the first ring) and volumes of the echoes. */
    private static final int[] ECHO_DELAYS = {7, 15, 24};
    private static final float[] ECHO_VOLUMES = {0.45F, 0.22F, 0.1F};
    /** Rings scheduled further ahead than this belong to a previous server and are dropped. */
    private static final int MAX_DELAY_TICKS = 100;

    private record Ring(ResourceKey<Level> dimension, BlockPos pos, int tick, float volume, float pitch) {}

    private static final List<Ring> PENDING = new ArrayList<>();

    private RespawnBell() {}

    public static synchronized void schedule(ServerLevel level, BlockPos pos, boolean echo) {
        int start = level.getServer().getTickCount() + INITIAL_DELAY_TICKS;
        PENDING.add(new Ring(level.dimension(), pos, start, 1.0F, 1.0F));
        if (!echo) return;
        for (int i = 0; i < ECHO_DELAYS.length; i++) {
            PENDING.add(new Ring(level.dimension(), pos, start + ECHO_DELAYS[i], ECHO_VOLUMES[i], 1.0F - 0.02F * (i + 1)));
        }
    }

    public static synchronized void tick(MinecraftServer server) {
        if (PENDING.isEmpty()) return;
        int now = server.getTickCount();
        for (Iterator<Ring> it = PENDING.iterator(); it.hasNext(); ) {
            Ring ring = it.next();
            if (ring.tick() > now + MAX_DELAY_TICKS) {
                it.remove();
            } else if (ring.tick() <= now) {
                it.remove();
                ServerLevel level = server.getLevel(ring.dimension());
                if (level != null) {
                    level.playSound((Entity) null, ring.pos(), SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, ring.volume(), ring.pitch());
                }
            }
        }
    }
}
