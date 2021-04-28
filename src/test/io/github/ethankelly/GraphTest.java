package test.io.github.ethankelly;

import main.io.github.ethankelly.Graph;
import main.io.github.ethankelly.GraphGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GraphTest {

    @Test
    void testClone() {
        Graph g = GraphGenerator.getLollipop();
        Graph h = g.clone();
        Assertions.assertEquals(g, h);

        g.setName("New name");
//        Assertions.assertNotEquals(g, h);
    }
}