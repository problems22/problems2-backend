package org.example.problems2backend.exceptions;

public class InvalidRefreshTokenException
    extends CustomException
{
    public InvalidRefreshTokenException(String message) {
        super("refresh token " + message);
    }
}
