package io.github.ethankelly.graph;

import io.github.ethankelly.symbols.Maths;

/**
 * The {@code VertexState} class represents an instance of a vertex being in a particular state in a compartmental
 * epidemiological model. Each vertex object has a state (for instance, {@code 'S'} for "Susceptible") and a numerical
 * location in the graph we are interested in.
 */
public class Vertex implements Comparable<Vertex> {
	private final int location;
	private char state = ' ';

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
		this.location = location;
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

		if (this.state == ' ') return this.getLocation() == vertex.getLocation();
		else return this.getLocation() == vertex.getLocation() && this.getState() == vertex.getState();
	}

	@Override
	public int hashCode() {
		int result = getState();
		result = 31 * result + getLocation();
		return result;
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
		else return Maths.L_ANGLE.uni() + this.getState() + (this.getLocation() + 1) + Maths.R_ANGLE.uni();
	}

	public String plainToString() {
		if (this.getState() == ' ') return String.valueOf(this.getLocation());
		else return String.valueOf(this.getState()) + this.getLocation();
	}

	/**
	 * Compares this object with the specified object for order.  Returns a negative integer, zero, or a positive
	 * integer as this object is less than, equal to, or greater than the specified object.
	 *
	 * @param that the object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
	 * 		the specified object.
	 * @throws NullPointerException if the specified object is null
	 * @throws ClassCastException   if the specified object's type prevents it from being compared to this object.
	 */
	@Override
	public int compareTo(Vertex that) {
		int BEFORE = -1;
		int SAME = 0;
		int AFTER = 1;

		int locations = SAME;

		if (this.getLocation() < that.getLocation()) locations = BEFORE;
		else if (this.getState() > that.getLocation()) locations = AFTER;

		return locations;
	}
}