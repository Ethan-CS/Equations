package io.github.ethankelly.graph;

import io.github.ethankelly.model.ModelParams;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathFinderTest {
    static ModelParams SIR = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
    private static Graph c5;

    @BeforeAll
    static void setUp() {
        SIR.addTransition('S', 'I', 0.6);
        SIR.addTransition('I', 'R', 0.1);
        c5 = GraphGenerator.cycle(5);
    }

//    @Test
//    void generator() {
//        Graph g = new Graph(6);
//        g.addEdge(0,1);
//        g.addEdge(0,2);
//        g.addEdge(1,3);
//        g.addEdge(1,4);
//        g.addEdge(2,4);
//        g.addEdge(2,5);
//
//        PathFinder pf = new PathFinder(g);
//        List<List<Vertex>> childrenOf0 =
//                pf.generator(new ArrayList<>(Collections.singletonList(new Vertex(0))));
//
//        List<List<Vertex>> expectedChildrenOf0 = new ArrayList<>();
//        expectedChildrenOf0.add(new ArrayList<>(Arrays.asList(new Vertex(0), new Vertex(1))));
//        expectedChildrenOf0.add(new ArrayList<>(Arrays.asList(new Vertex(0), new Vertex(2))));
//
//        assertEquals(expectedChildrenOf0, childrenOf0);
//
//        List<List<Vertex>> childrenOf01 =
//                pf.generator(new ArrayList<>(Arrays.asList(new Vertex(0), new Vertex(1))));
//
//        List<List<Vertex>> expectedChildrenOf01 = new ArrayList<>();
//        expectedChildrenOf01
//                .add(new ArrayList<>(Arrays.asList(
//                        new Vertex(0),
//                        new Vertex(1),
//                        new Vertex(3))));
//        expectedChildrenOf01
//                .add(new ArrayList<>(Arrays.asList(
//                        new Vertex(0),
//                        new Vertex(1),
//                        new Vertex(4))));
//
//        assertTrue(expectedChildrenOf01.containsAll(childrenOf01) &&
//                childrenOf01.containsAll(expectedChildrenOf01),
//                "\nExpected: " + expectedChildrenOf01 + "\nActual  : " + childrenOf01);
//
//        List<List<Vertex>> childrenOf02 =
//                pf.generator(new ArrayList<>(Arrays.asList(new Vertex(0), new Vertex(2))));
//
//        List<List<Vertex>> expectedChildrenOf02 = new ArrayList<>();
//        expectedChildrenOf02
//                .add(new ArrayList<>(Arrays.asList(
//                        new Vertex(0),
//                        new Vertex(2),
//                        new Vertex(4))));
//        expectedChildrenOf02
//                .add(new ArrayList<>(Arrays.asList(
//                        new Vertex(0),
//                        new Vertex(2),
//                        new Vertex(5))));
//
//        assertEquals(expectedChildrenOf02, childrenOf02);
//
//        List<List<Vertex>> childrenOf013 =
//                pf.generator(new ArrayList<>(Arrays.asList(
//                        new Vertex(0),
//                        new Vertex(1),
//                        new Vertex(3))));
//
//        List<List<Vertex>> expectedChildrenOf013 = new ArrayList<>();
//
//        expectedChildrenOf013.add(new ArrayList<>(Arrays.asList(
//                new Vertex(0),
//                new Vertex(1),
//                new Vertex(3),
//                new Vertex(2))));
//        expectedChildrenOf013.add(new ArrayList<>(Arrays.asList(
//                new Vertex(0),
//                new Vertex(1),
//                new Vertex(3),
//                new Vertex(4))));
//
//        assertEquals(expectedChildrenOf013, childrenOf013);
//
//        List<List<Vertex>> childrenOf014 =
//                pf.generator(new ArrayList<>(Arrays.asList(
//                        new Vertex(0),
//                        new Vertex(1),
//                        new Vertex(4))));
//
//        List<List<Vertex>> expectedChildrenOf014 = new ArrayList<>();
//
//        expectedChildrenOf014.add(new ArrayList<>(Arrays.asList(
//                new Vertex(0),
//                new Vertex(1),
//                new Vertex(4),
//                new Vertex(3))));
//        expectedChildrenOf014.add(new ArrayList<>(Arrays.asList(
//                new Vertex(0),
//                new Vertex(1),
//                new Vertex(4),
//                new Vertex(2))));
//
//        Assertions.assertTrue(expectedChildrenOf014.containsAll(childrenOf014) &&
//                        childrenOf014.containsAll(expectedChildrenOf014));
//
//        List<List<Vertex>> childrenOf024 =
//                pf.generator(new ArrayList<>(Arrays.asList(
//                        new Vertex(0),
//                        new Vertex(2),
//                        new Vertex(4))));
//
//        List<List<Vertex>> expectedChildrenOf024 = new ArrayList<>();
//
//        expectedChildrenOf024.add(new ArrayList<>(Arrays.asList(
//                new Vertex(0),
//                new Vertex(2),
//                new Vertex(4),
//                new Vertex(1)
//        )));
//
//        expectedChildrenOf024.add(new ArrayList<>(Arrays.asList(
//                new Vertex(0),
//                new Vertex(2),
//                new Vertex(4),
//                new Vertex(5)
//        )));
//
//        assertTrue(expectedChildrenOf024.containsAll(childrenOf024) &&
//                childrenOf024.containsAll(expectedChildrenOf024));
//
//        List<List<Vertex>> childrenOf025 =
//                pf.generator(new ArrayList<>(Arrays.asList(
//                        new Vertex(0),
//                        new Vertex(2),
//                        new Vertex(5))));
//
//        List<List<Vertex>> expectedChildrenOf025 = new ArrayList<>();
//
//        expectedChildrenOf025.add(new ArrayList<>(Arrays.asList(
//                new Vertex(0),
//                new Vertex(2),
//                new Vertex(5),
//                new Vertex(1)
//        )));
//        expectedChildrenOf025.add(new ArrayList<>(Arrays.asList(
//                new Vertex(0),
//                new Vertex(2),
//                new Vertex(5),
//                new Vertex(4)
//        )));
//
//        assertTrue(expectedChildrenOf025.containsAll(childrenOf025) &&
//                childrenOf025.containsAll(expectedChildrenOf025));
//
//        // TODO something that means generator returns all vertices of the graph
//    }

    @Test
    void generatorCycleSingles() {
        PathFinder pf = new PathFinder(c5);

        for (int v = 0; v < c5.getNumVertices(); v++) {
            List<List<Vertex>> actualChildren = pf.generator(new ArrayList<>(Collections.singletonList(new Vertex(v))));
            List<List<Vertex>> expectedChildren = new ArrayList<>(
                    Arrays.asList(
                            new ArrayList<>(Arrays.asList(
                                    new Vertex(v), new Vertex(Math.floorMod((v - 1), c5.getNumVertices())))),
                            new ArrayList<>(Arrays.asList(
                                    new Vertex(v), new Vertex(Math.floorMod((v + 1), c5.getNumVertices()))))
            ));

            assertTrue(actualChildren.containsAll(expectedChildren),
                    "Generated children of "+ v +" missing some states.\n" +
                            "Expected: " + expectedChildren + "\n" +
                            "Actual  : " + actualChildren);
            assertTrue(expectedChildren.containsAll(actualChildren),
                    "More states in generated states from " + v + " than expected.\n" +
                            "Expected: " + expectedChildren + "\n" +
                            "Actual  : " + actualChildren);
        }
    }

    @Test
    void combSearchOnPaths() {
        for (int i = 1; i <= 10; i++) {
            int expected = 0;
            Graph g = GraphGenerator.path(i);
            for (int j = 1; j <= i; j++) expected += (i - (j-1));
            List<List<Vertex>> act = new PathFinder(g).combSearch();
            int actual = act.size();
            assertEquals(expected, actual,
                    "\nExpected: " + expected + "\n" +
                            "Actual  : " + act);
        }
    }
}