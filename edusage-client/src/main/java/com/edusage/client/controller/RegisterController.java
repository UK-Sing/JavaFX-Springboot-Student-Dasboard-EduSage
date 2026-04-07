package com.edusage.client.controller;

import com.edusage.client.model.UserSession;
import com.edusage.client.service.ApiService;
import com.edusage.client.util.SceneManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label extraLabel;
    @FXML private TextField extraField;
    @FXML private Label errorLabel;

    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList("STUDENT", "TEACHER"));
        roleCombo.setOnAction(e -> {
            String role = roleCombo.getValue();
            if ("STUDENT".equals(role)) {
                extraLabel.setVisible(false);
                extraLabel.setManaged(false);
                extraField.setVisible(false);
                extraField.setManaged(false);
            } else if ("TEACHER".equals(role)) {
                extraLabel.setText("Department");
                extraLabel.setVisible(true);
                extraLabel.setManaged(true);
                extraField.setPromptText("Enter department");
                extraField.setVisible(true);
                extraField.setManaged(true);
            }
        });
    }

    @FXML
    public void handleRegister() {
        String name     = nameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();
        String role     = roleCombo.getValue();
        String extra    = extraField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            showError("All fields are required.");
            return;
        }

        errorLabel.setVisible(false);

        new Thread(() -> {
            try {
                String response = ApiService.register(name, email, password, role, extra);
                JsonNode json   = mapper.readTree(response);

                String token  = json.get("token").asText();
                String rRole  = json.get("role").asText();
                Long   userId = json.get("userId").asLong();
                String uName  = json.get("name").asText();

                UserSession.getInstance().setSession(token, rRole, userId, uName);

                Platform.runLater(() -> {
                    try {
                        switch (rRole) {
                            case "STUDENT" -> SceneManager.switchTo("student-dashboard.fxml");
                            case "TEACHER" -> SceneManager.switchTo("teacher-dashboard.fxml");
                            default        -> SceneManager.switchTo("admin-dashboard.fxml");
                        }
                    } catch (Exception e) {
                        showError("Navigation error: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Registration failed: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void goToLogin() {
        try { SceneManager.switchTo("login.fxml"); }
        catch (Exception e) { showError("Navigation error."); }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
