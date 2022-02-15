package io.github.ethankelly.graph;

import io.github.ethankelly.std.MinPriorityQueue;
import io.github.ethankelly.std.Rand;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * The {@code GraphGenerator} class provides several methods for creating various graphs, including Erdos-Renyi random
 * graphs, random bipartite graphs, random k-regular graphs, and random rooted trees.
 *
 * @author <a href="mailto:e.kelly.1@research.gla.ac.uk">Ethan Kelly</a>
 */
public final class GraphGenerator {
    private static final long seed;

    private static final Graph edge;
    private static final Graph triangle;
    private static final Graph toast;
    private static final Graph lollipop;
    private static final Graph bowTie;
    private static final Graph bowTieWithBridge;

    static {
        // Set the random generation seed
        seed = Rand.getSeed();

        // EDGE
        edge = new Graph(2, "Edge");
        edge.addEdge(0,1);


        // TRIANGLE
        triangle = new Graph(3, "Triangle");
        triangle.addEdge(0, 1);
        triangle.addEdge(1, 2);
        triangle.addEdge(2, 0);

        // TOAST
        toast = new Graph(4, "Toast");
        toast.addEdge(0, 1);
        toast.addEdge(0, 2);
        toast.addEdge(0, 3);
        toast.addEdge(1, 2);
        toast.addEdge(2, 3);

        // LOLLIPOP
        lollipop = new Graph(4, "Lollipop");
        lollipop.addEdge(0, 1);
        lollipop.addEdge(0, 2);
        lollipop.addEdge(0, 3);
        lollipop.addEdge(2, 3);
        // something strange was happening where methods later thought this had 5 vertices, not 4, so moved this to the
        // getter method itself.

        // BOWTIE
        bowTie = new Graph(5, "Bow tie");
        bowTie.addEdge(0, 1);
        bowTie.addEdge(0, 2);
        bowTie.addEdge(1, 2);
        bowTie.addEdge(2, 3);
        bowTie.addEdge(2, 4);
        bowTie.addEdge(3, 4);

        // BOW TIE WITH BRIDGE
        bowTieWithBridge = new Graph(6, "Bow tie with bridge");
        bowTieWithBridge.addEdge(0, 1);
        bowTieWithBridge.addEdge(0, 2);
        bowTieWithBridge.addEdge(1, 2);
        bowTieWithBridge.addEdge(2, 3);
        bowTieWithBridge.addEdge(3, 4);
        bowTieWithBridge.addEdge(3, 5);
        bowTieWithBridge.addEdge(4, 5);
    }

    /**
     * @return a graph consisting of two vertices connected by an undirected edge - the Edge Graph.
     */
    public static Graph getEdge() {
        // EDGE
        return edge;
    }

    /**
     * @return the Toast graph
     */
    public static Graph getToast() {
        // TOAST
        return toast;
    }

    /**
     * @return the Lollipop graph.
     */
    public static Graph getLollipop() {
        Graph l = new Graph(4, "Lollipop");
        l.addEdge(0, 1);
        l.addEdge(0, 2);
        l.addEdge(0, 3);
        l.addEdge(2, 3);

        return l;
    }

    /**
     * @return the bow-tie graph
     */
    public static Graph getBowTie() {
        // BOW TIE
        return bowTie;
    }

    public static Graph getBowTieWithBridge() {
        // BOW TIE WITH BRIDGE
        return bowTieWithBridge;
    }

    /**
     * @return the Triangle graph.
     */
    public static Graph getTriangle() {
        // TRIANGLE
        return triangle;
    }

    public static String[] getUserInput() {
        Scanner scan = new Scanner(System.in);
        String[] arguments = new String[5];

        getUserStates(scan, arguments);
        getUserGraphSelection(scan, arguments);

        System.out.println("Thanks! Generating equations now...");

        scan.close();
        return arguments;
    }

