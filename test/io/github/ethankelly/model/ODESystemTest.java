package io.github.ethankelly.model;

import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.symbols.Greek;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class ODESystemTest {

    @Test
    void testGetEquations() {
        // SIR Model parameters
        ModelParams SIR = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        SIR.addTransition('S', 'I', Greek.TAU.uni());
        SIR.addTransition('I', 'R', Greek.GAMMA.uni());
        SIR.addTransition('S', 'I', 0.8);
        SIR.addTransition('I', 'R', 0.1);
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

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void getEquations() {

    }
}