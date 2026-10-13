package dev.welles.villagerespawn.mixin;

import dev.welles.villagerespawn.RespawnConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Shadow
    protected abstract void destroyVanishingCursedItems();

    /** Keeps the inventory on death; it is copied to the respawned player in {@link ServerPlayerMixin}. */
    @Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
    private void villagerespawn$keepInventory(CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayer)) return;
        RespawnConfig config = RespawnConfig.get();
        if (!config.keepsInventory()) return;

        if (config.destroyCurseOfVanishing) destroyVanishingCursedItems();
        ci.cancel();
    }

    /** Prevents experience orbs from dropping unless the vanilla behaviour is configured. */
    @Inject(method = "getBaseExperienceReward", at = @At("HEAD"), cancellable = true)
    private void villagerespawn$noExperienceOrbs(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof ServerPlayer && RespawnConfig.get().experienceMode() != RespawnConfig.ExperienceMode.VANILLA) {
            cir.setReturnValue(0);
        }
    }
}
