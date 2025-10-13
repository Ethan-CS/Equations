package io.github.ethankelly.experiments;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.model.ModelParams;
import io.github.ethankelly.model.ODESystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static io.github.ethankelly.experiments.ExperimentMeasurement.ExperimentStatus;
import static io.github.ethankelly.experiments.ExperimentMeasurement.Phase;

/**
 * Orchestrates large-scale runtime experiments across a configurable set of graph families.
 */
public final class RuntimeExperimentRunner {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(10);
    private static final int DEFAULT_ODE_HORIZON = 10;
    private static final Path DEFAULT_CONFIG = Path.of("config/runtime_plan.csv");
    private static final Path DEFAULT_OUTPUT_ROOT = Path.of("results/runtime");
    private static final DateTimeFormatter RUN_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
            .withZone(ZoneId.systemDefault());

    public static void main(String[] args) {
        try {
            CliOptions options = CliOptions.parse(args);
            if (options.help()) {
                printUsage();
                return;
            }
            new RuntimeExperimentRunner().execute(options);
        } catch (Exception ex) {
            System.err.println("Experiment run failed: " + ex.getMessage());
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void execute(CliOptions options) throws IOException, InterruptedException {
        Instant startedAt = Instant.now();
        ExperimentPlan plan = ExperimentPlan.fromCsv(options.configPath());
        List<ExperimentSpec> workload = plan.slice(options.sliceIndex(), options.sliceCount());

        if (workload.isEmpty()) {
            System.out.println("No experiments scheduled for slice " + options.sliceIndex());
            return;
        }

        if (options.dryRun()) {
            System.out.println("Dry-run mode: the following experiments would be executed:");
            workload.forEach(spec -> System.out.printf(Locale.ROOT,
                    "  - %s (%s) iterations=%d params=%s%n",
                    spec.label(), spec.graphType().displayName(), spec.iterations(), spec.parameterSignature()));
            System.out.printf(Locale.ROOT, "Total iterations: %d%n", workload.stream().mapToInt(ExperimentSpec::iterations).sum());
            return;
        }

        Path runRoot = prepareOutputDirectory(options);
        Files.createDirectories(runRoot);
        Files.copy(options.configPath(), runRoot.resolve("plan.csv"), StandardCopyOption.REPLACE_EXISTING);

        ModelParams modelParams = buildModelParams(options);
        ExecutorService executor = Executors.newFixedThreadPool(options.parallelism());
        CompletionService<List<ExperimentMeasurement>> completionService = new ExecutorCompletionService<>(executor);

        AtomicLong seedCounter = new AtomicLong(options.seed());
        int totalTasks = submitTasks(workload, options, modelParams, completionService, seedCounter);
        executor.shutdown();

        List<ExperimentMeasurement> measurements = collectResults(options, completionService, totalTasks);
        writeOutputs(options, runRoot, measurements, startedAt);
    }

    private int submitTasks(List<ExperimentSpec> workload,
                            CliOptions options,
                            ModelParams modelParams,
                            CompletionService<List<ExperimentMeasurement>> completionService,
                            AtomicLong seedCounter) {
        AtomicInteger submitted = new AtomicInteger();
        workload.forEach(spec -> {
            for (int iteration = 0; iteration < spec.iterations(); iteration++) {
                final int iterationIndex = iteration;
                final long iterationSeed = seedCounter.getAndIncrement();
                Callable<List<ExperimentMeasurement>> task = () -> runIteration(spec, iterationIndex, iterationSeed, options, modelParams);
                completionService.submit(task);
                submitted.incrementAndGet();
            }
        });
        return submitted.get();
    }

    private List<ExperimentMeasurement> collectResults(CliOptions options,
                                                       CompletionService<List<ExperimentMeasurement>> completionService,
                                                       int totalTasks) throws InterruptedException {
        List<ExperimentMeasurement> all = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger completed = new AtomicInteger();
        for (int i = 0; i < totalTasks; i++) {
            Future<List<ExperimentMeasurement>> future = completionService.take();
            try {
                List<ExperimentMeasurement> result = future.get();
                all.addAll(result);
            } catch (ExecutionException ex) {
                Throwable cause = ex.getCause();
                System.err.println("Task failed: " + cause.getMessage());
                cause.printStackTrace(System.err);
            }
            int done = completed.incrementAndGet();
            if (done % Math.max(1, totalTasks / 10) == 0 || done == totalTasks) {
                System.out.printf(Locale.ROOT, "Progress: %d/%d iterations complete%n", done, totalTasks);
            }
        }
        return new ArrayList<>(all);
    }

    private List<ExperimentMeasurement> runIteration(ExperimentSpec spec,
                                                     int iteration,
                                                     long seed,
                                                     CliOptions options,
                                                     ModelParams modelParams) {
        Graph baseGraph = spec.graphType().createGraph(spec, seed);
        int edgeCount = baseGraph.getNumEdges();

        List<ExperimentMeasurement> results = new ArrayList<>(2);
        results.add(executePhase(spec, iteration, seed, options, modelParams, baseGraph.clone(), Phase.FULL, edgeCount));
        results.add(executePhase(spec, iteration, seed, options, modelParams, baseGraph.clone(), Phase.REDUCED, edgeCount));
        return results;
    }

    private ExperimentMeasurement executePhase(ExperimentSpec spec,
                                               int iteration,
                                               long seed,
                                               CliOptions options,
                                               ModelParams modelParams,
                                               Graph graph,
                                               Phase phase,
                                               int edgeCount) {
        long started = System.nanoTime();
        ExperimentStatus status = ExperimentStatus.SUCCESS;
        int tupleCount = 0;
        String message = "";
        try {
            ODESystem system = new ODESystem(graph, options.odeHorizon(), modelParams);
            if (phase == Phase.REDUCED) {
                system.reduce();
            }
            tupleCount = system.getTuples().size();
        } catch (Throwable t) {
            status = ExperimentStatus.ERROR;
            message = abbreviateMessage(t);
        }
        long durationNanos = System.nanoTime() - started;
        if (status == ExperimentStatus.SUCCESS) {
            if (Duration.ofNanos(durationNanos).compareTo(options.timeout()) > 0) {
                status = ExperimentStatus.TIMEOUT;
            }
        }
        return new ExperimentMeasurement(spec, iteration, phase, status, durationNanos, tupleCount, edgeCount, seed, message);
    }

    private void writeOutputs(CliOptions options,
                              Path runRoot,
                              List<ExperimentMeasurement> measurements,
                              Instant startedAt) throws IOException {
        Files.createDirectories(runRoot);
        Path rawPath = runRoot.resolve("raw.csv");
        Path summaryPath = runRoot.resolve("summary.csv");
        Path metadataPath = runRoot.resolve("metadata.json");

        writeRawCsv(options, rawPath, measurements);
        writeSummaryCsv(options, summaryPath, measurements);
        writeMetadata(options, metadataPath, measurements, startedAt);

        System.out.println("Run completed. Results written to " + runRoot.toAbsolutePath());
    }

    private static void writeRawCsv(CliOptions options, Path path, List<ExperimentMeasurement> measurements) throws IOException {
        Files.createDirectories(path.getParent());
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
            writer.println("run_id,node_tag,graph_type,label,num_vertices,parameters,iteration,phase,status,duration_millis,tuples,edges,seed,message");
            for (ExperimentMeasurement measurement : measurements) {
                writer.printf(Locale.ROOT,
                        "%s,%s,%s,%s,%d,%s,%d,%s,%s,%.3f,%d,%d,%d,%s%n",
                        options.runId(),
                        safeCsv(options.tag()),
                        safeCsv(measurement.spec().graphType().displayName()),
                        safeCsv(measurement.spec().label()),
                        measurement.spec().numVertices(),
                        safeCsv(measurement.spec().parameterSignature()),
                        measurement.iteration(),
                        measurement.phase().name().toLowerCase(Locale.ROOT),
                        measurement.statusLabel(),
                        measurement.durationMillis(),
                        measurement.tupleCount(),
                        measurement.edgeCount(),
                        measurement.seed(),
                        safeCsv(measurement.message())
                );
            }
        }
    }

    private static void writeSummaryCsv(CliOptions options, Path path, List<ExperimentMeasurement> measurements) throws IOException {
        List<SummaryAccumulator> accumulators = buildSummaries(measurements);
        accumulators.sort(Comparator.comparing(acc -> acc.spec.label()));

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
            writer.println("run_id,node_tag,graph_type,label,num_vertices,parameters,phase,total,successes,timeouts,errors,avg_duration_millis,p95_duration_millis,max_duration_millis,min_duration_millis,avg_tuples,max_tuples,min_tuples,avg_edges");
            for (SummaryAccumulator accumulator : accumulators) {
                writer.printf(Locale.ROOT,
                        "%s,%s,%s,%s,%d,%s,%s,%d,%d,%d,%d,%.3f,%.3f,%.3f,%.3f,%.3f,%d,%d,%.3f%n",
                        options.runId(),
                        safeCsv(options.tag()),
                        safeCsv(accumulator.spec.graphType().displayName()),
                        safeCsv(accumulator.spec.label()),
                        accumulator.spec.numVertices(),
                        safeCsv(accumulator.spec.parameterSignature()),
                        accumulator.phase.name().toLowerCase(Locale.ROOT),
                        accumulator.total,
                        accumulator.successes,
                        accumulator.timeouts,
                        accumulator.errors,
                        accumulator.averageDuration(),
                        accumulator.p95Duration(),
                        accumulator.maxDuration(),
                        accumulator.minDuration(),
                        accumulator.averageTuples(),
                        accumulator.maxTuples(),
                        accumulator.minTuples(),
                        accumulator.averageEdges()
                );
            }
        }
    }

