# FRC teams on GitHub: AI coding skills survey

*Compiled November 2026 from GitHub code and repository search.*

This survey looks at how FIRST Robotics Competition (FRC) teams, and some FTC teams, set up AI coding
agents in their public GitHub repositories: which agent skills they write, what those skills cover, and
which robot libraries they describe to the agent. The second half lists FRC-specific skills, plugins and
MCP servers that anyone can reuse.

## How this was gathered

- GitHub code search for `SKILL.md`, `CLAUDE.md`, `AGENTS.md`, `GEMINI.md`, `.github/copilot-instructions.md`
  and `.cursor/rules` files that mention WPILib, FRC, swerve, Limelight, PhotonVision, AdvantageKit, REVLib,
  `.wpilog` and similar terms. About 2,500 candidate files were downloaded.
- A file was kept only if it mentioned at least two FRC/FTC-specific terms (WPILib, roboRIO, SystemCore,
  AdvantageKit, Phoenix 6, PathPlanner, FTCLib and so on). That filter removes the many unrelated matches,
  such as legal skills that use "FRC" or "FTC" as abbreviations.
- GitHub repository search for FRC/WPILib projects with "skills", "claude", "agent", "mcp" and "copilot".
- Limits: GitHub code search returns at most 1,000 results per query and only indexes default branches.
  Library counts come from keyword matches in the AI config files, not from each repository's build files.
  The results are a large sample of public repositories, not a census.

## Summary

- **278 repositories** from FRC/FTC teams and FRC tool authors have AI-agent configuration. For scale,
  GitHub lists over 2,000 repositories tagged `frc`.
- **Most teams use a single instruction file, not skills.** Of the 278:
  | Config type | Repos |
  | --- | --- |
  | `CLAUDE.md` | 110 |
  | `AGENTS.md` (Codex, Cursor and others) | 66 |
  | GitHub Copilot instructions | 62 |
  | Agent skills (`SKILL.md`) | 53 |
  | Cursor rules (`.mdc`) | 8 |
  | `GEMINI.md` | 7 |
