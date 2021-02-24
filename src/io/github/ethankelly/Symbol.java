package io.github.ethankelly;

public enum Symbol {

    LANGLE("\u3008"),
    RANGLE("\u3009");

    private final String uni;

    Symbol(String uni) {
        this.uni = uni;
    }

    /**
     * Each symbol has a String unicode value associated to it, which is used to print it to the standard output.
     *
     * @return the value of the letter.
     */
    public String uni() {
        return this.uni;
    }
}
