package io.github.ethankelly.model;

import io.github.ethankelly.exceptions.UnexpectedStateException;

import java.util.Arrays;
import java.util.List;

public class Model {
	private List<Character> states;
	private boolean[][] transitionMatrix;
	private double[][] ratesMatrix;
	// Describes whether each transition can happen alone (false), e.g. recovery from
	// disease, or requires a second individual, such as transmission of disease (true)
	private final boolean[] requiresPair;

	public Model(List<Character> states, boolean[] requiresPair) {
		this.states = states;
		this.requiresPair = requiresPair;
		this.transitionMatrix = new boolean[states.size()][states.size()];
		this.ratesMatrix = new double[states.size()][states.size()];
	}

	public static void main(String[] args) {
		// For now, every element in transmission matrix is equal to the relevant rate (can change based on context)
		Model m = new Model(Arrays.asList('S', 'I', 'R'), new boolean[] {true, false, false});
		m.addTransition('S', 'I', 0.6);
		m.addTransition('I', 'R', 0.1);
		System.out.println(m);
	}

	public boolean[] getRequiresPair() {
		return requiresPair;
	}

	public double[][] getRatesMatrix() {
		return ratesMatrix;
	}

	public void setRatesMatrix(double[][] ratesMatrix) {
		this.ratesMatrix = ratesMatrix;
	}

	public List<Character> getStates() {
		return states;
	}

	public void setStates(List<Character> states) {
		this.states = states;
	}

	public void addTransition(char from, char to, double rate) {
		assert !String.valueOf(from).equals(String.valueOf(to)) :
				"The 'to' and 'from' characters should not have been the same, but were: " + to + " = " + from;

		boolean[][] transitions = this.getTransitionMatrix().clone();
		double[][] rates = this.getRatesMatrix().clone();

		int indexFrom = -1;
		int indexTo = -1;
		for (int i = 0; i < this.getStates().size(); i++) {
			if (this.getStates().get(i) == from) indexFrom = i;
			if (this.getStates().get(i) == to) indexTo = i;
		}

		if (indexFrom < 0 || indexTo < 0) {
			try {
				throw new UnexpectedStateException("One (or both) of the 'to'/'from' states {" + to + ", " + from
				                                   + "} could not be found in the array of states.");
			} catch (UnexpectedStateException e) {
				e.printStackTrace();
			}
		} else {
			transitions[indexFrom][indexTo] = true;
			rates[indexFrom][indexTo] = rate;
		}
		this.setTransitionMatrix(transitions);
		this.setRatesMatrix(rates);
	}

	public boolean[][] getTransitionMatrix() {
		return transitionMatrix;
	}

	public void setTransitionMatrix(boolean[][] transitionMatrix) {
		this.transitionMatrix = transitionMatrix;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		StringBuilder t = new StringBuilder();
		s.append(" ");
		t.append(" ");
		for (char c : states) {
			s.append(" ").append(c);
			t.append(" ").append(c);
		}
		for (int i = 0; i < this.getTransitionMatrix().length; i++) {
			s.append("\n").append(this.getStates().get(i));
			t.append("\n").append(this.getStates().get(i));
			for (int j = 0; j < this.getTransitionMatrix()[0].length; j++) {
				s.append(" ").append(this.getTransitionMatrix()[i][j]);
				t.append(" ").append(this.getRatesMatrix()[i][j]);
			}
		}
		s.append("\n\n").append(t);
		return String.valueOf(s);
	}
}
