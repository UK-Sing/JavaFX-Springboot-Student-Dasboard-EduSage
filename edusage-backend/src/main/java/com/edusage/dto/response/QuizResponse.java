package com.edusage.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class QuizResponse {
    private Long quizId;
    private String title;
    private String subject;
    private int totalQuestions;
    private int marks;
    private int totalMarks;
    private double percentage;
    private List<QuestionResult> results;

    @Data
    public static class QuestionResult {
        private Long questionId;
        private String questionText;
        private String yourAnswer;
        private String correctAnswer;
        private boolean correct;
    }
}
