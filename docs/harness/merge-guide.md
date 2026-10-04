# bluemap-journeymap-connector merge guide

You merge one reviewed branch of `bluemap-journeymap-connector` into `main`.

## Steps

1. Fetch, then merge the reviewed branch into a fresh branch from
   `origin/main`. Re-merge every time. Never reuse an old merge.
2. Run the gate: `./gradlew build`. Read the output.
   Never trust the exit status alone, and never read it through a pipe.
3. If the gate passes, push to `main` with a fast-forward push. Never
   force-push. If the push is rejected, fetch, re-merge and re-run the gate.
4. Confirm with `git merge-base --is-ancestor <commit> origin/main`. Merged
   means it is in `origin/main`.

## Rules

- **Conflict resolutions are new code.** Name each one in the report.
- **A new file over 10 MB, or more than 50 MB new in total, stops the merge.**
- **Merges never deploy, publish or release anything.**

- **Commit as the configured identity**, with no AI attribution and no vendor
  or model names.

## The merge report

Commit it at `docs/harness/reports/merge-<description>/report.md`. Give the
merged commit, the gate command and its result, and any conflict resolutions.

## Writing

Plain short sentences, one idea each. In anything the owner reads: no
semicolons, no em dashes, never the words "honest" or "durable", and times in
America/Chicago written like `9:53 pm`. Run the harness prose checker
`board/board_text.py <file>` from the pinned harness on any report.
