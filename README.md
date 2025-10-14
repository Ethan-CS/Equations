# Equations

![Java](https://img.shields.io/badge/Java-11%2B-blue)
![Build](https://img.shields.io/badge/Build-Gradle-brightgreen)
[![Licence: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENCE)
[![Citation](https://img.shields.io/badge/citation-CITATION.cff-blue)](CITATION.cff)

Codebase to generate and solve systems of ODEs that describe the dynamics of specified compartmental models on graphs.

## Requirements
- Java 11+ (since the remote cluster we use for experiments uses Java 11)
- Gradle Wrapper (included)

## Build

Build a runnable fat jar:

```bash
./gradlew clean runtimeJar
```

This is built to `build/libs/equations-runtime.jar`.

## Run locally

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

## Remote experiments

In case it's helpful, this is the process we follow to deploy experiments on the `fatanode` cluster (School of Computing Science, University of Glasgow).

1. Copy the locally built jar to the head node (likely too large for sensible transfer via git):
   ```bash
   scp build/libs/equations-runtime.jar <user>@<ssh-address>:~/Equations/build/libs/
   ```
2. Launch across nodes using the orchestrator - first, ssh to remote and cd to the directory containing the clone of this repo. Then, e.g. if you wanted to run on fatanodes 01 to 06:
   ```bash
   python3 scripts/run_remote_experiments.py \
     --nodes fatanode-01,fatanode-02,fatanode-03,fatanode-04,fatanode-05,fatanode-06 \
     --parallelism 32 \
     --timeout-seconds 1800 \
     --java-opts "-Xms4g -Xmx32g -XX:-UseParallelGC" \
     --output results/runtime \
     --tag-prefix fatanode- \
     --verbose
   ```

Notes:
- The orchestrator buffers per-node output; logs are written at completion to `logs/<run-id>/<node>.log`.
- Progress can be monitored by checking `results/runtime/<run-id>/` on the nodes.

## Tests

Run tests with:
```bash
./gradlew test
```

## Architecture

```mermaid
flowchart TD
    A[RuntimeExperimentRunner
    (experiments)] -->|builds| B[Graph
    (graph)]
    A -->|parses| C[ExperimentSpec
    (experiments)]
    A -->|configures| D[ModelParams/ODESystem
    (model)]
    A -->|executes| E[Simulation/SimulationSeries
    (simulation)]
    E -->|produces| F[ExperimentMeasurement
    (experiments)]
    F -->|aggregates| G[summary/raw CSV
    (results)]
    B <--generates--> H[GraphGenerator/GraphUtils
    (graph)]
    D --> I[ODEUtils/UpperBound
    (results)]
```

Key data flow:
- CLI options -> workload plan -> per-iteration runs (full and reduced ODEs) -> raw rows -> aggregated summary.
- Outputs are written per run-id under `results/runtime/<run-id>/`.

## Example outputs

Directory layout after a run (example):

```
results/
  runtime/
    runtime-20251013-221949/
      raw.csv
      summary.csv
      metadata.json
      plan.csv
```

Sample `summary.csv` (columns abbreviated):

```
graph_type,label,num_vertices,phase,successes,timeouts,errors,avg_ms,p95_ms
erdos_renyi,er-1000-p-0.01,1000,FULL,48,2,0,812.4,1325.8
erdos_renyi,er-1000-p-0.01,1000,REDUCED,50,0,0,121.3,180.2
```

Sample `metadata.json` keys:

```
{
  "runId": "runtime-20251013-221949",
  "parallelism": 32,
  "timeoutSeconds": 1800,
  "odeHorizon": 10,
  "slice": { "index": 0, "count": 6 },
  "javaOpts": "-Xms4g -Xmx32g -XX:-UseParallelGC"
}
```

## Citation

Please see `CITATION.cff` in the repository root.

## License

This project is licensed under the MIT Licence. See the `LICENCE` file for details.

## Contributing

Issues and pull requests are welcome. If contributing code:
- Open an issue first for larger changes to discuss the approach
- Keep PRs focused and include tests where reasonable
- Avoid committing large build artifacts (see `.gitignore`)
