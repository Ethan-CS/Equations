package io.github.ethankelly.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class AllPaths {
    private final boolean[] onPath;        // vertices in current path
    private final Stack<Integer> path;     // the current path
    private final List<List<List<Integer>>> allPaths;
    private int numberOfPaths;             // number of simple path

    // show all simple paths from s to t - use DFS
    public AllPaths(Graph G) {
        onPath = new boolean[G.getNumVertices()];
        path   = new Stack<>();
        allPaths = new ArrayList<>();
        for (int i = 0; i < G.getNumVertices(); i++) {
            allPaths.add(new ArrayList<>());
        }
    }

    public void getPaths(Graph g) {
        for (int i = 0; i < g.getNumVertices(); i++) {
            for (int j = 0; j < g.getNumVertices(); j++) {
                dfs(g, i, j);
            }
        }
    }

    // use DFS
    private void dfs(Graph G, int v, int t) {
        // add v to current path
        path.push(v);
        onPath[v] = true;
        // found path from s to t
        if (v == t) {
            List<Integer> thisPath = processCurrentPath();
            allPaths.get(thisPath.size()-1).add(thisPath);
            numberOfPaths++;
        }
        // consider all neighbors that would continue path with repeating a node
        else {
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

    // this implementation just prints the path to standard output
    private List<Integer> processCurrentPath() {
        Stack<Integer> reverse = new Stack<>();
        List<Integer> toReturn = new ArrayList<>();
        for (int v : path) {
            reverse.push(v);
            toReturn.add(v);
        }
        if (reverse.size() >= 1) {
            System.out.print(reverse.pop());
        }
        while (!reverse.isEmpty()) {
            System.out.print("-" + reverse.pop());
        }
        System.out.println();
        return toReturn;
    }

    // return number of simple paths between s and t
    public int numberOfPaths() {
        return numberOfPaths;
    }

    // test client
    public static void main(String[] args) {
        Graph G = new Graph(7, "");
        G.addEdge(0, 1);
        G.addEdge(1, 2);
        G.addEdge(2, 3);
        G.addEdge(3, 4);
        G.addEdge(4, 5);
        G.addEdge(5, 6);
        G.addEdge(6, 0);
        System.out.println(G);

        System.out.println();
        System.out.println("all simple paths between 0 and 6:");
        AllPaths allpaths1 = new AllPaths(G);
        allpaths1.getPaths(G);
        System.out.println(allpaths1.allPaths);
        System.out.println("# paths = " + allpaths1.numberOfPaths());

    }


}
