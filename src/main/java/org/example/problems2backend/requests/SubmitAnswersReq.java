package org.example.problems2backend.requests;

import lombok.Builder;
import lombok.Data;
import org.example.problems2backend.models.Quiz;

import java.util.List;

@Data
@Builder
public class SubmitAnswersReq
{
    List<Quiz.Question> questions;


}
