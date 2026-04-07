package com.edusage.service;

import com.edusage.model.Material;
import com.edusage.model.Teacher;
import com.edusage.repository.MaterialRepository;
import com.edusage.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Service
public class ContentService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Autowired private MaterialRepository materialRepository;
    @Autowired private TeacherRepository teacherRepository;

    public Material uploadMaterial(Long teacherId, String title, String subject, MultipartFile file)
            throws IOException {

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        String originalName = file.getOriginalFilename();
        String extension    = originalName.substring(originalName.lastIndexOf("."));
        String savedName    = System.currentTimeMillis() + "_" + originalName.replaceAll("[^a-zA-Z0-9._-]", "_");

        Path dir  = Paths.get(uploadDir, "materials");
        Files.createDirectories(dir);
        Path dest = dir.resolve(savedName);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        Material material = new Material();
        material.setTeacher(teacher);
        material.setTitle(title);
        material.setSubject(subject);
        material.setFilePath("materials/" + savedName);
        material.setFileType(extension.toUpperCase().replace(".", ""));

        return materialRepository.save(material);
    }

    public List<Material> getBySubject(String subject) {
        return materialRepository.findBySubject(subject);
    }

    public List<Material> getByTeacher(Long teacherId) {
        return materialRepository.findByTeacherId(teacherId);
    }
}
