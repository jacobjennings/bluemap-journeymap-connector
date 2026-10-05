# bluemap-journeymap-connector worker guide

You are a worker on one task for `bluemap-journeymap-connector`, tracked on the Huly board project
MCS with the other Minecraft repositories. This guide replaces any `AGENTS.md`
for you. Your brief is your task. This guide is the rules around it. The
owner's personal guide is also loaded, and it applies in full.

## What bluemap-journeymap-connector is

A Fabric mod by Jake, written in Kotlin, that connects BlueMap markers to JourneyMap waypoints. Modules: `core` and `fabric`. It uses Minecraft networking classes and a JourneyMap API that is `compileOnly`. Versions are in `gradle.properties`.

Server and client. Players need the matching jar, so a protocol change needs both sides updated together.

## Your working rules

- **Stay inside your worktree.** Read and change only what your brief names,
  plus what you must read to do it.
- **Commit early and push often.** Make a real commit within your first ten
  minutes, then commit and push after each meaningful step.
- **Every git command carries `-C <worktree>`.** Your shell's working
  directory does not persist between commands.
- **Commit as the configured identity.** Never add AI attribution or
  co-author trailers, and never name a vendor or model in a commit message.
  Never force-push or rewrite pushed history.
- **Work stays on your branch.** Do not merge into `main` and do not push to it.
  Merged means it is in `origin/main`, and the merge lane does that.
- **Run only the checks near your change.** The full build gate belongs to the
  merge lane, except where your brief names it.
- **Never launch a graphical application.**
- **You run directly on the owner's machine, with no sandbox.** You have his
  home folder, keys, git, the `gh` CLI and the network. Three things are
  forbidden because workers have caused damage with them before:
  - **Never kill, stop or signal a process you did not start.** No `kill`,
    `pkill`, `killall`, `tmux kill-*`, `systemctl stop` or restarts of other
    sessions, servers or workers. If something is in your way, report it.
  - **Never force-push or rewrite pushed history.** No `push --force`,
    `--force-with-lease`, `+refspec`, deleting remote branches you did not
    create, or rebasing or amending commits that are already pushed. Never
    push to the main branch unless your brief makes you the merge run.
  - **Never run anything on an NVIDIA GPU.** The RTX 5090 (jjpc) and RTX 3090
    (bb) serve inference only. Keep `CUDA_VISIBLE_DEVICES` empty, and use the
    AMD or Intel GPU or the CPU for browsers, captures, benchmarks and tests.
  - **Never open, stat-probe or enumerate NVIDIA device nodes, for any reason.**
    Tests and probes use stand-in device nodes or fixtures. To show that a container
    cannot reach a GPU, read its configuration (for example `podman inspect` lists no
    NVIDIA or CDI devices and no `--gpus`). Never look at `/dev/nvidia*` itself.
  - **Chrome's software renderer is not acceptable.** Jake's words: "chome's software
    renderer is not acceptable". Any browser or WebGPU check must show a hardware
    adapter that is not a fallback, and the report names the adapter it saw.
- **Never run a destructive command for real, not even in a test.** Every
  command hits the owner's real machine. No `kill -1`, `kill 0`, `pkill` or
  `killall` by name, `tmux kill-server`, `systemctl stop` or force-push to a
  real remote, even when a guard is supposed to catch it. Test a guard through
  its decision function, or with stub binaries first on `PATH`. A test like
  this killed every process the owner had on 3 October 2026.
- **Keep large files out of the repository.** A new file over 10 MB, or more
  than 50 MB of new content in total, fails the merge gate. Never commit built
  jars, `build/`, `dist/`, `node_modules/` or a Gradle cache.
- **Finish with a report when your brief asks for one**, committed and pushed
  under `docs/harness/reports/`, and name its path in your last message. End it
  with a RECOMMENDATIONS section for anything outside your scope.
- **Your run is not done until your checks pass and your commit is pushed.**
- **Name a card by its full link**, such as `http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-12`.
- **You cannot edit `AGENTS.md` or `CLAUDE.md`.** If your task needs a guide
  change, say so in your report.

## Targeted checks

Run these from your worktree.

Gradle needs a writable home. If `~/.gradle` is not writable,
run `export GRADLE_USER_HOME="$TMPDIR/gradle-home"` first. Never commit a Gradle cache or
a `build/` folder.

- **Compile:** `./gradlew classes` (fast, after any code or version change).
- **Build the jar:** `./gradlew build -x test`. The jar lands under `build/libs/` or
  `<module>/build/libs/`. Name its path and file name in your report.
- **Version changes** live in `gradle.properties`. Check each new version exists on its
  maven or on Modrinth before you use it, and report where you checked.

## Test scope

Jake, 5 October 2026, his words: "Full suite tests should only run on the train. impl/review should run targeted tests actually affected by the changed code."

- **Never run the full suite.** Here that is `./gradlew build`. It runs only at merge time.
- **Run targeted tests only.** That means the tests of the files you changed, the
  tests that import or execute the code you changed, and the tests the card names.
  Show the search you used to find them, such as the `grep -rn` command and its hits.
- **List each targeted test in the report with its result**: passed, failed, or could
  not run and why.
- Building the jar the card needs, such as for a staging boot, is allowed. Use
  `./gradlew build -x test` so the test suite stays at merge, then run the targeted
  tests by name.

## Keep your context small

- **Read ranges, not files.** Find what you need with `rg -n`, then read the
  lines around it with `sed -n '120,180p' <file>`.
- **Pipe long build output through `tail -40`.**
- **Commit and push after the first working edit and after every build.** A run
  that hits its time limit keeps only what is committed.
- **Stop at your brief's one change.** If the task needs more, say so in your
  final message and name the next step.

## Side jobs on the local cluster

- `local-subagent.sh '<question>'` runs a small isolated job. `--deep` uses the
  full model. `--vision <image>` asks about an image.
- Exit 75 means that machine is full. Exit 65 means it is down. Do not retry in
  a loop. Carry on without it and say so in your report.

## Rules that must never break

- **Fabric refuses mismatched mods at startup.** `fabric.mod.json` `depends.minecraft`
  must match the server's Minecraft version. A bare `"26.1"` does not match `26.1.1`, and
  one unmet hard dependency stops the whole server. Prefer a `~` or `>=` predicate that
  covers the target version, and say which you chose.
- **This jar runs on the live server `stlmc`.** It is installed by the
  mc-server-spinner-upper Ansible role from a local jar list. You build the jar. You never
  install it anywhere.
- **Never contact a real server.** Your SSH keys can reach them, and only this rule
  stops you. Do not reach `stlmc.lan`, `ss.lan` or any other lab host. Never
  start a Minecraft server or client unless your brief says so.
- **Never write a secret** or commit a token.

## Writing

Plain short sentences, one idea each. In anything the owner reads: no
semicolons, no em dashes, never the words "honest" or "durable", and times in
America/Chicago written like `9:53 pm`. Run the harness prose checker
`board/board_text.py <file>` from the pinned harness on any report.
