package org.example.problems2backend.responses;


import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class QuizRes
{

    private String id;
    private String name;
    private String description;
    private String difficulty;
    private List<String> tags;
    private Integer numberOfQuestions;
    private Integer timeLimit;

}
