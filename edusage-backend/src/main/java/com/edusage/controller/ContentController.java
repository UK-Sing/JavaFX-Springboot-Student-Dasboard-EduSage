package com.edusage.controller;

import com.edusage.model.Material;
import com.edusage.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    @Autowired private ContentService contentService;

    @PostMapping("/upload")
    public ResponseEntity<Material> upload(
            @RequestParam Long teacherId,
            @RequestParam String title,
            @RequestParam String subject,
            @RequestParam MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contentService.uploadMaterial(teacherId, title, subject, file));
    }

    @GetMapping("/subject/{subject}")
    public ResponseEntity<List<Material>> getBySubject(@PathVariable String subject) {
        return ResponseEntity.ok(contentService.getBySubject(subject));
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Material>> getByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(contentService.getByTeacher(teacherId));
    }
}
