package org.grupo.uno.parking.data.configuration;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String PROPERTY = "property";

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleSecurityException(Exception exception) {
        ProblemDetail errorDetail = null;

        // Log de la excepción
        logger.error("Exception caught: ", exception);

        if (exception instanceof BadCredentialsException) {
            logger.warn("Handling BadCredentialsException");
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
            errorDetail.setProperty(PROPERTY, "The username or password is incorrect");
        } else if (exception instanceof AccountStatusException) {
            logger.warn("Handling AccountStatusException");
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
            errorDetail.setProperty(PROPERTY, "The account is locked");
        } else if (exception instanceof AccessDeniedException) {
            logger.warn("Handling AccessDeniedException");
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
            errorDetail.setProperty(PROPERTY, "You are not authorized to access this resource");
        } else if (exception instanceof SignatureException) {
            logger.warn("Handling SignatureException");
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
            errorDetail.setProperty(PROPERTY, "The JWT signature is invalid");
        } else if (exception instanceof ExpiredJwtException) {
            logger.warn("Handling ExpiredJwtException");
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
            errorDetail.setProperty(PROPERTY, "The JWT token has expired");
        } else {
            logger.error("Unknown exception caught");
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
            errorDetail.setProperty(PROPERTY, "Unknown internal server error.");
        }

        return errorDetail;
    }
}