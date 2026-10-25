package dev.welles.villagerespawn;

//? if fabric {
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

/** "Config" button in Mod Menu's mod list. Only loaded by Mod Menu, so the mod works without it. */
public class VillageRespawnModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ConfigScreen(
            parent,
            FabricLoader.getInstance().getConfigDir().resolve(VillageRespawn.MOD_ID + ".properties"),
            Component.literal(FabricLoader.getInstance().getModContainer(VillageRespawn.MOD_ID).map(mod -> mod.getMetadata().getName()).orElse(VillageRespawn.MOD_ID))
        );
    }
}
//?}
