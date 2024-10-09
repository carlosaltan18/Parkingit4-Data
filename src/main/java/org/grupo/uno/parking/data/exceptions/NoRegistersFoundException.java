package org.grupo.uno.parking.data.exceptions;

public class NoRegistersFoundException extends RuntimeException{
    public NoRegistersFoundException(String message) {
        super(message);
    }
}
