package io.github.ethankelly.graph;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class VertexTest {

	@Test
	void areStatesDifferent() {
		List<Vertex> allSusceptible = new ArrayList<>();
		allSusceptible.add(new Vertex('S', 1));
		allSusceptible.add(new Vertex('S', 2));
		allSusceptible.add(new Vertex('S', 3));

		List<Vertex> allInfected = new ArrayList<>();
		allInfected.add(new Vertex('I', 1));
		allInfected.add(new Vertex('I', 2));
		allInfected.add(new Vertex('I', 3));

		List<Vertex> differentStates = new ArrayList<>();
		differentStates.add(new Vertex('S', 1));
		differentStates.add(new Vertex('I', 2));
		differentStates.add(new Vertex('R', 3));

		Assertions.assertFalse(Vertex.areStatesDifferent(allSusceptible, false),
				"All susceptible without closures should return false");
		Assertions.assertTrue(Vertex.areStatesDifferent(differentStates, false),
				"Different states should return true without closures");
		Assertions.assertTrue(Vertex.areStatesDifferent(differentStates, true),
				"Different states should return true with closures");
		Assertions.assertTrue(Vertex.areStatesDifferent(allSusceptible, true),
				"All susceptible with closures should return true");
		Assertions.assertFalse(Vertex.areStatesDifferent(allInfected, true),
				"All the same (other than susceptible) should return false with closures");
	}

	@Test
	void areLocationsDifferent() {
		List<Vertex> allSameLocation = new ArrayList<>();
		allSameLocation.add(new Vertex('S', 1));
		allSameLocation.add(new Vertex('I', 1));
		allSameLocation.add(new Vertex('R', 1));

		List<Vertex> allDifferentLocation = new ArrayList<>();
		allDifferentLocation.add(new Vertex('S', 1));
		allDifferentLocation.add(new Vertex('I', 2));
		allDifferentLocation.add(new Vertex('R', 3));

		Assertions.assertFalse(Vertex.areLocationsDifferent(allSameLocation),
				"All same location vertices should return false.");
		Assertions.assertTrue(Vertex.areLocationsDifferent(allDifferentLocation),
				"All different locations should return true.");
		allDifferentLocation.add(new Vertex('S', 1));
		Assertions.assertFalse(Vertex.areLocationsDifferent(allDifferentLocation),
				"Two of same location in list should return false.");
	}

	@Test
	void testEquals() {
		Vertex v = new Vertex('S', 1);
		Vertex w = new Vertex('S', 1);

		Assertions.assertEquals(v, w, "Equivalent vertices did not return true");

		Vertex x = new Vertex('I', 1);
		Vertex y = new Vertex('S', 2);

		Assertions.assertNotEquals(v, x, "Non-equivalent vertices returned equal");
		Assertions.assertNotEquals(v, y, "Non-equivalent vertices returned equal");
	}
}