package models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Patient {
    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String address;
    private LocalDate birthDate;
    private List<Appointment> activeAppointments;

    // Full constructor (7 params)
    public Patient(String userId, String fullName, String email, String phone, String password, String address, LocalDate birthDate) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.address = address;
        this.birthDate = birthDate;
        this.activeAppointments = new ArrayList<>();
    }

    // 6-param constructor (no birthDate) for login or dummy creation
    public Patient(String userId, String fullName, String email, String phone, String password, String address) {
        this(userId, fullName, email, phone, password, address, null);
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }

    public String getAddress() {
        return address;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public List<Appointment> getActiveAppointments() {
        return activeAppointments;
    }

    // For CardLayout identification
    public String getUsername() {
        return email.split("@")[0]; // simple username from email
    }
}