    private static void getUserGraphSelection(Scanner scan, String[] arguments) {
        System.out.println("Now, select a graph. Type \"help\" to see the list of graphs that can be randomly generated.");

        if (scan.nextLine().equalsIgnoreCase("help")) {
            System.out.println("""
                    Enter any of the following currently available graph types for generation:
                      1. Toast
                      2. Triangle
                      3. Lollipop
                      4. Simple
                      5. Erdős–Rényi
                      6. Complete
                      7. Bipartite
                      8. Complete Bipartite
                      9. Path
                     10. Binary Tree
                     11. Cycle
                     12. Eulerian Path
                     13. Eulerian Cycle
                     14. Wheel
                     15. Star
                     16. Regular
                     17. Tree
                    Please enter either the number corresponding to the desired graph type or the name of the graph.""");
        }

        try {
            arguments[1] = scan.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("You should have entered either an integer or graph name.");
            e.printStackTrace();
        }
        // 1, 2 and 3 don't require further input
        if (!arguments[1].equalsIgnoreCase("toast") &&
            !arguments[1].equalsIgnoreCase("triangle") &&
            !arguments[1].equalsIgnoreCase("lollipop") &&
            !arguments[1].equals("1") && !arguments[1].equals("2") && !arguments[1].equals("3")) {
            // 6, 9, 10, 11, 14, 15, 17 just need vertices
            System.out.println("Please enter the desired number of vertices in your selected graph.");
            try {
                arguments[2] = scan.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("You should have entered an integer number of vertices.");
                e.printStackTrace();
            }
            // 7 and 8 need two sets of vertices
            if (arguments[1].equalsIgnoreCase("bipartite") || arguments[1].equals("7") ||
                arguments[1].equalsIgnoreCase("complete bipartite") || arguments[1].equals("8")) {
                System.out.println("Please enter the number of vertices for the first partition.");
                try {
                    arguments[2] = scan.nextLine();
                } catch (InputMismatchException e) {
                    System.out.println("You should have entered an integer number of vertices.");
                    e.printStackTrace();
                }
                System.out.println("Please enter the number of vertices for the second partition.");
                try {
                    arguments[3] = scan.nextLine();
                } catch (InputMismatchException e) {
                    System.out.println("You should have entered an integer number of vertices.");
                    e.printStackTrace();
                }
                // 7 further needs a number of edges
                if (arguments[1].equalsIgnoreCase("bipartite") || arguments[1].equals("7")) {
                    System.out.println("Please enter the desired number of edges in your selected graph.");
                    try {
                        arguments[4] = scan.nextLine();
                    } catch (InputMismatchException e) {
                        System.out.println("You should have entered an integer number of edges.");
                        e.printStackTrace();
                    }
                }
            // 4, 12 and 13 need vertices and edges
            } else if (arguments[1].equalsIgnoreCase("simple") || arguments[1].equals("4") ||
                       arguments[1].equalsIgnoreCase("eulerian path") || arguments[1].equals("12") ||
                       arguments[1].equalsIgnoreCase("eulerian cycle") || arguments[1].equals("13")) {
                System.out.println("Please enter the desired number of edges in your selected graph.");
                try {
                    arguments[3] = scan.nextLine();
                } catch (InputMismatchException e) {
                    System.out.println("You should have entered an integer number of edges.");
                    e.printStackTrace();
                }
            // 16 needs a value for k
            } else if (arguments[1].equalsIgnoreCase("regular") || arguments[1].equals("16")) {
                System.out.println("Please enter the value of k for the k-regular graph.");
                try {
                    arguments[3] = scan.nextLine();
                } catch (InputMismatchException e) {
                    System.out.println("You should have entered an integer number.");
                    e.printStackTrace();
                }
            } else if (arguments[1].equalsIgnoreCase("erdos renyi") ||
                       arguments[1].equalsIgnoreCase("erdős rényi") || arguments[1].equals("5")) {
                System.out.println("Please enter a probability for the Erdős-Rényi graph.");
                try {
                    arguments[3] = scan.nextLine();
                } catch (InputMismatchException e) {
                    System.out.println("You should have entered a decimal number.");
                    e.printStackTrace();
                }
            }
        }
    }

    private static void getUserStates(Scanner scan, String[] arguments) {
        System.out.println("""
                
                *** COMPARTMENTAL GRAPH MODEL EQUATIONS GENERATOR ***
                
                Welcome to the Equations Generation program. This software generates the full system of differential
                equations required to describe a particular compartmental model of disease on a specified graph. A text
                file will be generated in the same directory as you are running this software from. If you choose to run
                the jar again, this text file will be overwritten unless you move it elsewhere or delete it.
                                
                To begin, please enter the states required in the model. Currently, this can be either of:
                 * SIR
                 * SIRP
                where S represents Susceptible, I is Infected, R is Recovered and P is Protected.""");
        try {
            arguments[0] = scan.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("You should have entered characters representing the required states.");
            e.printStackTrace();
        }
    }

