package test.io.github.ethankelly;

import main.io.github.ethankelly.Equations;
import org.junit.jupiter.api.Assertions;

class EquationsTest {
    public static char[] SIR = new char[]{'S', 'I', 'R'};
    public static char[] SIRP = new char[]{'S', 'I', 'R', 'P'};

    @org.junit.jupiter.api.Test
    void testEdgeBoundsClosuresSIR() {
        Assertions.assertEquals(7, Equations.getUpperBound(2, SIR, true),
                "Upper bound should be 7.");
        Assertions.assertEquals(4, Equations.getLowerBound(2, SIR, true),
                "Lower bound should be 4.");
    }

    @org.junit.jupiter.api.Test
    void testEdgeBoundsClosuresSIRP() {
        Assertions.assertEquals(13, Equations.getUpperBound(2, SIRP, true),
                "Upper bound should be 13.");
        Assertions.assertEquals(6, Equations.getLowerBound(2, SIRP, true),
                "Lower bound should be 6");
    }

    @org.junit.jupiter.api.Test
    void testEdgeBoundsNoClosuresSIR() {
        Assertions.assertEquals(6, Equations.getUpperBound(2, SIR, false),
                "Upper bound should be 6.");
        Assertions.assertEquals(4, Equations.getLowerBound(2, SIR, false),
                "Lower bound should be 4.");
    }

    @org.junit.jupiter.api.Test
    void testEdgeBoundsNoClosuresSIRP() {
        Assertions.assertEquals(12, Equations.getUpperBound(2, SIRP, false),
                "Upper bound should be 12.");
        Assertions.assertEquals(6, Equations.getLowerBound(2, SIRP, false),
                "Lower bound should be 6.");
    }

    @org.junit.jupiter.api.Test
    void testTriangleBoundsClosuresSIR() {
        Assertions.assertEquals(22, Equations.getUpperBound(3, SIR, true),
                "Upper bound should be 22.");
        Assertions.assertEquals(6, Equations.getLowerBound(3, SIR, true),
                "Lower bound should be 6.");
    }

    @org.junit.jupiter.api.Test
    void testTriangleBoundsClosuresSIRP() {
        Assertions.assertEquals(55, Equations.getUpperBound(3, SIRP, true),
                "Upper bound should be 55.");
        Assertions.assertEquals(9, Equations.getLowerBound(3, SIRP, true),
                "Lower bound should be 9.");
    }

    @org.junit.jupiter.api.Test
    void testTriangleBoundsNoClosuresSIR() {
        Assertions.assertEquals(18, Equations.getUpperBound(3, SIR, false),
                "Upper bound should be 18");
        Assertions.assertEquals(18, Equations.getUpperBound(3, SIR, false),
                "Lower bound should be .");
    }

    @org.junit.jupiter.api.Test
    void testTriangleBoundsNoClosuresSIRP() {
        Assertions.assertEquals(51, Equations.getUpperBound(3, SIRP, false),
                "Upper bound should be 51.");
        Assertions.assertEquals(9, Equations.getLowerBound(3, SIRP, false),
                "Lower bound should be 9.");
    }
}