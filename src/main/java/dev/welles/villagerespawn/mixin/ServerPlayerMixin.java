package dev.welles.villagerespawn.mixin;

import dev.welles.villagerespawn.RespawnBell;
import dev.welles.villagerespawn.RespawnConfig;
import dev.welles.villagerespawn.VillageLocator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    /** Applies the configured inventory and experience rules to the respawned player. */
    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void villagerespawn$restoreFrom(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        if (alive) return;
        ServerPlayer self = (ServerPlayer) (Object) this;
        RespawnConfig config = RespawnConfig.get();

        if (config.keepsInventory()) self.getInventory().replaceWith(oldPlayer.getInventory());

        switch (config.experienceMode()) {
            case LOSE -> {
                self.experienceLevel = 0;
                self.totalExperience = 0;
                self.experienceProgress = 0.0F;
            }
            case KEEP -> {
                self.experienceLevel = oldPlayer.experienceLevel;
                self.totalExperience = oldPlayer.totalExperience;
                self.experienceProgress = oldPlayer.experienceProgress;
            }
            case VANILLA -> {}
        }
    }

    /** Respawns players that died in the village closest to their place of death. */
    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At("HEAD"), cancellable = true)
    private void villagerespawn$respawnInVillage(boolean flag, DimensionTransition.PostDimensionTransition post,
                                                 CallbackInfoReturnable<DimensionTransition> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        // Also called when leaving the End through the exit portal; only handle actual deaths
        if (!self.isDeadOrDying()) return;

        RespawnConfig config = RespawnConfig.get();
        if (!config.respawnsInVillages()) return;
        if (!config.overrideSpawnPoint && villagerespawn$hasSpawnPoint(self)) return;

        DimensionTransition village = villagerespawn$villageTransition(self, config, post);
        if (village != null) cir.setReturnValue(village);
    }

    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At("RETURN"), cancellable = true)
    private void villagerespawn$respawnInVillageIfSpawnPointMissing(boolean flag, DimensionTransition.PostDimensionTransition post,
                                                                   CallbackInfoReturnable<DimensionTransition> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        RespawnConfig config = RespawnConfig.get();
        // With overrideSpawnPoint=false, a bed/anchor that was destroyed falls back to the village as well
        if (!self.isDeadOrDying() || !config.respawnsInVillages() || config.overrideSpawnPoint) return;
        if (!cir.getReturnValue().missingRespawnBlock()) return;

        DimensionTransition village = villagerespawn$villageTransition(self, config, post);
        if (village != null) cir.setReturnValue(village);
    }

    @Unique
    private static DimensionTransition villagerespawn$villageTransition(ServerPlayer player, RespawnConfig config,
                                                                        DimensionTransition.PostDimensionTransition post) {
        VillageLocator.Spot spot = VillageLocator.find(player, config);
        if (spot == null) return null;

        if (config.showMessage && !config.message.isBlank()) player.sendSystemMessage(Component.literal(config.message));
        if (config.ringBell) {
            RespawnBell.schedule(spot.level(), spot.bell() != null ? spot.bell() : BlockPos.containing(spot.position()), config.bellEcho);
        }
        return new DimensionTransition(spot.level(), spot.position(), Vec3.ZERO, spot.yaw(), 0.0F, post);
    }

    @Unique
    private static boolean villagerespawn$hasSpawnPoint(ServerPlayer player) {
        //? if >=1.21.5 {
        /*return player.getRespawnConfig() != null;
        *///?} else
        return player.getRespawnPosition() != null;
    }
}