    public static Graph getGraph(String[] args) {
        Graph graph;
        switch (args[1].toLowerCase()) {
            case "1", "toast" -> graph = getToast();
            case "2", "triangle" -> graph = getTriangle();
            case "3", "lollipop" -> graph = getLollipop();
            case "4", "simple" -> {
                // Requires number of vertices and number of edges
                int[] parsed = parseIntegers(new String[] {args[2], args[3]});
                graph = simple(parsed[0], parsed[1]);
            }
            case "5", "erdos renyi", "erdős rényi", "er" -> {
                // Requires number of vertices and a probability
                int numVertices = parseIntegers(new String[] {args[2]})[0];
                double p = parseDoubles(new String[] {args[3]})[0];
                graph = erdosRenyi(numVertices, p);
            }
            case "6", "complete" -> graph = complete(parseIntegers(new String[] {args[2]})[0]);
            case "7", "bipartite" -> {
                // Requires 2 numbers of vertices and a number of edges
                int[] parsed = parseIntegers(new String[] {args[2], args[3], args[4]});
                graph = bipartite(parsed[0], parsed[1], parsed[2]);
            }
            case "8", "complete bipartite" -> {
                // Requires 2 numbers of vertices
                int[] parsed = parseIntegers(new String[] {args[2], args[3]});
                graph = completeBipartite(parsed[0], parsed[1]);
            }
            case "9", "path" -> graph = path(parseIntegers(new String[] {args[2]})[0]);
            case "10", "binary tree" -> graph = binaryTree(parseIntegers(new String[] {args[2]})[0]);
            case "11", "cycle" -> graph = cycle(parseIntegers(new String[] {args[2]})[0]);
            case "12", "eulerian path" -> {
                // Requires number of vertices and number of edges
                int[] parsed = parseIntegers(new String[] {args[2], args[3]});
                graph = eulerianPath(parsed[0], parsed[1]);
            }
            case "13", "eulerian cycle" -> {
                // Requires number of vertices and number of edges
                int[] parsed = parseIntegers(new String[] {args[2], args[3]});
                graph = eulerianCycle(parsed[0], parsed[1]);
            }
            case "14", "wheel" -> graph = wheel(parseIntegers(new String[] {args[2]})[0]);
            case "15", "star" -> graph = star(parseIntegers(new String[] {args[2]})[0]);
            case "16", "regular" -> {
                // Requires number of vertices and value of k
                int[] parsed = parseIntegers(new String[] {args[2], args[3]});
                graph = regular(parsed[0], parsed[1]);
            }
            case "17", "tree" -> graph = tree(parseIntegers(new String[] {args[2]})[0]);
            default -> throw new IllegalStateException("Unexpected graph type: " + args[1].toLowerCase());
        }
        return graph;
    }

    public static int[] parseIntegers(String[] args) {
        int[] toReturn = new int[args.length];
        for (int i = 0; i < args.length; i++) {
            try {
                toReturn[i] = Integer.parseInt(args[i]);
            } catch (NumberFormatException e) {
                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
                System.out.println("Could not parse integer: " + e);
            }
        }
        return toReturn;
    }

    public static double[] parseDoubles(String[] args) {
        double[] toReturn = new double[args.length];
        for (int i = 0; i < args.length; i++) {
            try {
                toReturn[i] = Double.parseDouble(args[i]);
            } catch (NumberFormatException e) {
                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
                System.out.println("Could not parse double: " + e);
            }
        }
        return toReturn;
    }

    /**
     * The Edge class is used to represent an edge as a pair of vertex locations (v, w) where v < w, between which the
     * edge exists.
     *
     * @author <a href="mailto:e.kelly.1@research.gla.ac.uk">Ethan Kelly</a>
     */
    private static final class Edge implements Comparable<Edge> {
        private final int v;
        private final int w;


        /**
         * Class constructor - ensures v is less than w.
         *
         * @param v the first vertex the edge is attached to.
         * @param w the other vertex the edge is attached to.
         */
        private Edge(int v, int w) {
            if (v < w) {
                this.v = v;
                this.w = w;
            } else {
                this.v = w;
                this.w = v;
            }
        }

