package io.github.ethankelly;

import java.util.ArrayList;
import java.util.List;

public class Equations {

    public List<Vertex> generateEquation(Tuple tuple) {
        List<Vertex> equation = new ArrayList<>();
        char[] states = tuple.getStates();
        int numVertices = tuple.getGraph().getNumVertices();
        int[][] transmissionMatrix = tuple.getGraph().getTransmissionMatrix();


        return equation;
    }

    public static void main(String[] args) {

    }
}
