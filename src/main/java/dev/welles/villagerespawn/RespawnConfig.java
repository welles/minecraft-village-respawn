package dev.welles.villagerespawn;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Settings stored in {@code config/villagerespawn.properties}.
 * The file is re-read automatically whenever it changes on disk.
 */
public final class RespawnConfig {
    public enum ExperienceMode {
        /** Vanilla behaviour: a part of the experience is dropped as orbs. */
        VANILLA,
        /** All experience is gone and no orbs are dropped. */
        LOSE,
        /** Experience is kept completely. */
        KEEP
    }

    public boolean enabled = true;
    public boolean keepInventory = true;
    public boolean destroyCurseOfVanishing = true;
    public ExperienceMode experienceOnDeath = ExperienceMode.LOSE;

    public boolean villageRespawn = true;
    public boolean overrideSpawnPoint = true;
    public int searchRadiusChunks = 100;
    public boolean fromNether = true;
    public boolean fromEnd = true;
    public boolean fromOtherDimensions = true;
    public boolean preferBell = true;

    public boolean showMessage = true;
    public String message = "You wake up in the nearest village.";

    private static final String HEADER = """
        Village Respawn configuration
        Changes are picked up automatically, no restart required.

        Every feature can be switched back to vanilla behaviour individually.

        enabled                  Master switch. false = the mod does nothing, everything behaves like vanilla.
        keepInventory            true  = keep all items on death (independent of the keepInventory gamerule)
                                 false = vanilla (items drop unless the keepInventory gamerule is set)
        destroyCurseOfVanishing  Items with Curse of Vanishing still disappear when keepInventory is true.
        experienceOnDeath        LOSE    = all experience is gone, no orbs are dropped
                                 KEEP    = experience is kept completely
                                 VANILLA = vanilla behaviour (part of it is dropped as orbs)
        villageRespawn           true  = respawn in the village closest to the place of death
                                 false = vanilla respawn (bed, respawn anchor or world spawn)
        overrideSpawnPoint       true  = always respawn in the village, even if a bed/respawn anchor is set
                                 false = only use the village if there is no valid bed/respawn anchor
        searchRadiusChunks       Search radius around the place of death, in chunks (like /locate).
        fromNether               Deaths in the Nether use the closest Overworld village (coordinates x8).
        fromEnd                  Deaths in the End use the village closest to the world spawn.
        fromOtherDimensions      Deaths in other (modded) dimensions use the closest Overworld village.
        preferBell               Respawn next to the village bell instead of the village center.
        showMessage              Send a chat message after respawning in a village.
        message                  Text of that chat message.
        If no village is found, the player respawns normally (bed, respawn anchor or world spawn).""";

    /** Whether items are kept, taking the master switch into account. */
    public boolean keepsInventory() {
        return enabled && keepInventory;
    }

    /** The experience behaviour, taking the master switch into account. */
    public ExperienceMode experienceMode() {
        return enabled ? experienceOnDeath : ExperienceMode.VANILLA;
    }

    /** Whether players respawn in villages, taking the master switch into account. */
    public boolean respawnsInVillages() {
        return enabled && villageRespawn;
    }

    private static Path file;
    private static FileTime loadedAt;
    private static RespawnConfig current = new RespawnConfig();

    public static synchronized void init(Path configDir) {
        file = configDir.resolve(VillageRespawn.MOD_ID + ".properties");
        reload();
    }

    /** Returns the current settings, re-reading the file if it was modified. */
    public static synchronized RespawnConfig get() {
        if (file != null) {
            try {
                FileTime modified = Files.exists(file) ? Files.getLastModifiedTime(file) : null;
                if (modified == null || !modified.equals(loadedAt)) reload();
            } catch (IOException e) {
                VillageRespawn.LOGGER.warn("Could not check config file {}", file, e);
            }
        }
        return current;
    }

