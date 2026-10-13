# Village Respawn

A Minecraft mod for **Fabric** and **NeoForge** (Minecraft 1.21.1 – 26.3) that changes what happens when a player dies:

- **Items are kept** – nothing drops on death.
- **Experience is lost** – level and progress are reset to 0, no experience orbs drop.
- **Respawn in the nearest village** – instead of the bed or world spawn, players wake up next to the bell of
  the village closest to where they died. Deaths in the Nether use the closest Overworld village (coordinates ×8),
  deaths in the End use the village closest to the world spawn. Villages outside the world border are ignored.
  If no village is found, the normal respawn applies.
- **Start in a village** – players joining the world for the first time start in the village closest to the world
  spawn instead of the world spawn itself.
- **The village bell rings** with a fading echo when you wake up.

Every feature can be configured or switched back to vanilla behaviour individually.

## Server-side only

All logic runs on the server, so the mod works

- **in singleplayer** when installed on the client (the integrated server runs it), and
- **on dedicated servers** without being installed on the clients – vanilla clients can join.

## Configuration

`config/villagerespawn.properties` is created on first start. Changes are applied automatically while the game is
running.

| Key                       | Default | Description                                                                                         |
|---------------------------|---------|-----------------------------------------------------------------------------------------------------|
| `enabled`                 | `true`  | Master switch. `false` = the mod does nothing, everything behaves like vanilla.                     |
| `keepInventory`           | `true`  | Keep all items on death. `false` = vanilla (respects the `keepInventory` gamerule).                 |
| `destroyCurseOfVanishing` | `true`  | Items with Curse of Vanishing still disappear when `keepInventory` is `true`.                       |
| `experienceOnDeath`       | `LOSE`  | `LOSE` = all experience is gone, `KEEP` = experience is kept, `VANILLA` = vanilla behaviour.        |
| `villageRespawn`          | `true`  | Respawn in the nearest village. `false` = vanilla respawn.                                          |
| `overrideSpawnPoint`      | `true`  | `true` = ignore beds/respawn anchors. `false` = use the village only without a valid spawn point.   |
| `villageFirstSpawn`       | `true`  | New players start in the village closest to the world spawn. `false` = vanilla world spawn.         |
| `searchRadiusChunks`      | `100`   | Village search radius in chunks (like `/locate`).                                                   |
| `fromNether`              | `true`  | Deaths in the Nether respawn in the Overworld village closest to the corresponding coordinates.     |
| `fromEnd`                 | `true`  | Deaths in the End respawn in the village closest to the world spawn.                                |
| `fromOtherDimensions`     | `true`  | Deaths in other (modded) dimensions respawn in the closest Overworld village.                       |
| `preferBell`              | `true`  | Respawn next to the village bell instead of the village center.                                     |
| `ringBell`                | `true`  | Ring the village bell after respawning in a village.                                                |
| `bellEcho`                | `true`  | Let the bell ring echo (fading repetitions).                                                        |
| `showMessage`             | `true`  | Send a chat message after (re)spawning in a village.                                                |
| `message`                 | …       | Text of that message after respawning.                                                              |
| `firstSpawnMessage`       | …       | Text of that message on the first spawn.                                                            |

## Building

The project uses [Stonecutter](https://stonecutter.kikugie.dev/) to build every Minecraft version and loader from one
source tree. Each node in `settings.gradle.kts` targets one Minecraft version and declares the compatible range in
`stonecutter.properties.toml`.

```sh
./gradlew build                          # build all versions and loaders
./gradlew buildAndCollect                # copy all jars to build/libs/<mod version>/
./gradlew :1.21.1-fabric:runClient       # run a specific version (Fabric)
./gradlew :26.3-neoforge:runClient       # run a specific version (NeoForge)
```

Version-specific code uses Stonecutter comments (`//? if >=1.21.9 {`). The source tree is checked in for the
`1.21.1-fabric` node; switch the active version with the `Set active project to …` Gradle tasks before editing code
for another version, and switch back to `1.21.1-fabric` (`Reset active project`) before committing.

## License

[MIT](LICENSE)
