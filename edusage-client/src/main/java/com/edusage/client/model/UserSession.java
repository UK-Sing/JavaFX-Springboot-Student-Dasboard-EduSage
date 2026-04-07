package com.edusage.client.model;

public class UserSession {

    private static UserSession instance;

    private String token;
    private String role;
    private Long userId;
    private String name;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setSession(String token, String role, Long userId, String name) {
        this.token  = token;
        this.role   = role;
        this.userId = userId;
        this.name   = name;
    }

    public void clearSession() {
        this.token  = null;
        this.role   = null;
        this.userId = null;
        this.name   = null;
    }

    public String getAuthHeader() {
        return "Bearer " + token;
    }

    public boolean isLoggedIn()  { return token != null; }
    public String  getToken()    { return token; }
    public String  getRole()     { return role; }
    public Long    getUserId()   { return userId; }
    public String  getName()     { return name; }
}
