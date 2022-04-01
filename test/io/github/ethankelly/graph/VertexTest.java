package io.github.ethankelly.graph;

import io.github.ethankelly.model.Tuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class VertexTest {

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

		Assertions.assertFalse(allSameLocation.locationsAreDifferent(),
				"All same location vertices should return false.");
		Assertions.assertTrue(allDifferentLocation.locationsAreDifferent(),
				"All different locations should return true.");
		allDifferentLocation.add(new Vertex('I', 1));
		Assertions.assertFalse(allDifferentLocation.locationsAreDifferent(),
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