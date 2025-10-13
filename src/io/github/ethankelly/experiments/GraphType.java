package io.github.ethankelly.experiments;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;

import java.util.Locale;

/**
 * Graph families supported by the runtime experiments.
 */
public enum GraphType {
    ERDOS_RENYI {
        @Override
        public Graph createGraph(ExperimentSpec spec, long seed) {
            double probability = spec.requireDouble("probability");
            return GraphGenerator.erdosRenyi(spec.numVertices(), probability);
        }
    },
    RANDOM_REGULAR {
        @Override
        public Graph createGraph(ExperimentSpec spec, long seed) {
            int degree = spec.requireInt("degree");
            return GraphGenerator.regular(spec.numVertices(), degree);
        }
    },
    BARABASI_ALBERT {
        @Override
        public Graph createGraph(ExperimentSpec spec, long seed) {
            int attachment = spec.requireInt("attachment");
            return GraphGenerator.barabasiAlbert(spec.numVertices(), attachment);
        }
    },
    TREE {
        @Override
        public Graph createGraph(ExperimentSpec spec, long seed) {
            return GraphGenerator.tree(spec.numVertices());
        }
    },
    BINARY_TREE {
        @Override
        public Graph createGraph(ExperimentSpec spec, long seed) {
            return GraphGenerator.binaryTree(spec.numVertices());
        }
    },
    COMPLETE {
        @Override
        public Graph createGraph(ExperimentSpec spec, long seed) {
            return GraphGenerator.complete(spec.numVertices());
        }
    },
    PATH {
        @Override
        public Graph createGraph(ExperimentSpec spec, long seed) {
            return GraphGenerator.path(spec.numVertices());
        }
    },
    CYCLE {
        @Override
        public Graph createGraph(ExperimentSpec spec, long seed) {
            return GraphGenerator.cycle(spec.numVertices());
        }
    };

    public abstract Graph createGraph(ExperimentSpec spec, long seed);

    public static GraphType fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Graph type must be provided");
        }
        String normalised = raw.trim().toLowerCase(Locale.ROOT);
        return switch (normalised) {
            case "er", "erdos", "erdos_renyi", "erdos-renyi", "erdös", "erdős", "erdős-rényi", "erdos renyi" -> ERDOS_RENYI;
            case "regular", "k-regular", "random_regular", "random-regular" -> RANDOM_REGULAR;
            case "barabasi", "barabasi_albert", "barabási", "barabási-albert", "barabasi-albert", "scale-free", "scalefree", "scale_free" -> BARABASI_ALBERT;
            case "tree", "random_tree" -> TREE;
            case "binary_tree", "binary-tree", "btree" -> BINARY_TREE;
            case "complete", "clique" -> COMPLETE;
            case "path" -> PATH;
            case "cycle" -> CYCLE;
            default -> throw new IllegalArgumentException("Unknown graph type: " + raw);
        };
    }

    public String displayName() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
