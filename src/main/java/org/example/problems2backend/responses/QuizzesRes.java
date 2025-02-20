package org.example.problems2backend.responses;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuizzesRes
{
    List<QuizRes> quizzes;
    Integer currentPage;
    Integer totalPages;

}
