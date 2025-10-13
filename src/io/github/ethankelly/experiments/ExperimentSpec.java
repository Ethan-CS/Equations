package io.github.ethankelly.experiments;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Immutable description of a single experiment workload.
 */
public record ExperimentSpec(GraphType graphType,
                             String label,
                             int numVertices,
                             int iterations,
                             Map<String, String> parameters) {

    public ExperimentSpec {
        if (graphType == null) throw new IllegalArgumentException("Graph type is required");
        if (numVertices <= 0) throw new IllegalArgumentException("Number of vertices must be positive");
        if (iterations <= 0) throw new IllegalArgumentException("Iterations must be positive");

        label = normaliseLabel(label, graphType, numVertices, parameters);
        parameters = normaliseParameters(parameters);
    }

    private static String normaliseLabel(String candidate,
                                         GraphType graphType,
                                         int numVertices,
                                         Map<String, String> parameters) {
        if (candidate != null && !candidate.isBlank()) {
            return candidate.trim();
        }
        StringBuilder builder = new StringBuilder(graphType.displayName()).append('-').append(numVertices);
        if (parameters != null && !parameters.isEmpty()) {
            Map.Entry<String, String> first = parameters.entrySet().iterator().next();
            builder.append('-').append(first.getKey().toLowerCase(Locale.ROOT));
            if (first.getValue() != null && !first.getValue().isBlank()) {
                builder.append('-').append(first.getValue().replaceAll("\\s+", ""));
            }
        }
        return builder.toString();
    }

    private static Map<String, String> normaliseParameters(Map<String, String> source) {
        Map<String, String> copy = new LinkedHashMap<>();
        if (source != null) {
            source.forEach((rawKey, rawValue) -> {
                if (rawKey == null || rawKey.isBlank()) return;
                String key = rawKey.trim().toLowerCase(Locale.ROOT);
                String value = rawValue == null ? "" : rawValue.trim();
                copy.put(key, value);
            });
        }
        return Collections.unmodifiableMap(copy);
    }

    public Optional<String> parameter(String key) {
        if (key == null) return Optional.empty();
        return Optional.ofNullable(parameters.get(key.trim().toLowerCase(Locale.ROOT)));
    }

    public String parameterOrDefault(String key, String defaultValue) {
        return parameter(key).filter(s -> !s.isBlank()).orElse(defaultValue);
    }

    public String require(String key) {
        return parameter(key).filter(s -> !s.isBlank())
                .orElseThrow(() -> new IllegalArgumentException("Parameter '" + key + "' is required for " + label));
    }

    public int requireInt(String key) {
        String value = require(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Parameter '" + key + "' must be an integer for " + label + ": '" + value + "'", ex);
        }
    }

    public double requireDouble(String key) {
        String value = require(key);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Parameter '" + key + "' must be a decimal for " + label + ": '" + value + "'", ex);
        }
    }

    public String parameterSignature() {
        if (parameters.isEmpty()) return "";
        StringJoiner joiner = new StringJoiner(";");
        parameters.forEach((key, value) -> joiner.add(key + '=' + value));
        return joiner.toString();
    }
}
