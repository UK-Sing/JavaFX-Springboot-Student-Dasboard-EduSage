package com.edusage.dto.request;

import lombok.Data;
import java.util.Map;

@Data
public class SubmitQuizRequest {
    private Long studentId;
    private Long quizId;
    private Map<Long, String> answers;
}
