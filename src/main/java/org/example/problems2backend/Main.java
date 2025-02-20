package org.example.problems2backend;

import com.github.javafaker.Faker;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.problems2backend.models.Quiz;
import org.example.problems2backend.models.QuizResult;
import org.example.problems2backend.models.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    private static final Faker faker = new Faker();

    public static void main(String[] args) {
        String uri = "mongodb+srv://f10343002:IHgUAMcBWDJFWWgN@problems.ghky7.mongodb.net/";
        ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).build();
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .serverApi(serverApi)
                .build();

        try (MongoClient mongoClient = MongoClients.create(mongoClientSettings)) {
            MongoDatabase mongoDatabase = mongoClient.getDatabase("problems2-db");

            // Generate and insert fake data
            List<User> users = generateUsers(500);
            List<Quiz> quizzes = generateQuizzes(400);
            List<QuizResult> results = generateQuizResults(users, quizzes, 4000);

            insertData(mongoDatabase, users, quizzes, results);

        } catch (MongoException ex) {
            System.out.println(ex);
        }
    }

    private static List<User> generateUsers(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> User.builder()
                        .id(new ObjectId())
                        .username(faker.name().username())
                        .passwordHash(faker.crypto().md5())
                        .role(faker.random().nextBoolean() ? "USER" : "ADMIN")
                        .avatar(faker.internet().avatar())
                        .memberSince(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 365)))
                        .rankPoints(faker.number().numberBetween(0, 20000))
                        .isBanned(faker.random().nextDouble() < 0.05)
                        .stats(generateStats())
                        .weeklyPoints(faker.number().numberBetween(0, 1000))
                        .build())
                .peek(user -> user.setTitle(User.RankTitle.getTitleByPoints(user.getRankPoints())))
                .collect(Collectors.toList());
    }

    private static User.Stats generateStats() {
        int totalAttempts = faker.number().numberBetween(10, 1000);
        int correctAnswers = faker.number().numberBetween(0, totalAttempts);
        return User.Stats.builder()
                .totalAttempts(totalAttempts)
                .correctAnswers(correctAnswers)
                .incorrectAnswers(totalAttempts - correctAnswers)
                .build();
    }

    private static List<Quiz> generateQuizzes(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> Quiz.builder()
                        .id(new ObjectId())
                        .name(faker.programmingLanguage().name() + " Quiz")
                        .description(faker.lorem().paragraph())
                        .difficulty(faker.options().option("EASY", "MEDIUM", "HARD"))
                        .tags(generateTags())
                        .timeLimit(faker.number().numberBetween(5, 30))
                        .questions(generateQuestions())
                        .rules(faker.lorem().paragraph())
                        .instructions(faker.lorem().paragraph())
                        .build())
                .collect(Collectors.toList());
    }

    private static List<String> generateTags() {
        return IntStream.range(0, faker.number().numberBetween(1, 5))
                .mapToObj(i -> faker.programmingLanguage().name())
                .collect(Collectors.toList());
    }

    private static List<Quiz.Question> generateQuestions() {
        return IntStream.range(0, faker.number().numberBetween(5, 15))
                .mapToObj(i -> Quiz.Question.builder()
                        .id(UUID.randomUUID().toString())
                        .content(generateQuestionContent())
                        .build())
                .collect(Collectors.toList());
    }

    private static Quiz.Question.Content generateQuestionContent() {
        Quiz.Question.Content content = null;
        int contentType = faker.number().numberBetween(0, 3);

        content = switch (contentType) {
            case 0 -> generateMultipleChoice();
            case 1 -> generateFillInTheBlank();
            case 2 -> generateMultipleSelect();
            default -> content;
        };
        return content;
    }

    private static Quiz.Question.MultipleChoice generateMultipleChoice() {
        List<String> options = IntStream.range(0, 4)
                .mapToObj(i -> faker.lorem().sentence())
                .collect(Collectors.toList());

        return Quiz.Question.MultipleChoice.builder()
                .id(UUID.randomUUID().toString())
                .question(faker.lorem().sentence())
                .options(options)
                .correctOption(faker.number().numberBetween(0, options.size()))
                .build();
    }

    private static Quiz.Question.FillInTheBlank generateFillInTheBlank() {
        return Quiz.Question.FillInTheBlank.builder()
                .id(UUID.randomUUID().toString())
                .question(faker.lorem().sentence())
                .correctAnswer(faker.lorem().word())
                .build();
    }

    private static Quiz.Question.MultipleSelect generateMultipleSelect() {
        List<String> options = IntStream.range(0, 5)
                .mapToObj(i -> faker.lorem().sentence())
                .collect(Collectors.toList());

        List<Integer> correctOptions = IntStream.range(0, faker.number().numberBetween(1, 3))
                .map(i -> faker.number().numberBetween(0, options.size()))
                .boxed()
                .collect(Collectors.toList());

        return Quiz.Question.MultipleSelect.builder()
                .id(UUID.randomUUID().toString())
                .question(faker.lorem().sentence())
                .options(options)
                .correctOptions(correctOptions)
                .build();
    }

    private static List<QuizResult> generateQuizResults(List<User> users, List<Quiz> quizzes, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    User randomUser = users.get(faker.number().numberBetween(0, users.size()));
                    Quiz randomQuiz = quizzes.get(faker.number().numberBetween(0, quizzes.size()));

                    return QuizResult.builder()
                            .id(new ObjectId())
                            .userId(randomUser.getId())
                            .quizId(randomQuiz.getId())
                            .submissionDate(LocalDateTime.now().minusDays(faker.number().numberBetween(0, 30)))
                            .obtainedPoints(faker.number().numberBetween(0, 100))
                            .timeTaken(faker.number().numberBetween(1, randomQuiz.getTimeLimit()))
                            .content(generateQuizResultContent(randomQuiz))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private static List<QuizResult.Content> generateQuizResultContent(Quiz quiz) {
        return quiz.getQuestions().stream()
                .map(question -> QuizResult.Content.builder()
                        .questionId(question.getId())
                        .isCorrect(faker.random().nextBoolean())
                        .build())
                .collect(Collectors.toList());
    }

    private static void insertData(MongoDatabase database, List<User> users, List<Quiz> quizzes, List<QuizResult> results) {
        // Insert users
        List<Document> userDocuments = users.stream()
                .map(Main::convertUserToDocument)
                .collect(Collectors.toList());
        database.getCollection("users").insertMany(userDocuments);

        // Insert quizzes
        List<Document> quizDocuments = quizzes.stream()
                .map(Main::convertQuizToDocument)
                .collect(Collectors.toList());
        database.getCollection("quizzes").insertMany(quizDocuments);

        // Insert results
        List<Document> resultDocuments = results.stream()
                .map(Main::convertQuizResultToDocument)
                .collect(Collectors.toList());
        database.getCollection("quiz_results").insertMany(resultDocuments);
    }

    private static Document convertUserToDocument(User user) {
        return new Document()
                .append("_id", user.getId())
                .append("username", user.getUsername())
                .append("passwordHash", user.getPasswordHash())
                .append("role", user.getRole())
                .append("avatar", user.getAvatar())
                .append("memberSince", user.getMemberSince())
                .append("rankPoints", user.getRankPoints())
                .append("title", user.getTitle().name())
                .append("isBanned", user.getIsBanned())
                .append("stats", new Document()
                        .append("totalAttempts", user.getStats().getTotalAttempts())
                        .append("correctAnswers", user.getStats().getCorrectAnswers())
                        .append("incorrectAnswers", user.getStats().getIncorrectAnswers()))
                .append("weeklyPoints", user.getWeeklyPoints());
    }

    private static Document convertQuizToDocument(Quiz quiz) {
        return new Document()
                .append("_id", quiz.getId())
                .append("name", quiz.getName())
                .append("description", quiz.getDescription())
                .append("difficulty", quiz.getDifficulty())
                .append("tags", quiz.getTags())
                .append("timeLimit", quiz.getTimeLimit())
                .append("questions", convertQuestions(quiz.getQuestions()))
                .append("rules", quiz.getRules())
                .append("instructions", quiz.getInstructions());
    }

    private static List<Document> convertQuestions(List<Quiz.Question> questions) {
        return questions.stream()
                .map(q -> new Document()
                        .append("_id", q.getId())
                        .append("content", convertContent(q.getContent()))
                )
                .collect(Collectors.toList());
    }

    private static Document convertContent(Quiz.Question.Content content) {

        if (content instanceof Quiz.Question.MultipleChoice) {
            Quiz.Question.MultipleChoice mc = (Quiz.Question.MultipleChoice) content;
            return new Document()
                    .append("type", "MULTIPLE_CHOICE")
                    .append("id", mc.getId())
                    .append("question", mc.getQuestion())
                    .append("options", mc.getOptions())
                    .append("correctOption", mc.getCorrectOption());
        } else if (content instanceof Quiz.Question.FillInTheBlank) {
            Quiz.Question.FillInTheBlank fib = (Quiz.Question.FillInTheBlank) content;
            return new Document()
                    .append("type", "FILL_IN_THE_BLANK")
                    .append("id", fib.getId())
                    .append("question", fib.getQuestion())
                    .append("correctAnswer", fib.getCorrectAnswer());
        } else {
            Quiz.Question.MultipleSelect ms = (Quiz.Question.MultipleSelect) content;
            return new Document()
                    .append("type", "MULTIPLE_SELECT")
                    .append("id", ms.getId())
                    .append("question", ms.getQuestion())
                    .append("options", ms.getOptions())
                    .append("correctOptions", ms.getCorrectOptions());
        }

    }

    private static Document convertQuizResultToDocument(QuizResult result) {
        return new Document()
                .append("_id", result.getId())
                .append("userId", result.getUserId())
                .append("quizId", result.getQuizId())
                .append("submissionDate", result.getSubmissionDate())
                .append("obtainedPoints", result.getObtainedPoints())
                .append("timeTaken", result.getTimeTaken())
                .append("content", result.getContent().stream()
                        .map(c -> new Document()
                                .append("questionId", c.getQuestionId())
                                .append("isCorrect", c.isCorrect()))
                        .collect(Collectors.toList()));
    }
}