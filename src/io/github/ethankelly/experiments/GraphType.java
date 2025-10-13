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
        switch (normalised) {
            case "er":
            case "erdos":
            case "erdos_renyi":
            case "erdos-renyi":
            case "erdös":
            case "erdős":
            case "erdős-rényi":
            case "erdos renyi":
                return ERDOS_RENYI;
            case "regular":
            case "k-regular":
            case "random_regular":
            case "random-regular":
                return RANDOM_REGULAR;
            case "barabasi":
            case "barabasi_albert":
            case "barabási":
            case "barabási-albert":
            case "barabasi-albert":
            case "scale-free":
            case "scalefree":
            case "scale_free":
                return BARABASI_ALBERT;
            case "tree":
            case "random_tree":
                return TREE;
            case "binary_tree":
            case "binary-tree":
            case "btree":
                return BINARY_TREE;
            case "complete":
            case "clique":
                return COMPLETE;
            case "path":
                return PATH;
            case "cycle":
                return CYCLE;
            default:
                throw new IllegalArgumentException("Unknown graph type: " + raw);
        }
    }

    public String displayName() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
