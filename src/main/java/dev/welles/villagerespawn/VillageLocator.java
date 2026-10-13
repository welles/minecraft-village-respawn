package dev.welles.villagerespawn;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Finds the village closest to a player's place of death and a safe spot to respawn in it.
 */
public final class VillageLocator {
    /** Chunks around the village center that are loaded so the bell and terrain are available. */
    private static final int LOAD_RADIUS_CHUNKS = 2;
    private static final int BELL_SEARCH_RADIUS = LOAD_RADIUS_CHUNKS * 16;
    private static final int SPOT_SEARCH_RADIUS = 8;

    /** @param bell the village bell, or {@code null} if none was found */
    public record Spot(ServerLevel level, Vec3 position, float yaw, @Nullable BlockPos bell) {}

    private VillageLocator() {}

    @Nullable
    public static Spot find(ServerPlayer player, RespawnConfig config) {
        MinecraftServer server = player.level().getServer();
        ServerLevel overworld = server.overworld();

        BlockPos origin = searchOrigin(player, server, overworld, config);
        if (origin == null) return null;

        BlockPos village = overworld.findNearestMapStructure(StructureTags.VILLAGE, origin, config.searchRadiusChunks, false);
        if (village == null) {
            VillageRespawn.LOGGER.debug("No village found within {} chunks of {}", config.searchRadiusChunks, origin);
            return null;
        }

        int chunkX = village.getX() >> 4;
        int chunkZ = village.getZ() >> 4;
        for (int x = -LOAD_RADIUS_CHUNKS; x <= LOAD_RADIUS_CHUNKS; x++) {
            for (int z = -LOAD_RADIUS_CHUNKS; z <= LOAD_RADIUS_CHUNKS; z++) {
                overworld.getChunk(chunkX + x, chunkZ + z);
            }
        }

        PoiManager poi = overworld.getPoiManager();
        poi.ensureLoadedAndValid(overworld, village, BELL_SEARCH_RADIUS);
        BlockPos bell = poi.findClosest(type -> type.is(PoiTypes.MEETING), village, BELL_SEARCH_RADIUS, PoiManager.Occupancy.ANY)
            .orElse(null);
        BlockPos center = config.preferBell && bell != null ? bell : village;

        BlockPos spot = findSafeSpot(overworld, center);
        if (spot == null) return null;

        Vec3 position = Vec3.atBottomCenterOf(spot);
        // Look towards the bell / village center
        double dx = center.getX() + 0.5 - position.x;
        double dz = center.getZ() + 0.5 - position.z;
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        return new Spot(overworld, position, yaw, bell);
    }

    /** The Overworld position to search from, or {@code null} if the dimension of death is disabled. */
    @Nullable
    private static BlockPos searchOrigin(ServerPlayer player, MinecraftServer server, ServerLevel overworld, RespawnConfig config) {
        GlobalPos death = player.getLastDeathLocation()
            .orElseGet(() -> GlobalPos.of(player.level().dimension(), player.blockPosition()));
        ResourceKey<Level> dimension = death.dimension();
        BlockPos pos = death.pos();

        if (dimension == Level.OVERWORLD) return pos;
        if (dimension == Level.END) return config.fromEnd ? worldSpawn(overworld) : null;
        if (dimension == Level.NETHER ? !config.fromNether : !config.fromOtherDimensions) return null;

        ServerLevel level = server.getLevel(dimension);
        if (level == null) return worldSpawn(overworld);
        double scale = level.dimensionType().coordinateScale() / overworld.dimensionType().coordinateScale();
        return BlockPos.containing(pos.getX() * scale, pos.getY(), pos.getZ() * scale);
    }

    private static BlockPos worldSpawn(ServerLevel overworld) {
        //? if >=1.21.9 {
        /*return overworld.getRespawnData().pos();
        *///?} else
        return overworld.getSharedSpawnPos();
    }

    /** Searches outwards from {@code center} for a dry spot with solid ground and room for a player. */
    @Nullable
    private static BlockPos findSafeSpot(ServerLevel level, BlockPos center) {
        for (int radius = 1; radius <= SPOT_SEARCH_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    int x = center.getX() + dx;
                    int z = center.getZ() + dz;
                    BlockPos feet = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
                    if (isSafe(level, feet)) return feet;
                }
            }
        }
        return null;
    }

    private static boolean isSafe(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        BlockState ground = level.getBlockState(below);
        if (!ground.getFluidState().isEmpty() || !ground.isFaceSturdy(level, below, Direction.UP)) return false;
        if (ground.is(Blocks.MAGMA_BLOCK) || ground.is(BlockTags.CAMPFIRES) || ground.is(Blocks.BELL)) return false;
        return isFree(level, feet) && isFree(level, feet.above());
    }

    private static boolean isFree(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty() && state.getFluidState().isEmpty()
            && !state.is(BlockTags.FIRE) && !state.is(Blocks.SWEET_BERRY_BUSH) && !state.is(Blocks.POWDER_SNOW);
    }
}
