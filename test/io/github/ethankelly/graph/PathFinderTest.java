package io.github.ethankelly.graph;

import io.github.ethankelly.model.ModelParams;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PathFinderTest {
    static ModelParams SIR = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});

    @BeforeAll
    static void setUp() {
        SIR.addTransition('S', 'I', 0.6);
        SIR.addTransition('I', 'R', 0.1);
    }

    @Test
    void generator() {
        Graph g = new Graph(6);
        g.addEdge(0,1);
        g.addEdge(0,2);
        g.addEdge(1,3);
        g.addEdge(1,4);
        g.addEdge(2,4);
        g.addEdge(2,5);

        PathFinder pf = new PathFinder(g);
        List<List<Vertex>> allStates = new ArrayList<>();
        List<List<Vertex>> childrenOfZero =
                pf.generator(new ArrayList<>(Collections.singletonList(new Vertex(0))), allStates);

        List<List<Vertex>> expectedChildrenOfZero = new ArrayList<>();
        expectedChildrenOfZero.add(new ArrayList<>(Arrays.asList(new Vertex(0), new Vertex(1))));
        expectedChildrenOfZero.add(new ArrayList<>(Arrays.asList(new Vertex(0), new Vertex(2))));

        assertEquals(expectedChildrenOfZero, childrenOfZero);

        List<List<Vertex>> childrenOfZeroOne =
                pf.generator(new ArrayList<>(Arrays.asList(new Vertex(0), new Vertex(1))), allStates);

        List<List<Vertex>> expectedChildrenOfZeroOne = new ArrayList<>();
        expectedChildrenOfZeroOne
                .add(new ArrayList<>(Arrays.asList(
                        new Vertex(0),
                        new Vertex(1),
                        new Vertex(2))));
        expectedChildrenOfZeroOne
                .add(new ArrayList<>(Arrays.asList(
                        new Vertex(0),
                        new Vertex(1),
                        new Vertex(3))));
        expectedChildrenOfZeroOne
                .add(new ArrayList<>(Arrays.asList(
                        new Vertex(0),
                        new Vertex(1),
                        new Vertex(4))));

        assertEquals(expectedChildrenOfZeroOne, childrenOfZeroOne);

        List<List<Vertex>> childrenOfZeroTwo =
                pf.generator(new ArrayList<>(Arrays.asList(new Vertex(0), new Vertex(2))), allStates);

        List<List<Vertex>> expectedChildrenOfZeroTwo = new ArrayList<>();
        expectedChildrenOfZeroTwo
                .add(new ArrayList<>(Arrays.asList(
                        new Vertex(0),
                        new Vertex(2),
                        new Vertex(1))));
        expectedChildrenOfZeroTwo
                .add(new ArrayList<>(Arrays.asList(
                        new Vertex(0),
                        new Vertex(2),
                        new Vertex(4))));
        expectedChildrenOfZeroTwo
                .add(new ArrayList<>(Arrays.asList(
                        new Vertex(0),
                        new Vertex(2),
                        new Vertex(5))));

        assertEquals(expectedChildrenOfZeroTwo, childrenOfZeroTwo);

        List<List<Vertex>> childrenOfZeroOneThree =
                pf.generator(new ArrayList<>(Arrays.asList(
                        new Vertex(0),
                        new Vertex(1),
                        new Vertex(3))), allStates);

        //  TODO expected children of (0,1,3) i.e. (0,1,3,2) and (0,1,3,4)

        List<List<Vertex>> childrenOfZeroOneFour =
                pf.generator(new ArrayList<>(Arrays.asList(
                        new Vertex(0),
                        new Vertex(1),
                        new Vertex(4))), allStates);

        // TODO expected children of (0,1,4) i.e. (0,1,4,3) and (0,1,4,2)

        List<List<Vertex>> childrenOfZeroTwoFour =
                pf.generator(new ArrayList<>(Arrays.asList(
                        new Vertex(0),
                        new Vertex(2),
                        new Vertex(4))), allStates);

        // TODO expected children of (0,2,4) i.e. (0,2,4,1) and (0,2,4,5)

        List<List<Vertex>> childrenOfZeroTwoFive =
                pf.generator(new ArrayList<>(Arrays.asList(
                        new Vertex(0),
                        new Vertex(2),
                        new Vertex(5))), allStates);

        // TODO expected children of (0,2,5) i.e. (0,2,5,1) and (0,2,5,4)

        // TODO which current state leads to us getting the full graph back as child?
    }
}