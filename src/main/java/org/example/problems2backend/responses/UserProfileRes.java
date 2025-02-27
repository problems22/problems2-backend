package org.example.problems2backend.responses;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.problems2backend.models.QuizResult;
import org.example.problems2backend.models.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class UserProfileRes
{
    private String username;
    private String role;
    private String avatar;
    private LocalDateTime memberSince;
    private Integer rankPoints;
    private Boolean isBanned;
    private User.Stats stats;
    private Integer weeklyPoints;
    private List<QuizResultRes> recentResults;
    private Double averagePointsPerQuiz;
    private Integer totalTimeTaken;
    private Double averageTimeTaken;
    private Double accuracyRate;
    private Map<String, Long> quizzesByDifficulty;
    private Map<LocalDateTime, Long> quizzesOverTime;
    private User.RankTitle rankTitle;
    private Integer nextRankPoints;
    private Integer progressTowardsNextRank;

}
