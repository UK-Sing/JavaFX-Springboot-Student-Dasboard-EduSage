package com.edusage.model;

import com.edusage.model.enums.CounsellingOutcome;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "counselling_sessions")
@Data
@NoArgsConstructor
public class CounsellingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private LocalDate sessionDate;

    @Column(nullable = false)
    private String counsellorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CounsellingOutcome outcome;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
