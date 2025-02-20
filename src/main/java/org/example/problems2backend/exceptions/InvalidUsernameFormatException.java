package org.example.problems2backend.exceptions;

public class InvalidUsernameFormatException
    extends CustomException
{
    public InvalidUsernameFormatException(String message) {
        super(message);
    }
}
