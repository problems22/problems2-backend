package org.example.problems2backend.exceptions;

public class InvalidAccessTokenException
    extends CustomException
{
    public InvalidAccessTokenException(String message) {
        super(message);
    }
}