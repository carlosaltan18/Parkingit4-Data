package org.grupo.uno.parking.data.exception;

import org.springframework.dao.DataAccessException;

public class AllDataRequiredException extends DataAccessException {
    public AllDataRequiredException(String message) {
        super(message);
    }
}