    private static List<SummaryAccumulator> buildSummaries(List<ExperimentMeasurement> measurements) {
        List<SummaryAccumulator> accumulators = new ArrayList<>();
        for (ExperimentMeasurement measurement : measurements) {
            SummaryAccumulator accumulator = findOrCreate(accumulators, measurement);
            accumulator.accept(measurement);
        }
        return accumulators;
    }

    private static SummaryAccumulator findOrCreate(List<SummaryAccumulator> accumulators, ExperimentMeasurement measurement) {
        for (SummaryAccumulator accumulator : accumulators) {
            if (accumulator.matches(measurement)) {
                return accumulator;
            }
        }
        SummaryAccumulator created = new SummaryAccumulator(measurement.spec(), measurement.phase());
        accumulators.add(created);
        return created;
    }

    private static void writeMetadata(CliOptions options,
                                      Path metadataPath,
                                      List<ExperimentMeasurement> measurements,
                                      Instant startedAt) throws IOException {
        Files.createDirectories(metadataPath.getParent());
        int successCount = (int) measurements.stream().filter(m -> m.status() == ExperimentStatus.SUCCESS).count();
        int timeoutCount = (int) measurements.stream().filter(m -> m.status() == ExperimentStatus.TIMEOUT).count();
        int errorCount = (int) measurements.stream().filter(m -> m.status() == ExperimentStatus.ERROR).count();

        StringJoiner joiner = new StringJoiner(",\n  ", "{\n  ", "\n}\n");
        joiner.add("\"runId\": \"" + options.runId() + "\"");
        joiner.add("\"tag\": \"" + safeJson(options.tag()) + "\"");
        joiner.add("\"config\": \"" + safeJson(options.configPath().toString()) + "\"");
        joiner.add("\"output\": \"" + safeJson(metadataPath.getParent().toString()) + "\"");
        joiner.add("\"startedAt\": \"" + startedAt.toString() + "\"");
        joiner.add("\"finishedAt\": \"" + Instant.now().toString() + "\"");
        joiner.add("\"parallelism\": " + options.parallelism());
        joiner.add("\"timeoutSeconds\": " + options.timeout().toSeconds());
        joiner.add("\"odeHorizon\": " + options.odeHorizon());
        joiner.add("\"sliceIndex\": " + options.sliceIndex());
        joiner.add("\"sliceCount\": " + options.sliceCount());
        joiner.add("\"seed\": " + options.seed());
        joiner.add("\"model\": \"" + options.modelKey() + "\"");
        joiner.add("\"infectionRate\": " + options.infectionRate());
        joiner.add("\"recoveryRate\": " + options.recoveryRate());
        joiner.add("\"specifications\": " + measurements.stream().map(ExperimentMeasurement::spec).distinct().count());
        joiner.add("\"measurements\": " + measurements.size());
        joiner.add("\"successes\": " + successCount);
        joiner.add("\"timeouts\": " + timeoutCount);
        joiner.add("\"errors\": " + errorCount);

        Files.writeString(metadataPath, joiner.toString(), StandardCharsets.UTF_8);
    }

