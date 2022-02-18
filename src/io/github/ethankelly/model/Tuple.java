package io.github.ethankelly.model;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.Vertex;
import io.github.ethankelly.symbols.Maths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * A tuple represents the probability of one or more vertices of the graph being in particular states.
 */
public class Tuple extends ArrayList<Vertex> implements Cloneable, Comparable<Tuple> {
    /** List of all state-vertex pairs that make up this tuple */
    private final List<Vertex> vertices;
    /** Length of the current tuple. */
    public int length;

    /** Constructor - creates a tuple containing only a single (specified) vertex. */
    public Tuple(Vertex v) {
        // Parameterised ArrayList construction because singletonList returns an immutable list
        this.vertices = new ArrayList<>(Collections.singletonList(v));
    }

    /** Constructor - creates a tuple from a list of vertices. */
    public Tuple(List<Vertex> tuple) {
        Collections.sort(tuple);
        this.vertices = tuple;
        this.length = vertices.size();
    }

    /**
     * Given a potential tuple, checks it is valid using the following criteria:
     * <ol>
     *      <li> Each vertex in the tuple is distinct;</li>
     * 	    <li> The vertices in the tuple constitute a connected subgraph; and</li>
     * 	    <li> The states in the tuple constitute a walk in the filter graph.</li>
     * </ol>
     *
     * @return true if the tuple is essential to expressing the system dynamics, false otherwise.
     */
    public boolean isValidTuple(ModelParams modelParams, Graph g, boolean closures) {
        // TODO update this based on new validity criteria definition (i.e. filter graph definition)
        List<Character> states = new ArrayList<>();
        for (Vertex v : this.getVertices()) {
            System.out.println(v);
            if (!states.contains(v.getState())) {
                states.add(v.getState());
            }
        }
        if (size() == 1) return true;
        else return locationsAreDifferent() &&              // 1. each vertex is distinct
                areAllConnected(g) &&                       // 2. vertices constitute a connected subgraph
                modelParams.validStates(states, closures);  // 3. States form a walk in the filter graph

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
        Collections.sort(this.vertices);
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
        return Integer.compare(o.size(), this.size());
    }

    @Override
    public int size() {
        return this.getVertices().size();
    }

    public boolean contains(Vertex v) {
        return this.getVertices().contains(v);
    }

    @Override
    public Vertex get(int i) {
        return this.getVertices().get(i);
    }

    @Override
    public Tuple clone() {
        Tuple clone = (Tuple) super.clone();
        // TODO: copy mutable state here, so the clone can't change the internals of the original
        return clone;
    }

    /**
     * Given some list of vertices (tuple), this method verifies that all vertices in the graph given are in fact
     * connected. That is, we are only interested in a tuple of vertices that constitute some manner of path (cycle or
     * such like) - if not, then we do not have to consider the associated equation in the final system of equations
     * that describes our compartmental model, and we can discount the given tuple.
     *
     * @param graph the graph on which we are checking the vertices of the current tuple are connected.
     * @return true if the tuple forms a path, false otherwise.
     */
    public boolean areAllConnected(Graph graph) {
        System.out.println("CHECKING: " + this + ", GRAPH VERTICES: " + graph.getVertices());
        if ((!graph.getVertices().containsAll(this.getVertices()))) return false;
        List<Vertex> list = new ArrayList<>(this.getVertices());
        return graph.areAllConnected(list);
    }
}
