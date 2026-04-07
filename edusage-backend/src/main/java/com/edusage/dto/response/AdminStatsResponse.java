package com.edusage.dto.response;

import lombok.Data;

@Data
public class AdminStatsResponse {
    private long totalStudents;
    private long totalTeachers;
    private long totalAdmins;
    private long activeUsers;
    private long inactiveUsers;
    private long totalQuizzes;
    private long totalSubmissions;
    private double systemAvgScore;
    private long atRiskCount;
    private long onTrackCount;
}
