# SCREAM AI Tools

Free, open-source [Claude Code](https://code.claude.com) tools for FRC robot programming teams.
Use them, fork them, change them — they are MIT licensed.

| Tool | Slash command | What it does |
| --- | --- | --- |
| **frc-visualize-code** | `/frc-visualize-code` | Maps a WPILib Java robot project, starting from `Robot.java`, into an interactive HTML page and SVG diagrams: classes, mechanisms, commands, libraries, controller bindings and default commands. |

---

## frc-visualize-code

![Structure view: the class diagram with a mechanism selected](docs/images/structure.png)

![Controls view: every button binding, grouped by controller](docs/images/controls.png)

*Screenshots are of the small example robot in `tests/fixtures/v2-command-robot`.*

Run it in your robot project and it writes `docs/projVisu/`:

| File | Contents |
| --- | --- |
| `index.html` | Interactive page. **Structure**: class diagram; click a class for its fields, command factories, links, and the buttons that reach it. **Controls**: every trigger binding and default command, grouped by controller, with the mechanisms each command refers to. **About**: project facts and warnings. |
| `structure.svg` | Architecture diagram (constants and minor references hidden). |
| `structure-full.svg` | Every class reachable from `Robot`, including constants and library code. |
| `structure.json` | The extracted graph, if you want to build on it. |

The page is one self-contained file. It loads nothing from the internet, so it works offline in the
pits, and it follows light/dark mode.

### What it understands

- **Commands v2 and Commands v3**, including both on one robot.
- **2026 and earlier** (`edu.wpi.first.*`) and **2027** (`org.wpilib.*`) package names.
- Subsystems: anything that extends `SubsystemBase` or implements `Subsystem` (v2) or
  `Mechanism` (v3), directly or through a base class whose source is in the repository (for
  example a vendored library like SCREAMLib's `TalonFXSubsystem`).
- Bindings: `onTrue`, `whileTrue`, `toggleOnTrue`, `onFalse`, `whileFalse`, `onChange` (and the
  pre-2023 names) on `Command*Controller` / `CommandGamepad` / `CommandJoystick` buttons,
  `JoystickButton`, `POVButton`, `new Trigger(...)`, and combinations with `.and()`, `.or()`,
  `.negate()`. Controller ports are resolved through constants.
- Default commands: every `setDefaultCommand(...)`.
- For each bound command, the **mechanisms it refers to**, following method calls a few levels deep
  (e.g. `superstructure.shoot()` → the flywheel, indexer and drive it uses).

### How it works, and its limits

It is a single Java file (`plugins/scream-ai-tools/skills/frc-visualize-code/scripts/FrcVisualize.java`)
that reads your source with the JDK's own Java parser. **No downloads, no dependencies, no build**:
it runs on the JDK the WPILib installer already put on your laptop, and it works even when the
project does not compile. The same code always produces the same map, so a committed map only
changes when the code does.

It reads source text only. It never compiles or runs robot code. So:

- The "mechanisms" for each binding are an **estimate** from the source — not the command's
  declared requirements. Check the command before relying on it.
- Anything decided at runtime (auto choosers, reflection, generated code) is not visible.
- Libraries you pull in as jars (WPILib, vendor libraries) are not drawn; libraries whose source is
  in your repository are.

## Install

### As a Claude Code plugin (recommended)

In Claude Code:

```
/plugin marketplace add brsnell/scream-ai-tools
/plugin install scream-ai-tools@scream-ai-tools
```

Then, in your robot project:

```
/scream-ai-tools:frc-visualize-code
```

Plugin skills are namespaced by the plugin name. Claude Code may also accept the short form
`/frc-visualize-code` when nothing else has that name.

### As a standalone skill in one project

Copy `plugins/scream-ai-tools/skills/frc-visualize-code/` into your robot project at
`.claude/skills/frc-visualize-code/`. It then runs as `/frc-visualize-code`.

### Without Claude Code

```bash
# from your robot project folder
path/to/scream-ai-tools/plugins/scream-ai-tools/skills/frc-visualize-code/scripts/run.sh
# or directly, with any JDK 17+
java path/to/FrcVisualize.java --project . --out docs/projVisu
```

Options: `--project DIR` (default: current folder), `--out DIR` (default:
`<project>/docs/projVisu`), `--root CLASS` (default: the class extending `TimedRobot`, or `Robot`).

### Requirements

- A **JDK 17 or newer**. The launcher looks for the WPILib JDK first (`~/wpilib/<year>/jdk`,
  `~/.local/share/wpilib/<year>/jdk`, `C:\Users\Public\wpilib\<year>\jdk`), then `JAVA_HOME`, then
  `java` on your `PATH`. Set `FRC_VISUALIZE_JAVA` to force a specific `java`.
- Parse with a JDK at least as new as the Java your robot code uses (WPILib's own JDK always is).
- On Windows, run through Git Bash. Claude Code on Windows already uses it.

## Develop

```bash
tests/run-tests.sh
```

This runs the tool on the two fixture robots in `tests/fixtures/` (a Commands v2 robot on 2026
package names and a Commands v3 robot on 2027 package names) and checks what it extracts. It also
checks that output is identical across runs and that the page loads nothing from the network. CI
runs the same tests on Linux, macOS and Windows with JDK 17, 21 and 25.

Contributions are welcome. If the tool misreads your team's code, a pull request that adds a small
fixture reproducing it is the most useful thing you can send.

## Fork it

The license is MIT: fork it and make it yours. To publish your fork as its own plugin marketplace,
change `name` and `owner` in `.claude-plugin/marketplace.json`. Teams then install it with
`/plugin marketplace add <your-org>/<your-repo>`.

## License

[MIT](LICENSE)
