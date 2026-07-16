package org.example.api.exceptions;

public class AliasAlreadyExistsException
        extends RuntimeException {

    public AliasAlreadyExistsException(String alias) {
        super("O alias '" + alias + "' já está em uso");
    }
}