        /**
         * Compares the location of two edges. Returns -1 if either this v is less than the v given as a parameter or
         * this w is less than the w given as a parameter.
         *
         * @param that the edge to which we compare the current edge.
         * @return 1 if the original v or w are each greater than their parameter counterparts, -1 if at least one of
         * the original v or w are less than the comparison v or w, 0 if the edges have the same start and end vertices
         * (i.e. they are the same edge).
         */
        public int compareTo(Edge that) {
            if (this.v < that.v || this.w < that.w) return -1;
            else if (this.v > that.v || this.w > that.w) return +1;
            else return 0;
        }
    }

    /**
     * A simple graph is an unweighted and undirected graph with no loops or multiple edges.A simple graph may be either
     * connected or disconnected. This method randomly generates and returns such a simple graph given (valid) numbers
     * of vertices and edges.
     *
     * @param numVertices the number of vertices.
     * @param numEdges    the number of vertices.
     * @return a random simple graph on the given number of vertices with the given number of edges.
     * @throws AssertionError if no such simple graph can be generated.
     */
    public static Graph simple(int numVertices, int numEdges) {
        assert numEdges <= numVertices * (numVertices - 1) / 2 : "Too many edges.";
        assert numEdges >= 0 : "Too few edges.";
        Graph g = new Graph(numVertices, "Simple");
        g.setNumEdges(0);
        Set<Edge> set = new HashSet<>();
        while (g.getNumEdges() < numEdges) {
            int v = Rand.uniform(numVertices);
            int w = Rand.uniform(numVertices);
            Edge e = new Edge(v, w);
            if ((v != w) && !set.contains(e)) {
                set.add(e);
                g.addEdge(v, w);
            }
        }
        return g;
    }

    /**
     * Generates and returns a random Erdős–Rényi graph on a given number of vertices, with an edge between any two
     * vertices existing with some given probability.
     *
     * @param numVertices the number of vertices
     * @param probability the probability of choosing an edge.
     * @return a random erdosRenyi graph on {@code numVertices} vertices, with an edge between any two vertices with
     * probability {@code probability}.
     * @throws AssertionError if the given probability is not between 0 and 1.
     */
    public static Graph erdosRenyi(int numVertices, double probability) {
        assert !(probability < 0.0) && !(probability > 1.0) : "Probability must be between 0 and 1";
        Graph g = new Graph(numVertices, "Erdős–Rényi");
        for (int v = 0; v < numVertices; v++)
            for (int w = v + 1; w < numVertices; w++)
                if (Rand.bernoulli(probability))
                    g.addEdge(v, w);
        return g;
    }

    /**
     * A complete graph is a graph in which every pair of vertices is connected by a single unique edge. This method
     * generates and returns the complete graph on a given number of vertices.
     *
     * @param numVertices the number of vertices.
     * @return the complete graph on the given number of vertices.
     */
    public static Graph complete(int numVertices) {
        Graph complete = new Graph(numVertices, "Complete");
        for (int i = 0; i < complete.getNumVertices(); i++) {
            for (int j = 0; j < complete.getNumVertices(); j++) {
                if (i != j && !complete.hasEdge(i, j)) complete.addEdge(i, j);
            }
        }
        return complete;
    }

    /**
     * A bipartite graph (or simply bigraph) is a graph where the vertices can be divided into two disjoint, independent
     * sets such that every edge connects a vertex in the first set to one in the second set. This method generates and
     * returns a complete bipartite graph with {@code numVer1} vertices in the first set and {@code numVer2} vertices in
     * the second set.
     *
     * @param numVer1 the number of vertices in the first partition.
     * @param numVer2 the number of vertices in the second partition.
     * @return a complete bipartite graph on {@code numVer1} and {@code numVer2} vertices.
     */
    public static Graph completeBipartite(int numVer1, int numVer2) {
        return bipartite(numVer1, numVer2, numVer1 * numVer2).setName("Complete Bipartite");
    }

