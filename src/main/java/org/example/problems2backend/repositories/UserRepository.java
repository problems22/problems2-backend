package org.example.problems2backend.repositories;

import org.example.problems2backend.models.User;
import org.example.problems2backend.repositories.projections.PasswordHashProjection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

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
}
