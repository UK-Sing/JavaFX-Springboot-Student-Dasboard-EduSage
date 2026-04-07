package com.edusage.controller;

import com.edusage.dto.request.AttendanceRequest;
import com.edusage.dto.response.AnalyticsResponse;
import com.edusage.dto.response.StudentProfileResponse;
import com.edusage.model.*;
import com.edusage.repository.*;
import com.edusage.service.AnalyticsService;
import com.edusage.service.PdfExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired private AnalyticsService analyticsService;
    @Autowired private PdfExportService pdfExportService;
    @Autowired private RecommendationRepository recommendationRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private CounsellingSessionRepository counsellingSessionRepository;
    @Autowired private ScoreRepository scoreRepository;

    @GetMapping("/{studentId}/analytics")
    public ResponseEntity<AnalyticsResponse> getAnalytics(@PathVariable Long studentId) {
        return ResponseEntity.ok(analyticsService.computeFullAnalytics(studentId));
    }

    @GetMapping("/{studentId}/recommendations")
    public ResponseEntity<List<Recommendation>> getRecommendations(@PathVariable Long studentId) {
        return ResponseEntity.ok(
                recommendationRepository.findByStudentIdOrderByCreatedAtDesc(studentId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<?>> getAllStudents() {
        return ResponseEntity.ok(studentRepository.findAll());
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<byte[]> getReport(@PathVariable Long id) {
        byte[] pdf = pdfExportService.generateReport(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "student-report.pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<StudentProfileResponse> getProfile(@PathVariable Long id) {
        Student s = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        AnalyticsResponse a = analyticsService.computeFullAnalytics(id);

        LocalDateTime joined = s.getUser().getCreatedAt();
        long months = ChronoUnit.MONTHS.between(joined, LocalDateTime.now());
        String duration = months < 1 ? "< 1 month" : months + (months == 1 ? " month" : " months");

        StudentProfileResponse p = new StudentProfileResponse();
        p.setStudentId(id);
        p.setName(s.getUser().getName());
        p.setEmail(s.getUser().getEmail());
        p.setRollNo(s.getRollNo());
        p.setCourse(s.getCourse() != null ? s.getCourse() : "N/A");
        p.setPhotoUrl(s.getPhotoUrl());
        p.setCredits(s.getCredits());
        p.setJoinedAt(joined);
        p.setMemberDuration(duration);
        p.setOverallAverage(a.getOverallAverage());
        p.setMovingAverage(a.getMovingAverage());
        p.setAttendancePercentage(a.getAttendancePercentage());
        p.setImprovementRate(a.getImprovementRate());
        p.setRiskLevel(a.getRiskLevel());
        p.setDeclining(a.isDeclining());
        p.setSubjectAverages(a.getSubjectAverages());
        p.setTotalQuizzesTaken((int) scoreRepository.findByStudentIdOrderByAttemptDateAsc(id).size());
        p.setCounsellingSessionCount((int) counsellingSessionRepository.findByStudentIdOrderBySessionDateDesc(id).size());
        return ResponseEntity.ok(p);
    }

    @GetMapping("/{id}/counselling")
    public ResponseEntity<List<CounsellingSession>> getCounsellingSessions(@PathVariable Long id) {
        return ResponseEntity.ok(counsellingSessionRepository.findByStudentIdOrderBySessionDateDesc(id));
    }

    @PostMapping("/attendance")
    public ResponseEntity<String> markAttendance(@RequestBody AttendanceRequest req) {
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Attendance att = new Attendance();
        att.setStudent(student);
        att.setDate(req.getDate());
        att.setStatus(req.getStatus());
        attendanceRepository.save(att);
        return ResponseEntity.ok("Attendance marked.");
    }
}
