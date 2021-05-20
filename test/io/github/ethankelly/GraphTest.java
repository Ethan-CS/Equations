package io.github.ethankelly;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GraphTest {

    @Test
    void testClone() {
        Graph g = GraphGenerator.getLollipop();
        Graph h = g.clone();
        Assertions.assertEquals(g, h);

        g.setName("New name");
        Assertions.assertNotEquals(g, h);
    }
}