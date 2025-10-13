# Remote Runtime Experiments

This project now exposes a dedicated runtime harness that scales to larger graphs and can be scheduled across the fatanode cluster.

## Components

- `src/io/github/ethankelly/experiments/RuntimeExperimentRunner.java`: configurable Java entry-point that builds graph workloads, executes both the full and reduced ODE systems, and captures rich metrics.
- `config/runtime_plan.csv`: default experiment catalogue. Each row defines a graph family, vertex count, iteration budget, and any required parameters (for example `probability` for Erdos-Renyi graphs or `degree` for random regular graphs).
- `build/libs/equations-runtime.jar`: shaded jar produced by Gradle containing the runner and dependencies.
- `scripts/run_remote_experiments.py`: SSH orchestrator that slices the plan across the compute nodes and launches the Java runner with consistent JVM flags for the cluster.
- `scripts/build_runtime.sh`: helper that invokes Gradle (`gradle runtimeJar`) and confirms the runtime jar is ready.

## Building locally

```bash
./scripts/build_runtime.sh
```

The command assembles `build/libs/equations-runtime.jar`, a self-contained jar with dependencies and the manifest set to `io.github.ethankelly.experiments.RuntimeExperimentRunner`.

## Running an experiment locally

```bash
java -Xms4g -Xmx32g -XX:-UseParallelGC -jar build/libs/equations-runtime.jar \
  --config config/runtime_plan.csv \
  --output results/runtime \
  --run-id runtime-dev \
  --parallelism 8 \
  --timeout-seconds 900 \
  --infection-rate 0.6 \
  --recovery-rate 0.1
```

Outputs are written to `results/runtime/<run-id>/` and include:

- `raw.csv`: one row per iteration/phase (full vs reduced) with timings, tuple counts, and status.
- `summary.csv`: aggregated statistics (success, timeout, error counts; averages; p95 durations).
- `metadata.json`: provenance for the run (timestamps, CLI options, slice info, etc.).
- `plan.csv`: a verbatim copy of the plan file that was executed.

Use `--dry-run` to inspect the plan without running or `--help` to see all CLI options.

## Updating the plan

Each row in `config/runtime_plan.csv` must contain:

```
graph_type,label,num_vertices,iterations,parameters
```

- `graph_type`: one of `erdos_renyi`, `random_regular`, `barabasi_albert`, `tree`, `binary_tree`, `cycle`, or `complete` (aliases supported).
- `label`: optional human-readable tag used in outputs.
- `num_vertices`: integer vertex count.
- `iterations`: number of independent graphs sampled for that row.
- `parameters`: semicolon-separated key/value pairs. Required keys depend on the graph family (`probability`, `degree`, `attachment`, etc.). Leave blank if not needed.

Add or remove rows as needed for new graph families or vertex scales.

## Deploying to the fatanodes

1. **Connect** to the head node: `ssh ekelly@fatanode-head` (or via the institutional gateway).
2. **Build once** from the shared home directory (visible to all nodes):
   ```bash
   cd ~/Equations
   ./scripts/build_runtime.sh
   ```
3. **Launch experiments** using the orchestrator:
   ```bash
   python3 scripts/run_remote_experiments.py \
     --parallelism 48 \
     --timeout-seconds 1800 \
     --java-opts "-Xms4g -Xmx32g -XX:-UseParallelGC" \
     --output results/runtime \
     --tag-prefix fatanode- \
     --verbose
   ```

   - By default the script targets all 18 compute nodes (`fatanode-01` .. `fatanode-18`).
   - Use `--nodes fatanode-01,fatanode-02` to restrict the set, or `--slice-count` to over-/under-subscribe slices relative to the node count.
   - The script automatically derives per-node slice indices so that each node processes a disjoint subset of the plan. Adjust `config/runtime_plan.csv` or `--slice-count` when the plan grows.
   - JVM flags follow the cluster recommendation: disable parallel GC and cap the heap at 32 GiB. Modify `--java-opts` if tighter limits are needed.

4. **Monitor progress**: command output is captured per node under `logs/<run-id>/<node>.log` on the launching machine. Each node logs stdout/stderr from the Java process plus the exact SSH command used.

5. **Collect results**: the runner writes into `results/runtime/<run-id>/` on the shared filesystem (`fatanode-data` accessible from all nodes). Merge or analyse with your preferred tooling (Python/pandas, R, etc.).

### Practical tips for the cluster

- Keep `--parallelism` conservative (e.g., 32-72) to balance per-node CPU usage with JVM memory consumption. The Xeon E5-2697A v4 has hyperthreads, but spreading across too many threads increases GC overhead.
- Set `--timeout-seconds` high enough for the largest graphs (the harness records a `timeout` status if the observed duration exceeds the threshold; it does not forcibly interrupt the iteration).
- For reproducibility, pass a fixed `--seed` when running multiple slices.
- Use `--dry-run` to confirm the generated commands before a long launch. Combine with `--nodes fatanode-01` for smoke tests on a single worker.

## Extending further

- Add new graph families by extending `GraphType` and providing the necessary generator inside `GraphGenerator`.
- Expand the plan CSV with additional parameter keys; they are exposed via `ExperimentSpec` and will appear in output metadata.
- Integrate with a scheduler (e.g., Slurm) by wrapping `scripts/run_remote_experiments.py` inside job scripts, or replace the SSH launcher if the cluster offers a job queue.

Feel free to adjust JVM flags or orchestration behaviour; the scripts are standard Bash/Python and designed for easy adaptation.
