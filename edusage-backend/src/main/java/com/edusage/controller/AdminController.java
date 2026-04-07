package com.edusage.controller;

import com.edusage.dto.request.RegisterRequest;
import com.edusage.dto.response.AdminStatsResponse;
import com.edusage.dto.response.AuthResponse;
import com.edusage.model.User;
import com.edusage.model.enums.Role;
import com.edusage.repository.*;
import com.edusage.service.AnalyticsService;
import com.edusage.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private UserRepository userRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private QuizRepository quizRepository;
    @Autowired private ScoreRepository scoreRepository;
    @Autowired private AnalyticsService analyticsService;
    @Autowired private AuthService authService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        AdminStatsResponse s = new AdminStatsResponse();
        s.setTotalStudents(studentRepository.count());
        s.setTotalTeachers(teacherRepository.count());
        s.setTotalAdmins(userRepository.countByRole(Role.ADMIN));
        s.setActiveUsers(userRepository.countByActiveTrue());
        s.setInactiveUsers(userRepository.countByActiveFalse());
        s.setTotalQuizzes(quizRepository.count());
        s.setTotalSubmissions(scoreRepository.count());
        s.setSystemAvgScore(scoreRepository.findSystemAverageScore());

        long atRisk = studentRepository.findAll().stream()
                .filter(st -> {
                    try {
                        var a = analyticsService.computeFullAnalytics(st.getId());
                        return a.getRiskLevel() != null &&
                               !a.getRiskLevel().name().equals("ON_TRACK");
                    } catch (Exception e) { return false; }
                }).count();
        s.setAtRiskCount(atRisk);
        s.setOnTrackCount(studentRepository.count() - atRisk);
        return ResponseEntity.ok(s);
    }

    @PostMapping("/users")
    public ResponseEntity<AuthResponse> createUser(@RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() == Role.STUDENT) {
            studentRepository.findByUserId(id).ifPresent(studentRepository::delete);
        } else if (user.getRole() == Role.TEACHER) {
            teacherRepository.findByUserId(id).ifPresent(teacherRepository::delete);
        }
        userRepository.delete(user);
        return ResponseEntity.ok("User deleted.");
    }

    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(false);
        userRepository.save(user);
        return ResponseEntity.ok("User deactivated.");
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<String> activateUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(true);
        userRepository.save(user);
        return ResponseEntity.ok("User activated.");
    }
}
