package com.edusage.client.controller;

import com.edusage.client.model.UserSession;
import com.edusage.client.service.ApiService;
import com.edusage.client.util.SceneManager;
import com.fasterxml.jackson.databind.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.*;

public class QuizTakeController {

    public static Long pendingQuizId = null;

    @FXML private VBox questionsBox;
    @FXML private Label quizTitleLabel;
    @FXML private Label resultLabel;
    @FXML private Label scoreLabel;
    @FXML private Button submitButton;

    private Long quizId;
    private JsonNode questions;
    private final Map<Long, ToggleGroup> answerGroups = new LinkedHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        this.quizId = pendingQuizId;
        if (quizId != null) {
            loadQuiz(quizId);
        } else {
            resultLabel.setText("No quiz selected.");
        }
    }

    public void loadQuiz(Long quizId) {
        this.quizId = quizId;
        new Thread(() -> {
            try {
                String json = ApiService.getQuizQuestions(quizId);
                questions = mapper.readTree(json);
                Platform.runLater(this::renderQuestions);
            } catch (Exception e) {
                Platform.runLater(() -> resultLabel.setText("Failed to load quiz."));
            }
        }).start();
    }

    private void renderQuestions() {
        questionsBox.getChildren().clear();
        answerGroups.clear();
        if (questions == null || !questions.isArray()) return;

        quizTitleLabel.setText("Quiz #" + quizId);

        int num = 1;
        for (JsonNode q : questions) {
            Long qId = q.get("id").asLong();
            VBox qBox = new VBox(6);
            qBox.setStyle("-fx-padding: 12; -fx-background-color: white; -fx-background-radius: 8;");

            Label qLabel = new Label(num + ". " + q.get("questionText").asText());
            qLabel.setWrapText(true);
            qLabel.setStyle("-fx-font-weight: bold;");

            ToggleGroup tg = new ToggleGroup();
            answerGroups.put(qId, tg);

            String[] labels  = {"A", "B", "C", "D"};
            String[] options = {
                q.get("optionA").asText(),
                q.get("optionB").asText(),
                q.get("optionC").asText(),
                q.get("optionD").asText()
            };

            qBox.getChildren().add(qLabel);
            for (int i = 0; i < 4; i++) {
                RadioButton rb = new RadioButton(labels[i] + ". " + options[i]);
                rb.setToggleGroup(tg);
                rb.setUserData(labels[i]);
                qBox.getChildren().add(rb);
            }

            questionsBox.getChildren().add(qBox);
            num++;
        }
    }

    @FXML
    public void handleSubmit() {
        Map<Long, String> answers = new LinkedHashMap<>();
        for (Map.Entry<Long, ToggleGroup> entry : answerGroups.entrySet()) {
            Toggle selected = entry.getValue().getSelectedToggle();
            if (selected != null) {
                answers.put(entry.getKey(), (String) selected.getUserData());
            }
        }

        if (answers.size() < answerGroups.size()) {
            resultLabel.setText("Please answer all questions before submitting.");
            return;
        }

        submitButton.setDisable(true);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"studentId\":").append(UserSession.getInstance().getUserId());
        sb.append(",\"quizId\":").append(quizId);
        sb.append(",\"answers\":{");
        boolean first = true;
        for (Map.Entry<Long, String> a : answers.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(a.getKey()).append("\":\"").append(a.getValue()).append("\"");
            first = false;
        }
        sb.append("}}");

        new Thread(() -> {
            try {
                String response = ApiService.submitQuiz(sb.toString());
                JsonNode result = mapper.readTree(response);
                int marks  = result.get("marks").asInt();
                int total  = result.get("totalMarks").asInt();
                double pct = result.get("percentage").asDouble();

                Platform.runLater(() -> {
                    scoreLabel.setText(String.format("Your Score: %d / %d (%.2f%%)", marks, total, pct));
                    resultLabel.setText(pct >= 60 ? "✓ Good job!" : "✗ Keep practicing!");
                    submitButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    resultLabel.setText("Submission failed: " + e.getMessage());
                    submitButton.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    public void goBackToDashboard() {
        try { SceneManager.switchTo("student-dashboard.fxml"); }
        catch (Exception ignored) {}
    }
}
