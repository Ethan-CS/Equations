package io.github.ethankelly.model;

import io.github.ethankelly.graph.Graph;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelParamsTest {

    @Test
    void getFilterGraph() {
        ModelParams m = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        m.addTransition('S', 'I', 0.6);
        m.addTransition('I', 'R', 0.1);

        Graph expected = new Graph(2, "");
        expected.setLabels(Arrays.asList('S', 'I'));
        expected.addDirectedEdge(0, 1);

        assertTrue(m.getFilterGraph().isIsomorphic(expected),
                "Expected SIR filter graph:\nActual:" + m.getFilterGraph()+ "\nExpected:"+expected);

    }
}