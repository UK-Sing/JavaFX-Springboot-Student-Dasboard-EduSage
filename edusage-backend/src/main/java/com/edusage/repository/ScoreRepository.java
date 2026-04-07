package com.edusage.repository;

import com.edusage.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByStudentIdOrderByAttemptDateAsc(Long studentId);
    List<Score> findByStudentIdAndQuizSubjectOrderByAttemptDateDesc(Long studentId, String subject);
    boolean existsByStudentIdAndQuizId(Long studentId, Long quizId);

    @Query("SELECT DISTINCT s.quiz.subject FROM Score s WHERE s.student.id = :studentId")
    List<String> findDistinctSubjectsByStudentId(Long studentId);

    @Query("SELECT COALESCE(AVG(s.percentage), 0.0) FROM Score s")
    double findSystemAverageScore();
}
