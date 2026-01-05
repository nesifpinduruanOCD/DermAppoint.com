package services;

import UI.ILogin;
import models.Admin;
import models.Patient;
import models.User;
import utils.ValidationUtils;

import java.util.HashMap;
import java.util.Map;

public class UserService implements ILogin {
    private Map<String, Patient> patients;
    private Map<String, Admin> admins;
    private User currentUser;

    public UserService() {
        patients = new HashMap<>();
        admins = new HashMap<>();
        initializeSampleData();
    }

    private void initializeSampleData() {
        // Sample admin
        Admin admin = new Admin();
        admin.setUserId("ADM001"); // Use userId from parent User class
        admin.setFullName("Dr. Sarah Johnson");
        admin.setEmail("admin@dermaclinic.com");
        admin.setPassword("admin123");
        admin.setRole("Clinic Manager");
        admins.put(admin.getEmail(), admin);

        // Sample patient
        Patient patient = new Patient();
        patient.setUserId("PAT001");
        patient.setFullName("John Doe");
        patient.setEmail("john@example.com");
        patient.setPassword("patient123");
        patient.setPhone("09171234567");
        patients.put(patient.getEmail(), patient);
    }

    @Override
    public boolean login(String email, String password) {
        // Check admin first
        Admin admin = admins.get(email);
        if (admin != null && admin.getPassword().equals(password)) {
            currentUser = admin;
            return true;
        }

        // Check patient
        Patient patient = patients.get(email);
        if (patient != null && patient.getPassword().equals(password)) {
            currentUser = patient;
            return true;
        }

        return false;
    }

    public Patient registerPatient(String fullName, String email, String phone,
                                   String password, String address) {
        if (!ValidationUtils.validateEmail(email)) {
            throw new RuntimeException("Invalid email format");
        }

        if (!ValidationUtils.validatePhone(phone)) {
            throw new RuntimeException("Phone must be 11 digits");
        }

        if (patients.containsKey(email)) {
            throw new RuntimeException("Email already registered");
        }

        Patient patient = new Patient();
        patient.setUserId("PAT" + String.format("%03d", patients.size() + 1));
        patient.setFullName(fullName);
        patient.setEmail(email);
        patient.setPhone(phone);
        patient.setPassword(password);
        patient.setAddress(address);

        patients.put(email, patient);
        return patient;
    }

    @Override
    public void logout() {
        currentUser = null;
    }

    @Override
    public boolean changePassword(String oldPassword, String newPassword) {
        if (currentUser != null && currentUser.getPassword().equals(oldPassword)) {
            currentUser.setPassword(newPassword);
            return true;
        }
        return false;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAdminLoggedIn() {
        return currentUser instanceof Admin;
    }
}