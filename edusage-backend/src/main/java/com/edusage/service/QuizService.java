package com.edusage.service;

import com.edusage.dto.request.*;
import com.edusage.dto.response.QuizResponse;
import com.edusage.model.*;
import com.edusage.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class QuizService {

    @Autowired private QuizRepository quizRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private ScoreRepository scoreRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private RecommendationService recommendationService;

    @Transactional
    public Quiz createQuiz(CreateQuizRequest req) {
        Teacher teacher = teacherRepository.findById(req.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Quiz quiz = new Quiz();
        quiz.setTeacher(teacher);
        quiz.setTitle(req.getTitle());
        quiz.setSubject(req.getSubject());
        quiz.setDeadline(req.getDeadline());
        quiz = quizRepository.save(quiz);

        for (CreateQuizRequest.QuestionDto qDto : req.getQuestions()) {
            Question q = new Question();
            q.setQuiz(quiz);
            q.setQuestionText(qDto.getQuestionText());
            q.setOptionA(qDto.getOptionA());
            q.setOptionB(qDto.getOptionB());
            q.setOptionC(qDto.getOptionC());
            q.setOptionD(qDto.getOptionD());
            q.setCorrectOption(qDto.getCorrectOption().toUpperCase());
            questionRepository.save(q);
        }

        return quiz;
    }

    @Transactional
    public QuizResponse submitQuiz(SubmitQuizRequest req) {
        if (scoreRepository.existsByStudentIdAndQuizId(req.getStudentId(), req.getQuizId())) {
            throw new RuntimeException("Quiz already submitted by this student.");
        }

        Quiz quiz = quizRepository.findById(req.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<Question> questions = questionRepository.findByQuizId(quiz.getId());
        int correct = 0;
        List<QuizResponse.QuestionResult> results = new ArrayList<>();

        for (Question q : questions) {
            String given = req.getAnswers().getOrDefault(q.getId(), "");
            boolean isCorrect = q.getCorrectOption().equalsIgnoreCase(given);
            if (isCorrect) correct++;

            QuizResponse.QuestionResult r = new QuizResponse.QuestionResult();
            r.setQuestionId(q.getId());
            r.setQuestionText(q.getQuestionText());
            r.setYourAnswer(given);
            r.setCorrectAnswer(q.getCorrectOption());
            r.setCorrect(isCorrect);
            results.add(r);
        }

        int total = questions.size();
        double pct = total > 0 ? (correct * 100.0) / total : 0.0;

        Score score = new Score();
        score.setStudent(student);
        score.setQuiz(quiz);
        score.setMarks(correct);
        score.setTotal(total);
        score.setPercentage(pct);
        scoreRepository.save(score);

        recommendationService.generateSystemRecommendations(req.getStudentId());

        QuizResponse response = new QuizResponse();
        response.setQuizId(quiz.getId());
        response.setTitle(quiz.getTitle());
        response.setSubject(quiz.getSubject());
        response.setTotalQuestions(total);
        response.setMarks(correct);
        response.setTotalMarks(total);
        response.setPercentage(pct);
        response.setResults(results);
        return response;
    }

    public List<Quiz> getActiveQuizzes() {
        return quizRepository.findByActiveTrue();
    }

    public List<Question> getQuestions(Long quizId) {
        return questionRepository.findByQuizId(quizId);
    }
}
