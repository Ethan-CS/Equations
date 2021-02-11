package io.github.ethankelly;

import java.util.*;

public class NecessaryEquations {

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

                if (Vertex.areStatesDifferent(toAdd)
                        && Vertex.areLocationsDifferent(toAdd)
                        && Vertex.areAllConnected(toAdd, g)
                        && !(pairs.contains(toAdd))
                ) {
                    pairs.add(toAdd);
                }
            }
        }
        return pairs;
    }

    public List<List<Vertex>> findTriples(List<Vertex> singles, Graph g) {
        List<List<Vertex>> triples = new ArrayList<>();
        for (Vertex v : singles) {
            for (Vertex w : singles) {
                for (Vertex x : singles) {
                    List<Vertex> toAdd = new ArrayList<>();
                    toAdd.add(v);
                    toAdd.add(w);
                    toAdd.add(x);
                    Collections.sort(toAdd);

                    if (Vertex.areStatesDifferent(toAdd)
                            && Vertex.areLocationsDifferent(toAdd)
                            && Vertex.areAllConnected(toAdd, g)
                            && !(triples.contains(toAdd)))
                        triples.add(toAdd);
                }
            }
        }
        return triples;
    }

    public List<List<Vertex>> findTuples(Graph g, char[] states, int[] vertices) {

        int tupleNumber = vertices.length;
        List<Vertex> singles = this.findSingles(states, vertices);
        List<List<Vertex>> tuples = new ArrayList<>();


        while (tupleNumber > 0) {
            switch (tupleNumber) {
                case 1 -> {
                    tuples.add(singles);
                    tupleNumber--;
                }
                case 2 -> {
                    tuples.addAll((this.findPairs(singles, g)));
                    tupleNumber--;
                }
                case 3 -> {
                    tuples.addAll(this.findTriples(singles, g));
                    tupleNumber--;
                }
            }
        }

        return tuples;
    }



    public static void main(String[] args) {
        NecessaryEquations ne = new NecessaryEquations();

        char[] states = new char[]{'S', 'I'};

        System.out.println();
        // TRIANGLE
        Graph triangle = new Graph(3);
        triangle.addEdge(0, 1);
        triangle.addEdge(1, 2);
        triangle.addEdge(2, 0);
        int[] triangleVertices = new int[triangle.getNumVertices()];
        Arrays.setAll(triangleVertices, i -> i);

        System.out.println("Triangle graph:\n" + triangle);
        List<Vertex> triangleSingles = ne.findSingles(states, triangleVertices);
        List<List<Vertex>> trianglePairs = ne.findPairs(triangleSingles, triangle);
        triangleSingles.forEach(System.out::println);
        trianglePairs.forEach(System.out::println);
        List<List<Vertex>> triangleTriples = ne.findTriples(triangleSingles, triangle);
        triangleTriples.forEach(System.out::println);
        System.out.println();

//        // LOLLIPOP
//        Graph lollipop = new Graph(4);
//        lollipop.addEdge(0, 1);
//        lollipop.addEdge(0, 2);
//        lollipop.addEdge(0, 3);
//        lollipop.addEdge(2, 3);
//        int[] lollipopVertices = new int[lollipop.getNumVertices()];
//        Arrays.setAll(lollipopVertices, i -> i);
//
//        System.out.println("Lollipop graph:\n" + lollipop);
//        List<Vertex> lollipopSingles = ne.findSingles(states, lollipopVertices);
//        List<List<Vertex>> lollipopPairs = ne.findPairs(lollipopSingles, lollipop);
//        lollipopSingles.forEach(System.out::println);
//        lollipopPairs.forEach(System.out::println);
//        List<List<Vertex>> lollipopTriples = ne.findTriples(lollipopSingles, lollipop);
//        lollipopTriples.forEach(System.out::println);
//        System.out.println();
//
//        // TOAST
//        Graph toast = new Graph(4);
//        toast.addEdge(0, 1);
//        toast.addEdge(0, 2);
//        toast.addEdge(0, 3);
//        toast.addEdge(1, 2);
//        toast.addEdge(2, 3);
//        int[] toastVertices = new int[toast.getNumVertices()];
//        Arrays.setAll(toastVertices, i -> i);
//
//        System.out.println("Toast graph:\n" + toast);
//        List<Vertex> toastSingles = ne.findSingles(states, toastVertices);
//        toastSingles.forEach(System.out::println);
//        List<List<Vertex>> toastPairs = ne.findPairs(toastSingles, toast);
//        toastPairs.forEach(System.out::println);
//        List<List<Vertex>> toastTriples = ne.findTriples(toastSingles, toast);
//        toastTriples.forEach(System.out::println);
//        System.out.println();


        System.out.println("Testing tuple method on triangle...");
        List<List<Vertex>> tuples = ne.findTuples(triangle, states, triangleVertices);
        for (List<Vertex> tuple : tuples) {

                System.out.println(tuple);

        }


    }
}
