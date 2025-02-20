package org.example.problems2backend.requests;

import lombok.Builder;
import lombok.Data;
import org.example.problems2backend.models.Quiz;

@Data
@Builder
public class SubmitAnswerReq {
    Quiz.Question question;
}
