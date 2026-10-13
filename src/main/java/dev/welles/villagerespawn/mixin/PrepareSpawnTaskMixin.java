package dev.welles.villagerespawn.mixin;

//? if >=1.21.9 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.welles.villagerespawn.FirstSpawn;
import dev.welles.villagerespawn.RespawnConfig;
import dev.welles.villagerespawn.VillageLocator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.config.PrepareSpawnTask;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/^*
 * Since 1.21.9 the spawn of a joining player is prepared (chunks loaded) during the configuration phase.
 * New players get the village position here, so the chunks around the village are prepared instead.
 ^/
@Mixin(PrepareSpawnTask.class)
public abstract class PrepareSpawnTaskMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    @Final
    private NameAndId nameAndId;

    @Unique
    private VillageLocator.Spot villagerespawn$spot;

    /^* Players without saved data are new and spawn at the world spawn in vanilla. ^/
    @ModifyExpressionValue(method = "start", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/players/PlayerList;loadPlayerData(Lnet/minecraft/server/players/NameAndId;)Ljava/util/Optional;"))
    private Optional<?> villagerespawn$findVillage(Optional<?> data) {
        RespawnConfig config = RespawnConfig.get();
        if (data.isPresent() || !config.spawnsInVillages()) return data;
        if (server.getWorldData().overworldData().getRespawnData().dimension() != Level.OVERWORLD) return data;

        villagerespawn$spot = VillageLocator.findNearWorldSpawn(server, config);
        if (villagerespawn$spot != null) FirstSpawn.mark(nameAndId.id());
        return data;
    }

    @ModifyArg(method = "start", index = 2, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/network/config/PrepareSpawnTask$Preparing;<init>(Lnet/minecraft/server/network/config/PrepareSpawnTask;Lnet/minecraft/server/level/ServerLevel;Ljava/util/concurrent/CompletableFuture;Lnet/minecraft/world/phys/Vec2;)V"))
    private CompletableFuture<Vec3> villagerespawn$spawnInVillage(CompletableFuture<Vec3> spawn) {
        return villagerespawn$spot != null ? CompletableFuture.completedFuture(villagerespawn$spot.position()) : spawn;
    }

    @ModifyArg(method = "start", index = 3, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/network/config/PrepareSpawnTask$Preparing;<init>(Lnet/minecraft/server/network/config/PrepareSpawnTask;Lnet/minecraft/server/level/ServerLevel;Ljava/util/concurrent/CompletableFuture;Lnet/minecraft/world/phys/Vec2;)V"))
    private Vec2 villagerespawn$faceBell(Vec2 rotation) {
        return villagerespawn$spot != null ? new Vec2(villagerespawn$spot.yaw(), 0.0F) : rotation;
    }
}
*///?}
