package io.github.ethankelly.model;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import io.github.ethankelly.graph.Rand;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class RequiredTuplesTest {
    // Usual SIR model parameters
    static ModelParams m = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});

    @BeforeAll
    static void setUp() {
        m.addTransition('S', 'I', 0.6);
        m.addTransition('I', 'R', 0.1);
    }

    @Test
    void genTuplesTriangle() {
        String expected = """
                〈I0 S1 I2〉
                〈S0 I1 S2〉
                〈I0 I1 S2〉
                〈S0 S1 I2〉
                〈S0 I1 I2〉
                〈I0 S1 S2〉
                〈I0 S1〉
                〈S0 I1〉
                〈I0 S2〉
                〈S0 I2〉
                〈I1 S2〉
                〈S1 I2〉
                〈S0〉
                〈S1〉
                〈S2〉
                〈I0〉
                〈I1〉
                〈I2〉
                """;
        RequiredTuples C3 = new RequiredTuples(GraphGenerator.cycle(3), m, false);
        StringBuilder actual = new StringBuilder();
        List<RequiredTuples.Tuple> triangleTuples = C3.genTuples();
        for (RequiredTuples.Tuple t : triangleTuples) actual.append(t).append("\n");
        assertEquals(expected, actual.toString());
    }

    @RepeatedTest(10)
    void testFindSingles() {
        // Random number of vertices between 1 and 10
        int v = Rand.uniform(1, 10);
        // Random probability between 0 and 1
        double p = Math.min(0.5, Rand.uniform());
        // Generate an Erdos-Renyi graph using the provided random values
        Graph g = GraphGenerator.erdosRenyi(v, p);
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
        assertEquals(18, triangle.size(), "Actual: " + triangle.getTuples());
    }


    @Test
    void testEquals() {
        // Random number of vertices between 3 and 10
        int v = Rand.uniform(1, 10);
        // Random probability between 0 and 1
        double p = Math.min(0.5, Rand.uniform());
        // Generate an Erdos-Renyi graph using the provided random values
        Graph g = GraphGenerator.erdosRenyi(v, p);

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