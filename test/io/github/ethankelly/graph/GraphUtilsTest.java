package io.github.ethankelly.graph;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphUtilsTest {

    @Test
    void powerSet() {
        List<Integer> mySet = new ArrayList<>();
        mySet.add(1);
        mySet.add(2);
        mySet.add(3);
        // Get the output of power set method on above list
        List<List<Integer>> powerSet = GraphUtils.powerSet(mySet);
        // Manually create the expected power set
        List<List<Integer>> expected = new ArrayList<>();
        expected.add(new ArrayList<>(Collections.emptyList()));
        expected.add(new ArrayList<>(Collections.singletonList(1)));
        expected.add(new ArrayList<>(Collections.singletonList(2)));
        expected.add(new ArrayList<>(Collections.singletonList(3)));
        expected.add(new ArrayList<>(Arrays.asList(1, 2)));
        expected.add(new ArrayList<>(Arrays.asList(1, 3)));
        expected.add(new ArrayList<>(Arrays.asList(2, 3)));
        expected.add(new ArrayList<>(Arrays.asList(1, 2, 3)));
        // Containment - make sure A subset B and B subset A to ensure A=B
        assertTrue(powerSet.containsAll(expected));
        assertTrue(expected.containsAll(powerSet));
    }
}