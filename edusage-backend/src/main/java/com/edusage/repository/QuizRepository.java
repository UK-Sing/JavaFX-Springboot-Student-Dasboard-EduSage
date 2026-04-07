package com.edusage.repository;

import com.edusage.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByTeacherId(Long teacherId);
    List<Quiz> findBySubjectAndActiveTrue(String subject);
    List<Quiz> findByActiveTrue();
}
