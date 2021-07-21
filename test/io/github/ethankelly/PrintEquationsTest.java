package io.github.ethankelly;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PrintEquationsTest {
    public static char[] SIR = new char[]{'S', 'I', 'R'};
    public static char[] SIRP = new char[]{'S', 'I', 'R', 'P'};

    @Test
    void testEdgeBoundsClosuresSIR() {
        Assertions.assertEquals(PrintEquations.getLowerBound(2, SIR, true),
                PrintEquations.getUpperBound(2, SIR, true),
                "Upper and lower bounds should be same for single edge since cycle and connected are the same");
        Assertions.assertEquals(7, PrintEquations.getUpperBound(2, SIR, true),
                "Upper bound should be 7 equations.");
        Assertions.assertEquals(7, PrintEquations.getLowerBound(2, SIR, true),
                "Lower bound should be 7 equations.");
    }

    @Test
    void testEdgeBoundsClosuresSIRP() {
        Assertions.assertEquals(PrintEquations.getLowerBound(2, SIRP, true),
                PrintEquations.getUpperBound(2, SIRP, true),
                "Upper and lower bounds should be same for single edge since cycle and connected are the same");
        Assertions.assertEquals(13, PrintEquations.getUpperBound(2, SIRP, true),
                "Upper bound should be 13 equations.");
        Assertions.assertEquals(13, PrintEquations.getLowerBound(2, SIRP, true),
                "Lower bound should be 13 equations.");
    }

    @Test
    void testEdgeBoundsNoClosuresSIR() {
        Assertions.assertEquals(PrintEquations.getLowerBound(2, SIR, false),
                PrintEquations.getUpperBound(2, SIR, false),
                "Upper and lower bounds should be same for single edge since cycle and connected are the same");
        Assertions.assertEquals(6, PrintEquations.getUpperBound(2, SIR, false),
                "Upper bound should be 6 equations.");
        Assertions.assertEquals(6, PrintEquations.getLowerBound(2, SIR, false),
                "Lower bound should be 6 equations.");
    }

    @Test
    void testEdgeBoundsNoClosuresSIRP() {
        Assertions.assertEquals(PrintEquations.getLowerBound(2, SIRP, false),
                PrintEquations.getUpperBound(2, SIRP, false),
                "Upper and lower bounds should be same for single edge since cycle and connected are the same");
        Assertions.assertEquals(12, PrintEquations.getUpperBound(2, SIRP, false),
                "Upper bound should be 12 equations.");
        Assertions.assertEquals(12, PrintEquations.getLowerBound(2, SIRP, false),
                "Lower bound should be 12 equations.");
    }

    @Test
    void testTriangleBoundsClosuresSIR() {
        Assertions.assertEquals(PrintEquations.getLowerBound(3, SIR, true),
                PrintEquations.getUpperBound(3, SIR, true),
                "Upper and lower bounds should be same for triangle, " +
                        "since cycle and connected graphs on 3 vertices are the same");
        Assertions.assertEquals(22, PrintEquations.getUpperBound(3, SIR, true),
                "Upper bound should be 22 equations.");
        Assertions.assertEquals(22, PrintEquations.getLowerBound(3, SIR, true),
                "Lower bound should be 22 equations.");
    }

    @Test
    void testTriangleBoundsClosuresSIRP() {
        Assertions.assertEquals(PrintEquations.getLowerBound(3, SIRP, true),
                PrintEquations.getUpperBound(3, SIRP, true),
                "Upper and lower bounds should be same for triangle, " +
                        "since cycle and connected graphs on 3 vertices are the same");
        Assertions.assertEquals(55, PrintEquations.getUpperBound(3, SIRP, true),
                "Upper bound should be 55 equations.");
        Assertions.assertEquals(55, PrintEquations.getLowerBound(3, SIRP, true),
                "Lower bound should be 55 equations.");
    }

    @Test
    void testTriangleBoundsNoClosuresSIR() {
        Assertions.assertEquals(PrintEquations.getLowerBound(3, SIR, false),
                PrintEquations.getUpperBound(3, SIR, false),
                "Upper and lower bounds should be same for triangle, " +
                        "since cycle and connected graphs on 3 vertices are the same");
        Assertions.assertEquals(18, PrintEquations.getUpperBound(3, SIR, false),
                "Upper bound should be 18 equations.");
        Assertions.assertEquals(18, PrintEquations.getLowerBound(3, SIR, false),
                "Lower bound should be 18 equations.");
    }

    @Test
    void testTriangleBoundsNoClosuresSIRP() {
        Assertions.assertEquals(PrintEquations.getLowerBound(3, SIRP, false),
                PrintEquations.getUpperBound(3, SIRP, false),
                "Upper and lower bounds should be same for triangle, " +
                        "since cycle and connected graphs on 3 vertices are the same");
        Assertions.assertEquals(51, PrintEquations.getUpperBound(3, SIRP, false),
                "Upper bound should be 51 equations.");
        Assertions.assertEquals(51, PrintEquations.getLowerBound(3, SIRP, false),
                "Lower bound should be 51 equations.");
    }

    @Test
    void testLollipopBoundsClosuresSIR() {
        

    }
}