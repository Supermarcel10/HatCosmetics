# Hat Cosmetics

[![Discord](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/social/discord-plural_vector.svg)](https://discord.gg/FKzTPWZAYW)

## About

HatCosmetics is a plugin that provides a simple and user-friendly way to implement custom hat resource pack models on your server.

## Features

**Hat Selection GUI**
- A customisable GUI that serves as the main menu for players to browse and select hats to equip.
- Players can view all available hats, but can only equip the ones they have permissions for.

**Permissions and Access Control**
- Granular permission system to control which players can access and equip specific hats.
- Out of the box support for a wide range of permissions plugins.

**Customisation and Extensibility**
- Easily add or remove hats by modifying the plugin's configuration files.
- Designed with easy design, allowing easy creation and implementation of custom hats.

## Installation
### Releases

> [!CAUTION]
> Do not download HatCosmetics from any other source than the ones listed below.

[![GitHub](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy-minimal/available/github_vector.svg)](https://github.com/ItsTonus/HatCosmetics/releases)
[![SpigotMC](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy-minimal/supported/spigot_vector.svg)](https://www.spigotmc.org/resources/83111/)
[![CurseForge](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy-minimal/available/curseforge_vector.svg)](https://www.curseforge.com/minecraft/bukkit-plugins/hatcosmetics)
[![BuiltByBit](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy-minimal/available/builtbybit_vector.svg)](https://builtbybit.com/resources/hatcosmetics.44686/)
[![Modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy-minimal/available/modrinth_vector.svg)](https://modrinth.com/plugin/hatcosmetics)

1. Download the latest release for your server version.
2. Download the latest release of [NBT API](https://modrinth.com/plugin/nbtapi/version/2.12.4).
3. Drop the JAR files into the `plugins` folder of your Minecraft server.
4. Restart the server.

### Building from Source

1. Clone the repository.
2. Run `gradlew build` in the root directory of the repository.
3. The JAR file will be located in `build/libs/`.
4. Drop the JAR file into the `plugins` folder of your Minecraft server.

> [!NOTE]
> You will need to download or build the latest release of [NBT API](https://modrinth.com/plugin/nbtapi/version/2.12.4) and drop it into the `plugins` folder of your server.

5. Restart the server.

## Usage
### Commands

**Aliases**: `/hats`, `/hatcosmetics`

| Command         | Description                                    |
|:----------------|:-----------------------------------------------|
| `/hats`         | Opens the main GUI                             |
| `/hats help`    | Opens the command list for the plugin          |
| `/hats unequip` | Removes the hat (if the player is wearing one) |
| `/hats equip`   | Equips the specified hat                       |
| `/hats reload`  | Reloads the plugin configuration               |

### Permissions

The pluguin supports permissions, and works with any permissions plugin that supports the Bukkit API.<br>
This includes plugins like LuckPerms, PermissionsEx, and more.

Each command has its own permission node.<br/>
For e.g. `/hats reload` has the permission node `hatcosmetics.reload`.

Each hat has its own permission node.<br/>
For e.g. a hat with the name `example` has the permission node `hatcosmetics.hat.example`.

### GUI
TO BE DOCUMENTED FURTHER

## Contributing
### Contributing Guide

Anyone and everyone is welcome to contribute and help out with the project!

### Contributors
[![Contributors](https://contrib.rocks/image?repo=ItsTonus/HatCosmetics)](https://github.com/ItsTonus/HatCosmetics/graphs/contributors)

## Licence

HatCosmetics is licensed under the [MIT License](LICENSE).
