package org.example.problems2backend.repositories;

import org.example.problems2backend.models.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


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

}
