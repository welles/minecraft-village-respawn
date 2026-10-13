package dev.welles.villagerespawn.mixin;

import dev.welles.villagerespawn.FirstSpawn;
import dev.welles.villagerespawn.RespawnBell;
import dev.welles.villagerespawn.RespawnConfig;
import dev.welles.villagerespawn.VillageLocator;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if <1.21.9 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
//?}
//? if <1.21.6 {
import java.util.Optional;
//?} else if <1.21.9 {
/*import net.minecraft.core.BlockPos;
*///?}

/**
 * Places players joining the world for the first time in the village closest to the world spawn.
 * <p>
 * The position is chosen before the client learns where it spawns, so the join loading screen covers
 * the village search and chunk loading. From 1.21.9 on this happens in {@link PrepareSpawnTaskMixin}.
 */
@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    //? if <1.21.9 {
    @Shadow
    @Final
    private MinecraftServer server;
    //?}

    //? if <1.21.6 {
    /** Players without saved data are new; vanilla already placed them at the world spawn. */
    @ModifyExpressionValue(method = "placeNewPlayer", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/players/PlayerList;load(Lnet/minecraft/server/level/ServerPlayer;)Ljava/util/Optional;"))
    private Optional<?> villagerespawn$placeNewPlayerInVillage(Optional<?> data, @Local(argsOnly = true) ServerPlayer player) {
        if (data.isPresent()) return data;
        VillageLocator.Spot spot = villagerespawn$findFirstSpawn(player);
        if (spot != null) {
            //? if >=1.21.5 {
            /*player.snapTo(spot.position().x, spot.position().y, spot.position().z, spot.yaw(), 0.0F);
            *///?} else
            player.moveTo(spot.position().x, spot.position().y, spot.position().z, spot.yaw(), 0.0F);
        }
        return data;
    }
    //?} else if <1.21.9 {
    /*/^* Only called for players without saved data, to place them at the world spawn. ^/
    @ModifyExpressionValue(method = "placeNewPlayer", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/level/ServerPlayer;adjustSpawnLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"))
    private BlockPos villagerespawn$placeNewPlayerInVillage(BlockPos spawn, @Local(argsOnly = true) ServerPlayer player) {
        VillageLocator.Spot spot = villagerespawn$findFirstSpawn(player);
        return spot != null ? BlockPos.containing(spot.position()) : spawn;
    }
    *///?}

    //? if <1.21.9 {
    @Unique
    private VillageLocator.Spot villagerespawn$findFirstSpawn(ServerPlayer player) {
        RespawnConfig config = RespawnConfig.get();
        if (!config.spawnsInVillages()) return null;
        VillageLocator.Spot spot = VillageLocator.findNearWorldSpawn(server, config);
        if (spot != null) FirstSpawn.mark(player.getUUID(), spot);
        return spot;
    }
    //?}

    /** Greets the player and rings the bell once they are in the world. */
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void villagerespawn$welcomeToVillage(Connection connection, ServerPlayer player, CommonListenerCookie cookie,
                                                CallbackInfo ci) {
        VillageLocator.Spot spot = FirstSpawn.consume(player.getUUID());
        if (spot == null) return;

        RespawnConfig config = RespawnConfig.get();
        if (config.showMessage && !config.firstSpawnMessage.isBlank()) {
            player.sendSystemMessage(Component.literal(config.firstSpawnMessage));
        }
        RespawnBell.ringFor(spot, config, RespawnBell.JOIN_DELAY_TICKS);
    }
}
