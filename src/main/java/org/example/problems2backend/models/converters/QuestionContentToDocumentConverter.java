package org.example.problems2backend.models.converters;

import lombok.NonNull;
import org.example.problems2backend.models.Quiz;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import com.fasterxml.jackson.databind.ObjectMapper;

@WritingConverter
public class QuestionContentToDocumentConverter implements Converter<Quiz.Question.Content, org.bson.Document> {

    private final ObjectMapper objectMapper;

    public QuestionContentToDocumentConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @NonNull
    public org.bson.Document convert(@NonNull Quiz.Question.Content source) {
        return objectMapper.convertValue(source, org.bson.Document.class);
    }
}

