#!/usr/bin/env bash
set -euo pipefail
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

if ! command -v mvn >/dev/null 2>&1; then
  echo "Maven (mvn) is required to build the runtime experiments jar." >&2
  exit 1
fi

mvn -q -DskipTests package
JAR_PATH="target/equations-runtime.jar"
if [[ ! -f "$JAR_PATH" ]]; then
  echo "Expected jar not found at $JAR_PATH" >&2
  exit 1
fi

echo "Built runtime jar at $JAR_PATH"
