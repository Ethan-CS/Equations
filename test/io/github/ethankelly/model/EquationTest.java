package io.github.ethankelly.model;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

class EquationTest {
    static Graph lollipop;
    static ModelParams SIR;

    @BeforeAll
    static void setUp() {
        lollipop = GraphGenerator.getLollipop();
        SIR = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        SIR.addTransition('S', 'I', 0.6);
        SIR.addTransition('I', 'R', 0.1);
    }

    @Test
    void testLollipopI1() {
        // 〈I1〉=τ〈S1 I2〉+τ〈S1 I3〉+τ〈S1 I4〉-γ〈I1〉
        Tuple I1 = new Tuple(new Vertex('I', 0));
        Equation eqnI1 = new Equation(I1, SIR, lollipop);

        Map<Tuple, List<Double>> expectedTerms = new HashMap<>();
        expectedTerms.put(new Tuple(List.of(new Vertex('S', 0), new Vertex('I', 1))),
                new ArrayList<>(List.of(0.6)));
        expectedTerms.put(new Tuple(List.of(new Vertex('S', 0), new Vertex('I', 2))),
                new ArrayList<>(List.of(0.6)));
        expectedTerms.put(new Tuple(List.of(new Vertex('S', 0), new Vertex('I', 3))),
                new ArrayList<>(List.of(0.6)));
        expectedTerms.put(new Tuple(List.of(new Vertex('I', 0))),
                new ArrayList<>(List.of(-0.1)));

        Assertions.assertEquals(eqnI1.getTerms(), expectedTerms);
    }

    @Test
    void testLollipopS1() {
        // 〈S1〉=-τ〈S1 I2〉-τ〈S1 I3〉-τ〈S1 I4〉
        Tuple S1 = new Tuple(new Vertex('S', 0));
        Equation eqnS1 = new Equation(S1, SIR, lollipop);

        Map<Tuple, List<Double>> expectedTerms = new HashMap<>();
        expectedTerms.put(new Tuple(List.of(new Vertex('S', 0), new Vertex('I', 1))),
                new ArrayList<>(List.of(-0.6)));
        expectedTerms.put(new Tuple(List.of(new Vertex('S', 0), new Vertex('I', 2))),
                new ArrayList<>(List.of(-0.6)));
        expectedTerms.put(new Tuple(List.of(new Vertex('S', 0), new Vertex('I', 3))),
                new ArrayList<>(List.of(-0.6)));

        Assertions.assertEquals(eqnS1.getTerms(), expectedTerms);
    }

    @Test
    void testLollipopI2() {
        // 〈I2〉=τ〈I1 S2〉-γ〈I2〉
        Tuple I2 = new Tuple(new Vertex('I', 1));
        Equation eqnI2 = new Equation(I2, SIR, lollipop);

        Map<Tuple, List<Double>> expectedTerms = new HashMap<>();
        expectedTerms.put(new Tuple(List.of(new Vertex('I', 0), new Vertex('S', 1))),
                new ArrayList<>(List.of(0.6)));
        expectedTerms.put(new Tuple(List.of(new Vertex('I', 1))),
                new ArrayList<>(List.of(-0.1)));

        Assertions.assertEquals(eqnI2.getTerms(), expectedTerms);
    }

    @Test
    void testLollipopS2() {
        // 〈S2〉=-τ〈I1 S2〉
        Tuple S2 = new Tuple(new Vertex('S', 1));
        Equation eqnS2 = new Equation(S2, SIR, lollipop);

        Map<Tuple, List<Double>> expectedTerms = new HashMap<>();
        expectedTerms.put(new Tuple(List.of(new Vertex('I', 0), new Vertex('S', 1))),
                new ArrayList<>(List.of(-0.6)));

        Assertions.assertEquals(eqnS2.getTerms(), expectedTerms);
    }

    @Test
    void testLollipopI3() {
        //〈I3〉=τ〈I1 S3〉+τ〈S3 I4〉-γ〈I3〉
        Tuple I3 = new Tuple(new Vertex('I', 2));
        Equation eqnI3 = new Equation(I3, SIR, lollipop);

        Map<Tuple, List<Double>> expectedTerms = new HashMap<>();
        expectedTerms.put(new Tuple(List.of(new Vertex('I', 0), new Vertex('S', 2))),
                new ArrayList<>(List.of(0.6)));
        expectedTerms.put(new Tuple(List.of(new Vertex('S', 2), new Vertex('I', 3))),
                new ArrayList<>(List.of(0.6)));
        expectedTerms.put(new Tuple(List.of(new Vertex('I', 2))),
                new ArrayList<>(List.of(-0.1)));

        Assertions.assertEquals(eqnI3.getTerms(), expectedTerms);
    }

    @Test
    void testLollipopS3() {
        //〈S3〉=-τ〈I1 S3〉-τ〈S3 I4〉
        Tuple S3 = new Tuple(new Vertex('S', 2));
        Equation eqnS3 = new Equation(S3, SIR, lollipop);

        Map<Tuple, List<Double>> expectedTerms = new HashMap<>();
        expectedTerms.put(new Tuple(List.of(new Vertex('I', 0), new Vertex('S', 2))),
                new ArrayList<>(List.of(-0.6)));
        expectedTerms.put(new Tuple(List.of(new Vertex('S', 2), new Vertex('I', 3))),
                new ArrayList<>(List.of(-0.6)));

        Assertions.assertEquals(eqnS3.getTerms(), expectedTerms);
    }

    @Test
    void testLollipopI4() {
        //〈I4〉=τ〈I1 S4〉+τ〈I3 S4〉-γ〈I4〉
        Tuple I4 = new Tuple(new Vertex('I', 3));
        Equation eqnI4 = new Equation(I4, SIR, lollipop);

        Map<Tuple, List<Double>> expectedTerms = new HashMap<>();
        expectedTerms.put(new Tuple(List.of(new Vertex('I', 0), new Vertex('S', 3))),
                new ArrayList<>(List.of(0.6)));
        expectedTerms.put(new Tuple(List.of(new Vertex('I', 2), new Vertex('S', 3))),
                new ArrayList<>(List.of(0.6)));
        expectedTerms.put(new Tuple(List.of(new Vertex('I', 3))),
                new ArrayList<>(List.of(-0.1)));

        Assertions.assertEquals(eqnI4.getTerms(), expectedTerms);
    }

    @Test
    void testLollipopS4() {
        // 〈S4〉=-τ〈I1 S4〉-τ〈I3 S4〉
        Tuple S4 = new Tuple(new Vertex('S', 3));
        Equation eqnS4 = new Equation(S4, SIR, lollipop);

        Map<Tuple, List<Double>> expectedTerms = new HashMap<>();
        expectedTerms.put(new Tuple(List.of(new Vertex('I', 0), new Vertex('S', 3))),
                new ArrayList<>(List.of(-0.6)));
        expectedTerms.put(new Tuple(List.of(new Vertex('I', 2), new Vertex('S', 3))),
                new ArrayList<>(List.of(-0.6)));

        Assertions.assertEquals(eqnS4.getTerms(), expectedTerms);
    }
}