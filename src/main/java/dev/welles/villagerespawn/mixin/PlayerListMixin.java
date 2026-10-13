package dev.welles.villagerespawn.mixin;

import dev.welles.villagerespawn.RespawnBell;
import dev.welles.villagerespawn.RespawnConfig;
import dev.welles.villagerespawn.VillageLocator;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    /** Moves players joining the world for the first time to the village closest to the world spawn. */
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void villagerespawn$firstSpawnInVillage(Connection connection, ServerPlayer player, CommonListenerCookie cookie,
                                                   CallbackInfo ci) {
        RespawnConfig config = RespawnConfig.get();
        if (!config.spawnsInVillages()) return;
        // Players that have played before have a play time; new players start at 0
        if (player.getStats().getValue(Stats.CUSTOM, Stats.PLAY_TIME) > 0) return;
        if (player.level().dimension() != Level.OVERWORLD) return;

        VillageLocator.Spot spot = VillageLocator.findNearWorldSpawn(player.level().getServer(), config);
        if (spot == null) return;

        Vec3 pos = spot.position();
        //? if >=1.21.2 {
        /*player.teleportTo(spot.level(), pos.x, pos.y, pos.z, java.util.Set.of(), spot.yaw(), 0.0F, true);
        *///?} else
        player.teleportTo(spot.level(), pos.x, pos.y, pos.z, spot.yaw(), 0.0F);

        if (config.showMessage && !config.firstSpawnMessage.isBlank()) {
            player.sendSystemMessage(Component.literal(config.firstSpawnMessage));
        }
        RespawnBell.ringFor(spot, config);
    }
}
