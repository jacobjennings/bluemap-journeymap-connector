Merge after replacing the tick callback screen setter with `client.gui.setScreen(WaypointDiffScreen())`.

Review task: http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-13
Implementation task: http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-10
Parent task: http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-2

Reviewed wave: `mcs-10-journeymap-connector-26-3`.
Reviewed branch: `codex/mcs-10-journeymap-connector-26-3`.
Reviewed commit: `32d4500c72d826ba9bf7473d30480dc9dcd34127`.
Base: `origin/main` at `db9e797400b07eab086173bc6c5e63ad18527b26`.

I read the implementation brief, repository guides, source diff, and worker report.
There is no repository `AGENTS.md`.
The worker report is committed at `docs/harness/reports/mcs-10-journeymap-connector-26-3/report.md`.
The report path supplied in the automatic brief was incomplete.

## Finding

Blocking. `fabric/src/main/kotlin/com/machinepeople/bluemapjourneymapconnector/client/BlueMapJourneyMapConnectorClient.kt:64`.
The brief requires preserving behavior.
The old `Minecraft.setScreen` changes the screen without forcing a frame.
The new `Minecraft.setScreenAndShow` calls `Gui.setScreen`, then `renderFrame(false)`.
This forces immediate rendering inside the `END_CLIENT_TICK` callback.
It is not a behavior-preserving rename.

The original setter moved to `client.gui.setScreen` in 26.3.
Use `client.gui.setScreen(WaypointDiffScreen())` at this call site.
I inspected the old and new Minecraft bytecode with `javap`.
The forced-render wrapper already existed in 26.1.2.
I also compiled the proposed replacement in the isolated review copy.
That check passed with three actionable tasks, one executed and two up-to-date.
The worker report should describe this setter migration accurately after the fix.

## Verification

I exported the exact reviewed commit into an isolated local directory.
The source worktree and review branch implementation files were untouched.


- `./gradlew classes` passed. Three actionable tasks, all executed.

- `./gradlew build` passed. Seven actionable tasks, four executed and three up-to-date.

- Both modules reported test tasks as `NO-SOURCE`. There are no test sources.

- Build output included native-access and Gradle deprecation warnings.
  The core jar task also reported that it could not determine a Mixin version.
  These messages did not prevent the build.

- The generated jar is `/tmp/mc-server-spinner-upper-workers/connector-review-DZfXQ6/fabric/build/libs/bluemap-journeymap-connector-1.2.0.jar`.

- Its metadata reports version `1.2.0` and nests `META-INF/jars/core-1.2.0.jar`.

- The Minecraft dependency is `>=26.3`.
  The retained `>=` predicate covers 26.3 and 26.3.x patches.
  It also permits future versions, whose compatibility remains untested.

- The diff passes `git diff --check`.
  It contains no license or license-header edits, secrets, built artifacts, or caches.
  The changed files fit the implementation brief.

## Published versions

I fetched each new dependency version from its public repository.
Each endpoint returned HTTP 200 and identified the requested version.


- Minecraft `26.3`. [Fabric game metadata](https://meta.fabricmc.net/v2/versions/game).


- Fabric Loader `0.19.5`. [Published POM](https://maven.fabricmc.net/net/fabricmc/fabric-loader/0.19.5/fabric-loader-0.19.5.pom).


- Fabric API `0.161.0+26.3`. [Published POM](https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/0.161.0+26.3/fabric-api-0.161.0+26.3.pom).


- Fabric Language Kotlin `1.14.1+kotlin.2.4.20`. [Published POM](https://maven.fabricmc.net/net/fabricmc/fabric-language-kotlin/1.14.1+kotlin.2.4.20/fabric-language-kotlin-1.14.1+kotlin.2.4.20.pom).


- Loom `1.17.21`. [Published POM](https://maven.fabricmc.net/net/fabricmc/fabric-loom/1.17.21/fabric-loom-1.17.21.pom).


- Kotlin plugins `2.4.20`. [Published POM](https://repo.maven.apache.org/maven2/org/jetbrains/kotlin/kotlin-gradle-plugin/2.4.20/kotlin-gradle-plugin-2.4.20.pom) and [Serialization plugin POM](https://repo.maven.apache.org/maven2/org/jetbrains/kotlin/kotlin-serialization/2.4.20/kotlin-serialization-2.4.20.pom).


- Coroutines `1.11.0`. [Published POM](https://repo.maven.apache.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-core/1.11.0/kotlinx-coroutines-core-1.11.0.pom).


- JourneyMap API `26.2-2.0.0-SNAPSHOT`. [Snapshot metadata](https://jm.gserv.me/repository/maven-snapshots/info/journeymap/journeymap-api-fabric/26.2-2.0.0-SNAPSHOT/maven-metadata.xml).

The language adapter POM confirms Kotlin 2.4.20 and Coroutines 1.11.0.
The build versions match those bundled runtime libraries.

I downloaded [BlueMap 5.28 for Fabric](https://modrinth.com/plugin/bluemap/version/bbzcTCOs).
All 49 bundled API classes match the public [BlueMap API 2.8.0 artifact](https://repo.bluecolored.de/releases/de/bluecolored/bluemap-api/2.8.0/bluemap-api-2.8.0.jar) byte for byte.
Keeping BlueMap API 2.8.0 is supported by that comparison.

I downloaded [JourneyMap 26.3-6.0.10 for Fabric](https://modrinth.com/mod/journeymap/version/2zmj8ela).
Its nested jar is `journeymap-api-fabric-26.3-2.0.0.jar`.
All 126 API classes from the compile dependency are present.
Only three class files differ.
The `IClientAPI` public signatures and JVM descriptors match between these two jars.
The annotation and waypoint factory class files are identical.
The string-dimension `createWaypoint` overload retains the argument order used here.
This supports binary compatibility for the changed calls.
It does not establish plugin discovery or waypoint behavior in game.

The keyboard migration uses the 26.3 `KEYBOARD` enum and its own `KEY_J` constant.
The 26.3 constant is 13, so keeping the old numeric GLFW value would have been wrong.
The patch correctly uses the new constant.

## RECOMMENDATIONS

Apply the screen setter correction and update the worker report before acceptance.
Re-run compilation and rebuild the corrected jar.

Before any separately authorized rollout, check the keybind and diff screen in game.
Check JourneyMap plugin discovery, waypoint add and remove, and client/server waypoint round trips.
No client or server was started during this review.
No lab host was contacted.
No jar was installed, and no branch was merged.
