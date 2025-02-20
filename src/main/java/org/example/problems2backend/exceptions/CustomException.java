package org.example.problems2backend.exceptions;

import lombok.Data;

@Data
public abstract class CustomException
    extends RuntimeException
{
    public CustomException(String message) {
        super(message);
    }

}
