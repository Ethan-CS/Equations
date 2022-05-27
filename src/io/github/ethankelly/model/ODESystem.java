package io.github.ethankelly.model;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a system of differential equations that describe a compartmental model of disease on a given graph. This
 * class contains method to generate the system of equations and return as a string representation or use this system
 * and specified initial conditions to solve the system of equations.
 */
public class ODESystem implements FirstOrderDifferentialEquations {
    /** Contact network. */
    private final Graph g;
    private final List<Graph> subGraphs;
    /** Tuples requiring equations in the ODE system. */
    private List<Tuple> requiredTuples;
    /** Parameters of the model. */
    private final ModelParams modelParams;
    /** Maximum value of t (time), for equation solving. */
    public int tMax;
    /** Number of equations required (i.e. number of tuples). */
    private int dimension;
    /** Mapping of requiredTuples to unique integers. */
    private Map<Tuple, Integer> indicesMapping;
    /** String representation of the system of equations. */
    private List<Equation> equations;

    /**
     * Class constructor.
     *
     * @param g    the graph that underpins our compartmental model.
     * @param tMax the maximum value of time to be considered when calculating solutions
     */
    public ODESystem(Graph g, int tMax, ModelParams modelParams) {
        this.g = g;
        this.tMax = tMax;
        this.modelParams = modelParams;
        this.subGraphs = new ArrayList<>(Collections.singletonList(g));
        this.requiredTuples = new ArrayList<>();
        this.equations = new ArrayList<>();
        this.indicesMapping = new HashMap<>();
    }

    public static void main(String[] args) {
        ModelParams SIR = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        SIR.addTransition('S', 'I', 0.6);
        SIR.addTransition('I', 'R', 0.1);

        ODESystem system = new ODESystem(GraphGenerator.getLollipop(), 10, SIR);
        List<Equation> equations = system.findEquations();
        equations.forEach(System.out::println);

        ODESystem reducedSystem = new ODESystem(GraphGenerator.getLollipop(), 10, SIR);
        reducedSystem.reduce();
    }

    public void reduce() {
        decomposeGraph();

        // for required tuples in list of required tuples
        //  if tuple contains cut-vertex (in susceptible state)
        //    split into separate terms
        List<Vertex> cutVertices = this.g.getCutVertices();
        List<Tuple> noLongerRequired = new ArrayList<>();
        List<Tuple> nowRequired = new ArrayList<>();
        for (Tuple t : requiredTuples) {
            if (t.size() > 2) {
                for (Vertex cut : cutVertices) {
                    if (t.contains(cut)) {
                        // Yes -> split, can close if > 1 connected component
                        Map<Integer, Character> originalStates = getOriginalStateMap(t, cut); // Original vertex states

                        List<List<Vertex>> components = g.splice(cut);
                        components.forEach(component -> component.removeIf(v -> !t.contains(v)));

                        List<List<Vertex>> toGetRid = new ArrayList<>();
                        for (List<Vertex> component : components) {
                            if (component.equals(new ArrayList<>(Collections.singletonList(cut))))
                                toGetRid.add(component);
                        }
                        components.removeAll(toGetRid);

                        if (components.size() > 1) {
                            noLongerRequired.add(t);
                            Set<Tuple> split = new HashSet<>();

                            // Make sure we still have original model states
                            for (List<Vertex> component : components) {
                                for (Vertex v : component) {
                                    v.setState(originalStates.get(v.getLocation()));
                                }
                            }

                            for (List<Vertex> list : components) split.add(new Tuple(list));
                            nowRequired.addAll(split);
                        }
                    }
                }
            }
        }
        // Get rid of the tuples we can replace with closures
        requiredTuples.removeAll(noLongerRequired);
        for (Tuple t : nowRequired) if (!requiredTuples.contains(t)) requiredTuples.add(t);
        requiredTuples.forEach(System.out::println);
        this.equations = getEquationsForTuples(requiredTuples);
    }

    private Map<Integer, Character> getOriginalStateMap(Tuple t, Vertex cut) {
        // Mapping of vertex locations to original states, for restoring after splicing
        Map<Integer, Character> originalStates = new HashMap<>();
        for (Vertex v : t.getVertices()) originalStates.put(v.getLocation(), v.getState());
        originalStates.put(cut.getLocation(), 'S'); // Only consider cut-vertices as susceptible
        return originalStates;
    }

    private void decomposeGraph() {
        this.requiredTuples = this.getEquations().stream().map(Equation::getTuple).collect(Collectors.toList());
        if (this.subGraphs.contains(g) && this.subGraphs.size() == 1) this.subGraphs.remove(g);
        this.subGraphs.addAll(this.g.getSpliced());
    }

    /**
     * Using the states required in the model and the number of vertices in the graph, determines the full
     * list of single termed equations that are involved in describing the model exactly.
     *
     * @return the number of single-probability tuples required by the model.
     */
    public List<Tuple> findSingles(Graph graph) {
        List<Character> statesToGenerateFor = new ArrayList<>(this.getModelParameters().getStates());
        for (Character state : this.getModelParameters().getStates()) {
            int i = this.getModelParameters().getStates().indexOf(state);
            // If we can leave this state without any other vertices being involved,
            // Singles containing the state are not dynamically significant
            if (this.getModelParameters().getToExit()[i] == 0) statesToGenerateFor.remove(state);
        }
        List<Vertex> vertexList = graph.getVertices();
        int[] vertices = new int[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) vertices[i] = vertexList.get(i).getLocation();

        // We need an equation for the probability of each vertex being in each state
        List<Tuple> verticesWeNeed = new ArrayList<>();
        for (char c : statesToGenerateFor) {
            for (int v : vertices) {
                verticesWeNeed.add(new Tuple(new Vertex(c, v)));
            }
        }
        Collections.sort(verticesWeNeed);
        return verticesWeNeed;
    }

