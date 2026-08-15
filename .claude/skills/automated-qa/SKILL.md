---
name: automated-qa
description: Programmatically drive the game client with a temporary in-process QA driver, capture framebuffer screenshots of the feature under test, verify them, and attach the evidence to the PR. Use for any change with a visible or interactive surface BEFORE handing off to manual QA.
---

# Automated QA

Drive the real game client programmatically, capture framebuffer screenshots of the feature under
test, verify them, and attach the evidence to the PR. Use this after implementing any change with a
visible or interactive surface (screens, HUD, rendering, in-world interactions) BEFORE handing off
to manual QA. Manual QA then confirms feel and edge cases instead of discovering basics.

Why in-process instead of OS automation: macOS input automation (System Events, cliclick) needs
accessibility permissions the agent shell usually lacks, and OS-level clicks are brittle against
window focus and Retina scaling. A temporary in-process driver has full deterministic control over
the client, needs no OS permissions, and captures pixel-exact framebuffer screenshots.

## The Temp Driver Pattern

All driver code is TEMPORARY and must never be committed. It exists only in the worktree during QA.

1. Create `src/client/java/com/grahambartley/dogsunleashed/<Feature>QaDriver.java` from
   `templates/QaDriver.java` in this skill directory. It is a tick-driven state machine registered
   on `ClientTickEvents.END_CLIENT_TICK`.
2. Register it with one line at the end of `DogsUnleashedClient.onInitializeClient()`:
   `<Feature>QaDriver.register();`
3. After QA passes, revert both:
   `git checkout -- src/client/java/com/grahambartley/dogsunleashed/DogsUnleashedClient.java`
   `rm src/client/java/com/grahambartley/dogsunleashed/<Feature>QaDriver.java`

## Capabilities Toolbox

- **World loading**: from the title screen call
  `client.createIntegratedServerLoader().start("New World", () -> {})` once
  `client.currentScreen instanceof TitleScreen` and ~60 ticks have passed (resources settled).
  Do NOT bother with loom `programArgs "--quickPlaySingleplayer", ...` — it is not picked up.
  The dev `run/saves/New World` world exists in every worktree because the `worktree` skill copies
  `run/`.
- **Server-side setup**: `client.getServer().execute(() -> ...)` reaches the integrated server.
  Spawn entities, set NBT/DataTracker state, tame to
  `server.getPlayerManager().getPlayerList().get(0)`, and trigger S2C packets exactly as
  production code would (e.g. calling the same `ModNetworking.send*` used by gameplay). This tests
  the real network round trip, not a mock.
- **Clicks and keys**: call `client.currentScreen.mouseClicked(sx, sy, 0)` / `keyPressed(...)`
  directly with SCALED screen coordinates. No cursor movement needed; this drives the same code
  path as a real click and real C2S packets flow.
- **Hover states**: hover rendering reads the real OS cursor, so move it with
  `GLFW.glfwSetCursorPos` plus the iterative settle loop in the template. Never trust a single
  set call: GLFW cursor space vs framebuffer size differs per display (Retina), so converge with
  the multiplicative feedback loop until `client.mouse` derives to the target scaled position.
- **Screenshots**: `ScreenshotRecorder.saveScreenshot(client.runDirectory, name + ".png",
  client.getFramebuffer(), text -> {})` writes to `run/screenshots/`. Log the derived mouse
  position alongside each shot so a failed hover is diagnosable from the log.
- **GUI scale sweeps**: `client.setScreen(null); client.options.getGuiScale().setValue(n);
  client.onResolutionChanged();` then re-trigger the screen. Capture at least scales 1, 2, and 4
  for anything with custom rendering.
- **Lifecycle**: print `[QA]`-prefixed markers for every step, a final `[QA] DONE`, catch every
  exception into `[QA] ERROR`, and end with `client.scheduleStop()` so the run terminates itself.

## Environment Prep (once per worktree)

- `run/options.txt`: set `pauseOnLostFocus:false` (MANDATORY — the client runs unfocused in the
  background and singleplayer would otherwise sit on the pause screen, which also blocks any
  screen-open packet handler that checks `currentScreen == null`). Pin `guiScale` to a known value
  so the first screenshots are deterministic.
- These are `run/` files, untracked; no cleanup needed beyond not committing them.

## Run Protocol

1. **Absolute paths only.** Every command runs with an explicit
   `cd /path/to/.claude/worktrees/dogs-unleashed-<branch> && ...`. The classic failure mode is the
   shell cwd silently resetting to the main checkout, which launches the OLD mod without the
   driver and "does nothing". If a run produces zero `[QA]` log lines, check cwd first.
2. Compile and PROVE the driver is in the build before launching:
   `./gradlew compileClientJava` then confirm
   `ls build/classes/java/client/.../<Feature>QaDriver.class` exists and
   `javap -c -classpath build/classes/java/client com.grahambartley.dogsunleashed.DogsUnleashedClient | grep -c QaDriver`
   returns non-zero. Do not launch until both check out.
3. Launch in the background with output to a log file:
   `./gradlew runClient > /tmp/qa-run.log 2>&1` (background).
4. Wait on the sentinels, never on time:
   `until grep -qE '\[QA\] (DONE|ERROR)|BUILD FAILED' /tmp/qa-run.log; do sleep 3; done`
5. Read the `[QA]` log lines, then Read every captured PNG and actually LOOK at it: sector
   alignment, text legibility, state highlights, scale behaviour. A green log with wrong pixels is
   a failed QA.

## Publishing Evidence to the PR

GitHub's `user-attachments` uploads are not available via `gh`, so commit the evidence instead:

1. Downscale the keeper screenshots: `sips -Z 1000 run/screenshots/qa_*.png --out docs/qa/<feature>/`
2. Commit `docs/qa/<feature>/*.png` on the PR branch.
3. Embed them in the PR body via raw URLs:
   `https://raw.githubusercontent.com/grabartley/minecraft-dogs-unleashed/<branch>/docs/qa/<feature>/<name>.png`
   placed next to the paragraph each illustrates, then `gh pr edit <num> --body-file ...`.
4. If a later run replaces the screenshots, overwrite the same file names and update the body only
   if the prose changed; the raw URLs track the branch head.

## Checklist Before Handoff to Manual QA

- [ ] Driver ran to `[QA] DONE` with zero `[QA] ERROR`
- [ ] Every screenshot visually verified by reading the PNG, at multiple GUI scales for rendering
- [ ] Server-side state assertions logged and correct (e.g. command/DataTracker values after a click)
- [ ] Temp driver + initializer hook reverted; `git status` shows only intended files
- [ ] Evidence committed under `docs/qa/<feature>/` and embedded in the PR body

## Related Skills

- `build` — requires this skill before manual QA handoff
- `run-game-client` — plain manual launch, used when a human is driving
- `worktree` — provides the isolated `run/` directory this skill relies on
