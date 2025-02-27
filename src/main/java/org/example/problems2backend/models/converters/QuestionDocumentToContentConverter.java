package org.example.problems2backend.models.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.bson.Document;
import org.example.problems2backend.exceptions.InternalServerErrorException;
import org.example.problems2backend.models.Quiz;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class QuestionDocumentToContentConverter implements Converter<Document, Quiz.Question.Content> {

    private final ObjectMapper objectMapper;

    public QuestionDocumentToContentConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @NonNull
    public Quiz.Question.Content convert(Document source) {
        String type = source.getString("type");
        try {
            return switch (type) {
                case "MULTIPLE_CHOICE" -> objectMapper.convertValue(source, Quiz.Question.MultipleChoice.class);
                case "FILL_IN_THE_BLANK" -> objectMapper.convertValue(source, Quiz.Question.FillInTheBlank.class);
                case "MULTIPLE_SELECT" -> objectMapper.convertValue(source, Quiz.Question.MultipleSelect.class);
                default -> throw new IllegalArgumentException("Unknown type: " + type);
            };
        } catch (Exception e) {
            throw new InternalServerErrorException("error converting Document to QuestionContent");
        }
    }
}
