package models;


public class Staff {

    private String staffId;
    private String fullName;
    private String email;
    private String phone;
    private String role; // e.g., Doctor, Nurse, Receptionist, Admin
    private boolean isActive;
    private String Username;

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    // ---------------- CONSTRUCTORS ----------------
    public Staff() {
    }

    public Staff(String staffId, String fullName, String email, String phone, String role, boolean isActive) {
        this.staffId = staffId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.isActive = isActive;
    }

    // ---------------- GETTERS & SETTERS ----------------
    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
