package io.github.ethankelly.model;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.GraphUtils;
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
    /** Contact network */
    private final Graph graph;
    /** Information about compartmental model */
    private final ModelParams modelParams;
    /** Whether we are considering closures (may need to consider unusual tuples) */
    private final boolean closures;
    /** Tuples required to describe a model on the specified graph */
    private List<Tuple> tuples;

    /**
     * Class constructor - assigns a graph, an array of states and a boolean representing whether we may need to
     * consider to a new Tuple object.
     *
     * @param graph       the graph of which we are interested in equations.
     * @param modelParams the parameters of the model, including which states each vertex can be.
     * @param closures    whether to include closures in the generated tuples.
     */
    public RequiredTuples(Graph graph, ModelParams modelParams, boolean closures) {
        this.graph = graph;
        this.modelParams = modelParams;
        this.tuples = getTuples();
        this.closures = closures;
    }

    /**
     * @return the tuples required for the specified model, generated at object construction and updated whenever
     * changes are made to the model that would constitute a change to the required tuples.
     */
    public List<Tuple> getTuples() {
        if (this.tuples == null || this.tuples.isEmpty()) genTuples();
        return this.tuples;
    }

    /**
     * Creates a list of required tuples by creating a tuple for each connected subgraph in the contact network with
     * states given by walks in the filter graphs (there is one tuple per connected subgraph per walk in the filter
     * graph).
     *
     * @return the list of tuples required to provide a full dyanmic representation of the current system.
     */
    private List<Tuple> genTuples() {
        // Add all singles (always need these)
        List<Tuple> tuples = new ArrayList<>(this.findSingles());
        // Get all walks in the filter graph up to length of contact network
        Graph filter = modelParams.getFilterGraph();
        List<List<List<Character>>> charWalks = filter.getCharWalks(this.getGraph().getNumVertices());
        // Get all connected sub-graphs of contact network
        List<List<Vertex>> connectedSubGraphs = getConnectedSubGraphs();
        // Create a tuple for each connected sub-graph with states from list of walks of that length
        List<List<Vertex>> tuplesToMake = new ArrayList<>();
        for (List<Vertex> subGraph : connectedSubGraphs) { // Each connected sub-graph
            for (List<Character> walk : charWalks.get(subGraph.size() - 1)) { // Each walk in filter graph
                List<Vertex> tupleToMake = new ArrayList<>();
                for (int i = 0; i < subGraph.size(); i++) { // Combine sub-graph and walk as a tuple
                    tupleToMake.add(new Vertex(walk.get(i), subGraph.get(i).getLocation()));
                }
                tuplesToMake.add(tupleToMake); // Add new tuple to list of tuples to make
            }
        }
        // Create each of the tuples in the list of tuples to make and add to total list of tuples
        for (List<Vertex> t : tuplesToMake) {
            Tuple tuple = new Tuple(t);
            if (!tuples.contains(tuple)) tuples.add(tuple);
        }
        // Assign to tuples field and return
        this.tuples = tuples;
        return tuples;
    }

    /**
     * Using the states required in the model and the number of vertices in the graph, this method determines the full
     * list of single termed equations that are involved in describing the model exactly.
     *
     * @return the number of single-probability tuples required by the model.
     */
    public List<Tuple> findSingles() {
        List<Character> statesToGenerateEqnsFor = new ArrayList<>(this.getModelParams().getStates());
        for (Character state : this.getModelParams().getStates()) {
            int i = this.getModelParams().getStates().indexOf(state);
            // If we can leave this state without any other vertices being involved,
            // Singles containing the state are not dynamically significant
            if (this.getModelParams().getToExit()[i] == 0) statesToGenerateEqnsFor.remove(state);
        }
        List<Vertex> vertexList = getGraph().getVertices();
        int[] vertices = new int[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) vertices[i] = vertexList.get(i).getLocation();


        // We need an equation for the probability of each vertex being in each state
        List<Tuple> verticesWeNeed = new ArrayList<>();
        for (char c : statesToGenerateEqnsFor) {
            for (int v : vertices) {
                verticesWeNeed.add(new Tuple(new Vertex(c, v)));
            }
        }
        Collections.sort(verticesWeNeed);
        return verticesWeNeed;
    }

    /**
     * @return the graph associated with the model in question.
     */
    public Graph getGraph() {
        return this.graph;
    }

	/**
	 * @return all connected sub-graphs of the current graph.
 	 */
    public List<List<Vertex>> getConnectedSubGraphs() {
        // Generate power set of vertices in the graph
        Set<List<Vertex>> powerSet = GraphUtils.powerSet(getGraph().getVertices());
        // Remove any sets of vertices not constituting connected sub-graphs
        List<List<Vertex>> connectedSubGraphs = new ArrayList<>();
        for (List<Vertex> vertices : powerSet) {
            if (vertices.size() > 0) {
                Graph candidate = getGraph().makeSubGraph(vertices);
                if (candidate.isConnected()) {
                    // Need to check this so we only use valid permutations with walks
                    boolean isPath = true;
                    for (int i = 0; i < vertices.size() - 1; i++) {
                        if (!this.getGraph().hasDirectedEdge(vertices.get(i), vertices.get(i + 1))) {
                            isPath = false;
                            break;
                        }
                    }
                    if (isPath) connectedSubGraphs.add(vertices);
                }
            }
        }
        return connectedSubGraphs;
    }

    /**
     * Each vertex in the graph model can be in any of the given states, which might be 'Susceptible,' 'Infected' and
     * 'Recovered' (standard SIR model) for instance. The model parameters are stored in a custom data structure,
     * defined in the {@code Model} class.
     *
     * @return the model parameters defined for the model in question.
     */
    public ModelParams getModelParams() {
        return this.modelParams;
    }

	/**
	 * Main method for unit testing.
	 *
	 * @param args command-line args, ignored.
	 */
	public static void main(String[] args) {
        // SIR Model parameters
        ModelParams m = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        m.addTransition('S', 'I', 0.6);
        m.addTransition('I', 'R', 0.1);

        // Path and cycle on 3 vertices
        RequiredTuples P3 = new RequiredTuples(GraphGenerator.path(3), m, false);
        RequiredTuples C3 = new RequiredTuples(GraphGenerator.cycle(3), m, false);

        // Generate and print tuples for path on 3 vertices
        List<Tuple> pathTuples = P3.genTuples();
        for (Tuple t : pathTuples) System.out.println(t);
        System.out.println(pathTuples.size());

        // Generate and print tuples for cycle on 3 vertices
        List<Tuple> triangleTuples = C3.genTuples();
        for (Tuple t : triangleTuples) System.out.println(t);
        System.out.println(triangleTuples.size());
    }

    /**
     * Given the equations necessary to exactly describe an SIR epidemic on some graph, returns the numbers of n-tuple
     * equations that are required.
     *
     * @param equations the equations to find the numbers of each length.
     * @return An array with the number of equations of each size required to exactly describe the SIR system.
     */
    @SuppressWarnings("unused")
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
     * @return the number of tuples in the current object of required tuples.
     */
    public int size() {
        return this.getTuples().size();
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

    /**
     * @return a unique hashcode for the current instance.
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(getTuples(), getGraph(), closures);
        result = 31 * result + (this.getModelParams().getStates().hashCode());
        return result;
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
                this.getModelParams().getStates().containsAll(tuple.getModelParams().getStates());
    }

    /**
     * @return a string representation of the current set of required tuples, which implicitly utilises the analogous
     * method in the {@code Tuple} class.
     */
    @Override
    public String toString() {
        return String.valueOf(getTuples());
    }

    /**
     * Returns {@code true} if this list contains the specified element.
     * More formally, returns {@code true} if and only if this list contains
     * at least one element {@code e} such that
     * {@code Objects.equals(o, e)}.
     *
     * @param o element whose presence in this list is to be tested
     * @return {@code true} if this list contains the specified element
     * @throws ClassCastException   if the type of the specified element
     *                              is incompatible with this list
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *                              list does not permit null elements
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     */
	public boolean contains(Object o) {
        Tuple t;
        try {
            t = (Tuple) o;
        } catch (ClassCastException ignored) {
            return false;
        }
        return this.getTuples().contains(t);
    }
}
