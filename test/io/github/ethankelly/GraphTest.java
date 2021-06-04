package io.github.ethankelly;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class GraphTest {
    private static final Graph g1 = new Graph(6, "Test");

    @BeforeAll
    static void setUpGraphs() {
        g1.addEdge(0,1);
        g1.addEdge(0,5);
        g1.addEdge(1,2);
        g1.addEdge(2,5);
        g1.addEdge(2,4);
        g1.addEdge(2,3);
    }

    @Test
    void testCutVerticesG1() {
        Graph g1Copy = g1.clone();
        System.out.println(g1.getCutVertices());
        Assertions.assertEquals(g1.getCutVertices(), Collections.singletonList(new Vertex(2)),
                "Should have identified 2 as the only cut-vertex for G1.");
        g1Copy.appendVertices(1);
        g1Copy.addEdge(2, 6);
        Assertions.assertEquals(g1Copy.getCutVertices(), Collections.singletonList(new Vertex(2)),
                "Should have identified 2 as the only cut-vertex for G1.");
    }

    @Test
    void testSpliceG1() {
        Graph subA = new Graph(4, "Test Subgraph");
        subA.addEdge(0, 1);
        subA.addEdge(0, 3);
        subA.addEdge(1, 2);

        Graph subB = GraphGenerator.getEdge().setName("Test Subgraph");
        Graph subC = subB.clone();

        List<Graph> correctSubGraphs = Arrays.asList(subA, subB, subC);
        List<Graph> foundSubGraphs = g1.splice();

        Assertions.assertEquals(correctSubGraphs.size(), foundSubGraphs.size(),
                "Splice method did not find the right number of sub-graphs for G1.");
        Assertions.assertTrue(correctSubGraphs.containsAll(foundSubGraphs),
                "Not all of the correct sub-graphs were found for G1");
        Assertions.assertTrue(foundSubGraphs.containsAll(correctSubGraphs),
                "Some incorrect sub-graphs were found.");
    }

    @Test
    void testCutVerticesG2() {
        Graph g2 = new Graph(5, "Test");
        g2.addEdge(0, 1);
        g2.addEdge(0, 4);
        g2.addEdge(1, 2);
        g2.addEdge(1, 4);
        g2.addEdge(2, 3);

        Assertions.assertEquals(g2.getCutVertices(), Arrays.asList(new Vertex(1), new Vertex(2)),
                "Should have identified 1 and 2 as cut-vertices for G2.");
    }

    @Test
    void testSpliceLollipop() {
        // Get the lollipop graph from the graph generator
        Graph lollipop = GraphGenerator.getLollipop();
        // Get the cut vertices - should just be vertex 0
        Assertions.assertEquals(lollipop.getCutVertices(), Collections.singletonList(new Vertex(0)),
                "Lollipop has exactly one cut vertex at location 0");
        // Now, splice the graph and ensure two graphs are created correctly
        List<Graph> lollipopSplice = lollipop.splice();
        // First subgraph is a single edge
        Graph edge = new Graph(2, "Lollipop Subgraph");
        edge.addEdge(0,1);
        // Second subgraph is a triangle
        Graph triangle = GraphGenerator.getTriangle();
        triangle.setName("Lollipop Subgraph");
        // Add both to a list
        List<Graph> correctSplice = Arrays.asList(edge, triangle);
        // Now, assert expected and actual are equal - bit convoluted so we can ignore order of graphs in list
        Assertions.assertEquals(correctSplice.size(), lollipopSplice.size(), "Splices were different sizes");
        Assertions.assertTrue(correctSplice.containsAll(lollipopSplice),
                "The lollipop slice contains too much");
        Assertions.assertTrue(lollipopSplice.containsAll(correctSplice),
                "The lollipop splice is missing something.");
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