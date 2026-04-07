package com.edusage.repository;

import com.edusage.model.CounsellingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CounsellingSessionRepository extends JpaRepository<CounsellingSession, Long> {
    List<CounsellingSession> findByStudentIdOrderBySessionDateDesc(Long studentId);
}
