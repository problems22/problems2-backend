package org.example.problems2backend.controllers;


import lombok.AllArgsConstructor;
import org.example.problems2backend.models.User;
import org.example.problems2backend.requests.SubmitAnswersReq;
import org.example.problems2backend.responses.*;
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


    @PostMapping("/quiz/start/{quizId}")
    public ResponseEntity<StartQuizRes> startQuiz(@PathVariable String quizId, @AuthenticationPrincipal User user)
    {
        StartQuizRes startQuizRes = quizService.startQuiz(quizId, user);
        return new ResponseEntity<>(startQuizRes, HttpStatus.CREATED);
    }


    @PostMapping("/quiz/stop/{quizId}")
    public ResponseEntity<Void> stopQuiz(@PathVariable String quizId, @AuthenticationPrincipal User user)
    {
        quizService.stopQuiz(quizId, user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @PostMapping("/quiz/answer/submit/{quizId}")
    public ResponseEntity<QuizResultRes> submitAnswer(@PathVariable String quizId, @RequestBody SubmitAnswersReq submitAnswerReq, @AuthenticationPrincipal User user)
    {
        return new ResponseEntity<>(quizService.submitAnswer(quizId, submitAnswerReq, user), HttpStatus.CREATED);
    }

    @GetMapping("/quiz/questions/{quizId}")
    public ResponseEntity<StartQuizRes> getQuestions(@PathVariable String quizId)
    {
        StartQuizRes startQuizRes = quizService.getQuestions(quizId);
        return new ResponseEntity<>(startQuizRes, HttpStatus.OK);
    }



}
