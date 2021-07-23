package io.github.ethankelly;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
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
public class Tuples {
    private List<Tuple> tuples;
    private final Graph graph;
    private final char[] states;
    private final boolean closures;

    /**
     * Class constructor - assigns a graph and some array of states to a new Tuple object.
     * @param graph  the graph of which we are interested in equations.
     * @param states the states that a vertex can be in (e.g. SIR).
     * @param closures whether to include closures in the generated tuples.
     */
    public Tuples(Graph graph, char[] states, boolean closures) {
        this.graph = graph;
        this.states = states;
        this.tuples = new ArrayList<>();
        this.closures = closures;
    }


    public List<Tuple> getTuples() {
        if (this.tuples.isEmpty()) {
            this.tuples = this.generateTuples(this.closures);
        }
        return this.tuples;
    }

    public static void main(String[] args) {
        Tuples t1 = new Tuples(GraphGenerator.getTriangle(), new char[]{'S','I','R'}, false);
        System.out.println(t1.findSingles());
        System.out.println(t1.getTuples() + "\nSize: " + t1.getTuples().size());

        Tuples t2 = new Tuples(GraphGenerator.getTriangle(), new char[]{'S','I','R'}, true);
        System.out.println(t2.findSingles());
        System.out.println(t2.getTuples()+ "\nSize: " + t2.getTuples().size());
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
        int[] sizes = new int[equations.get(equations.size() - 1).getSingles().size()];
        // For each sub-list we come across, increase the element at the array position corresponding
        // to its size.
        equations.forEach(equation -> sizes[equation.getSingles().size() - 1]++);

        return sizes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuples tuple = (Tuples) o;
        return closures == tuple.closures &&
                Objects.equals(getTuples(), tuple.getTuples()) &&
                Objects.equals(getGraph(), tuple.getGraph()) &&
                Arrays.equals(getStates(), tuple.getStates());
    }

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
            selected.add(items.get(index).getSingles().get(0)); // get(0) since items is composed of singleton lists
            generateTuples(items, selected, index + 1, result);
            selected.remove(selected.size() - 1);
        }
    }

    /**
     * Using the graph we are determining equations for and the states that a vertex can take, this method finds all of
     * the equations that we need to use to exactly express the system dynamics of the model in question.
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

    @Override
    public String toString() {
        return String.valueOf(tuples);
    }

    /**
     * A tuple represents the probability of one or more vertices of the graph being in particular states.
     */
    public static class Tuple extends ArrayList<Vertex> implements Cloneable, Comparable<Tuple> {

        private final List<Vertex> singles; // List of all state-vertex pairs that make up this tuple

        // For when we just want to add a single vertex to the tuple
        public Tuple(Vertex v) {
            this.singles = Collections.singletonList(v);
        }

        // When we have a list of vertices that we want to form a tuple from
        public Tuple(List<Vertex> tuple) {
            Collections.sort(tuple);
            this.singles = tuple;
        }

        /**
         * Given a list of vertices (some tuple), this method checks whether all of the states are the same. If they are the
         * same, we do not have to consider the associated equation of the tuple in the final system of equations that
         * describes our compartmental model.
         *
         * @return true if at least one vertex state is different to that of the others, false if they are all the same.
         */
        public boolean areStatesDifferent(boolean reqClosures) {
            boolean statesDifferent = false;
            // If there's only one vertex in the list, by definition we require it
            // in the system of equations so we ensure this method returns true.
            if (size() == 1) statesDifferent = true;
            else {
                // We only need to find two different states to know that the states are
                // at least not all the same, returning true.
                for (Vertex v : getSingles()) {
                    for (Vertex w : getSingles()) {
                        if (v.getState() != w.getState()) {
                            statesDifferent = true;
                            break;
                        } else if (reqClosures) {
                            if ((v.getState() == 'S') || (w.getState() == 'S')) {
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
         * @return true if no index location is repeated in the vertices, false otherwise.
         */
        public boolean areLocationsDifferent() {
            boolean locationsDifferent = true;
            // Only need to find two vertices in the list with same index location
            // to know we don't have a required tuple, returning false.
            for (Vertex v : getSingles()) {
                if (getSingles().stream().anyMatch(w -> v.getLocation() == w.getLocation() && v != w)) {
                    locationsDifferent = false;
                    break;
                }
            }
            return locationsDifferent;
        }

        /**
         * Given a potential tuple, this method checks that the states of each probability are not all the same, that each
         * probability corresponds to a different vertex and that all of the vertices involved are in fact connected. If
         * these three conditions are met, then it is a tuple that we are required to express in the total set of equations
         * describing the system dynamics.
         *
         * @return true if the tuple is essential to expressing the system dynamics, false otherwise.
         */
        boolean isValidTuple(Graph g, boolean closures) {
            return this.areStatesDifferent(closures)
                    && this.areLocationsDifferent()
                    && g.areAllConnected(this);
        }

        /**
         * @return a list of the vertices that compose the current tuple.
         */
        public List<Vertex> getSingles() {
            return singles;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Tuple tuple = (Tuple) o;

            return getSingles() != null ? getSingles().equals(tuple.getSingles()) : tuple.getSingles() == null;
        }

        @Override
        public int hashCode() {
            return getSingles() != null ? getSingles().hashCode() : 0;
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            List<Vertex> vertices = this.getSingles();

            s.append(Maths.L_ANGLE.uni()).append(vertices.get(0).getState()).append(vertices.get(0).getLocation());
            IntStream.range(1, vertices.size())
                    .mapToObj(vertices::get)
                    .forEach(v -> s.append("_").append(v.getState()).append(v.getLocation()));
            s.append(Maths.R_ANGLE.uni());

            return String.valueOf(s);
        }

        public String plainToString(int howPlain) {
            StringBuilder s = new StringBuilder();
            List<Vertex> vertices = this.getSingles();
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
            Tuple copy = new Tuple(this.singles);
            this.singles.add(v);
            Collections.sort(this.singles);
            return !this.getSingles().equals(copy.getSingles());
        }

        public static void main(String[] args) {
            Tuple t = new Tuple(Arrays.asList(new Vertex('S', 0), new Vertex('S', 1), new Vertex('I', 2)));
            System.out.println(t);
            System.out.println(t.plainToString(0));
            System.out.println(t.plainToString(1));
        }

        /**
         * Compares this object with the specified object for order.  Returns a negative integer, zero, or a positive
         * integer as this object is less than, equal to, or greater than the specified object.
         *
         * <p>The implementor must ensure
         * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))} for all {@code x} and {@code y}.  (This implies that
         * {@code x.compareTo(y)} must throw an exception iff {@code y.compareTo(x)} throws an exception.)
         *
         * <p>The implementor must also ensure that the relation is transitive:
         * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies {@code x.compareTo(z) > 0}.
         *
         * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
         * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for all {@code z}.
         *
         * <p>It is strongly recommended, but <i>not</i> strictly required that
         * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any class that implements the {@code
         * Comparable} interface and violates this condition should clearly indicate this fact.  The recommended
         * language is "Note: this class has a natural ordering that is inconsistent with equals."
         *
         * <p>In the foregoing description, the notation
         * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
         * <i>signum</i> function, which is defined to return one of {@code -1},
         * {@code 0}, or {@code 1} according to whether the value of
         * <i>expression</i> is negative, zero, or positive, respectively.
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
            return Integer.compare(o.size(), this.size());
        }
    }
}
