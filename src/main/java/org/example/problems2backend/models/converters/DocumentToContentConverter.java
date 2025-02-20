package org.example.problems2backend.models.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.example.problems2backend.models.Quiz;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class DocumentToContentConverter implements Converter<Document, Quiz.Question.Content> {

    private final ObjectMapper objectMapper;

    public DocumentToContentConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Quiz.Question.Content convert(Document source) {
        String type = source.getString("type");
        try {
            switch (type) {
                case "MULTIPLE_CHOICE":
                    return objectMapper.convertValue(source, Quiz.Question.MultipleChoice.class);
                case "FILL_IN_THE_BLANK":
                    return objectMapper.convertValue(source, Quiz.Question.FillInTheBlank.class);
                case "MULTIPLE_SELECT":
                    return objectMapper.convertValue(source, Quiz.Question.MultipleSelect.class);
                default:
                    throw new IllegalArgumentException("Unknown type: " + type);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error converting Document to Content", e);
        }
    }
}
