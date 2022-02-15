package io.github.ethankelly.graph;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class GraphTest {
    private static final Graph g1 = new Graph(6, "Test");
    private static final Graph g2 = new Graph(5, "Test");
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
        subA.setLabels(Arrays.asList('0','1','2','5'));

        Graph subB = GraphGenerator.getEdge().setName("Test Subgraph");
        subB.setLabels(Arrays.asList('2','3'));

        Graph subC = GraphGenerator.getEdge().setName("Test Subgraph");
        subB.setLabels(Arrays.asList('3','4'));

        Graph[] correctSubGraphs = new Graph[]{subA, subB, subC};
        Graph g1clone = g1.clone();

        Graph[] foundSubGraphs = g1clone.splice().toArray(new Graph[0]);

        boolean test = true;
        if (correctSubGraphs.length == foundSubGraphs.length) {
            for (int i = 0; i < correctSubGraphs.length; i++) {
                if (correctSubGraphs[i].getNumVertices()!=(foundSubGraphs[i].getNumVertices())) {
                    test = false;
                    System.err.println("Differ at " + i + "\n" +  correctSubGraphs[i] + "\n" + foundSubGraphs[i]);
                    break;
                }
            }
        } else test = false;

        Assertions.assertEquals(correctSubGraphs.length, foundSubGraphs.length,
                "Splice method did not find the right number of sub-graphs for G1.");
        Assertions.assertTrue(test, "Each sub-graph is of different lengths - check this.");
    }

    @Test
    void testCutVerticesG2() {
        Assertions.assertEquals(g2.getCutVertices(), Arrays.asList(new Vertex(1), new Vertex(2)),
                "Should have identified 1 and 2 as cut-vertices for G2.");
    }

    @Test
    void testSpliceG2() {
        Graph g2clone = g2.clone();
        Graph subA = GraphGenerator.getTriangle().setName("Test Subgraph");
        Graph subB = GraphGenerator.getEdge().setName("Test Subgraph");

        List<Graph> correctSubGraphs = Arrays.asList(subA, subB, subB);
        List<Graph> foundSubGraphs = g2clone.splice();

        Assertions.assertEquals(correctSubGraphs.size(), foundSubGraphs.size(),
                "Splice method did not find the right number of sub-graphs for G2.");

        boolean test = true;
        if (correctSubGraphs.size() == foundSubGraphs.size()) {
            for (int i = 0; i < correctSubGraphs.size(); i++) {
                if (correctSubGraphs.get(i).getNumVertices()!=(foundSubGraphs.get(i).getNumVertices())) {
                    test = false;
                    System.err.println("Differ at " + i + "\n" + correctSubGraphs.get(i) + "\n" + foundSubGraphs.get(i));
                    break;
                }
            }
        } else test = false;
        Assertions.assertTrue(test, "Each sub-graph is of different lengths - check this.");

    }

    @Test
    void testCutVerticesLollipop() {
        // Get the cut vertices - should just be vertex 0
        Assertions.assertEquals(GraphGenerator.getLollipop().getCutVertices(), Collections.singletonList(new Vertex(0)),
                "Lollipop has exactly one cut vertex at location 0");
    }

    @Test
    void testSpliceLollipop() {
        // Now, splice the graph and ensure two graphs are created correctly
        Graph lollipopSpliceGraph = GraphGenerator.getLollipop();
        List<Graph> lollipopSplice = lollipopSpliceGraph.splice();
        // First subgraph is a single edge
        Graph edge = new Graph(2, "Lollipop Subgraph");
        edge.addEdge(0,1);
        // Second subgraph is a triangle
        Graph triangle = GraphGenerator.getTriangle();
        triangle.setName("Lollipop Subgraph");
        // Add both to a list
        List<Graph> correctSplice = Arrays.asList(edge, triangle);
        // Now, assert expected and actual are equal - bit convoluted, so we can ignore order of graphs in list
        Assertions.assertEquals(correctSplice.size(), lollipopSplice.size(), "Splices were different sizes");
//        Assertions.assertTrue(correctSplice.containsAll(lollipopSplice),
//                "The lollipop splice contains too much");
//        Assertions.assertTrue(lollipopSplice.containsAll(correctSplice),
//                "The lollipop splice is missing something.");
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
        Assertions.assertEquals(Collections.singletonList(GraphGenerator.getToast()), toast.clone().splice(),
                "Toast cannot be spliced into sub-graphs.");
    }

    @Test
    void testClone() {
        // Make sure clone() creates an identical instance
        Graph g = GraphGenerator.getLollipop();
        Graph h = g.clone();
        Assertions.assertEquals(g, h, "The clone() method did not make an identical instance.");
        // Now, ensure that changing an attribute of the clone makes it not equal the original
        g.appendVertices(1);
        Assertions.assertNotEquals(g, h, "Expected instances to be different.");
    }

    @Test
    void testEquals() {
        Graph g = GraphGenerator.erdosRenyi(50, 0.5);
        Graph h = g.clone();
        Assertions.assertEquals(g, h, "Identical graphs were not returned true.");
        h.appendVertices(1);
        Assertions.assertNotEquals(g, h, "When passed different graphs, equals method should return false");
    }
}