- **About 147 distinct FRC/FTC skills** were found in team repositories (Appendix B), plus several hundred
  more in dedicated skill packs (see [FRC-specific skills you can reuse](#frc-specific-skills-you-can-reuse)).
- Almost every configured repository is from the **2026 (REBUILT) season or later**. Many teams are
  already writing skills for **WPILib 2027, SystemCore and Commands v3**.

### What teams' skills do

Rough grouping of the skills found in team repositories, by name and description:

| Theme | Approx. skills | Examples |
| --- | --- | --- |
| Log analysis and match debriefs | ~27 | `analyze-wpilog` (Teams 581, 1923, 5419, 10183), `debug-match-log` (guerin-robotics), `match-debrief`, `churro-debug`, `debug-hoot`, `robot-log-upload` (Spectrum 3847) |
| Motor controllers and vendor APIs | ~20 | `ctre-phoenix6`, `revlib`, `neo-spark-motors`, `cim-talon-motors`, `add-device`, `frc-robot-safety-can` |
| Build, deploy and pre-match checks | ~12 | `build-and-deploy`, `frc-pre-deploy`, `check-releases`, `wpilib-prematch` |
| API and version freshness | ~10 | `current-wpilib-2027`, `wpilib-yearly-changes`, `api-check`, `port-from-2026`, `wpilib-best-practices` |
| Code review and teaching | ~10 | `review-my-code` (2713), `cr` (Demacia 5635), `teaching` (Hemlock 5712), `frc-student-mentor-persona` (robototes) |
| Mechanism scaffolding | ~8 | `add-a-mechanism`, `new-mechanism`, `frc-motor-groups` |
| Simulation and testing | ~8 | `run-sim`, `simulate`, `powerlib-sim` (9410), `frc-simulation-validation` |
| Vision, autonomous and paths | ~10 | `frc-code-vision`, `limelight3a`, `mirror-auto` (3316), `pedropathing`, `roadrunner` |
| PID and feedforward tuning | ~4 | `pid-tune`, `wpilib-tune`, `marslib-control-theory` |
| CAD, scouting and strategy | ~8 | `frc-design-lib`, `frcstack`, `game-info`, `ftc-decode` |

**Patterns worth copying:**

1. **Log analysis is the most common skill.** Teams want to hand an agent a `.wpilog` or `.hoot` file and
   get back what went wrong and what to fix. Several teams reuse the same `analyze-wpilog` skill from
   [jonahsnider/wpilog-parser](https://github.com/jonahsnider/wpilog-parser).
2. **Skills that prevent outdated API answers.** Models often suggest last year's WPILib or vendor API.
   Teams add skills that check APIs before use (`api-check`, `current-wpilib-2027`, `check-releases`) or
   that explain changes between seasons (`wpilib-yearly-changes`).
3. **Repository-specific procedures.** The most detailed team skills describe how *their* code works:
   "add a mechanism by copying the Arm", "use our StateBaseMechanism", "run our headless sim". Good
   examples are [Hemlock5712/2027-Template](https://github.com/Hemlock5712/2027-Template/tree/HEAD/.claude/skills)
   and [Demacia5635/utils2027--Alpha7](https://github.com/Demacia5635/utils2027--Alpha7/tree/HEAD/.claude/skills).
4. **Teaching mode.** Many teams tell the agent to explain things to students instead of writing the code
   for them (`teaching`, `review-my-code`, `frc-student-mentor-persona`, `teach-me`).
5. **Safety rules.** Skills and instruction files require current limits, soft limits, brake/coast settings
   and CAN checks, and tell the agent never to deploy without a person present.

### Robot libraries teams describe to their agents

Number of the 278 repositories whose AI config files mention each library or topic:

| Library or topic | Repos |
| --- | --- |
| CTRE Phoenix 6 (TalonFX, Kraken, CANcoder) | 162 |
| Command-based (Commands v2) | 142 |
| Swerve drive | 136 |
| AdvantageKit | 97 |
| PathPlanner | 86 |
| Limelight | 81 |
| PhotonVision | 72 |
| REVLib (SPARK MAX/Flex, NEO) | 72 |
| Spotless formatting | 53 |
| C++ | 53 |
| AdvantageScope | 52 |
| WPILib 2027 / SystemCore / Commands v3 | 40 |
| SysId | 40 |
| Choreo | 31 |
| maple-sim | 24 |
| RobotPy (Python) | 23 |
| Elastic dashboard | 23 |
| YAGSL | 10 |
| QuestNav | 10 |
| Kotlin | 10 |

Simulation (167 repos) and unit tests (92) also come up often. Many teams want the agent to check its
work in simulation before anything goes on the robot.

## FRC-specific skills you can reuse

These repositories exist to share FRC/FTC skills or agent tooling with other teams.

### Skill packs and plugins

| Repository | What it is | Skills it includes |
| --- | --- | --- |
| [j0n5m1th/frc-agent-toolkit](https://github.com/j0n5m1th/frc-agent-toolkit) | Claude Code plugin plus MCP servers for debugging robots: live NetworkTables, match logs, simulation, SystemCore and CTRE devices. | `frc-start-here`, `frc-explain`, `wpilib-build`, `wpilib-deploy`, `wpilib-live`, `wpilib-log-analysis`, `wpilib-triage`, `wpilib-tune`, `wpilib-profile`, `wpilib-prematch`, `wpilib-project`, `ctre-convert`, `ctre-diagnose`, `ctre-canivore`, `systemcore`, `systemcore-live`, `systemcore-restart`, `systemcore-provision` |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot/tree/HEAD/packs/skills) | A large general skill pack (Team 6925) with an extensive FRC code and design section. | `frc-code-start-here`, `frc-code-project-setup`, `frc-code-command-based`, `frc-code-mechanisms`, `frc-code-motor-controllers`, `frc-code-swerve`, `frc-code-vision`, `frc-code-autonomous`, `frc-code-pid-feedforward`, `frc-code-units-constants`, `frc-code-logging-replay`, `frc-code-log-analysis`, `frc-code-sim-testing`, `frc-code-build-deploy`, `frc-code-can-power`, `frc-code-debugging`, `frc-code-driver-controls`, `frc-code-review-checklist`, `frc-code-competition-day`, `frc-code-team-git`, `frc-code-2027-preview`, `frc-design-*` (process, motors, swerve, elevators, climbers, pneumatics, electrical layout, bumpers/CG, structure, Onshape, rules, strategy, review), `java-kotlin-wpilib` |
| [ehartye/agent-frc](https://github.com/ehartye/agent-frc) | Education and engineering skills that adjust to the current season. | `frc-setup`, `frc-season`, `frc-season-meta`, `frc-modes`, `frc-java`, `frc-swerve`, `frc-vision`, `frc-project-setup`, `frc-troubleshoot`, `frc-conceptual-design`, `frc-preliminary-design`, `frc-detailed-design`, `frc-mechanism-hardening`, `frc-mechanical-readiness`, `frc-case-study`, `frc-strategic-narrative`, `frc-robot-highlights`, `frc-start-over` |
| [unlabs-llc/unlabs-ai-toolkit-frc-2027](https://github.com/unlabs-llc/unlabs-ai-toolkit-frc-2027) | Claude Code skills, agents and subagents for WPILib 2027 teams. | `current-wpilib-2027`, `current-claude-models`, `frc-pre-deploy`, `frc-behavior-first` |
| [FRC2713/skills](https://github.com/FRC2713/skills) | Team 2713's shared Claude Code plugin. | `review-my-code`, `teach-me`, `create-hawk-app` |
| [Feramirr/frc-claude-config](https://github.com/Feramirr/frc-claude-config) | Team 1868 (Space Cookies) shared Claude Code config: rules, agents, slash commands (`/frc-review`, `/frc-premerge`, `/frc-safety-audit`) and skills. | CTRE Phoenix 6, command-based patterns, vision |
| [FRC1555/frc-skill](https://github.com/FRC1555/frc-skill) | One skill for any agent: WPILib code, "won't move" debugging, DS logs, roboRIO SSH, Elastic dashboards. | single `SKILL.md` |
| [Filip-Kin/CTRE-Skill](https://github.com/Filip-Kin/CTRE-Skill) | Claude Code skill for CTRE Phoenix 6 Java. | `phoenix6-frc` |
| [DDguan2010/frc-code-skill](https://github.com/DDguan2010/frc-code-skill) | FRC Java skill with code style, architecture and workflow references. | single skill plus references |
| [Warren-Warbots/FRC-skills](https://github.com/Warren-Warbots/FRC-skills) | Reads and analyzes WPILib and hoot logs. | `FRCLogReader` |
| [bullbots/frc-claude-skills](https://github.com/bullbots/frc-claude-skills) | Skills to help Claude debug and write better robot code. | – |
| [jgermita/magic-prompts-frc](https://github.com/jgermita/magic-prompts-frc) | Prompts and skills for agents working on FRC code. | – |
| [FutureShock7130/AI-resources](https://github.com/FutureShock7130/AI-resources) | Guidelines and skill files for agentic FRC coding. | – |
| [sallyluenoa/FRClaudeSkills](https://github.com/sallyluenoa/FRClaudeSkills) | Personal FRC Claude Code skills, installed by symlink. | – |
| [thehomelessguy/MARSLib](https://github.com/thehomelessguy/MARSLib) | FRC library that ships agent skills for using it. | `marslib`, `marslib-mechanisms`, `marslib-swerve`, `marslib-vision`, `marslib-autonomous`, `marslib-statemachine`, `marslib-control-theory`, `marslib-telemetry`, `marslib-testing`, `marslib-diagnostics`, `marslib-network`, `marslib-math`, `marslib-ci` |
| [Hemlock5712/2027-Template](https://github.com/Hemlock5712/2027-Template) | WPILib 2027 robot template that includes Claude skills. | `robot-description`, `add-a-mechanism`, `build-and-deploy`, `run-sim`, `log-reading`, `game-info`, `teaching` |
| [brsnell/scream-tools](https://github.com/brsnell/scream-tools) | This repository. | `frc-visualize-code` |

### Simulation, logs and telemetry

| Repository | What it is |
| --- | --- |
| [rylero/ClaudeScope](https://github.com/rylero/ClaudeScope) | Skills (`scope`, `simulate`, `replay`) for analyzing `.wpilog` files, reading live NetworkTables, and replaying logs through robot code. Also used by [rylero/RobotLogBot](https://github.com/rylero/RobotLogBot), a Discord log-analysis bot. |
| [edanliahovetsky/wpilib-agent-tools](https://github.com/edanliahovetsky/wpilib-agent-tools) | CLI and skill bundle that lets agents improve robot code using simulation, NT4 recording and WPILOG analysis. |
| [jonahsnider/wpilog-parser](https://github.com/jonahsnider/wpilog-parser) | TypeScript WPILOG parser with the `analyze-wpilog` skill that several teams copy. |
| [Alphabots-8810/AlphaHarness](https://github.com/Alphabots-8810/AlphaHarness) | MCP server: read-only NT4 telemetry, step-response metrics, offline WPILOG analysis, and PID gain writes that a person must approve. |
| [TripleHelixProgramming/wpilog-mcp](https://github.com/TripleHelixProgramming/wpilog-mcp) | MCP server for asking questions about WPILib and REV logs, with checks against overconfident answers. |
| [GabrielNakamoto/artemis](https://github.com/GabrielNakamoto/artemis) | Minimal MCP server for NT4 and `.wpilog`. |
| [jlmcmchl/datalog-mcp-server](https://github.com/jlmcmchl/datalog-mcp-server) | MCP server for FRC datalogs. |
| [Mzzj114/nt-mcp-server](https://github.com/Mzzj114/nt-mcp-server) | MCP server for reading, writing and monitoring NetworkTables (NT4). |
| [Srjohnson589/wpilib-xrp-mcp](https://github.com/Srjohnson589/wpilib-xrp-mcp) | MCP server for driving an XRP robot through WPILib HALSim. |
| [horner/open-ds.ai](https://github.com/horner/open-ds.ai) | Cross-platform Driver Station with MCP and AI features. |
| [FRC-Flux-Robotics/FluxProcessor](https://github.com/FRC-Flux-Robotics/FluxProcessor) | Team 10413's offline match-log analyzer and replayer, with an MCP server. |

### Documentation and knowledge MCP servers

| Repository | What it is |
| --- | --- |
| [ramalamadingdong/frc-rag-mcpserver](https://github.com/ramalamadingdong/frc-rag-mcpserver) | MCP server with all of the WPILib documentation. |
| [ramalamadingdong/agentic-csa](https://github.com/ramalamadingdong/agentic-csa) | "Agentic CSA" (Control System Advisor) MCP server. |
| [fikretyukselit/frc-mcp](https://github.com/fikretyukselit/frc-mcp) | MCP server with WPILib docs, API references and vendor documentation. |
| [DugboTek/FRCDocsMCP](https://github.com/DugboTek/FRCDocsMCP) | MCP server for current CTRE, AdvantageScope and WPILib docs. |
| [Project516/frc-mcp-server](https://github.com/Project516/frc-mcp-server) | MCP server for searching robot code, WPILib docs and vendor references. |
| [rylero/chiefdelphi-mcp](https://github.com/rylero/chiefdelphi-mcp) | Read-only MCP server that uses Chief Delphi as an FRC design knowledge base. |
| [o-bots7160/AdvantageScope-mcp](https://github.com/o-bots7160/AdvantageScope-mcp) / [o-bots7160/elastic_dashboard-mcp](https://github.com/o-bots7160/elastic_dashboard-mcp) | MCP servers for building AdvantageScope and Elastic layout files. |
| [ericg4/robostats-mcp-cloudflare](https://github.com/ericg4/robostats-mcp-cloudflare) | Statbotics data for Claude and Cursor. |
| [ag2-mcp-servers/frc-events](https://github.com/ag2-mcp-servers/frc-events) | Generated MCP server for the FRC Events API. |

### CAD, design, scouting and strategy

| Repository | What it is |
| --- | --- |
| [AndeluTongXue/FRC-Onshape-Skills](https://github.com/AndeluTongXue/FRC-Onshape-Skills) | Tree of Onshape CAD skills rooted at `frc-cad-foundations`, following FRCDesign.org. Available as a Claude plugin. |
| [amorygalili/frc-design-skills](https://github.com/amorygalili/frc-design-skills) | `frc-design-lib` (COTS FRC part search and download) plus CAD, DXF, URDF, G-code, Bambu and Onshape FeatureScript skills. |
| [AkarshSharma2012/FRCstack-skill](https://github.com/AkarshSharma2012/FRCstack-skill) | Onshape CAD with real component libraries, and scouting exported to Excel. |
| [gfleming1992/CadCove-FRC](https://github.com/gfleming1992/CadCove-FRC) | CadQuery CAD, robot code and simulation for VS Code. Skills: `frc-workflow`, `frc-season-rules`, `frc-cad-mechanisms`, `frc-ctre-controls`, `frc-vision`, `frc-simulation-validation`. |
| [tech-support03/Claude4FRC](https://github.com/tech-support03/Claude4FRC) | MCP server connecting Claude to Onshape for robot design. |
| [ARES-23247/robotics-cad-workflow](https://github.com/ARES-23247/robotics-cad-workflow) | CAD workflow with a 2,588-part FTC/FRC COTS library and an MCP server. |
| [safiqsindha/TheEngine](https://github.com/safiqsindha/TheEngine) | Strategy and design skills: `frc-kickoff-assistant`, `frc-game-analyzer`, `frc-scouting-setup`, `frc-pre-event-report`, `frc-alliance-advisor`, `frc-elevator-designer`. |
| [dougdliu/FRC_AI_Mega_Project](https://github.com/dougdliu/FRC_AI_Mega_Project) | Skills and agents that take a season's game manual and build FRC projects from it, including `game_manual_decomposition`. |

### FTC

| Repository | What it is |
| --- | --- |
| [ncssm-robotics/ftc-claude](https://github.com/ncssm-robotics/ftc-claude) | FTC Claude Code plugin marketplace: `decode`, `ftclib`, `limelight`, `roadrunner`, `skill-builder`. |
| [N0v4ont0p/Refract](https://github.com/N0v4ont0p/Refract) | FTC code generation that checks the team's robot configuration first (`ftc-construct`, `ftc-team-config`, `ftc-hardware-lookup`). |
| [ftc8569/2025-Decode](https://github.com/ftc8569/2025-Decode/tree/HEAD/.claude/skills) | Team 8569's skills: `ftc-decode`, `ftclib`, `limelight3a`, `pedropathing`. |
| [IamCoder18/synapse](https://github.com/IamCoder18/synapse) | `synapse-pubsub-ftc` skill for the Synapse pub/sub bus. |

## Team repositories worth studying

- [Hemlock5712/2027-Template](https://github.com/Hemlock5712/2027-Template): the most complete per-repository
  skill set, covering description, mechanisms, build/deploy, simulation, logs, game rules and teaching.
- [Demacia5635/utils2027--Alpha7](https://github.com/Demacia5635/utils2027--Alpha7): skills for adding devices
  and mechanisms, porting from 2026 to 2027, and code review against the team's checklist.
- [CohenHill/WPILIB27-testing-repo](https://github.com/CohenHill/WPILIB27-testing-repo): `api-check`,
  `new-mechanism` and `match-debrief`, plus matching AGENTS.md and Copilot instructions.
- [robototes/REBUILT2026](https://github.com/robototes/REBUILT2026): review skills for architecture, CAN
  safety, telemetry/units and mentor tone.
- [team581/bolt](https://github.com/team581/bolt): `access-fetch-logs`, `analyze-wpilog` and `debug-hoot` for
  working through logs stored in the cloud.
- [guerin-robotics/Rebuilt2026](https://github.com/guerin-robotics/Rebuilt2026): a `pid-tune` loop through
  simulation, a `debug-match-log` skill, plus ClaudeScope and wpilib-agent-tools vendored in.
- [bolme/Parade-Bot-2024](https://github.com/bolme/Parade-Bot-2024): motor skills for CIM/Talon and NEO/SPARK
  hardware, `frc-motor-groups` and `wpilib-yearly-changes`.
- [Spectrum3847/2026-Spectrum](https://github.com/Spectrum3847/2026-Spectrum): `robot-log-upload` pulls logs
  and Limelight clips off the robot.
- [yeti-robotics/rebuilt-2026](https://github.com/yeti-robotics/rebuilt-2026): `advantagekit` and
  `wpilib-best-practices`.

## Ideas for scream-tools

Gaps compared with what teams are building by hand:

- **Log triage**: the most common team skill, but no standard cross-team version exists yet.
- **API freshness check**: verify WPILib 2026/2027 and vendordep APIs against the project's `vendordeps/`
  versions before generating code.
- **Mechanism scaffolding** that reads the existing project (which `frc-visualize-code` already maps) and
  copies its conventions.
- **Pre-deploy / pre-match checklist** combining build, tests, a headless sim run and CAN/current-limit checks.

## Appendix A: Every repository found with AI-agent configuration

Detected libraries come from keyword matches in the AI config files (CLAUDE.md, AGENTS.md, skills and so on), not from the build files, so treat them as approximate.

| Repository | AI config | Skills | Libraries and tools mentioned |
| --- | --- | --- | --- |
| [0908869905/frc-expense-money](https://github.com/0908869905/frc-expense-money) | CLAUDE.md | – | – |
| [1307Robosaints/1307Robosaints.github.io](https://github.com/1307Robosaints/1307Robosaints.github.io) | CLAUDE.md | – | – |
| [2611-Vectors/robot-2027](https://github.com/2611-Vectors/robot-2027) | AGENTS.md | – | AdvantageKit, CTRE Phoenix 6, WPILib 2027/SystemCore, Commands v2, Swerve, Spotless/format |
| [2702rebels/recipes](https://github.com/2702rebels/recipes) | AGENTS.md | – | CTRE Phoenix 6, Swerve |
| [6238/2026-Robot-public](https://github.com/6238/2026-Robot-public) | CLAUDE.md, Copilot instructions | – | AdvantageKit, CTRE Phoenix 6, PathPlanner, PhotonVision, MapleSim, Commands v2, Swerve, Spotless/format |
| [6391-Ursuline-Bearbotics/2026-6391-Rebuilt](https://github.com/6391-Ursuline-Bearbotics/2026-6391-Rebuilt) | CLAUDE.md | – | AdvantageKit, CTRE Phoenix 6, Choreo, PhotonVision, Limelight, QuestNav, Commands v2, Swerve, Python/RobotPy, SysId, Spotless/format |
| [6391-Ursuline-Bearbotics/2027-6391-Rebuilt-V3](https://github.com/6391-Ursuline-Bearbotics/2027-6391-Rebuilt-V3) | AGENTS.md | – | AdvantageKit, CTRE Phoenix 6, REVLib, Choreo, PhotonVision, Limelight, QuestNav, WPILib 2027/SystemCore, Commands v2, Swerve |
| [8810bot/2026_8810_10711](https://github.com/8810bot/2026_8810_10711) | AGENTS.md | – | AdvantageKit, CTRE Phoenix 6, PathPlanner, Choreo, Limelight, Commands v2, Swerve, Spotless/format |
| [ADARSH-hacker-2011/INVINCIBLE](https://github.com/ADARSH-hacker-2011/INVINCIBLE) | CLAUDE.md, GEMINI.md | – | AdvantageKit, AdvantageScope, CTRE Phoenix 6, REVLib, PathPlanner, Choreo, PhotonVision, MapleSim, Swerve, SysId |
| [ADARSH-hacker-2011/frc-inspiration](https://github.com/ADARSH-hacker-2011/frc-inspiration) | CLAUDE.md, GEMINI.md | – | AdvantageKit, AdvantageScope, CTRE Phoenix 6, REVLib, PathPlanner, Choreo, PhotonVision, MapleSim, Swerve, SysId |
| [AaronZhenger/Multi-Unit-Dynamic-System](https://github.com/AaronZhenger/Multi-Unit-Dynamic-System) | Copilot instructions | – | Commands v2, C++ |
| [AaronZhenger/Offseason-Elevator](https://github.com/AaronZhenger/Offseason-Elevator) | Copilot instructions | – | Commands v2, C++ |
| [AdaTomruk/2026-compbot](https://github.com/AdaTomruk/2026-compbot) | Copilot instructions | – | CTRE Phoenix 6, PathPlanner, Limelight, YAGSL, Commands v2, Swerve, SysId |
| [Adelson-Robotics/10183-2025-rebuilt-frc-Summer-Scorcher](https://github.com/Adelson-Robotics/10183-2025-rebuilt-frc-Summer-Scorcher) | Skills | `analyze-wpilog` | – |
| [AkarshSharma2012/FRCstack-skill](https://github.com/AkarshSharma2012/FRCstack-skill) | Skills | `frcstack` | – |
| [Alphabots-8810/8810-2026-offseason](https://github.com/Alphabots-8810/8810-2026-offseason) | CLAUDE.md | – | AdvantageKit, AdvantageScope, CTRE Phoenix 6, PathPlanner, Choreo, PhotonVision, Limelight, MapleSim, Elastic, Commands v2, Swerve, SysId, Spotless/format |
| [Archimedes9522/team9522_2026](https://github.com/Archimedes9522/team9522_2026) | CLAUDE.md, Copilot instructions, GEMINI.md | – | AdvantageKit, AdvantageScope, REVLib, PathPlanner, PhotonVision, YAGSL, MapleSim, Commands v2, Swerve, SysId |
| [ArrowheadCyberhawks/RobotCode2026](https://github.com/ArrowheadCyberhawks/RobotCode2026) | Copilot instructions | – | CTRE Phoenix 6, REVLib, PhotonVision, Limelight, Commands v2, C++ |
| [Arthur225/regionbool](https://github.com/Arthur225/regionbool) | AGENTS.md | – | – |
| [Ben-Lauderbaugh/FRC-Copilot](https://github.com/Ben-Lauderbaugh/FRC-Copilot) | Copilot instructions | – | Commands v2 |
| [BioNanomics/10434-Northrop-Cybears](https://github.com/BioNanomics/10434-Northrop-Cybears) | Copilot instructions | – | REVLib, Commands v2 |
| [BioNanomics/F3iducial](https://github.com/BioNanomics/F3iducial) | Copilot instructions | – | – |
| [BioNanomics/refinery-forcefield](https://github.com/BioNanomics/refinery-forcefield) | Copilot instructions | – | AdvantageKit, PathPlanner, Choreo, Swerve, C++ |
| [BioNanomics/refinery-roborio-mcp](https://github.com/BioNanomics/refinery-roborio-mcp) | Copilot instructions | – | C++ |
| [Camo-Bots-4285/TrainerBoard-Motors](https://github.com/Camo-Bots-4285/TrainerBoard-Motors) | Skills | `ctre-phoenix6`, `revlib`, `wpilib-frc` | AdvantageKit, AdvantageScope, CTRE Phoenix 6, REVLib |
| [CarvalhoJeo/translation_ptbr_store_wpilib](https://github.com/CarvalhoJeo/translation_ptbr_store_wpilib) | CLAUDE.md | – | – |
| [CoderJT-Elite/CoderJT-Elite.github.io](https://github.com/CoderJT-Elite/CoderJT-Elite.github.io) | GEMINI.md | – | Limelight, MapleSim, Swerve |
| [CohenHill/WPILIB27-testing-repo](https://github.com/CohenHill/WPILIB27-testing-repo) | AGENTS.md, Copilot instructions, Skills | `api-check`, `match-debrief`, `new-mechanism` | AdvantageKit, AdvantageScope, CTRE Phoenix 6, PathPlanner, PhotonVision, Elastic, WPILib 2027/SystemCore, Commands v2, Spotless/format |
| [CyberCoyotes/2026-Rebuilt](https://github.com/CyberCoyotes/2026-Rebuilt) | CLAUDE.md | – | AdvantageKit, CTRE Phoenix 6, Choreo, Limelight, Elastic, Swerve |
| [Demacia5635/utils2027--Alpha7](https://github.com/Demacia5635/utils2027--Alpha7) | CLAUDE.md, Skills | `add-device`, `cr`, `new-mechanism`, `port-from-2026` | CTRE Phoenix 6, REVLib, Limelight, Elastic, WPILib 2027/SystemCore, Commands v2, Swerve, SysId |
| [ExcaliburFRC/Rebuilt2026](https://github.com/ExcaliburFRC/Rebuilt2026) | AGENTS.md, CLAUDE.md | – | CTRE Phoenix 6, REVLib, PathPlanner, Limelight, Swerve, SysId |
| [FIRSTTeam3189/Robot2026](https://github.com/FIRSTTeam3189/Robot2026) | CLAUDE.md | – | CTRE Phoenix 6, REVLib, PathPlanner, PhotonVision, QuestNav, Swerve, C++, SysId |
| [FIRSTinMI/FTA-Buddy](https://github.com/FIRSTinMI/FTA-Buddy) | CLAUDE.md | – | CTRE Phoenix 6, Limelight, WPILib 2027/SystemCore |
| [FRC-8557-Conquera/Frc2026autoaimtry](https://github.com/FRC-8557-Conquera/Frc2026autoaimtry) | CLAUDE.md | – | AdvantageKit, CTRE Phoenix 6, REVLib, PathPlanner, Limelight, YAGSL, Commands v2, Swerve |
| [FRC-Flux-Robotics/New_Robot](https://github.com/FRC-Flux-Robotics/New_Robot) | AGENTS.md | – | CTRE Phoenix 6, PhotonVision, Swerve |
| [FRC-Hammond-71/Robot2026](https://github.com/FRC-Hammond-71/Robot2026) | CLAUDE.md | – | CTRE Phoenix 6, REVLib, PathPlanner, Limelight, Commands v2, Swerve |
| [FRC-Team-1405/2027Robot](https://github.com/FRC-Team-1405/2027Robot) | CLAUDE.md | – | AdvantageKit, CTRE Phoenix 6, PathPlanner, PhotonVision, Commands v2, Swerve |
| [FRC-Team-4143/scriptum-lessons](https://github.com/FRC-Team-4143/scriptum-lessons) | CLAUDE.md | – | – |
| [FRC-Team-5669-Techmen/2026-5669-Official](https://github.com/FRC-Team-5669-Techmen/2026-5669-Official) | CLAUDE.md | – | CTRE Phoenix 6, PathPlanner, PhotonVision, Limelight, MapleSim, Elastic, Commands v2, Swerve |
| [FRC1466/2026RobotCode](https://github.com/FRC1466/2026RobotCode) | Copilot instructions | – | AdvantageKit, CTRE Phoenix 6, PathPlanner, Choreo, PhotonVision, Limelight, Commands v2, Swerve, Spotless/format |
| [FRC1884/Preseason2027](https://github.com/FRC1884/Preseason2027) | AGENTS.md | – | – |
| [FRC2713/skills](https://github.com/FRC2713/skills) | Skills | `review-my-code` | – |
| [FRC3005/reefscape-offseason-2025-python-simple](https://github.com/FRC3005/reefscape-offseason-2025-python-simple) | AGENTS.md | – | Commands v2, Python/RobotPy |
| [FRC3950/Rebuilt26](https://github.com/FRC3950/Rebuilt26) | AGENTS.md, Copilot instructions | – | AdvantageScope, CTRE Phoenix 6, REVLib, PathPlanner, Choreo, Limelight, Commands v2, Swerve |
| [FRC4607/comp-bot2-2026](https://github.com/FRC4607/comp-bot2-2026) | CLAUDE.md | – | CTRE Phoenix 6, PathPlanner, Limelight, Commands v2, Swerve |
| [FRC5010/BasicRobotLessons](https://github.com/FRC5010/BasicRobotLessons) | CLAUDE.md | – | AdvantageKit, AdvantageScope, CTRE Phoenix 6, PathPlanner, PhotonVision, Limelight, MapleSim, Elastic, WPILib 2027/SystemCore, Commands v2, Swerve, C++, SysId |
| [FRC5190/2026CompetitionSeason](https://github.com/FRC5190/2026CompetitionSeason) | AGENTS.md | – | AdvantageKit, CTRE Phoenix 6, REVLib, PathPlanner, Limelight, YAGSL, Elastic, Swerve |
| [FRC5827/2026Public](https://github.com/FRC5827/2026Public) | AGENTS.md | – | AdvantageKit, CTRE Phoenix 6, Spotless/format |
| [FRC7660/robot-2026](https://github.com/FRC7660/robot-2026) | AGENTS.md | – | – |
| [FRC9015/Umbra](https://github.com/FRC9015/Umbra) | AGENTS.md | – | C++ |
| [FRC9410/Robot-Library](https://github.com/FRC9410/Robot-Library) | Skills | `powerlib-sim` | AdvantageScope, CTRE Phoenix 6, REVLib, MapleSim, Swerve |
| [FRC999/ParadeBot-MechaREX](https://github.com/FRC999/ParadeBot-MechaREX) | Skills | `update-mecharams-demo-robot` | CTRE Phoenix 6, Commands v2, Swerve |
| [FRCTeam1073-TheForceTeam/viper](https://github.com/FRCTeam1073-TheForceTeam/viper) | Skills | `viper-new-season`, `viper-overview` | – |
| [FRCTeam1987/Robot2026](https://github.com/FRCTeam1987/Robot2026) | Copilot instructions | – | CTRE Phoenix 6, REVLib, PathPlanner, Limelight, Commands v2 |
| [FRCTeam1987/Robot2026_SYSCORE_A2](https://github.com/FRCTeam1987/Robot2026_SYSCORE_A2) | Copilot instructions | – | CTRE Phoenix 6, REVLib, PathPlanner, Limelight, Commands v2 |
| [FRCTeam31/2026_competition_robot](https://github.com/FRCTeam31/2026_competition_robot) | Copilot instructions | – | AdvantageKit, CTRE Phoenix 6, REVLib, PathPlanner, Elastic, Commands v2, Swerve |
| [FRCTeam3255/2026_Offbot_Code](https://github.com/FRCTeam3255/2026_Offbot_Code) | Copilot instructions | – | CTRE Phoenix 6, Choreo, Commands v2 |
| [FRCTeam3255/Wiki](https://github.com/FRCTeam3255/Wiki) | Copilot instructions | – | CTRE Phoenix 6 |
| [FRCTeam360/RainMaker26](https://github.com/FRCTeam360/RainMaker26) | AGENTS.md, CLAUDE.md, Copilot instructions, Skills | `new-doc` | AdvantageKit, CTRE Phoenix 6, REVLib, PathPlanner, Commands v2, Spotless/format |
| [FROG3160/2026-rebuilt](https://github.com/FROG3160/2026-rebuilt) | GEMINI.md, Skills | `frog-robotpy` | AdvantageScope, CTRE Phoenix 6, PathPlanner, PhotonVision, Commands v2, Swerve, Python/RobotPy |
| [Filip-Kin/CTRE-Skill](https://github.com/Filip-Kin/CTRE-Skill) | Skills | `phoenix6-frc` | CTRE Phoenix 6, REVLib, PathPlanner, Choreo, Limelight, Commands v2, Swerve, SysId |
| [First8612/2026-vision-sim](https://github.com/First8612/2026-vision-sim) | Copilot instructions | – | Commands v2 |
| [GOAT-8092/8092-2026](https://github.com/GOAT-8092/8092-2026) | AGENTS.md, CLAUDE.md | – | REVLib, PathPlanner, Limelight, Commands v2 |
| [GOAT-8092/8092.tr](https://github.com/GOAT-8092/8092.tr) | CLAUDE.md | – | Limelight, Swerve |
| [GOAT-8092/robot-2025](https://github.com/GOAT-8092/robot-2025) | Cursor rules | – | Limelight |
| [Galaxia5987/offseason-2026-charged-up](https://github.com/Galaxia5987/offseason-2026-charged-up) | Copilot instructions | – | AdvantageKit, CTRE Phoenix 6, Kotlin |
| [Galaxia5987/recode-2026](https://github.com/Galaxia5987/recode-2026) | Copilot instructions | – | AdvantageKit, CTRE Phoenix 6, Kotlin |
| [Gears-and-Buccaneers/2026](https://github.com/Gears-and-Buccaneers/2026) | Copilot instructions | – | CTRE Phoenix 6, Choreo, PhotonVision, Commands v2, Swerve, C++, Python/RobotPy |
| [Gh0stlyKn1ght/YAGSL-TEST](https://github.com/Gh0stlyKn1ght/YAGSL-TEST) | AGENTS.md | – | AdvantageScope, REVLib, PathPlanner, PhotonVision, Limelight, Commands v2, Swerve |
| [Hemlock5712/2027-Template](https://github.com/Hemlock5712/2027-Template) | CLAUDE.md, Skills | `add-a-mechanism`, `build-and-deploy`, `game-info`, `log-reading`, `robot-description`, `run-sim`, `teaching` | AdvantageKit, AdvantageScope, CTRE Phoenix 6, PathPlanner, Choreo, PhotonVision, Limelight, MapleSim, WPILib 2027/SystemCore, Commands v2, Swerve, Python/RobotPy, Spotless/format |
| [Hemlock5712/Workshop-Site](https://github.com/Hemlock5712/Workshop-Site) | CLAUDE.md | – | AdvantageKit, PathPlanner, WPILib 2027/SystemCore, Commands v2 |
| [IamCoder18/synapse](https://github.com/IamCoder18/synapse) | Skills | `synapse-pubsub-ftc` | – |
| [IgniteRobotics/Phoenix-2026](https://github.com/IgniteRobotics/Phoenix-2026) | AGENTS.md | – | Spotless/format |
| [IgniteRobotics/flashpoint](https://github.com/IgniteRobotics/flashpoint) | AGENTS.md, CLAUDE.md | – | CTRE Phoenix 6, Python/RobotPy |
| [Iron-Panthers/OFFSEASON-2026](https://github.com/Iron-Panthers/OFFSEASON-2026) | CLAUDE.md | – | AdvantageKit, CTRE Phoenix 6, PathPlanner, PhotonVision, Elastic, Swerve, Spotless/format |
| [KHS-Robotics/Demonator15](https://github.com/KHS-Robotics/Demonator15) | AGENTS.md, Cursor rules | – | REVLib, PathPlanner, Commands v2, Swerve |
| [KawiMinn/frc-bot-2026-cxx](https://github.com/KawiMinn/frc-bot-2026-cxx) | Copilot instructions | – | CTRE Phoenix 6, PhotonVision, Commands v2, Swerve, C++ |
| [KrazyKactus/test-repo](https://github.com/KrazyKactus/test-repo) | AGENTS.md | – | AdvantageKit, AdvantageScope, CTRE Phoenix 6, PathPlanner, PhotonVision, Limelight, MapleSim, Commands v2, Swerve, SysId, Spotless/format |
| [LYT259/intake-template-2026](https://github.com/LYT259/intake-template-2026) | AGENTS.md | – | CTRE Phoenix 6, Commands v2 |
| [MDHSRobotics/Rebuilt-2026-Updated](https://github.com/MDHSRobotics/Rebuilt-2026-Updated) | AGENTS.md | – | AdvantageKit, CTRE Phoenix 6, PathPlanner, PhotonVision, Limelight, WPILib 2027/SystemCore, Commands v2, Swerve |
| [MHHSRobotics/2026-season](https://github.com/MHHSRobotics/2026-season) | CLAUDE.md | – | AdvantageKit, CTRE Phoenix 6, REVLib, PhotonVision, Commands v2, Swerve, Python/RobotPy, Spotless/format |
| [MRT3216/MRT3216-2026-Rebuilt](https://github.com/MRT3216/MRT3216-2026-Rebuilt) | Copilot instructions | – | AdvantageKit, CTRE Phoenix 6, REVLib, PathPlanner, PhotonVision, YAGSL, Swerve, Spotless/format |
| [MTCHS-Coders/NeoAndKrakenExampleWithClaudeMD](https://github.com/MTCHS-Coders/NeoAndKrakenExampleWithClaudeMD) | CLAUDE.md | – | CTRE Phoenix 6, REVLib, Commands v2 |
| [McQuaidRobotics/2026_Rebuilt](https://github.com/McQuaidRobotics/2026_Rebuilt) | GEMINI.md | – | CTRE Phoenix 6, Choreo, Commands v2, Swerve, Spotless/format |
| [McQuaidRobotics/2026_Robot_Base](https://github.com/McQuaidRobotics/2026_Robot_Base) | Skills | `frc-loop-overrun` | AdvantageKit, CTRE Phoenix 6, Limelight, Swerve, Spotless/format |
| [MegaZord7563/XBbot-FRC2025](https://github.com/MegaZord7563/XBbot-FRC2025) | CLAUDE.md | – | CTRE Phoenix 6 |
| [MillenniumFalcons/2026-Rebuilt-Bane](https://github.com/MillenniumFalcons/2026-Rebuilt-Bane) | CLAUDE.md | – | AdvantageKit, CTRE Phoenix 6, REVLib, Choreo, Limelight, Swerve, Spotless/format |
| [Mona-Shores-FTC-Robotics/biobuzz](https://github.com/Mona-Shores-FTC-Robotics/biobuzz) | Skills | `localization-triage`, `match-review` | AdvantageScope, Limelight |
| [N0v4ont0p/Refract](https://github.com/N0v4ont0p/Refract) | Skills | `ftc-construct`, `ftc-team-config` | Limelight, WPILib 2027/SystemCore, Commands v2, Swerve, C++ |
| [NBNTT/Reefscape_Refactor_v2](https://github.com/NBNTT/Reefscape_Refactor_v2) | AGENTS.md | – | AdvantageKit, AdvantageScope, CTRE Phoenix 6, REVLib, PathPlanner, PhotonVision, Swerve |
| [NormalDuck/XBot-Documentation](https://github.com/NormalDuck/XBot-Documentation) | AGENTS.md | – | – |
| [Nsl106/frc-kickoff-countdown](https://github.com/Nsl106/frc-kickoff-countdown) | CLAUDE.md | – | – |
| [OlympusRobotics/Olympus_Robotics_FRC](https://github.com/OlympusRobotics/Olympus_Robotics_FRC) | Copilot instructions | – | AdvantageKit, AdvantageScope, CTRE Phoenix 6, REVLib, PathPlanner, PhotonVision, Commands v2, Swerve, C++, Python/RobotPy |
| [OrenMic/ftc-robot-2027](https://github.com/OrenMic/ftc-robot-2027) | AGENTS.md | – | AdvantageKit, AdvantageScope, REVLib, PathPlanner, Limelight, Swerve, Kotlin, Spotless/format |
| [Overture-7421/wpi-emulator](https://github.com/Overture-7421/wpi-emulator) | Copilot instructions | – | C++ |
| [OwenRossing/7028-post-match](https://github.com/OwenRossing/7028-post-match) | CLAUDE.md | – | CTRE Phoenix 6 |
| [PinewoodRobotics/command-robot-base](https://github.com/PinewoodRobotics/command-robot-base) | Cursor rules | – | AdvantageKit, Commands v2 |
| [PlasmaRobotics2403/2026Robot](https://github.com/PlasmaRobotics2403/2026Robot) | AGENTS.md | – | AdvantageKit, AdvantageScope, CTRE Phoenix 6, PathPlanner, PhotonVision, Limelight, MapleSim, Commands v2, Swerve, SysId, Spotless/format |
| [Project516/frc-dev-wasm](https://github.com/Project516/frc-dev-wasm) | AGENTS.md | – | AdvantageKit, CTRE Phoenix 6, PhotonVision, Elastic, WPILib 2027/SystemCore, Commands v2, C++, SysId |
| [RAR1741/RA26_RobotCode](https://github.com/RAR1741/RA26_RobotCode) | CLAUDE.md | – | AdvantageKit, CTRE Phoenix 6, REVLib, Commands v2, Swerve, SysId |
| [RambotsTeam1350/REBUILT-2026](https://github.com/RambotsTeam1350/REBUILT-2026) | CLAUDE.md | – | CTRE Phoenix 6, REVLib, PathPlanner, Limelight, Commands v2, Swerve |
| [RamenRobotics9036/Mochi2026](https://github.com/RamenRobotics9036/Mochi2026) | CLAUDE.md, Copilot instructions | – | CTRE Phoenix 6, REVLib, PathPlanner, PhotonVision, Limelight, Elastic, Commands v2, Swerve |
| [Raptacon/Robot-2026](https://github.com/Raptacon/Robot-2026) | CLAUDE.md | – | AdvantageScope, CTRE Phoenix 6, REVLib, PathPlanner, PhotonVision, Commands v2, Swerve, C++, Python/RobotPy, SysId, Spotless/format |
| [Raptacon/robot-2027](https://github.com/Raptacon/robot-2027) | CLAUDE.md | – | CTRE Phoenix 6, REVLib, PathPlanner, PhotonVision, WPILib 2027/SystemCore, Commands v2, Swerve, C++, Python/RobotPy, SysId, Spotless/format |
| [Riverside-Robotics-5683/Robot2026-2.0](https://github.com/Riverside-Robotics-5683/Robot2026-2.0) | CLAUDE.md | – | CTRE Phoenix 6, REVLib, PathPlanner, PhotonVision, Commands v2, Swerve |
| [Robohawk2022/2026CompetitionRobot](https://github.com/Robohawk2022/2026CompetitionRobot) | CLAUDE.md | – | AdvantageScope, CTRE Phoenix 6, REVLib, Limelight, Elastic, QuestNav, Commands v2, Swerve |
| [RossRambotics/2026-Rebuilt](https://github.com/RossRambotics/2026-Rebuilt) | CLAUDE.md | – | AdvantageKit, CTRE Phoenix 6, PathPlanner, PhotonVision, Limelight, Commands v2, Swerve, SysId, Spotless/format |
| [RunnymedeRobotics1310/2026_Swerve](https://github.com/RunnymedeRobotics1310/2026_Swerve) | CLAUDE.md | – | CTRE Phoenix 6, REVLib, Limelight, Commands v2, Swerve |
| [RunnymedeRobotics1310/RunnymedeSwerve](https://github.com/RunnymedeRobotics1310/RunnymedeSwerve) | CLAUDE.md | – | REVLib, Limelight, Swerve |
| [STEM-Plus-C/stemplusc.org](https://github.com/STEM-Plus-C/stemplusc.org) | CLAUDE.md | – | – |
| [Saint-Francis-Robotics-Team2367/2026dev](https://github.com/Saint-Francis-Robotics-Team2367/2026dev) | CLAUDE.md, Skills | `check-releases` | AdvantageScope, CTRE Phoenix 6, REVLib, Elastic, QuestNav, WPILib 2027/SystemCore, Commands v2, Swerve, C++, Python/RobotPy |
| [Saint-Francis-Robotics-Team2367/minibots-2026](https://github.com/Saint-Francis-Robotics-Team2367/minibots-2026) | Other | – | – |
| [ShadowOfTheVOID/WPILIB-Java-Website-To-Learn](https://github.com/ShadowOfTheVOID/WPILIB-Java-Website-To-Learn) | CLAUDE.md | – | CTRE Phoenix 6, Swerve, SysId |
| [Shenzhen-Robotics-Alliance/5516-2026-OnSeason-main](https://github.com/Shenzhen-Robotics-Alliance/5516-2026-OnSeason-main) | AGENTS.md | – | AdvantageKit, AdvantageScope, CTRE Phoenix 6, PathPlanner, PhotonVision, Limelight, MapleSim, Commands v2, Swerve, SysId, Spotless/format |
| [Shenzhen-Robotics-Alliance/AdvantageKit-TalonSwerveTemplate-MapleSim](https://github.com/Shenzhen-Robotics-Alliance/AdvantageKit-TalonSwerveTemplate-MapleSim) | AGENTS.md | – | AdvantageKit, AdvantageScope, CTRE Phoenix 6, PathPlanner, PhotonVision, Limelight, MapleSim, Commands v2, Swerve, SysId, Spotless/format |
| [Shenzhen-Robotics-Alliance/maple-sim](https://github.com/Shenzhen-Robotics-Alliance/maple-sim) | CLAUDE.md | – | MapleSim, Commands v2, Swerve, C++, Spotless/format |
| [Shishir99-code/CADChecker](https://github.com/Shishir99-code/CADChecker) | CLAUDE.md | – | – |
| [SouthwestRoboticsProgramming/2026_Main_Robot](https://github.com/SouthwestRoboticsProgramming/2026_Main_Robot) | CLAUDE.md | – | CTRE Phoenix 6, PathPlanner, Limelight, Commands v2, Swerve |
| [Spectrum3847/2026-FM-SystemCore](https://github.com/Spectrum3847/2026-FM-SystemCore) | AGENTS.md | – | AdvantageKit, CTRE Phoenix 6, WPILib 2027/SystemCore, Swerve, Spotless/format |
| [Spectrum3847/2026-Spectrum](https://github.com/Spectrum3847/2026-Spectrum) | AGENTS.md, Skills | `robot-log-upload` | AdvantageKit, CTRE Phoenix 6, PathPlanner, PhotonVision, Limelight, MapleSim, Elastic, WPILib 2027/SystemCore, Swerve, Spotless/format |
| [Spectrum3847/2027-MiniSwerve](https://github.com/Spectrum3847/2027-MiniSwerve) | AGENTS.md | – | AdvantageKit, CTRE Phoenix 6, WPILib 2027/SystemCore, Swerve, Spotless/format |
| [Stormgears-FRC-5422/Rebuilt](https://github.com/Stormgears-FRC-5422/Rebuilt) | CLAUDE.md | – | AdvantageKit, CTRE Phoenix 6, REVLib, Limelight, Commands v2, Swerve |
| [Suave101/SysMod](https://github.com/Suave101/SysMod) | CLAUDE.md | – | CTRE Phoenix 6, REVLib, PhotonVision, Limelight, WPILib 2027/SystemCore, Commands v2, C++, Python/RobotPy |
| [SushiSquad7461/2026-dango](https://github.com/SushiSquad7461/2026-dango) | CLAUDE.md | – | AdvantageKit, CTRE Phoenix 6, REVLib, PathPlanner, PhotonVision, Limelight, Commands v2, Swerve |
| [SushiSquad7461/SushiScout26](https://github.com/SushiSquad7461/SushiScout26) | CLAUDE.md | – | – |
| [TESRobotics539/2026-Robot-Code](https://github.com/TESRobotics539/2026-Robot-Code) | CLAUDE.md | – | AdvantageKit, CTRE Phoenix 6, REVLib, PathPlanner, Choreo, Commands v2, Swerve |
| [TaubatexasRobotics/Teste_Swerve-2.0](https://github.com/TaubatexasRobotics/Teste_Swerve-2.0) | CLAUDE.md | – | AdvantageScope, CTRE Phoenix 6, REVLib, PathPlanner, Limelight, WPILib 2027/SystemCore, Commands v2, Swerve, SysId |
| [Team-663/my-sample-robot-2027](https://github.com/Team-663/my-sample-robot-2027) | AGENTS.md | – | AdvantageKit, CTRE Phoenix 6, WPILib 2027/SystemCore, Commands v2, SysId |
| [Team-7530/2026-Robotics](https://github.com/Team-7530/2026-Robotics) | Copilot instructions | – | CTRE Phoenix 6, PathPlanner, Limelight, Commands v2 |
| [Team100/all26](https://github.com/Team100/all26) | AGENTS.md | – | CTRE Phoenix 6, REVLib, Commands v2 |
| [Team1923/2026OffseasonCloneBot](https://github.com/Team1923/2026OffseasonCloneBot) | Skills | `analyze-wpilog` | AdvantageKit, AdvantageScope, CTRE Phoenix 6, PathPlanner, Limelight, Swerve |
| [Team2556/FRC-2026](https://github.com/Team2556/FRC-2026) | CLAUDE.md | – | CTRE Phoenix 6, PathPlanner, Commands v2, Swerve, Python/RobotPy |
| [Team302/2026Rebuilt](https://github.com/Team302/2026Rebuilt) | Copilot instructions | – | CTRE Phoenix 6, Choreo, Limelight, QuestNav, Swerve, C++ |
| [Team302/2026RebuiltSystemCoreCPP](https://github.com/Team302/2026RebuiltSystemCoreCPP) | Copilot instructions | – | CTRE Phoenix 6, Choreo, Limelight, QuestNav, Swerve, C++ |
| [Team41Robotics/2026-robot-code](https://github.com/Team41Robotics/2026-robot-code) | CLAUDE.md, Copilot instructions | – | AdvantageKit, CTRE Phoenix 6, PhotonVision, Swerve, Spotless/format |
| [Team4539/2026-2027](https://github.com/Team4539/2026-2027) | CLAUDE.md, Copilot instructions | – | CTRE Phoenix 6, PathPlanner, PhotonVision, Elastic, Swerve |
| [Team488/Programming-Docs](https://github.com/Team488/Programming-Docs) | AGENTS.md | – | Commands v2, Swerve |
| [Team488/SeriouslyCommonLib](https://github.com/Team488/SeriouslyCommonLib) | AGENTS.md | – | AdvantageKit, PhotonVision, Swerve, Spotless/format |
| [Team488/TeamXbot2026](https://github.com/Team488/TeamXbot2026) | CLAUDE.md | – | AdvantageKit, CTRE Phoenix 6, REVLib, Swerve |
| [Team4973/2026-rebuilt](https://github.com/Team4973/2026-rebuilt) | CLAUDE.md | – | CTRE Phoenix 6, Commands v2, Swerve, Python/RobotPy, SysId |
| [Team4973/results](https://github.com/Team4973/results) | CLAUDE.md | – | – |
| [Team5427/Rebuilt](https://github.com/Team5427/Rebuilt) | Copilot instructions | – | CTRE Phoenix 6, REVLib, PhotonVision, Limelight, Commands v2, C++ |
| [Team5427/SteelTalonsLibrary](https://github.com/Team5427/SteelTalonsLibrary) | Copilot instructions | – | Commands v2, C++ |
| [Team624/2026Robot-Public](https://github.com/Team624/2026Robot-Public) | Copilot instructions | – | AdvantageKit, CTRE Phoenix 6, REVLib, PathPlanner, Choreo, PhotonVision, Limelight, Commands v2, Swerve, Spotless/format |
| [Team6940/2026Rebuilt-Offseason](https://github.com/Team6940/2026Rebuilt-Offseason) | AGENTS.md | – | AdvantageKit, AdvantageScope, CTRE Phoenix 6, PathPlanner, Choreo, PhotonVision, Limelight, MapleSim, Swerve |
| [Team7520/FRC-2026](https://github.com/Team7520/FRC-2026) | Copilot instructions | – | AdvantageKit, AdvantageScope, CTRE Phoenix 6, REVLib, PathPlanner, PhotonVision, Limelight, Commands v2, Swerve, SysId, Spotless/format |
| [Team846/gibbon](https://github.com/Team846/gibbon) | AGENTS.md | – | CTRE Phoenix 6, REVLib, Commands v2, Swerve, C++, Spotless/format |
| [TeamRoboto447/2026-robot-code](https://github.com/TeamRoboto447/2026-robot-code) | CLAUDE.md | – | AdvantageKit, PathPlanner, Commands v2 |
| [TeamTechmaker/REBUILT_2026](https://github.com/TeamTechmaker/REBUILT_2026) | CLAUDE.md | – | AdvantageKit, AdvantageScope, CTRE Phoenix 6, REVLib, PathPlanner, Choreo, Limelight, Elastic, Commands v2, Swerve, Spotless/format |
| [TeamTitaniumRobotics/FRC-2026](https://github.com/TeamTitaniumRobotics/FRC-2026) | Copilot instructions | – | AdvantageKit, CTRE Phoenix 6, MapleSim, Commands v2, Swerve, C++, Spotless/format |
| [TecDroid-3354/Common_Code](https://github.com/TecDroid-3354/Common_Code) | AGENTS.md | – | AdvantageKit, AdvantageScope, CTRE Phoenix 6, Commands v2, Kotlin |
| [The-Allsparks/TRACE](https://github.com/The-Allsparks/TRACE) | Skills | `tlog-advantagescope` | AdvantageScope |
| [TheTriSonics/Rebuilt2026](https://github.com/TheTriSonics/Rebuilt2026) | CLAUDE.md | – | CTRE Phoenix 6, Choreo, PhotonVision, Commands v2, Swerve, Python/RobotPy |
| [TomAs-1226/FrcCatalyst](https://github.com/TomAs-1226/FrcCatalyst) | AGENTS.md | – | CTRE Phoenix 6, WPILib 2027/SystemCore, Commands v2 |
| [TripleHelixProgramming/wpilog-mcp](https://github.com/TripleHelixProgramming/wpilog-mcp) | CLAUDE.md | – | Swerve |
| [Warren-Warbots/FRC-skills](https://github.com/Warren-Warbots/FRC-skills) | Skills | `FRCLogReader` | CTRE Phoenix 6, Limelight |
| [Webstie/25-26-swerve](https://github.com/Webstie/25-26-swerve) | CLAUDE.md | – | AdvantageKit, CTRE Phoenix 6, REVLib, PathPlanner, PhotonVision, Commands v2, Swerve |
| [WheatleyRobotics/Rebuilt](https://github.com/WheatleyRobotics/Rebuilt) | AGENTS.md | – | AdvantageKit, PathPlanner, Spotless/format |
| [XrxcGH/BIOCORE-Analysis](https://github.com/XrxcGH/BIOCORE-Analysis) | CLAUDE.md | – | AdvantageKit, CTRE Phoenix 6, Limelight, Elastic, WPILib 2027/SystemCore, Commands v2, Swerve, C++, Spotless/format |
| [YCSRobotics/2026ReBuilt-CC](https://github.com/YCSRobotics/2026ReBuilt-CC) | CLAUDE.md | – | CTRE Phoenix 6, PathPlanner, Choreo, Limelight, Commands v2, Swerve |
| [Yet-Another-Software-Suite/YAMS-Gitbook](https://github.com/Yet-Another-Software-Suite/YAMS-Gitbook) | Skills | `yams-docs-review` | AdvantageKit, WPILib 2027/SystemCore, Commands v2, Swerve, C++ |
| [Yuvadeep-D/programming-final-27-YD-nm-final](https://github.com/Yuvadeep-D/programming-final-27-YD-nm-final) | AGENTS.md | – | AdvantageKit, AdvantageScope, CTRE Phoenix 6, PathPlanner, PhotonVision, Limelight, MapleSim, Commands v2, Swerve, SysId |
| [acappon/hunker](https://github.com/acappon/hunker) | CLAUDE.md | – | REVLib, WPILib 2027/SystemCore, C++ |
| [aesatchien/FRC2429_2026](https://github.com/aesatchien/FRC2429_2026) | CLAUDE.md | – | AdvantageScope, CTRE Phoenix 6, REVLib, PathPlanner, QuestNav, WPILib 2027/SystemCore, Swerve, Python/RobotPy, SysId |
| [amorygalili/frc-design-skills](https://github.com/amorygalili/frc-design-skills) | Skills | `frc-design-lib` | REVLib, Swerve |
| [anAvgCoder/2026Robot-demo](https://github.com/anAvgCoder/2026Robot-demo) | Copilot instructions | – | Commands v2, C++ |
| [armandomm09/blue-banner-engine](https://github.com/armandomm09/blue-banner-engine) | CLAUDE.md | – | – |
| [arnan-bajaj/phantomtrack](https://github.com/arnan-bajaj/phantomtrack) | CLAUDE.md | – | – |
| [artemisIsNotBeam/choose-subsystems](https://github.com/artemisIsNotBeam/choose-subsystems) | CLAUDE.md | – | Swerve |
| [atlee-circuitree/Rebuilt-new](https://github.com/atlee-circuitree/Rebuilt-new) | CLAUDE.md | – | CTRE Phoenix 6, PathPlanner, Limelight, Commands v2, Swerve |
| [avgupta456/statbotics](https://github.com/avgupta456/statbotics) | CLAUDE.md | – | – |
| [aydenterranella/2026-ROBOT-SIM-TESTING](https://github.com/aydenterranella/2026-ROBOT-SIM-TESTING) | Skills | `powerlib-sim` | AdvantageScope, CTRE Phoenix 6, REVLib, MapleSim, Swerve |
| [bolme/Parade-Bot-2024](https://github.com/bolme/Parade-Bot-2024) | AGENTS.md, Skills | `cim-talon-motors`, `cim-talon-motors-advanced`, `frc-development`, `frc-motor-groups`, `neo-spark-motors`, `neo-spark-motors-advanced`, `wpilib-yearly-changes` | AdvantageScope, CTRE Phoenix 6, REVLib, PhotonVision, Elastic, WPILib 2027/SystemCore, Commands v2, Swerve, C++, Python/RobotPy |
| [brsnell/scream-tools](https://github.com/brsnell/scream-tools) | Skills | `frc-visualize-code` | WPILib 2027/SystemCore, Commands v2 |
| [bruce-peters/bruce-peters.github.io](https://github.com/bruce-peters/bruce-peters.github.io) | CLAUDE.md | – | – |
| [cgpadwick/frc-camera-sim](https://github.com/cgpadwick/frc-camera-sim) | CLAUDE.md | – | – |
| [churrobots/2026-rebuilt](https://github.com/churrobots/2026-rebuilt) | Skills | `churro-debug` | REVLib, PhotonVision |
| [clrozeboom/FRC5010Claude](https://github.com/clrozeboom/FRC5010Claude) | CLAUDE.md | – | AdvantageKit, AdvantageScope, CTRE Phoenix 6, REVLib, PhotonVision, Limelight, YAGSL, WPILib 2027/SystemCore, Commands v2, Swerve, SysId |
| [cpfaffinger/CnCNew](https://github.com/cpfaffinger/CnCNew) | CLAUDE.md | – | CTRE Phoenix 6, C++ |
| [crossbotics/ftc-helper-lib](https://github.com/crossbotics/ftc-helper-lib) | Other | – | Limelight |
| [deslobodzian/talOS](https://github.com/deslobodzian/talOS) | AGENTS.md | – | C++ |
| [dfn-slxxp/MoSim-Mod-Tracker](https://github.com/dfn-slxxp/MoSim-Mod-Tracker) | CLAUDE.md | – | – |
| [dmonachello/swerveBringupMotorTest1](https://github.com/dmonachello/swerveBringupMotorTest1) | AGENTS.md | – | CTRE Phoenix 6, REVLib |
| [dougdliu/FRC_AI_Mega_Project](https://github.com/dougdliu/FRC_AI_Mega_Project) | Skills | `skill` | AdvantageScope, MapleSim |
| [dr-hextanium/jimmy](https://github.com/dr-hextanium/jimmy) | CLAUDE.md | – | Commands v2, Kotlin, SysId |
| [edanliahovetsky/wpilib-agent-tools](https://github.com/edanliahovetsky/wpilib-agent-tools) | Skills | `wpilib-agent-tools` | – |
| [ehartye/agent-frc](https://github.com/ehartye/agent-frc) | Skills | `frc-java` | Swerve |
| [frc-6045/2026-naan-REALEST](https://github.com/frc-6045/2026-naan-REALEST) | CLAUDE.md | – | AdvantageKit, CTRE Phoenix 6, REVLib, PathPlanner, Limelight, YAGSL, MapleSim, Commands v2, Swerve |
| [frc-7419/Rebuilt2026](https://github.com/frc-7419/Rebuilt2026) | Copilot instructions | – | AdvantageKit, CTRE Phoenix 6, PathPlanner, PhotonVision, Limelight, Commands v2, Spotless/format |
| [frc-862/mac-mini-monitoring](https://github.com/frc-862/mac-mini-monitoring) | CLAUDE.md | – | PhotonVision, Python/RobotPy |
| [frc10101/TealTeam](https://github.com/frc10101/TealTeam) | CLAUDE.md | – | – |
| [frc2053/Robot2026](https://github.com/frc2053/Robot2026) | CLAUDE.md, Copilot instructions | – | CTRE Phoenix 6, REVLib, PhotonVision, Limelight, Commands v2, Swerve, C++, Spotless/format |
| [frc4451/garagedocs](https://github.com/frc4451/garagedocs) | CLAUDE.md, Skills | `docs-prose-style-guide` | AdvantageKit, CTRE Phoenix 6, Spotless/format |
| [frc461/Rowdy25](https://github.com/frc461/Rowdy25) | AGENTS.md | – | CTRE Phoenix 6, PathPlanner, PhotonVision, Limelight, QuestNav, Swerve, SysId |
| [frc6941/2025-offseason-robot-big](https://github.com/frc6941/2025-offseason-robot-big) | Cursor rules | – | AdvantageKit, CTRE Phoenix 6 |
| [frc6941/2025-offseason-robot-small](https://github.com/frc6941/2025-offseason-robot-small) | Cursor rules | – | AdvantageKit, CTRE Phoenix 6 |
| [frc6941/Iron-Tank-2025](https://github.com/frc6941/Iron-Tank-2025) | Cursor rules | – | AdvantageKit, CTRE Phoenix 6 |
| [frc78/kotlin-fsm-frc-course](https://github.com/frc78/kotlin-fsm-frc-course) | CLAUDE.md, Skills | `new-lesson` | CTRE Phoenix 6, Commands v2, Swerve, Kotlin |
| [frcvan/moneyshift](https://github.com/frcvan/moneyshift) | AGENTS.md | – | AdvantageKit, PhotonVision, WPILib 2027/SystemCore, Commands v2, Swerve |
| [fry42guy/FRC-SmartDashboard](https://github.com/fry42guy/FRC-SmartDashboard) | CLAUDE.md | – | CTRE Phoenix 6, Commands v2, Swerve, C++ |
| [ftc8569/2025-Decode](https://github.com/ftc8569/2025-Decode) | Skills | `ftc-decode`, `ftclib`, `limelight3a`, `pedropathing` | Limelight, Commands v2, Swerve, Kotlin |
| [gatorbotics1700/1700_preseason_2027](https://github.com/gatorbotics1700/1700_preseason_2027) | CLAUDE.md | – | AdvantageKit, AdvantageScope, CTRE Phoenix 6, Commands v2, SysId |
| [gearsincorg/Alpha](https://github.com/gearsincorg/Alpha) | CLAUDE.md | – | WPILib 2027/SystemCore, Commands v2 |
| [gerth2/photonGroundTruthBench](https://github.com/gerth2/photonGroundTruthBench) | AGENTS.md | – | PhotonVision, WPILib 2027/SystemCore, Python/RobotPy |
| [gfleming1992/CadCove-FRC](https://github.com/gfleming1992/CadCove-FRC) | Skills | `frc-ctre-controls`, `frc-simulation-validation` | CTRE Phoenix 6, Swerve |
| [gladiatorsprogramming1591/Rebuilt2026](https://github.com/gladiatorsprogramming1591/Rebuilt2026) | Copilot instructions | – | AdvantageKit, CTRE Phoenix 6, PathPlanner, PhotonVision, Commands v2, Spotless/format |
| [goldenhornfrc/robot-2026](https://github.com/goldenhornfrc/robot-2026) | Copilot instructions | – | AdvantageKit, CTRE Phoenix 6, Commands v2, Spotless/format |
| [gryphoncommand/2026-bootcamp](https://github.com/gryphoncommand/2026-bootcamp) | CLAUDE.md | – | – |
| [guerin-robotics/Rebuilt2026](https://github.com/guerin-robotics/Rebuilt2026) | CLAUDE.md, Skills | `debug-match-log`, `pid-tune`, `scope`, `simulate`, `wpilib-agent-tools` | AdvantageKit, AdvantageScope, CTRE Phoenix 6, PathPlanner, Swerve |
| [hadley31/FRC-27-Testing](https://github.com/hadley31/FRC-27-Testing) | AGENTS.md | – | Choreo, WPILib 2027/SystemCore, Commands v2 |
| [importly/custom_2025_robotics_build](https://github.com/importly/custom_2025_robotics_build) | CLAUDE.md | – | AdvantageKit, PathPlanner, Commands v2, Spotless/format |
| [itkan-robotics/mantik](https://github.com/itkan-robotics/mantik) | CLAUDE.md, Cursor rules | – | AdvantageScope, CTRE Phoenix 6, REVLib, PhotonVision, Limelight, Elastic |
| [j0n5m1th/frc-agent-toolkit](https://github.com/j0n5m1th/frc-agent-toolkit) | AGENTS.md, Skills | `ctre-convert`, `ctre-diagnose`, `frc-start-here`, `systemcore`, `systemcore-live`, `wpilib-build`, `wpilib-deploy`, `wpilib-live`, `wpilib-log-analysis`, `wpilib-prematch`, `wpilib-profile`, `wpilib-project`, `wpilib-triage`, `wpilib-tune` | AdvantageKit, CTRE Phoenix 6, WPILib 2027/SystemCore, Commands v2, SysId |
| [jamesdooley4/LogReader1](https://github.com/jamesdooley4/LogReader1) | Copilot instructions | – | AdvantageScope, CTRE Phoenix 6, Python/RobotPy |
| [jasondaming/first-package-manager](https://github.com/jasondaming/first-package-manager) | CLAUDE.md | – | – |
| [johnnybongo486/2026_Cyclops](https://github.com/johnnybongo486/2026_Cyclops) | CLAUDE.md | – | CTRE Phoenix 6, Commands v2, Swerve, SysId |
| [jonahsnider/dotfiles](https://github.com/jonahsnider/dotfiles) | Skills | `analyze-wpilog`, `robot-ssh` | – |
| [jonahsnider/wpilog-parser](https://github.com/jonahsnider/wpilog-parser) | Skills | `analyze-wpilog` | – |
| [kaushalpemmaraju/AutoAlign](https://github.com/kaushalpemmaraju/AutoAlign) | Copilot instructions | – | Commands v2, C++ |
| [kaushalpemmaraju/QuestNav](https://github.com/kaushalpemmaraju/QuestNav) | Copilot instructions | – | Commands v2, C++ |
| [kaushalpemmaraju/Rebuilt-Indexer](https://github.com/kaushalpemmaraju/Rebuilt-Indexer) | Copilot instructions | – | CTRE Phoenix 6, REVLib, PhotonVision, Limelight, Commands v2, C++ |
| [kaushalpemmaraju/TinyTalon](https://github.com/kaushalpemmaraju/TinyTalon) | Copilot instructions | – | Commands v2, C++ |
| [kaushalpemmaraju/Turret](https://github.com/kaushalpemmaraju/Turret) | Copilot instructions | – | Commands v2, C++ |
| [kevinclark/hindsight](https://github.com/kevinclark/hindsight) | AGENTS.md | – | AdvantageKit, AdvantageScope, CTRE Phoenix 6, PhotonVision, C++, Spotless/format |
| [laviRZ/basicsim26](https://github.com/laviRZ/basicsim26) | AGENTS.md | – | AdvantageKit, AdvantageScope, CTRE Phoenix 6, Commands v2, Swerve, Spotless/format |
| [ligerbots/Rebuilt2026](https://github.com/ligerbots/Rebuilt2026) | Copilot instructions | – | CTRE Phoenix 6, Commands v2 |
| [lunariselysium/FRC2026-afterseason](https://github.com/lunariselysium/FRC2026-afterseason) | AGENTS.md | – | CTRE Phoenix 6, PhotonVision, Commands v2, Swerve |
| [mathewdunne/CodeRunner](https://github.com/mathewdunne/CodeRunner) | AGENTS.md | – | AdvantageScope, PathPlanner |
| [mathewdunne/CodeRunner-Lessons](https://github.com/mathewdunne/CodeRunner-Lessons) | CLAUDE.md | – | AdvantageKit, CTRE Phoenix 6, PathPlanner, Commands v2, Spotless/format |
| [mxtbyrm/parabolica-2026](https://github.com/mxtbyrm/parabolica-2026) | CLAUDE.md | – | CTRE Phoenix 6, PathPlanner, Limelight, Commands v2, Swerve, SysId |
| [ncssm-robotics/ftc-claude](https://github.com/ncssm-robotics/ftc-claude) | Skills | `decode`, `ftclib`, `limelight`, `roadrunner`, `skill-builder` | Limelight, Commands v2, Kotlin |
| [neia-robotics/2026-Robot-Code](https://github.com/neia-robotics/2026-Robot-Code) | AGENTS.md | – | Spotless/format |
| [neia-robotics/strix](https://github.com/neia-robotics/strix) | AGENTS.md | – | WPILib 2027/SystemCore, Python/RobotPy |
| [netarcx/systemcore-rpi5-guide](https://github.com/netarcx/systemcore-rpi5-guide) | CLAUDE.md | – | CTRE Phoenix 6, Limelight, WPILib 2027/SystemCore |
| [nirobotics/cyber-power](https://github.com/nirobotics/cyber-power) | Skills | `cyber-power-log-analysis` | AdvantageKit |
| [nowireless/DiffySwerve](https://github.com/nowireless/DiffySwerve) | AGENTS.md | – | Commands v2 |
| [npmanos/wpilib-container](https://github.com/npmanos/wpilib-container) | CLAUDE.md | – | WPILib 2027/SystemCore, C++ |
| [o-bots7160/2026-frc-rebuilt](https://github.com/o-bots7160/2026-frc-rebuilt) | Copilot instructions | – | AdvantageKit, AdvantageScope, PathPlanner, Elastic, Commands v2, Swerve |
| [o-bots7160/elastic_dashboard-mcp](https://github.com/o-bots7160/elastic_dashboard-mcp) | Copilot instructions | – | AdvantageKit, AdvantageScope, YAGSL, Elastic, Swerve |
| [ramalamadingdong/agentic-csa](https://github.com/ramalamadingdong/agentic-csa) | CLAUDE.md, Copilot instructions | – | CTRE Phoenix 6, REVLib, PhotonVision, Limelight, Commands v2, Swerve, C++ |
| [ramalamadingdong/frc-rag-mcpserver](https://github.com/ramalamadingdong/frc-rag-mcpserver) | Copilot instructions | – | Commands v2, C++ |
| [realaaravdas/dasAfterburner](https://github.com/realaaravdas/dasAfterburner) | CLAUDE.md | – | C++ |
| [rebels2638/2026-Robot-Code](https://github.com/rebels2638/2026-Robot-Code) | Cursor rules | – | AdvantageScope |
| [robotdotnet/WPILib](https://github.com/robotdotnet/WPILib) | AGENTS.md | – | C++ |
| [robototes/REBUILT2026](https://github.com/robototes/REBUILT2026) | Skills | `frc-robot-safety-can`, `frc-student-mentor-persona`, `frc-telemetry-units`, `frc-wpilib-architecture` | AdvantageKit, CTRE Phoenix 6, REVLib, Commands v2, Swerve |
| [ryanabraham1/Mayhem](https://github.com/ryanabraham1/Mayhem) | Other | – | CTRE Phoenix 6, Commands v2, Swerve, C++ |
| [ryanabraham1/frc-3d-sim](https://github.com/ryanabraham1/frc-3d-sim) | CLAUDE.md | – | – |
| [rylero/ClaudeScope](https://github.com/rylero/ClaudeScope) | Skills | `replay`, `scope`, `simulate` | AdvantageKit, AdvantageScope, Swerve |
| [rylero/DeepstreamYOLO](https://github.com/rylero/DeepstreamYOLO) | CLAUDE.md | – | – |
| [rylero/RobotLogBot](https://github.com/rylero/RobotLogBot) | Skills | `scope` | AdvantageKit, Swerve |
| [safiqsindha/TheEngine](https://github.com/safiqsindha/TheEngine) | Other | – | CTRE Phoenix 6, REVLib |
| [sahil-patel-2011/6925-RobotCodeUpdated](https://github.com/sahil-patel-2011/6925-RobotCodeUpdated) | Skills | `frc-6925-robot` | CTRE Phoenix 6, PathPlanner, Choreo, Limelight, SysId |
| [sahil-patel-2011/6925-coding-resources](https://github.com/sahil-patel-2011/6925-coding-resources) | Other | – | – |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | Skills | `frc-code-2027-preview`, `frc-code-autonomous`, `frc-code-build-deploy`, `frc-code-can-power`, `frc-code-command-based`, `frc-code-competition-day`, `frc-code-debugging`, `frc-code-driver-controls`, `frc-code-log-analysis`, `frc-code-logging-replay`, `frc-code-mechanisms`, `frc-code-motor-controllers`, `frc-code-pid-feedforward`, `frc-code-project-setup`, `frc-code-review-checklist`, `frc-code-sim-testing`, `frc-code-start-here`, `frc-code-swerve`, `frc-code-team-git`, `frc-code-units-constants`, `frc-code-vision`, `frc-design-electrical-layout`, `frc-design-pneumatics`, `frc-design-process`, `java-kotlin-wpilib` | AdvantageKit, AdvantageScope, CTRE Phoenix 6, REVLib, PathPlanner, Choreo, PhotonVision, Limelight, YAGSL, MapleSim, WPILib 2027/SystemCore, Commands v2, Swerve, Kotlin, C++, SysId, Spotless/format |
| [sean-zhen/1468-2026](https://github.com/sean-zhen/1468-2026) | Copilot instructions | – | AdvantageKit, CTRE Phoenix 6, PathPlanner, Commands v2 |
| [seanedwards/xrp-mechatronics-2026](https://github.com/seanedwards/xrp-mechatronics-2026) | AGENTS.md | – | Commands v2 |
| [snidercs/frc-bot-2026-cxx](https://github.com/snidercs/frc-bot-2026-cxx) | Copilot instructions | – | CTRE Phoenix 6, PhotonVision, Commands v2, Swerve, C++ |
| [snidercs/frc-bot-2026-java](https://github.com/snidercs/frc-bot-2026-java) | Copilot instructions | – | CTRE Phoenix 6, PathPlanner, PhotonVision, Commands v2, Swerve, C++ |
| [snidercs/luabot](https://github.com/snidercs/luabot) | Copilot instructions | – | Commands v2, C++ |
| [superhornets/2026-Robot](https://github.com/superhornets/2026-Robot) | Copilot instructions | – | CTRE Phoenix 6, REVLib, PhotonVision, Limelight, Commands v2, Swerve, C++ |
| [team3316/rebuilt-2026](https://github.com/team3316/rebuilt-2026) | GEMINI.md, Skills | `mirror-auto` | AdvantageKit, AdvantageScope, CTRE Phoenix 6, PathPlanner, Choreo, Elastic, Commands v2, Swerve, SysId, Spotless/format |
| [team4909/2027-biocore](https://github.com/team4909/2027-biocore) | AGENTS.md | – | AdvantageKit, CTRE Phoenix 6, PathPlanner, Choreo, PhotonVision, Swerve, SysId |
| [team5142/RoboDominators_2026](https://github.com/team5142/RoboDominators_2026) | Copilot instructions | – | – |
| [team5419/frc-2026](https://github.com/team5419/frc-2026) | Skills | `analyze-wpilog` | – |
| [team581/bolt](https://github.com/team581/bolt) | Skills | `access-fetch-logs`, `analyze-wpilog`, `debug-hoot` | CTRE Phoenix 6 |
| [team581/offseason-2026](https://github.com/team581/offseason-2026) | Skills | `analyze-wpilog` | – |
| [team751/2026Robot](https://github.com/team751/2026Robot) | CLAUDE.md | – | AdvantageScope, CTRE Phoenix 6, PathPlanner, Limelight, MapleSim, Swerve, Spotless/format |
| [teamresistance/Drive_Template](https://github.com/teamresistance/Drive_Template) | Copilot instructions | – | AdvantageKit, AdvantageScope, CTRE Phoenix 6, REVLib, PathPlanner, Choreo, PhotonVision, Commands v2, Swerve, Spotless/format |
| [tervay/recalc](https://github.com/tervay/recalc) | AGENTS.md | – | C++ |
| [thebuilder6/2027Dreams](https://github.com/thebuilder6/2027Dreams) | AGENTS.md | – | AdvantageKit, WPILib 2027/SystemCore |
| [thehomelessguy/MARSLib](https://github.com/thehomelessguy/MARSLib) | AGENTS.md, Skills | `marslib`, `marslib-autonomous`, `marslib-ci`, `marslib-control-theory`, `marslib-diagnostics`, `marslib-math`, `marslib-mechanisms`, `marslib-network`, `marslib-statemachine`, `marslib-swerve`, `marslib-telemetry`, `marslib-testing`, `marslib-vision` | AdvantageKit, AdvantageScope, CTRE Phoenix 6, PathPlanner, Choreo, PhotonVision, Limelight, QuestNav, Commands v2, Swerve, SysId, Spotless/format |
| [tuffinmuffin/raptaboard](https://github.com/tuffinmuffin/raptaboard) | CLAUDE.md | – | Elastic, WPILib 2027/SystemCore, Swerve, Python/RobotPy |
| [tulsaroboticscenter/FRC-Rebuilt-2025-2026](https://github.com/tulsaroboticscenter/FRC-Rebuilt-2025-2026) | AGENTS.md | – | CTRE Phoenix 6, Swerve, Kotlin |
| [unlabs-llc/unlabs-ai-toolkit-frc-2027](https://github.com/unlabs-llc/unlabs-ai-toolkit-frc-2027) | Skills | `current-claude-models`, `current-wpilib-2027`, `frc-pre-deploy` | CTRE Phoenix 6, PhotonVision, WPILib 2027/SystemCore, Commands v2, C++ |
| [vincent0926/FRC-elevator-PID-Teaching-website](https://github.com/vincent0926/FRC-elevator-PID-Teaching-website) | CLAUDE.md | – | AdvantageKit, CTRE Phoenix 6, SysId |
| [waterprisem/learn-frc](https://github.com/waterprisem/learn-frc) | CLAUDE.md | – | – |
| [wpilibsuite/systemcore-blocks-interface](https://github.com/wpilibsuite/systemcore-blocks-interface) | CLAUDE.md | – | WPILib 2027/SystemCore, Python/RobotPy |
| [yeti-robotics/rebuilt-2026](https://github.com/yeti-robotics/rebuilt-2026) | Skills | `advantagekit`, `wpilib-best-practices` | AdvantageKit, REVLib, YAGSL, Commands v2 |

## Appendix B: Every skill found inside FRC/FTC repositories

| Repository | Skill | What it does (from its description) |
| --- | --- | --- |
| [Adelson-Robotics/10183-2025-rebuilt-frc-Summer-Scorcher](https://github.com/Adelson-Robotics/10183-2025-rebuilt-frc-Summer-Scorcher) | [`analyze-wpilog`](https://github.com/Adelson-Robotics/10183-2025-rebuilt-frc-Summer-Scorcher/blob/HEAD/.claude/skills/analyze-wpilog/SKILL.md) | Analyze WPILOG log files from FRC robots using the wpilog-parser library with TypeScript. |
| [AkarshSharma2012/FRCstack-skill](https://github.com/AkarshSharma2012/FRCstack-skill) | [`frcstack`](https://github.com/AkarshSharma2012/FRCstack-skill/blob/HEAD/skills/frcstack/SKILL.md) | Create FRC/FTC Onshape robot CAD using real component libraries, scout competitions into evidence-backed Excel workbooks, reconstruct observed robots, and review mechanisms. Supports create, scout, replicate, review, com |
| [Camo-Bots-4285/TrainerBoard-Motors](https://github.com/Camo-Bots-4285/TrainerBoard-Motors) | [`ctre-phoenix6`](https://github.com/Camo-Bots-4285/TrainerBoard-Motors/blob/HEAD/.claude/skills/ctre-phoenix6/SKILL.md) | CTRE Phoenix 6 (2026) API reference for TalonFX/TalonFXS/CANcoder - control requests, status signals, configuration, followers, and simulation via TalonFXSimState. Use when reading or writing any code that imports com.ct |
| [Camo-Bots-4285/TrainerBoard-Motors](https://github.com/Camo-Bots-4285/TrainerBoard-Motors) | [`revlib`](https://github.com/Camo-Bots-4285/TrainerBoard-Motors/blob/HEAD/.claude/skills/revlib/SKILL.md) | REVLib 2026 API reference for SPARK MAX / SPARK Flex - configuration objects, closed-loop control, MAXMotion, native units (RPM!), and SparkSim simulation. Use when reading or writing any code that imports com.revrobotic |
| [Camo-Bots-4285/TrainerBoard-Motors](https://github.com/Camo-Bots-4285/TrainerBoard-Motors) | [`wpilib-frc`](https://github.com/Camo-Bots-4285/TrainerBoard-Motors/blob/HEAD/.claude/skills/wpilib-frc/SKILL.md) | WPILib 2026 reference for FRC Java - the units library (Measure/Angle/AngularVelocity), physics simulation (DCMotorSim, FlywheelSim, ElevatorSim, SingleJointedArmSim), Alerts, RobotController/RoboRioSim timing, and the A |
| [CohenHill/WPILIB27-testing-repo](https://github.com/CohenHill/WPILIB27-testing-repo) | [`api-check`](https://github.com/CohenHill/WPILIB27-testing-repo/blob/HEAD/.claude/skills/api-check/SKILL.md) | Verify a WPILib 2027, Commands v3 or vendor API before writing a call to it. Use whenever adding or changing code that calls org.wpilib, org.littletonrobotics or a vendor library. |
| [CohenHill/WPILIB27-testing-repo](https://github.com/CohenHill/WPILIB27-testing-repo) | [`match-debrief`](https://github.com/CohenHill/WPILIB27-testing-repo/blob/HEAD/.claude/skills/match-debrief/SKILL.md) | Write a one-page debrief of a match or sim run from its log. Use after a match, or when asked for a debrief, driver report or "how did that run go". Needs a .wpilog file. |
| [CohenHill/WPILIB27-testing-repo](https://github.com/CohenHill/WPILIB27-testing-repo) | [`new-mechanism`](https://github.com/CohenHill/WPILIB27-testing-repo/blob/HEAD/.claude/skills/new-mechanism/SKILL.md) | Scaffold a new mechanism (IO, sim IO, mechanism, tests, scenario, doc card) by copying the Arm. Use when the user types /new-mechanism <Name>. |
| [Demacia5635/utils2027--Alpha7](https://github.com/Demacia5635/utils2027--Alpha7) | [`add-device`](https://github.com/Demacia5635/utils2027--Alpha7/blob/HEAD/.claude/skills/add-device/SKILL.md) | Add a motor or sensor using the Demacia config/wrapper classes on WPILib 2027, or write a new wrapper type (sensor or motor controller) for the library. Use when the user asks to add, configure or support a motor, encode |
| [Demacia5635/utils2027--Alpha7](https://github.com/Demacia5635/utils2027--Alpha7) | [`cr`](https://github.com/Demacia5635/utils2027--Alpha7/blob/HEAD/.claude/skills/cr/SKILL.md) | Code review of a student's robot code (a mechanism branch, PR, folder or the current changes) against Demacia's review checklist, written like a team mentor's CR. Use when the user types /cr or asks for a code review, CR |
| [Demacia5635/utils2027--Alpha7](https://github.com/Demacia5635/utils2027--Alpha7) | [`new-mechanism`](https://github.com/Demacia5635/utils2027--Alpha7/blob/HEAD/.claude/skills/new-mechanism/SKILL.md) | Create a new robot subsystem (arm, elevator, intake, shooter, climber...) on the Demacia StateBaseMechanism for WPILib 2027, with a state enum, constants, limits, calibration and a default command. Use when the user asks |
| [Demacia5635/utils2027--Alpha7](https://github.com/Demacia5635/utils2027--Alpha7) | [`port-from-2026`](https://github.com/Demacia5635/utils2027--Alpha7/blob/HEAD/.claude/skills/port-from-2026/SKILL.md) | Port a class or feature from the 2026 DemaciaCode repo (frc.demacia, WPILib 2026) into this 2027 alpha library (first.demacia). Use when the user asks to bring over, migrate or convert 2026 code. |
| [FRC2713/skills](https://github.com/FRC2713/skills) | [`review-my-code`](https://github.com/FRC2713/skills/blob/HEAD/plugins/frc2713-skills/skills/review-my-code/SKILL.md) | Interactively review a student's FRC / WPILib Java robot code with them, as a teaching exercise. Walks through the code together, surfacing issues one at a time and guiding the student to fix them themselves rather than  |
| [FRC9410/Robot-Library](https://github.com/FRC9410/Robot-Library) | [`powerlib-sim`](https://github.com/FRC9410/Robot-Library/blob/HEAD/skills/SKILL.md) | Use this skill whenever a Team 9410 PowerLib robot project needs MapleSim simulation set up, |
| [FRC999/ParadeBot-MechaREX](https://github.com/FRC999/ParadeBot-MechaREX) | [`update-mecharams-demo-robot`](https://github.com/FRC999/ParadeBot-MechaREX/blob/HEAD/.codex/skills/update-mecharams-demo-robot/SKILL.md) | Safely maintain and verify FRC Team 999's MeetTheMechaRAMS WPILib/Phoenix drive-only demo project. Use for changes to Xbox driving, CTRE swerve configuration, speed profiles, dashboard selection, dependency upgrades, fir |
| [FRCTeam1073-TheForceTeam/viper](https://github.com/FRCTeam1073-TheForceTeam/viper) | [`viper-new-season`](https://github.com/FRCTeam1073-TheForceTeam/viper/blob/HEAD/.agents/skills/viper-new-season/SKILL.md) | – |
| [FRCTeam1073-TheForceTeam/viper](https://github.com/FRCTeam1073-TheForceTeam/viper) | [`viper-overview`](https://github.com/FRCTeam1073-TheForceTeam/viper/blob/HEAD/.agents/skills/viper-overview/SKILL.md) | Viper FRC/FTC scouting application repository structure, technology stack, and project organization. Use this to understand how the project is organized and find relevant files for requested changes. |
| [FRCTeam360/RainMaker26](https://github.com/FRCTeam360/RainMaker26) | [`new-doc`](https://github.com/FRCTeam360/RainMaker26/blob/HEAD/.claude/skills/new-doc/SKILL.md) | Create a new documentation file in docs/ following this repo's standardized templates for advanced_topic, core_architecture, and implementation_plan doc types. Use when the user asks to write/create new documentation, a  |
| [FROG3160/2026-rebuilt](https://github.com/FROG3160/2026-rebuilt) | [`frog-robotpy`](https://github.com/FROG3160/2026-rebuilt/blob/HEAD/frog-robotpy-skill-source/frog-robotpy/SKILL.md) | Senior Python Developer specializing in RobotPy (WPILib for Python) for FRC. Use when creating or modifying subsystems, commands, or hardware configurations for the FROG (FIRST Robotics Organization of Grove) codebase. |
| [Filip-Kin/CTRE-Skill](https://github.com/Filip-Kin/CTRE-Skill) | [`phoenix6-frc`](https://github.com/Filip-Kin/CTRE-Skill/blob/HEAD/phoenix6-frc/SKILL.md) | Use when writing or reviewing FRC Java code involving CTRE Phoenix 6 |
| [Hemlock5712/2027-Template](https://github.com/Hemlock5712/2027-Template) | [`add-a-mechanism`](https://github.com/Hemlock5712/2027-Template/blob/HEAD/.claude/skills/add-a-mechanism/SKILL.md) | Step-by-step procedure for adding a new mechanism (subsystem) or a new OpMode to this robot template — the file to create, the Mechanism/Command shape to copy, how to wire it into Robot, how to give it a simulation model |
| [Hemlock5712/2027-Template](https://github.com/Hemlock5712/2027-Template) | [`build-and-deploy`](https://github.com/Hemlock5712/2027-Template/blob/HEAD/.claude/skills/build-and-deploy/SKILL.md) | How to compile this project from the command line and put code on the robot — the Java 25 JDK requirement, what spotless does on every build, the SystemCore deploy target (not a roboRIO), and what to check before and aft |
| [Hemlock5712/2027-Template](https://github.com/Hemlock5712/2027-Template) | [`game-info`](https://github.com/Hemlock5712/2027-Template/blob/HEAD/.claude/skills/game-info/SKILL.md) | Season/field conventions this codebase enforces — the blue-origin field frame, automatic red-alliance pose flipping (AllianceFlip), the CTRE operator perspective, and AprilTag/target-space conventions. Use whenever a tas |
| [Hemlock5712/2027-Template](https://github.com/Hemlock5712/2027-Template) | [`log-reading`](https://github.com/Hemlock5712/2027-Template/blob/HEAD/.claude/skills/log-reading/SKILL.md) | How to find and analyze this robot's logs — AdvantageKit .wpilog files (drive telemetry + DS + system stats) and Phoenix .hoot files written by the CANivore. Lists the real keys this code logs, where logs live, and how t |
| [Hemlock5712/2027-Template](https://github.com/Hemlock5712/2027-Template) | [`robot-description`](https://github.com/Hemlock5712/2027-Template/blob/HEAD/.claude/skills/robot-description/SKILL.md) | High-level map of this FRC robot template — the OpMode/Commands-v3 wiring, the subsystems and commands, where each piece lives in the source tree, and the hardware/tooling stack (WPILib 2027 alpha, CTRE swerve, Advantage |
| [Hemlock5712/2027-Template](https://github.com/Hemlock5712/2027-Template) | [`run-sim`](https://github.com/Hemlock5712/2027-Template/blob/HEAD/.claude/skills/run-sim/SKILL.md) | Run this robot in simulation — both the normal GUI sim and a headless, auto-enabling mode for agent / CI loops (the simulateJavaAgent Gradle task). Use whenever you need to actually run the robot code in sim, drive an au |
| [Hemlock5712/2027-Template](https://github.com/Hemlock5712/2027-Template) | [`teaching`](https://github.com/Hemlock5712/2027-Template/blob/HEAD/.claude/skills/teaching/SKILL.md) | Teacher mode for students. How to explain this robot code to middle/high-school students learning FRC — simple words, short answers, one idea at a time, and where to send them to learn more (PID/feedforward tuning, Comma |
| [IamCoder18/synapse](https://github.com/IamCoder18/synapse) | [`synapse-pubsub-ftc`](https://github.com/IamCoder18/synapse/blob/HEAD/skill/synapse-pubsub-ftc/SKILL.md) | Builds FIRST Tech Challenge (FTC) robot code on the Synapse annotation-driven pub/sub bus. ALWAYS use this skill for any FTC coding task — new OpModes, TeleOp or autonomous, subsystems, gamepad handling, sensors, refacto |
| [McQuaidRobotics/2026_Robot_Base](https://github.com/McQuaidRobotics/2026_Robot_Base) | [`frc-loop-overrun`](https://github.com/McQuaidRobotics/2026_Robot_Base/blob/HEAD/ai%20skills/optimization/SKILL.md) | Diagnose and fix loop overruns ("Loop time of 0.02s overrun", "CommandScheduler loop overrun", high CPU, laggy/stuttering robot) in FRC WPILib Java robot code — by profiling the real roboRIO over JMX/JFR (the port Visual |
| [Mona-Shores-FTC-Robotics/biobuzz](https://github.com/Mona-Shores-FTC-Robotics/biobuzz) | [`localization-triage`](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/HEAD/.claude/skills/localization-triage/SKILL.md) | Read a match log or a "Vision - HIVE & Pose" session log and say which one number to change when the camera and the odometry disagree - camera mount, a HIVE field point, Pinpoint odometry, a gate/sigma, or nothing. Use f |
| [Mona-Shores-FTC-Robotics/biobuzz](https://github.com/Mona-Shores-FTC-Robotics/biobuzz) | [`match-review`](https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/HEAD/.claude/skills/match-review/SKILL.md) | Review a match from its robot log and its match video, lined up - what the robot did, when, and whether the log is telling the truth - and use or add to the event's video-evidence gallery. Use for "what happened in Q-14? |
| [N0v4ont0p/Refract](https://github.com/N0v4ont0p/Refract) | [`ftc-construct`](https://github.com/N0v4ont0p/Refract/blob/HEAD/.claude/skills/ftc-construct/SKILL.md) | Config-gated FTC robot code generation, grounded in library documentation, the elite-team pattern corpus, and the hardware catalog: scaffolds new OpModes, subsystems, and mechanisms from the quickstart interface template |
| [N0v4ont0p/Refract](https://github.com/N0v4ont0p/Refract) | [`ftc-team-config`](https://github.com/N0v4ont0p/Refract/blob/HEAD/.claude/skills/ftc-team-config/SKILL.md) | Establishes, confirms, and maintains the team''s robot configuration (drivetrain, season mechanisms, sensors, software stack) that gates all FTC robot code generation. Use this skill BEFORE writing or modifying any robot |
| [Saint-Francis-Robotics-Team2367/2026dev](https://github.com/Saint-Francis-Robotics-Team2367/2026dev) | [`check-releases`](https://github.com/Saint-Francis-Robotics-Team2367/2026dev/blob/HEAD/.claude/skills/check-releases/SKILL.md) | Check whether newer RobotPy / WPILib 2027 releases or Phoenix6 / REVLib SystemCore vendordeps have shipped relative to the version pinned in pyproject.toml, and recommend whether to bump. Use when asked to check for upda |
| [Spectrum3847/2026-Spectrum](https://github.com/Spectrum3847/2026-Spectrum) | [`robot-log-upload`](https://github.com/Spectrum3847/2026-Spectrum/blob/HEAD/.agents/skills/robot-log-upload/SKILL.md) | Pull match logs (.wpilog) and Limelight rewind clips off a roboRIO or SystemCore and upload them to robotlog.spectrum3847.org. Use when match logs or rewinds need to go to the team log site, for 3847 or 8515. |
| [Team1923/2026OffseasonCloneBot](https://github.com/Team1923/2026OffseasonCloneBot) | [`analyze-wpilog`](https://github.com/Team1923/2026OffseasonCloneBot/blob/HEAD/.claude/skills/analyze-wpilog/SKILL.md) | Analyze AdvantageKit .wpilog logs (plus Driver Station .dslog/.dsevents) from the 2026 Offseason CloneBot - ingest logs from the robot's USB stick, generate a post-match/practice health report, find the root cause of a r |
| [The-Allsparks/TRACE](https://github.com/The-Allsparks/TRACE) | [`tlog-advantagescope`](https://github.com/The-Allsparks/TRACE/blob/HEAD/.cursor/skills/tlog-advantagescope/SKILL.md) | Pull TRACE .tlog files from the Control Hub, convert to WPILOG, and open |
| [Warren-Warbots/FRC-skills](https://github.com/Warren-Warbots/FRC-skills) | [`FRCLogReader`](https://github.com/Warren-Warbots/FRC-skills/blob/HEAD/SKILL.md) | read and analyze wpilib or hoot logs |
| [Yet-Another-Software-Suite/YAMS-Gitbook](https://github.com/Yet-Another-Software-Suite/YAMS-Gitbook) | [`yams-docs-review`](https://github.com/Yet-Another-Software-Suite/YAMS-Gitbook/blob/HEAD/.claude/skills/yams-docs-review/SKILL.md) | Review or write YAMS-Gitbook pages so a new FRC programmer can learn YAMS from them. Use when adding, editing or reviewing any page in YAMS-Gitbook, when restructuring SUMMARY.md, or when someone says the docs are hard t |
| [amorygalili/frc-design-skills](https://github.com/amorygalili/frc-design-skills) | [`frc-design-lib`](https://github.com/amorygalili/frc-design-skills/blob/HEAD/.agents/skills/frc-design-lib/SKILL.md) | Search and download COTS (common off-the-shelf) FRC robot parts — motor controllers, power distribution boards, radios, bearings, bushings, and similar hardware — from the FRC Design Lib Onshape library, as STEP files re |
| [aydenterranella/2026-ROBOT-SIM-TESTING](https://github.com/aydenterranella/2026-ROBOT-SIM-TESTING) | [`powerlib-sim`](https://github.com/aydenterranella/2026-ROBOT-SIM-TESTING/blob/HEAD/skills/SKILL.md) | Use this skill whenever a Team 9410 PowerLib robot project needs MapleSim simulation set up, |
| [bolme/Parade-Bot-2024](https://github.com/bolme/Parade-Bot-2024) | [`cim-talon-motors-advanced`](https://github.com/bolme/Parade-Bot-2024/blob/HEAD/.claude/skills/cim-talon-motors-advanced/SKILL.md) | Use when implementing closed-loop control, Motion Magic, CANcoder remote/fused sensors, follower motors, or status-signal tuning for CTRE brushless motors (Falcon 500, Kraken X60 via TalonFX) on Phoenix 6, or advanced Ph |
| [bolme/Parade-Bot-2024](https://github.com/bolme/Parade-Bot-2024) | [`cim-talon-motors`](https://github.com/bolme/Parade-Bot-2024/blob/HEAD/.claude/skills/cim-talon-motors/SKILL.md) | Use when wiring up, configuring, or debugging brushed FRC motors (CIM, 775pro/Redline) driven by CTRE Talon SRX or Victor SPX controllers via Phoenix 5 (com.ctre.phoenix) in Java — motor/controller selection, basic perce |
| [bolme/Parade-Bot-2024](https://github.com/bolme/Parade-Bot-2024) | [`frc-development`](https://github.com/bolme/Parade-Bot-2024/blob/HEAD/.claude/skills/frc-development/SKILL.md) | Use when writing, reviewing, or restructuring Java robot code for an FRC (FIRST Robotics Competition) project built on WPILib/GradleRIO — command-based architecture, subsystem/command organization, hardware safety, and c |
| [bolme/Parade-Bot-2024](https://github.com/bolme/Parade-Bot-2024) | [`frc-motor-groups`](https://github.com/bolme/Parade-Bot-2024/blob/HEAD/.claude/skills/frc-motor-groups/SKILL.md) | Use when wiring multiple motors together to act as one mechanism in FRC Java robot code — e.g. a multi-motor drivetrain side, a two-motor elevator, or any leader/follower setup. Covers WPILib's deprecated MotorController |
| [bolme/Parade-Bot-2024](https://github.com/bolme/Parade-Bot-2024) | [`neo-spark-motors-advanced`](https://github.com/bolme/Parade-Bot-2024/blob/HEAD/.claude/skills/neo-spark-motors-advanced/SKILL.md) | Use when implementing closed-loop control (PID, MAXMotion/Smart Motion), alternate/absolute encoders, follower motors, soft limits/limit switches, multiple PID slots, CAN signal/status-frame tuning, faults/diagnostics, o |
| [bolme/Parade-Bot-2024](https://github.com/bolme/Parade-Bot-2024) | [`neo-spark-motors`](https://github.com/bolme/Parade-Bot-2024/blob/HEAD/.claude/skills/neo-spark-motors/SKILL.md) | Use when wiring up, configuring, or debugging REV NEO/NEO 550/NEO Vortex brushless motors driven by SPARK MAX or SPARK Flex controllers via REVLib in Java — motor/controller selection, basic SparkMaxConfig/SparkFlexConfi |
| [bolme/Parade-Bot-2024](https://github.com/bolme/Parade-Bot-2024) | [`wpilib-yearly-changes`](https://github.com/bolme/Parade-Bot-2024/blob/HEAD/.claude/skills/wpilib-yearly-changes/SKILL.md) | Use when reading, upgrading, or explaining FRC Java robot code that spans multiple WPILib seasons (2023-2026) — reasoning about why old code looks different from new code, migrating a project to a newer season, or figuri |
| [brsnell/scream-tools](https://github.com/brsnell/scream-tools) | [`frc-visualize-code`](https://github.com/brsnell/scream-tools/blob/HEAD/plugins/scream-tools/skills/frc-visualize-code/SKILL.md) | Maps an FRC (WPILib) Java robot project starting from Robot.java — classes, subsystems/mechanisms, commands, libraries, controller button bindings and default commands — and writes an interactive HTML page plus SVG diagr |
| [churrobots/2026-rebuilt](https://github.com/churrobots/2026-rebuilt) | [`churro-debug`](https://github.com/churrobots/2026-rebuilt/blob/HEAD/.claude/skills/churro-debug/SKILL.md) | Walk a student through debugging the robot using the latest robot debug data in debug/. Use when someone asks what's wrong with the robot, why a motor/camera/sensor isn't working, why the robot crashed or won't drive, or |
| [dougdliu/FRC_AI_Mega_Project](https://github.com/dougdliu/FRC_AI_Mega_Project) | [`skill`](https://github.com/dougdliu/FRC_AI_Mega_Project/blob/HEAD/skill.md) | – |
| [edanliahovetsky/wpilib-agent-tools](https://github.com/edanliahovetsky/wpilib-agent-tools) | [`wpilib-agent-tools`](https://github.com/edanliahovetsky/wpilib-agent-tools/blob/HEAD/agent/src/wpilib_agent_tools/integrations/codex/skill_bundle/SKILL.md) | Sandbox-first WPILib simulation, NT4 recording, and WPILOG analysis orchestration with the wpilib-agent-tools CLI. Use when tasks involve running simulation, validating autonomous or subsystem behavior, querying/graphing |
| [ehartye/agent-frc](https://github.com/ehartye/agent-frc) | [`frc-java`](https://github.com/ehartye/agent-frc/blob/HEAD/skills/frc-java/SKILL.md) | Use when designing, implementing or testing WPILib Java code. |
| [frc4451/garagedocs](https://github.com/frc4451/garagedocs) | [`docs-prose-style-guide`](https://github.com/frc4451/garagedocs/blob/HEAD/.claude/skills/docs-prose-style-guide/SKILL.md) | GarageDocs Docs Prose Style Guide (formerly the Mentor Voice Prose Guide). Use before writing, editing or reviewing any GarageDocs prose (lessons, badge pages, overviews, headings, frontmatter titles, prose review report |
| [frc78/kotlin-fsm-frc-course](https://github.com/frc78/kotlin-fsm-frc-course) | [`new-lesson`](https://github.com/frc78/kotlin-fsm-frc-course/blob/HEAD/.claude/skills/new-lesson/SKILL.md) | Add a new lesson (a group of tasks) to this EduTools course — creates the lesson directory, registers it in course-info.yaml, then uses the new-task skill for each task inside it. |
| [ftc8569/2025-Decode](https://github.com/ftc8569/2025-Decode) | [`ftc-decode`](https://github.com/ftc8569/2025-Decode/blob/HEAD/.claude/skills/ftc-decode/SKILL.md) | Expert knowledge about FTC DECODE game (2025-2026 season). Use when planning autonomous strategies, understanding scoring rules, discussing field layout, working with artifacts, pattern matching, gate mechanics, or optim |
| [ftc8569/2025-Decode](https://github.com/ftc8569/2025-Decode) | [`ftclib`](https://github.com/ftc8569/2025-Decode/blob/HEAD/.claude/skills/ftclib/SKILL.md) | Expert guidance for FTCLib command-based programming. Use when creating subsystems and commands, binding buttons, using PID controllers, composing command groups, working with hardware wrappers, or implementing FTC robot |
| [ftc8569/2025-Decode](https://github.com/ftc8569/2025-Decode) | [`limelight3a`](https://github.com/ftc8569/2025-Decode/blob/HEAD/.claude/skills/limelight3a/SKILL.md) | Expert guidance for using Limelight 3A vision camera in FTC robotics. Use when working with AprilTag detection, robot localization, MegaTag, pipeline configuration, coordinate transformations, or vision-based targeting. |
| [ftc8569/2025-Decode](https://github.com/ftc8569/2025-Decode) | [`pedropathing`](https://github.com/ftc8569/2025-Decode/blob/HEAD/.claude/skills/pedropathing/SKILL.md) | Expert guidance for creating FTC robot autonomous paths using pedroPathing v2.0. Use when building paths, configuring PathBuilder, using PathChains, adding callbacks, setting speed controls, working with Bezier curves, o |
| [gfleming1992/CadCove-FRC](https://github.com/gfleming1992/CadCove-FRC) | [`frc-ctre-controls`](https://github.com/gfleming1992/CadCove-FRC/blob/HEAD/agent-skills/frc-ctre-controls/SKILL.md) | Integrate and debug FRC WPILib and Phoenix 6 motor, swerve, encoder, and gyro code against the actual robot and CadCove plant. |
| [gfleming1992/CadCove-FRC](https://github.com/gfleming1992/CadCove-FRC) | [`frc-simulation-validation`](https://github.com/gfleming1992/CadCove-FRC/blob/HEAD/agent-skills/frc-simulation-validation/SKILL.md) | Commission, diagnose and profile a CAD-first FRC simulation using real robot code, measurable trials and separate practice/accuracy settings. |
| [guerin-robotics/Rebuilt2026](https://github.com/guerin-robotics/Rebuilt2026) | [`debug-match-log`](https://github.com/guerin-robotics/Rebuilt2026/blob/HEAD/.claude/skills/debug-match-log/SKILL.md) | Analyze an AdvantageKit match log (.wpilog/.hoot) to root-cause a field problem. Use when something went wrong on the robot during a match or practice and a log file exists. Correlates signals around the anomaly, tests h |
| [guerin-robotics/Rebuilt2026](https://github.com/guerin-robotics/Rebuilt2026) | [`pid-tune`](https://github.com/guerin-robotics/Rebuilt2026/blob/HEAD/.claude/skills/pid-tune/SKILL.md) | Run the PID/feedforward tuning loop for a mechanism — change gains, run simulation, read the resulting logs, evaluate, and iterate until criteria are met. Use when tuning any closed-loop mechanism (flywheel, hood, pivot, |
| [guerin-robotics/Rebuilt2026](https://github.com/guerin-robotics/Rebuilt2026) | [`scope`](https://github.com/guerin-robotics/Rebuilt2026/blob/HEAD/tools/ClaudeScope/skills/scope/SKILL.md) | Use ClaudeScope to analyze FRC robot .wpilog files or query live NetworkTables. |
| [guerin-robotics/Rebuilt2026](https://github.com/guerin-robotics/Rebuilt2026) | [`simulate`](https://github.com/guerin-robotics/Rebuilt2026/blob/HEAD/tools/ClaudeScope/skills/simulate/SKILL.md) | Use when asked to simulate the FRC robot, test behavior in simulation, analyze robot performance without hardware, or run auto/teleop in a virtual environment. Trigger on: "/simulate", "run the sim", "test in simulation" |
| [guerin-robotics/Rebuilt2026](https://github.com/guerin-robotics/Rebuilt2026) | [`wpilib-agent-tools`](https://github.com/guerin-robotics/Rebuilt2026/blob/HEAD/tools/wpilib-agent-tools/agent/src/wpilib_agent_tools/integrations/codex/skill_bundle/SKILL.md) | Sandbox-first WPILib simulation, NT4 recording, and WPILOG analysis orchestration with the wpilib-agent-tools CLI. Use when tasks involve running simulation, validating autonomous or subsystem behavior, querying/graphing |
| [j0n5m1th/frc-agent-toolkit](https://github.com/j0n5m1th/frc-agent-toolkit) | [`ctre-convert`](https://github.com/j0n5m1th/frc-agent-toolkit/blob/HEAD/plugins/frc/skills/ctre-convert/SKILL.md) | Convert CTRE .hoot logs to .wpilog, or MIDI to .chrp for Orchestra. Use for a .hoot or .mid file, or a robot playing a song. |
| [j0n5m1th/frc-agent-toolkit](https://github.com/j0n5m1th/frc-agent-toolkit) | [`ctre-diagnose`](https://github.com/j0n5m1th/frc-agent-toolkit/blob/HEAD/plugins/frc/skills/ctre-diagnose/SKILL.md) | Check CAN bus, CTRE devices and CTRE tool versions. Use when a device is missing, a bus looks unhealthy, or a CTRE tool is missing or old. |
| [j0n5m1th/frc-agent-toolkit](https://github.com/j0n5m1th/frc-agent-toolkit) | [`frc-start-here`](https://github.com/j0n5m1th/frc-agent-toolkit/blob/HEAD/plugins/frc/skills/frc-start-here/SKILL.md) | Welcome students and show what is set up. Use for "start here", "what can you do", "help me get started", or when unsure where to begin. |
| [j0n5m1th/frc-agent-toolkit](https://github.com/j0n5m1th/frc-agent-toolkit) | [`systemcore-live`](https://github.com/j0n5m1th/frc-agent-toolkit/blob/HEAD/plugins/frc/skills/systemcore-live/SKILL.md) | Read a live Systemcore's health, CAN buses, robot-code state and alerts, no SSH. Use for "is the controller OK right now" or "is the code running". |
| [j0n5m1th/frc-agent-toolkit](https://github.com/j0n5m1th/frc-agent-toolkit) | [`systemcore`](https://github.com/j0n5m1th/frc-agent-toolkit/blob/HEAD/plugins/frc/skills/systemcore/SKILL.md) | Check the Systemcore controller and its logs. Use for "is the controller OK", a slow or crashing robot, a full disk, support bundles, or match logs. |
| [j0n5m1th/frc-agent-toolkit](https://github.com/j0n5m1th/frc-agent-toolkit) | [`wpilib-build`](https://github.com/j0n5m1th/frc-agent-toolkit/blob/HEAD/plugins/frc/skills/wpilib-build/SKILL.md) | Build, test and clean robot code; manage vendordeps, team number and WPILib tools. Use for compile or test failures. |
| [j0n5m1th/frc-agent-toolkit](https://github.com/j0n5m1th/frc-agent-toolkit) | [`wpilib-deploy`](https://github.com/j0n5m1th/frc-agent-toolkit/blob/HEAD/plugins/frc/skills/wpilib-deploy/SKILL.md) | Deploy robot code to the robot. Use when the user asks to deploy or download code to the robot. |
| [j0n5m1th/frc-agent-toolkit](https://github.com/j0n5m1th/frc-agent-toolkit) | [`wpilib-live`](https://github.com/j0n5m1th/frc-agent-toolkit/blob/HEAD/plugins/frc/skills/wpilib-live/SKILL.md) | Read robot or simulator values over NetworkTables, or run the sim. Use for current values, watching a signal, or "does it start without a robot". |
| [j0n5m1th/frc-agent-toolkit](https://github.com/j0n5m1th/frc-agent-toolkit) | [`wpilib-log-analysis`](https://github.com/j0n5m1th/frc-agent-toolkit/blob/HEAD/plugins/frc/skills/wpilib-log-analysis/SKILL.md) | Analyze .wpilog, Driver Station .dslog and converted .hoot logs, synced. Use for what happened in a match, brownouts, lost connection, or comparing logs. |
| [j0n5m1th/frc-agent-toolkit](https://github.com/j0n5m1th/frc-agent-toolkit) | [`wpilib-prematch`](https://github.com/j0n5m1th/frc-agent-toolkit/blob/HEAD/plugins/frc/skills/wpilib-prematch/SKILL.md) | Read-only pre-match check of battery, controller health, alerts, CAN and code state. Use for "are we ready to queue", a pit check or "is the robot OK". |
| [j0n5m1th/frc-agent-toolkit](https://github.com/j0n5m1th/frc-agent-toolkit) | [`wpilib-profile`](https://github.com/j0n5m1th/frc-agent-toolkit/blob/HEAD/plugins/frc/skills/wpilib-profile/SKILL.md) | Profile a running Java program (robot sim or any JVM) with JFR. Use for CPU, GC, allocation, locks, leaks, or loop-time spikes. |
| [j0n5m1th/frc-agent-toolkit](https://github.com/j0n5m1th/frc-agent-toolkit) | [`wpilib-project`](https://github.com/j0n5m1th/frc-agent-toolkit/blob/HEAD/plugins/frc/skills/wpilib-project/SKILL.md) | Explain or review a WPILib Java robot project. Use for "explain our code", "what does each button do", "review my code", loop overruns, or before competition. |
| [j0n5m1th/frc-agent-toolkit](https://github.com/j0n5m1th/frc-agent-toolkit) | [`wpilib-triage`](https://github.com/j0n5m1th/frc-agent-toolkit/blob/HEAD/plugins/frc/skills/wpilib-triage/SKILL.md) | Turn a match or practice log into a short report of what went wrong and what to fix first. Use after a match, "why did auto fail" or "what broke". |
| [j0n5m1th/frc-agent-toolkit](https://github.com/j0n5m1th/frc-agent-toolkit) | [`wpilib-tune`](https://github.com/j0n5m1th/frc-agent-toolkit/blob/HEAD/plugins/frc/skills/wpilib-tune/SKILL.md) | Tune a mechanism's PID or feedforward by measuring overshoot, rise and settle time. Use for "tune the arm", "it oscillates" or "how fast does it settle". |
| [jonahsnider/dotfiles](https://github.com/jonahsnider/dotfiles) | [`analyze-wpilog`](https://github.com/jonahsnider/dotfiles/blob/HEAD/dot_agents/skills/analyze-wpilog/SKILL.md) | Analyze WPILOG log files from FRC robots using the wpilog-parser library with TypeScript. |
| [jonahsnider/dotfiles](https://github.com/jonahsnider/dotfiles) | [`robot-ssh`](https://github.com/jonahsnider/dotfiles/blob/HEAD/dot_agents/skills/robot-ssh/SKILL.md) | SSH into an FRC robot to run commands or access logs |
| [jonahsnider/wpilog-parser](https://github.com/jonahsnider/wpilog-parser) | [`analyze-wpilog`](https://github.com/jonahsnider/wpilog-parser/blob/HEAD/packages/wpilog-parser/skills/analyze-wpilog/SKILL.md) | Analyze WPILOG log files from FRC robots using the wpilog-parser library with TypeScript. |
| [ncssm-robotics/ftc-claude](https://github.com/ncssm-robotics/ftc-claude) | [`skill-builder`](https://github.com/ncssm-robotics/ftc-claude/blob/HEAD/.claude/skills/skill-builder/SKILL.md) | Create, structure, and optimize FTC skills for the marketplace. Use when creating a new skill, improving an existing skill, scaffolding a plugin, or needing guidance on skill design patterns, triggers, frontmatter, progr |
| [ncssm-robotics/ftc-claude](https://github.com/ncssm-robotics/ftc-claude) | [`decode`](https://github.com/ncssm-robotics/ftc-claude/blob/HEAD/plugins/decode/skills/decode/SKILL.md) | DECODE 2025-2026 FTC game reference. Use when programming autonomous routines, calculating scores, understanding field layout, or working with game-specific coordinates. |
| [ncssm-robotics/ftc-claude](https://github.com/ncssm-robotics/ftc-claude) | [`ftclib`](https://github.com/ncssm-robotics/ftc-claude/blob/HEAD/plugins/ftclib/skills/ftclib/SKILL.md) | Helps write FTC robot code using the FTCLib library. Use for command-based programming, subsystems, GamepadEx input handling, Pure Pursuit path following, or vision pipelines. |
| [ncssm-robotics/ftc-claude](https://github.com/ncssm-robotics/ftc-claude) | [`limelight`](https://github.com/ncssm-robotics/ftc-claude/blob/HEAD/plugins/limelight/skills/limelight/SKILL.md) | Helps integrate Limelight 3A vision system for FTC robots. Use when working with AprilTag detection, MegaTag2 localization, color tracking, or shooter auto-aim. |
| [ncssm-robotics/ftc-claude](https://github.com/ncssm-robotics/ftc-claude) | [`roadrunner`](https://github.com/ncssm-robotics/ftc-claude/blob/HEAD/plugins/roadrunner/skills/roadrunner/SKILL.md) | Helps build autonomous routines using the RoadRunner 1.0 path planning library. |
| [nirobotics/cyber-power](https://github.com/nirobotics/cyber-power) | [`cyber-power-log-analysis`](https://github.com/nirobotics/cyber-power/blob/HEAD/.agents/skills/cyber-power-log-analysis/SKILL.md) | Inspect, validate, and troubleshoot WPILOG files produced by robots using NI EnergyLogger, and maintain Cyber Power's parser, CLI, Worker, metrics, and golden tests. Use for .wpilog compatibility questions, EnergyLogger  |
| [robototes/REBUILT2026](https://github.com/robototes/REBUILT2026) | [`frc-robot-safety-can`](https://github.com/robototes/REBUILT2026/blob/HEAD/skills/frc-robot-safety-can/SKILL.md) | Enforces motor current limits, brake/coast configurations, soft limits, and CAN bandwidth optimization for FRC mechanisms. |
| [robototes/REBUILT2026](https://github.com/robototes/REBUILT2026) | [`frc-student-mentor-persona`](https://github.com/robototes/REBUILT2026/blob/HEAD/skills/frc-student-mentor-persona/SKILL.md) | Governs review tone to ensure feedback acts as an encouraging, pedagogical mentor for high school student programmers. |
| [robototes/REBUILT2026](https://github.com/robototes/REBUILT2026) | [`frc-telemetry-units`](https://github.com/robototes/REBUILT2026/blob/HEAD/skills/frc-telemetry-units/SKILL.md) | Enforces WPILib Units library usage, structured logging practices, and clean SmartDashboard/AdvantageKit conventions. |
| [robototes/REBUILT2026](https://github.com/robototes/REBUILT2026) | [`frc-wpilib-architecture`](https://github.com/robototes/REBUILT2026/blob/HEAD/skills/frc-wpilib-architecture/SKILL.md) | Reviews FRC robot code for proper WPILib Command-Based paradigms, periodic loop efficiency, and command lifecycle safety. |
| [rylero/ClaudeScope](https://github.com/rylero/ClaudeScope) | [`replay`](https://github.com/rylero/ClaudeScope/blob/HEAD/skills/replay/SKILL.md) | Use when asked to replay an FRC log file through the robot code, re-run a real-world or simulation .wpilog deterministically, regenerate AdvantageKit outputs, or compare recorded outputs against recomputed ones. Trigger  |
| [rylero/ClaudeScope](https://github.com/rylero/ClaudeScope) | [`scope`](https://github.com/rylero/ClaudeScope/blob/HEAD/skills/scope/SKILL.md) | Use ClaudeScope to analyze FRC robot .wpilog files or query live NetworkTables. |
| [rylero/ClaudeScope](https://github.com/rylero/ClaudeScope) | [`simulate`](https://github.com/rylero/ClaudeScope/blob/HEAD/skills/simulate/SKILL.md) | Use when asked to simulate the FRC robot, test behavior in simulation, analyze robot performance without hardware, or run auto/teleop in a virtual environment. Trigger on: "/simulate", "run the sim", "test in simulation" |
| [rylero/RobotLogBot](https://github.com/rylero/RobotLogBot) | [`scope`](https://github.com/rylero/RobotLogBot/blob/HEAD/.claude/skills/scope/SKILL.md) | Use ClaudeScope to analyze FRC robot .wpilog files or query live NetworkTables. |
| [sahil-patel-2011/6925-RobotCodeUpdated](https://github.com/sahil-patel-2011/6925-RobotCodeUpdated) | [`frc-6925-robot`](https://github.com/sahil-patel-2011/6925-RobotCodeUpdated/blob/HEAD/.cursor/skills/frc-6925-robot/SKILL.md) | Guides FRC Team 6925 W.A.Robotics 2026 REBUILT robot code (Fuel shooter, |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-2027-preview`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-2027-preview/SKILL.md) | What changes for the 2027 FRC season (SystemCore controller, WPILib 2027 alpha, Java 25, org.wpilib packages, Commands v3, vendor alphas) and how to prepare a 2026 codebase. Version-sensitive: verify online first. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-autonomous`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-autonomous/SKILL.md) | Build reliable autos with PathPlanner (AutoBuilder, named commands, event triggers, pathfinding) or Choreo (AutoFactory, AutoRoutine), with correct alliance flipping and an auto chooser. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-build-deploy`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-build-deploy/SKILL.md) | Build, test, simulate and deploy WPILib robot code with ./gradlew through run_command, with the robot-safety checks that must come first. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-can-power`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-can-power/SKILL.md) | Keep the CAN bus and the battery healthy: bus utilization, status frame rates, CANivore, CAN error diagnosis, current limits and brownout prevention. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-command-based`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-command-based/SKILL.md) | Structure robot code with WPILib command-based (v2): subsystems with command factories, triggers, default commands, requirements, composition, and the mistakes that break it. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-competition-day`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-competition-day/SKILL.md) | Software checklist for an FRC event: before leaving, load-in, practice day, before each match, between matches, and after the event — versions, deploys, logs, field connection, radio and code freeze discipline. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-debugging`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-debugging/SKILL.md) | Debug robot problems fast: no robot code, robot won't move, mechanism runs away, CAN errors, loop overruns, comms drops, brownouts — using Driver Station messages, logs and targeted experiments. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-driver-controls`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-driver-controls/SKILL.md) | Design driver and operator controls: CommandXboxController bindings, deadbands, input shaping, slew limits, field-relative driving, rumble feedback, and a bindings map for the drive team. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-log-analysis`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-log-analysis/SKILL.md) | Diagnose a match or practice run from a .wpilog with the frc_code tool (wpilog_summary, wpilog_series), chart signals in a Page, and turn findings into fixes. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-logging-replay`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-logging-replay/SKILL.md) | Log everything useful: WPILib DataLog/Epilogue or AdvantageKit (IO layers, @AutoLog inputs, replay), CTRE hoot logs, and reading them in AdvantageScope. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-mechanisms`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-mechanisms/SKILL.md) | Recipes for elevator, arm/pivot, flywheel/shooter, intake and turret subsystems with command factories, safety limits, state triggers, and matching WPILib physics sims. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-motor-controllers`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-motor-controllers/SKILL.md) | Configure TalonFX/Kraken (CTRE Phoenix 6) and SPARK MAX/Flex (REVLib) correctly: config objects, inversion, neutral mode, current limits, closed loop, followers, status signals. 2026 APIs with version flags. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-pid-feedforward`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-pid-feedforward/SKILL.md) | Tune a mechanism the way top teams do: characterize with SysId, set feedforward (kS/kV/kA/kG), add motion profiling (Motion Magic, MAXMotion, ProfiledPIDController), then a small kP, with units checked. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-project-setup`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-project-setup/SKILL.md) | Create or audit a WPILib Java robot project: GradleRIO version, team number, folder layout, vendordeps (install, update, mismatches). |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-review-checklist`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-review-checklist/SKILL.md) | Review FRC robot code like a top team's software lead: run the project scan, then check units, inversions, conversions, requirements, blocking code, allocation in loops, enable/disable behavior, safety and style. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-sim-testing`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-sim-testing/SKILL.md) | Test robot code without a robot: WPILib simulation (physics sims, sim GUI, AdvantageScope), vendor sim support, and JUnit unit tests with the HAL and the CommandScheduler. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-start-here`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-start-here/SKILL.md) | Map of the FRC programming skills, the canonical resources, the 2026-vs-2027 version picture, and how to check an API before using it. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-swerve`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-swerve/SKILL.md) | Get a swerve drive driving well: choose CTRE generator vs AdvantageKit template vs YAGSL, set up modules/gyro/offsets, field-relative driving, odometry, and tune the modules. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-team-git`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-team-git/SKILL.md) | Git workflow for an FRC programming team: branches per feature, protected main that always builds, event branches and tags, reviews, what to ignore, and recovering at competition. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-units-constants`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-units-constants/SKILL.md) | Keep units straight in robot code: WPILib Units library (Measure types), constants classes, naming with units, conversion factors, and the classic unit bugs. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-code-vision`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-code-vision/SKILL.md) | Fuse AprilTag vision into the pose estimator with PhotonVision (2026 PhotonPoseEstimator API) or Limelight MegaTag2, with standard deviations that scale with distance and filters that reject bad measurements. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-design-electrical-layout`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-design-electrical-layout/SKILL.md) | Electrical layout for FRC mechanical designers: space and access for battery, main breaker, PDH, roboRIO, radio, CAN, wire gauge, routing and strain relief. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-design-pneumatics`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-design-pneumatics/SKILL.md) | FRC pneumatics basics and rules: legal system layout, pressures, cylinder force and air use, when pneumatics beat motors. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`frc-design-process`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/frc-design-process/SKILL.md) | FRC robot design process from kickoff to competition: requirements, archetypes, prototyping, layout, CAD, reviews and iteration. |
| [sahil-patel-2011/SahilBot](https://github.com/sahil-patel-2011/SahilBot) | [`java-kotlin-wpilib`](https://github.com/sahil-patel-2011/SahilBot/blob/HEAD/packs/skills/java-kotlin-wpilib/SKILL.md) | Work in Java or Kotlin Gradle projects, including FRC WPILib robots - gradlew build/test, JUnit 5, null safety, coroutines, robot loop rules. |
| [team3316/rebuilt-2026](https://github.com/team3316/rebuilt-2026) | [`mirror-auto`](https://github.com/team3316/rebuilt-2026/blob/HEAD/skills/mirror_auto/SKILL.md) | Mirrors an FRC PathPlanner auto routine to the opposite side of the field |
| [team5419/frc-2026](https://github.com/team5419/frc-2026) | [`analyze-wpilog`](https://github.com/team5419/frc-2026/blob/HEAD/resources/SKILL.md) | Analyze WPILOG log files from FRC robots using the wpilog-parser library with TypeScript. |
| [team581/bolt](https://github.com/team581/bolt) | [`access-fetch-logs`](https://github.com/team581/bolt/blob/HEAD/app/skills/access-fetch-logs/SKILL.md) | Find Team 581 WPILOG files, run metadata, and canvas notes in the Fetch GCS bucket. Use when a user asks about a Fetch-hosted log, match, recording, canvas, survey, metric, or timestamped note. |
| [team581/bolt](https://github.com/team581/bolt) | [`analyze-wpilog`](https://github.com/team581/bolt/blob/HEAD/app/skills/analyze-wpilog/SKILL.md) | Analyze WPILOG log files from FRC robots using the wpilog-parser library with TypeScript. |
| [team581/bolt](https://github.com/team581/bolt) | [`debug-hoot`](https://github.com/team581/bolt/blob/HEAD/app/skills/debug-hoot/SKILL.md) | Convert and troubleshoot CTRE Phoenix .hoot signal logs with owlet, then analyze the resulting WPILOG. Use when a user asks to inspect, debug, or analyze a .hoot file. |
| [team581/offseason-2026](https://github.com/team581/offseason-2026) | [`analyze-wpilog`](https://github.com/team581/offseason-2026/blob/HEAD/.agents/skills/analyze-wpilog/SKILL.md) | Analyze WPILOG log files from FRC robots using the wpilog-parser library with TypeScript. |
| [thehomelessguy/MARSLib](https://github.com/thehomelessguy/MARSLib) | [`marslib-autonomous`](https://github.com/thehomelessguy/MARSLib/blob/HEAD/.agents/skills/marslib-autonomous/SKILL.md) | Helps build autonomous code, PathPlanner macros, and GhostManager teleop replay commands in MARSLib. Use when creating paths, configuring AutoBuilder, adding named commands, or recording driver replays. |
| [thehomelessguy/MARSLib](https://github.com/thehomelessguy/MARSLib) | [`marslib-ci`](https://github.com/thehomelessguy/MARSLib/blob/HEAD/.agents/skills/marslib-ci/SKILL.md) | Helps understand build automations, Gradle tasks, Spotless linting formatting, and deployment workflows. Use when fixing build errors, running tests, or configuring CI pipelines. |
| [thehomelessguy/MARSLib](https://github.com/thehomelessguy/MARSLib) | [`marslib-control-theory`](https://github.com/thehomelessguy/MARSLib/blob/HEAD/.agents/skills/marslib-control-theory/SKILL.md) | Helps architect PID loops, Feedforwards, slew limiters, and kinematics in MARSLib mechanisms. Use when tuning mechanisms, implementing control loops, or working with swerve dynamics. |
| [thehomelessguy/MARSLib](https://github.com/thehomelessguy/MARSLib) | [`marslib-diagnostics`](https://github.com/thehomelessguy/MARSLib/blob/HEAD/.agents/skills/marslib-diagnostics/SKILL.md) | Helps architect pre-match system checks, hardware sweep assertions, and CAN bus dropout tracking. Use when adding health checks, creating fault alerts, or implementing pre-match test routines. |
| [thehomelessguy/MARSLib](https://github.com/thehomelessguy/MARSLib) | [`marslib-math`](https://github.com/thehomelessguy/MARSLib/blob/HEAD/.agents/skills/marslib-math/SKILL.md) | Helps process continuous math, regression logic, filtering, and vector transformations. Use when implementing interpolation maps, sensor filtering, projectile calculations, or geometric transforms. |
| [thehomelessguy/MARSLib](https://github.com/thehomelessguy/MARSLib) | [`marslib-mechanisms`](https://github.com/thehomelessguy/MARSLib/blob/HEAD/.agents/skills/marslib-mechanisms/SKILL.md) | Helps create new mechanism subsystems (elevators, arms, intakes, shooters) using the MARSLib IO abstraction pattern. Use when scaffolding new mechanisms, writing LinearMechanismIO or RotaryMechanismIO layers, or configur |
| [thehomelessguy/MARSLib](https://github.com/thehomelessguy/MARSLib) | [`marslib-network`](https://github.com/thehomelessguy/MARSLib/blob/HEAD/.agents/skills/marslib-network/SKILL.md) | Helps manage Python Coprocessor streams, NetworkTables 4 pipelines, and byte-array deserialization. Use when integrating coprocessors, reading NT4 data, or handling network resilience. |
| [thehomelessguy/MARSLib](https://github.com/thehomelessguy/MARSLib) | [`marslib-statemachine`](https://github.com/thehomelessguy/MARSLib/blob/HEAD/.agents/skills/marslib-statemachine/SKILL.md) | Helps build validated finite state machines using MARSStateMachine for subsystem coordination. Use when implementing multi-state mechanisms, sequencing logic, or any system that needs transition validation and logging. |
| [thehomelessguy/MARSLib](https://github.com/thehomelessguy/MARSLib) | [`marslib-swerve`](https://github.com/thehomelessguy/MARSLib/blob/HEAD/.agents/skills/marslib-swerve/SKILL.md) | Helps configure and extend the MARSLib swerve drivetrain subsystem. Use when modifying SwerveDrive, adding odometry sources, tuning PathPlanner, or configuring the PhoenixOdometryThread. |
| [thehomelessguy/MARSLib](https://github.com/thehomelessguy/MARSLib) | [`marslib-telemetry`](https://github.com/thehomelessguy/MARSLib/blob/HEAD/.agents/skills/marslib-telemetry/SKILL.md) | Helps manage logging and telemetry in MARSLib using AdvantageKit and AdvantageScope. Use when adding new log keys, debugging Sim/Real disparities, or enforcing replay-only algorithm constraints. |
| [thehomelessguy/MARSLib](https://github.com/thehomelessguy/MARSLib) | [`marslib-testing`](https://github.com/thehomelessguy/MARSLib/blob/HEAD/.agents/skills/marslib-testing/SKILL.md) | Helps write and maintain tests for MARSLib. Use this skill when writing JUnit 5 tests for WPILib commands, subsystems, or autonomous behaviors to ensure high-fidelity physical testing with dyn4j over mocked tests. |
| [thehomelessguy/MARSLib](https://github.com/thehomelessguy/MARSLib) | [`marslib-vision`](https://github.com/thehomelessguy/MARSLib/blob/HEAD/.agents/skills/marslib-vision/SKILL.md) | Helps implement or debug Vision localization, Megatag fusion, and origin coordinate spaces in MARSLib. Use when injecting AprilTag 3D Odometry poses, configuring VisionIO interfaces, or mapping vision data properly into  |
| [thehomelessguy/MARSLib](https://github.com/thehomelessguy/MARSLib) | [`marslib`](https://github.com/thehomelessguy/MARSLib/blob/HEAD/.agents/skills/marslib/SKILL.md) | Helps write FRC robot code using the MARSLib Advanced Simulation and Abstraction framework. Use when creating new subsystems, writing hardware IO layers, configuring Dyn4j simulation physics, or setting up fault manageme |
| [unlabs-llc/unlabs-ai-toolkit-frc-2027](https://github.com/unlabs-llc/unlabs-ai-toolkit-frc-2027) | [`current-claude-models`](https://github.com/unlabs-llc/unlabs-ai-toolkit-frc-2027/blob/HEAD/baseline/skills/current-claude-models/SKILL.md) | Use when choosing a model or an effort level for robot-code work, when any answer would name a Claude model, or before pinning a model anywhere — this is the single source of truth for Claude model identifiers, effort le |
| [unlabs-llc/unlabs-ai-toolkit-frc-2027](https://github.com/unlabs-llc/unlabs-ai-toolkit-frc-2027) | [`current-wpilib-2027`](https://github.com/unlabs-llc/unlabs-ai-toolkit-frc-2027/blob/HEAD/baseline/skills/current-wpilib-2027/SKILL.md) | Use when generating, reviewing, or explaining WPILib 2027 robot code; load this skill before any answer names a WPILib package, class, or Gradle task. It records volatile WPILib 2027 facts and available verification cont |
| [unlabs-llc/unlabs-ai-toolkit-frc-2027](https://github.com/unlabs-llc/unlabs-ai-toolkit-frc-2027) | [`frc-pre-deploy`](https://github.com/unlabs-llc/unlabs-ai-toolkit-frc-2027/blob/HEAD/baseline/skills/frc-pre-deploy/SKILL.md) | Use before deploying robot code to validate it — runs compile, unit tests, a bounded simulation launch, a warning check and a git-cleanliness check, then writes one machine-readable verdict for the team to read. Invoke i |
| [yeti-robotics/rebuilt-2026](https://github.com/yeti-robotics/rebuilt-2026) | [`advantagekit`](https://github.com/yeti-robotics/rebuilt-2026/blob/HEAD/.agents/skills/advantagekit/SKILL.md) | AdvantageKit logging framework best practices for FRC Java robots (2026 / AKit 4.x). Use when |
| [yeti-robotics/rebuilt-2026](https://github.com/yeti-robotics/rebuilt-2026) | [`wpilib-best-practices`](https://github.com/yeti-robotics/rebuilt-2026/blob/HEAD/.agents/skills/wpilib-best-practices/SKILL.md) | WPILib and FRC robot programming best practices, design patterns, and code guidance for Java. |
