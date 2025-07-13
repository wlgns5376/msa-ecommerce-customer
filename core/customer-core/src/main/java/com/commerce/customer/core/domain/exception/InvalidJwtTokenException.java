package com.commerce.customer.core.domain.exception;

public class InvalidJwtTokenException extends JwtTokenException {
    public InvalidJwtTokenException(String message) {
        super(message);
    }

    public InvalidJwtTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}