package com.edusage.service;

import com.edusage.dto.request.*;
import com.edusage.dto.response.AuthResponse;
import com.edusage.model.*;
import com.edusage.model.enums.Role;
import com.edusage.repository.*;
import com.edusage.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private AuthenticationManager authManager;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered.");
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        userRepository.save(user);

        if (req.getRole() == Role.STUDENT) {
            Student student = new Student();
            student.setUser(user);
            student.setRollNo("PENDING");
            studentRepository.save(student);
            student.setRollNo("STU-" + String.format("%04d", student.getId()));
            studentRepository.save(student);
        } else if (req.getRole() == Role.TEACHER) {
            Teacher teacher = new Teacher();
            teacher.setUser(user);
            teacher.setDepartment(req.getDepartment());
            teacherRepository.save(teacher);
        }

        Long entityId = resolveEntityId(user);
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getRole().name(), entityId, user.getName());
    }

    public AuthResponse login(LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found."));

        Long entityId = resolveEntityId(user);
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getRole().name(), entityId, user.getName());
    }

    private Long resolveEntityId(User user) {
        return switch (user.getRole()) {
            case STUDENT -> studentRepository.findByUserId(user.getId())
                    .map(s -> s.getId()).orElse(user.getId());
            case TEACHER -> teacherRepository.findByUserId(user.getId())
                    .map(t -> t.getId()).orElse(user.getId());
            default -> user.getId();
        };
    }
}
