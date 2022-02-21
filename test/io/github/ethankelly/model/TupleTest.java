package io.github.ethankelly.model;

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
    void areLocationsDifferent() {
        Random r = new Random();
        int i = r.nextInt(10000);
        int maybe = r.nextInt(10000);
        int j = (maybe != i) ? maybe : (maybe + 1);
        int maybe2 = r.nextInt(10000);
        int k = (maybe2 != i && maybe2 != j) ? maybe2 : (maybe2 + 1);

        RequiredTuples.Tuple tValid = new RequiredTuples.Tuple(Arrays.asList(
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

        RequiredTuples.Tuple t = new RequiredTuples.Tuple(Arrays.asList(
                new Vertex(c, i),
                new Vertex(d, j),
                new Vertex(e, k)
        ));

        RequiredTuples.Tuple u = new RequiredTuples.Tuple(Arrays.asList(
                new Vertex(c, i),
                new Vertex(d, j),
                new Vertex(e, k)
        ));

        Assertions.assertEquals(t, u, "Equivalent tuples are not equal. Check: " + t + ", " + u);

        RequiredTuples.Tuple v = new RequiredTuples.Tuple(Arrays.asList(
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

        RequiredTuples.Tuple t = new RequiredTuples.Tuple(Arrays.asList(
                new Vertex(c, i),
                new Vertex(d, j),
                new Vertex(e, k)
        ));

        RequiredTuples.Tuple u = new RequiredTuples.Tuple(Arrays.asList(
                new Vertex(c, i),
                new Vertex(d, j),
                new Vertex(e, k)
        ));

        Assertions.assertEquals(t.hashCode(), u.hashCode(), "Equivalent tuples don't have the same hash. Check: " + t + ", " + u);

        RequiredTuples.Tuple v = new RequiredTuples.Tuple(Arrays.asList(
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