package com.edusage.dto.response;

import com.edusage.model.enums.RiskLevel;
import lombok.Data;

@Data
public class StudentSummaryResponse {
    private Long studentId;
    private String name;
    private String rollNo;
    private String course;
    private double overallAverage;
    private RiskLevel riskLevel;
    private double attendancePercentage;
}
