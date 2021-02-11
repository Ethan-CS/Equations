package io.github.ethankelly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NecessaryEquations {

    public int findNumberNeeded(Graph g, char[] states, int r) {
        int numberNeeded = 0;

        for (int i = 0; i < g.getNumVertices(); i++) {
            for (int j = 0; j < g.getNumVertices(); j++) {
                if (g.isEdge(i, j)) {

                }
            }
        }
        // Calculate the value of nCr
        int n = states.length * g.getNumVertices();

        return numberNeeded;
    }

    public List<Vertex> findSingles(char[] states, int[] vertices) {
        List<Vertex> verticesWeNeed = new ArrayList<>();
        for (char c : states) {
            for (int v : vertices) {
                verticesWeNeed.add(new Vertex(c, v));
            }
        }
        return verticesWeNeed;
    }

    public List<List<Vertex>> findPairs(List<Vertex> singles, Graph g) {
        List<List<Vertex>> pairs = new ArrayList<>();
        for (Vertex v : singles) {
            for (Vertex w : singles) {
                List<Vertex> toAdd = new ArrayList<>();
                toAdd.add(v);
                toAdd.add(w);
                Collections.sort(toAdd);

                List<Vertex> toCheck = toAdd;
                toCheck.sort(Collections.reverseOrder());

                if (v.getState() != w.getState()
                        && v.getLocation() != w.getLocation()
                        && !(pairs.contains(toAdd) || pairs.contains(toCheck))
                        && g.isEdge(v.getLocation(), w.getLocation())) {
                    pairs.add(toAdd);
                }
            }
        }
        return pairs;
    }

    public static void main(String[] args) {
        NecessaryEquations ne = new NecessaryEquations();

        char[] states = new char[]{'S','I'};

        Graph triangle = new Graph(3);
        triangle.addEdge(0, 1);
        triangle.addEdge(1, 2);
        triangle.addEdge(2, 0);
        int[] triangleVertices = new int[triangle.getNumVertices()];
        Arrays.setAll(triangleVertices, i -> i);

        Graph lollipop = new Graph(4);
        lollipop.addEdge(0,1);
        lollipop.addEdge(0,2);
        lollipop.addEdge(0, 3);
        lollipop.addEdge(2,3);
        int[] lollipopVertices = new int[lollipop.getNumVertices()];
        Arrays.setAll(lollipopVertices, i -> i);

        Graph toast = new Graph(4);
        toast.addEdge(0, 1);
        toast.addEdge(0, 2);
        toast.addEdge(0, 3);
        toast.addEdge(1, 2);
        toast.addEdge(2, 3);
        int[] toastVertices = new int[toast.getNumVertices()];
        Arrays.setAll(toastVertices, i -> i);

        System.out.println("Triangle graph:\n" + triangle);
        List<Vertex> triangleSingles = ne.findSingles(states, triangleVertices);
        List<List<Vertex>> trianglePairs = ne.findPairs(triangleSingles, triangle);
        triangleSingles.forEach(System.out::println);
        trianglePairs.forEach(System.out::println);
        System.out.println();

        System.out.println("Lollipop graph:\n" + lollipop);
        List<Vertex> lollipopSingles = ne.findSingles(states, lollipopVertices);
        List<List<Vertex>> lollipopPairs = ne.findPairs(lollipopSingles, lollipop);
        lollipopSingles.forEach(System.out::println);
        lollipopPairs.forEach(System.out::println);
        System.out.println();

        System.out.println("Toast graph:\n" + toast);
        List<Vertex> toastSingles = ne.findSingles(states, toastVertices);
        toastSingles.forEach(System.out::println);
        List<List<Vertex>> toastPairs = ne.findPairs(toastSingles, toast);
        toastPairs.forEach(System.out::println);
        System.out.println();

    }
}
