package io.github.ethankelly.experiments;

import java.time.Duration;
import java.util.Locale;
import java.util.Objects;

/**
 * Captures the outcome of a single experiment phase.
 */
public final class ExperimentMeasurement {

    private final ExperimentSpec spec;
    private final int iteration;
    private final Phase phase;
    private final ExperimentStatus status;
    private final long durationNanos;
    private final int tupleCount;
    private final int edgeCount;
    private final long seed;
    private final String message;

    public ExperimentMeasurement(ExperimentSpec spec,
                                 int iteration,
                                 Phase phase,
                                 ExperimentStatus status,
                                 long durationNanos,
                                 int tupleCount,
                                 int edgeCount,
                                 long seed,
                                 String message) {
        if (spec == null) throw new IllegalArgumentException("spec must not be null");
        if (iteration < 0) throw new IllegalArgumentException("iteration index must be non-negative");
        if (phase == null) throw new IllegalArgumentException("phase must not be null");
        if (status == null) throw new IllegalArgumentException("status must not be null");
        if (durationNanos < 0) throw new IllegalArgumentException("duration must be non-negative");
        if (tupleCount < 0) throw new IllegalArgumentException("tuple count must be non-negative");
        if (edgeCount < 0) throw new IllegalArgumentException("edge count must be non-negative");

        this.spec = spec;
        this.iteration = iteration;
        this.phase = phase;
        this.status = status;
        this.durationNanos = durationNanos;
        this.tupleCount = tupleCount;
        this.edgeCount = edgeCount;
        this.seed = seed;
        this.message = message == null ? "" : message.trim();
    }

    public ExperimentSpec spec() {
        return spec;
    }

    public int iteration() {
        return iteration;
    }

    public Phase phase() {
        return phase;
    }

    public ExperimentStatus status() {
        return status;
    }

    public long durationNanos() {
        return durationNanos;
    }

    public int tupleCount() {
        return tupleCount;
    }

    public int edgeCount() {
        return edgeCount;
    }

    public long seed() {
        return seed;
    }

    public String message() {
        return message;
    }

    public double durationMillis() {
        return durationNanos / 1_000_000.0;
    }

    public Duration duration() {
        return Duration.ofNanos(durationNanos);
    }

    public String statusLabel() {
        return status.name().toLowerCase(Locale.ROOT);
    }

    public enum Phase { FULL, REDUCED }

    public enum ExperimentStatus { SUCCESS, TIMEOUT, ERROR }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExperimentMeasurement)) return false;
        ExperimentMeasurement that = (ExperimentMeasurement) o;
        return iteration == that.iteration
                && durationNanos == that.durationNanos
                && tupleCount == that.tupleCount
                && edgeCount == that.edgeCount
                && seed == that.seed
                && Objects.equals(spec, that.spec)
                && phase == that.phase
                && status == that.status
                && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spec, iteration, phase, status, durationNanos, tupleCount, edgeCount, seed, message);
    }

    @Override
    public String toString() {
        return "ExperimentMeasurement{" +
                "spec=" + spec +
                ", iteration=" + iteration +
                ", phase=" + phase +
                ", status=" + status +
                ", durationNanos=" + durationNanos +
                ", tupleCount=" + tupleCount +
                ", edgeCount=" + edgeCount +
                ", seed=" + seed +
                ", message='" + message + '\'' +
                '}';
    }
}

