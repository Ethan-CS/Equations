package io.github.ethankelly.graph;

/**
 * The {@code Vertex} class represents a vertex of a graph.
 */
public class Vertex implements Comparable<Vertex> {
	private int location;

	public Vertex(int location) {
		this.location = location;
	}

	public int getLocation() {
		return location;
	}

	public void setLocation(int newLocation) {
		this.location = newLocation;
	}

	@Override
	public String toString() {
		return String.valueOf(location);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Vertex vertex = (Vertex) o;

		return this.location == vertex.location;
	}

	@Override
	public Vertex clone() {
		try {
			return (Vertex) super.clone();
		} catch (CloneNotSupportedException e) {
			return new Vertex(this.getLocation());
		}
	}

	@Override
	public int hashCode() {
		return location;
	}

	/**
	 * Gives a method to compare two vertices. This comparison is done using the numerical index locations of the two
	 * vertices we wish to compare.
	 *
	 * @param that the vertex to which we wish to compare the vertex in question.
	 * @return -1 if the location of {@code that} is greater than the vertex in question, 0 if they are the same and +1
	 * if the location of {@code that} is less than the vertex in question.
	 */
	@Override
	public int compareTo(Vertex that) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;

		if (this == that) return EQUAL;

		// Primitive numbers follow this form
		if (this.getLocation() < that.getLocation()) return BEFORE;
		if (this.getLocation() > that.getLocation()) return AFTER;

		// All comparisons have yielded equality
		// verify that compareTo is consistent with equals (optional)
		assert this.equals(that) : "compareTo inconsistent with equals.";

		return EQUAL;
	}

}
