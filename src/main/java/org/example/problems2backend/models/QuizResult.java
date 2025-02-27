package org.example.problems2backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection="quiz_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResult
{
    @Id
    private ObjectId id;
    private ObjectId userId;
    private ObjectId quizId;
    private LocalDateTime submissionDate;
    private Integer obtainedPoints;
    private Integer timeTaken;
    private List<Content> content;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Content {
        private ObjectId questionId;
        private boolean isCorrect;
    }


}
