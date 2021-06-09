package io.github.ethankelly.graph;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class GraphTest {
    private static final Graph g1 = new Graph(6, "Test");
    private static final Graph g2 = new Graph(5, "Test");
    // Get the lollipop graph from the graph generator
    private static final Graph lollipop = GraphGenerator.getLollipop();
    private static final Graph toast = GraphGenerator.getToast();

    @BeforeAll
    static void setUpGraphs() {
        // Add edges  to  G1
        g1.addEdge(0,1);
        g1.addEdge(0,5);
        g1.addEdge(1,2);
        g1.addEdge(2,5);
        g1.addEdge(3,4);
        g1.addEdge(2,3);

        // Add edges to G2
        g2.addEdge(0, 1);
        g2.addEdge(0, 4);
        g2.addEdge(1, 2);
        g2.addEdge(1, 4);
        g2.addEdge(2, 3);
    }

    @Test
    void testCutVerticesG1() {
        Assertions.assertEquals(g1.getCutVertices(), Arrays.asList(new Vertex(2), new Vertex(3)),
                "Should have identified 2 and 3 as the only cut-vertices for G1.");
    }

    @Test
    void testSpliceG1() {
        Graph subA = new Graph(4, "Test Subgraph");
        subA.addEdge(0, 1);
        subA.addEdge(0, 3);
        subA.addEdge(1, 2);
        subA.addEdge(2, 3);

        Graph subB = GraphGenerator.getEdge().setName("Test Subgraph");

        List<Graph> correctSubGraphs = Arrays.asList(subA, subB, subB);
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
        Assertions.assertEquals(g2.getCutVertices(), Arrays.asList(new Vertex(1), new Vertex(2)),
                "Should have identified 1 and 2 as cut-vertices for G2.");
    }

    @Test
    void testSpliceG2() {
        Graph subA = GraphGenerator.getTriangle().setName("Test Subgraph");
        Graph subB = GraphGenerator.getEdge().setName("Test Subgraph");

        List<Graph> correctSubGraphs = Arrays.asList(subA, subB, subB);
        List<Graph> foundSubGraphs = g2.splice();

        Assertions.assertEquals(correctSubGraphs.size(), foundSubGraphs.size(),
                "Splice method did not find the right number of sub-graphs for G2.");
        Assertions.assertTrue(correctSubGraphs.containsAll(foundSubGraphs),
                "Not all of the correct sub-graphs were found for G2");
        Assertions.assertTrue(foundSubGraphs.containsAll(correctSubGraphs),
                "Some incorrect sub-graphs were found.");

    }

    @Test
    void testCutVerticesLollipop() {
        // Get the cut vertices - should just be vertex 0
        Assertions.assertEquals(lollipop.getCutVertices(), Collections.singletonList(new Vertex(0)),
                "Lollipop has exactly one cut vertex at location 0");
    }

    @Test
    void testSpliceLollipop() {
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
                "The lollipop splice contains too much");
        Assertions.assertTrue(lollipopSplice.containsAll(correctSplice),
                "The lollipop splice is missing something.");
    }

    @Test
    void testCutVerticesToast() {
        // Get the cut vertices - should just be vertex 0
        Assertions.assertEquals(toast.getCutVertices(), Collections.emptyList(),
                "Toast has no cut vertices");
    }

    @Test
    void testSpliceToast() {
        // Now, assert expected and actual are equal - bit convoluted so we can ignore order of graphs in list
        Assertions.assertEquals(Collections.singletonList(GraphGenerator.getToast()), toast.splice(),
                "Toast cannot be spliced into sub-graphs.");
    }

    @RepeatedTest(50)
    void testRecalculateEdges() {
        Graph g = GraphGenerator.erdosRenyi(50, 0.5);
        Assertions.assertEquals(g.getNumEdges(), g.recalculateEdges(),
                "Recalculate edges did not get the correct number of edges");
    }

    @Test
    void testClone() {
        // Make sure clone() creates an identical instance
        Graph g = lollipop.clone();
        Graph h = g.clone();
        Assertions.assertEquals(g, h, "The clone() method did not make an identical instance.");
        // Now, ensure that changing an attribute of the clone makes it not equal the original
        g.setName("New name");
        Assertions.assertNotEquals(g, h, "Expected instances to be different.");
        Assertions.assertNotEquals(h.getName(), "New name", "Name of clone should not change");
    }

    @RepeatedTest(50)
    void testEquals() {
        Graph g = GraphGenerator.erdosRenyi(50, 0.5);
        Graph h = new Graph(g.getNumVertices(), g.getName());
        h.setAdjList(g.getAdjList());
        h.setNumEdges(g.getNumEdges());
        Assertions.assertEquals(g, h, "Identical graphs were not returned true.");
        h.appendVertices(1);
        Assertions.assertNotEquals(g, h, "When passed different graphs, equals method should return false");
    }
}