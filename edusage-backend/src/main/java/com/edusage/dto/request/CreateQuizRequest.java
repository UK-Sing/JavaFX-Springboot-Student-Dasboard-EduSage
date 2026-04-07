package com.edusage.dto.request;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateQuizRequest {
    private Long teacherId;
    private String title;
    private String subject;
    private LocalDateTime deadline;
    private List<QuestionDto> questions;

    @Data
    public static class QuestionDto {
        private String questionText;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
        private String correctOption;
    }
}
