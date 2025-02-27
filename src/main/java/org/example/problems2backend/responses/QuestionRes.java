package org.example.problems2backend.responses;

import lombok.Builder;
import lombok.Data;
import org.example.problems2backend.models.Quiz;

@Data
@Builder
public class QuestionRes
{
    private String id;
    private ContentRes content;
}
