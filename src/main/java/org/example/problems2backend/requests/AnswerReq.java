package org.example.problems2backend.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerReq {

    private String questionId;
    private String type;
    private Integer selectedOption;
    private String correctAnswer;
    private List<Integer> correctOptions;

}
