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
    private final List<Vertex> vertices; // List of all state-vertex pairs that make up this tuple
    public int length;

    // For when we just want to add a single vertex to the tuple
    public Tuple(Vertex v) {
        this.vertices = Collections.singletonList(v);
    }

    // When we have a list of vertices that we want to form a tuple from
    public Tuple(List<Vertex> tuple) {
        Collections.sort(tuple);
        this.vertices = tuple;
        this.length = vertices.size();
    }

    public static void main(String[] args) {

    }

    /**
     * Given a potential tuple, this method checks the tuple is valid using the following criteria:
     * <ol>
     *      <li> Each vertex in the tuple is distinct,</li>
     * 	    <li> The vertices in the tuple constitute a connected subgraph,</li>
     * 	    <li> The tuple contains two or more states and </li>
     * 	    <li> The states in the tuple constitute a connected subgraph of the transition graph.</li>
     * </ol>
     *
     * @return true if the tuple is essential to expressing the system dynamics, false otherwise.
     */
    boolean isValidTuple(Model modelParams, Graph g, boolean closures) {
        List<Character> states = new ArrayList<>();
        this.getVertices().stream().filter(v -> !states.contains(v.getState())).forEach(v -> states.add(v.getState()));
        if (size() == 1) return true;
        else return areLocationsDifferent() &&              // 1. each vertex is distinct
                g.areAllConnected(this) &&          // 2. vertices constitute a connected subgraph
                areStatesDifferent(modelParams, closures) && // 3. tuple contains two or more unique states
                modelParams.validStates(states);             // 4. States form a complete subgraph of transition graph

    }

    /**
     * Given a list of vertices (some tuple), this method checks whether all the states are the same. If they are the
     * same, we do not have to consider the associated equation of the tuple in the final system of equations that
     * describes our compartmental model.
     *
     * @return true if at least one vertex state is different to that of the others, false if they are all the same.
     */
    public boolean areStatesDifferent(Model modelParams, boolean reqClosures) {
        boolean statesDifferent = false;
        // If there's only one vertex in the list, by definition we require it
        // in the system of equations, so we ensure this method returns true.
        if (size() == 1) statesDifferent = true;
        else {
            // We only need to find two different states to know that the states are
            // at least not all the same, returning true.
            for (Vertex v : getVertices()) {
                for (Vertex w : getVertices()) {
                    char vState = v.getState(), wState = w.getState();
                    if (vState != wState && modelParams.getTransitionMatrix()
                            [modelParams.getStates().indexOf(vState)][modelParams.getStates().indexOf(wState)]) {
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
     * purposes, and we will not have to consider the associated equation in the final system of equations that describes
     * our compartmental model.
     *
     * @return true if no index location is repeated in the vertices, false otherwise.
     */
    public boolean areLocationsDifferent() {
        boolean locationsDifferent = true;
        // Only need to find two vertices in the list with same index location
        // to know we don't have a required tuple, returning false.
        for (Vertex v : getVertices()) {
            if (getVertices().stream().anyMatch(w -> v.getLocation() == w.getLocation() && v != w)) {
                locationsDifferent = false;
                break;
            }
        }
        return locationsDifferent;
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

    public void addAll(List<Vertex> vertices) {
        Tuple copy = new Tuple(this.vertices);
        this.vertices.addAll(vertices);
        Collections.sort(this.vertices);
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


}
