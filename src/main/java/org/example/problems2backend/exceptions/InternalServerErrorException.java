package org.example.problems2backend.exceptions;

public class InternalServerErrorException
extends CustomException
{
    public InternalServerErrorException(String message) {
        super(message);
    }
}
