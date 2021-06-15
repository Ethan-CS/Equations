package io.github.ethankelly.graph;

import io.github.ethankelly.symbols.Maths;

import java.util.HashMap;
import java.util.List;

/**
 * The {@code VertexState} class represents an instance of a vertex being in a particular state in a compartmental
 * epidemiological model. Each vertex object has a state (for instance, {@code 'S'} for "Susceptible") and a numerical
 * location in the graph we are interested in.
 */
public class Vertex implements Comparable<Vertex> {
	private final char state;
	private final int location;

	/**
	 * Class constructor, assigning a state and location to a vertex instance object.
	 *
	 * @param state    the state for which we are interested in studying the probability of the vertex being in.
	 * @param location the numerical index that represents the location of the vertex in the associated graph.
	 */
	public Vertex(char state, int location) {
		this.state = state;
		this.location = location;
	}

	public Vertex(int location) {
		this.state = ' ';
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
	 * @return the state of the vertex.
	 */
	public char getState() {
		return this.state;
	}

	/**
	 * @return the index location of the vertex.
	 */
	public int getLocation() {
		return this.location;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Vertex vertex = (Vertex) o;

		return this.getLocation() == vertex.getLocation() && this.getState() == vertex.getState();
	}

	/**
	 * Returns a hash code value for the object. This method is supported for the benefit of hash tables such as those
	 * provided by {@link HashMap}.
	 *
	 * @return a hash code value for this object.
	 * @implSpec As far as is reasonably practical, the {@code hashCode} method defined by class {@code Object} returns
	 * distinct integers for distinct objects.
	 * @see Object#equals(Object)
	 * @see System#identityHashCode
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * Creates and returns a copy of this object.
	 *
	 * @return a clone of this instance.
	 * @see Cloneable
	 */
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return new Vertex(this.getState(), this.getLocation());
		}
	}

	/**
	 * Represents a given vertex textually, for printing to standard output.
	 *
	 * @return a String representation of the vertex.
	 */
	@Override
	public String toString() {
		if (this.getState() == ' ') return String.valueOf(this.getLocation());
		else return Maths.LANGLE.uni() + this.getState() + this.getLocation() + Maths.RANGLE.uni();
	}

	/**
	 * Compares this object with the specified object for order.  Returns a negative integer, zero, or a positive
	 * integer as this object is less than, equal to, or greater than the specified object.
	 *
	 * @param that the object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
	 * the specified object.
	 * @throws NullPointerException if the specified object is null
	 * @throws ClassCastException   if the specified object's type prevents it from being compared to this object.
	 */
	@Override
	public int compareTo(Vertex that) {
		int BEFORE = -1;
		int SAME = 0;
		int AFTER = 1;

		int states = SAME;
		int locations = SAME;

		if (this.getState() < that.getState()) states = BEFORE;
		else if (this.getState() > that.getState()) states = AFTER;

		if (this.getLocation() < that.getLocation()) locations = BEFORE;
		else if (this.getState() > that.getLocation()) locations = AFTER;

		if (states == SAME) return locations;
		else return states;
	}
}
