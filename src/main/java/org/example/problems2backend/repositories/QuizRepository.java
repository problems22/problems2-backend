package org.example.problems2backend.repositories;

import org.example.problems2backend.models.Quiz;
import org.example.problems2backend.repositories.projections.DifficultyCountProjection;
import org.example.problems2backend.repositories.projections.DifficultyProjection;
import org.example.problems2backend.repositories.projections.TagCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface QuizRepository extends MongoRepository<Quiz, String> {

    @Query("{ $and: [ " +
            "  { $or: [ " +
            "    { 'name': { $regex: ?0, $options: 'i' } }, " +
            "    { 'description': { $regex: ?0, $options: 'i' } } " +
            "  ]}, " +
            "  { $or: [ " +
            "    { 'difficulty': { $regex: ?1, $options: 'i' } }, " +
            "    { $expr: { $eq: [ ?1, '' ] } } " +
            "  ]}, " +
            "  { $expr: { " +
            "    $and: [ " +
            "      { $gte: [{ $size: '$questions' }, ?3] }, " +
            "      { $lte: [{ $size: '$questions' }, ?4] } " +
            "    ] " +
            "  }}, " +
            "  { $or: [ " +
            "    { 'tags': { $in: ?2 } }, " +
            "    { $expr: { $eq: [{ $literal: true }, ?5] } } " +
            "  ]} " +
            "] }")
    Page<Quiz> findQuizzesWithFilters(
            String searchRegex,
            String difficultyRegex,
            List<String> tags,
            int minQuestions,
            int maxQuestions,
            boolean ignoreTags,
            Pageable pageable
    );

    // Count total quizzes
    @Query(value = "{}", count = true)
    long countTotalQuizzes();



    // Count total questions across all quizzes
    @Aggregation(pipeline = {
            "{ $unwind: '$questions' }", // Unwind the questions array
            "{ $group: { _id: null, totalQuestions: { $sum: 1 } } }" // Count all questions
    })
    Long countTotalQuestions();

    // Count quizzes by difficulty
    @Aggregation(pipeline = {
            "{ $group: { _id: '$difficulty', count: { $sum: 1 } } }",
            "{ $project: { difficulty: '$_id', count: 1, _id: 0 } }"
    })
    List<DifficultyCountProjection> countQuizzesByDifficulty();;

    // Count tags and their frequencies
    @Aggregation(pipeline = {
            "{ $unwind: '$tags' }",
            "{ $group: { _id: '$tags', count: { $sum: 1 } } }"
    })
    List<TagCountProjection> countTags();

    @Query(value = "{ '_id': ObjectId(?0) }", fields = "{ 'difficulty': 1, '_id': 0 }")
    DifficultyProjection findDifficultyById(String id);

    @Query(value = "{ '_id' : ObjectId(?0) }", fields = "{ 'name': 1, 'description': 1, 'difficulty': 1, 'tags': 1, 'timeLimit': 1, 'questions.content': 1, 'rules': 1, 'instructions': 1 }")
    Optional<Quiz> findQuizById(String quizId);




}











