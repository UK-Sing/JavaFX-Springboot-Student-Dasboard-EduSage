package com.edusage.repository;

import com.edusage.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findBySubject(String subject);
    List<Material> findByTeacherId(Long teacherId);
}
