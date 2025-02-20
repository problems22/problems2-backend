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
    private User.RankTitle title;
    private Boolean isBanned;
    private User.Stats stats;
    private Integer weeklyPoints;
    private List<QuizResult> recentResults;

}
