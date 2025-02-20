package org.example.problems2backend.controllers;


import lombok.AllArgsConstructor;
import org.example.problems2backend.models.QuizResult;
import org.example.problems2backend.models.User;
import org.example.problems2backend.requests.SubmitAnswerReq;
import org.example.problems2backend.requests.SubmitAnswersReq;
import org.example.problems2backend.responses.QuizDetailsRes;
import org.example.problems2backend.responses.QuizzesRes;
import org.example.problems2backend.responses.QuizzesStatsRes;
import org.example.problems2backend.service.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@AllArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping
    public ResponseEntity<QuizzesRes> getQuizzes(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) Integer withMinimumNumberOfQuestions,
            @RequestParam(required = false) Integer withMaximumNumberOfQuestions
    ) {

        QuizzesRes quizzes = quizService.getQuizzes(page, pageSize, searchTerm, difficulty, tags, withMinimumNumberOfQuestions, withMaximumNumberOfQuestions);
        return new ResponseEntity<>(quizzes, HttpStatus.OK);
    }

    @GetMapping("/stats")
    public ResponseEntity<QuizzesStatsRes> getQuizzesStats() {

        QuizzesStatsRes quizzesStats = quizService.getQuizzesStats();
        return new ResponseEntity<>(quizzesStats, HttpStatus.OK);
    }


    @GetMapping("/quiz/details/{quizId}")
    public ResponseEntity<QuizDetailsRes> getQuizDetails(@PathVariable String quizId) {
        QuizDetailsRes quizDetailsRes = quizService.getQuizDetails(quizId);
        return new ResponseEntity<>(quizDetailsRes, HttpStatus.OK);
    }



    @GetMapping("/quiz/result/{quizId}")
    public ResponseEntity<QuizResult> getQuizResult(@PathVariable String quizId, @AuthenticationPrincipal User user)
    {
        QuizResult quizResult = quizService.getQuizResult(quizId, user);
        return new ResponseEntity<>(quizResult, HttpStatus.OK);
    }

    @PostMapping("/quiz/start/{quizId}")
    public ResponseEntity<Void> startQuiz(@PathVariable String quizId, @AuthenticationPrincipal User user)
    {
        quizService.startQuiz();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/quiz/answers/submit/{quizId}")
    public ResponseEntity<Void> submitAnswers(@PathVariable String quizId, @RequestBody SubmitAnswersReq submitAnswersReq, @AuthenticationPrincipal User user)
    {
        quizService.submitAnswers(quizId, submitAnswersReq, user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/quiz/answer/submit/{quizId}")
    public ResponseEntity<Boolean> submitAnswer(@PathVariable String quizId, @RequestBody SubmitAnswerReq submitAnswerReq, @AuthenticationPrincipal User user)
    {
        return new ResponseEntity<>(quizService.submitAnswer(quizId, submitAnswerReq, user), HttpStatus.CREATED);
    }





}
