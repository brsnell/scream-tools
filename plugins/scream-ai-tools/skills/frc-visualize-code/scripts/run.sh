#!/usr/bin/env bash
# SCREAM AI Tools — frc-visualize-code launcher
# SPDX-License-Identifier: MIT
#
# Finds a JDK (17 or newer) and runs FrcVisualize.java with the source-file launcher. No network,
# no build step. Arguments are passed through: [--project DIR] [--out DIR] [--root CLASS]
#
# JDK search order:
#   1. $FRC_VISUALIZE_JAVA (path to a java executable), if set
#   2. the JDK the WPILib installer ships, newest year first
#   3. $JAVA_HOME
#   4. java on PATH
set -euo pipefail

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
tool="$here/FrcVisualize.java"

java_major() {
  # Prints the feature version (e.g. 17, 21, 25) of the given java, or nothing.
  "$1" -version 2>&1 | grep -Eo 'version "[0-9]+' | head -n1 | grep -Eo '[0-9]+$' || true
}

is_usable_jdk() {
  local java="$1"
  [ -n "$java" ] && [ -x "$java" ] || return 1
  local dir major
  dir="$(dirname "$java")"
  # A JRE has no javac and no parser; the tool needs a full JDK.
  [ -x "$dir/javac" ] || [ -x "$dir/javac.exe" ] || return 1
  major="$(java_major "$java")"
  [ -n "$major" ] && [ "$major" -ge 17 ]
}

candidates=()
[ -n "${FRC_VISUALIZE_JAVA:-}" ] && candidates+=("$FRC_VISUALIZE_JAVA")

wpilib_roots=(
  "$HOME/wpilib"                                  # macOS, older Linux installs
  "${XDG_DATA_HOME:-$HOME/.local/share}/wpilib"   # Linux, WPILib 2027+
  "/c/Users/Public/wpilib"                        # Windows (Git Bash)
)
if [ -n "${PUBLIC:-}" ] && command -v cygpath >/dev/null 2>&1; then
  wpilib_roots+=("$(cygpath -u "$PUBLIC")/wpilib")
fi
for root in "${wpilib_roots[@]}"; do
  [ -d "$root" ] || continue
  # Newest year first (directory names start with the year, e.g. 2027_alpha7, 2026).
  while IFS= read -r year; do
    for exe in "$year/jdk/bin/java" "$year/jdk/bin/java.exe" "$year/jdk/Contents/Home/bin/java"; do
      [ -e "$exe" ] && candidates+=("$exe")
    done
  done < <(find "$root" -mindepth 1 -maxdepth 1 -type d 2>/dev/null | sort -r)
done

if [ -n "${JAVA_HOME:-}" ]; then
  jh="$JAVA_HOME"
  # On Windows, JAVA_HOME is a C:\ path; Git Bash needs /c/... to test and run it.
  if command -v cygpath >/dev/null 2>&1; then jh="$(cygpath -u "$jh")"; fi
  candidates+=("$jh/bin/java" "$jh/bin/java.exe")
fi
if command -v java >/dev/null 2>&1; then
  candidates+=("$(command -v java)")
fi

java=""
for c in "${candidates[@]}"; do
  if is_usable_jdk "$c"; then
    java="$c"
    break
  fi
done

if [ -z "$java" ]; then
  echo "frc-visualize-code: no JDK 17+ found." >&2
  echo "  Install WPILib (it ships a JDK under ~/wpilib/<year>/jdk), or set JAVA_HOME," >&2
  echo "  or set FRC_VISUALIZE_JAVA to the path of a JDK's java executable." >&2
  exit 4
fi

# A larger thread stack keeps deeply nested source (long builder chains) from overflowing the parser.
exec "$java" -Xss16m "$tool" "$@"