    /**
     * Generates and returns a bipartite graph using the Erdős–Rényi model on {@code numVer1} and {@code numVer2}
     * vertices with {@code numEdges} edges.
     *
     * @param numVer1  the number of vertices in the first partition.
     * @param numVer2  the number of vertices in the second partition.
     * @param numEdges the number of edges onto which to generate the graph.
     * @return a random Erdős–Rényi bipartite graph on {@code numVer1} and {@code numVer2} vertices, containing a total
     * of {@code numEdges} edges.
     * @throws AssertionError if no such Erdős–Rényi bipartite graph can be generated.
     */
    public static Graph bipartite(int numVer1, int numVer2, int numEdges) {
        assert numEdges <= numVer1 * numVer2 : "Too many edges";
        assert numEdges >= 0 : "Too few edges";
        Graph g = new Graph(numVer1 + numVer2, "Bipartite"
        );

        int[] vertices = IntStream.range(0, numVer1 + numVer2).toArray();
        Rand.shuffle(vertices);

        Set<Edge> set = new HashSet<>();
        while (g.getNumEdges() < numEdges) {
            int i = Rand.uniform(numVer1);
            int j = numVer1 + Rand.uniform(numVer2);
            Edge e = new Edge(vertices[i], vertices[j]);
            if (!set.contains(e)) {
                set.add(e);
                g.addEdge(vertices[i], vertices[j]);
            }
        }
        return g;
    }

    /**
     * Returns a random Erdős–Rényi bipartite graph on {@code numVer1} and {@code numVer2} vertices, containing each
     * possible edge with probability {@code probability}.
     *
     * @param numVer1     the number of vertices in the first partition.
     * @param numVer2     the number of vertices in the second partition.
     * @param probability the probability that the graph contains an edge with one endpoint in either side.
     * @return a random erdosRenyi bipartite graph on {@code numVer1} and {@code numVer2} vertices, containing each
     * possible edge with probability {@code probability}.
     * @throws AssertionError if probability is not between 0 and 1.
     */
    @SuppressWarnings("unused")
    public static Graph bipartite(int numVer1, int numVer2, double probability) {
        assert !(probability < 0.0) && !(probability > 1.0) : "Probability must be between 0 and 1";
        int[] vertices = IntStream.range(0, numVer1 + numVer2).toArray();
        Rand.shuffle(vertices);
        Graph G = new Graph(numVer1 + numVer2, "Bipartite"
        );
        for (int i = 0; i < numVer1; i++)
            for (int j = 0; j < numVer2; j++)
                if (Rand.bernoulli(probability))
                    G.addEdge(vertices[i], vertices[numVer1 + j]);
        return G;
    }

    /**
     * A path is a sequence of edges joining a given sequence of vertices. This method generates and returns a path
     * graph on {@code numVertices} vertices.
     *
     * @param numVertices the number of vertices in the path
     * @return a path graph on {@code numVertices} vertices
     */
    public static Graph path(int numVertices) {
        Graph g = new Graph(numVertices, "Path");
        // Generate an array: [0, 1, ..., numVertices] and randomly shuffle it.
        int[] vertices = IntStream.range(0, numVertices).toArray();
        Rand.shuffle(vertices);
        // Connect the consecutive vertices to generate the random path graph
        for (int i = 0; i < numVertices - 1; i++) {
            g.addEdge(vertices[i], vertices[i + 1]);
        }
        return g;
    }

    /**
     * A binary tree is a tree where each vertex has at most two children. A complete binary tree is a binary tree in
     * which every level is completely filled (every vertex has exactly two children) and all nodes are as far left as
     * possible. This method generates and returns a complete binary tree graph on {@code numVertices} vertices.
     *
     * @param numVertices the number of vertices in the binary tree
     * @return a complete binary tree graph on {@code numVertices} vertices
     */
    public static Graph binaryTree(int numVertices) {
        Graph g = new Graph(numVertices, "Binary Tree");
        // Generate an array: [0, 1, ..., numVertices] and randomly shuffle it.
        int[] vertices = IntStream.range(0, numVertices).toArray();
        Rand.shuffle(vertices);
        // Give each vertex two children (if we can)
        for (int i = 1; i < numVertices; i++) {
            g.addEdge(vertices[i], vertices[(i - 1) / 2]);
        }
        return g;
    }

