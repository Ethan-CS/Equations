package io.github.ethankelly.model;

import io.github.ethankelly.graph.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static io.github.ethankelly.graph.GraphUtils.countWalks;
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
                〈S0〉
                〈S1〉
                〈S2〉
                〈I0〉
                〈I1〉
                〈I2〉
                〈I0 S1〉
                〈S0 I1〉
                〈I0 S2〉
                〈S0 I2〉
                〈I1 S2〉
                〈S1 I2〉
                〈I0 S1 I2〉
                〈S0 I1 S2〉
                〈I0 I1 S2〉
                〈S0 S1 I2〉
                〈S0 I1 I2〉
                〈I0 S1 S2〉
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

    private int expectedNumberPath(int K, int n, Graph filter) {
        int result = 0;
        //  Single vertex terms
        result += (K*n);

        // Count walks in filter graph up to number of vertices
        countWalks(filter, n);
        int[] numWalks = new int[n];
        for (int i = 0; i < numWalks.length; i++) {
            Integer[][] walks = filter.getNumWalks().get(i);
            int w = 0;
            // Add up all elements in power of adj mat to get all walks of length i
            for (Integer[] row : walks) for (int k = 0; k < walks[0].length; k++) w += row[k];
            // Store num of walks of length i for later calculation
            numWalks[i]=w;
        }
        // Sum: i from 1 to n, (num walks length i) * (n-i)
        result += IntStream.range(1, n).map(i -> numWalks[i] * (n - i)).sum();

        return result;
    }

    @Test
    void getTuplesOnPaths() {
        // Run on paths up to 100 vertices
        for (int i = 1; i <= 100; i++) {
            // Print a new line and num vertices
            System.out.print("\nn=" + i );
            // Use helper method to get expected number of equations from analytically derived expression
            int expected = expectedNumberPath(2, i,  m.getFilterGraph());
            System.out.print(" -> EXPECTED: " + expected);
            // Create a path and use algorithmic method to generate tuples and print how many were generated
            Graph p = GraphGenerator.path(i);
            RequiredTuples path = new RequiredTuples(p, m, false);
            int actual = path.size();
            System.out.print(", ACTUAL: " + actual);
            // Assert that we generated the correct number of equations
            assertEquals(expected, actual);
        }
    }

    @Test
    void generateTuples() {
        // TODO not yet implemented
    }
}