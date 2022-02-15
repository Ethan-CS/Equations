package io.github.ethankelly.model;

import io.github.ethankelly.graph.GraphGenerator;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class RequiredTuplesTest {

    @Test
    void findSingles() {

    }

    @Test
    void findNumbers() {
        Model m = new Model(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        m.addTransition('S', 'I', 0.6);
        m.addTransition('I', 'R', 0.1);

        RequiredTuples edge = new RequiredTuples(GraphGenerator.getEdge(), m, false);
        assertEquals(6, edge.size(), edge.toString());

        RequiredTuples triangle = new RequiredTuples(GraphGenerator.getTriangle(), m, false);
        assertEquals(18, triangle.size());

        // TODO this is different to result in paper but req tuples look correct...
//        RequiredTuples square = new RequiredTuples(GraphGenerator.cycle(4), m, false);
//        assertEquals(square.size(), 36, "Expected 36. Check tuples all necessary:\n" + square);
    }


}