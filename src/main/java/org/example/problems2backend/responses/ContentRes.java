package org.example.problems2backend.responses;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ContentRes
{
    private String id;
    private String question;
    private String type;
    private List<String> options;
    private Integer correctOption;
    private List<Integer> correctOptions;
    private String correctAnswer;

}