    private static String safeCsv(String value) {
        if (value == null) return "";
        String sanitised = value.replace('\n', ' ').replace('\r', ' ').replace('"', '\'');
        if (sanitised.contains(",")) {
            return '"' + sanitised + '"';
        }
        return sanitised;
    }

    private static String safeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }

    private static String abbreviateMessage(Throwable throwable) {
        String message = throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
        if (message.length() > 512) {
            return message.substring(0, 512) + "…";
        }
        return message;
    }

    private static void printUsage() {
        System.out.println("Usage: java -cp <classpath> " + RuntimeExperimentRunner.class.getName() + " [options]\n" +
                "Options:\n" +
                "  --config <path>           CSV plan describing experiments (default config/runtime_plan.csv)\n" +
                "  --output <dir>            Root directory for results (default results/runtime)\n" +
                "  --run-id <id>             Identifier for this run (default runtime-<timestamp>)\n" +
                "  --parallelism <n>         Number of worker threads (default available processors)\n" +
                "  --timeout-seconds <n>     Timeout threshold per phase (default 600)\n" +
                "  --ode-horizon <n>         Horizon parameter for ODE generation (default 10)\n" +
                "  --slice-index <n>         Zero-based slice index (default 0)\n" +
                "  --slice-count <n>         Number of slices to partition plan (default 1)\n" +
                "  --model <sir>            Epidemiological model key (currently only 'sir')\n" +
                "  --infection-rate <x>      Infection rate beta for SIR (default 0.6)\n" +
                "  --recovery-rate <x>       Recovery rate gamma for SIR (default 0.1)\n" +
                "  --tag <label>             Free-form label (e.g. hostname)\n" +
                "  --seed <long>             Base seed for experiment ordering\n" +
                "  --dry-run                 Print plan and exit\n" +
                "  --help                    Show this help message\n");
    }

    private static Path prepareOutputDirectory(CliOptions options) throws IOException {
        Path root = options.outputDir();
        Files.createDirectories(root);
        return root.resolve(options.runId());
    }

    private static ModelParams buildModelParams(CliOptions options) {
        if (!"sir".equalsIgnoreCase(options.modelKey())) {
            throw new IllegalArgumentException("Unsupported model: " + options.modelKey());
        }
        ModelParams params = new ModelParams(List.of('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        params.addTransition('S', 'I', options.infectionRate());
        params.addTransition('I', 'R', options.recoveryRate());
        return params;
    }

    private static class SummaryAccumulator {
        private final ExperimentSpec spec;
        private final Phase phase;
        private int total;
        private int successes;
        private int timeouts;
        private int errors;
        private final List<Double> successDurations = new ArrayList<>();
        private final List<Integer> successTuples = new ArrayList<>();
        private final List<Integer> edgeCounts = new ArrayList<>();

        SummaryAccumulator(ExperimentSpec spec, Phase phase) {
            this.spec = spec;
            this.phase = phase;
        }

        boolean matches(ExperimentMeasurement measurement) {
            return measurement.spec().equals(spec) && measurement.phase() == phase;
        }

        void accept(ExperimentMeasurement measurement) {
            total++;
            edgeCounts.add(measurement.edgeCount());
            switch (measurement.status()) {
                case SUCCESS -> {
                    successes++;
                    successDurations.add(measurement.durationMillis());
                    successTuples.add(measurement.tupleCount());
                }
                case TIMEOUT -> timeouts++;
                case ERROR -> errors++;
            }
        }

        double averageDuration() {
            return successDurations.isEmpty() ? 0.0 : successDurations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }

        double p95Duration() {
            if (successDurations.isEmpty()) return 0.0;
            List<Double> sorted = new ArrayList<>(successDurations);
            sorted.sort(Double::compareTo);
            double rank = 0.95 * (sorted.size() - 1);
            int lower = (int) Math.floor(rank);
            int upper = (int) Math.ceil(rank);
            if (lower == upper) return sorted.get(lower);
            double fraction = rank - lower;
            return sorted.get(lower) + fraction * (sorted.get(upper) - sorted.get(lower));
        }

        double maxDuration() {
            return successDurations.isEmpty() ? 0.0 : successDurations.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        }

        double minDuration() {
            return successDurations.isEmpty() ? 0.0 : successDurations.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        }

        double averageTuples() {
            return successTuples.isEmpty() ? 0.0 : successTuples.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        }

        int maxTuples() {
            return successTuples.isEmpty() ? 0 : successTuples.stream().mapToInt(Integer::intValue).max().orElse(0);
        }

        int minTuples() {
            return successTuples.isEmpty() ? 0 : successTuples.stream().mapToInt(Integer::intValue).min().orElse(0);
        }

        double averageEdges() {
            return edgeCounts.isEmpty() ? 0.0 : edgeCounts.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        }
    }

    record CliOptions(Path configPath,
                      Path outputDir,
                      String runId,
                      int parallelism,
                      Duration timeout,
                      int odeHorizon,
                      int sliceIndex,
                      int sliceCount,
                      String modelKey,
                      double infectionRate,
                      double recoveryRate,
                      String tag,
                      long seed,
                      boolean dryRun,
                      boolean help) {

        static CliOptions parse(String[] args) {
            Path config = DEFAULT_CONFIG;
            Path output = DEFAULT_OUTPUT_ROOT;
            String runId = "runtime-" + RUN_ID_FORMAT.format(Instant.now());
            int parallelism = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
            Duration timeout = DEFAULT_TIMEOUT;
            int odeHorizon = DEFAULT_ODE_HORIZON;
            int sliceIndex = 0;
            int sliceCount = 1;
            String modelKey = "sir";
            double infectionRate = 0.6;
            double recoveryRate = 0.1;
            String tag = "";
            long seed = System.nanoTime();
            boolean dryRun = false;
            boolean help = false;

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                switch (arg) {
                    case "--config" -> config = Path.of(requireValue(args, ++i, "--config"));
                    case "--output" -> output = Path.of(requireValue(args, ++i, "--output"));
                    case "--run-id" -> runId = requireValue(args, ++i, "--run-id");
                    case "--parallelism" -> parallelism = Integer.parseInt(requireValue(args, ++i, "--parallelism"));
                    case "--timeout-seconds" -> timeout = Duration.ofSeconds(Long.parseLong(requireValue(args, ++i, "--timeout-seconds")));
                    case "--ode-horizon" -> odeHorizon = Integer.parseInt(requireValue(args, ++i, "--ode-horizon"));
                    case "--slice-index" -> sliceIndex = Integer.parseInt(requireValue(args, ++i, "--slice-index"));
                    case "--slice-count" -> sliceCount = Integer.parseInt(requireValue(args, ++i, "--slice-count"));
                    case "--model" -> modelKey = requireValue(args, ++i, "--model");
                    case "--infection-rate" -> infectionRate = Double.parseDouble(requireValue(args, ++i, "--infection-rate"));
                    case "--recovery-rate" -> recoveryRate = Double.parseDouble(requireValue(args, ++i, "--recovery-rate"));
                    case "--tag" -> tag = requireValue(args, ++i, "--tag");
                    case "--seed" -> seed = Long.parseLong(requireValue(args, ++i, "--seed"));
                    case "--dry-run" -> dryRun = true;
                    case "--help", "-h" -> help = true;
                    default -> throw new IllegalArgumentException("Unknown option: " + arg);
                }
            }

            if (parallelism <= 0) throw new IllegalArgumentException("parallelism must be positive");
            if (odeHorizon <= 0) throw new IllegalArgumentException("ode-horizon must be positive");
            if (sliceCount <= 0) throw new IllegalArgumentException("slice-count must be positive");
            if (sliceIndex < 0 || sliceIndex >= sliceCount) {
                throw new IllegalArgumentException("slice-index must be between 0 and " + (sliceCount - 1));
            }
            if (infectionRate <= 0.0 || recoveryRate <= 0.0) {
                throw new IllegalArgumentException("infection and recovery rates must be positive");
            }

            return new CliOptions(config, output, runId, parallelism, timeout, odeHorizon, sliceIndex, sliceCount,
                    modelKey, infectionRate, recoveryRate, tag, seed, dryRun, help);
        }

        private static String requireValue(String[] args, int index, String option) {
            if (index >= args.length) {
                throw new IllegalArgumentException("Missing value for " + option);
            }
            return args[index];
        }
    }
}
