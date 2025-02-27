package org.example.problems2backend.responses;


import lombok.Builder;
import lombok.Data;
import org.example.problems2backend.models.QuizResult;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class QuizResultRes
{

    private String quizId;
    private String userId;
    private String quizName;
    private Integer timeTaken;
    private List<QuizContentRes> content;
    private int obtainedPoints;
    private Double averageTimeTaken;
    private Double averageObtainedPoints;
    private LocalDateTime submissionDate;

}
