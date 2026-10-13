package dev.welles.villagerespawn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class VillageRespawn {
    public static final String MOD_ID = "villagerespawn";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private VillageRespawn() {}

    public static void init(Path configDir) {
        RespawnConfig.init(configDir);
        LOGGER.info("Village Respawn loaded");
    }
}
