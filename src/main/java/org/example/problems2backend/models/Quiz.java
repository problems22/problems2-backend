package org.example.problems2backend.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.example.problems2backend.exceptions.QuestionContentException;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection="quizzes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quiz {

    @Id
    private ObjectId id;
    private String name;
    private String description;
    private String difficulty;
    private List<String> tags;
    @Builder.Default
    private Integer timeLimit = 10;
    private List<Question> questions;
    private String rules;
    private String instructions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Question
    {
        @Id
        private String id;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
        @JsonSubTypes({
                @JsonSubTypes.Type(value = MultipleChoice.class, name = "MULTIPLE_CHOICE"),
                @JsonSubTypes.Type(value = FillInTheBlank.class, name = "FILL_IN_THE_BLANK"),
                @JsonSubTypes.Type(value = MultipleSelect.class, name = "MULTIPLE_SELECT")
        })
        private Content content;

        public interface Content
        {
            String getQuestion();
            List<String> getOptions();

            Integer getCorrectOption();

            String getCorrectAnswer();

            List<Integer> getCorrectOptions();

        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonTypeName("MULTIPLE_CHOICE")
        public static class MultipleChoice implements Content {

            @Id
            private String id;

            private String question;
            private List<String> options;
            private Integer correctOption;


            @Override
            public String getQuestion() {
                return question;
            }

            @Override
            public List<String> getOptions() {
                return options;
            }

            @Override
            public Integer getCorrectOption() {
                return correctOption;
            }

            @Override
            public String getCorrectAnswer() {
                throw new QuestionContentException("multiple choice question doesn't have correct answer attribute");
            }

            @Override
            public List<Integer> getCorrectOptions() {
                throw new QuestionContentException("multiple choice question doesn't have correct options");
            }
        }
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonTypeName("FILL_IN_THE_BLANK")
        public static class FillInTheBlank implements Content {

            @Id
            private String id;

            private String question;

            private String correctAnswer;

            @Override
            public String getQuestion() {
                return question;
            }

            @Override
            public List<String> getOptions() {
                throw new QuestionContentException("fill in the blank question doesn't have options");
            }

            @Override
            public Integer getCorrectOption() {
                throw new QuestionContentException("fill in the blank question doesn't have correct options");
            }

            @Override
            public String getCorrectAnswer() {
                return correctAnswer;
            }

            @Override
            public List<Integer> getCorrectOptions() {
                throw new QuestionContentException("fill in the form doesn't have correct options");
            }
        }
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonTypeName("MULTIPLE_SELECT")
        public static class MultipleSelect implements Content {

            @Id
            private String id;

            private String question;
            private List<String> options;
            private List<Integer> correctOptions;


            @Override
            public String getQuestion() {
                return question;
            }

            @Override
            public List<String> getOptions() {
                return options;
            }

            @Override
            public Integer getCorrectOption() {
                throw new QuestionContentException("multiple select doesn't have correct option");
            }

            @Override
            public String getCorrectAnswer() {
                throw new QuestionContentException("multiple select doesn't have correct answer");
            }

            @Override
            public List<Integer> getCorrectOptions() {
                return correctOptions;
            }
        }



    }


}
