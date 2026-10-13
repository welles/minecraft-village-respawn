package dev.welles.villagerespawn;

//? if fabric {
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class VillageRespawnFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        VillageRespawn.init(FabricLoader.getInstance().getConfigDir());
    }
}
//?}
