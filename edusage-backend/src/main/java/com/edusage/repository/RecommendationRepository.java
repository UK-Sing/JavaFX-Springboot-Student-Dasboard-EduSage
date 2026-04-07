package com.edusage.repository;

import com.edusage.model.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    void deleteByStudentIdAndSource(Long studentId, String source);
}
