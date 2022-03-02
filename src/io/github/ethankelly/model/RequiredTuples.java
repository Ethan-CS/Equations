package io.github.ethankelly.model;

import io.github.ethankelly.graph.*;
import io.github.ethankelly.symbols.Maths;

import java.util.*;
import java.util.stream.IntStream;

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
        if (this.tuples == null || this.tuples.isEmpty()) {
            this.tuples = this.genTuples();
        }
        return this.tuples;
    }

    /**
     * Creates a list of required tuples by creating a tuple for each connected subgraph in the contact network with
     * states given by walks in the filter graphs (there is one tuple per connected subgraph per walk in the filter
     * graph).
     *
     * @return the list of tuples required to provide a full dynamic representation of the current system.
     */
    public List<Tuple> genTuples() {
        // Add all singles (always need these)
        List<Tuple> tuples = new ArrayList<>(this.findSingles());
        // Get all walks in the filter graph up to length of contact network
        Graph filter = modelParams.getFilterGraph();
        List<List<List<Character>>> charWalks = filter.getCharWalks(this.getGraph().getNumVertices());
        // Get all connected sub-graphs of contact network
        List<List<Vertex>> connectedSubGraphs = new PathFinder(this.graph).combSearch();
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
        Collections.sort(tuples);
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
        List<Character> statesToGenerateFor = new ArrayList<>(this.getModelParams().getStates());
        for (Character state : this.getModelParams().getStates()) {
            int i = this.getModelParams().getStates().indexOf(state);
            // If we can leave this state without any other vertices being involved,
            // Singles containing the state are not dynamically significant
            if (this.getModelParams().getToExit()[i] == 0) statesToGenerateFor.remove(state);
        }
        List<Vertex> vertexList = getGraph().getVertices();
        int[] vertices = new int[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) vertices[i] = vertexList.get(i).getLocation();


        // We need an equation for the probability of each vertex being in each state
        List<Tuple> verticesWeNeed = new ArrayList<>();
        for (char c : statesToGenerateFor) {
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

        StringBuilder sb = new StringBuilder();
        sb.append("Vertices,Path,Cycle,Clique");

        for (int i = 1; i <= 40; i++) {
            sb.append("\n").append(i).append(",");
            // Generate and print tuples for path on i vertices
            RequiredTuples path = new RequiredTuples(GraphGenerator.path(i), m, false);
            List<Tuple> pathTuples = path.genTuples();
            System.out.println("  PATH ON " + i + " VERTICES: " + pathTuples.size());
            sb.append(pathTuples.size()).append(",");

            // Generate and print tuples for cycle on i vertices
            RequiredTuples cycle = new RequiredTuples(GraphGenerator.cycle(i), m, false);
            List<Tuple> triangleTuples = cycle.genTuples();
            System.out.println(" CYCLE ON " + i + " VERTICES: " + triangleTuples.size());
            sb.append(triangleTuples.size());

            // I haven't had the patience to wait for number of tuples on clique on >7 vertices...
            if (i<8) {
                // Generate and print tuples for clique on i vertices
                RequiredTuples clique = new RequiredTuples(GraphGenerator.complete(i), m, false);
                List<Tuple> cliqueTuples = clique.genTuples();
                System.out.println("CLIQUE ON " + i + " VERTICES: " + cliqueTuples.size());
                sb.append(",").append(cliqueTuples.size());
            }
        }
        System.out.println(sb);
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
        StringBuilder sb = new StringBuilder();
        for (Tuple t : this.getTuples()) sb.append(t).append("\n");
        return String.valueOf(sb);
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

    /**
     * A tuple represents the probability of one or more vertices of the graph being in particular states.
     */
    public static class Tuple extends ArrayList<Vertex> implements Cloneable, Comparable<Tuple> {
        /** List of all state-vertex pairs that make up this tuple */
        private List<Vertex> vertices;

        /**
         * Constructor - creates a tuple containing only a single (specified) vertex.
         *
         * @param v a single vertex to add to a new Tuple instance.
         */
        public Tuple(Vertex v) {
            // Parameterised ArrayList construction because singletonList returns an immutable list
            this.vertices = new ArrayList<>(Collections.singletonList(v));
        }

        /**
         * Constructor - creates a tuple from a list of vertices.
         *
         * @param tuple the list of vertices from which to form a tuple.
         */
        public Tuple(List<Vertex> tuple) {
            tuple.sort(new Vertex.VertexComparator());
            this.vertices = tuple;
        }

        /**
         * Given some list of vertices (some tuple), this method checks whether the index locations of every element of the
         * tuple are all distinct. If even two of the vertices have the same location, it is not a valid tuple for our
         * purposes, and we will not have to consider the associated equation in the final system of equations that describes
         * our compartmental model.
         *
         * @return true if no index location is repeated in the vertices, false otherwise.
         */
        public boolean locationsAreDifferent() {
            // Only need to find two vertices in the list with same index location
            // to know we don't have a required tuple, returning false.
            for (Vertex v : getVertices()) {
                for (Vertex w : getVertices()) {
                    if ((v.getLocation() == w.getLocation()) && (getVertices().indexOf(v) != getVertices().indexOf(w))) {
                        return false;
                    }
                }
            }
            return true;
        }

        /**
         * @return a list of the vertices that compose the current tuple.
         */
        public List<Vertex> getVertices() {
            return vertices;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Tuple tuple = (Tuple) o;

            return getVertices() != null ? getVertices().equals(tuple.getVertices()) : tuple.getVertices() == null;
        }

        @Override
        public int hashCode() {
            return getVertices() != null ? getVertices().hashCode() : 0;
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            List<Vertex> vertices = this.getVertices();

            if (vertices != null && !vertices.isEmpty()) {
                s.append(Maths.L_ANGLE.uni()).append(vertices.get(0).plainToString());
                IntStream.range(1, vertices.size())
                        .mapToObj(vertices::get)
                        .forEach(v -> s.append(" ").append(v.plainToString()));
                s.append(Maths.R_ANGLE.uni());
            }

            return String.valueOf(s);
        }

        @SuppressWarnings("unused")
        public String plainToString(int howPlain) {
            StringBuilder s = new StringBuilder();
            List<Vertex> vertices = this.getVertices();
            String separator = "";
            if (howPlain == 1) separator = "_";
            String finalSeparator = separator;
            s.append(vertices.get(0).getState()).append(vertices.get(0).getLocation());
            IntStream.range(1, vertices.size())
                    .mapToObj(vertices::get)
                    .forEach(v -> s.append(finalSeparator).append(v.getState()).append(v.getLocation()));

            return String.valueOf(s);
        }

        @Override
        public boolean add(Vertex v) {
            Tuple copy = new Tuple(this.vertices);
            this.vertices.add(v);
            this.vertices.sort(new Vertex.VertexComparator());
            return !this.getVertices().equals(copy.getVertices());
        }

        /**
         * Compares this object with the specified object for order.  Returns a negative integer, zero, or a positive
         * integer as this object is less than, equal to, or greater than the specified object.
         *
         * @param o the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater
         * than the specified object.
         * @throws NullPointerException if the specified object is null
         * @throws ClassCastException   if the specified object's type prevents it from being compared to this object.
         */
        @Override
        public int compareTo(Tuple o) {
            // We want to sort in size order, so if o is bigger it should come later
            return Integer.compare(this.size(), o.size());
        }

        @Override
        public int size() {
            return this.getVertices().size();
        }

        @Override
        public boolean contains(Object o) {
            Vertex v;
            try {
                v = (Vertex) o;
            } catch (ClassCastException ignored) {
                return false;
            }
            return this.getVertices().contains(v);
        }

        @Override
        public Vertex get(int i) {
            return this.getVertices().get(i);
        }

        @Override
        public Tuple clone() {
            Tuple clone = (Tuple) super.clone();
            clone.vertices = new ArrayList<>();
            clone.vertices.addAll(this.getVertices());
            return clone;
        }
    }
}
