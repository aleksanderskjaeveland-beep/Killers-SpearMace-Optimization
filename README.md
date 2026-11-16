# Killer's SpearMace Optimization — Standalone Fabric mod

Target: Minecraft Java 1.21.11, Fabric Loader 0.19.5, Java 21.

**This project does not use or require Meteor Client.**

## Features
- A custom menu with ESP / Flight toggles.
- Menu key is `R` by default and can be reassigned via Options > Controls > Key Binds > Killer's SpearMace Optimization.
- ESP applies a glowing outline to loaded living entities; limited to entities already sent to the client.
- Flight offers local vertical movement with Jump/Shift; online servers may reject movement or disconnect you. It is not anti-cheat bypass.

## Build
Install JDK 21 and Gradle 9.2+ (or use the Gradle wrapper once generated). Run `gradle build` in this folder. Find the release JAR in `build/libs/`.

Install the generated mod JAR along with Fabric API (1.21.11 compatible) on Fabric Loader 0.19.5. This mod does **not** need Meteor Client.

## Important
The code has not been compiled or tested against a Minecraft runtime in this environment. API method names may require small corrections during compilation. No security/anti-cheat bypass features are included. Client-side glowing may be reset on entity updates.
