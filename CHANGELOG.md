# Changelog

All notable changes to this mod are documented in this file. The format is based on
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the mod follows [Semantic Versioning](https://semver.org/).

## [1.2.0] - 2026-11-03

### Added

- A pixel art icon, shown in Mod Menu, in NeoForge's mod list and at the top of the README.

## [1.1.0] - 2026-11-02

### Added

- The config screen checks every value while typing: numbers outside their range or an unknown experience mode are
  marked red, and Done stays disabled until every value is valid, listing the invalid ones in its tooltip.
- Every value in the config screen can be reset to its default with the ↺ button next to it, or all at once with
  Reset all. Nothing is saved until Done.

## [1.0.0] - 2026-10-26

First release, for Fabric and NeoForge on Minecraft 1.21.1 – 26.3.

### Added

- Players keep their items on death and lose all experience (configurable).
- Players respawn next to the bell of the village closest to where they died, instead of at their bed or the world
  spawn. Deaths in the Nether use the closest Overworld village, deaths in the End the one closest to the world spawn.
  Villages outside the world border are ignored.
- New players start in the village closest to the world spawn. They are placed there before they join, so the
  loading screen covers loading the village.
- A chat message tells players that they woke up in a village.
- `config/villagerespawn.properties`, reloaded automatically and editable in game through NeoForge's mod list or
  Mod Menu.

[1.2.0]: https://github.com/welles/minecraft-village-respawn/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/welles/minecraft-village-respawn/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/welles/minecraft-village-respawn/releases/tag/v1.0.0
