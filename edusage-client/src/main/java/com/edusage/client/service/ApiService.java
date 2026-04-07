package com.edusage.client.service;

import com.edusage.client.model.UserSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.Duration;

public class ApiService {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient HTTP  = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public static String login(String email, String password) throws Exception {
        String body = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        return post("/auth/login", body, false);
    }

    public static String register(String name, String email, String password,
                                   String role, String extraField) throws Exception {
        String body = String.format(
                "{\"name\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"role\":\"%s\",\"rollNo\":\"%s\",\"department\":\"%s\"}",
                name, email, password, role,
                role.equals("STUDENT") ? extraField : "",
                role.equals("TEACHER") ? extraField : "");
        return post("/auth/register", body, false);
    }

    public static String getAnalytics(Long studentId) throws Exception {
        return get("/student/" + studentId + "/analytics");
    }

    public static String getRecommendations(Long studentId) throws Exception {
        return get("/student/" + studentId + "/recommendations");
    }

    public static String getClassOverview() throws Exception {
        return get("/teacher/class-overview");
    }

    public static String getAtRiskStudents() throws Exception {
        return get("/teacher/at-risk");
    }

    public static String getActiveQuizzes() throws Exception {
        return get("/quiz/active");
    }

    public static String getQuizQuestions(Long quizId) throws Exception {
        return get("/quiz/" + quizId + "/questions");
    }

    public static String submitQuiz(String jsonBody) throws Exception {
        return post("/quiz/submit", jsonBody, true);
    }

    public static String createQuiz(String jsonBody) throws Exception {
        return post("/quiz/create", jsonBody, true);
    }

    public static String getAdminStats() throws Exception {
        return get("/admin/stats");
    }

    public static String getAllUsers() throws Exception {
        return get("/admin/users");
    }

    public static String createAdminUser(String jsonBody) throws Exception {
        return post("/admin/users", jsonBody, true);
    }

    public static String deleteUser(Long userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/admin/users/" + userId))
                .header("Authorization", UserSession.getInstance().getAuthHeader())
                .DELETE()
                .build();
        HttpResponse<String> resp;
        try {
            resp = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (java.net.ConnectException e) {
            throw new IOException("Cannot reach backend at " + BASE_URL + ". Is the server running?", e);
        }
        if (resp.statusCode() >= 400) throw new IOException("HTTP " + resp.statusCode());
        return resp.body();
    }

    public static String toggleUserActive(Long userId, boolean activate) throws Exception {
        String action = activate ? "activate" : "deactivate";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/admin/users/" + userId + "/" + action))
                .header("Authorization", UserSession.getInstance().getAuthHeader())
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> resp;
        try {
            resp = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (java.net.ConnectException e) {
            throw new IOException("Cannot reach backend at " + BASE_URL + ". Is the server running?", e);
        }
        if (resp.statusCode() >= 400) throw new IOException("HTTP " + resp.statusCode());
        return resp.body();
    }

    public static String getAdminStudents() throws Exception {
        return get("/teacher/class-overview");
    }

    public static String getStudentProfile(Long studentId) throws Exception {
        return get("/student/" + studentId + "/profile");
    }

    public static String getCounsellingSessions(Long studentId) throws Exception {
        return get("/student/" + studentId + "/counselling");
    }

    public static String addCounsellingSession(Long studentId, String jsonBody) throws Exception {
        return post("/teacher/student/" + studentId + "/counselling", jsonBody, true);
    }

    public static String getLeaderboard() throws Exception {
        return get("/teacher/leaderboard");
    }

    public static byte[] downloadReport(Long studentId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/student/" + studentId + "/report"))
                .header("Authorization", UserSession.getInstance().getAuthHeader())
                .GET()
                .build();
        HttpResponse<byte[]> response = HTTP.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() >= 400) {
            throw new java.io.IOException("HTTP " + response.statusCode());
        }
        return response.body();
    }

    private static String get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .header("Authorization", UserSession.getInstance().getAuthHeader())
                .GET()
                .build();
        HttpResponse<String> response;
        try {
            response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (java.net.ConnectException e) {
            throw new IOException("Cannot reach backend at " + BASE_URL + ". Is the server running?", e);
        }
        if (response.statusCode() >= 400) {
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }

    private static String post(String path, String jsonBody, boolean withAuth) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(jsonBody));
        if (withAuth) {
            builder.header("Authorization", UserSession.getInstance().getAuthHeader());
        }
        HttpResponse<String> response;
        try {
            response = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (java.net.ConnectException e) {
            throw new IOException("Cannot reach backend at " + BASE_URL + ". Is the server running?", e);
        }
        if (response.statusCode() >= 400) {
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }
}
