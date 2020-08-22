package com.vanivskyi.test.exception;

public class InvalidUserRegistrationDataException extends RuntimeException {

    public InvalidUserRegistrationDataException(String message) {
        super(message);
    }
}