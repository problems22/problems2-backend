package org.example.problems2backend.responses;

import lombok.Builder;
import lombok.Data;
import org.example.problems2backend.models.Quiz;

import java.util.List;

@Data
@Builder
public class StartQuizRes {

    private List<QuestionRes> questionsWithoutCorrectAnswers;

}
