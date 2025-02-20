package org.example.problems2backend.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler
{
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleException(CustomException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("error", ex.getClass().getSimpleName());
        response.put("message", ex.getMessage());

        var currentExceptionClass = ex.getClass();
        // exceptions that require BAD_REQUEST
        Set<Class<?>> badRequestEx = Set.of(

        );

        Set<Class<?>> unauthorizedRequestEx = Set.of(
                InvalidCredentialsException.class,
                InvalidRefreshTokenException.class,
                InvalidAccessTokenException.class
        );


        Set<Class<?>> unprocessableEntityEx = Set.of(
                InvalidPasswordFormatException.class,
                InvalidUsernameFormatException.class
        );



        // TODO: add other exception sets

        if (badRequestEx.contains(currentExceptionClass))
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        else if (unauthorizedRequestEx.contains(currentExceptionClass))
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        else if (unprocessableEntityEx.contains(currentExceptionClass))
            return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
        // TODO: add more else statements


        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }




}
