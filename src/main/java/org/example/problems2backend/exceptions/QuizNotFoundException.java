package org.example.problems2backend.exceptions;

public class QuizNotFoundException
    extends CustomException
{

    public QuizNotFoundException(String message) {
        super(message);
    }
}
