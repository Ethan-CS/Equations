package io.github.ethankelly.model;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import io.github.ethankelly.graph.Rand;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class RequiredTuplesTest {

    @RepeatedTest(10)
    void testFindSingles() {
        // Random number of vertices between 1 and 100
        int v = Rand.uniform(1, 100);
        // Random probability between 0 and 1
        double p = Rand.uniform();
        // Generate an Erdos-Renyi graph using the provided random values
        Graph g = GraphGenerator.erdosRenyi(v, p);
        // Usual SIR model parameters
        ModelParams m = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        // Create required tuple instance
        RequiredTuples rt = new RequiredTuples(g, m, false);
        // Assert that the singles are (#singles * #states in filter graph)
        assertEquals(v * 2, rt.findSingles().size());
    }

    @Test
    void testSize() {
        ModelParams m = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        m.addTransition('S', 'I', 0.6);
        m.addTransition('I', 'R', 0.1);

        RequiredTuples edge = new RequiredTuples(GraphGenerator.getEdge(), m, false);
        assertEquals(6, edge.size(), edge.toString());

        RequiredTuples triangle = new RequiredTuples(GraphGenerator.getTriangle(), m, false);
        assertEquals(18, triangle.size());
    }


    @Test
    void testEquals() {
        // Random number of vertices between 3 and 10
        int v = Rand.uniform(3, 10);
        // Random probability between 0 and 1
        double p = Rand.uniform();
        // Generate an Erdos-Renyi graph using the provided random values
        Graph g = GraphGenerator.erdosRenyi(v, p);
        // Usual SIR model parameters
        ModelParams m = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});

        RequiredTuples rt1 = new RequiredTuples(g, m, false);
        RequiredTuples rt2 = new RequiredTuples(g, m, false);

        assertEquals(rt1, rt2);

        Graph h = g.clone();
        h.removeVertex(new Vertex(0));
        RequiredTuples rt3 = new RequiredTuples(h, m, false);

        assertNotEquals(rt3, rt1);
    }

    @Test
    void generateTuples() {
        // TODO not yet implemented
    }
}