package io.github.ethankelly.experiments;

import java.time.Duration;
import java.util.Locale;

/**
 * Captures the outcome of a single experiment phase.
 */
public record ExperimentMeasurement(ExperimentSpec spec,
                                    int iteration,
                                    Phase phase,
                                    ExperimentStatus status,
                                    long durationNanos,
                                    int tupleCount,
                                    int edgeCount,
                                    long seed,
                                    String message) {

    public ExperimentMeasurement {
        if (spec == null) throw new IllegalArgumentException("spec must not be null");
        if (iteration < 0) throw new IllegalArgumentException("iteration index must be non-negative");
        if (phase == null) throw new IllegalArgumentException("phase must not be null");
        if (status == null) throw new IllegalArgumentException("status must not be null");
        if (durationNanos < 0) throw new IllegalArgumentException("duration must be non-negative");
        if (tupleCount < 0) throw new IllegalArgumentException("tuple count must be non-negative");
        if (edgeCount < 0) throw new IllegalArgumentException("edge count must be non-negative");
        message = message == null ? "" : message.trim();
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
}
