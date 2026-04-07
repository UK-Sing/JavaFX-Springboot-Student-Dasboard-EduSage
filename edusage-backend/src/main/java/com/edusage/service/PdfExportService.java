package com.edusage.service;

import com.edusage.dto.response.AnalyticsResponse;
import com.edusage.model.Student;
import com.edusage.repository.StudentRepository;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.*;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class PdfExportService {

    @Autowired private AnalyticsService analyticsService;
    @Autowired private StudentRepository studentRepository;

    public byte[] generateReport(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        AnalyticsResponse a = analyticsService.computeFullAnalytics(studentId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf  = new PdfDocument(writer);
            Document doc     = new Document(pdf);

            doc.add(new Paragraph("EduSage FX — Student Report")
                    .setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Student: " + student.getUser().getName()).setBold());
            doc.add(new Paragraph("Roll No: " + student.getRollNo()));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Performance Metrics").setBold().setFontSize(14));

            Table metricTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .useAllAvailableWidth();
            metricTable.addHeaderCell("Metric");
            metricTable.addHeaderCell("Value");

            metricTable.addCell("Overall Average");
            metricTable.addCell(String.format("%.2f%%", a.getOverallAverage()));
            metricTable.addCell("Moving Average (last 5)");
            metricTable.addCell(String.format("%.2f%%", a.getMovingAverage()));
            metricTable.addCell("Attendance");
            metricTable.addCell(String.format("%.2f%%", a.getAttendancePercentage()));
            metricTable.addCell("Improvement Rate");
            metricTable.addCell(String.format("%+.2f%%", a.getImprovementRate()));
            metricTable.addCell("Risk Level");
            metricTable.addCell(a.getRiskLevel() != null ? a.getRiskLevel().name() : "N/A");

            doc.add(metricTable);
            doc.add(new Paragraph(" "));

            if (a.getSubjectAverages() != null && !a.getSubjectAverages().isEmpty()) {
                doc.add(new Paragraph("Subject Averages").setBold().setFontSize(14));
                Table subjectTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                        .useAllAvailableWidth();
                subjectTable.addHeaderCell("Subject");
                subjectTable.addHeaderCell("Average %");
                for (Map.Entry<String, Double> entry : a.getSubjectAverages().entrySet()) {
                    subjectTable.addCell(entry.getKey());
                    subjectTable.addCell(String.format("%.2f%%", entry.getValue()));
                }
                doc.add(subjectTable);
            }

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }
}
