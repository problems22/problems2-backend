package org.example.problems2backend.service;

import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.example.problems2backend.exceptions.*;
import org.example.problems2backend.models.QuizResult;
import org.example.problems2backend.models.User;
import org.example.problems2backend.repositories.QuizRepository;
import org.example.problems2backend.repositories.QuizResultRepository;
import org.example.problems2backend.repositories.UserRepository;
import org.example.problems2backend.repositories.projections.DifficultyCountProjection;
import org.example.problems2backend.repositories.projections.QuizAverageResultProjection;
import org.example.problems2backend.repositories.projections.TagCountProjection;
import org.example.problems2backend.requests.AnswerReq;
import org.example.problems2backend.requests.SubmitAnswersReq;
import org.example.problems2backend.responses.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.example.problems2backend.models.Quiz;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;
    private final ConcurrentHashMap<String, LocalDateTime> userQuizEndsIn = new ConcurrentHashMap<>();
    private final MongoTemplate mongoTemplate;


    @Scheduled(cron = "0 0 0 * * MON") //
    // every Monday at midnight
    public void clearWeeklyPoints() {
        mongoTemplate.updateMulti(
                new Query(), // Creates an empty query to match all documents
                new Update().set("weeklyPoints", 0), // Sets the weeklyPoints field to 0
                User.class
        );
    }

    @Scheduled(fixedRate = 300_000) // once 5 minutes
    public void clearEndedQuizzes() {
        LocalDateTime now = LocalDateTime.now();
        for (String username : userQuizEndsIn.keySet())
            userQuizEndsIn.computeIfPresent(username, (k, v) -> v.isBefore(now) ? null : v);
    }



    public QuizzesRes getQuizzes(int page, int pageSize, String searchTerm, String difficulty,
          List<String> tags, Integer withMinimumNumberOfQuestions, Integer withMaximumNumberOfQuestions) {
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
    public QuizzesStatsRes getQuizzesStats() {
        // Count total quizzes
        long totalQuizzes = quizRepository.countTotalQuizzes();

        // Count total questions
        long totalQuestions = quizRepository.countTotalQuestions();

        // Count quizzes by difficulty
        Map<String, Long> quizzesByDifficulty = quizRepository.countQuizzesByDifficulty().stream()
                .collect(Collectors.toMap(DifficultyCountProjection::getDifficulty, DifficultyCountProjection::getCount));

        // Count tags and their frequencies
        Map<String, Long> totalTags = quizRepository.countTags().stream()
                .collect(Collectors.toMap(TagCountProjection::get_id, TagCountProjection::getCount));

        // Build and return the response
        return QuizzesStatsRes.builder()
                .totalQuizzes(totalQuizzes)
                .totalQuestions(totalQuestions)
                .totalEasyQuizzes(quizzesByDifficulty.getOrDefault("EASY", 0L))
                .totalMediumQuizzes(quizzesByDifficulty.getOrDefault("MEDIUM", 0L))
                .totalHardQuizzes(quizzesByDifficulty.getOrDefault("HARD", 0L))
                .totalTags(totalTags)
                .build();
    }


    public QuizDetailsRes getQuizDetails(String quizId) {
        // Fetch the quiz from the repository
        Quiz quiz = quizRepository.findQuizById(quizId).orElseThrow(() -> new QuizNotFoundException("quiz not found"));

        // Map the Quiz entity to QuizRes
        QuizRes quizRes = QuizRes.builder()
                .id(quiz.getId().toString())
                .name(quiz.getName())
                .description(quiz.getDescription())
                .difficulty(quiz.getDifficulty())
                .tags(quiz.getTags())
                .numberOfQuestions(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0)
                .timeLimit(quiz.getTimeLimit())
                .build();

        // Build the QuizDetailsRes response object
        return QuizDetailsRes.builder()
                .quiz(quizRes)
                .rules(quiz.getRules())
                .instructions(quiz.getInstructions())
                .build();

    }




    public StartQuizRes startQuiz(String quizId, User user) {
        if (userQuizEndsIn.containsKey(user.getUsername()) &&
                userQuizEndsIn.getOrDefault(user.getUsername(), LocalDateTime.MIN).isAfter(LocalDateTime.now())) {
            throw new InvalidQuizStateException("can't start quiz, please stop or submit previous quiz");
        }

        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new QuizNotFoundException("quiz not found"));

        userQuizEndsIn.put(user.getUsername(), LocalDateTime.now().plusMinutes(quiz.getTimeLimit()));

        List<QuestionRes> questions = quiz.getQuestions().stream()
                .map(this::sanitizeQuestion)
                .toList();

        return StartQuizRes.builder()
                .questionsWithoutCorrectAnswers(questions)
                .build();
    }


    private QuestionRes sanitizeQuestion(Quiz.Question question) {
        QuestionRes sanitizedQuestion = QuestionRes.builder().build();
        sanitizedQuestion.setId(question.getId().toString());

        // Sanitize the content based on its type
        if (question.getContent() instanceof Quiz.Question.MultipleChoice) {
            sanitizedQuestion.setContent(sanitizeMultipleChoice(question.getContent()));
        } else if (question.getContent() instanceof Quiz.Question.FillInTheBlank) {
            sanitizedQuestion.setContent(sanitizeFillInTheBlank(question.getContent()));
        } else if (question.getContent() instanceof Quiz.Question.MultipleSelect) {
            sanitizedQuestion.setContent(sanitizeMultipleSelect(question.getContent()));
        }

        return sanitizedQuestion;
    }
    private ContentRes sanitizeMultipleChoice(Quiz.Question.Content multipleChoice) {
        return ContentRes.builder()
                .id(multipleChoice.getId().toString())
                .type("MULTIPLE_CHOICE")
                .question(multipleChoice.getQuestion())
                .options(multipleChoice.getOptions())
                .build();
    }
    private ContentRes sanitizeFillInTheBlank(Quiz.Question.Content fillInTheBlank) {
        return ContentRes.builder()
                .id(fillInTheBlank.getId().toString())
                .type("FILL_IN_THE_BLANK")
                .question(fillInTheBlank.getQuestion())
                .build();
    }
    private ContentRes sanitizeMultipleSelect(Quiz.Question.Content multipleSelect) {
        return ContentRes.builder()
                .id(multipleSelect.getId().toString())
                .type("MULTIPLE_SELECT")
                .question(multipleSelect.getQuestion())
                .options(multipleSelect.getOptions())
                .build();
    }


    public void stopQuiz(String quizId, User user)
    {
        if (!quizRepository.existsById(quizId))
            throw new QuizNotFoundException("quiz not found to stop");
        if (!userQuizEndsIn.containsKey(user.getUsername()) || userQuizEndsIn.getOrDefault(user.getUsername(), LocalDateTime.MIN).isBefore(LocalDateTime.now()))
        {
            throw new InvalidQuizStateException("can't stop quiz, it's not started at all or ended already");
        }
        userQuizEndsIn.remove(user.getUsername());

    }





    public QuizResultRes submitAnswer(String quizId, SubmitAnswersReq submitReq, User user) {
        LocalDateTime submissionDate = LocalDateTime.now();
        if (!userQuizEndsIn.containsKey(user.getUsername()) || userQuizEndsIn.getOrDefault(user.getUsername(), LocalDateTime.MIN).isBefore(LocalDateTime.now())) {
            throw new InvalidQuizStateException("can't submit quiz, it's not started at all or ended already");
        }

        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new QuizNotFoundException("can't submit answer for quiz that is not found"));
        Integer timeTaken = Math.toIntExact(Duration.between(userQuizEndsIn.getOrDefault(user.getUsername(), LocalDateTime.MIN).minusMinutes(quiz.getTimeLimit()), submissionDate).getSeconds());

        if (quiz.getQuestions().size() != submitReq.getAnswers().size()) {
            throw new InvalidAnswerFormatException("there is more or less answers than questions");
        }

        // Initialize obtainedPoints to 0
        int obtainedPoints = 0;

        // Calculate points per question based on difficulty
        int pointsPerQuestion = switch (quiz.getDifficulty()) {
            case "HARD" -> 45 / quiz.getQuestions().size();
            case "MEDIUM" -> 30 / quiz.getQuestions().size();
            default -> 20 / quiz.getQuestions().size();
        };

        QuizResult quizResult = QuizResult
                .builder()
                .quizId(new ObjectId(quizId))
                .userId(user.getId())
                .timeTaken(timeTaken)
                .content(new ArrayList<>())
                .obtainedPoints(obtainedPoints)
                .build();

        int countCorrect = 0;
        int countIncorrect = 0;
        for (int i = 0; i < quiz.getQuestions().size(); i++) {
            AnswerReq answerI = submitReq.getAnswers().get(i);
            Quiz.Question questionI = quiz.getQuestions().get(i);
            if (!questionI.getId().toString().equals(answerI.getQuestionId())) {
                throw new InvalidAnswerFormatException("answer id didn't match question id");
            }

            boolean isCorrect = isCorrect(questionI, answerI);

            quizResult.getContent().add(QuizResult.Content
                    .builder()
                    .questionId(questionI.getId())
                    .isCorrect(isCorrect)
                    .build());

            // Update obtainedPoints based on correctness
            if (isCorrect) {
                obtainedPoints += pointsPerQuestion;
                countCorrect++;
            } else {
                obtainedPoints -= pointsPerQuestion; // Deduct points for incorrect answers
                countIncorrect++;
            }
        }

        userRepository.incrementStatsByUsername(user.getUsername(), 1, countCorrect, countIncorrect);
        // Ensure obtainedPoints is not negative
        obtainedPoints = Math.max(obtainedPoints, 0);
        userRepository.incrementRankWeeklyPointsAndTotalAttempts(user.getUsername(), obtainedPoints, obtainedPoints);

        quizResult.setObtainedPoints(obtainedPoints);
        quizResult.setSubmissionDate(submissionDate);
        quizResultRepository.save(quizResult);
        stopQuiz(quizId, user);

        QuizAverageResultProjection averageRes =  quizResultRepository.findTotalAverageResultsByQuizId(quizId).orElseThrow(() -> new QuizNotFoundException("quiz not found"));

        return QuizResultRes.builder()
                .quizId(quizId)
                .userId(user.getId().toString())
                .content(quizResult.getContent().stream().map(e -> QuizContentRes.builder().questionId(e.getQuestionId().toString()).correct(e.isCorrect()).build()).toList())
                .obtainedPoints(quizResult.getObtainedPoints())
                .averageTimeTaken(averageRes.getAverageTimeTaken())
                .averageObtainedPoints(averageRes.getAverageObtainedPoints())
                .timeTaken(timeTaken)
                .submissionDate(submissionDate)
                .build();
    }

    private boolean isCorrect(Quiz.Question questionI, AnswerReq answerI) {
        boolean isCorrect = false;

        if (questionI.getContent() instanceof Quiz.Question.MultipleChoice) {
            isCorrect = questionI.getContent().getCorrectOption().equals(answerI.getSelectedOption());
        } else if (questionI.getContent() instanceof Quiz.Question.FillInTheBlank) {
            isCorrect = questionI.getContent().getCorrectAnswer().equals(answerI.getCorrectAnswer());
        } else if (questionI.getContent() instanceof Quiz.Question.MultipleSelect) {
            isCorrect = questionI.getContent().getCorrectOptions().equals(answerI.getCorrectOptions());
        }
        return isCorrect;
    }

    public StartQuizRes getQuestions(String quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new QuizNotFoundException("quiz not found"));

        List<QuestionRes> questions = quiz.getQuestions().stream()
                .map(this::sanitizeQuestion)
                .toList();

        return StartQuizRes.builder()
                .questionsWithoutCorrectAnswers(questions)
                .build();
    }
}