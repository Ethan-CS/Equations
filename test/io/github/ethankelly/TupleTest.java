package io.github.ethankelly;

import io.github.ethankelly.exceptions.UnexpectedStateException;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import io.github.ethankelly.model.RequiredTuples;
import io.github.ethankelly.model.Tuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TupleTest {
    public static char[] sir = new char[]{'S', 'I', 'R'};

    public static RequiredTuples triangleTuplesClosuresSIR = new RequiredTuples(GraphGenerator.getTriangle(), sir, true);
    public static RequiredTuples triangleTuplesNoClosuresSIR = new RequiredTuples(GraphGenerator.getTriangle(), sir, false);

    public static List<Tuple> expectedTriangleSIRSingles = new ArrayList<>();
    public static List<Tuple> expectedTriangleSIRTuples = new ArrayList<>();

    @BeforeAll
    static void setUp() {
        // Set up expected singles and tuples for SIR triangle
        List<Tuple> TriangleSIRSingles = Arrays.asList(
                new Tuple(Collections.singletonList(new Vertex('S', 0))),
                new Tuple(Collections.singletonList(new Vertex('S', 1))),
                new Tuple(Collections.singletonList(new Vertex('S', 2))),
                new Tuple(Collections.singletonList(new Vertex('I', 0))),
                new Tuple(Collections.singletonList(new Vertex('I', 1))),
                new Tuple(Collections.singletonList(new Vertex('I', 2)))
        );

        expectedTriangleSIRSingles.addAll(TriangleSIRSingles);

        List<Tuple> TriangleSIRDoubles = new ArrayList<>();
        TriangleSIRDoubles.add(new Tuple(Arrays.asList(new Vertex('S', 0),
                new Vertex('I', 1))));
        TriangleSIRDoubles.add(new Tuple(Arrays.asList(new Vertex('S', 0),
                new Vertex('I', 2))));
        TriangleSIRDoubles.add(new Tuple(Arrays.asList(new Vertex('I', 0),
                new Vertex('S', 1))));
        TriangleSIRDoubles.add(new Tuple(Arrays.asList(new Vertex('I', 0),
                new Vertex('S', 2))));
        TriangleSIRDoubles.add(new Tuple(Arrays.asList(new Vertex('S', 1),
                new Vertex('I', 2))));
        TriangleSIRDoubles.add(new Tuple(Arrays.asList(new Vertex('I', 1),
                new Vertex('S', 2))));

        List<Tuple> TriangleSIRTriples = new ArrayList<>();
        TriangleSIRTriples.add(new Tuple(Arrays.asList(new Vertex('S', 0),
                new Vertex('S', 1),
                new Vertex('I', 2))));
        TriangleSIRTriples.add(new Tuple(Arrays.asList(new Vertex('S', 0),
                new Vertex('I', 1),
                new Vertex('S', 2))));
        TriangleSIRTriples.add(new Tuple(Arrays.asList(new Vertex('S', 0),
                new Vertex('I', 1),
                new Vertex('I', 2))));
        TriangleSIRTriples.add(new Tuple(Arrays.asList(new Vertex('I', 0),
                new Vertex('S', 1),
                new Vertex('S', 2))));
        TriangleSIRTriples.add(new Tuple(Arrays.asList(new Vertex('I', 0),
                new Vertex('S', 1),
                new Vertex('I', 2))));
        TriangleSIRTriples.add(new Tuple(Arrays.asList(new Vertex('I', 0),
                new Vertex('I', 1),
                new Vertex('S', 2))));

        expectedTriangleSIRTuples.addAll(TriangleSIRSingles);
        expectedTriangleSIRTuples.addAll(TriangleSIRDoubles);
        expectedTriangleSIRTuples.addAll(TriangleSIRTriples);

    }

    @Test
    void testTriangleGenerateSingles() throws UnexpectedStateException {
        // Check expected and actual singles for SIR
        // With closures
        List<Tuple> actualClosedSIR = triangleTuplesClosuresSIR.findSingles();
        Assertions.assertTrue(expectedTriangleSIRSingles.containsAll(actualClosedSIR),
                "SIR Triangle singles with closures not as expected");

        // Without closures (shouldn't matter for singles)
        List<Tuple> actualNotClosedSIR = triangleTuplesNoClosuresSIR.findSingles();
        Assertions.assertTrue(expectedTriangleSIRSingles.containsAll(actualNotClosedSIR),
                "SIR Triangle singles without closures not as expected");
    }

    @Test
    void testTriangleTuples() {
        Assertions.assertEquals(expectedTriangleSIRTuples.size(), triangleTuplesNoClosuresSIR.getTuples().size(),
                "Expected and actual triangle tuples are different in size.");
        Assertions.assertTrue(expectedTriangleSIRTuples.containsAll(triangleTuplesNoClosuresSIR.getTuples()),
                "Expected triangle tuples without closures does not contain all of actual tuples.");
        Assertions.assertTrue(expectedTriangleSIRTuples.containsAll(triangleTuplesNoClosuresSIR.getTuples()),
                "Obtained triangle tuples without closures does not contain all of expected tuples.");

        // Add in extra tuples for closures (all susceptible states)
        expectedTriangleSIRTuples.add(
                new Tuple(Arrays.asList(new Vertex('S', 0), new Vertex('S', 1))));
        expectedTriangleSIRTuples.add(
                new Tuple(Arrays.asList(new Vertex('S', 0), new Vertex('S', 2))));
        expectedTriangleSIRTuples.add(
                new Tuple(Arrays.asList(new Vertex('S', 1), new Vertex('S', 2))));
        expectedTriangleSIRTuples.add(
                new Tuple(Arrays.asList(new Vertex('S', 0), new Vertex('S', 1),
                new Vertex('S', 2))));

        Assertions.assertTrue(expectedTriangleSIRTuples.containsAll(triangleTuplesClosuresSIR.getTuples()) &&
                        triangleTuplesClosuresSIR.getTuples().containsAll(expectedTriangleSIRTuples),
                "Triangle tuples with closures not as expected");
    }
}
