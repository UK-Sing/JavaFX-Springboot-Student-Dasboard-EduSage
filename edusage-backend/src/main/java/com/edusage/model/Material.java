package com.edusage.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "materials")
@Data
@NoArgsConstructor
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    @JsonIgnoreProperties({"user", "department", "subjectExpertise"})
    private Teacher teacher;

    @Column(nullable = false)
    private String title;

    private String subject;

    private String filePath;

    private String fileType;

    private LocalDateTime uploadDate = LocalDateTime.now();
}
