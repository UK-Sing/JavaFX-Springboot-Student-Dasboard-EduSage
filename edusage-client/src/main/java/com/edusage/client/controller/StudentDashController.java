package com.edusage.client.controller;

import com.edusage.client.model.UserSession;
import com.edusage.client.service.ApiService;
import com.edusage.client.util.SceneManager;
import com.fasterxml.jackson.databind.*;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.nio.file.*;
import java.util.Iterator;
import java.util.Map;

public class StudentDashController {

    @FXML private Label welcomeLabel;
    @FXML private Label avgLabel;
    @FXML private Label movingAvgLabel;
    @FXML private Label attendanceLabel;
    @FXML private Label improvementLabel;
    @FXML private Label riskBadge;
    @FXML private LineChart<String, Number> scoreLineChart;
    @FXML private BarChart<String, Number> subjectBarChart;
    @FXML private VBox recommendationsBox;
    @FXML private ListView<String> quizListView;

    private final ObjectMapper mapper = new ObjectMapper();
    private final UserSession session = UserSession.getInstance();

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + session.getName());
        loadAnalytics();
        loadQuizzes();
        wireQuizDoubleClick();
    }

    private void loadAnalytics() {
        new Thread(() -> {
            try {
                String json = ApiService.getAnalytics(session.getUserId());
                JsonNode data = mapper.readTree(json);

                String avg         = String.format("%.2f%%", data.get("overallAverage").asDouble());
                String movingAvg   = String.format("%.2f%%", data.get("movingAverage").asDouble());
                String attendance  = String.format("%.2f%%", data.get("attendancePercentage").asDouble());
                String improvement = String.format("%+.2f%%", data.get("improvementRate").asDouble());
                String riskLevel   = data.get("riskLevel").asText();

                Platform.runLater(() -> {
                    avgLabel.setText(avg);
                    movingAvgLabel.setText(movingAvg);
                    attendanceLabel.setText(attendance);
                    improvementLabel.setText(improvement);
                    styleRiskBadge(riskLevel);
                    populateLineChart(data.get("scoreTimeline"));
                    populateBarChart(data.get("subjectAverages"));
                });

                String recJson = ApiService.getRecommendations(session.getUserId());
                JsonNode recs  = mapper.readTree(recJson);
                Platform.runLater(() -> populateRecommendations(recs));

            } catch (Exception e) {
                Platform.runLater(() -> showError("Failed to load analytics: " + e.getMessage()));
            }
        }).start();
    }

    private void styleRiskBadge(String risk) {
        switch (risk) {
            case "HIGH_RISK" -> {
                riskBadge.setText("High Risk");
                riskBadge.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-padding: 6 14; -fx-background-radius: 12; -fx-font-weight: bold;");
            }
            case "NEEDS_ATTENTION" -> {
                riskBadge.setText("Needs Attention");
                riskBadge.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-padding: 6 14; -fx-background-radius: 12; -fx-font-weight: bold;");
            }
            default -> {
                riskBadge.setText("On Track");
                riskBadge.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-padding: 6 14; -fx-background-radius: 12; -fx-font-weight: bold;");
            }
        }
    }

    private void populateLineChart(JsonNode timeline) {
        scoreLineChart.getData().clear();
        if (timeline == null || !timeline.isArray()) return;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Score %");
        for (JsonNode point : timeline) {
            series.getData().add(new XYChart.Data<>(
                    point.get("date").asText(),
                    point.get("percentage").asDouble()));
        }
        scoreLineChart.getData().add(series);
    }

    private void populateBarChart(JsonNode subjectAvgs) {
        subjectBarChart.getData().clear();
        if (subjectAvgs == null || subjectAvgs.isEmpty()) return;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Subject Average %");
        Iterator<Map.Entry<String, JsonNode>> fields = subjectAvgs.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue().asDouble()));
        }
        subjectBarChart.getData().add(series);
    }

    private void populateRecommendations(JsonNode recs) {
        recommendationsBox.getChildren().clear();
        if (recs == null || !recs.isArray() || recs.isEmpty()) {
            recommendationsBox.getChildren().add(new Label("No recommendations at this time. Great work!"));
            return;
        }
        for (JsonNode rec : recs) {
            HBox card = new HBox();
            card.setSpacing(12);
            String type = rec.get("type") != null ? rec.get("type").asText("INFO") : "INFO";
            String bg   = switch (type) {
                case "HIGH_RISK"  -> "-fx-background-color: #FEE2E2;";
                case "ATTENDANCE" -> "-fx-background-color: #FEF3C7;";
                case "DECLINING"  -> "-fx-background-color: #FEF3C7;";
                default           -> "-fx-background-color: #DBEAFE;";
            };
            card.setStyle(bg + " -fx-padding: 12; -fx-background-radius: 8;");
            Label msg = new Label(rec.get("message").asText());
            msg.setWrapText(true);
            card.getChildren().add(msg);
            recommendationsBox.getChildren().add(card);
        }
    }

    private void loadQuizzes() {
        new Thread(() -> {
            try {
                String json = ApiService.getActiveQuizzes();
                JsonNode quizzes = mapper.readTree(json);
                ObservableList<String> items = FXCollections.observableArrayList();
                for (JsonNode q : quizzes) {
                    items.add(q.get("id").asText() + " | " +
                              q.get("subject").asText() + " — " +
                              q.get("title").asText());
                }
                Platform.runLater(() -> quizListView.setItems(items));
            } catch (Exception e) {
                Platform.runLater(() -> showError("Could not load quizzes."));
            }
        }).start();
    }

    private void wireQuizDoubleClick() {
        quizListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selected = quizListView.getSelectionModel().getSelectedItem();
                if (selected == null) return;
                try {
                    Long quizId = Long.parseLong(selected.split("\\|")[0].trim());
                    QuizTakeController.pendingQuizId = quizId;
                    SceneManager.switchTo("quiz-take.fxml");
                } catch (Exception e) {
                    showError("Could not open quiz: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    public void handleDownloadReport() {
        new Thread(() -> {
            try {
                byte[] pdf = ApiService.downloadReport(session.getUserId());
                Path dest = Paths.get(System.getProperty("user.home"), "student-report.pdf");
                Files.write(dest, pdf);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Report Downloaded");
                    alert.setHeaderText(null);
                    alert.setContentText("Report saved to:\n" + dest.toAbsolutePath());
                    alert.showAndWait();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Download failed: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void handleLogout() {
        UserSession.getInstance().clearSession();
        try { SceneManager.switchTo("login.fxml"); }
        catch (Exception ignored) {}
    }

    private void showError(String msg) {
        Label err = new Label(msg);
        err.setStyle("-fx-text-fill: red;");
        recommendationsBox.getChildren().add(0, err);
    }
}
