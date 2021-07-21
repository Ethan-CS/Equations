package io.github.ethankelly;

import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TupleTest {
    public static char[] si = new char[]{'S', 'I'};
    public static char[] sir = new char[]{'S', 'I', 'R'};
    public static char[] sip = new char[]{'S', 'I', 'P'};
    public static char[] sirp = new char[]{'S', 'I', 'R', 'P'};

    public static Tuples triangleTuplesClosuresSIR = new Tuples(GraphGenerator.getTriangle(), sir, true);
    public static Tuples triangleTuplesNoClosuresSIR = new Tuples(GraphGenerator.getTriangle(), sir, false);
    public static Tuples triangleTuplesClosuresSIRP = new Tuples(GraphGenerator.getTriangle(), sirp, true);
    public static Tuples triangleTuplesNoClosuresSIRP = new Tuples(GraphGenerator.getTriangle(), sirp, false);

    public static List<List<Vertex>> expectedTriangleSIRPSingles = new ArrayList<>();
    public static List<List<Vertex>> expectedTriangleSIRPTuples = new ArrayList<>();
    public static List<List<Vertex>> expectedTriangleSIRSingles = new ArrayList<>();
    public static List<List<Vertex>> expectedTriangleSIRTuples = new ArrayList<>();

    @BeforeAll
    static void setUp() {
        // Set up expected singles and tuples for SIR triangle
        expectedTriangleSIRSingles.add(Collections.singletonList(new Vertex('S', 0)));
        expectedTriangleSIRSingles.add(Collections.singletonList(new Vertex('S', 1)));
        expectedTriangleSIRSingles.add(Collections.singletonList(new Vertex('S', 2)));
        expectedTriangleSIRSingles.add(Collections.singletonList(new Vertex('I', 0)));
        expectedTriangleSIRSingles.add(Collections.singletonList(new Vertex('I', 1)));
        expectedTriangleSIRSingles.add(Collections.singletonList(new Vertex('I', 2)));

        List<List<Vertex>> expectedTriangleSIRDoubles = new ArrayList<>();
        expectedTriangleSIRDoubles.add(Arrays.asList(new Vertex('S', 0), new Vertex('I', 1)));
        expectedTriangleSIRDoubles.add(Arrays.asList(new Vertex('S', 0), new Vertex('I', 2)));
        expectedTriangleSIRDoubles.add(Arrays.asList(new Vertex('I', 0), new Vertex('S', 1)));
        expectedTriangleSIRDoubles.add(Arrays.asList(new Vertex('I', 0), new Vertex('S', 2)));
        expectedTriangleSIRDoubles.add(Arrays.asList(new Vertex('S', 1), new Vertex('I', 2)));
        expectedTriangleSIRDoubles.add(Arrays.asList(new Vertex('I', 1), new Vertex('S', 2)));

        expectedTriangleSIRTuples.addAll(expectedTriangleSIRSingles);
        expectedTriangleSIRTuples.addAll(expectedTriangleSIRDoubles);

        expectedTriangleSIRTuples.add(Arrays.asList(new Vertex('S', 0),
                new Vertex('S', 1),
                new Vertex('I', 2)));
        expectedTriangleSIRTuples.add(Arrays.asList(new Vertex('S', 0),
                new Vertex('I', 1),
                new Vertex('S', 2)));
        expectedTriangleSIRTuples.add(Arrays.asList(new Vertex('S', 0),
                new Vertex('I', 1),
                new Vertex('I', 2)));
        expectedTriangleSIRTuples.add(Arrays.asList(new Vertex('I', 0),
                new Vertex('S', 1),
                new Vertex('S', 2)));
        expectedTriangleSIRTuples.add(Arrays.asList(new Vertex('I', 0),
                new Vertex('S', 1),
                new Vertex('I', 2)));
        expectedTriangleSIRTuples.add(Arrays.asList(new Vertex('I', 0),
                new Vertex('I', 1),
                new Vertex('S', 2)));

        System.out.println(expectedTriangleSIRTuples);
        System.out.println(triangleTuplesNoClosuresSIR);
        System.out.println(expectedTriangleSIRTuples.size());
        System.out.println(triangleTuplesNoClosuresSIR.getTuples().size());

        expectedTriangleSIRPSingles.addAll(expectedTriangleSIRSingles);
        expectedTriangleSIRPSingles.add(Collections.singletonList(new Vertex('P', 0)));
        expectedTriangleSIRPSingles.add(Collections.singletonList(new Vertex('P', 1)));
        expectedTriangleSIRPSingles.add(Collections.singletonList(new Vertex('P', 2)));

//        List<List<Vertex>> expectedTriangleSIRPDoubles = new ArrayList<>();
//        expectedTriangleSIRPDoubles.add()

        expectedTriangleSIRPTuples.addAll(expectedTriangleSIRPSingles);

    }

    @Test
    void testTriangleGenerateSingles() {
        // Check expected and actual singles for SIR
        // With closures
        List<List<Vertex>> actualClosedSIR = triangleTuplesClosuresSIR.findSingles();
        Assertions.assertTrue(expectedTriangleSIRSingles.containsAll(actualClosedSIR),
                "SIR Triangle singles with closures not as expected");

        // Without closures (shouldn't matter for singles)
        List<List<Vertex>> actualNotClosedSIR = triangleTuplesNoClosuresSIR.findSingles();
        Assertions.assertTrue(expectedTriangleSIRSingles.containsAll(actualNotClosedSIR),
                "SIR Triangle singles without closures not as expected");

        // Same again, for SIRP
        // With closures
        List<List<Vertex>> actualClosedSIRP = triangleTuplesClosuresSIRP.findSingles();
        Assertions.assertTrue(expectedTriangleSIRPSingles.containsAll(actualClosedSIRP),
                "SIRP triangle singles with closures not as expected");

        // Without closures (shouldn't matter for singles)
        List<List<Vertex>> actualNotClosedSIRP = triangleTuplesNoClosuresSIRP.findSingles();
        Assertions.assertTrue(expectedTriangleSIRPSingles.containsAll(actualNotClosedSIRP),
                "SIRP triangle singles without closures not as expected");
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
        expectedTriangleSIRTuples.add(Arrays.asList(new Vertex('S', 0), new Vertex('S', 1)));
        expectedTriangleSIRTuples.add(Arrays.asList(new Vertex('S', 0), new Vertex('S', 2)));
        expectedTriangleSIRTuples.add(Arrays.asList(new Vertex('S', 1), new Vertex('S', 2)));
        expectedTriangleSIRTuples.add(Arrays.asList(new Vertex('S', 0), new Vertex('S', 1),
                new Vertex('S', 2)));

        Assertions.assertTrue(expectedTriangleSIRTuples.containsAll(triangleTuplesClosuresSIR.getTuples()) &&
                        triangleTuplesClosuresSIR.getTuples().containsAll(expectedTriangleSIRTuples),
                "Triangle tuples with closures not as expected");
    }
}
