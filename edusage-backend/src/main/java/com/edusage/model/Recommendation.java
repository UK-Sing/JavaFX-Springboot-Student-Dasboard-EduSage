package com.edusage.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations")
@Data
@NoArgsConstructor
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"user", "rollNo", "className", "section"})
    private Student student;

    private String source;

    private String type;

    @Column(length = 1000)
    private String message;

    private String subject;

    private LocalDateTime createdAt = LocalDateTime.now();
}
