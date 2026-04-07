package com.edusage.controller;

import com.edusage.dto.request.CounsellingSessionRequest;
import com.edusage.dto.response.*;
import com.edusage.model.*;
import com.edusage.model.enums.RiskLevel;
import com.edusage.repository.*;
import com.edusage.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired private StudentRepository studentRepository;
    @Autowired private AnalyticsService analyticsService;
    @Autowired private CounsellingSessionRepository counsellingSessionRepository;

    @GetMapping("/class-overview")
    public ResponseEntity<List<StudentSummaryResponse>> getClassOverview() {
        List<Student> students = studentRepository.findAll();
        List<StudentSummaryResponse> summaries = students.stream().map(s -> {
            AnalyticsResponse a = analyticsService.computeFullAnalytics(s.getId());
            StudentSummaryResponse r = new StudentSummaryResponse();
            r.setStudentId(s.getId());
            r.setName(s.getUser().getName());
            r.setRollNo(s.getRollNo());
            r.setCourse(s.getCourse() != null ? s.getCourse() : "N/A");
            r.setOverallAverage(a.getOverallAverage());
            r.setRiskLevel(a.getRiskLevel());
            r.setAttendancePercentage(a.getAttendancePercentage());
            return r;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(summaries);
    }

    @PostMapping("/student/{id}/counselling")
    public ResponseEntity<CounsellingSession> addCounsellingSession(
            @PathVariable Long id,
            @RequestBody CounsellingSessionRequest req) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        CounsellingSession cs = new CounsellingSession();
        cs.setStudent(student);
        cs.setSessionDate(req.getSessionDate());
        cs.setCounsellorName(req.getCounsellorName());
        cs.setOutcome(req.getOutcome());
        cs.setComments(req.getComments());
        return ResponseEntity.ok(counsellingSessionRepository.save(cs));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<StudentSummaryResponse>> getLeaderboard() {
        List<Student> students = studentRepository.findAll();
        List<StudentSummaryResponse> board = students.stream()
                .map(s -> {
                    AnalyticsResponse a = analyticsService.computeFullAnalytics(s.getId());
                    StudentSummaryResponse r = new StudentSummaryResponse();
                    r.setStudentId(s.getId());
                    r.setName(s.getUser().getName());
                    r.setRollNo(s.getRollNo());
                    r.setCourse(s.getCourse() != null ? s.getCourse() : "N/A");
                    r.setOverallAverage(a.getOverallAverage());
                    r.setRiskLevel(a.getRiskLevel());
                    r.setAttendancePercentage(a.getAttendancePercentage());
                    return r;
                })
                .sorted(Comparator.comparingDouble(StudentSummaryResponse::getOverallAverage).reversed())
                .limit(10)
                .collect(Collectors.toList());
        return ResponseEntity.ok(board);
    }

    @GetMapping("/at-risk")
    public ResponseEntity<List<StudentSummaryResponse>> getAtRiskStudents() {
        List<Student> students = studentRepository.findAll();
        List<StudentSummaryResponse> atRisk = students.stream()
                .map(s -> {
                    AnalyticsResponse a = analyticsService.computeFullAnalytics(s.getId());
                    StudentSummaryResponse r = new StudentSummaryResponse();
                    r.setStudentId(s.getId());
                    r.setName(s.getUser().getName());
                    r.setRollNo(s.getRollNo());
                    r.setCourse(s.getCourse() != null ? s.getCourse() : "N/A");
                    r.setOverallAverage(a.getOverallAverage());
                    r.setRiskLevel(a.getRiskLevel());
                    r.setAttendancePercentage(a.getAttendancePercentage());
                    return r;
                })
                .filter(r -> r.getRiskLevel() != RiskLevel.ON_TRACK)
                .collect(Collectors.toList());
        return ResponseEntity.ok(atRisk);
    }
}
