package io.github.ethankelly.graph;

import io.github.ethankelly.model.ModelParams;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
}