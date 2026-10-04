Merge.

Review task: http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-16
Implementation task: http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-10
Previous review: http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-13

Reviewed wave: `mcs-10-fix-setscreen`.
Reviewed branch: `codex/mcs-10-fix-setscreen`.
Reviewed commit: `5d58c1d9997698c3cd9ee1090eaeaa4e8bcfa4cf`.
Fix base: `32d4500c72d826ba9bf7473d30480dc9dcd34127`.
Main comparison: `origin/main` at `db9e797400b07eab086173bc6c5e63ad18527b26`.

## Acceptance

No blocking findings or suggestions in this fix.

Accepted. `fabric/src/main/kotlin/com/machinepeople/bluemapjourneymapconnector/client/BlueMapJourneyMapConnectorClient.kt:64`.
The call now uses `client.gui.setScreen(WaypointDiffScreen())`.
This resolves the previous review's blocking finding.
The callback still requests waypoint data before opening the screen.

I inspected the 26.3 bytecode with `javap`.
`Minecraft.setScreenAndShow` calls `Gui.setScreen`, then `renderFrame(false)`.
`Gui.setScreen` performs screen lifecycle and initialization without that forced render.
The compiled connector calls `Gui.setScreen` directly and returns.
It therefore avoids the forced frame inside `END_CLIENT_TICK`.

Accepted. `docs/harness/reports/mcs-10-journeymap-connector-26-3/report.md:57`.
The corrected report describes the setter's move to `client.gui.setScreen`.
It also explains why `setScreenAndShow` was an unsuitable replacement.

I read the worker brief, both repository guides, previous review, and worker report.
No repository `AGENTS.md` exists.
The worker report is on the source branch at `docs/harness/reports/mcs-10-fix-setscreen/report.md`.
The automatic brief supplied an incomplete report path.

I inspected the complete diff against `origin/main`.
The 26.3 port is inherited from the named fix base.
The fix itself changes only the setter, the earlier report, and its own report.
It adds no dependency versions.
No license text or headers changed.
The diff contains no secrets, built artifacts, or caches.
`git diff --check` passed.

## Verification

I exported the reviewed commit into `/tmp/connector-review-setscreen.kHE5Q1`.
The build used that isolated copy.
The source worktree and review branch source files were untouched.

- `./gradlew classes build` passed from the clean archive.
  Seven actionable tasks executed.
  Both modules reported test tasks as `NO-SOURCE`.
- The brief's separate `./gradlew classes` check passed.
  Three actionable tasks were up-to-date after the clean build.
- The brief's separate `./gradlew assemble` check passed.
  Seven actionable tasks were up-to-date after the clean build.
- Both source roots contain one screen setter call, at the accepted location.
  No `setScreenAndShow` calls remain.

I read the build output.
Native-access and Gradle deprecation warnings appeared.
The core jar task could not determine a Mixin version.
These messages did not prevent compilation or jar creation.

Jar: `/tmp/connector-review-setscreen.kHE5Q1/fabric/build/libs/bluemap-journeymap-connector-1.2.0.jar`.
Its metadata reports version `1.2.0` and nests `META-INF/jars/core-1.2.0.jar`.
The retained Minecraft predicate is `>=26.3`.
It covers 26.3 and 26.3.x patches.
It also permits future versions whose compatibility remains untested.

## RECOMMENDATIONS

Accept the setter fix and corrected report.
Before a separately authorized rollout, verify the keybind and diff screen in game.
This review establishes the direct setter call and a successful build.
It does not establish in-game behavior.
No client or server was started.
No lab host was contacted.
No jar was installed and no branch was merged.
