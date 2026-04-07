package com.edusage.service;

import com.edusage.dto.response.AnalyticsResponse;
import com.edusage.model.Score;
import com.edusage.model.enums.AttendanceStatus;
import com.edusage.model.enums.RiskLevel;
import com.edusage.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired private ScoreRepository scoreRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private StudentRepository studentRepository;

    /**
     * Master method. Returns all analytics for one student.
     * Called after every quiz submission and whenever the dashboard loads.
     */
    public AnalyticsResponse computeFullAnalytics(Long studentId) {
        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

        List<Score> allScores = scoreRepository.findByStudentIdOrderByAttemptDateAsc(studentId);

        AnalyticsResponse result = new AnalyticsResponse();
        result.setStudentId(studentId);
        result.setStudentName(student.getUser().getName());
        result.setOverallAverage(computeOverallAverage(allScores));
        result.setMovingAverage(computeMovingAverage(allScores, 5));
        result.setImprovementRate(computeImprovementRate(allScores));
        result.setAttendancePercentage(computeAttendance(studentId));
        result.setRiskLevel(classifyRisk(result.getOverallAverage()));
        result.setSubjectAverages(computeSubjectAverages(studentId));
        result.setWeakSubjects(detectWeakSubjects(result.getSubjectAverages()));
        result.setDeclining(detectDecline(allScores, 3));
        result.setScoreTimeline(buildTimeline(allScores));

        return result;
    }

    /**
     * METRIC 1: Overall average.
     * Formula: sum of all score percentages / count of scores
     */
    public double computeOverallAverage(List<Score> scores) {
        if (scores.isEmpty()) return 0.0;
        return scores.stream()
                .mapToDouble(Score::getPercentage)
                .average()
                .orElse(0.0);
    }

    /**
     * METRIC 2: Moving average over last N scores.
     * Formula: sum of last N percentages / N
     * If fewer than N scores exist, use all available.
     */
    public double computeMovingAverage(List<Score> scores, int n) {
        if (scores.isEmpty()) return 0.0;
        int size = scores.size();
        List<Score> last = scores.subList(Math.max(0, size - n), size);
        return last.stream()
                .mapToDouble(Score::getPercentage)
                .average()
                .orElse(0.0);
    }

    /**
     * METRIC 3: Improvement rate.
     * Formula: ((latest - previous) / previous) * 100
     * Positive = improving. Negative = declining.
     * Returns 0 if fewer than 2 scores.
     */
    public double computeImprovementRate(List<Score> scores) {
        if (scores.size() < 2) return 0.0;
        double latest   = scores.get(scores.size() - 1).getPercentage();
        double previous = scores.get(scores.size() - 2).getPercentage();
        if (previous == 0) return 0.0;
        return ((latest - previous) / previous) * 100.0;
    }

    /**
     * METRIC 4: Attendance percentage.
     * Formula: (PRESENT + LATE) / total_records * 100
     * LATE counts as attended.
     */
    public double computeAttendance(Long studentId) {
        long total   = attendanceRepository.countByStudentId(studentId);
        if (total == 0) return 100.0;
        long present = attendanceRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.PRESENT);
        long late    = attendanceRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.LATE);
        return ((present + late) * 100.0) / total;
    }

    /**
     * METRIC 5: Risk Level classification.
     * Rule table (DO NOT MODIFY — matches requirements exactly):
     *   avg < 40  → HIGH_RISK
     *   avg < 60  → NEEDS_ATTENTION
     *   avg >= 60 → ON_TRACK
     */
    public RiskLevel classifyRisk(double average) {
        if (average < 40.0) return RiskLevel.HIGH_RISK;
        if (average < 60.0) return RiskLevel.NEEDS_ATTENTION;
        return RiskLevel.ON_TRACK;
    }

    /**
     * METRIC 6: Subject-wise averages.
     * Returns a map of subject → average percentage.
     */
    public Map<String, Double> computeSubjectAverages(Long studentId) {
        List<String> subjects = scoreRepository.findDistinctSubjectsByStudentId(studentId);
        Map<String, Double> result = new LinkedHashMap<>();

        for (String subject : subjects) {
            List<Score> subjectScores = scoreRepository
                    .findByStudentIdAndQuizSubjectOrderByAttemptDateDesc(studentId, subject);
            double avg = subjectScores.stream()
                    .mapToDouble(Score::getPercentage)
                    .average()
                    .orElse(0.0);
            result.put(subject, Math.round(avg * 100.0) / 100.0);
        }
        return result;
    }

    /**
     * METRIC 7a: Weak subject detection.
     * Returns bottom 2 subjects sorted by ascending average.
     * If fewer than 2 subjects exist, returns what's available.
     */
    public List<String> detectWeakSubjects(Map<String, Double> subjectAverages) {
        return subjectAverages.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(2)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * METRIC 7b: Declining trend detection.
     * Returns true if the last N consecutive scores are strictly decreasing.
     */
    public boolean detectDecline(List<Score> scores, int consecutiveCount) {
        if (scores.size() < consecutiveCount) return false;
        int size = scores.size();
        for (int i = size - consecutiveCount + 1; i < size; i++) {
            if (scores.get(i).getPercentage() >= scores.get(i - 1).getPercentage()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Build timeline data for the student's line chart.
     */
    private List<AnalyticsResponse.ScorePoint> buildTimeline(List<Score> scores) {
        return scores.stream().map(s -> {
            AnalyticsResponse.ScorePoint p = new AnalyticsResponse.ScorePoint();
            p.setDate(s.getAttemptDate().toLocalDate().toString());
            p.setPercentage(s.getPercentage());
            p.setSubject(s.getQuiz().getSubject());
            return p;
        }).collect(Collectors.toList());
    }
}
