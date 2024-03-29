package io.github.ethankelly.graph;

import java.util.*;
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
        int i = 5;
        Graph path = GraphGenerator.path(i);

        System.out.println(path+"\nPath - all simple paths:");
        List<List<Vertex>> combSearchPaths = new PathFinder(path).combSearch();
        for (List<Vertex> list : combSearchPaths) System.out.println(list);
        System.out.println("# paths = " + combSearchPaths.size());

        Graph cycle = GraphGenerator.cycle(i);
        System.out.println(cycle+"\nCycle - all simple paths:");
        List<List<Vertex>> combSearchPathsCycle = new PathFinder(cycle).combSearch();
        for (List<Vertex> list : combSearchPathsCycle) System.out.println(list);
        System.out.println("# paths = " + combSearchPathsCycle.size());

        Graph complete = GraphGenerator.complete(i);
        System.out.println(complete+"\nClique - all simple paths:");
        List<List<Vertex>> combSearchPathsClique = new PathFinder(complete).combSearch();
        for (List<Vertex> list : combSearchPathsClique) System.out.println(list);
        System.out.println("# paths = " + combSearchPathsClique.size());
    }

    /**
     * Finds all paths between all vertices in the current graph
     */
    public void findPaths() {
        for (int i = 0; i < this.graph.getNumVertices(); i++)
            for (int j = 0; j < this.graph.getNumVertices(); j++)
                dfs(this.graph, i, j);
    }

    public List<List<Vertex>> combSearch() {
        // List of all states to avoid searching for children of same state twice
        List<List<Vertex>> allStates = new ArrayList<>();
        for (Vertex v  : this.graph.getVertices()) { // Loop through all vertices in the graph
            // Start expand with list containing only current vertex v
            List<List<Vertex>> expand = new ArrayList<>();
            expand.add(Collections.singletonList(v));
            // Carry on searching until we've recurred on every child state until no more successors exist
            while (!expand.isEmpty()) {
                List<Vertex> x = expand.get(0);
                // Don't add duplicates to allStates list, as we have already searched for children
                if (!allStates.contains(x)) {
                    allStates.add(x);
                    // Generate successor states and add them to expander list
                    List<List<Vertex>> children = generator(x);
                    expand.addAll(children);
                }
                expand.remove(x);
            }
        }

        return allStates;
    }

    protected List<List<Vertex>> generator(List<Vertex> currentState) {
        List<List<Vertex>> children = new ArrayList<>();
        Vertex v  = currentState.get(currentState.size()-1);
        List<Vertex> neighbours = new ArrayList<>(
                this.graph.getAdjList().get(this.graph.getVertices().indexOf(v)).keySet());
        for (Vertex w : neighbours) {
            if (!currentState.contains(w)) {
                List<Vertex> child = new ArrayList<>(currentState);
                child.add(w);
                if (!children.contains(child)) children.add(child);
            }
        }

        return children;
    }

    /**
     * @return the number of paths found in the graph associated with the current PathFinder instance.
     */
    @SuppressWarnings("unused")
    public int getNoOfPaths() {
        return this.getAllPaths().stream().mapToInt(List::size).sum();
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

    // Use Depth First Search to find any path(s) between vertex v and vertex w
    private void dfs(Graph G, int v, int w) {
        // Add v to current path
        path.push(this.graph.getVertices().get(v));
        onPath[v] = true;
        // Found path from v to w
        if (v == w) {
            List<Vertex> thisPath = new ArrayList<>(path);
            allPaths.get(thisPath.size() - 1).add(thisPath);
        } else {
            // Consider all neighbors that would continue path without repeating a node
            for (Vertex u : G.getAdjList().get(v).keySet()) {
                int i = G.getVertices().indexOf(u);
                if (!onPath[i])
                    dfs(G, i, w);
            }
        }
        // Done exploring from v, so remove from path
        path.pop();
        onPath[v] = false;
    }
}
