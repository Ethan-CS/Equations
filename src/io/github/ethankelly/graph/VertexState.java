package io.github.ethankelly.graph;

import io.github.ethankelly.symbols.Maths;

import java.util.List;
import java.util.stream.IntStream;

/**
 * The {@code VertexState} class represents an instance of a vertex being in a particular state in a compartmental
 * epidemiological model. Each vertex object has a state (for instance, {@code 'S'} for "Susceptible") and a numerical
 * location in the graph we are interested in.
 */
public class VertexState extends Vertex  {
	private final char state;
	private final int location;

	/**
	 * Class constructor, assigning a state and location to a vertex instance object.
	 *
	 * @param state    the state for which we are interested in studying the probability of the vertex being in.
	 * @param location the numerical index that represents the location of the vertex in the associated graph.
	 */
	public VertexState(char state, int location) {
		super(location);
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
	public static boolean areStatesDifferent(List<VertexState> toCheck, boolean reqClosures) {
		boolean statesDifferent = false;
		// If there's only one vertex in the list, by definition we require it
		// in the system of equations so we ensure this method returns true.
		if (toCheck.size() == 1) statesDifferent = true;
        else {
			// We only need to find two different states to know that the states are
			// at least not all the same, returning true.
			for (VertexState v : toCheck) {
				for (VertexState w : toCheck) {
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
	public static boolean areLocationsDifferent(List<VertexState> vertices) {
		boolean locationsDifferent = true;
		// Only need to find two vertices in the list with same index location
		// to know we don't have a required tuple, returning false.
		for (VertexState v : vertices) {
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
	public static boolean areAllConnected(List<VertexState> toCheck, Graph g) {
		// If there's only one vertex in the list, required in the system of equations - ensure this returns true.
		// If more than one vertex, return whether they all have some path between them.
		return toCheck.size() == 1 || IntStream.range(0, toCheck.size() - 1).allMatch(
				i -> g.hasEdge(new Vertex(toCheck.get(i).getLocation()),
						new Vertex(toCheck.get(i + 1).getLocation()))
		);
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
