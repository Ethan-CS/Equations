package io.github.ethankelly;

import java.util.List;
import java.util.stream.IntStream;

public class Vertex implements Comparable<Vertex> {
    private final char state;
    private final int location;

    public Vertex(char state, int location) {
        this.state = state;
        this.location = location;
    }

    public char getState() {
        return state;
    }

    public int getLocation() {
        return location;
    }

    public static boolean areStatesDifferent(List<Vertex> toCheck) {
        boolean statesDifferent = false;

        for (Vertex v : toCheck) {
            for (Vertex w : toCheck) {
                if (v.getState() != w.getState()) {
                    statesDifferent = true;
                    break;
                }
            }
        }
        return statesDifferent;
    }

    public static boolean areLocationsDifferent(List<Vertex> toCheck) {
        boolean locationsDifferent = true;
        for (Vertex v : toCheck) {
            if (toCheck.stream().anyMatch(w -> v.getLocation() == w.getLocation() && v != w)) {
                locationsDifferent = false;
            }
        }
        return locationsDifferent;
    }

    public static boolean areAllConnected(List<Vertex> toCheck, Graph g) {
        return IntStream.range(0, toCheck.size() - 1).allMatch(
                i -> g.isEdge(toCheck.get(i).getLocation(),
                        toCheck.get(i + 1).getLocation())
        );
    }


    @Override
    public String toString() {
        return "\u3008" + this.getState() + this.getLocation() + "\u3009";
    }

    @Override
    public int compareTo(Vertex that) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        //this optimization is usually worthwhile, and can
        //always be added
        if (this == that) return EQUAL;

        //primitive numbers follow this form
        if (this.getLocation() < that.getLocation()) return BEFORE;
        if (this.getLocation() > that.getLocation()) return AFTER;

        //all comparisons have yielded equality
        //verify that compareTo is consistent with equals (optional)
        assert this.equals(that) : "compareTo inconsistent with equals.";

        return EQUAL;
    }

    public static void main(String[] args) {
        Vertex test = new Vertex('S', 1);
        System.out.println(test);
    }
}
