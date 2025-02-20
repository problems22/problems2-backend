package org.example.problems2backend.models.converters;

import org.example.problems2backend.models.Quiz;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import com.fasterxml.jackson.databind.ObjectMapper;

@WritingConverter
public class ContentToDocumentConverter implements Converter<Quiz.Question.Content, org.bson.Document> {

    private final ObjectMapper objectMapper;

    public ContentToDocumentConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public org.bson.Document convert(Quiz.Question.Content source) {
        return objectMapper.convertValue(source, org.bson.Document.class);
    }
}

