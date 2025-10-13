package io.github.ethankelly.experiments;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the collection of experiment specifications that should be executed in a single run.
 */
public final class ExperimentPlan {
    private final List<ExperimentSpec> specifications;

    private ExperimentPlan(List<ExperimentSpec> specifications) {
        this.specifications = List.copyOf(specifications);
    }

    public static ExperimentPlan fromCsv(Path path) throws IOException {
        Objects.requireNonNull(path, "Config path must not be null");
        if (!Files.exists(path)) {
            throw new IOException("Experiment config file not found: " + path.toAbsolutePath());
        }

        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Experiment config file is empty: " + path);
        }

        int headerIndex = findHeaderIndex(lines);
        String headerRaw = lines.get(headerIndex);
        Map<String, Integer> headerMapping = parseHeader(headerRaw);

        List<ExperimentSpec> specs = new ArrayList<>();
        for (int i = headerIndex + 1; i < lines.size(); i++) {
            String rawLine = lines.get(i).trim();
            if (rawLine.isEmpty() || rawLine.startsWith("#")) continue;
            String[] tokens = rawLine.split(",", -1);
            if (tokens.length < headerMapping.size()) {
                throw new IllegalArgumentException("Line " + (i + 1) + " has fewer columns than header: " + rawLine);
            }

            try {
                ExperimentSpec spec = parseSpec(tokens, headerMapping);
                specs.add(spec);
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("Failed to parse line " + (i + 1) + ": " + rawLine, ex);
            }
        }

        if (specs.isEmpty()) {
            throw new IllegalArgumentException("No experiments found in config file: " + path);
        }

        return new ExperimentPlan(specs);
    }

    private static int findHeaderIndex(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String trimmed = lines.get(i).trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                return i;
            }
        }
        throw new IllegalArgumentException("No header row found in experiment config");
    }

    private static Map<String, Integer> parseHeader(String headerRaw) {
        String[] headerTokens = headerRaw.split(",", -1);
        Map<String, Integer> mapping = new LinkedHashMap<>();
        for (int i = 0; i < headerTokens.length; i++) {
            String key = headerTokens[i].trim().toLowerCase(Locale.ROOT);
            if (key.isEmpty()) continue;
            mapping.put(key, i);
        }
        if (!mapping.containsKey("graph_type")) {
            throw new IllegalArgumentException("Header must contain a 'graph_type' column");
        }
        if (!mapping.containsKey("num_vertices")) {
            throw new IllegalArgumentException("Header must contain a 'num_vertices' column");
        }
        if (!mapping.containsKey("iterations")) {
            throw new IllegalArgumentException("Header must contain an 'iterations' column");
        }
        return mapping;
    }

    private static ExperimentSpec parseSpec(String[] tokens, Map<String, Integer> headerMapping) {
        GraphType graphType = GraphType.fromString(tokens[headerMapping.get("graph_type")]);
        String label = valueOrNull(tokens, headerMapping, "label");
        int numVertices = Integer.parseInt(tokens[headerMapping.get("num_vertices")].trim());
        int iterations = Integer.parseInt(tokens[headerMapping.get("iterations")].trim());
        Map<String, String> parameters = parseParameters(valueOrNull(tokens, headerMapping, "parameters"));
        return new ExperimentSpec(graphType, label, numVertices, iterations, parameters);
    }

    private static String valueOrNull(String[] tokens, Map<String, Integer> headerMapping, String key) {
        Integer index = headerMapping.get(key);
        if (index == null || index >= tokens.length) return null;
        String raw = tokens[index];
        return raw == null ? null : raw.trim();
    }

    private static Map<String, String> parseParameters(String raw) {
        if (raw == null || raw.isBlank()) return Map.of();
        String[] entries = raw.split(";", -1);
        Map<String, String> result = new LinkedHashMap<>();
        for (String entry : entries) {
            if (entry == null) continue;
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) continue;
            String[] pair = trimmed.split("=", 2);
            String key = pair[0].trim();
            String value = pair.length > 1 ? pair[1].trim() : "";
            result.put(key, value);
        }
        return result;
    }

    public List<ExperimentSpec> specifications() {
        return specifications;
    }

    public int totalIterations() {
        return specifications.stream().mapToInt(ExperimentSpec::iterations).sum();
    }

    public List<ExperimentSpec> slice(int sliceIndex, int sliceCount) {
        if (sliceCount <= 1) return specifications;
        if (sliceIndex < 0 || sliceIndex >= sliceCount) {
            throw new IllegalArgumentException("Slice index " + sliceIndex + " out of bounds for slice count " + sliceCount);
        }
        List<ExperimentSpec> subset = new ArrayList<>();
        for (int i = 0; i < specifications.size(); i++) {
            if (i % sliceCount == sliceIndex) subset.add(specifications.get(i));
        }
        return subset;
    }

    @Override
    public String toString() {
        return specifications.stream()
                .map(ExperimentSpec::label)
                .collect(Collectors.joining(", "));
    }
}
