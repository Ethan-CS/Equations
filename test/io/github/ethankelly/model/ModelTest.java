package io.github.ethankelly.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class ModelTest {

    @Test
    void validStates() {
        Model m = new Model(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        m.addTransition('S', 'I', 0.6);
        m.addTransition('I', 'R', 0.1);

        Assertions.assertTrue(m.validStates(Arrays.asList('S', 'I', 'R')),
                "The whole list of states should be a valid subset of states.");
        Assertions.assertTrue(m.validStates(Arrays.asList('S', 'I')),
                "S and I share a transition.");
        Assertions.assertTrue(m.validStates(Arrays.asList('I', 'S')),
                "Does not check for backwards transitions.");
        Assertions.assertTrue(m.validStates(Arrays.asList('I', 'R')),
                "I and R share a transition.");
        Assertions.assertTrue(m.validStates(Arrays.asList('R', 'I')),
                "Does not check for backwards transitions.");
        Assertions.assertFalse(m.validStates(Arrays.asList('S', 'R')),
                "S and R do not share a direct transition.");
        Assertions.assertFalse(m.validStates(Arrays.asList('R', 'S')),
                "R and S do not share a direct transition.");
        Assertions.assertFalse(m.validStates(Arrays.asList('S', 'P', 'R')),
                "Does not deal with input including states not in the model states correctly.");
    }
}