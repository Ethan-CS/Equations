package io.github.ethankelly;

import org.junit.jupiter.api.Assertions;

class EquationsTest {
    public static char[] SIR = new char[]{'S', 'I', 'R'};
    public static char[] SIRP = new char[]{'S', 'I', 'R', 'P'};

    @org.junit.jupiter.api.Test
    void testEdgeBoundsClosuresSIR() {
        Assertions.assertEquals(Equations.getLowerBound(2, SIR, true),
                Equations.getUpperBound(2, SIR, true),
                "Upper and lower bounds should be same for single edge since cycle and connected are the same");
        Assertions.assertEquals(7, Equations.getUpperBound(2, SIR, true),
                "Upper bound should be 7 equations.");
        Assertions.assertEquals(7, Equations.getLowerBound(2, SIR, true),
                "Lower bound should be 7 equations.");
    }

    @org.junit.jupiter.api.Test
    void testEdgeBoundsClosuresSIRP() {
        Assertions.assertEquals(Equations.getLowerBound(2, SIRP, true),
                Equations.getUpperBound(2, SIRP, true),
                "Upper and lower bounds should be same for single edge since cycle and connected are the same");
        Assertions.assertEquals(13, Equations.getUpperBound(2, SIRP, true),
                "Upper bound should be 13 equations.");
        Assertions.assertEquals(13, Equations.getLowerBound(2, SIRP, true),
                "Lower bound should be 13 equations.");
    }

    @org.junit.jupiter.api.Test
    void testEdgeBoundsNoClosuresSIR() {
        Assertions.assertEquals(Equations.getLowerBound(2, SIR, false),
                Equations.getUpperBound(2, SIR, false),
                "Upper and lower bounds should be same for single edge since cycle and connected are the same");
        Assertions.assertEquals(6, Equations.getUpperBound(2, SIR, false),
                "Upper bound should be 6 equations.");
        Assertions.assertEquals(6, Equations.getLowerBound(2, SIR, false),
                "Lower bound should be 6 equations.");
    }

    @org.junit.jupiter.api.Test
    void testEdgeBoundsNoClosuresSIRP() {
        Assertions.assertEquals(Equations.getLowerBound(2, SIRP, false),
                Equations.getUpperBound(2, SIRP, false),
                "Upper and lower bounds should be same for single edge since cycle and connected are the same");
        Assertions.assertEquals(12, Equations.getUpperBound(2, SIRP, false),
                "Upper bound should be 12 equations.");
        Assertions.assertEquals(12, Equations.getLowerBound(2, SIRP, false),
                "Lower bound should be 12 equations.");
    }

    @org.junit.jupiter.api.Test
    void testTriangleBoundsClosuresSIR() {
        Assertions.assertEquals(Equations.getLowerBound(3, SIR, true),
                Equations.getUpperBound(3, SIR, true),
                "Upper and lower bounds should be same for triangle, " +
                        "since cycle and connected graphs on 3 vertices are the same");
        Assertions.assertEquals(22, Equations.getUpperBound(3, SIR, true),
                "Upper bound should be 22 equations.");
        Assertions.assertEquals(22, Equations.getLowerBound(3, SIR, true),
                "Lower bound should be 22 equations.");
    }

    @org.junit.jupiter.api.Test
    void testTriangleBoundsClosuresSIRP() {
        Assertions.assertEquals(Equations.getLowerBound(3, SIRP, true),
                Equations.getUpperBound(3, SIRP, true),
                "Upper and lower bounds should be same for triangle, " +
                        "since cycle and connected graphs on 3 vertices are the same");
        Assertions.assertEquals(55, Equations.getUpperBound(3, SIRP, true),
                "Upper bound should be 55 equations.");
        Assertions.assertEquals(55, Equations.getLowerBound(3, SIRP, true),
                "Lower bound should be 55 equations.");
    }

    @org.junit.jupiter.api.Test
    void testTriangleBoundsNoClosuresSIR() {
        Assertions.assertEquals(Equations.getLowerBound(3, SIR, false),
                Equations.getUpperBound(3, SIR, false),
                "Upper and lower bounds should be same for triangle, " +
                        "since cycle and connected graphs on 3 vertices are the same");
        Assertions.assertEquals(18, Equations.getUpperBound(3, SIR, false),
                "Upper bound should be 18 equations.");
        Assertions.assertEquals(18, Equations.getLowerBound(3, SIR, false),
                "Lower bound should be 18 equations.");
    }

    @org.junit.jupiter.api.Test
    void testTriangleBoundsNoClosuresSIRP() {
        Assertions.assertEquals(Equations.getLowerBound(3, SIRP, false),
                Equations.getUpperBound(3, SIRP, false),
                "Upper and lower bounds should be same for triangle, " +
                        "since cycle and connected graphs on 3 vertices are the same");
        Assertions.assertEquals(51, Equations.getUpperBound(3, SIRP, false),
                "Upper bound should be 51 equations.");
        Assertions.assertEquals(51, Equations.getLowerBound(3, SIRP, false),
                "Lower bound should be 51 equations.");
    }
}