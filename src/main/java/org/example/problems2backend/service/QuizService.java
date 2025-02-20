package org.example.problems2backend.service;

import lombok.AllArgsConstructor;
import org.example.problems2backend.models.Quiz;
import org.example.problems2backend.models.QuizResult;
import org.example.problems2backend.models.User;
import org.example.problems2backend.repositories.QuizRepository;
import org.example.problems2backend.repositories.QuizResultRepository;
import org.example.problems2backend.requests.SubmitAnswerReq;
import org.example.problems2backend.requests.SubmitAnswersReq;
import org.example.problems2backend.responses.QuizDetailsRes;
import org.example.problems2backend.responses.QuizRes;
import org.example.problems2backend.responses.QuizzesStatsRes;
import org.example.problems2backend.responses.QuizzesRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final QuizResultRepository quizResultRepository;

    /**
     * This method will return quizzes with specified filters.
     * @param page from total pages to return quizzes starting from here.
     * @param pageSize is number of quiz data per page.
     * @param searchTerm in name and description as substring.
     * @param difficulty EASY, MEDIUM, HARD.
     * @param tags of which at least one of them should be in returning quiz document tags.
     * @param withMinimumNumberOfQuestions quiz document contains at least this many questions.
     * @param withMaximumNumberOfQuestions quiz document contains at most this many questions.
     * @return Quizzes matching above criteria. each quiz is of type QuizzRes.
     */
    public QuizzesRes getQuizzes(int page, int pageSize, String searchTerm, String difficulty,
                                 List<String> tags, Integer withMinimumNumberOfQuestions,
                                 Integer withMaximumNumberOfQuestions) {

        // Set default values for optional parameters
        String searchRegex = searchTerm != null ? searchTerm : "";
        String difficultyRegex = difficulty != null ? difficulty : "";
        List<String> tagsList = tags != null ? tags : new ArrayList<>();
        int minQuestions = withMinimumNumberOfQuestions != null ? withMinimumNumberOfQuestions : 0;
        int maxQuestions = withMaximumNumberOfQuestions != null ? withMaximumNumberOfQuestions : Integer.MAX_VALUE;
        boolean ignoreTags = tags == null || tags.isEmpty();

        // Create pageable object
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        // Get the page of quizzes
        Page<Quiz> quizPage = quizRepository.findQuizzesWithFilters(
                searchRegex,
                difficultyRegex,
                tagsList,
                minQuestions,
                maxQuestions,
                ignoreTags,
                pageable
        );

        // Convert Quiz entities to QuizRes DTOs
        List<QuizRes> quizResList = quizPage.getContent().stream()
                .map(quiz -> QuizRes.builder()
                        .id(quiz.getId().toString())
                        .name(quiz.getName())
                        .description(quiz.getDescription())
                        .difficulty(quiz.getDifficulty())
                        .tags(quiz.getTags())
                        .numberOfQuestions(quiz.getQuestions().size())
                        .timeLimit(quiz.getTimeLimit())
                        .build())
                .collect(Collectors.toList());

        // Build and return the response
        return QuizzesRes.builder()
                .quizzes(quizResList)
                .currentPage(page)
                .totalPages(quizPage.getTotalPages())
                .build();
    }


    /**
     * measurements about quizzes.
     * @return quiz stats.
     */
    public QuizzesStatsRes getQuizzesStats()
    {
        return null;
    }


    public QuizDetailsRes getQuizDetails(String quizId) {
        return null;
    }


    public QuizResult getQuizResult(String quizId, User user) {
        return null;
    }


    public void startQuiz() {

    }

    public Boolean submitAnswer(String quizId, SubmitAnswerReq submitAnswerReq, User user) {
        return null;
    }

    public void submitAnswers(String quizId, SubmitAnswersReq submitAnswersReq, User user) {

    }
}