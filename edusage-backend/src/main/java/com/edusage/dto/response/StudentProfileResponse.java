package com.edusage.dto.response;

import com.edusage.model.enums.RiskLevel;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentProfileResponse {
    private Long studentId;
    private String name;
    private String email;
    private String rollNo;
    private String course;
    private String photoUrl;
    private int credits;
    private LocalDateTime joinedAt;
    private String memberDuration;

    private double overallAverage;
    private double movingAverage;
    private double attendancePercentage;
    private double improvementRate;
    private RiskLevel riskLevel;
    private boolean isDeclining;
    private java.util.Map<String, Double> subjectAverages;

    private int totalQuizzesTaken;
    private int counsellingSessionCount;
}
