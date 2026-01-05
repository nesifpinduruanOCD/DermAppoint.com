package models;

import java.time.LocalDateTime;

public class Admin extends User {
    private String role;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private String username; // optional
    private String password; // override if needed

    // Constructors
    public Admin() {
        super();
        this.role = "ADMIN";
    }

    public Admin(String userId, String fullName, String email, String phone, String role) {
        super(userId, fullName, email, phone);
        this.role = role;
        this.role = "ADMIN";
    }

    // Getters and Setters
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password != null ? password : super.getPassword();
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "userId='" + getUserId() + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phone='" + getPhone() + '\'' +
                ", role='" + role + '\'' +
                ", lastLogin=" + lastLogin +
                ", createdAt=" + createdAt +
                '}';
    }
}
