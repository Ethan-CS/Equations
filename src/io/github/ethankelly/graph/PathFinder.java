package io.github.ethankelly.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Contains methods to find all paths and concatenations of paths of a given length in a given graph.
 */
public class PathFinder {
    /** Vertices in the current path. */
    private final boolean[] onPath;
    /** Stores each current path. */
    private final Deque<Vertex> path;
    /** Stores all paths as each one is found. */
    private final List<List<List<Vertex>>> allPaths;
    /** The graph in which the class is finding paths.*/
    private final Graph graph;

    /**
     * Class constructor.
     *
     * @param graph the graph in which we will find paths.
     */
    public PathFinder(Graph graph) {
        this.onPath = new boolean[graph.getNumVertices()];
        this.graph = graph;
        this.path = new ArrayDeque<>();
        this.allPaths = new ArrayList<>();
        IntStream.range(0, graph.getNumVertices()).<List<List<Vertex>>>mapToObj(i -> new ArrayList<>()).forEach(allPaths::add);
    }

    /**
     * Main method, used for unit testing.
     *
     * @param args command-line args, ignored.
     */
    public static void main(String[] args) {
        Graph G = new Graph(4, "");
        G.addEdge(0, 1);
        G.addEdge(1, 2);
        G.addEdge(2, 3);
        G.addEdge(3, 0);

        System.out.println(G+"\nAll simple paths:");
        PathFinder allPaths = new PathFinder(G);
        allPaths.findPaths();
        for (List<List<Vertex>> list : allPaths.allPaths) System.out.println(list);
        System.out.println("# paths = " + allPaths.getNoOfPaths());
        List<List<Vertex>> l10 = allPaths.findWalksOfLength(10);
        for (List<Vertex> l : l10) System.out.println(l);
    }

    /**
     * Finds all paths between all vertices in the current graph
     */
    public void findPaths() {
        for (int i = 0; i < this.graph.getNumVertices(); i++)
            for (int j = 0; j < this.graph.getNumVertices(); j++)
                dfs(this.graph, i, j);
    }

    // Use Depth First Search to find any path(s) between vertex v and vertex w
    private void dfs(Graph G, int v, int u) {
        // Add v to current path
        path.push(this.graph.getVertices().get(v));
        onPath[v] = true;
        // Found path from v to u
        if (v == u) {
            List<Vertex> thisPath = new ArrayList<>(path);
            allPaths.get(thisPath.size() - 1).add(thisPath);
        } else {
            // Consider all neighbors that would continue path without repeating a node
            for (Vertex w : G.getAdjList().get(v)) {
                int i = G.getVertices().indexOf(w);
                if (!onPath[i])
                    dfs(G, i, u);
            }
        }
        // Done exploring from v, so remove from path
        path.pop();
        onPath[v] = false;
    }

    /**
     * @return the number of paths found in the graph associated with the current PathFinder instance.
     */
    public int getNoOfPaths() {
        int i = 0;
        for (List<List<Vertex>> pathList : this.getAllPaths()) i += pathList.size();
        return i;
    }

    /**
     * Finds all walks of a specified length by attempting to concatenate combinations of the paths in the graph and
     * returning combinations of paths that have the required number of vertices.
     *
     * @param l the length of walks to be found.
     * @return a list of all l-length walks in the graph of the current PathFinder object.
     */
    public List<List<Vertex>> findWalksOfLength(int l) {
        // TODO check - does this alg guarantee us all of the walks of specified length?

        // If specified length is smaller than number of vertices,
        // we can return the list of paths of that length
        if (l <= graph.getNumVertices()) return this.getAllPaths().get(l - 1);

        List<List<Vertex>> walks = new ArrayList<>();
        for (int i = getAllPaths().size() - 1; i >= 0; i--) { // Each list of paths of size i
            List<List<Vertex>> iLengthWalks = getAllPaths().get(i);
            for (List<Vertex> iPath : iLengthWalks) { // Each path of size i
                for (int j = getAllPaths().size() - 1; j >= 0; j--) { // Each list of paths of size j
                    List<List<Vertex>> jLengthWalks = getAllPaths().get(j);
                    List<Vertex> l3 = new ArrayList<>(iPath);
                    // Loop until either we have a walk of length l
                    // Or we have not been able to create such a walk
                    loop:
                    while (l3.size() <= l) {
                        for (List<Vertex> jPath : jLengthWalks) { // Each path of size j
                            // Only concat paths if end vertex of one and start of other share an edge
                            // No need to check converse - covered by other iterations of i and j loops
                            if (graph.hasEdge(iPath.get(iPath.size() - 1), jPath.get(0))) {
                                l3.addAll(jPath);
                                // Ensure we don't add duplicate walks
                                if (!walks.contains(l3)) {
                                    // If we have found a walk of the required size,
                                    // add it to the list and continue iteration
                                    if (l3.size() == l) {
                                        walks.add(l3);
                                        break loop;
                                    }
                                } else break loop;
                            }
                        }
                    }
                }
            }
        }
        return walks;
    }

    /**
     * @return all paths in the graph of the current PathFinder object.
     */
    public List<List<List<Vertex>>> getAllPaths() {
        if (this.allPaths.get(0).isEmpty()) findPaths();
        return this.allPaths;
    }

    /**
     * @return a string representation of the list of paths in the graph associated with the current PathFinder instance.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        IntStream.range(0, this.graph.getNumVertices()).mapToObj(this.getAllPaths()::get).forEach(sb::append);
        return String.valueOf(sb);
    }
}
