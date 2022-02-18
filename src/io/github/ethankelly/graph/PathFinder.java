package io.github.ethankelly.graph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PathFinder {
    /** Vertices in the current path. */
    private final boolean[] onPath;
    private Graph graph;
    /** Stores each current path. */
    private final Deque<Integer> path;
    /** Stores all paths as each one is found. */
    private final List<List<List<Integer>>> allPaths;

    // Get all simple paths from s to t - use DFS
    public PathFinder(Graph graph) {
        onPath = new boolean[graph.getNumVertices()];
        this.graph = graph;
        path   = new ArrayDeque<>();
        allPaths = new ArrayList<>();
        IntStream.range(0, graph.getNumVertices()).<List<List<Integer>>>mapToObj(i -> new ArrayList<>()).forEach(allPaths::add);
    }

    public void findPaths() {
        for (int i = 0; i < this.graph.getNumVertices(); i++)
            for (int j = 0; j < this.graph.getNumVertices(); j++)
                dfs(this.graph, i, j);
    }

    // use DFS
    private void dfs(Graph G, int v, int t) {
        // add v to current path
        path.push(v);
        onPath[v] = true;
        // found path from s to t
        if (v == t) {
            List<Integer> thisPath = path.stream().mapToInt(u -> u).boxed().collect(Collectors.toList());
            allPaths.get(thisPath.size()-1).add(thisPath);
        } else {
            // consider all neighbors that would continue path with repeating a node
            for (Vertex w : G.getAdjList().get(v)) {
                int i = G.getVertices().indexOf(w);
                if (!onPath[i])
                    dfs(G, i, t);
            }
        }
        // done exploring from v, so remove from path
        path.pop();
        onPath[v] = false;
    }

    public int getNoOfPaths() {
        return this.allPaths.size();
    }

    public List<List<Integer>> findPathsOfLength(int l) {
        if (l<=graph.getNumVertices()) return this.getAllPaths().get(l-1);
        List<List<Integer>> subList = new ArrayList<>();
        for (int j = getAllPaths().size() - 1; j >= 0; j--) {
            List<List<Integer>> l1 = getAllPaths().get(j);
            for (List<Integer> list1 : l1) {
                for (int i = getAllPaths().size() - 1; i >= 0; i--) {
                    List<List<Integer>> l2 = getAllPaths().get(i);
                    List<Integer> l3 = new ArrayList<>(list1);
                    loop: while (l3.size()<=l) {
                        for (List<Integer> list2 : l2) {
                            if (graph.hasEdge(list1.get(list1.size() - 1), list2.get(0))) {
                                l3.addAll(list2);
                                if (!subList.contains(l3)) {
                                    if (l3.size()==l) {
                                        subList.add(l3);
                                        break loop;
                                    }
                                } else {
                                    break loop;
                                }
                            }
                        }
                    }
                }
            }
        }
        return subList;
    }

    private List<List<List<Integer>>> getAllPaths() {
        if (this.allPaths.get(0).isEmpty()) findPaths();
        return this.allPaths;
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

        System.out.println(G);

        System.out.println();
        System.out.println("all simple paths");
        PathFinder allpaths1 = new PathFinder(G);
        allpaths1.findPaths();
        for (List<List<Integer>> list : allpaths1.allPaths) System.out.println(list);
        System.out.println("# paths = " + allpaths1.getNoOfPaths());
        List<List<Integer>> l10 = allpaths1.findPathsOfLength(10);
        for (List<Integer> l : l10) System.out.println(l);
    }
}
