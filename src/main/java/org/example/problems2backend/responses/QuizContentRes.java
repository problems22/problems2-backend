package org.example.problems2backend.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuizContentRes
{
    private String questionId;
    private boolean correct;
}
