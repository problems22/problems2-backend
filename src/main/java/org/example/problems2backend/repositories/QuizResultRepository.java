package org.example.problems2backend.repositories;

import org.bson.types.ObjectId;
import org.example.problems2backend.models.QuizResult;
import org.example.problems2backend.repositories.projections.QuizAverageResultProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizResultRepository
    extends MongoRepository<QuizResult, String>
{


    @Aggregation(pipeline = {
            "{ $match: { quizId: ObjectId(?0) } }", // Filter by quizId
            "{ $group: { _id: null, averageObtainedPoints: { $avg: '$obtainedPoints' }, averageTimeTaken: { $avg: '$timeTaken' } } }", // Group all results into one
            "{ $project: { _id: 0, averageObtainedPoints: 1, averageTimeTaken: 1 } }" // Format output
    })
    Optional<QuizAverageResultProjection> findTotalAverageResultsByQuizId(String quizId);


    List<QuizResult> findByUserIdOrderBySubmissionDateDesc(ObjectId userId, Pageable pageable);
}
