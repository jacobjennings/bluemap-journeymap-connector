# MCS-10 fix-up: use client.gui.setScreen instead of setScreenAndShow

Task: http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-10
Review card: http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-13
Branch: `codex/mcs-10-fix-setscreen`, cut from
`origin/codex/mcs-10-journeymap-connector-26-3` at `32d4500c`.

## The change

`fabric/src/main/kotlin/com/machinepeople/bluemapjourneymapconnector/client/BlueMapJourneyMapConnectorClient.kt:64`
now reads:

```kotlin
client.gui.setScreen(WaypointDiffScreen())
```

It previously read `client.setScreenAndShow(WaypointDiffScreen())`.
In 26.3 the old `Minecraft.setScreen` moved to `Gui.setScreen`.
`setScreenAndShow` is a different wrapper that also calls
`renderFrame(false)`. That forces a frame inside the `END_CLIENT_TICK`
callback, so it was not a behavior-preserving rename. The review reached
the same conclusion from bytecode.

No imported names changed. `WaypointDiffScreen` was already imported in
this file.

## Other call sites

Grep over both source roots for `setScreen` and `setScreenAndShow`:

```text
rg -n "setScreenAndShow|setScreen" fabric/src/main/kotlin core/src/main/kotlin
fabric/.../client/BlueMapJourneyMapConnectorClient.kt:64: client.setScreenAndShow(WaypointDiffScreen())
```

That was the only call site. Nothing remained to change after the fix.
The final source tree has zero `setScreenAndShow` occurrences.

## Report correction

`docs/harness/reports/mcs-10-journeymap-connector-26-3/report.md` said
`Minecraft#setScreen` no longer exists and that `setScreenAndShow` is the
only setter. That is wrong. The bullet now says the setter moved to
`client.gui.setScreen`, names `setScreenAndShow` as the frame-forcing
method that was not a valid substitute, and notes the correction.

## Checks

Both run from the worktree with `HULY_BOARD_CONFIG` and
`VIKUNJA_BASE_URL` pointed at `http://127.0.0.1:1`.

- `./gradlew classes`: BUILD SUCCESSFUL. "3 actionable tasks: 3 executed".
- `./gradlew assemble`: BUILD SUCCESSFUL. "7 actionable tasks: 4 executed,
  3 up-to-date".

No test sources exist in the repo, so there are no unit-test counts.
Nothing was installed or deployed. No host was contacted.

## RECOMMENDATIONS

- In-game check of the keybind and diff screen is still owed before
  rollout, per the review. This fix makes the open path match 26.1
  behavior. It does not prove it in game.
- If a later port targets a Minecraft version that renames `Gui.setScreen`
  again, re-run this grep pattern. It is the whole surface of this risk.