    /**
     * Creates a mapping between each of the required requiredTuples which we want to generate equations for and unique
     * integers that will be used as indices in the differential equation solving methods in a systematic (i.e. starting
     * from 0) way.
     *
     * @return a mapping of required requiredTuples to unique integers.
     */
    public Map<Tuple, Integer> getIndicesMapping() {
        // If we have already created a mapping, return it;
        // Else, create a mapping and store to class attribute
        if (this.indicesMapping.size() != this.requiredTuples.size()) {
            Map<Tuple, Integer> indices = new HashMap<>(); //  The mapping we will return
            List<Tuple> requiredTuples = this.getTuples(); // The required requiredTuples to assign unique integers to each
            int i = 0; // Counter, to make sure we use a systematic mapping
            for (Tuple list : requiredTuples) indices.put(list, i++);
            this.indicesMapping = indices;
            return indices;
        } else {
            return this.indicesMapping;
        }
    }

    /**
     * @return the requiredTuples for which we are required to generate equations.
     */
    public List<Tuple> getTuples() {
        return this.requiredTuples;
    }

    /**
     * @return the number of equations we are required to generate (i.e. the number of requiredTuples).
     */
    @Override
    public int getDimension() {
        this.dimension = this.getTuples().size();
        return this.dimension;
    }

    /**
     * Computes the current time derivatives of the state vector corresponding to the current system of equations.
     *
     * @param v    the current value of the independent time variable.
     * @param y    the current value of the state vector (as a double array).
     * @param yDot a placeholder array to contain the time derivative of the state vector.
     * @throws MaxCountExceededException  if the number of evaluations of the function is exceeded.
     * @throws DimensionMismatchException if the array dimensions do not match the settings of the system.
     */
    @Override
    public void computeDerivatives(double v, double[] y, double[] yDot) throws MaxCountExceededException, DimensionMismatchException {
        this.getEquations(); // gets mappings of tuples to terms in their differential equations
        for (Equation eqn : equations) {
            int index = indicesMapping.get(eqn.getTuple());
            for (Tuple t : eqn.getTerms().keySet()) {
                for (double rate : eqn.getTerms().get(t)) {
                    yDot[index] += (y[indicesMapping.get(t)] * rate);
                }
            }
        }
    }

    /**
     * Generates the system of equations given the parameters provided for the current model.
     *
     * @return the list of equations in this system.
     */
    public List<Equation> getEquations() {
        if (this.equations == null || this.equations.isEmpty()) this.equations = this.findEquations();
        return this.equations;
    }

    public ModelParams getModelParameters() {
        return this.modelParams;
    }

    /**
     * @return the graph that the current system of differential equations is applied to.
     */
    public Graph getG() {
        return g;
    }

    // Given a list of tuples, generates the equations for each one and returns a list of these equations
    private List<Equation> getEquationsForTuples(List<Tuple> tuples) {
        List<Equation> equations = new ArrayList<>();
        for (Tuple t : tuples) {
            equations.add(new Equation(t, this.getModelParameters(), this.getG()));
        }
        return equations;
    }

    /**
     * @return a string (textual) summary representation of the current system of equations to print to the console.
     */
    @Override
    public String toString() {
        return "ODESystem\n" +
                "GRAPH:" + g +
                "\nREQUIRED TUPLES:\n" + requiredTuples +
                "\nNUMBER OF TUPLES: " + dimension +
                "\nEQUATIONS:" + getEquations();
    }

    private List<Equation> findEquations() {
        List<Equation> eq;
        if (requiredTuples == null || requiredTuples.isEmpty()){
            // Get equations for singles
            List<Tuple> singles = findSingles(g);
            // Add singles to list of required tuples
            requiredTuples.addAll(singles);
            // Get equations for singles
            eq = getEquationsForTuples(singles);
            for (Graph graph : this.subGraphs) {
                // Get equations for higher-order terms
                for (Equation e : getNextTuples(graph, eq, 2)) {
                    if (!eq.contains(e)) eq.add(e);
                }
                eq.stream().map(Equation::getTuple).forEach(requiredTuples::add);
            }
        } else {
            eq = getEquationsForTuples(requiredTuples);
        }

        return eq;
    }

    private List<Equation> getNextTuples(Graph graph, List<Equation> prevEquations, int length) {
        // Stop recurring if specified length is longer than the number of vertices in the graph
        if (length <= graph.getNumVertices()) {
            List<Tuple> nextSizeTuples = new ArrayList<>();
            // Go through previous equations and add any tuples of this
            // length for which we have not yet generated equations.
            for (Equation eq : prevEquations) {
                for (Tuple t : eq.getTerms().keySet()) {
                    if (!nextSizeTuples.contains(t) && t.size() == length) nextSizeTuples.add(t);
                }
            }
            List<Equation> equations = getEquationsForTuples(nextSizeTuples);
            equations.addAll(getNextTuples(graph, equations, length+1));
            return equations;
        } else return new ArrayList<>();
    }
}
