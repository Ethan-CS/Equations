package io.github.ethankelly.graph;

import io.github.ethankelly.symbols.Maths;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;


/**
 * The {@code VertexState} class represents an instance of a vertex being in a particular state in a compartmental
 * epidemiological model. Each vertex object has a state (for instance, {@code 'S'} for "Susceptible") and a numerical
 * location in the graph we are interested in.
 */
public class Vertex implements Comparable<Vertex> {
	private int location;
	private char state;

	public Vertex(int location) {
		this.location = location;
		this.state = ' ';
	}

	public Vertex(char state, int location) {
		this.state = state;
		this.location = location;
	}

	/**
	 * Given a list of vertices (some tuple), this method checks whether all of the states are the same. If they are the
	 * same, we do not have to consider the associated equation of the tuple in the final system of equations that
	 * describes our compartmental model.
	 *
	 * @param toCheck the tuple we wish to verify has at least two different states in its elements.
	 * @return true if at least one vertex state is different to that of the others, false if they are all the same.
	 */
	public static boolean areStatesDifferent(List<Vertex> toCheck, boolean reqClosures) {
		boolean statesDifferent = false;
		// If there's only one vertex in the list, by definition we require it
		// in the system of equations so we ensure this method returns true.
		if (toCheck.size() == 1) statesDifferent = true;
		else {
			// We only need to find two different states to know that the states are
			// at least not all the same, returning true.
			for (Vertex v : toCheck) {
				for (Vertex w : toCheck) {
					if (v.getState() != w.getState()) {
						statesDifferent = true;
						break;
					} else if (reqClosures) {
						if ((v.getState() == 'S') ||  (w.getState() == 'S')) {
							statesDifferent = true;
							break;
						}
					}
				}
			}
		}
		return statesDifferent;
	}

	public char getState() {
		return this.state;
	}


	public int getLocation() {
		return location;
	}

	public void setLocation(int newLocation) {
		this.location = newLocation;
	}

	@Override
	public Vertex clone() {
		try {
			return (Vertex) super.clone();
		} catch (CloneNotSupportedException e) {
			return new Vertex(this.getLocation());
		}
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

	/**
	 * Given some list of vertices (some tuple), this method checks whether the index locations of every element of the
	 * tuple are all distinct. If even two of the vertices have the same location, it is not a valid tuple for our
	 * purposes and we will not hve to consider the associated equation in the final system of equations that describes
	 * our compartmental model.
	 *
	 * @param vertices the tuple we wish to verify has no repeated vertex locations.
	 * @return true if no index location is repeated in the vertices, false otherwise.
	 */
	public static boolean areLocationsDifferent(List<Vertex> vertices) {
		boolean locationsDifferent = true;
		// Only need to find two vertices in the list with same index location
		// to know we don't have a required tuple, returning false.
		for (Vertex v : vertices) {
			if (vertices.stream().anyMatch(w -> v.getLocation() == w.getLocation() && v != w)) {
				locationsDifferent = false;
				break;
			}
		}
		return locationsDifferent;
	}

	/**
	 * Given some list of vertices (tuple), this method verifies that all of the vertices in the graph given are in fact
	 * connected. That is, we are only interested in a tuple of vertices that constitute some manner of path (cycle or
	 * such like) - if not, then we do not have to consider the associated equation in the final system of equations
	 * that describes our compartmental model and we can discount the given tuple.
	 *
	 * @param toCheck the tuple we wish to check constitutes some kind of path.
	 * @param g       the graph in which the tuple exists.
	 * @return true if the tuple forms a path in some way, false otherwise.
	 */
	public static boolean areAllConnected(List<Vertex> toCheck, Graph g) {
		// If there's only one vertex in the list, required in the system of equations - ensure this returns true.
		// If more than one vertex, return whether they all have some path between them.
		return toCheck.size() == 1 || IntStream.range(0, toCheck.size() - 1).allMatch(
				i -> g.hasEdge(new Vertex(toCheck.get(i).getLocation()),
						new Vertex(toCheck.get(i + 1).getLocation()))
		);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		Vertex that = (Vertex) o;
		return getState() == that.getState() && getLocation() == that.getLocation();
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getState(), getLocation());
	}

//	@Override
//	public int compareTo(VertexState that) {
//		int comparison = 0;
//		int stateComparison = String.valueOf(this.getState()).compareTo(String.valueOf(that.getState()));
//		int locationComparison = Integer.compare(this.getLocation(), that.getLocation());
//
//		if (stateComparison == locationComparison) comparison = stateComparison;
//		else if (stateComparison == 0) comparison = locationComparison;
//		else if (locationComparison == 0) comparison = stateComparison;
//
//		return comparison;
//	}


	/**
	 * Represents a given vertex textually, for printing to standard output.
	 *
	 * @return a String representation of the vertex.
	 */
	@Override
	public String toString() {
		return Maths.LANGLE.uni() + this.getState() + this.getLocation() + Maths.RANGLE.uni();
	}
}
