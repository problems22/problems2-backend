package org.example.problems2backend.responses;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class QuizzesStatsRes {
    Long totalQuizzes;
    Long totalQuestions;
    Long totalEasyQuizzes;
    Long totalMediumQuizzes;
    Long totalHardQuizzes;
    Map<String, Long> totalTags; // all available tags alongside frequency of each through all quizzes.
}
