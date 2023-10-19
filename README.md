## PickPack

[![GitHub release](https://img.shields.io/github/release/onebeastchris/PickPack?include_prereleases=&sort=semver&color=yellowgreen)](https://github.com/onebeastchris/PickPack/releases/)
[![License](https://img.shields.io/badge/License-MIT-yellowgreen)](https://github.com/onebeastchris/PickPack/blob/main/LICENSE)
[![issues - PickPack](https://img.shields.io/github/issues/onebeastchris/PickPack)](https://github.com/onebeastchris/PickPack/issues)

PickPack allows you to give each of your Geyser players the option to toggle bedrock resource packs on or off. Unfortunately, due to how Bedrock works, a reconnect is necessary - but done seamlessly.
This can be useful for console players that cannot easily install resource packs, such as consoles - or to just allow each player to customize what pack to use.

**IMPORTANT: This ONLY works on Geyser with the version #193 or higher. If you are using an older version, please update Geyser.**

Examples:
- Setup invisible item frames using resource packs, and allow players to toggle them on or off.
- Give console players the option to use a resource pack, or not.
- Add a pack to e.g. show chunk borders - that can be easily toggled on and off.

https://youtu.be/VImgD_DCC1Q

### This is an extension for Geyser; NOT a plugin/mod. To install it, drop the .jar file in Geyser's extensions folder.

For help with this project: https://discord.gg/WdmrRHRJhS

Download available in the releases tab.

## DISCLAIMER: While this project is made to work with Geyser (literally a geyser extension), it is not an official one - for help, ask in issues here or on the linked discord.

### Installing:
- Place the extension in Geyser's extensions folder.
- Restart the server.

### Usage:
- Configure the ip/port in the config.yml. The file is located under `/Geyser-xxx/extensions/PickPack/config.yml`.
These are needed so the automatic transfer packet works.
- Place packs that are applied by default go in the "DefaultPacks" folder. (located in `/Geyser-xxx/extensions/PickPack/DefaultPacks`)
- Place packs that players can optionally turn on go in the "OptionalPacks" folder. (located in `/Geyser-xxx/extensions/OptionalPacks`)
- Reload the extension using `/pickpack reload`, or restart the server.

### Commands:
- `/pickpack menu` or `/pickpack list` - Opens the pack selection menu.
- `/pickpack reset` or `/pickpack default` - Resets the player's pack selection to the default packs.
- `/pickpack reload` - Reloads the config.yml and the packs.

### Languages:
This extension has multi-language support. However, the default language is English - to add more languages, copy the `en_US.properties` file from the `/extensions/PickPack/translations` folder, and translate the strings. 
Then, save the file as e.g. `de_DE.properties` for German. You can select a default locale in the config.yml. Otherwise, all locales will be loaded & used automatically if a player's language matches.
To see all possible locales, check the Geyser locales [here](https://github.com/GeyserMC/languages/tree/master/texts).  
