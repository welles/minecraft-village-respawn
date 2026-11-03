> ⚠️ **This project is AI-generated.** The code, this description and the icon of this mod were created with generative AI (Claude by Anthropic), directed by the author. Please keep that in mind, and report any problems on GitHub.

# Village Respawn

Dying sends you to the nearest village: you keep your items, lose your experience, and wake up next to the village bell.

## Features

- **Items are kept** – nothing drops on death.
- **Experience is lost** – level and progress are reset to 0, no experience orbs drop.
- **Respawn in the nearest village** – instead of your bed or the world spawn, you wake up next to the bell of the village closest to where you died. Deaths in the Nether use the closest Overworld village, deaths in the End the one closest to the world spawn. If no village is found, the normal respawn applies.
- **Start in a village** – new players start in the village closest to the world spawn.

Every feature can be switched back to vanilla individually.

## Installation

All logic runs on the server, so the mod works in **singleplayer** and on **dedicated servers** without being installed on the clients: players with an unmodded game can join.

## Configuration

`config/villagerespawn.properties` is created on first start and reloaded automatically when it changes. It can also be edited in game: on NeoForge in *Mods → Village Respawn → Config*, on Fabric through [Mod Menu](https://www.curseforge.com/minecraft/mc-mods/modmenu). Invalid values are marked red and cannot be saved, and every value can be reset to its default.

Keeping items, losing experience (lose, keep or vanilla), respawning in villages, overriding beds, starting in a village, the search radius, the respawn from other dimensions and the chat messages can be configured.

## Versions

Available for **Fabric** and **NeoForge** on Minecraft **1.21 – 26.3**. Download the file that matches your loader and Minecraft version.

## Links

- [Source code](https://github.com/welles/minecraft-village-respawn)
- [Report a problem](https://github.com/welles/minecraft-village-respawn/issues)
- [Changelog](https://github.com/welles/minecraft-village-respawn/blob/main/CHANGELOG.md)
