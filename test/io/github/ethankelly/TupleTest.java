package io.github.ethankelly;

import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
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
        for (int i = 0; i < 3; i++) {
            expectedTriangleSIRSingles.add(new Vertex('S', i));
            expectedTriangleSIRSingles.add(new Vertex('I', i));

            expectedTriangleSIRTuples.add(Collections.singletonList(new Vertex('S', i)));
            expectedTriangleSIRTuples.add(Collections.singletonList(new Vertex('I', i)));
            List<Vertex> newVerticesSI = new ArrayList<>();
            List<Vertex> newVerticesIS = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                newVerticesSI.clear();
                newVerticesIS.clear();
                if (i != j) {
                    newVerticesSI.add(new Vertex('S', i));
                    newVerticesSI.add(new Vertex('I', j));
                    newVerticesIS.add(new Vertex('I', i));
                    newVerticesIS.add(new Vertex('S', j));
                    expectedTriangleSIRTuples.add(newVerticesSI);
                    expectedTriangleSIRTuples.add(newVerticesIS);
                }
            }
        }
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
        Assertions.assertTrue(expectedTriangleSIRTuples.containsAll(triangleTuplesNoClosuresSIR.getTuples()) &&
                        triangleTuplesNoClosuresSIR.getTuples().containsAll(expectedTriangleSIRTuples),
                "Triangle tuples without closures not as expected");
        Assertions.assertTrue(expectedTriangleSIRTuples.containsAll(triangleTuplesClosuresSIR.getTuples()) &&
                        triangleTuplesClosuresSIR.getTuples().containsAll(expectedTriangleSIRTuples),
                "Triangle tuples with closures not as expected");
    }
}
