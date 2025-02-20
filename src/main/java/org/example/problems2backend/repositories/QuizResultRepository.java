package org.example.problems2backend.repositories;

import org.example.problems2backend.models.QuizResult;
import org.example.problems2backend.responses.QuizRes;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizResultRepository
    extends MongoRepository<QuizResult, String>
{

}
