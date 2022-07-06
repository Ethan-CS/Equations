package io.github.ethankelly.model;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.jetbrains.annotations.NotNull;

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
    private Map<Double, double[]> yDotResults;

	public void setClosures(boolean closures) {
		if (this.requiredTuples != null && this.requiredTuples.size() != 0) this.reduce();
		this.closures = closures;
	}

	/** Whether we are implementing closure result when generasting equations. */
	private boolean closures = false;

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
        this.yDotResults = new LinkedHashMap<>();
    }

    public static void main(String[] args) {
        ModelParams SIR = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        SIR.addTransition('S', 'I', 0.6);
        SIR.addTransition('I', 'R', 0.1);

//	    compareRunTimes(SIR);

//	    int i = 3;

	    for (int i = 6; i < 7; i++) {
		    ODESystem ode = new ODESystem(GraphGenerator.path(i), 1, SIR);
//		    ode.getEquations().forEach(System.out::println);
		    System.out.println("SETTING CLOSURES TO TRUE");
		    ODESystem odeReduced = new ODESystem(GraphGenerator.path(i), 1, SIR);
		    odeReduced.setClosures(true);
		    odeReduced.getEquations().forEach(System.out::println);

		    int regExpected = (3 * i * i - i + 2) / 2;
		    int redExpected = 5 * i - 3;
		    System.out.println("i=" + i + ": " + regExpected + "=" + ode.getDimension() + ", " + redExpected + "=" + odeReduced.getDimension());

	    }
    }

	private static void compareRunTimes(ModelParams SIR) {
		for (int i = 1; i <= 50; i++) {
			ODESystem ode = new ODESystem(GraphGenerator.path(i), 1, SIR);

			final long startTime = System.nanoTime();
			ode.getEquations();
				final long endTime = System.nanoTime() - startTime;

			ODESystem odeReduced = new ODESystem(GraphGenerator.path(i), 1, SIR);
			odeReduced.closures = true;

			final long startTimeForReduced = System.nanoTime();
			odeReduced.getEquations();
			final long endTimeForReduced = System.nanoTime() - startTimeForReduced;

			System.out.println(i + ", " + ode.getDimension() + ", " + endTime + ", "  + odeReduced.getDimension() + ", " + endTimeForReduced + ", ");

	//          ode.getEquations().forEach(System.out::println);
	//			odeCut.getEquations().forEach(System.out::println);
		}
	}

	private void setInitialConditions(double[] y, List<Vertex> infected) {
        // SINGLES
        for (Vertex v : getG().getVertices()) {
            int I = getIndicesMapping().get(new Tuple(new Vertex('I', v.getLocation())));
            int S = getIndicesMapping().get(new Tuple(new Vertex('S', v.getLocation())));
            if (infected.contains(new Vertex(v.getLocation()))) {
                // This vertex is infected - prob it is infected = 1, prob susceptible = 0
                y[I] = 1;
                y[S] = 0;
            } else {
                // This is not infected - prob susceptible = 1, prob infected = 0
                y[I] = 0;
                y[S] = 1;
            }
        }

        for (Tuple t : getTuples()) {
            int index = getIndicesMapping().get(t);
            y[index] = 1;

            List<Integer> infectedLocations = new ArrayList<>();
            for (Vertex v : infected) infectedLocations.add(v.getLocation());

            for (Vertex v : t.getVertices()) {
                int indexOfStateV = this.getModelParameters().getStates().indexOf(v.getState());
                if (indexOfStateV >= 0 && this.getModelParameters().getToEnter()[indexOfStateV] > 1) {
                    if (!infected.contains(v)) {
                        y[index] = 0;
                        break;
                    }
                } else if (infectedLocations.contains(v.getLocation()) && this.getModelParameters().getToEnter()[indexOfStateV] < 2) {
                    y[index] = 0;
                    break;
                }
            }
        }

        // Final check that no probability is less than 0 or greater than 1
        for (int index = 0; index < y.length; index++) {
            if (y[index] > 1) y[index] = 1;
            if (y[index] < 0) y[index] = 0;
        }
    }

    private void printResults(double[] y) {
        assert y.length == this.indicesMapping.size();
        for (Tuple t : this.getIndicesMapping().keySet()) {
            System.out.println(t + "=" + y[this.getIndicesMapping().get(t)]);
        }
        System.out.println();
    }

    public void reduce() {
        decomposeGraph();

	    List<Tuple> currentTuples = closeTuples(new ArrayList<>(requiredTuples), new ArrayList<>(g.getCutVertices()));

	    setRequiredTuples(new ArrayList<>(currentTuples));
        this.equations = this.getEquationsForTuples(requiredTuples);
    }

	private void setRequiredTuples(List<Tuple> tuples) {
		this.requiredTuples = new ArrayList<>(tuples);
		this.dimension = tuples.size();
	}

	@NotNull
	private List<Tuple> closeTuples(List<Tuple> tuplesToClose, List<Vertex> cutVertices) {
		// List of cut-vertices in the current graph
		List<Vertex> CVs = new ArrayList<>(cutVertices);
		// Mapping of decomposed graphs to the cut-vertex that was removed to generate them
		Map<Vertex, List<List<Vertex>>> subGraphsByCutVertex = getDecomposedGraphs(CVs);
		// List of tuples to (try to) close
		List<Tuple> currentTuples = new ArrayList<>(tuplesToClose);
		// List of all tuples that can be replaced by closure terms
		Map<Tuple, Vertex> canBeClosed = getTuplesToClose(CVs, currentTuples);

		Map<Tuple, Vertex> stillToClose = new HashMap<>(canBeClosed);
		// Find the closure terms for each of tuples we can possibly close
		Set<Tuple> closuresToAdd = new HashSet<>();
		List<Tuple> closedToRemove = new ArrayList<>();
		while (!stillToClose.isEmpty()){
		    for (Tuple toClose : canBeClosed.keySet()) {
		        if (stillToClose.containsKey(toClose)) {

		        List<List<Vertex>> filteredComponents = new ArrayList<>();

		        List<List<Vertex>> get = subGraphsByCutVertex.get(canBeClosed.get(toClose));
		        for (int i = 0; i < get.size(); i++) {
		            filteredComponents.add(new ArrayList<>());
		            for (Vertex v : get.get(i)) {
		                filteredComponents.get(i).add(new Vertex(v.getState(), v.getLocation()));
		            }
		        }

		        for (List<Vertex> component : filteredComponents) {
		            component.removeIf(v -> !toClose.contains(v));
		        }

		        filteredComponents.removeIf(List::isEmpty);
		        // If there's still more than one non-empty component, we can replace this term with closures
		        if (filteredComponents.size() > 1) {
		            closedToRemove.add(toClose);
		            for (List<Vertex> component : filteredComponents) {
		                List<Vertex> toCreate = new ArrayList<>(component);
		                Map<Integer, Character> originalStates = getOriginalStateMap(toClose, canBeClosed.get(toClose));
		                for (Vertex v : toCreate) {
		                    v.setState(originalStates.get(v.getLocation()));
		                }
		                Tuple candidate = new Tuple(toCreate);

		                // If the tuple can be closed, don't add it to tuples yet
		                // Add to the list to close and then add its closure terms instead
		                boolean canClose = false;
		                for (Vertex otherCut : CVs) {
		                    if (canClose(candidate, otherCut)) {
		                        stillToClose.put(candidate, otherCut);
		                        canClose = true;
		                        break;
		                    }
		                }
		                if (!canClose) {
		                    closuresToAdd.add(candidate);
		                    closedToRemove.add(toClose);
		                }
		            }
		        } else {
		            System.err.println("Couldn't close " + toClose);
		        }
		        stillToClose.remove(toClose);
		    }
		    }
		    for (Tuple stillToDo : stillToClose.keySet()) {
		        canBeClosed.put(stillToDo, stillToClose.get(stillToDo));
		    }

		}

		for (Tuple toAdd : closuresToAdd) if (!currentTuples.contains(toAdd)) currentTuples.add(toAdd);
		currentTuples.removeAll(closedToRemove);
		return currentTuples;
	}

	@NotNull
	private Map<Vertex, List<List<Vertex>>> getDecomposedGraphs(List<Vertex> cutVertices) {
		// List of components resulting from removing each cut vertex in turn mapped to cut-vertex
		Map<Vertex, List<List<Vertex>>> subGraphsByCutVertex = new HashMap<>();
		for (Vertex vertex : cutVertices) {
			subGraphsByCutVertex.put(new Vertex(vertex.getLocation()), new ArrayList<>(g.splice(vertex)));
		}
		return subGraphsByCutVertex;
	}

	@NotNull
    private Map<Tuple, Vertex> getTuplesToClose(List<Vertex> cutVertices, List<Tuple> currentTuples) {
        Map<Tuple, Vertex> canBeClosed = new HashMap<>();
        for (Tuple t : currentTuples) {
            for (Vertex cut : cutVertices) {
                if (canClose(t, cut)) canBeClosed.put(t, cut);
				else System.err.println("CAN'T BE CLOSED: " + t + ", CV: " + cut);
            }
        }
		System.err.println("Can close " + canBeClosed);
        return canBeClosed;
    }

    private boolean canClose(Tuple t, Vertex cut) {
        return t.size() >= 3 && t.contains(cut);
    }

    private void setOriginalTupleStates(Tuple t, Vertex cut, List<List<Vertex>> components) {
        // Make sure we still have original model states
        Map<Integer, Character> originalStates = getOriginalStateMap(t, cut);
        for (List<Vertex> component : components) {
            for (Vertex v : component) {
                v.setState(originalStates.get(v.getLocation()));
            }
        }
    }

	private Map<Integer, Character> getOriginalStateMap(Tuple t, Vertex cut) {
        // Mapping of vertex locations to original states, for restoring after splicing
        Map<Integer, Character> originalStates = new HashMap<>();
        for (Vertex v : t.getVertices()) originalStates.put(v.getLocation(), v.getState());
        originalStates.put(cut.getLocation(), 'S'); // Only consider cut-vertices as susceptible
        return originalStates;
    }

    private void decomposeGraph() {
		// Add all tuples in required equations to required tuples field
        setRequiredTuples(this.getEquations().stream().map(Equation::getTuple).collect(Collectors.toList()));
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
            Map<Tuple, Integer> indices = new LinkedHashMap<>(); //  The mapping we will return
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
        if (requiredTuples == null || requiredTuples.isEmpty()){
            this.findEquations();
        }
//        this.requiredTuples.sort(null);
        return this.requiredTuples;
    }

    /**
     * @return the number of equations we are required to generate (i.e. the number of requiredTuples).
     */
    @Override
    public int getDimension() {
        return this.getTuples().size();
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
        for (Equation eqn : this.getEquations()) { // iterate over mappings of tuples to terms in their differential equations
            // Index of the equation (specifically, the tuple on the LHS of the equation)
            int index = getIndicesMapping().get(eqn.getTuple());
            // Iterate over tuples in the equation
            for (Tuple t : eqn.getTerms().keySet()) {
                for (double rate : eqn.getTerms().get(t)) {
                    try {
                        yDot[index] += (y[getIndicesMapping().get(t)] * rate);
                    } catch (NullPointerException e) {
                        // No mapping! Must be a closure term
                        List<Tuple> closed = eqn.getClosures().get(t);
						// TODO does this need to change after refactoring how closures work?
                        List<Vertex> cuts = new ArrayList<>(closed.get(0).getVertices());
                        cuts.removeAll(new ArrayList<>(closed.get(1).getVertices())); // Only ever two terms, so this is okay
                        Vertex cut = cuts.get(0);

                        double toAdd = rate;
                        for (Tuple term : eqn.getClosures().get(t)) {
                            try {
                                toAdd *= (y[getIndicesMapping().get(term)]);
                            } catch (NullPointerException e1) {
                                // No mapping here either! Genuine issue
                                System.err.println("Can't find index for " + term);
                            }
                        }
                        yDot[index] += toAdd / y[getIndicesMapping().get(new Tuple(new Vertex('S', cut.getLocation())))];
                    }
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
		    List<Tuple> required = new ArrayList<>(singles);
		    // Get equations for singles
		    eq = getEquationsForTuples(singles);

		    for (Graph graph : this.subGraphs) {
			    // Get equations for higher-order terms
			    for (Equation e : getNextTuples(graph, eq, 2, closures)) {
				    if (!eq.contains(e)) eq.add(e);
			    }
			    for (Equation equation : eq) {
				    Tuple tuple = equation.getTuple();
				    if (!required.contains(tuple)) required.add(tuple);
			    }
		    }
		    setRequiredTuples(required);
	    } else {
		    eq = getEquationsForTuples(requiredTuples);
	    }

	    return eq;
    }

	private List<Equation> getNextTuples(Graph graph, List<Equation> prevEquations, int length, boolean closures) {
        // Stop recurring if specified length is longer than the number of vertices in the graph
        if (length <= graph.getNumVertices()) {
            List<Tuple> nextSizeTuples = new ArrayList<>();
            // Go through previous equations and add any tuples of this
            // length for which we have not yet generated equations.
	        List<Tuple> canClose = new ArrayList<>();
            for (Equation eq : prevEquations) {
                for (Tuple t : eq.getTerms().keySet()) {
					Tuple compare = new Tuple(new ArrayList<>(Arrays.asList(new Vertex('I',1), new Vertex('S',2), new Vertex('S', 3))));
					if (t.equals(compare)) System.err.println("FOUND " + t);
                    if (t.size() == length) {
						if(!nextSizeTuples.contains(t)) {
							if (!closures) nextSizeTuples.add(t);
							else {
								// Swap t for closure terms
								if (!canClose.contains(t)) canClose.add(t);
								if (t.equals(compare)) System.err.println("Added " + t + " to close list");
							}
						}
                    }
                }
            }
	        System.err.println("Expecting to close " + canClose);
//			if (closures) nextSizeTuples.addAll(closeTuples(canClose, g.getCutVertices()));
	        // Equations for tuples that can't be closed
            List<Equation> equations = getEquationsForTuples(nextSizeTuples);
			if (closures) {
				List<Equation> closedEquations = (getEquationsForTuples(closeTuples(canClose, g.getCutVertices())));
				equations.addAll(closedEquations);
			}
            equations.addAll(getNextTuples(graph, equations, length+1, closures));
            return equations;
        } else return new ArrayList<>();
    }
}
