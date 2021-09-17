package io.github.ethankelly.exceptions;

public class UnexpectedStateException extends Exception {
    public UnexpectedStateException(String errorMessage) {
        super(errorMessage);
    }
}
