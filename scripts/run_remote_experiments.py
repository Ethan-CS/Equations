#!/usr/bin/env python3
"""Launch runtime experiments across the fatanode cluster via SSH."""
from __future__ import annotations

import argparse
import asyncio
import datetime as dt
import os
import shlex
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, List

DEFAULT_NODES = [f"fatanode-{i:02d}" for i in range(1, 19)]


@dataclass
class NodeResult:
    node: str
    returncode: int
    stdout: str
    stderr: str

    def succeeded(self) -> bool:
        return self.returncode == 0


def parse_args(argv: Iterable[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--nodes", type=str,
                        help="Comma-separated list of compute nodes (default: fatanode-01..18)")
    parser.add_argument("--user", type=str, default=os.getenv("USER", "ekelly"),
                        help="SSH username (default: current user)")
    parser.add_argument("--gateway", type=str,
                        help="Optional SSH gateway hostname (used for ProxyJump)")
    parser.add_argument("--remote-root", type=str,
                        help="Path to repository on remote nodes (default: inferred from local path)")
    parser.add_argument("--config", type=str, default="config/runtime_plan.csv",
                        help="Relative path to experiment plan inside the repo")
    parser.add_argument("--output", type=str, default="results/runtime",
                        help="Output directory inside the repo (shared storage recommended)")
    parser.add_argument("--run-id", type=str,
                        help="Override run identifier (default: runtime-<timestamp>)")
    parser.add_argument("--parallelism", type=int, default=32,
                        help="Worker threads per node for the Java runner")
    parser.add_argument("--timeout-seconds", type=int, default=1800,
                        help="Per-phase timeout passed to the Java runner")
    parser.add_argument("--ode-horizon", type=int, default=10,
                        help="ODE horizon passed to the Java runner")
    parser.add_argument("--slice-count", type=int,
                        help="Total number of slices; defaults to number of nodes")
    parser.add_argument("--java-opts", type=str, default="-Xms4g -Xmx32g -XX:-UseParallelGC",
                        help="Additional JVM options (quoted as a single string)")
    parser.add_argument("--infection-rate", type=float, default=0.6,
                        help="Infection rate (beta) for the SIR model")
    parser.add_argument("--recovery-rate", type=float, default=0.1,
                        help="Recovery rate (gamma) for the SIR model")
    parser.add_argument("--seed", type=int, default=None,
                        help="Base seed; defaults to current timestamp")
    parser.add_argument("--tag-prefix", type=str, default="",
                        help="Prefix for per-node tag passed to the Java runner")
    parser.add_argument("--dry-run", action="store_true",
                        help="Print planned commands without executing them")
    parser.add_argument("--verbose", action="store_true",
                        help="Echo remote stdout/stderr to console as well as logs")
    return parser.parse_args(list(argv))


def ensure_jar(repo_root: Path) -> Path:
    jar_path = repo_root / "target" / "equations-runtime.jar"
    if not jar_path.exists():
        raise FileNotFoundError(
            f"Runtime jar not found at {jar_path}. Run scripts/build_runtime.sh before launching remote jobs.")
    return jar_path


def determine_remote_root(args: argparse.Namespace, repo_root: Path) -> Path:
    if args.remote_root:
        return Path(args.remote_root)
    return repo_root


def build_run_id(args: argparse.Namespace) -> str:
    if args.run_id:
        return args.run_id
    timestamp = dt.datetime.now().strftime("%Y%m%d-%H%M%S")
    return f"runtime-{timestamp}"


def resolve_nodes(args: argparse.Namespace) -> List[str]:
    if args.nodes:
        return [node.strip() for node in args.nodes.split(",") if node.strip()]
    return list(DEFAULT_NODES)


async def run_on_node(node: str,
                      slice_index: int,
                      slice_count: int,
                      args: argparse.Namespace,
                      run_id: str,
                      remote_root: Path,
                      log_dir: Path) -> NodeResult:
    tag = f"{args.tag_prefix}{node}"
    jar_remote = remote_root / "target" / "equations-runtime.jar"
    config_remote = remote_root / args.config
    output_remote = remote_root / args.output
    seed_for_node = args.seed + slice_index

    command_parts = [
        f"cd {shlex.quote(str(remote_root))}",
        f"java {args.java_opts} -jar {shlex.quote(str(jar_remote))}",
        f"--config {shlex.quote(str(config_remote))}",
        f"--output {shlex.quote(str(output_remote))}",
        f"--run-id {shlex.quote(run_id)}",
        f"--slice-index {slice_index}",
        f"--slice-count {slice_count}",
        f"--parallelism {args.parallelism}",
        f"--timeout-seconds {args.timeout_seconds}",
        f"--ode-horizon {args.ode_horizon}",
        f"--infection-rate {args.infection_rate}",
        f"--recovery-rate {args.recovery_rate}",
        f"--tag {shlex.quote(tag)}",
        f"--seed {seed_for_node}"
    ]
    remote_sequence = " && ".join(command_parts)
    ssh_command = ["ssh"]
    if args.gateway:
        ssh_command.extend(["-J", f"{args.user}@{args.gateway}"])
    ssh_command.append(f"{args.user}@{node}")
    ssh_command.extend(["bash", "-lc", shlex.quote(remote_sequence)])

    if args.dry_run:
        print(f"[DRY-RUN] {' '.join(ssh_command)}")
        return NodeResult(node=node, returncode=0, stdout="", stderr="")

    process = await asyncio.create_subprocess_exec(
        *ssh_command,
        stdout=asyncio.subprocess.PIPE,
        stderr=asyncio.subprocess.PIPE,
    )
    stdout_bytes, stderr_bytes = await process.communicate()
    stdout_text = stdout_bytes.decode("utf-8", errors="replace")
    stderr_text = stderr_bytes.decode("utf-8", errors="replace")

    log_dir.mkdir(parents=True, exist_ok=True)
    log_path = log_dir / f"{node}.log"
    with log_path.open("w", encoding="utf-8") as handle:
        handle.write("# Command\n")
        handle.write(" ".join(ssh_command) + "\n\n")
        handle.write("# STDOUT\n")
        handle.write(stdout_text)
        handle.write("\n# STDERR\n")
        handle.write(stderr_text)

    if args.verbose:
        print(f"===== {node} stdout =====")
        print(stdout_text)
        if stderr_text.strip():
            print(f"===== {node} stderr =====", file=sys.stderr)
            print(stderr_text, file=sys.stderr)

    return NodeResult(node=node, returncode=process.returncode, stdout=stdout_text, stderr=stderr_text)


async def orchestrate(args: argparse.Namespace) -> int:
    repo_root = Path(__file__).resolve().parents[1]
    ensure_jar(repo_root)
    remote_root = determine_remote_root(args, repo_root)
    if args.seed is None:
        args.seed = int(dt.datetime.now().timestamp())
    run_id = build_run_id(args)
    nodes = resolve_nodes(args)
    slice_count = args.slice_count or len(nodes)

    print(f"Launching run {run_id} across {len(nodes)} nodes (slice-count={slice_count})")

    log_dir = repo_root / "logs" / run_id
    tasks = [
    run_on_node(node, idx % slice_count, slice_count, args, run_id, remote_root, log_dir)
        for idx, node in enumerate(nodes)
    ]

    results = await asyncio.gather(*tasks)

    failures = [result for result in results if not result.succeeded()]
    if failures:
        print(f"{len(failures)} nodes reported errors:")
        for failure in failures:
            print(f"  - {failure.node}: exit {failure.returncode}")
        return 1

    print("All remote tasks completed successfully.")
    return 0


def main(argv: Iterable[str]) -> int:
    args = parse_args(argv)
    try:
        return asyncio.run(orchestrate(args))
    except FileNotFoundError as exc:
        print(exc, file=sys.stderr)
        return 1
    except KeyboardInterrupt:
        print("Interrupted", file=sys.stderr)
        return 130


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