    /**
     * A cycle in a graph is a non-empty sequence of edges (a trail) in which the only repeated vertices are the first
     * and last ones. This method generates and returns a cycle graph on {@code numVertices} vertices, a graph composed
     * of a single cycle.
     *
     * @param numVertices the number of vertices in the cycle.
     * @return a cycle graph on {@code numVertices} vertices.
     */
    public static Graph cycle(int numVertices) {
        Graph g = new Graph(numVertices, "Cycle");
        // Generate an array: [0, 1, ..., numVertices]
        int[] vertices = IntStream.range(0, numVertices).toArray();
        // Connect vertices from 0 to 2 less than the number of vertices consecutively
        for (int i = 0; i < numVertices - 1; i++) {
            g.addEdge(vertices[i], vertices[i + 1]);
        }
        // Connect the last and first vertices to complete the cycle.
        g.addEdge(vertices[numVertices - 1], vertices[0]);
        return g;
    }

    /**
     * An Eulerian path is a path on a graph that visits each edge precisely once. This method generates and returns an
     * Eulerian path graph on {@code numVertices} vertices.
     *
     * @param numVertices the number of vertices in the path
     * @param numEdges    the number of edges in the path
     * @return a graph that is an Eulerian path on {@code numVertices} vertices and {@code numEdges} edges
     * @throws AssertionError if either {@code numVertices <= 0} or {@code numEdges < 0}
     */
    public static Graph eulerianPath(int numVertices, int numEdges) {
        // Catch incorrect input of number of edges or vertices
        assert numEdges >= 0 : "negative number of edges";
        assert numVertices > 0 : "An Eulerian path must have at least one vertex";
        Graph g = new Graph(numVertices, "Eulerian Path");
        // Fill an array of length equal to the number of edges with uniformly random values
        int[] vertices = IntStream.range(0, numEdges + 1).map(i -> Rand.uniform(numVertices)).toArray();
        // Connect consecutive (i, i+1) vertices
        IntStream.range(0, numEdges).forEach(i -> g.addEdge(vertices[i], vertices[i + 1]));
        return g;
    }

    /**
     * An Eulerian cycle is a graph cycle that has each edge in the cycle exactly once. This method generates and
     * returns an Eulerian cycle graph on {@code numVertices} vertices.
     *
     * @param numVertices the number of vertices in the cycle.
     * @param numEdges    the number of edges in the cycle.
     * @return a graph that is an Eulerian cycle on {@code numVertices} vertices and {@code numEdges} edges
     * @throws AssertionError if either {@code numVertices <= 0} or {@code numEdges <= 0}
     */
    public static Graph eulerianCycle(int numVertices, int numEdges) {
        assert numEdges > 0 : "An Eulerian cycle must have at least one edge";
        assert numVertices > 0 : "An Eulerian cycle must have at least one vertex";
        Graph G = new Graph(numVertices, "Eulerian Cycle");
        // Fill an array of length equal to the number of edges with uniformly random values
        int[] vertices = IntStream.range(0, numEdges).map(i -> Rand.uniform(numVertices)).toArray();
        // Connect consecutive (i, i+1) vertices
        IntStream.range(0, numEdges - 1).forEach(i -> G.addEdge(vertices[i], vertices[i + 1]));
        // Connect the last edge-indexed element in the vertex array to the first vertex to complete the cycle
        G.addEdge(vertices[numEdges - 1], vertices[0]);
        return G;
    }

    /**
     * A wheel graph is a single vertex connected to every vertex in a cycle. This method generates and returns a wheel
     * graph on {@code numVertices} vertices.
     *
     * @param numVertices the number of vertices in the wheel
     * @return a wheel graph on {@code numVertices} vertices
     * @throws AssertionError if the number of vertices provided is not at least 2.
     */
    public static Graph wheel(int numVertices) {
        assert numVertices > 1 : "Number of vertices must be at least 2";
        Graph g = new Graph(numVertices, "Wheel");
        // Generate an array: [0, 1, ..., numVertices] and randomly shuffle it.
        int[] vertices = IntStream.range(0, numVertices).toArray();
        Rand.shuffle(vertices);

        // Create an Erdős–Rényi cycle on numVertices-1 vertices
        IntStream.range(1, numVertices - 1).forEach(i -> g.addEdge(vertices[i], vertices[i + 1]));
        // Connect the last vertex to the first to complete the cycle
        g.addEdge(vertices[numVertices - 1], vertices[1]);

        // Connect vertices[0] to every vertex on cycle
        IntStream.range(1, numVertices).forEach(i -> g.addEdge(vertices[0], vertices[i]));

        return g;
    }