    private static void reload() {
        RespawnConfig config = new RespawnConfig();
        Properties props = new Properties();
        boolean complete = false;

        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                props.load(reader);
                complete = config.read(props);
            } catch (IOException | RuntimeException e) {
                VillageRespawn.LOGGER.error("Could not read config file {}, using defaults", file, e);
                current = config;
                return;
            }
        }

        if (!complete) {
            try {
                Files.createDirectories(file.getParent());
                try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                    config.write(writer);
                }
            } catch (IOException e) {
                VillageRespawn.LOGGER.error("Could not write config file {}", file, e);
            }
        }

        try {
            loadedAt = Files.exists(file) ? Files.getLastModifiedTime(file) : null;
        } catch (IOException e) {
            loadedAt = null;
        }
        current = config;
    }

    /** Applies all values present in {@code props}; returns whether every key was present. */
    private boolean read(Properties props) {
        enabled = bool(props, "enabled", enabled);
        keepInventory = bool(props, "keepInventory", keepInventory);
        destroyCurseOfVanishing = bool(props, "destroyCurseOfVanishing", destroyCurseOfVanishing);
        experienceOnDeath = experience(props, "experienceOnDeath", experienceOnDeath);
        villageRespawn = bool(props, "villageRespawn", villageRespawn);
        overrideSpawnPoint = bool(props, "overrideSpawnPoint", overrideSpawnPoint);
        searchRadiusChunks = integer(props, "searchRadiusChunks", searchRadiusChunks, 1, 1000);
        fromNether = bool(props, "fromNether", fromNether);
        fromEnd = bool(props, "fromEnd", fromEnd);
        fromOtherDimensions = bool(props, "fromOtherDimensions", fromOtherDimensions);
        preferBell = bool(props, "preferBell", preferBell);
        showMessage = bool(props, "showMessage", showMessage);
        message = props.getProperty("message", message);
        return props.keySet().containsAll(toProperties().keySet());
    }

    private void write(Writer writer) throws IOException {
        for (String line : HEADER.split("\n")) writer.write(("# " + line).stripTrailing() + "\n");
        writer.write("\n");
        for (var entry : toProperties().entrySet()) {
            String value = entry.getValue().replace("\\", "\\\\").replace("\n", "\\n");
            writer.write(entry.getKey() + "=" + value + "\n");
        }
    }

    private Map<String, String> toProperties() {
        Map<String, String> props = new LinkedHashMap<>();
        props.put("enabled", String.valueOf(enabled));
        props.put("keepInventory", String.valueOf(keepInventory));
        props.put("destroyCurseOfVanishing", String.valueOf(destroyCurseOfVanishing));
        props.put("experienceOnDeath", experienceOnDeath.name());
        props.put("villageRespawn", String.valueOf(villageRespawn));
        props.put("overrideSpawnPoint", String.valueOf(overrideSpawnPoint));
        props.put("searchRadiusChunks", String.valueOf(searchRadiusChunks));
        props.put("fromNether", String.valueOf(fromNether));
        props.put("fromEnd", String.valueOf(fromEnd));
        props.put("fromOtherDimensions", String.valueOf(fromOtherDimensions));
        props.put("preferBell", String.valueOf(preferBell));
        props.put("showMessage", String.valueOf(showMessage));
        props.put("message", message);
        return props;
    }

    private static boolean bool(Properties props, String key, boolean fallback) {
        String value = props.getProperty(key);
        return value == null ? fallback : Boolean.parseBoolean(value.trim());
    }

    private static int integer(Properties props, String key, int fallback, int min, int max) {
        String value = props.getProperty(key);
        if (value == null) return fallback;
        try {
            return Math.clamp(Integer.parseInt(value.trim()), min, max);
        } catch (NumberFormatException e) {
            VillageRespawn.LOGGER.warn("Invalid number for {}: {}", key, value);
            return fallback;
        }
    }

    private static ExperienceMode experience(Properties props, String key, ExperienceMode fallback) {
        String value = props.getProperty(key);
        if (value == null) return fallback;
        try {
            return ExperienceMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            VillageRespawn.LOGGER.warn("Invalid value for {}: {} (expected LOSE, KEEP or VANILLA)", key, value);
            return fallback;
        }
    }
}
