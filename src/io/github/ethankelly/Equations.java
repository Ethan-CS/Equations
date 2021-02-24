package io.github.ethankelly;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("DuplicatedCode")
public class Equations {

    public static List<String> generateEquations(Tuple tuples) {
        List<String> equations = new ArrayList<>();
        char[] states = tuples.getStates();
        Arrays.sort(states);
        char[] si = new char[]{'S', 'I'};
        char[] sir = new char[]{'S', 'I', 'R'};
        char[] sip = new char[]{'S', 'I', 'P'};
        char[] sirp = new char[]{'S', 'I', 'R', 'P'};
        Arrays.sort(si);
        Arrays.sort(sir);
        Arrays.sort(sip);
        Arrays.sort(sirp);
        int numVertices = tuples.getGraph().getNumVertices();
        int[][] t = tuples.getGraph().getTransmissionMatrix();

        if (Arrays.equals(states, sir)) states = si;
        else if (Arrays.equals(states, sirp)) states = sip;

        for (List<Vertex> tuple : tuples.getTuples()) {
            StringBuilder eqn = new StringBuilder(); // The String representation of the equation of the tuples
            eqn.append(tuple.toString()).append(" = ");
            for (Vertex vertex : tuple) {
                switch (Character.toUpperCase(vertex.getState())) {
                    case 'S' -> {
                        if (new String(states).contains("P")) {
                            if (eqn.length() > (tuple.toString() + " = ").length()) eqn.append("+ ");
                            eqn.append(Greek.ALPHA.uni()).append(Symbol.LANGLE.uni()).append("P")
                                    .append(vertex.getLocation()).append(Symbol.RANGLE.uni());
                        }

                        for (int j = 0; j < numVertices; j++) {
                            if (t[vertex.getLocation()][j] > 0) {
                                eqn.append("- ");
                                if (t[vertex.getLocation()][j] != 1) eqn.append(t[vertex.getLocation()][j]);
                                eqn.append(Greek.BETA.uni()).append(Symbol.LANGLE.uni()).append("S").append(vertex.getLocation())
                                        .append(" I").append(j).append(Symbol.RANGLE.uni());
                            }
                        }
                        if (Arrays.equals(states, sirp))
                            eqn.append("- ").append(Greek.ZETA.uni()).append(Symbol.LANGLE.uni())
                                    .append("S").append(vertex.getLocation()).append(Symbol.RANGLE.uni());
                    }
                    case 'I' -> {
                        for (int j = 0; j < numVertices; j++) {
                            if (t[vertex.getLocation()][j] > 0) {
                                if (eqn.length() > (tuple.toString() + " = ").length() && eqn.charAt(eqn.length() - 1) != '+')
                                    eqn.append("+ ");
                                if (t[vertex.getLocation()][j] != 1) eqn.append(t[vertex.getLocation()][j]);
                                eqn.append(Greek.GAMMA.uni()).append(Symbol.LANGLE.uni()).append("S").append(vertex.getLocation())
                                        .append(" I").append(j).append(Symbol.RANGLE.uni());
                            }
                        }
                        eqn.append("- ").append(Greek.GAMMA.uni()).
                                append(Symbol.LANGLE.uni()).append("I").append(vertex.getLocation()).append(Symbol.RANGLE.uni());
                    }
                    case 'P' -> {
                        if (eqn.length() > (tuple.toString() + " = ").length() && eqn.charAt(eqn.length() - 1) != '+')
                            eqn.append("+ ");
                        eqn.append(Greek.ZETA.uni()).append(Symbol.LANGLE.uni()).append("S").append(vertex.getLocation())
                                .append(Symbol.RANGLE.uni()).append("- ").append(Greek.ALPHA.uni()).append(Symbol.LANGLE.uni())
                                .append("P").append(vertex.getLocation()).append(Symbol.RANGLE.uni());
                    }
                }
            }
            equations.add(String.valueOf(eqn));
        }

        return equations;
    }

    public static void main(String[] args) throws FileNotFoundException {
        PrintStream o = new PrintStream("TLTEquations.txt");
        System.setOut(o);

        char[] states = new char[]{'S', 'I', 'R', 'P'};
        // TRIANGLE
        Graph triangle = new Graph(3);
        triangle.addEdge(0, 1);
        triangle.addEdge(1, 2);
        triangle.addEdge(2, 0);
        Tuple triangleNE = new Tuple(triangle, states);

        System.out.println(" *** TRIANGLE GRAPH ***\n\n" + triangle + "Numbers of equations of each size: "
                + Arrays.toString(triangleNE.findNumbers(triangleNE.getTuples())) + "\n");
        Equations.generateEquations(triangleNE).forEach(System.out::println);

        // LOLLIPOP
        Graph lollipop = new Graph(4);
        lollipop.addEdge(0, 1);
        lollipop.addEdge(0, 2);
        lollipop.addEdge(0, 3);
        lollipop.addEdge(2, 3);
        Tuple lollipopNE = new Tuple(lollipop, states);

        System.out.println("\n *** LOLLIPOP GRAPH ***\n\n" + lollipop + "Numbers of equations of each size: "
                + Arrays.toString(lollipopNE.findNumbers(lollipopNE.getTuples())) + "\n");
        Equations.generateEquations(lollipopNE).forEach(System.out::println);

        // TOAST
        Graph toast = new Graph(4);
        toast.addEdge(0, 1);
        toast.addEdge(0, 2);
        toast.addEdge(0, 3);
        toast.addEdge(1, 2);
        toast.addEdge(2, 3);
        Tuple toastNE = new Tuple(toast, states);

        System.out.println("\n *** TOAST GRAPH ***\n\n" + toast + "Numbers of equations of each size: "
                + Arrays.toString(toastNE.findNumbers(toastNE.getTuples())) + "\n");
        Equations.generateEquations(toastNE).forEach(System.out::println);

        // Creating another File object that represents the disk file and assign to output stream
        PrintStream er = new PrintStream("ErdosRenyiTest.txt");
        System.setOut(er);

        // ERDOS-RENYI
        Graph erdosRenyi = GraphGenerator.erdosRenyi(5, 0.5);
        Tuple erdosRenyiNE = new Tuple(erdosRenyi, states);

        System.out.println(" *** ERDOS-RENYI GRAPH ***\n\n" + erdosRenyi + "Numbers of equations of each size: "
                + Arrays.toString(erdosRenyiNE.findNumbers(erdosRenyiNE.getTuples())) + "\n");
        Equations.generateEquations(erdosRenyiNE).forEach(System.out::println);
    }
}
