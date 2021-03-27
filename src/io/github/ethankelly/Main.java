package io.github.ethankelly;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;

public class Main {

    /**
     * Takes command-line args to generate equations for a compartmental model on a specified graph.
     *
     * @param args command-line args
     * @throws FileNotFoundException if the files cannot be written to.
     */
    public static void main(String[] args) throws FileNotFoundException {
        // Creating a File object that represents the disk file and assign to output stream
        PrintStream o = new PrintStream("data/Tuples.txt");
        System.setOut(o);

        char[] states = args[0].toUpperCase().toCharArray();
        Graph graph;
        Tuple tuples;

        switch(args[1].toLowerCase()) {
            case "toast" -> {
                graph = GraphGenerator.getToastGraph();
                tuples = new Tuple(graph, states);
            }
            case "triangle" -> {
                graph = GraphGenerator.getTriangleGraph();
                tuples = new Tuple(graph, states);
            }
            case "lollipop" -> {
                graph = GraphGenerator.getLollipopGraph();
                tuples = new Tuple(graph, states);
            }
            case "erdos-renyi" -> {
                int numVertices = 0;
                try {
                    numVertices = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    System.out.println("Second argument should have been an integer");
                }

                double p = 0;
                try {
                    p = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    System.out.println("Third argument should have been a double");
                }
                graph = GraphGenerator.erdosRenyi(numVertices, p);
                tuples = new Tuple(graph, states);
            }
            default -> throw new IllegalStateException("Unexpected value: " + args[1].toLowerCase());
        }

        System.out.println(" *** " + args[1].toUpperCase() + " GRAPH ***\n\n" + graph +
                "Numbers of equations of each size: " + Arrays.toString(tuples.findNumbers(tuples.getTuples())) + "\n");
        tuples.getTuples().forEach(System.out::println);
    }
}
