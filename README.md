## PickPack

### This does not yet work on Geysers master branch.

# This is an extension for Geyser; NOT a plugin/mod. To install it, drop the .jar file in Geyser's extensions folder.

per-player-packs allows you to give each of your Geyser players the option to toggle specific packs. This can be useful for console players that cannot easily install resource packs - or to just allow each player to customize what pack to use.

Usage:
- Install the extension, and restart the server.
- Once restarted, the extension will create a new folder in the "extensions" folder. Open it, and there will be 3 folders: "cache", "opt-in", and "opt-out".
  The "cache" folder is used to store player's selected packs; I would not recommend to change the folder or its contents.
  "opt-in" is for packs that you want to "offer" to your player - they will NOT be applied by default, instead, players can select them.
  "opt-out" is for packs that are sent to all bedrock players by default - players can turn those packs OFF.
- After adding your packs to either "opt-in" or "opt-out", restart the server again. If everything succeeded, there should be a message in the console stating that all packs were loaded.

To switch packs, type "todo" in the chat. This command is bedrock-only.

For help with this project: https://discord.gg/NrUwZuXD 

Download: Releases tab.

#DISCLAIMER: While this project is made to work with Geyser (literally a geyser extension), it is not an official one - for help, ask in issues here or on the linked discord.
