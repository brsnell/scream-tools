#!/usr/bin/env bash
# Runs frc-visualize-code on the fixture robots and checks what it extracts.
# Usage: tests/run-tests.sh            (needs a JDK 17+; uses the same JDK search as the skill)
set -euo pipefail

repo="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
run="$repo/plugins/scream-tools/skills/frc-visualize-code/scripts/run.sh"
fixtures="$repo/tests/fixtures"
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT

pass=0
fail=0
check() { # description, file, fixed string expected in it
  if grep -qF -- "$3" "$2"; then pass=$((pass + 1)); else
    fail=$((fail + 1)); echo "FAIL: $1"; echo "      expected to find: $3"; fi
}
check_not() {
  if grep -qE -- "$3" "$2"; then
    fail=$((fail + 1)); echo "FAIL: $1"; echo "      did not expect: $3"; else pass=$((pass + 1)); fi
}

for fx in v2-command-robot v3-mechanism-robot; do
  "$run" --project "$fixtures/$fx" --out "$work/$fx" > "$work/$fx.log" 2>&1 || {
    echo "FAIL: tool exited non-zero on $fx"; cat "$work/$fx.log"; exit 1; }
  for f in index.html structure.svg structure-full.svg structure.json; do
    [ -s "$work/$fx/$f" ] && pass=$((pass + 1)) || { fail=$((fail + 1)); echo "FAIL: $fx missing $f"; }
  done
  # Offline: the page must not load anything from the network.
  check_not "$fx page loads no external scripts" "$work/$fx/index.html" '<script[^>]+src=|<link[^>]+href="https?:'
  # Deterministic: a second run gives byte-identical output.
  "$run" --project "$fixtures/$fx" --out "$work/$fx-again" > /dev/null 2>&1
  if diff -r "$work/$fx" "$work/$fx-again" > /dev/null; then pass=$((pass + 1)); else
    fail=$((fail + 1)); echo "FAIL: $fx output differs between two runs"; fi
done

j="$work/v2-command-robot/structure.json"
check "v2 framework detected" "$j" '"commandFramework": "Commands v2"'
check "v2 namespace detected" "$j" '"wpilibNamespace": "edu.wpi.first (2026 and earlier)"'
check "v2 WPILib version read" "$j" '"wpilibVersion": "2026.1.1"'
check "v2 team number read" "$j" '"teamNumber": "9999"'
check "v2 root is Robot" "$j" '"root": "frc.robot.Robot"'
check "v2 SubsystemBase subclass is a subsystem" "$j" '"category": "subsystem"'
check "v2 Command subclass is a command" "$j" '"id": "frc.robot.commands.ScoreCommand"'
check "v2 Xbox port resolved through a constant" "$j" '"port": "0"'
check "v2 Joystick port resolved" "$j" '"port": "1"'
check "v2 composed trigger" "$j" '"trigger": "b + leftBumper"'
check "v2 toggle binding" "$j" '"binding": "toggleOnTrue"'
check "v2 JoystickButton binding" "$j" '"trigger": "button 3"'
check "v2 custom Trigger binding" "$j" '"trigger": "custom: intake::hasPiece"'
check "v2 static factory followed to its subsystems" "$j" '"Autos.scoreAndBackUp"'
check "v2 default command found" "$j" '"mechanismField": "drivetrain"'
check "v2 nested IO class is a mechanism part" "$j" '"category": "subsystem-part"'

j="$work/v3-mechanism-robot/structure.json"
check "v3 framework detected" "$j" '"commandFramework": "Commands v3"'
check "v3 namespace detected" "$j" '"wpilibNamespace": "org.wpilib (2027+)"'
check "v3 Mechanism implementer is a subsystem" "$j" '"id": "org.example.robot.mechanisms.Elevator"'
check "v3 negated trigger" "$j" '"trigger": "start + not back"'
check "v3 face button hint" "$j" '"triggerHint": "faceDown = south face button (A on Xbox)"'
check "v3 port from same-class constant" "$j" '"port": "0"'
check "v3 superstructure command reaches both mechanisms" "$j" '"Superstructure.scoreHigh"'
check "v3 default command set inside the mechanism" "$j" '"mechanism": "org.example.robot.mechanisms.Shooter"'

echo "$pass passed, $fail failed"
[ "$fail" -eq 0 ]
