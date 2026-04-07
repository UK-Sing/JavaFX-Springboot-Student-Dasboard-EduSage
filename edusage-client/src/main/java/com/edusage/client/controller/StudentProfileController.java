package com.edusage.client.controller;

import com.edusage.client.service.ApiService;
import com.edusage.client.util.SceneManager;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class StudentProfileController {

    public static Long   pendingStudentId = null;
    public static String returnScene      = "teacher-dashboard.fxml";

    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label rollNoLabel;
    @FXML private Label courseLabel;
    @FXML private Label creditsLabel;
    @FXML private Label joinedLabel;
    @FXML private Label durationLabel;
    @FXML private Label riskBadge;
    @FXML private Label avgLabel;
    @FXML private Label movingAvgLabel;
    @FXML private Label attendanceLabel;
    @FXML private Label improvementLabel;
    @FXML private Label quizzesLabel;
    @FXML private Label sessionCountLabel;
    @FXML private ImageView photoView;
    @FXML private Label initialsLabel;
    @FXML private VBox subjectsBox;
    @FXML private VBox counsellingBox;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private Long studentId;

    @FXML
    public void initialize() {
        studentId = pendingStudentId;
        if (studentId == null) return;
        loadProfile();
        loadCounsellingSessions();
    }

    private void loadProfile() {
        new Thread(() -> {
            try {
                String json = ApiService.getStudentProfile(studentId);
                JsonNode p = mapper.readTree(json);
                Platform.runLater(() -> {
                    String name  = p.get("name").asText();
                    String email = p.get("email").asText();
                    String roll  = p.path("rollNo").asText("N/A");
                    String course= p.path("course").asText("N/A");
                    int credits  = p.path("credits").asInt(0);
                    String joined= p.path("joinedAt").asText("").substring(0, 10);
                    String dur   = p.path("memberDuration").asText("N/A");
                    String risk  = p.path("riskLevel").asText("N/A");
                    double avg   = p.path("overallAverage").asDouble();
                    double mav   = p.path("movingAverage").asDouble();
                    double att   = p.path("attendancePercentage").asDouble();
                    double imp   = p.path("improvementRate").asDouble();
                    int quizzes  = p.path("totalQuizzesTaken").asInt(0);
                    int sessions = p.path("counsellingSessionCount").asInt(0);
                    String photoUrl = p.path("photoUrl").asText(null);

                    nameLabel.setText(name);
                    emailLabel.setText(email);
                    rollNoLabel.setText("Roll: " + roll);
                    courseLabel.setText(course);
                    creditsLabel.setText(String.valueOf(credits));
                    joinedLabel.setText("Joined: " + joined);
                    durationLabel.setText(dur + " enrolled");
                    avgLabel.setText(String.format("%.2f%%", avg));
                    movingAvgLabel.setText(String.format("%.2f%%", mav));
                    attendanceLabel.setText(String.format("%.2f%%", att));
                    improvementLabel.setText(String.format("%+.2f%%", imp));
                    quizzesLabel.setText(String.valueOf(quizzes));
                    sessionCountLabel.setText(String.valueOf(sessions));

                    styleRiskBadge(risk);

                    if (photoUrl != null && !photoUrl.isBlank()) {
                        try {
                            photoView.setImage(new Image(photoUrl, true));
                            initialsLabel.setVisible(false);
                            photoView.setVisible(true);
                        } catch (Exception ignored) {
                            showInitials(name);
                        }
                    } else {
                        showInitials(name);
                    }

                    JsonNode subjects = p.path("subjectAverages");
                    if (subjects != null && !subjects.isMissingNode()) {
                        subjects.fields().forEachRemaining(e -> {
                            HBox row = new HBox(12);
                            row.setStyle("-fx-background-color: #F9FAFB; -fx-padding: 8 12; -fx-background-radius: 6;");
                            Label subj = new Label(e.getKey());
                            subj.setStyle("-fx-font-weight: bold; -fx-min-width: 120;");
                            double pct = e.getValue().asDouble();
                            ProgressBar bar = new ProgressBar(pct / 100.0);
                            bar.setPrefWidth(200);
                            bar.setStyle(pct >= 70 ? "-fx-accent: #1E7B45;" : pct >= 50 ? "-fx-accent: #E07B20;" : "-fx-accent: #DC2626;");
                            Label val = new Label(String.format("%.2f%%", pct));
                            HBox.setHgrow(bar, Priority.ALWAYS);
                            row.getChildren().addAll(subj, bar, val);
                            subjectsBox.getChildren().add(row);
                        });
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> nameLabel.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    private void loadCounsellingSessions() {
        new Thread(() -> {
            try {
                String json = ApiService.getCounsellingSessions(studentId);
                JsonNode sessions = mapper.readTree(json);
                Platform.runLater(() -> {
                    counsellingBox.getChildren().clear();
                    if (!sessions.isArray() || sessions.size() == 0) {
                        counsellingBox.getChildren().add(
                            new Label("No counselling sessions recorded.") {{
                                setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic;");
                            }});
                        return;
                    }
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
                    for (JsonNode s : sessions) {
                        VBox card = buildSessionCard(s, fmt);
                        counsellingBox.getChildren().add(card);
                    }
                    sessionCountLabel.setText(String.valueOf(sessions.size()));
                });
            } catch (Exception e) {
                Platform.runLater(() -> counsellingBox.getChildren().add(
                    new Label("Failed to load sessions: " + e.getMessage())));
            }
        }).start();
    }

    private VBox buildSessionCard(JsonNode s, DateTimeFormatter fmt) {
        String outcome  = s.path("outcome").asText("NEUTRAL");
        String date     = s.path("sessionDate").asText("");
        String counsel  = s.path("counsellorName").asText("N/A");
        String comments = s.path("comments").asText("");

        String outcomeColor = switch (outcome) {
            case "POSITIVE"       -> "#D1FAE5";
            case "NEEDS_FOLLOWUP" -> "#FEE2E2";
            default               -> "#FEF3C7";
        };
        String outcomeFg = switch (outcome) {
            case "POSITIVE"       -> "#065F46";
            case "NEEDS_FOLLOWUP" -> "#991B1B";
            default               -> "#92400E";
        };

        VBox card = new VBox(6);
        card.setStyle("-fx-background-color: " + outcomeColor + "; -fx-padding: 12; -fx-background-radius: 8;");
        card.setPadding(new Insets(12));

        HBox header = new HBox(12);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label dateLbl = new Label(date);
        dateLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");
        Label outcomeLbl = new Label(outcome.replace("_", " "));
        outcomeLbl.setStyle("-fx-background-color: " + outcomeFg + "; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 4; -fx-font-size: 11;");
        Label counselLbl = new Label("Counsellor: " + counsel);
        counselLbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(dateLbl, outcomeLbl, spacer, counselLbl);

        Label commentsLbl = new Label(comments.isBlank() ? "No comments recorded." : comments);
        commentsLbl.setStyle("-fx-text-fill: #374151; -fx-font-size: 13;");
        commentsLbl.setWrapText(true);

        card.getChildren().addAll(header, commentsLbl);
        return card;
    }

    @FXML
    public void showAddSessionDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Log Counselling Session");
        dialog.setHeaderText("Student: " + nameLabel.getText());

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField counsellorField = new TextField();
        counsellorField.setPromptText("Counsellor name");
        ComboBox<String> outcomeBox = new ComboBox<>();
        outcomeBox.getItems().addAll("POSITIVE", "NEUTRAL", "NEEDS_FOLLOWUP");
        outcomeBox.setValue("NEUTRAL");
        TextArea commentsArea = new TextArea();
        commentsArea.setPromptText("Session notes and outcome details...");
        commentsArea.setPrefRowCount(4);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.add(new Label("Date:"),       0, 0); grid.add(datePicker,     1, 0);
        grid.add(new Label("Counsellor:"), 0, 1); grid.add(counsellorField,1, 1);
        grid.add(new Label("Outcome:"),    0, 2); grid.add(outcomeBox,      1, 2);
        grid.add(new Label("Comments:"),   0, 3); grid.add(commentsArea,    1, 3);
        pane.setContent(grid);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;
            String body = String.format(
                "{\"sessionDate\":\"%s\",\"counsellorName\":\"%s\",\"outcome\":\"%s\",\"comments\":\"%s\"}",
                datePicker.getValue(),
                counsellorField.getText().replace("\"", "'"),
                outcomeBox.getValue(),
                commentsArea.getText().replace("\"", "'").replace("\n", " "));
            new Thread(() -> {
                try {
                    ApiService.addCounsellingSession(studentId, body);
                    Platform.runLater(this::loadCounsellingSessions);
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("Error", e.getMessage()));
                }
            }).start();
        });
    }

    @FXML
    public void handleBack() {
        try { SceneManager.switchTo(returnScene); }
        catch (Exception ignored) {}
    }

    private void styleRiskBadge(String risk) {
        String bg = switch (risk) {
            case "HIGH_RISK"   -> "#FEE2E2";
            case "MEDIUM_RISK" -> "#FEF3C7";
            default            -> "#D1FAE5";
        };
        String fg = switch (risk) {
            case "HIGH_RISK"   -> "#991B1B";
            case "MEDIUM_RISK" -> "#92400E";
            default            -> "#065F46";
        };
        riskBadge.setText(risk.replace("_", " "));
        riskBadge.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-padding: 6 16; -fx-background-radius: 12;");
    }

    private void showInitials(String name) {
        photoView.setVisible(false);
        initialsLabel.setVisible(true);
        String[] parts = name.split(" ");
        String initials = parts.length >= 2
            ? "" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)
            : name.substring(0, Math.min(2, name.length())).toUpperCase();
        initialsLabel.setText(initials);
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
