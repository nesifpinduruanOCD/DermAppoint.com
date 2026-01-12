package models;

public class DoctorUser {
    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String address;

    public DoctorUser(String userId, String fullName, String email, String phone, String password, String address) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.address = address;
    }

    public String getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPassword() { return password; }
    public String getAddress() { return address; }

    public String getUsername() {
        return email.split("@")[0];
    }
}
