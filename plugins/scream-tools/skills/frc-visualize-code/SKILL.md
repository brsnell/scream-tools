---
name: frc-visualize-code
description: Maps an FRC (WPILib) Java robot project starting from Robot.java — classes, subsystems/mechanisms, commands, libraries, controller button bindings and default commands — and writes an interactive HTML page plus SVG diagrams to docs/projVisu. Supports Commands v2 and v3, 2026 (edu.wpi.first) and 2027 (org.wpilib) namespaces. Runs offline on the JDK WPILib installs; reads source only, never builds or runs robot code.
argument-hint: "[project-dir] [--out DIR] [--root CLASS]"
disable-model-invocation: true
allowed-tools: Bash(${CLAUDE_SKILL_DIR}/scripts/run.sh *), Read, Glob
---

# frc-visualize-code

Part of **SCREAM Tools**. Produces a map of a robot project's code, from the perspective of
`Robot.java`:

- `docs/projVisu/index.html` — interactive page: **Structure** (class diagram, click a class for its
  fields, command factories, links and the buttons that reach it), **Controls** (every trigger
  binding and default command, grouped by controller), **About** (project facts and warnings).
  Self-contained: no network, no CDN; open it straight from disk.
- `docs/projVisu/structure.svg` — architecture view diagram (also embedded in the page).
- `docs/projVisu/structure-full.svg` — every class reachable from `Robot`, including constants.
- `docs/projVisu/structure.json` — the graph the page is built from.

The work is done by a deterministic program (`scripts/FrcVisualize.java`), not by reading the code
yourself, so the same code always gives the same map. Your job is to run it and report on it
accurately.

## Steps

1. **Pick the project folder.** Use the first argument in `$ARGUMENTS` if it is a folder; otherwise
   the current working directory. It should contain `build.gradle` (GradleRIO) and a
   `src/main/java` tree. If it does not look like a robot project, say so and ask before running.
   Pass any `--out` or `--root` options from `$ARGUMENTS` through unchanged. The default output
   folder is `<project>/docs/projVisu`.

2. **Run the tool** (it finds the WPILib JDK by itself; takes a few seconds):

   ```bash
   ${CLAUDE_SKILL_DIR}/scripts/run.sh --project "<project-folder>" [--out DIR] [--root CLASS]
   ```

   Exit codes: `0` done · `2` bad arguments or no Java sources · `3` no robot entry class found
   (ask the user which class is the entry point and rerun with `--root`) · `4` no JDK 17+ found
   (tell the user to install WPILib or set `JAVA_HOME`; do not try to install a JDK yourself).

3. **Report what it found.** Base the report on the tool's printed summary and on
   `structure.json` — read the JSON, do not re-derive anything from the robot source. Include:
   - framework (Commands v2 / v3 / both), WPILib version and namespace, team number if found;
   - counts: classes reachable, mechanisms, commands, controllers, bindings, default commands;
   - every entry in `warnings`, verbatim;
   - from `bindings`, anything a driver or programmer would want to know, stated as an
     observation, not a verdict:
     - the same controller and trigger bound more than once;
     - bindings whose `mechanisms` list is empty (the tool could not tell what the command drives);
     - mechanisms (category `subsystem`) that no binding or default command refers to.

   Say plainly that the `mechanisms` lists are a static estimate — the subsystems a command
   expression refers to, following calls a few levels deep — **not** the commands' declared
   requirements, and that anything decided at runtime (choosers, reflection, generated code) is
   invisible to the tool.

4. **Finish.** Give the path to `index.html`. The output is meant to be committed with the
   project, but **do not commit, push or open a pull request unless the user asks.**

## Rules

- Never edit robot source code as part of this skill. It only writes into the output folder.
- Never run a Gradle task (build, simulate or deploy) for this skill; it does not need one.
- If the tool's output and the source seem to disagree, say so and point at the file and line
  (`file:line` is in the JSON) rather than correcting the map by hand.
- Do not paste the whole JSON into the conversation; summarise it.
