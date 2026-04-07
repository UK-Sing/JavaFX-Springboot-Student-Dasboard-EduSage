package com.edusage.dto.response;

import com.edusage.model.enums.RiskLevel;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AnalyticsResponse {
    private Long studentId;
    private String studentName;
    private double overallAverage;
    private double movingAverage;
    private double improvementRate;
    private double attendancePercentage;
    private RiskLevel riskLevel;
    private List<String> weakSubjects;
    private boolean isDeclining;
    private Map<String, Double> subjectAverages;
    private List<ScorePoint> scoreTimeline;

    @Data
    public static class ScorePoint {
        private String date;
        private double percentage;
        private String subject;
    }
}
