package org.example.api.exceptions;

public class UrlNotFoundException
        extends RuntimeException {

    public UrlNotFoundException(String alias) {
        super("URL não encontrada para o alias '" + alias + "'");
    }
}
