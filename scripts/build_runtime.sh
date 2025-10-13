#!/usr/bin/env bash
set -euo pipefail
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

GRADLEW_PATH="$REPO_ROOT/gradlew"
if [[ -x "$GRADLEW_PATH" ]]; then
  GRADLE_CMD="$GRADLEW_PATH"
elif command -v gradle >/dev/null 2>&1; then
  GRADLE_CMD="gradle"
else
  echo "Gradle is required to build the runtime experiments jar." >&2
  exit 1
fi

"$GRADLE_CMD" --no-daemon -q runtimeJar
JAR_PATH="build/libs/equations-runtime.jar"
if [[ ! -f "$JAR_PATH" ]]; then
  echo "Expected jar not found at $JAR_PATH" >&2
  exit 1
fi

echo "Built runtime jar at $JAR_PATH"
