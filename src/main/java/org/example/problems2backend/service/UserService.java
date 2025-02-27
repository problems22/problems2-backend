package org.example.problems2backend.service;

import lombok.RequiredArgsConstructor;
import org.example.problems2backend.exceptions.*;
import org.example.problems2backend.models.QuizResult;
import org.example.problems2backend.models.User;
import org.example.problems2backend.repositories.QuizRepository;
import org.example.problems2backend.repositories.QuizResultRepository;
import org.example.problems2backend.repositories.UserRepository;
import org.example.problems2backend.repositories.projections.UserPointsProjection;
import org.example.problems2backend.repositories.projections.UserRankProjection;
import org.example.problems2backend.repositories.projections.UserWeeklyRankProject;
import org.example.problems2backend.responses.LeaderboardRes;
import org.example.problems2backend.responses.AuthRes;
import org.example.problems2backend.responses.QuizContentRes;
import org.example.problems2backend.responses.QuizResultRes;
import org.example.problems2backend.responses.UserProfileRes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService
{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final QuizResultRepository quizResultRepository;
    private final QuizRepository quizRepository;

    public AuthRes register(String username, String password) {

        if (username == null)
            throw new InvalidUsernameFormatException("username can't be null");

        if (password == null)
            throw new InvalidPasswordFormatException("password can't be null");


        if (username.isBlank())
            throw new InvalidUsernameFormatException("username can't be blank");

        if (username.length() > 20)
            throw new InvalidUsernameFormatException("maximum 20 characters for username");

        if (userRepository.existsByUsername(username))
            throw new InvalidCredentialsException("username already taken");

        validatePasswordFormat(password);

        User user = User
                .builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password)) // encoded password
                .avatar("https://api.dicebear.com/8.x/pixel-art/png?seed=" + username) // randomly generated with seed
                .build();

        userRepository.save(user);

        // generate jwt for the user
        return AuthRes
                .builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();

    }


    public void validatePasswordFormat(String password)
    {
        // validate password format
        String password_regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$";
        if (!Pattern.compile(password_regex).matcher(password).matches()) {
            // Determine which requirement failed and throw appropriate message
            if (!password.matches(".*[A-Z].*"))
                throw new InvalidPasswordFormatException("at least one uppercase character");
            if (!password.matches(".*[a-z].*"))
                throw new InvalidPasswordFormatException("at least one lowercase letter");
            if (!password.matches(".*\\d.*"))
                throw new InvalidPasswordFormatException("at least one digit");
            if (password.length() < 8)
                throw new InvalidPasswordFormatException("minimum 8 characters");
            if (password.length() > 20)
                throw new InvalidPasswordFormatException("password too long");
        }
    }


    public AuthRes login(String username, String password) {

        if (username == null)
            throw new InvalidUsernameFormatException("username can't be null");

        if (password == null)
            throw new InvalidPasswordFormatException("password can't be null");


        if (username.isBlank())
            throw new InvalidUsernameFormatException("username can't be blank");

        if (!userRepository.existsByUsername(username))
            throw new InvalidCredentialsException("username doesn't exist");

        User user = userRepository.findByUsername(username).get();

        // verify password
        if (!passwordEncoder.matches(password, user.getPasswordHash()))
            throw new InvalidCredentialsException("wrong password");

        return AuthRes
                .builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();


    }


    public AuthRes refreshToken(String accessToken, String refreshToken)
    {
        if (accessToken == null)
            throw new InvalidAccessTokenException("access token can't be null");

        if (refreshToken == null)
            throw new InvalidRefreshTokenException("refresh token can't be null");

        String username = jwtService.extractUsername(refreshToken, false);

        if (username == null) {
            throw new InvalidRefreshTokenException("is invalid");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidRefreshTokenException("username not found"));

        if (jwtService.isTokenExpired(refreshToken, false))
        {
            throw new InvalidRefreshTokenException("refresh token has expired, please login again");
        }

        if (!jwtService.isTokenValid(refreshToken, user, false))
        {
            throw new InvalidRefreshTokenException("token not valid");
        }

        if (jwtService.isTokenValid(accessToken, user, true)) {

            return AuthRes.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        }
        else
        {
            return AuthRes.builder()
                    .accessToken(jwtService.generateAccessToken(user))
                    .refreshToken(refreshToken)
                    .build();
        }

    }


    public void changePassword(String username, String oldPassword, String newPassword)
    {
        if (username == null)
            throw new InvalidUsernameFormatException("username can't be null");

        if (oldPassword == null)
            throw new InvalidPasswordFormatException("old password can't be null");

        if (newPassword == null)
            throw new InvalidPasswordFormatException("new password can't be null");


        if (!userRepository.existsByUsername(username))
            throw new InvalidCredentialsException("username doesn't exist");

        String passwordHash = userRepository.findPasswordHashByUsername(username).get().getPasswordHash();

        System.out.println(passwordHash);

        // verify password
        if (!passwordEncoder.matches(oldPassword, passwordHash))
            throw new InvalidCredentialsException("wrong password");

        validatePasswordFormat(newPassword);

        userRepository.updatePasswordHashByUsername(username, passwordEncoder.encode(newPassword));

    }


    public UserProfileRes getUserProfile(String username) {
        // Fetch basic user information
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("username not found"));

        UserProfileRes userProfileRes = UserProfileRes.builder().build();

        userProfileRes.setUsername(user.getUsername());
        userProfileRes.setRole(user.getRole());
        userProfileRes.setAvatar(user.getAvatar());
        userProfileRes.setMemberSince(user.getMemberSince());
        userProfileRes.setRankPoints(user.getRankPoints());
        userProfileRes.setRankTitle(user.getTitle());
        userProfileRes.setIsBanned(user.getIsBanned());
        userProfileRes.setStats(user.getStats());
        userProfileRes.setWeeklyPoints(user.getWeeklyPoints());

        // Fetch quiz results for the user
        List<QuizResult> quizResults = quizResultRepository.findByUserIdOrderBySubmissionDateDesc(user.getId(), PageRequest.of(0, 10));
        userProfileRes.setRecentResults(quizResults.stream().map(el -> QuizResultRes.builder()
                .quizId(el.getQuizId().toString())
                .userId(el.getUserId().toString())
                .timeTaken(el.getTimeTaken())
                .content(el.getContent().stream().map(e -> QuizContentRes.builder()
                        .questionId(e.getQuestionId().toString())
                        .correct(e.isCorrect())
                        .build()).toList())
                .obtainedPoints(el.getObtainedPoints())
                .submissionDate(el.getSubmissionDate())
                .build()).toList());

        // Calculate average points per quiz
        double averagePointsPerQuiz = quizResults.isEmpty() ? 0 : (double) (userProfileRes.getRankPoints()) / quizResults.size();
        userProfileRes.setAveragePointsPerQuiz(averagePointsPerQuiz);

        // Calculate total time taken for all quizzes
        int totalTimeTaken = quizResults.stream().mapToInt(QuizResult::getTimeTaken).sum();
        userProfileRes.setTotalTimeTaken(totalTimeTaken);

        // Calculate average time taken per quiz
        double averageTimeTaken = quizResults.isEmpty() ? 0 : (double) totalTimeTaken / quizResults.size();
        userProfileRes.setAverageTimeTaken(averageTimeTaken);

        // Calculate the number of correct and incorrect answers
        int totalCorrectAnswers = user.getStats().getCorrectAnswers();
        int totalIncorrectAnswers = user.getStats().getIncorrectAnswers();

        // Calculate the accuracy rate
        double accuracyRate = (totalCorrectAnswers + totalIncorrectAnswers) == 0 ? 0 :
                (double) totalCorrectAnswers / (totalCorrectAnswers + totalIncorrectAnswers) * 100;
        userProfileRes.setAccuracyRate(accuracyRate);

        // Calculate the number of quizzes taken by difficulty level
        Map<String, Long> quizzesByDifficulty = quizResults.stream()
                .collect(Collectors.groupingBy(result -> quizRepository.findDifficultyById(result.getQuizId().toString()).getDifficulty(), Collectors.counting()));
        userProfileRes.setQuizzesByDifficulty(quizzesByDifficulty);


        // Calculate the number of quizzes taken over time
        Map<LocalDateTime, Long> quizzesOverTime = quizResults.stream()
                .collect(Collectors.groupingBy(QuizResult::getSubmissionDate, Collectors.counting()));
        userProfileRes.setQuizzesOverTime(quizzesOverTime);

        // Calculate the user's rank title based on total points
        User.RankTitle rankTitle = User.RankTitle.getTitleByPoints(user.getRankPoints());
        userProfileRes.setRankTitle(rankTitle);

        // Calculate the progress towards the next rank
        int nextRankPoints = rankTitle.ordinal() < User.RankTitle.values().length - 1 ?
                User.RankTitle.values()[rankTitle.ordinal() + 1].getRequiredPoints() : rankTitle.getRequiredPoints();
        userProfileRes.setNextRankPoints(nextRankPoints);

        int progressTowardsNextRank = Math.max(0, nextRankPoints - user.getRankPoints());
        userProfileRes.setProgressTowardsNextRank(progressTowardsNextRank);

        return userProfileRes;
    }

    public LeaderboardRes getLeaderboard(User user)
    {

        Map<String, Integer> rank = userRepository.findAllUsersWithRank().stream().collect(Collectors.toMap(UserRankProjection::getUsername, UserRankProjection::getRankPoints));
        Map<String, Integer> weeklyRank = userRepository.findAllUsersWithWeeklyRank().stream().collect(Collectors.toMap(UserWeeklyRankProject::getUsername, UserWeeklyRankProject::getWeeklyPoints));
        UserPointsProjection userPointsProjection = userRepository.findUsernameWithRankAndWeeklyRankPoints(user.getUsername());


        return LeaderboardRes
                .builder()
                .rank(rank)
                .weeklyRank(weeklyRank)
                .userPoints(userPointsProjection)
                .build();
    }
}
