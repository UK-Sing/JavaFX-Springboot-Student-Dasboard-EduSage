package com.edusage.client.controller;

import com.edusage.client.model.UserSession;
import com.edusage.client.service.ApiService;
import com.edusage.client.util.SceneManager;
import com.edusage.client.controller.StudentProfileController;
import com.fasterxml.jackson.databind.*;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class TeacherDashController {

    @FXML private Label welcomeLabel;
    @FXML private TableView<StudentRow> studentTable;
    @FXML private TableColumn<StudentRow, String> nameCol;
    @FXML private TableColumn<StudentRow, String> rollNoCol;
    @FXML private TableColumn<StudentRow, String> riskCol;
    @FXML private TableColumn<StudentRow, Double> avgCol;
    @FXML private TableColumn<StudentRow, Double> attendanceCol;
    @FXML private Label totalStudentsLabel;
    @FXML private Label atRiskCountLabel;

    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        welcomeLabel.setText("Teacher Dashboard — " + UserSession.getInstance().getName());
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        rollNoCol.setCellValueFactory(new PropertyValueFactory<>("rollNo"));
        riskCol.setCellValueFactory(new PropertyValueFactory<>("riskLevel"));
        avgCol.setCellValueFactory(new PropertyValueFactory<>("overallAverage"));
        attendanceCol.setCellValueFactory(new PropertyValueFactory<>("attendancePercentage"));
        loadClassOverview();
        wireRowDoubleClick();
    }

    private void wireRowDoubleClick() {
        studentTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                StudentRow selected = studentTable.getSelectionModel().getSelectedItem();
                if (selected == null) return;
                try {
                    StudentProfileController.pendingStudentId = selected.getStudentId();
                    StudentProfileController.returnScene = "teacher-dashboard.fxml";
                    SceneManager.switchTo("student-profile.fxml");
                } catch (Exception e) {
                    System.err.println("Could not open profile: " + e.getMessage());
                }
            }
        });
    }

    private void loadClassOverview() {
        new Thread(() -> {
            try {
                String json = ApiService.getClassOverview();
                JsonNode students = mapper.readTree(json);
                ObservableList<StudentRow> rows = FXCollections.observableArrayList();
                long atRisk = 0;
                for (JsonNode s : students) {
                    String risk = s.get("riskLevel").asText();
                    if (!risk.equals("ON_TRACK")) atRisk++;
                    rows.add(new StudentRow(
                            s.get("studentId").asLong(),
                            s.get("name").asText(),
                            s.get("rollNo") != null ? s.get("rollNo").asText("N/A") : "N/A",
                            risk,
                            s.get("overallAverage").asDouble(),
                            s.get("attendancePercentage").asDouble()));
                }
                long finalAtRisk = atRisk;
                Platform.runLater(() -> {
                    studentTable.setItems(rows);
                    totalStudentsLabel.setText("Total: " + rows.size());
                    atRiskCountLabel.setText("At Risk: " + finalAtRisk);
                });
            } catch (Exception e) {
                Platform.runLater(() -> System.err.println("Error loading overview: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void showLeaderboard() {
        new Thread(() -> {
            try {
                String json = ApiService.getLeaderboard();
                JsonNode board = mapper.readTree(json);
                StringBuilder sb = new StringBuilder("Rank  Name                   Avg%\n");
                sb.append("─".repeat(40)).append("\n");
                int rank = 1;
                for (JsonNode s : board) {
                    sb.append(String.format("%-5d %-22s %.2f%%\n",
                            rank++,
                            s.get("name").asText(),
                            s.get("overallAverage").asDouble()));
                }
                String text = sb.toString();
                Platform.runLater(() -> {
                    javafx.scene.control.Alert alert =
                            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("Leaderboard — Top 10");
                    alert.setHeaderText("Students ranked by overall average");
                    alert.setContentText(text);
                    alert.showAndWait();
                });
            } catch (Exception e) {
                Platform.runLater(() -> System.err.println("Leaderboard error: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void handleLogout() {
        UserSession.getInstance().clearSession();
        try { SceneManager.switchTo("login.fxml"); } catch (Exception ignored) {}
    }

    public static class StudentRow {
        private final Long studentId;
        private final String name;
        private final String rollNo;
        private final String riskLevel;
        private final Double overallAverage;
        private final Double attendancePercentage;

        public StudentRow(Long studentId, String name, String rollNo, String riskLevel, Double avg, Double att) {
            this.studentId = studentId;
            this.name = name;
            this.rollNo = rollNo;
            this.riskLevel = riskLevel;
            this.overallAverage = avg;
            this.attendancePercentage = att;
        }

        public Long   getStudentId()            { return studentId; }
        public String getName()                 { return name; }
        public String getRollNo()               { return rollNo; }
        public String getRiskLevel()            { return riskLevel; }
        public Double getOverallAverage()       { return overallAverage; }
        public Double getAttendancePercentage() { return attendancePercentage; }
    }
}