    /**
     * A star graph is a tree with a single internal vertex and a given number of leaves. This method generates and
     * returns a star graph on {@code numVertices} vertices.
     *
     * @param numVertices the number of vertices in the star.
     * @return a star graph on {@code numVertices} vertices.
     */
    public static Graph star(int numVertices) {
        assert numVertices > 0 : "Number of vertices must be at least 1";
        Graph g = new Graph(numVertices, "Star");
        // Generate an array: [0, 1, ..., numVertices] and randomly shuffle it.
        int[] vertices = IntStream.range(0, numVertices).toArray();
        Rand.shuffle(vertices);

        // Connect vertices[0] to every other vertex
        IntStream.range(1, numVertices).forEach(i -> g.addEdge(vertices[0], vertices[i]));

        return g;
    }

    /**
     * Returns a uniformly random {@code k}-regular graph on {@code numVertices} vertices. This graph is not necessarily
     * Erdős–Rényi and is in fact Erdős–Rényi with probability only about <em>e^(-k^2/4)</em>, which is tiny when k =
     * 14.
     *
     * @param numVertices the number of vertices in the graph
     * @param k           degree of each vertex
     * @return a uniformly random {@code k}-regular graph on {@code numVertices} vertices.
     * @throws AssertionError if the number of vertices multiplied by k is not even.
     */
    public static Graph regular(int numVertices, int k) {
        assert numVertices * k % 2 == 0 : "Number of vertices * k must be even";
        Graph g = new Graph(numVertices, k + "-Regular");

        // Create k copies of each vertex
        int[] vertices = new int[numVertices * k];
        for (int v = 0; v < numVertices; v++) {
            for (int j = 0; j < k; j++) {
                vertices[v + numVertices * j] = v;
            }
        }
        // Pick a random perfect matching:
        // Shuffle the vertices and, for i from 0 to (k*numVertices)/2,
        // add edges between consecutive vertices (2i, 2i + 1)
        Rand.shuffle(vertices);
        IntStream.range(0, numVertices * k / 2).forEach(i -> g.addEdge(vertices[2 * i], vertices[2 * i + 1]));
        return g;
    }

    /**
     * Returns a uniformly random tree on {@code numVertices} vertices. This algorithm uses a Prüfer sequence, which is
     * a unique sequence associated to a tree, and takes time proportional to <em>numVertices log(numVertices)</em>.
     *
     * @param numVertices the number of vertices in the tree.
     * @return a uniformly random tree on {@code numVertices} vertices.
     */
    public static Graph tree(int numVertices) {
        Graph g = new Graph(numVertices, "Tree");

        if (numVertices == 1) return g;

        // Cayley's theorem: there are numVertices^(numVertices-2) labeled trees on numVertices vertices
        // Prüfer sequence: sequence of numVertices-2 values between 0 and numVertices-1
        // Prüfer's proof of Cayley's theorem: Prufer sequences are in 1-1 with labeled trees on numVertices vertices

        // Fill a new array of size two less than numVertices with uniformly random integers
        int[] prufer = IntStream.range(0, numVertices - 2).map(i -> Rand.uniform(numVertices)).toArray();

        // Degree of vertex v = 1 + no. times it appears in Prüfer sequence
        int[] degree = IntStream.range(0, numVertices).map(v -> 1).toArray();
        IntStream.range(0, numVertices - 2).forEach(i -> degree[prufer[i]]++);

        // Minimum priority queue object pq contains all vertices of degree 1
        MinPriorityQueue<Integer> pq = new MinPriorityQueue<>();
        IntStream.range(0, numVertices).filter(v -> degree[v] == 1).forEach(pq::insert);

        // Repeatedly call delMin() (removes and returns smallest key on the priority queue)
        // on each degree 1 vertex that has the current minimum index
        for (int i = 0; i < numVertices - 2; i++) {
            int v = pq.delMin();
            g.addEdge(v, prufer[i]);
            degree[v]--;
            degree[prufer[i]]--;
            if (degree[prufer[i]] == 1) pq.insert(prufer[i]);
        }
        g.addEdge(pq.delMin(), pq.delMin());
        return g;
    }

    /**
     * @return the seed from StdRandom used to generate the pseudo-random values used to create random graphs.
     */
    @SuppressWarnings("unused")
    public static long getSeed() {
        return seed;
    }
}
