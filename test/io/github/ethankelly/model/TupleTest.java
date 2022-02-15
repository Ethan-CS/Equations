package io.github.ethankelly.model;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

class TupleTest {

    public static ModelParams m;

    @BeforeEach
    void setUp() {
        m = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        m.addTransition('S', 'I', 0.6);
        m.addTransition('I', 'R', 0.1);
    }

    @Test
    void isValidTuple() {
        /*
         * This must test the following:
         * (1) Each vertex in the tuple is distinct,
         * (2) The vertices in the tuple constitute a connected subgraph,
         * (3) The tuple contains two or more states and
         * (4) The states in the tuple constitute a connected subgraph of the transition graph.
         */

        Tuple t1 = new Tuple(Arrays.asList(
                new Vertex('S', 0),
                new Vertex('S',  1),
                new Vertex('S', 2)));


        Graph edge = new Graph(2, "Edge");
        edge.addEdge(0, 1);
        // Vertices not in the tuple
        Assertions.assertFalse(t1.isValidTuple(m, edge,  true),
                "Tuples with vertices not in the graph should not be valid");

        // (1) All values are distinct
        Tuple t2 = new Tuple(Arrays.asList(new Vertex(0), new Vertex(1), new Vertex(0)));
        Assertions.assertFalse(t2.locationsAreDifferent(),
                "Two vertices with same location accepted as a valid tuple");

        Tuple t3 = new Tuple(Arrays.asList(new Vertex('S', 0), new Vertex('T', 1), new Vertex('U', 2)));
        Assertions.assertTrue(t3.locationsAreDifferent(),
                "Tuple with no duplicate vertex locations not accepted as valid");

        // (2) Vertices in tuple constitute a connected subgraph
        Tuple t4 = new Tuple(Arrays.asList(new Vertex('A', 0), new Vertex('B', 1), new Vertex('C', 2)));
        Assertions.assertTrue(t4.areAllConnected(GraphGenerator.complete(3)),
                "Connected subgraph of vertices not accepted as valid");
        Assertions.assertFalse(t4.areAllConnected(new Graph(3, "")),
                "Disjoint components incorrectly accepted as a valid single tuple");

        // (3) and (4) tested in validStates test
    }

    @Test
    void validStates() {
        Graph triangle = GraphGenerator.getTriangle();

        Tuple tStatesNotInModel = new Tuple(Arrays.asList(new Vertex('S', 0), new Vertex('P', 1)));

        // States not in the tuple
        Assertions.assertFalse(tStatesNotInModel.isValidTuple(m, GraphGenerator.getEdge(),  true),
                "Tuples with states not in the model should not be valid");

        Tuple t1 = new Tuple(Arrays.asList(
                new Vertex('S', 0),
                new Vertex('S',  1),
                new Vertex('S', 2)));

        // Test all susceptible tuples
        Assertions.assertFalse(t1.isValidTuple(m, triangle, false),
                "An all-susceptible tuple when we don't require closures should not be valid");
        System.out.println("*****");
        Assertions.assertTrue(t1.isValidTuple(m, triangle, true),
                "An all-susceptible tuple when we do require closures should be valid");
    }

    @Test
    void areLocationsDifferent() {
        Random r = new Random();
        int i = r.nextInt(10000);
        int maybe = r.nextInt(10000);
        int j = (maybe != i) ? maybe : (maybe + 1);
        int maybe2 = r.nextInt(10000);
        int k = (maybe2 != i && maybe2 != j) ? maybe2 : (maybe2 + 1);

        Tuple tValid = new Tuple(Arrays.asList(
                new Vertex('S', i),
                new Vertex('P', j),
                new Vertex('Q', k)));

        Assertions.assertTrue(tValid.locationsAreDifferent(),
                "Method returns false on a tuple with different locations");

    }

    @RepeatedTest(50)
    void testEquals() {
        Random r = new Random();
        int i = r.nextInt(10000), j = r.nextInt(10000), k = r.nextInt(10000),
                l = r.nextInt(10000), m = r.nextInt(10000), n = r.nextInt(10000);
        char c = (char) r.nextInt(128), d = (char) r.nextInt(128), e = (char) r.nextInt(128),
        f = (char) r.nextInt(128), g = (char) r.nextInt(128), h = (char) r.nextInt(128);

        Tuple t = new Tuple(Arrays.asList(
                new Vertex(c, i),
                new Vertex(d, j),
                new Vertex(e, k)
        ));

        Tuple u = new Tuple(Arrays.asList(
                new Vertex(c, i),
                new Vertex(d, j),
                new Vertex(e, k)
        ));

        Assertions.assertEquals(t, u, "Equivalent tuples are not equal. Check: " + t + ", " + u);

        Tuple v = new Tuple(Arrays.asList(
                new Vertex(f, l),
                new Vertex(g, m),
                new Vertex(h, n)
        ));

        Assertions.assertNotEquals(t, v, "Different vertices are declared equal. Check: " + t + ", " + v);
        Assertions.assertNotEquals(u, v, "Different vertices are declared equal. Check: " + t + ", " + v);

    }

    @Test
    void testHashCode() {
        Random r = new Random();
        int i = r.nextInt(10000), j = r.nextInt(10000), k = r.nextInt(10000),
                l = r.nextInt(10000), m = r.nextInt(10000), n = r.nextInt(10000);
        char c = (char) r.nextInt(128), d = (char) r.nextInt(128), e = (char) r.nextInt(128),
                f = (char) r.nextInt(128), g = (char) r.nextInt(128), h = (char) r.nextInt(128);

        Tuple t = new Tuple(Arrays.asList(
                new Vertex(c, i),
                new Vertex(d, j),
                new Vertex(e, k)
        ));

        Tuple u = new Tuple(Arrays.asList(
                new Vertex(c, i),
                new Vertex(d, j),
                new Vertex(e, k)
        ));

        Assertions.assertEquals(t.hashCode(), u.hashCode(), "Equivalent tuples don't have the same hash. Check: " + t + ", " + u);

        Tuple v = new Tuple(Arrays.asList(
                new Vertex(f, l),
                new Vertex(g, m),
                new Vertex(h, n)
        ));

        Assertions.assertNotEquals(t.hashCode(), v.hashCode(), "Different vertices have the same hash. Check: " + t + ", " + v);
        Assertions.assertNotEquals(u.hashCode(), v.hashCode(), "Different vertices have the same hash. Check: " + t + ", " + v);

    }

    @Test
    void add() {

    }

    @Test
    void addAll() {
    }

    @Test
    void compareTo() {
    }

    @Test
    void size() {
    }

    @Test
    void contains() {
    }

    @Test
    void testClone() {
    }
}