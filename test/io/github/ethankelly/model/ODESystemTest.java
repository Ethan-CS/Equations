package io.github.ethankelly.model;

import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.symbols.Greek;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        // ODE system for cycle SIR model
        ODESystem ode = new ODESystem(GraphGenerator.cycle(3), 5, SIR);
        String actual = ode.getEquations();
        // Hard-coded actual equations for triangle network (from original Kiss et al. paper)
        String expected = """
                〈S1〉=-τ〈S1 I2〉-τ〈S1 I3〉
                〈S2〉=-τ〈I1 S2〉-τ〈S2 I3〉
                〈S3〉=-τ〈I1 S3〉-τ〈I2 S3〉
                〈I1〉=τ〈S1 I2〉+τ〈S1 I3〉-γ〈I1〉
                〈I2〉=τ〈I1 S2〉+τ〈S2 I3〉-γ〈I2〉
                〈I3〉=τ〈I1 S3〉+τ〈I2 S3〉-γ〈I3〉
                〈I1 S2〉=τ〈S1 S2 I3〉-γ〈I1 S2〉-τ〈I1 S2〉-τ〈I1 S2 I3〉
                〈S1 I2〉=-τ〈S1 I2〉-τ〈S1 I2 I3〉+τ〈S1 S2 I3〉-γ〈S1 I2〉
                〈I1 S3〉=τ〈S1 I2 S3〉-γ〈I1 S3〉-τ〈I1 S3〉-τ〈I1 I2 S3〉
                〈S1 I3〉=-τ〈S1 I2 I3〉-τ〈S1 I3〉+τ〈S1 I2 S3〉-γ〈S1 I3〉
                〈I2 S3〉=τ〈I1 S2 S3〉-γ〈I2 S3〉-τ〈I1 I2 S3〉-τ〈I2 S3〉
                〈S2 I3〉=-τ〈I1 S2 I3〉-τ〈S2 I3〉+τ〈I1 S2 S3〉-γ〈S2 I3〉
                〈I1 S2 I3〉=τ〈S1 S2 I3〉-γ〈I1 S2 I3〉-τ〈I1 S2 I3〉-τ〈I1 S2 I3〉+τ〈I1 S2 S3〉-γ〈I1 S2 I3〉
                〈S1 I2 S3〉=-τ〈S1 I2 S3〉-γ〈S1 I2 S3〉-τ〈S1 I2 S3〉
                〈I1 I2 S3〉=τ〈S1 I2 S3〉-γ〈I1 I2 S3〉+τ〈I1 S2 S3〉-γ〈I1 I2 S3〉-τ〈I1 I2 S3〉-τ〈I1 I2 S3〉
                〈S1 S2 I3〉=-τ〈S1 S2 I3〉-τ〈S1 S2 I3〉-γ〈S1 S2 I3〉
                〈S1 I2 I3〉=-τ〈S1 I2 I3〉-τ〈S1 I2 I3〉+τ〈S1 S2 I3〉-γ〈S1 I2 I3〉+τ〈S1 I2 S3〉-γ〈S1 I2 I3〉
                〈I1 S2 S3〉=-γ〈I1 S2 S3〉-τ〈I1 S2 S3〉-τ〈I1 S2 S3〉
                """;

        assertEquals(expected, actual);
    }

    @Test
    void lollipopEquations() {
        // ODE system for cycle SIR model
        ODESystem ode = new ODESystem(GraphGenerator.getLollipop(), 5, SIR);
        String actual = ode.getEquations();

        String[] actualSplit = actual.split("\\r?\\n");
//        for (String a : actualSplit) System.out.println(a);

        // Hard-coded actual singles for triangle network (from original Kiss et al. paper)
        String[] expectedSingles = new String[]{
                "〈S1〉=-τ〈S1 I2〉-τ〈S1 I3〉-τ〈S1 I4〉",
                "〈S2〉=-τ〈I1 S2〉",
                "〈S3〉=-τ〈I1 S3〉-τ〈S3 I4〉",
                "〈S4〉=-τ〈I1 S4〉-τ〈I3 S4〉",
                "〈I1〉=τ〈S1 I2〉+τ〈S1 I3〉+τ〈S1 I4〉-γ〈I1〉",
                "〈I2〉=τ〈I1 S2〉-γ〈I2〉",
                "〈I3〉=τ〈I1 S3〉+τ〈S3 I4〉-γ〈I3〉",
                "〈I4〉=τ〈I1 S4〉+τ〈I3 S4〉-γ〈I4〉"
        };
        String[] actualSingles = Arrays.copyOfRange(actualSplit,0,8);

        StringBuilder expectedSinglesSB = new StringBuilder(), actualSinglesSB = new StringBuilder();
        for (String s : expectedSingles) expectedSinglesSB.append(s).append("\n");
        for (String s : actualSingles) actualSinglesSB.append(s).append("\n");
        assertEquals(String.valueOf(expectedSinglesSB), String.valueOf(actualSinglesSB),
                "Incorrect singles produced.");

        String[] expectedDoubles = new String[]{
                "〈I1 S2〉=-γ〈I1 S2〉-τ〈I1 S2〉+τ〈S1 S2 I3〉+τ〈S1 S2 I4〉",
                "〈S1 I2〉=-τ〈S1 I2〉-τ〈S1 I2 I3〉-τ〈S1 I2 I4〉-γ〈S1 I2〉",
                "〈I1 S3〉=τ〈S1 S3 I4〉-γ〈I1 S3〉-τ〈I1 S3〉-τ〈I1 S3 I4〉+τ〈S1 I2 S3〉",
                "〈S1 I3〉=-τ〈S1 I2 I3〉-τ〈S1 I3〉-τ〈S1 I3 I4〉+τ〈S1 S3 I4〉-γ〈S1 I3〉",
                "〈I1 S4〉=τ〈S1 I3 S4〉-γ〈I1 S4〉-τ〈I1 S4〉-τ〈I1 I3 S4〉+τ〈S1 I2 S4〉",
                "〈S1 I4〉=-τ〈S1 I2 I4〉-τ〈S1 I3 I4〉-τ〈S1 I4〉+τ〈S1 I3 S4〉-γ〈S1 I4〉",
                "〈I3 S4〉=τ〈I1 S3 S4〉-γ〈I3 S4〉-τ〈I1 I3 S4〉-τ〈I3 S4〉",
                "〈S3 I4〉=-τ〈I1 S3 I4〉-τ〈S3 I4〉+τ〈I1 S3 S4〉-γ〈S3 I4〉"
        };
        String[] actualDoubles = Arrays.copyOfRange(actualSplit,8,16);

        StringBuilder expectedDoublesSB = new StringBuilder(), actualDoublesSB = new StringBuilder();
        for (String s : expectedDoubles) expectedDoublesSB.append(s).append("\n");
        for (String s : actualDoubles) actualDoublesSB.append(s).append("\n");
        assertEquals(String.valueOf(expectedDoublesSB), String.valueOf(actualDoublesSB),
                "Incorrect doubles produced.");

        // TODO check these with original Kiss paper
//        String[] expectedTriples = new String[]{
//                "〈I1 S3 I4〉=τ〈S1 I2 S3 I4〉+τ〈S1 S3 I4〉-γ〈I1 S3 I4〉-τ〈I1 S3 I4〉-τ〈I1 S3 I4〉+τ〈I1 S3 S4〉-γ〈I1 S3 I4〉",
//                "〈S1 I3 S4〉=-τ〈S1 I2 I3 S4〉-τ〈S1 I3 S4〉-γ〈S1 I3 S4〉-τ〈S1 I3 S4〉",
//                "〈I1 I3 S4〉=τ〈S1 I2 I3 S4〉+τ〈S1 I3 S4〉-γ〈I1 I3 S4〉+τ〈I1 S3 S4〉-γ〈I1 I3 S4〉-τ〈I1 I3 S4〉-τ〈I1 I3 S4〉",
//                "〈S1 S3 I4〉=-τ〈S1 I2 S3 I4〉-τ〈S1 S3 I4〉-τ〈S1 S3 I4〉-γ〈S1 S3 I4〉",
//                "〈S1 I2 I3〉=-τ〈S1 I2 I3〉-τ〈S1 I2 I3〉-γ〈S1 I2 I3〉+τ〈S1 I2 S3 I4〉-γ〈S1 I2 I3〉",
//                "〈I1 S2 S3〉=-γ〈I1 S2 S3〉-τ〈I1 S2 S3〉-τ〈I1 S2 S3〉-τ〈I1 S2 S3 I4〉",
//                "〈S1 I2 I4〉=-τ〈S1 I2 I4〉-τ〈S1 I2 I4〉-γ〈S1 I2 I4〉+τ〈S1 I2 I3 S4〉-γ〈S1 I2 I4〉",
//                "〈I1 S2 S4〉=-γ〈I1 S2 S4〉-τ〈I1 S2 S4〉-τ〈I1 S2 S4〉-τ〈I1 S2 I3 S4〉",
//                "〈S1 I3 I4〉=-τ〈S1 I3 I4〉-τ〈S1 I3 I4〉+τ〈S1 S3 I4〉-γ〈S1 I3 I4〉+τ〈S1 I3 S4〉-γ〈S1 I3 I4〉",
//                "〈I1 S3 S4〉=-γ〈I1 S3 S4〉-τ〈I1 S3 S4〉-τ〈I1 S3 S4〉"
//        };
//        String[] actualTriples = Arrays.copyOfRange(actualSplit,16,26);
//
//        StringBuilder expectedTriplesSB = new StringBuilder(), actualTriplesSB = new StringBuilder();
//        for (String s : expectedTriples) expectedTriplesSB.append(s).append("\n");
//        for (String s : actualTriples) actualTriplesSB.append(s).append("\n");
//        assertEquals(String.valueOf(expectedTriplesSB), String.valueOf(actualTriplesSB),
//                "Incorrect triples produced.");
//
//        String[] expectedQuads = new String[]{
//                "〈S1 I2 I3 S4〉=-τ〈S1 I2 I3 S4〉-τ〈S1 I2 I3 S4〉-γ〈S1 I2 I3 S4〉-γ〈S1 I2 I3 S4〉-τ〈S1 I2 I3 S4〉",
//                "〈I1 S2 S3 I4〉=-γ〈I1 S2 S3 I4〉-τ〈I1 S2 S3 I4〉-τ〈I1 S2 S3 I4〉-τ〈I1 S2 S3 I4〉-γ〈I1 S2 S3 I4〉",
//                "〈S1 I2 S3 I4〉=-τ〈S1 I2 S3 I4〉-τ〈S1 I2 S3 I4〉-γ〈S1 I2 S3 I4〉-τ〈S1 I2 S3 I4〉-γ〈S1 I2 S3 I4〉",
//                "〈I1 S2 I3 S4〉=-γ〈I1 S2 I3 S4〉-τ〈I1 S2 I3 S4〉-γ〈I1 S2 I3 S4〉-τ〈I1 S2 I3 S4〉-τ〈I1 S2 I3 S4〉"
//
//        };
//        String[] actualQuads = Arrays.copyOfRange(actualSplit,26,30);
//
//        StringBuilder expectedQuadsSB = new StringBuilder(), actualQuadsSB = new StringBuilder();
//        for (String s : expectedQuads) expectedQuadsSB.append(s).append("\n");
//        for (String s : actualQuads) actualQuadsSB.append(s).append("\n");
//        assertEquals(String.valueOf(expectedQuadsSB), String.valueOf(actualQuadsSB),
//                "Incorrect triples produced.");
    }

    @Test
    void getEquations() {

    }
}