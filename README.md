# GardensEnchants

[![Build](https://github.com/BrightSteel/GardensEnchants/actions/workflows/build.yml/badge.svg)](https://github.com/BrightSteel/GardensEnchants/actions/workflows/build.yml)

Replaces Minecraft's enchanting with an extensible custom system, used on [GardensMC](https://gardensmc.com).

<!-- TODO: add a screenshot of the enchanting menu or an enchant book here -->

## Features

- **Custom enchantments** that sit alongside vanilla ones, each with a rarity, max level and restrictions on which items it can apply to.
- **Enchanting books and a custom enchanting table** that offer enchants by rarity, with tunable probabilities.
- **Runes and relics** as extra item upgrades.
- **Area-break and other gameplay effects**, driven by game event listeners.
- **Admin and player commands:** `/enchant`, `/disenchant`, `/getbook`, `/givebook`, `/fixbook`, `/lore`, `/mobhead`.
- **Resource pack integration:** custom font glyphs render the enchant menus.

## How it's built

- **Paper 26.1 / Java 25**, built with Gradle, paperweight-userdev and Shadow.
- **MySQL through HikariCP**, with a **Caffeine** cache.
- **Unit tests** for offer rarity, level rolls, area-break targeting and font mappings, run in GitHub Actions.

Set your database details in `src/main/resources/config.yml` before running.

## Part of the Gardens suite

The Gardens suite is a set of Paper plugins that run [GardensMC](https://gardensmc.com), a survival community with 736 registered players. The plugins share APIs and depend on each other:

| Plugin | Role |
|---|---|
| [GardensPlayers](https://github.com/BrightSteel/GardensPlayers) | Core platform: economy, quests, shops, warps, resource worlds, player data |
| [GardensEnchants](https://github.com/BrightSteel/GardensEnchants) | Custom enchanting system that replaces vanilla enchanting |
| [GardensFurniture](https://github.com/BrightSteel/GardensFurniture) | Framework for persistent, interactive custom blocks and furniture |
| [GardensCrops](https://github.com/BrightSteel/GardensCrops) | Farming: planters, sprinklers, watering cans, harvest golems |

```
GardensPlayers ──► GardensEnchants ──► GardensFurniture
       │                                     ▲
       └──────────► GardensCrops ────────────┘
```

## Building

Requires Java 25.

```
./gradlew build
```

The shaded jar is written to `build/libs/`. Plugins that are not published to a Maven repository (other Gardens plugins and some third-party plugins) are compiled against jars in `dependencies/`, which are not committed. See [dependencies/README.md](dependencies/README.md) for the list.

## License

[MIT](LICENSE)
