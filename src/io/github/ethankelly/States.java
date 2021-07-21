package io.github.ethankelly;

public enum States {
    si(new char[]{'I', 'S'}),
    sir(new char[]{'I', 'R', 'S'}),
    sip(new char[]{'I', 'P', 'S'}),
    sirp(new char[]{'I', 'P', 'R', 'S'});

    private final char[] states;

    States(char[] states) {
        this.states = states;
    }

    public char[] states() {
        return this.states;
    }
}
