# Killer's SpearMace Optimization

Standalone Fabric client mod for Minecraft Java **1.21.11**, Fabric Loader **0.19.5**, and Java 21. Author: **esoria**.

## Controls and features

- Press **R** to open the mod menu. Rebind via **Options → Controls → Key Binds → Killer's SpearMace Optimization**.
- **Block ESP**: toggle in the menu; right-click the row or choose *Choose Blocks* to select blocks from a searchable scrollable Minecraft item grid. Highlights matching nearby client-loaded blocks within a radius of 8 horizontal and 6 vertical blocks, refreshed every 30 game ticks.
- **Tracers**: toggle in the menu; right-click the row or choose *Choose Entities* to select entity types from a searchable grid. Players use a player-head icon; mobs with spawn eggs use their spawn-egg icons. Only loaded entities are considered.
- **Flight**: toggle in the menu; right-click the row or choose *Flight speed* to set horizontal and vertical speeds. Movement accelerates and decelerates smoothly rather than snapping to a fixed speed. **Space** rises and **Shift** descends.
- Custom item-grid mod icon derived from the supplied Minecraft items image.

## Limitations

Flight changes local movement, not server permissions. Multiplayer servers can reject flight or disconnect the player. It does not include server-side permission changes or anti-cheat bypass. This flight mode is not identical to Meteor Client's multiple flight modes.

Settings are currently held in memory and reset when restarting Minecraft. The player should test the visual rendering in-game; a successful compilation does not guarantee every feature renders as intended.

## Install

Build using the included GitHub Actions workflow. Download the successful run's artifact, extract its ZIP, and put the normal non-`-sources` mod JAR in your Modrinth profile's `mods` folder. Fabric API compatible with 1.21.11 is required. Meteor Client is **not** required.
