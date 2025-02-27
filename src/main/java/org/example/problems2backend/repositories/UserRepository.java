package org.example.problems2backend.repositories;

import org.bson.types.ObjectId;
import org.example.problems2backend.models.User;
import org.example.problems2backend.repositories.projections.PasswordHashProjection;
import org.example.problems2backend.repositories.projections.UserPointsProjection;
import org.example.problems2backend.repositories.projections.UserRankProjection;
import org.example.problems2backend.repositories.projections.UserWeeklyRankProject;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


/* TODO: do it with plain driver not with mongorepository */
@Repository
public interface UserRepository
    extends MongoRepository<User, String>
{
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query(value = "{ 'username': ?0 }", fields = "{ 'passwordHash': 1, '_id': 0 }")
    Optional<PasswordHashProjection> findPasswordHashByUsername(String username);

    @Query("{ 'username': ?0 }")
    @Update("{ '$set': { 'passwordHash': ?1 }}")
    void updatePasswordHashByUsername(String username, String newPasswordHash);

    void deleteByUsername(String username);

    @Query("{ 'username': ?0 }")
    @Update("{ '$inc': { 'rankPoints': ?1, 'weeklyPoints': ?2 } }")
    void incrementRankWeeklyPointsAndTotalAttempts(String username, int rankPointsIncrement, int weeklyPointsIncrement);

    @Query("{ 'username': ?0 }")
    @Update("{ '$inc': { 'stats.totalAttempts': ?1, 'stats.correctAnswers': ?2, 'stats.incorrectAnswers': ?3 } }")
    void incrementStatsByUsername(String username, int totalAttemptsIncrement, int correctAnswersIncrement, int incorrectAnswersIncrement);

    @Aggregation(pipeline = {
            "{ $sort: { rankPoints: -1 } }", // Sort by rankPoints in descending order
            "{ $limit: 100 }", // Limit to 100 results
            "{ $project: { username: 1, rankPoints: 1, _id: 0 } }" // Include only specific fields
    })
    List<UserRankProjection> findAllUsersWithRank();

    @Aggregation(pipeline = {
            "{ $sort: { weeklyPoints: -1 } }", // Sort by rankPoints in descending order
            "{ $limit: 100 }", // Limit to 100 results
            "{ $project: { username: 1, weeklyPoints: 1, _id: 0 } }" // Include only specific fields
    })
    List<UserWeeklyRankProject> findAllUsersWithWeeklyRank();

    @Query(value = "{ 'username': ?0 }", fields = "{ 'username': 1, 'rankPoints': 1,'weeklyPoints': 1, '_id': 0 }")
    UserPointsProjection findUsernameWithRankAndWeeklyRankPoints(String username);


}
