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
public class Tuple implements Cloneable, Comparable<Tuple> {
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
        List<Vertex> t = new ArrayList<>(tuple);
        t.sort(new Vertex.VertexComparator());
        this.vertices = t;
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

        return getVertices() != null ?
                (getVertices().containsAll(tuple.getVertices()) && tuple.getVertices().containsAll(getVertices())) :
                tuple.getVertices() == null;
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

    public boolean add(Vertex v) {
        Tuple copy = new Tuple(this.vertices);
        this.vertices.add(v);
        this.vertices.sort(new Vertex.VertexComparator());
        return !this.getVertices().equals(copy.getVertices());
    }

    public boolean remove(Object o) {
        Tuple copy = new Tuple(this.vertices);
        this.vertices.remove(o);
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
        if (this.equals(o)) return 0; // if they're the same, no need to compare
        // We want to sort in size order, so if o is bigger it should come later
        int result;
        if (this.size() == o.size()) { // If sizes are same, compare vertex locations
            for (int i = 0; i < this.size(); i++) {
                result = (this.getVertices().get(i).compareTo(o.getVertices().get(i)));
                if (result != 0) return result;
            }
        } else return Integer.compare(this.size(), o.size());
        return 0;
    }

    public int size() {
        return this.getVertices().size();
    }

    public boolean contains(Object o) {
        Vertex v;
        try {
            v = (Vertex) o;
        } catch (ClassCastException ignored) {
            return false;
        }
        return this.getVertices().contains(v);
    }

    public Vertex get(int i) {
        return this.getVertices().get(i);
    }

    @Override
    public Tuple clone() {
        Tuple clone = null;
        try {
            clone = (Tuple) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assert clone != null;

        clone.vertices = new ArrayList<>();
        clone.vertices.addAll(this.getVertices());
        return clone;
    }

    public static List<Tuple> split(Tuple t, Graph g, Vertex cutVertex) {
        List<Tuple> newTuples = new ArrayList<>();
        newTuples.add(new Tuple(new Vertex('S', cutVertex.getLocation())));

        return newTuples;

    }
}
