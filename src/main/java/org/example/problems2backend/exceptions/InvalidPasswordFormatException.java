package org.example.problems2backend.exceptions;

public class InvalidPasswordFormatException
    extends CustomException
{
    public InvalidPasswordFormatException(String message) {
        super("password must contain " + message);
    }

}
