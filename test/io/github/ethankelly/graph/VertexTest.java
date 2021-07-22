package io.github.ethankelly.graph;

import io.github.ethankelly.Tuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class VertexTest {

	@Test
	void areStatesDifferent() {
		List<Vertex> S = new ArrayList<>();
		S.add(new Vertex('S', 1));
		S.add(new Vertex('S', 2));
		S.add(new Vertex('S', 3));
		Tuple allSusceptible = new Tuple(S);

		List<Vertex> I = new ArrayList<>();
		I.add(new Vertex('I', 1));
		I.add(new Vertex('I', 2));
		I.add(new Vertex('I', 3));
		Tuple allInfected = new Tuple(I);

		List<Vertex> SIR = new ArrayList<>();
		SIR.add(new Vertex('S', 1));
		SIR.add(new Vertex('I', 2));
		SIR.add(new Vertex('R', 3));
		Tuple differentStates = new Tuple(SIR);

		Assertions.assertFalse(allSusceptible.areStatesDifferent(false),
				"All susceptible without closures should return false");
		Assertions.assertTrue(differentStates.areStatesDifferent(false),
				"Different states should return true without closures");
		Assertions.assertTrue(differentStates.areStatesDifferent(true),
				"Different states should return true with closures");
		Assertions.assertTrue(allSusceptible.areStatesDifferent(true),
				"All susceptible with closures should return true");
		Assertions.assertFalse(allInfected.areStatesDifferent(true),
				"All the same (other than susceptible) should return false with closures");
	}

	@Test
	void areLocationsDifferent() {
		List<Vertex> same = new ArrayList<>();
		same.add(new Vertex('S', 1));
		same.add(new Vertex('I', 1));
		same.add(new Vertex('R', 1));
		Tuple allSameLocation = new Tuple(same);

		List<Vertex> different = new ArrayList<>();
		different.add(new Vertex('S', 1));
		different.add(new Vertex('I', 2));
		different.add(new Vertex('R', 3));
		Tuple allDifferentLocation = new Tuple(different);

		Assertions.assertFalse(allSameLocation.areLocationsDifferent(),
				"All same location vertices should return false.");
		Assertions.assertTrue(allDifferentLocation.areLocationsDifferent(),
				"All different locations should return true.");
		allDifferentLocation.add(new Vertex('S', 1));
		Assertions.assertFalse(allDifferentLocation.areLocationsDifferent(),
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