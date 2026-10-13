package dev.welles.villagerespawn.mixin;

import dev.welles.villagerespawn.RespawnConfig;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
}
