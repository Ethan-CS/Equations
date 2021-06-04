package io.github.ethankelly;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

class GraphTest {

    @Test
    void testCutVerticesG1() {
        Graph g1 = new Graph(6, "Test");
        g1.addEdge(0,1);
        g1.addEdge(0,5);
        g1.addEdge(1,2);
        g1.addEdge(2,5);
        g1.addEdge(2,4);
        g1.addEdge(2,3);

        Assertions.assertEquals(g1.getCutVertices(), Collections.singletonList(2),
                "Should have identified 2 as the only cut-vertex for G1.");

        g1.appendVertices(1);
        g1.addEdge(2, 6);

        Assertions.assertEquals(g1.getCutVertices(), Collections.singletonList(2),
                "Should have identified 2 as the only cut-vertex for G1.");

    }

    @Test
    void testCutVerticesG2() {
        Graph g2 = new Graph(5, "Test");
        g2.addEdge(0, 1);
        g2.addEdge(0, 4);
        g2.addEdge(1, 2);
        g2.addEdge(1, 4);
        g2.addEdge(2, 3);

        Assertions.assertEquals(g2.getCutVertices(), Arrays.asList(1, 2),
                "Should have identified 1 and 2 as cut-vertices for G2.");
    }



    @Test
    void testClone() {
        // Make sure clone() creates an identical instance
        Graph g = GraphGenerator.getLollipop();
        Graph h = g.clone();
        Assertions.assertEquals(g, h, "The clone() method did not make an identical instance.");
        // Now, ensure that changing an attribute of the clone makes it not equal the original
        g.setName("New name");
        Assertions.assertNotEquals(g, h, "Expected instances to be different.");
        Assertions.assertNotEquals(h.getName(), "New name", "Name of clone should not change");
    }
}