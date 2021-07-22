package io.github.ethankelly;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.Vertex;

import java.util.Collections;
import java.util.List;

public class Tuple {
    // For when we just want to add a single vertex to the tuple
    public Tuple(Vertex v) {
        this.singles = Collections.singletonList(v);
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

    public List<Vertex> getSingles() {
        return singles;
    }

    private final List<Vertex> singles; // List of all state-vertex pairs that make up this tuple

    public Tuple(List<Vertex> tuple) {
        this.singles = tuple;
    }

    public int size() {
        return this.getSingles().size();
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

    public void add(Vertex v) {
        this.getSingles().add(v);
    }
}
