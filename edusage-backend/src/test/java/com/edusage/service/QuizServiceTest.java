package com.edusage.service;

import com.edusage.dto.request.SubmitQuizRequest;
import com.edusage.dto.response.QuizResponse;
import com.edusage.model.*;
import com.edusage.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class QuizServiceTest {

    @InjectMocks private QuizService quizService;

    @Mock private QuizRepository quizRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private ScoreRepository scoreRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private TeacherRepository teacherRepository;
    @Mock private RecommendationService recommendationService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void submitQuiz_allCorrect_returns100Percent() {
        Long studentId = 1L;
        Long quizId    = 1L;

        when(scoreRepository.existsByStudentIdAndQuizId(studentId, quizId)).thenReturn(false);

        Quiz quiz = new Quiz();
        quiz.setId(quizId);
        quiz.setTitle("Test Quiz");
        quiz.setSubject("Math");
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));

        Student student = new Student();
        student.setId(studentId);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

        List<Question> questions = new ArrayList<>();
        Map<Long, String> answers = new LinkedHashMap<>();
        for (long i = 1; i <= 4; i++) {
            Question q = new Question();
            q.setId(i);
            q.setQuestionText("Question " + i);
            q.setOptionA("Opt A"); q.setOptionB("Opt B");
            q.setOptionC("Opt C"); q.setOptionD("Opt D");
            q.setCorrectOption("A");
            questions.add(q);
            answers.put(i, "A");
        }
        when(questionRepository.findByQuizId(quizId)).thenReturn(questions);
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(recommendationService).generateSystemRecommendations(studentId);

        SubmitQuizRequest req = new SubmitQuizRequest();
        req.setStudentId(studentId);
        req.setQuizId(quizId);
        req.setAnswers(answers);

        QuizResponse response = quizService.submitQuiz(req);

        assertEquals(100.0, response.getPercentage(), 0.01);
        assertEquals(4, response.getMarks());
        assertEquals(4, response.getTotalMarks());
    }

    @Test
    void submitQuiz_duplicate_throwsException() {
        Long studentId = 1L;
        Long quizId    = 1L;

        when(scoreRepository.existsByStudentIdAndQuizId(studentId, quizId)).thenReturn(true);

        SubmitQuizRequest req = new SubmitQuizRequest();
        req.setStudentId(studentId);
        req.setQuizId(quizId);
        req.setAnswers(Map.of());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> quizService.submitQuiz(req));
        assertTrue(ex.getMessage().contains("already submitted"));
    }
}
