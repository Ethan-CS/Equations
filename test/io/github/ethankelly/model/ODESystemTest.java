package io.github.ethankelly.model;

import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import io.github.ethankelly.symbols.Greek;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ODESystemTest {
    private static ModelParams SIR;

    @BeforeAll
    static void setUp() {
        // SIR Model parameters
        SIR = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        SIR.addTransition('S', 'I', Greek.TAU.uni());
        SIR.addTransition('I', 'R', Greek.GAMMA.uni());
        SIR.addTransition('S', 'I', 0.8);
        SIR.addTransition('I', 'R', 0.1);
    }

    @Test
    void triangleEquations() {
        // Expected tuples
        List<Tuple> expectedTuples = new ArrayList<>(Arrays.asList(
                // Singles
                new Tuple(new Vertex('S', 0)), new Tuple(new Vertex('I', 0)),
                new Tuple(new Vertex('S', 1)), new Tuple(new Vertex('I', 1)),
                new Tuple(new Vertex('S', 2)), new Tuple(new Vertex('I', 2)),
                // Doubles
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S', 0), new Vertex('I', 1)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('I', 0), new Vertex('S', 1)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S', 1), new Vertex('I', 2)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('I', 1), new Vertex('S', 2)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S', 0), new Vertex('I', 2)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('I', 0), new Vertex('S', 2)))),
                // Triples
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S', 0), new Vertex('S',1), new Vertex('I', 2)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('I', 0), new Vertex('S', 1), new Vertex('S',2)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S',0), new Vertex('I', 1), new Vertex('S', 2)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('I',0), new Vertex('I', 1), new Vertex('S', 2)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('I',0), new Vertex('S', 1), new Vertex('I', 2)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S',0), new Vertex('I', 1), new Vertex('I', 2))))
                ));

        // Actual tuples
        ODESystem system = new ODESystem(GraphGenerator.getTriangle(), 10, SIR);
        List<Equation> equations = system.getEquations();
        List<Tuple> actual = new ArrayList<>();
        for(Equation e : equations) actual.add(e.getTuple());

        assertTrue(actual.containsAll(expectedTuples), "actual:\n"+actual+"\nexpected:\n"+expectedTuples);
        assertTrue(expectedTuples.containsAll(actual), "actual:\n"+actual+"\nexpected:\n"+expectedTuples);
    }

    @Test
    void lollipopEquations() {
        // Expected tuples - no closures
        List<Tuple> expectedTuples = new ArrayList<>(Arrays.asList(
                // Singles
                new Tuple(new Vertex('S', 0)), new Tuple(new Vertex('I', 0)),
                new Tuple(new Vertex('S', 1)), new Tuple(new Vertex('I', 1)),
                new Tuple(new Vertex('S', 2)), new Tuple(new Vertex('I', 2)),
                new Tuple(new Vertex('S', 3)), new Tuple(new Vertex('I', 3)),
                // Doubles
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S', 0), new Vertex('I', 1)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('I', 0), new Vertex('S', 1)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S', 0), new Vertex('I', 2)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('I', 0), new Vertex('S', 2)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S', 0), new Vertex('I', 3)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('I', 0), new Vertex('S', 3)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S', 2), new Vertex('I', 3)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('I', 2), new Vertex('S', 3)))),
                // Triples
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S', 0), new Vertex('I',2), new Vertex('I', 3)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S', 0), new Vertex('S', 2), new Vertex('I',3)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S',0), new Vertex('I', 2), new Vertex('S', 3)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('I',0), new Vertex('S', 2), new Vertex('I', 3)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('I',0), new Vertex('I', 2), new Vertex('S', 3)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('I',0), new Vertex('S', 2), new Vertex('S', 3)))),
                // Can be closed:
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S',0), new Vertex('I', 1), new Vertex('I', 3)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S',0), new Vertex('I', 1), new Vertex('I', 2)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S',0), new Vertex('S', 1), new Vertex('I', 2)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S',0), new Vertex('S', 1), new Vertex('I', 3)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S',0), new Vertex('I', 1), new Vertex('S', 2)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S',0), new Vertex('I', 1), new Vertex('S', 3)))),
                // Quads
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S',0), new Vertex('I', 1), new Vertex('I', 2), new Vertex('I',3)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S',0), new Vertex('I', 1), new Vertex('I', 2), new Vertex('S',3)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S',0), new Vertex('I', 1), new Vertex('S', 2), new Vertex('I',3)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S',0), new Vertex('S', 1), new Vertex('I', 2), new Vertex('I',3)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S',0), new Vertex('S', 1), new Vertex('I', 2), new Vertex('S',3)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S',0), new Vertex('S', 1), new Vertex('S', 2), new Vertex('I',3)))),
                new Tuple(new ArrayList<>(Arrays.asList(
                        new Vertex('S',0), new Vertex('I', 1), new Vertex('S', 2), new Vertex('S',3))))
        ));
        // Actual tuples
        ODESystem system = new ODESystem(GraphGenerator.getLollipop(), 10, SIR);
        List<Equation> equations = system.getEquations();
        List<Tuple> actual = new ArrayList<>();
        for(Equation e : equations) actual.add(e.getTuple());
        // Get any terms in expected output not in the actual output
        List<Tuple> missingFromActual = new ArrayList<>(expectedTuples);
        missingFromActual.removeAll(actual);

        // Get any terms in actual output that weren't expected
        List<Tuple> extraInActual = new ArrayList<>(actual);
        extraInActual.removeAll(expectedTuples);

        assertTrue(missingFromActual.isEmpty(), "Didn't get all the tuples expected.\n"+missingFromActual.stream().map(m -> m + "\n").collect(Collectors.joining()));
        assertTrue(extraInActual.isEmpty(), "Got more tuples than expected:\n"+extraInActual.stream().map(m -> m + "\n").collect(Collectors.joining()));
    }

    @Test
    void getEquations() {

    }
}