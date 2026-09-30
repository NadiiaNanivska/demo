package com.example.demo.exceptions;

public class InvalidUserSearchException extends RuntimeException {
    public InvalidUserSearchException() {
        super("Invalid user search. Check the filter, fields, values, sort, and date range.");
    }
}
