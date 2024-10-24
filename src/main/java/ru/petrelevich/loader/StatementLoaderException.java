package ru.petrelevich.loader;

public class StatementLoaderException extends RuntimeException {
    public StatementLoaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public StatementLoaderException(String message) {
        super(message);
    }
}
