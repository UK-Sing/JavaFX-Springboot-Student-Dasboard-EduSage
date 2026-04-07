package com.edusage.controller;

import com.edusage.dto.request.*;
import com.edusage.dto.response.QuizResponse;
import com.edusage.model.*;
import com.edusage.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    @Autowired private QuizService quizService;

    @PostMapping("/create")
    public ResponseEntity<Quiz> createQuiz(@RequestBody CreateQuizRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quizService.createQuiz(req));
    }

    @PostMapping("/submit")
    public ResponseEntity<QuizResponse> submitQuiz(@RequestBody SubmitQuizRequest req) {
        return ResponseEntity.ok(quizService.submitQuiz(req));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Quiz>> getActiveQuizzes() {
        return ResponseEntity.ok(quizService.getActiveQuizzes());
    }

    @GetMapping("/{quizId}/questions")
    public ResponseEntity<List<Question>> getQuestions(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuestions(quizId));
    }
}
