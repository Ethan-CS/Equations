package io.github.ethankelly.graph;

import io.github.ethankelly.symbols.Maths;

import java.util.Comparator;

/**
 * The {@code Vertex} class represents an instance of a vertex being in a particular state in a compartmental
 * epidemiological model. Each vertex object has a state (for instance, {@code 'S'} for "Susceptible") and a numerical
 * location in the graph we are interested in.
 */
public class Vertex implements Comparable<Vertex> {
	private final int location;
	private char state;

	public static class VertexComparator implements Comparator<Vertex> {

		/**
		 * Compares its two arguments for order.  Returns a negative integer,
		 * zero, or a positive integer as the first argument is less than, equal
		 * to, or greater than the second.<p>
		 * <p>
		 * The implementor must ensure that {@code sgn(compare(x, y)) ==
		 * -sgn(compare(y, x))} for all {@code x} and {@code y}.  (This
		 * implies that {@code compare(x, y)} must throw an exception if and only
		 * if {@code compare(y, x)} throws an exception.)<p>
		 * <p>
		 * The implementor must also ensure that the relation is transitive:
		 * {@code ((compare(x, y)>0) && (compare(y, z)>0))} implies
		 * {@code compare(x, z)>0}.<p>
		 * <p>
		 * Finally, the implementor must ensure that {@code compare(x, y)==0}
		 * implies that {@code sgn(compare(x, z))==sgn(compare(y, z))} for all
		 * {@code z}.<p>
		 * <p>
		 * It is generally the case, but <i>not</i> strictly required that
		 * {@code (compare(x, y)==0) == (x.equals(y))}.  Generally speaking,
		 * any comparator that violates this condition should clearly indicate
		 * this fact.  The recommended language is "Note: this comparator
		 * imposes orderings that are inconsistent with equals."<p>
		 * <p>
		 * In the foregoing description, the notation
		 * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
		 * <i>signum</i> function, which is defined to return one of {@code -1},
		 * {@code 0}, or {@code 1} according to whether the value of
		 * <i>expression</i> is negative, zero, or positive, respectively.
		 *
		 * @param o1 the first object to be compared.
		 * @param o2 the second object to be compared.
		 * @return a negative integer, zero, or a positive integer as the
		 * first argument is less than, equal to, or greater than the
		 * second.
		 * @throws NullPointerException if an argument is null and this
		 *                              comparator does not permit null arguments
		 * @throws ClassCastException   if the arguments' types prevent them from
		 *                              being compared by this comparator.
		 */
		@Override
		public int compare(Vertex o1, Vertex o2) {
			int intComparison = Integer.compare(o1.getLocation(), o2.getLocation());
			return (intComparison == 0 ? Character.compare(o1.getState(),o2.getState()) : intComparison);
		}

		/**
		 * Returns a comparator that imposes the reverse ordering of this
		 * comparator.
		 *
		 * @return a comparator that imposes the reverse ordering of this
		 * comparator.
		 * @since 1.8
		 */
		@Override
		public Comparator<Vertex> reversed() {
			return Comparator.super.reversed();
		}
	}

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
		this.state = ' ';
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
		if (this.getState() == ' ') return String.valueOf(this.getLocation()+1);
		else return String.valueOf(this.getState()) + (this.getLocation()+1);
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