# MCS-10 report: bluemap-journeymap-connector build for Minecraft 26.3

Branch: `codex/mcs-10-journeymap-connector-26-3`. Card: http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-10

Run started 11:50 pm CDT Oct 3. Finished jar by 12:08 am CDT Oct 4.

## Result

The connector now builds against Minecraft 26.3. Mod version is 1.2.0.
The jar is `fabric/build/libs/bluemap-journeymap-connector-1.2.0.jar` (260 KB).
It nests `core-1.2.0.jar` in `META-INF/jars`. Nothing was installed anywhere.

## Dependency versions set

| Item | Old | New | Checked at |
| --- | --- | --- | --- |
| Minecraft | 26.1.2 | 26.3 | meta.fabricmc.net game list |
| Fabric loader | 0.19.3 | 0.19.5 | meta.fabricmc.net loader/26.3 |
| Fabric API | 0.153.0+26.1.2 | 0.161.0+26.3 | Modrinth API for game_version 26.3 |
| fabric-language-kotlin | 1.13.11+kotlin.2.3.21 | 1.14.1+kotlin.2.4.20 | maven.fabricmc.net metadata |
| Loom plugin | 1.17.12 | 1.17.21 | maven.fabricmc.net metadata |
| Kotlin plugin | 2.3.21 | 2.4.20 | Matches the Kotlin the new FLK bundles |
| kotlinx-coroutines-core | 1.10.2 | 1.11.0 | FLK 1.14.1 dependency list |
| kotlinx-serialization-json | 1.11.0 | 1.11.0 | FLK 1.14.1 already ships this |
| BlueMap API | 2.8.0 | 2.8.0 (kept) | see below |
| JourneyMap API | 2.0.0-26.1-SNAPSHOT | 26.2-2.0.0-SNAPSHOT | jm.gserv.me maven-snapshots metadata |

`fabric.mod.json` needs no hand edit. Its `minecraft` dependency expands from
`gradle.properties` to `">=26.3"`. I kept the `>=` predicate. It covers 26.3
and any 26.3.x patch.

### BlueMap API check

BlueMap 5.28 is the 26.3 build on Modrinth. I downloaded
`bluemap-5.28-fabric.jar` from the Modrinth CDN and compared its bundled
`de/bluecolored/bluemap/api` classes with the maven artifacts
`bluemap-api-2.8.0.jar` and `bluemap-api-2.8.1.jar` from repo.bluecolored.de.
All 49 api classes match, and the 2.8.0 and 2.8.1 artifacts are byte
identical. So API 2.8.0 still matches BlueMap 5.28 and I left it alone.

### JourneyMap API check

The brief was right. The `jm.gserv.me` maven-snapshots repo has nothing newer
than `26.2-2.0.0-SNAPSHOT`. There are no published releases in
maven-releases. Building against the 26.2 API snapshot works. It needed two
small changes in `JourneyMapIntegration.kt`. JourneyMap itself ships a
`26.3-6.0.10+fabric` client build on Modrinth. The API-vs-runtime match for
26.3 can only be confirmed in game.

## API changes made in code

- `org.lwjgl.glfw.GLFW` is gone from the 26.3 classpath. The keybind now uses
  `InputConstants.Type.KEYBOARD` and `InputConstants.KEY_J`.
- `InputConstants.Type.KEYSYM` no longer exists. The enum is now
  `KEYBOARD` and `MOUSE`. Confirmed with `javap` on the loom-produced
  `minecraft-merged-deobf-26.3.jar`.
- The `Minecraft#setScreen` setter moved to `client.gui.setScreen` in 26.3.
  The diff screen call uses it. `setScreenAndShow` is a different method that
  also forces a render frame, so it was not a behavior-preserving substitute.
  (Corrected by MCS-10 follow-up. This bullet previously described the
  migration inaccurately while the code used `setScreenAndShow`.)
- The `@JourneyMapPlugin` annotation moved from `journeymap.api.v2.client` to
  `journeymap.api.v2.common` in the 26.2 API jar.
- `WaypointFactory.createClientWaypoint` is gone. It is replaced by
  `createWaypoint`. The overload
  `createWaypoint(String modId, BlockPos, String name, String dimension, boolean)`
  takes the same arguments in the same order. The `javap` disassembly of the
  factory shows the delegate call keeps that order.

No behavior was changed beyond these renames. Network payload code
(`CustomPacketPayload`, `StreamCodec`, `FriendlyByteBuf`, `Identifier`)
compiled against 26.3 with no edits. That says the wire classes kept their
shape. It does not prove the server and client agree at runtime, so the both
sides rule still applies to rollout.

## Test counts

First `./gradlew classes` failed with 7 compile errors (the ones listed above).
After the fixes:

- `./gradlew classes`: BUILD SUCCESSFUL, "3 actionable tasks: 1 executed, 2
  up-to-date".
- `./gradlew assemble`: BUILD SUCCESSFUL, "7 actionable tasks: 4 executed, 3
  up-to-date". Jar written at `fabric/build/libs/bluemap-journeymap-connector-1.2.0.jar`.

No test sources exist in the repo, so no unit-test counts to report. No maven
host was blocked by the network on this run.

## Needs an in-game check

1. Keybind opening the diff screen, since the GLFW constants and `setScreen`
   path changed.
2. JourneyMap plugin discovery and waypoint add/remove against JourneyMap
   26.3-6.0.10+fabric, because we compiled against the 26.2 API.
3. Round-trip waypoint sync client to server, since payload classes come from
   the 26.3 networking stack.

## RECOMMENDATIONS

- Ask the JourneyMap authors, or watch jm.gserv.me, for a `26.3` API
  snapshot. Until then the compileOnly 26.2 API is the best available.
- The merge lane should treat this jar as needing paired client and server
  installs on stlmc, because both sides now run 26.3 binaries.
- Consider raising `fabricloader` in `fabric.mod.json` from `>=0.19.0` to
  `>=0.19.5` in a future release if any loader behavior turns out to matter.
  I left it untouched because it is outside this brief.
