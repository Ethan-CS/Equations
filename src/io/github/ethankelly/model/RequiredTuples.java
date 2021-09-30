package io.github.ethankelly.model;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;

import java.util.*;

/**
 * The {@code Tuple} class is used to determine and represent the number of differential equations required to fully
 * describe a given compartmental epidemiological model on a particular graph. The class contains methods that determine
 * the total number of equations that would be required to exactly describe the dynamics of a compartmental model with
 * some number of states (for instance, susceptible/infected/recovered - SIR) and a given graph (with a number of
 * vertices connected in some way by a number of edges). The graphs may be randomly generated or hard-coded and the
 * states can be specified when running the model.
 */
public class RequiredTuples {
	private final List<Tuple> tuples; // The list of tuples required to describe a model on the specified graph
	private final Graph graph; // The graph representing underpinning the compartmental model
	private final char[] states; // The compartmental model states, e.g. SIR
	private final boolean closures; // Whether we are considering closures (may need to consider unusual tuples)

	/**
	 * Class constructor - assigns a graph, an array of states and a boolean representing whether we may need to
	 * consider to a new Tuple object.
	 *
	 * @param graph    the graph of which we are interested in equations.
	 * @param states   the states that a vertex can be in (e.g. SIR).
	 * @param closures whether to include closures in the generated tuples.
	 */
	public RequiredTuples(Graph graph, char[] states, boolean closures) {
		this.graph = graph;
		this.states = states;
		this.tuples = this.generateTuples(closures);
		this.closures = closures;
	}

	/**
	 * Unit testing.
	 *
	 * @param args command-line arguments (ignored).
	 */
	public static void main(String[] args) {
//		RequiredTuples t1 = new RequiredTuples(GraphGenerator.getTriangle(), new char[] {'S', 'I', 'R'}, false);
//		System.out.println(t1.findSingles());
//		System.out.println(t1.getTuples() + "\nSize: " + t1.getTuples().size());
//
//		RequiredTuples t2 = new RequiredTuples(GraphGenerator.getTriangle(), new char[] {'S', 'I', 'R'}, true);
//		System.out.println(t2.findSingles());
//		System.out.println(t2.getTuples() + "\nSize: " + t2.getTuples().size());

		RequiredTuples complete5 = new RequiredTuples(GraphGenerator.complete(5), new char[]{'S', 'I', 'R'}, false);
		RequiredTuples cycle5 = new RequiredTuples(GraphGenerator.cycle(5), new char[]{'S', 'I', 'R'}, false);
		RequiredTuples complete3 = new RequiredTuples(GraphGenerator.complete(3), new char[]{'S', 'I', 'R'}, false);
		RequiredTuples cycle3 = new RequiredTuples(GraphGenerator.cycle(3), new char[]{'S', 'I', 'R'}, false);
		RequiredTuples complete4 = new RequiredTuples(GraphGenerator.complete(4), new char[]{'S', 'I', 'R'}, false);
		RequiredTuples cycle4 = new RequiredTuples(GraphGenerator.cycle(4), new char[]{'S', 'I', 'R'}, false);

		System.out.println("CYCLE 3");
//		System.out.println(cycle3.findSingles());
		System.out.println(
//				cycle3.getTuples() +
				"Size: " + cycle3.getTuples().size());

		System.out.println("\nCOMPLETE 3");
//		System.out.println(complete3.findSingles());
		System.out.println(
//				complete3.getTuples() +
				"Size: " +  complete3.getTuples().size());

		System.out.println("\nCYCLE 4");
//		System.out.println(cycle4.findSingles());
		System.out.println(
				cycle4.getTuples() +
				"Size: " + cycle4.getTuples().size());

		System.out.println("\nCOMPLETE 4");
//		System.out.println(complete4.findSingles());
		System.out.println(
//				complete4.getTuples() +
				"Size: " +  complete4.getTuples().size());

		System.out.println("\nCYCLE 5");
//		System.out.println(cycle5.findSingles());
		System.out.println(
				cycle5.getTuples() +
				"Size: " + cycle5.getTuples().size());

		System.out.println("\nCOMPLETE 5");
//		System.out.println(complete5.findSingles());
		System.out.println(
//				complete5.getTuples() +
				"Size: " +  complete5.getTuples().size());

	}

	/**
	 * @return the tuples required for the specified model, generated at object construction and updated whenever
	 * changes are made to the model that would constitute a change to the required tuples.
	 */
	public List<Tuple> getTuples() {
		return this.tuples;
	}

