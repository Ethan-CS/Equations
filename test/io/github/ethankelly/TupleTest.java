package io.github.ethankelly;

import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TupleTest {
    public static char[] si = new char[]{'S', 'I'};
    public static char[] sir = new char[]{'S', 'I', 'R'};
    public static char[] sip = new char[]{'S', 'I', 'P'};
    public static char[] sirp = new char[]{'S', 'I', 'R', 'P'};

    public static Tuple triangleTuplesClosuresSIR = new Tuple(GraphGenerator.getTriangle(), sir, true);
    public static Tuple triangleTuplesNoClosuresSIR = new Tuple(GraphGenerator.getTriangle(), sir, false);
    public static Tuple triangleTuplesClosuresSIRP = new Tuple(GraphGenerator.getTriangle(), sirp, true);
    public static Tuple triangleTuplesNoClosuresSIRP = new Tuple(GraphGenerator.getTriangle(), sirp, false);

    public static List<Vertex> expectedTriangleSIRPSingles = new ArrayList<>();
    public static List<List<Vertex>> expectedTriangleSIRPTuples = new ArrayList<>();
    public static List<Vertex> expectedTriangleSIRSingles = new ArrayList<>();
    public static List<List<Vertex>> expectedTriangleSIRTuples = new ArrayList<>();

    @BeforeAll
    static void setUp() {
        // Set up expected singles and tuples for SIR triangle
        expectedTriangleSIRSingles.add(new Vertex('S', 0));
        expectedTriangleSIRSingles.add(new Vertex('S', 1));
        expectedTriangleSIRSingles.add(new Vertex('S', 2));
        expectedTriangleSIRSingles.add(new Vertex('I', 0));
        expectedTriangleSIRSingles.add(new Vertex('I', 1));
        expectedTriangleSIRSingles.add(new Vertex('I', 2));

        expectedTriangleSIRPSingles.addAll(expectedTriangleSIRSingles);
        expectedTriangleSIRPSingles.add(new Vertex('P', 0));
        expectedTriangleSIRPSingles.add(new Vertex('P', 1));
        expectedTriangleSIRPSingles.add(new Vertex('P', 2));

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
                new Vertex('S', 1),
                new Vertex('S', 2)));

        System.out.println(expectedTriangleSIRTuples);
        System.out.println(expectedTriangleSIRTuples.size());
        expectedTriangleSIRSingles.sort(null);

        // Set up expected singles for SIRP triangle
        for (int i = 0; i < 3; i++) {
            expectedTriangleSIRPSingles.add(new Vertex('S', i));
            expectedTriangleSIRPSingles.add(new Vertex('I', i));
            expectedTriangleSIRPSingles.add(new Vertex('P', i));
        }
        expectedTriangleSIRPSingles.sort(null);
    }

    @Test
    void testTriangleGenerateSingles() {
        // Check expected and actual singles for SIR
        // With closures
        List<Vertex> actualClosedSIR = triangleTuplesClosuresSIR.findSingles();
        actualClosedSIR.sort(null);
        Assertions.assertEquals(expectedTriangleSIRSingles, actualClosedSIR,
                "SIR Triangle singles with closures not as expected");

        // Without closures (shouldn't matter for singles)
        List<Vertex> actualNotClosedSIR = triangleTuplesNoClosuresSIR.findSingles();
        actualNotClosedSIR.sort(null);
        Assertions.assertEquals(expectedTriangleSIRSingles, actualNotClosedSIR,
                "SIR Triangle singles without closures not as expected");

        // Same again, for SIRP
        // With closures
        List<Vertex> actualClosedSIRP = triangleTuplesClosuresSIRP.findSingles();
        actualClosedSIRP.sort(null);
        Assertions.assertEquals(expectedTriangleSIRPSingles, actualClosedSIRP,
                "SIRP triangle singles with closures not as expected");

        // Without closures (shouldn't matter for singles)
        List<Vertex> actualNotClosedSIRP = triangleTuplesNoClosuresSIRP.findSingles();
        actualNotClosedSIRP.sort(null);
        Assertions.assertEquals(expectedTriangleSIRPSingles, actualNotClosedSIRP,
                "SIRP triangle singles without closures not as expected");
    }

    @Test
    void testTriangleTuples() {
        Assertions.assertTrue(expectedTriangleSIRTuples.containsAll(triangleTuplesNoClosuresSIR.getTuples()),
                "Triangle tuples without closures contains too many tuples");
        Assertions.assertTrue(triangleTuplesNoClosuresSIR.getTuples().containsAll(expectedTriangleSIRTuples),
                "Triangle tuples without closures is missing tuples");
//        Assertions.assertTrue(expectedTriangleSIRTuples.containsAll(triangleTuplesClosuresSIR.getTuples()) &&
//                        triangleTuplesClosuresSIR.getTuples().containsAll(expectedTriangleSIRTuples),
//                "Triangle tuples with closures not as expected");
    }
}
