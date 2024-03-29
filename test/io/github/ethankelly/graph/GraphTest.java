package io.github.ethankelly.graph;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(g1.getCutVertices(), Arrays.asList(new Vertex(2), new Vertex(3)),
                "Should have identified 2 and 3 as the only cut-vertices for G1.");
    }

    @Test
    void testCutVerticesG2() {
        assertEquals(g2.getCutVertices(), Arrays.asList(new Vertex(1), new Vertex(2)),
                "Should have identified 1 and 2 as cut-vertices for G2.");
    }

    @Test
    void testCutVerticesLollipop() {
        // Get the cut vertices - should just be vertex 0
        assertEquals(GraphGenerator.getLollipop().getCutVertices(), Collections.singletonList(new Vertex(0)),
                "Lollipop has exactly one cut vertex at location 0");
    }

    @Test
    void testCutVerticesToast() {
        // Get the cut vertices - should just be vertex 0
        assertEquals(toast.getCutVertices(), Collections.emptyList(),
                "Toast has no cut vertices");
    }

    @Test
    void testSpliceToast() {
        // Now, assert expected and actual are equal - bit convoluted so we can ignore order of graphs in list
        assertEquals(Collections.singletonList(GraphGenerator.getToast()), toast.clone().splice(),
                "Toast cannot be spliced into sub-graphs.");
    }

    @Test
    void testClone() {
        // Make sure clone() creates an identical instance
        Graph g = GraphGenerator.getLollipop();
        Graph h = g.clone();
        assertEquals(g, h, "The clone() method did not make an identical instance.");
        // Now, ensure that changing an attribute of the clone makes it not equal the original
        g.appendVertices(1);
        Assertions.assertNotEquals(g, h, "Expected instances to be different.");
    }

    @Test
    void testEquals() {
        Graph g = GraphGenerator.erdosRenyi(50, 0.5);
        Graph h = g.clone();
        assertEquals(g, h, "Identical graphs were not returned true.");
        h.appendVertices(1);
        Assertions.assertNotEquals(g, h, "When passed different graphs, equals method should return false");
    }

	@Test
	void hasDirectedEdge() {
        Graph g = new Graph(4, "");
        g.addDirectedEdge(0,1);
        g.addDirectedEdge(1,2);
        g.addDirectedEdge(2,3);
        g.addDirectedEdge(3,0);

        Assertions.assertTrue(g.hasDirectedEdge(0,1));
        Assertions.assertTrue(g.hasDirectedEdge(1,2));
        Assertions.assertTrue(g.hasDirectedEdge(2,3));
        Assertions.assertTrue(g.hasDirectedEdge(3,0));

        assertFalse(g.hasDirectedEdge(0, 2));
        assertFalse(g.hasDirectedEdge(0, 3));
        assertFalse(g.hasDirectedEdge(1, 0));
        assertFalse(g.hasDirectedEdge(1, 3));
        assertFalse(g.hasDirectedEdge(2, 1));
        assertFalse(g.hasDirectedEdge(2, 0));
        assertFalse(g.hasDirectedEdge(3, 2));
        assertFalse(g.hasDirectedEdge(3, 1));

        }

    @Test
    void makeSubGraph() {
        Graph K5 = GraphGenerator.complete(5);

        List<Vertex> subG = new ArrayList<>();
        subG.add(new Vertex(2));
        subG.add(new Vertex(3));
        subG.add(new Vertex(4));

        Graph K5_sub = K5.makeSubGraph(subG);
        Graph triangle = GraphGenerator.getTriangle();

        assertTrue(K5_sub.isIsomorphic(triangle));
    }

    @Test
    void isConnected() {
        // Connected graph
        Graph graph = new Graph(5, "");
        graph.addEdge(0,1);
        graph.addEdge(0,4);
        graph.addEdge(1,4);
        graph.addEdge(1,3);
        graph.addEdge(3,4);
        graph.addEdge(2,1);
        graph.addEdge(2,3);

        assertTrue(graph.isConnected());

        // Disconnected graph
        graph = new Graph(5, "");
        graph.addEdge(0,1);
        graph.addEdge(0,4);
        graph.addEdge(1,4);
        graph.addEdge(1,3);
        graph.addEdge(3,4);

        assertFalse(graph.isConnected());
    }
}