	/**
	 * Given the equations necessary to exactly describe an SIR epidemic on some graph, returns the numbers of n-tuple
	 * equations that are required.
	 *
	 * @param equations the equations to find the numbers of each length.
	 * @return An array with the number of equations of each size required to exactly describe the SIR system.
	 */
	public int[] findNumbers(List<Tuple> equations) {
		// The array needs as many elements as there are different sizes in the equations list,
		// So initialise to (1 less than) the size of the largest (final) sub-list.
		int[] sizes = new int[equations.get(equations.size() - 1).getVertices().size()];
		// For each sub-list we come across, increase the element at the array position corresponding
		// to its size.
		equations.forEach(equation -> sizes[equation.getVertices().size() - 1]++);

		return sizes;
	}

	/**
	 * Returns true if the current and given objects are equal. This is verified by checking that both sets of required
	 * tuples agree on requiring tuples nor not, have the same required tuples, possess the same underlying graph and
	 * that the underlying models have the same epidemiological states.
	 *
	 * @param o the object to check equality to.
	 * @return true if the current and provided RequiredTuples instances are equal, false otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RequiredTuples tuple = (RequiredTuples) o;
		return closures == tuple.closures &&
		       Objects.equals(getTuples(), tuple.getTuples()) &&
		       Objects.equals(getGraph(), tuple.getGraph()) &&
		       Arrays.equals(getStates(), tuple.getStates());
	}

	/**
	 * @return a unique hashcode for the current instance.
	 */
	@Override
	public int hashCode() {
		int result = Objects.hash(getTuples(), getGraph(), closures);
		result = 31 * result + Arrays.hashCode(getStates());
		return result;
	}

	/**
	 * Using the states required in the model and the number of vertices in the graph, this method determines the full
	 * list of single termed equations that are involved in describing the model exactly.
	 *
	 * @return the number of single-probability differential equations required by the model.
	 */
	public List<Tuple> findSingles() {
		// Get states, graph and the vertices in the associated graph for the model
		char[] states = this.getStates();
		Arrays.sort(states);

		if (Arrays.equals(states, States.sir.states())) states = States.si.states();
		else if (Arrays.equals(states, States.sirp.states())) states = States.sip.states();

		Graph graph = this.getGraph();
		int numVertices = graph.getNumVertices();
		int[] vertices = new int[numVertices];
		Arrays.setAll(vertices, i -> i);

		// We need an equation for the probability of each vertex being in each state
		List<Tuple> verticesWeNeed = new ArrayList<>();
		for (char c : states) {
			for (int v : vertices) {
				verticesWeNeed.add(new Tuple(new Vertex(c, v)));
			}
		}
		Collections.sort(verticesWeNeed);
		return verticesWeNeed;
	}

	// Recursive helper method to generate the total equations we need
	private void generateTuples(List<Tuple> items,
	                            List<Vertex> selected,
	                            int index,
	                            List<Tuple> result) {
		if (index >= items.size()) {
			result.add(new Tuple(new ArrayList<>(selected)));
		} else {
			generateTuples(items, selected, index + 1, result);
			selected.add(items.get(index).getVertices().get(0)); // get(0) since items is composed of singleton lists
			generateTuples(items, selected, index + 1, result);
			selected.remove(selected.size() - 1);
		}
	}

	/**
	 * Using the graph we are determining equations for and the states that a vertex can take, this method finds all the
	 * equations that we need to use to exactly express the system dynamics of the model in question.
	 *
	 * @return a list of each n-tuple we are required to express to exactly detail the model system dynamics.
	 */
	public List<Tuple> generateTuples(boolean closures) {
		List<Tuple> items = this.findSingles();
		List<Tuple> result = new ArrayList<>();
		List<Vertex> selected = new ArrayList<>();
		generateTuples(items, selected, 0, result);

		// The helper method gives us all combinations, so remove the ones that don't
		// constitute valid (necessary) tuples (and sort by length of sub-arrays).
		result.removeIf(row -> !row.isValidTuple(this.getGraph(), closures));
		this.findSingles().stream().filter(single -> !result.contains(single)).forEach(result::add);
		Collections.sort(result);

		return result;
	}

	/**
	 * @return the graph associated with the model in question.
	 */
	public Graph getGraph() {
		return this.graph;
	}

	/**
	 * Each vertex in the graph model can be in any of the given states, which might be 'Susceptible,' 'Infected' and
	 * 'Recovered' (standard SIR model) for instance.
	 *
	 * @return the states defined for the model in question.
	 */
	public char[] getStates() {
		return this.states;
	}

	/**
	 * @return a string representation of the current set of required tuples, which implicitly utilises the analogous
	 * method in the {@code Tuple} class.
	 */
	@Override
	public String toString() {
		return String.valueOf(tuples);
	}

	/**
	 * @return the number of tuples in the current object of required tuples.
	 */
	public int size() {
		return this.tuples.size();
	}

	/**
	 * Returns the required tuple at the specified index of the required tuples.
	 *
	 * @param i the index of the tuple instance to be returned from the list of required tuples.
	 * @return the tuple from the list of required tuples at the specified index.
	 */
	public Tuple get(int i) {
		return this.getTuples().get(i);
	}
}
