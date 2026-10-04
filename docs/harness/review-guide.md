# bluemap-journeymap-connector review guide

You review one branch of `bluemap-journeymap-connector` against its brief. Read the brief, the diff
against `origin/main` and the worker's report. Then write a verdict.

## Hard rules that decide a diff

- **Fabric refuses mismatched mods at startup.** `fabric.mod.json` `depends.minecraft`
  must match the server's Minecraft version. A bare `"26.1"` does not match `26.1.1`, and
  one unmet hard dependency stops the whole server. Prefer a `~` or `>=` predicate that
  covers the target version, and say which you chose.
- **This jar runs on the live server `stlmc`.** It is installed by the
  mc-server-spinner-upper Ansible role from a local jar list. You build the jar. You never
  install it anywhere.
- **Never contact a real server.** Do not reach `stlmc.lan` or any other lab host. Never
  start a Minecraft server or client unless your brief says so.
- **No secrets, no built artifacts, no caches in the diff.**
- **No AI attribution and no vendor or model names** in commits or files.
- **The change stays inside the brief.** Extra changes are a finding.
- **Legal and licensing text needs the owner's approval.** Flag any edit to a
  license file or license header.

## Ways a check can pass while proving nothing

- **A compile proves types, not behavior.** A version bump that compiles can
  still fail at runtime when Minecraft changed a method's meaning.
- **A version string must exist.** Check each new dependency version on its
  maven or on Modrinth yourself. A guessed version that happens to resolve from
  a cache is still wrong.
- **Never conclude that a run passed from its exit status alone.** Read the
  output.

## Checks you run

Gradle needs a writable home. If `~/.gradle` is not writable in your sandbox,
run `export GRADLE_USER_HOME="$TMPDIR/gradle-home"` first. Never commit a Gradle cache or
a `build/` folder.

- **Compile:** `./gradlew classes` (fast, after any code or version change).
- **Build the jar:** `./gradlew build`. The jar lands under `build/libs/` or
  `<module>/build/libs/`. Name its path and file name in your report.
- **Version changes** live in `gradle.properties`. Check each new version exists on its
  maven or on Modrinth before you use it, and report where you checked.

## Review report rules

- **The first line is exactly one verdict word**: `Merge.`, `Merge after` with
  the named change, or `Reject.`
- **Each finding gives its severity (blocking or suggestion), the file and
  line, and why.**
- **The report goes at `docs/harness/reports/review-<description>/report.md`**
  on the review branch, and names the branch or commit it reviewed.
- **A review's deliverable is a verdict, not a merge.**

## Writing

Plain short sentences, one idea each. In anything the owner reads: no
semicolons, no em dashes, never the words "honest" or "durable", and times in
America/Chicago written like `9:53 pm`. Run the harness prose checker
`board/board_text.py <file>` from the pinned harness on any report.
