package com.edusage.service;

import com.edusage.dto.response.AnalyticsResponse;
import com.edusage.model.*;
import com.edusage.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class RecommendationService {

    @Autowired private RecommendationRepository recommendationRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private AnalyticsService analyticsService;

    /**
     * RULE TABLE — the heart of the recommendation engine.
     * Runs after every quiz submission. Clears old SYSTEM recommendations,
     * recomputes analytics, applies rules, saves new recommendations.
     *
     * Rule 1: If any subject average < 50 → recommend study module for that subject
     * Rule 2: If attendance < 75% → attendance warning
     * Rule 3: If 3 consecutive scores declining → revision alert
     * Rule 4: If risk = HIGH_RISK → urgent support message
     */
    @Transactional
    public void generateSystemRecommendations(Long studentId) {
        recommendationRepository.deleteByStudentIdAndSource(studentId, "SYSTEM");

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        AnalyticsResponse analytics = analyticsService.computeFullAnalytics(studentId);
        List<Recommendation> recs = new ArrayList<>();

        // RULE 1: Weak subject — score < 50
        for (Map.Entry<String, Double> entry : analytics.getSubjectAverages().entrySet()) {
            if (entry.getValue() < 50.0) {
                recs.add(buildRec(student, "SYSTEM", "LOW_SCORE", entry.getKey(),
                        "Your average in " + entry.getKey() + " is " +
                        String.format("%.2f", entry.getValue()) + "%. " +
                        "Practice the foundational modules for " + entry.getKey() +
                        " and attempt additional exercises."));
            }
        }

        // RULE 2: Attendance < 75%
        if (analytics.getAttendancePercentage() < 75.0) {
            recs.add(buildRec(student, "SYSTEM", "ATTENDANCE", null,
                    "Your attendance is " +
                    String.format("%.2f", analytics.getAttendancePercentage()) + "%. " +
                    "Minimum required is 75%. Please improve your attendance immediately."));
        }

        // RULE 3: Declining trend
        if (analytics.isDeclining()) {
            recs.add(buildRec(student, "SYSTEM", "DECLINING", null,
                    "Your scores have declined in your last 3 attempts. " +
                    "Schedule a revision session and revisit recent topics."));
        }

        // RULE 4: High Risk
        if (analytics.getRiskLevel().name().equals("HIGH_RISK")) {
            recs.add(buildRec(student, "SYSTEM", "HIGH_RISK", null,
                    "You are classified as High Risk with an overall average of " +
                    String.format("%.2f", analytics.getOverallAverage()) + "%. " +
                    "Please meet your teacher immediately for academic support."));
        }

        recommendationRepository.saveAll(recs);
    }

    private Recommendation buildRec(Student student, String source, String type, String subject, String message) {
        Recommendation r = new Recommendation();
        r.setStudent(student);
        r.setSource(source);
        r.setType(type);
        r.setSubject(subject);
        r.setMessage(message);
        return r;
    }
}
