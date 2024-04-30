package com.example.demo.exceptions;

public class InvalidAgeException extends RuntimeException{

    public InvalidAgeException(String message) {
        super(message);
    }
}
