package org.example.problems2backend.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuizDetailsRes
{
    private QuizRes quiz;
    private String rules;
    private String instructions;

}
