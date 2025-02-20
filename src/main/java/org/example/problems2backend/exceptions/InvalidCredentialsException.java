package org.example.problems2backend.exceptions;

public class InvalidCredentialsException
    extends CustomException
{
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
