package com.edusage.client.controller;

import com.edusage.client.model.UserSession;
import com.edusage.client.service.ApiService;
import com.edusage.client.util.SceneManager;
import com.fasterxml.jackson.databind.*;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminDashController {

    @FXML private Label welcomeLabel;
    @FXML private Label statStudents, statTeachers, statActive, statAtRisk, statAvg, statQuizzes;

    @FXML private TextField userSearchField;
    @FXML private TableView<UserRow> usersTable;
    @FXML private TableColumn<UserRow, Long>   userIdCol;
    @FXML private TableColumn<UserRow, String> userNameCol, userEmailCol, userRoleCol, userStatusCol, userJoinedCol;
    @FXML private TableColumn<UserRow, Void>   userActionsCol;

    @FXML private TextField studentSearchField;
    @FXML private TableView<StuRow> studentsTable;
    @FXML private TableColumn<StuRow, String> stuNameCol, stuRollCol, stuCourseCol, stuAvgCol, stuAttCol, stuRiskCol;

    private final ObjectMapper mapper = new ObjectMapper();
    private ObservableList<UserRow> allUsers      = FXCollections.observableArrayList();
    private ObservableList<StuRow>  allStudents   = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + UserSession.getInstance().getName());
        setupUserTable();
        setupStudentTable();
        loadStats();
        loadUsers();
        loadStudents();
    }

    private void setupUserTable() {
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        userNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        userEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        userRoleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        userStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        userJoinedCol.setCellValueFactory(new PropertyValueFactory<>("joined"));
        userActionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button toggleBtn = new Button();
            private final Button deleteBtn = new Button("Delete");
            private final HBox   box       = new HBox(6, toggleBtn, deleteBtn);
            {
                deleteBtn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 4;");
                toggleBtn.setStyle("-fx-padding: 4 10; -fx-background-radius: 4;");
                deleteBtn.setOnAction(e -> {
                    UserRow r = getTableRow().getItem();
                    if (r == null) return;
                    Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                            "Delete user '" + r.getName() + "'? This cannot be undone.", ButtonType.YES, ButtonType.NO);
                    conf.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b ->
                        new Thread(() -> {
                            try {
                                ApiService.deleteUser(r.getId());
                                Platform.runLater(() -> { allUsers.remove(r); loadStats(); });
                            } catch (Exception ex) {
                                Platform.runLater(() -> showAlert("Error", ex.getMessage()));
                            }
                        }).start());
                });
                toggleBtn.setOnAction(e -> {
                    UserRow r = getTableRow().getItem();
                    if (r == null) return;
                    boolean makeActive = r.getStatus().equals("INACTIVE");
                    new Thread(() -> {
                        try {
                            ApiService.toggleUserActive(r.getId(), makeActive);
                            Platform.runLater(() -> { loadUsers(); loadStats(); });
                        } catch (Exception ex) {
                            Platform.runLater(() -> showAlert("Error", ex.getMessage()));
                        }
                    }).start();
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    UserRow r = getTableRow().getItem();
                    boolean active = r.getStatus().equals("ACTIVE");
                    toggleBtn.setText(active ? "Deactivate" : "Activate");
                    toggleBtn.setStyle("-fx-background-color: " + (active ? "#E07B20" : "#1E7B45") +
                            "; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 4;");
                    setGraphic(box);
                }
            }
        });

        FilteredList<UserRow> filtered = new FilteredList<>(allUsers, p -> true);
        userSearchField.textProperty().addListener((obs, o, n) -> {
            String q = n == null ? "" : n.toLowerCase();
            filtered.setPredicate(r -> q.isEmpty()
                    || r.getName().toLowerCase().contains(q)
                    || r.getEmail().toLowerCase().contains(q)
                    || r.getRole().toLowerCase().contains(q));
        });
        usersTable.setItems(filtered);
    }

    private void setupStudentTable() {
        stuNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        stuRollCol.setCellValueFactory(new PropertyValueFactory<>("rollNo"));
        stuCourseCol.setCellValueFactory(new PropertyValueFactory<>("course"));
        stuAvgCol.setCellValueFactory(new PropertyValueFactory<>("avg"));
        stuAttCol.setCellValueFactory(new PropertyValueFactory<>("attendance"));
        stuRiskCol.setCellValueFactory(new PropertyValueFactory<>("riskLevel"));

        FilteredList<StuRow> filtered = new FilteredList<>(allStudents, p -> true);
        studentSearchField.textProperty().addListener((obs, o, n) -> {
            String q = n == null ? "" : n.toLowerCase();
            filtered.setPredicate(r -> q.isEmpty()
                    || r.getName().toLowerCase().contains(q)
                    || r.getRollNo().toLowerCase().contains(q));
        });
        studentsTable.setItems(filtered);

        studentsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                StuRow sel = studentsTable.getSelectionModel().getSelectedItem();
                if (sel == null) return;
                try {
                    StudentProfileController.pendingStudentId = sel.getStudentId();
                    StudentProfileController.returnScene = "admin-dashboard.fxml";
                    SceneManager.switchTo("student-profile.fxml");
                } catch (Exception e) {
                    showAlert("Error", e.getMessage());
                }
            }
        });
    }

    private void loadStats() {
        new Thread(() -> {
            try {
                String json = ApiService.getAdminStats();
                JsonNode s = mapper.readTree(json);
                Platform.runLater(() -> {
                    statStudents.setText(String.valueOf(s.path("totalStudents").asLong()));
                    statTeachers.setText(String.valueOf(s.path("totalTeachers").asLong()));
                    statActive.setText(String.valueOf(s.path("activeUsers").asLong()));
                    statAtRisk.setText(String.valueOf(s.path("atRiskCount").asLong()));
                    statAvg.setText(String.format("%.2f%%", s.path("systemAvgScore").asDouble()));
                    statQuizzes.setText(String.valueOf(s.path("totalQuizzes").asLong()));
                });
            } catch (Exception e) {
                Platform.runLater(() -> statStudents.setText("!"));
            }
        }).start();
    }

    private void loadUsers() {
        new Thread(() -> {
            try {
                String json = ApiService.getAllUsers();
                JsonNode users = mapper.readTree(json);
                ObservableList<UserRow> rows = FXCollections.observableArrayList();
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
                for (JsonNode u : users) {
                    String joined = "N/A";
                    try {
                        String raw = u.path("createdAt").asText("");
                        if (!raw.isEmpty()) {
                            joined = LocalDateTime.parse(raw.substring(0, 19)).format(fmt);
                        }
                    } catch (Exception ignored) {}
                    rows.add(new UserRow(
                            u.path("id").asLong(),
                            u.path("name").asText(),
                            u.path("email").asText(),
                            u.path("role").asText(),
                            u.path("active").asBoolean(true) ? "ACTIVE" : "INACTIVE",
                            joined));
                }
                Platform.runLater(() -> allUsers.setAll(rows));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error loading users", e.getMessage()));
            }
        }).start();
    }

    private void loadStudents() {
        new Thread(() -> {
            try {
                String json = ApiService.getAdminStudents();
                JsonNode students = mapper.readTree(json);
                ObservableList<StuRow> rows = FXCollections.observableArrayList();
                for (JsonNode s : students) {
                    rows.add(new StuRow(
                            s.path("studentId").asLong(),
                            s.path("name").asText(),
                            s.path("rollNo").asText("N/A"),
                            s.path("course").asText("N/A"),
                            String.format("%.2f%%", s.path("overallAverage").asDouble()),
                            String.format("%.2f%%", s.path("attendancePercentage").asDouble()),
                            s.path("riskLevel").asText("N/A")));
                }
                Platform.runLater(() -> allStudents.setAll(rows));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error loading students", e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void showCreateUserDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New User");
        dialog.setHeaderText("Add a new user to the system");
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField  = new TextField(); nameField.setPromptText("Full name");
        TextField emailField = new TextField(); emailField.setPromptText("Email address");
        PasswordField passField = new PasswordField(); passField.setPromptText("Password");
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("STUDENT", "TEACHER", "ADMIN");
        roleBox.setValue("STUDENT");
        TextField extraField = new TextField(); extraField.setPromptText("Department");
        Label extraLabel = new Label("Department:");
        extraField.setVisible(false); extraField.setManaged(false);
        extraLabel.setVisible(false); extraLabel.setManaged(false);

        roleBox.valueProperty().addListener((obs, o, n) -> {
            boolean isTeacher = "TEACHER".equals(n);
            extraField.setVisible(isTeacher); extraField.setManaged(isTeacher);
            extraLabel.setVisible(isTeacher); extraLabel.setManaged(isTeacher);
        });

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.add(new Label("Name:"),     0, 0); grid.add(nameField,  1, 0);
        grid.add(new Label("Email:"),    0, 1); grid.add(emailField, 1, 1);
        grid.add(new Label("Password:"), 0, 2); grid.add(passField,  1, 2);
        grid.add(new Label("Role:"),     0, 3); grid.add(roleBox,    1, 3);
        grid.add(extraLabel,             0, 4); grid.add(extraField, 1, 4);
        pane.setContent(grid);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;
            String role = roleBox.getValue();
            String extra = extraField.getText().replace("\"", "'");
            String body = String.format(
                "{\"name\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"role\":\"%s\",\"rollNo\":\"%s\",\"department\":\"%s\"}",
                nameField.getText().replace("\"", "'"),
                emailField.getText(),
                passField.getText(),
                role,
                role.equals("STUDENT") ? extra : "",
                role.equals("TEACHER") ? extra : "");
            new Thread(() -> {
                try {
                    ApiService.createAdminUser(body);
                    Platform.runLater(() -> { loadUsers(); loadStats(); });
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("Create failed", e.getMessage()));
                }
            }).start();
        });
    }

    @FXML
    public void handleLogout() {
        UserSession.getInstance().clearSession();
        try { SceneManager.switchTo("login.fxml"); } catch (Exception ignored) {}
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setContentText(msg); a.showAndWait();
    }

    public static class UserRow {
        private final Long id; private final String name, email, role, status, joined;
        public UserRow(Long id, String name, String email, String role, String status, String joined) {
            this.id=id; this.name=name; this.email=email; this.role=role; this.status=status; this.joined=joined;
        }
        public Long   getId()     { return id; }
        public String getName()   { return name; }
        public String getEmail()  { return email; }
        public String getRole()   { return role; }
        public String getStatus() { return status; }
        public String getJoined() { return joined; }
    }

    public static class StuRow {
        private final Long studentId;
        private final String name, rollNo, course, avg, attendance, riskLevel;
        public StuRow(Long studentId, String name, String rollNo, String course,
                      String avg, String attendance, String riskLevel) {
            this.studentId=studentId; this.name=name; this.rollNo=rollNo; this.course=course;
            this.avg=avg; this.attendance=attendance; this.riskLevel=riskLevel;
        }
        public Long   getStudentId()  { return studentId; }
        public String getName()       { return name; }
        public String getRollNo()     { return rollNo; }
        public String getCourse()     { return course; }
        public String getAvg()        { return avg; }
        public String getAttendance() { return attendance; }
        public String getRiskLevel()  { return riskLevel; }
    }
}
