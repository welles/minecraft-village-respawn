package dev.welles.villagerespawn.mixin;

import dev.welles.villagerespawn.RespawnBell;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "tickServer", at = @At("TAIL"))
    private void villagerespawn$tickBell(CallbackInfo ci) {
        RespawnBell.tick((MinecraftServer) (Object) this);
    }
}
