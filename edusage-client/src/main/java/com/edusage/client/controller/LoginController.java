package com.edusage.client.controller;

import com.edusage.client.model.UserSession;
import com.edusage.client.service.ApiService;
import com.edusage.client.util.SceneManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Email and password are required.");
            return;
        }

        errorLabel.setVisible(false);

        new Thread(() -> {
            try {
                String response = ApiService.login(email, password);
                JsonNode json   = mapper.readTree(response);

                String token  = json.get("token").asText();
                String role   = json.get("role").asText();
                Long   userId = json.get("userId").asLong();
                String name   = json.get("name").asText();

                UserSession.getInstance().setSession(token, role, userId, name);

                Platform.runLater(() -> {
                    try {
                        switch (role) {
                            case "STUDENT" -> SceneManager.switchTo("student-dashboard.fxml");
                            case "TEACHER" -> SceneManager.switchTo("teacher-dashboard.fxml");
                            case "ADMIN"   -> SceneManager.switchTo("admin-dashboard.fxml");
                            default        -> showError("Unknown role: " + role);
                        }
                    } catch (Exception e) {
                        showError("Navigation error: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Login failed: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void goToRegister() {
        try { SceneManager.switchTo("register.fxml"); }
        catch (Exception e) { showError("Navigation error."); }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
