package com.commerce.customer.core.domain.exception;

public class ExpiredJwtTokenException extends JwtTokenException {
    public ExpiredJwtTokenException(String message) {
        super(message);
    }

    public ExpiredJwtTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}