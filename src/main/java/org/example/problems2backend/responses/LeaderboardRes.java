package org.example.problems2backend.responses;

import lombok.Builder;
import lombok.Data;
import org.example.problems2backend.repositories.projections.UserPointsProjection;

import java.util.Map;

@Data
@Builder
public class LeaderboardRes
{
    private Map<String, Integer> rank;
    private Map<String, Integer> weeklyRank;
    private UserPointsProjection userPoints;

}
