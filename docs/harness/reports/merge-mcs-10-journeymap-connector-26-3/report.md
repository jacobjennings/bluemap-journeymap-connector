# MCS-10 merge report

```mermaid
flowchart LR
    A[Reviewed 26.3 port and screen fix] --> B[Conflict-free merge]
    B --> C[Build and classes passed]
    C --> D[Confirmed in origin/main]
```

Merged the reviewed 26.3 port and screen fix into `origin/main`.
Task: http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-10
Review: http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-16

Reviewed branch: `codex/mcs-10-fix-setscreen`.
Reviewed tip: `5d58c1d9997698c3cd9ee1090eaeaa4e8bcfa4cf`.
Merge commit: `236d65b3590578f63ce27ca65acaf793d5ca4afb`.
Main before the merge: `db9e797400b07eab086173bc6c5e63ad18527b26`.

The fetched source tip matched the brief before the merge and before the main push.
The report on `origin/review/mcs-10-fix-setscreen` began with `Merge.`.
Its path was `docs/reports/review-mcs-10-fix-setscreen/report.md`.
The fetched review branch tip was `d4a16c771ef25119356da9ddefba4c6bd3ef6930`.

## Merge checks

The worktree began clean at the fetched main tip.
The trial used `git merge --no-commit --no-ff origin/codex/mcs-10-fix-setscreen`.
I inspected its staged changes, then ran `git merge --abort`.
The real merge used `git merge --no-ff origin/codex/mcs-10-fix-setscreen`.
There were no conflicts and no conflict resolutions.
`git diff --cached --check` passed during the trial.

Git's staged added-file list contained two new reports.
Their blob sizes were 2,534 bytes and 5,249 bytes.
The total was 7,783 bytes.
No new file exceeded 10 MB and the total remained below 50 MB.

## Gate

Both commands ran in the foreground without a pipe.
I read their complete output.
The environment used `TMPDIR=/tmp/mc-server-spinner-upper-workers` and empty `CUDA_VISIBLE_DEVICES`.
`/usr/bin/node --version` reported `v26.10.0`.
The step-zero directory check found no `node_modules/typescript`.
This repository uses Kotlin and Gradle.
Both modules compiled Kotlin successfully.

| Command | Output summary | Exit status |
| --- | --- | --- |
| `./gradlew build` | `BUILD SUCCESSFUL in 11s`. `7 actionable tasks: 7 executed`. | 0 |
| `./gradlew classes` | `BUILD SUCCESSFUL in 638ms`. `3 actionable tasks: 3 up-to-date`. | 0 |

The build reported `:core:test NO-SOURCE` and `:fabric:test NO-SOURCE`.
Both modules also reported test compilation as `NO-SOURCE`.
There was no test-runner summary of files or passed, failed, skipped, or todo tests.
The gate therefore provides compilation and jar evidence with no executed tests.

Before the gate, `/proc/loadavg` read `16.70 15.73 13.89 17/11123 3900698`.
The process-state filter counted zero tasks whose state began with `D`.
Two `/proc/stat` readings three seconds apart measured CPU busy at 37.4 percent and idle at 62.6 percent.
No deferral was needed.

The output included native-access and Gradle deprecation warnings.
The core jar task could not determine a Mixin version.
These warnings did not stop either gate.
The built jar is `fabric/build/libs/bluemap-journeymap-connector-1.2.0.jar`.
It remains an uncommitted build artifact.

## Remote confirmation

I pushed the merge commit to the assigned merge branch and checked its remote hash with `git ls-remote`.
After both gates, I fetched again.
Main and the reviewed source tip had not moved.
`git push origin HEAD:main` advanced main without force.
I fetched after that push.
`origin/main` resolved to `236d65b3590578f63ce27ca65acaf793d5ca4afb`.

Both ancestry commands returned exit status 0:

```text
git merge-base --is-ancestor 5d58c1d9997698c3cd9ee1090eaeaa4e8bcfa4cf origin/main
git merge-base --is-ancestor 236d65b3590578f63ce27ca65acaf793d5ca4afb origin/main
```

All git commands used the assigned worktree through `git -C`.
No browser was started, so no browser cleanup was required.
No client or server was started.
Nothing was deployed, published, or released.

The report passed `board/board_text.py` from the pinned harness at `mc-server-spinner-upper-pin-53e707d`.

## RECOMMENDATIONS

- Fix the harness verdict reader to accept a first-line `Merge.` verdict.
- Before a separately authorized rollout, verify the keybind and diff screen in game.
