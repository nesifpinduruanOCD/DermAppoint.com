package models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a doctor in the system.
 */
public class Doctor {
    private String doctorId;
    private String name;
    private String specialization;
    private List<String> availableDays;
    private boolean availableAM;
    private boolean availablePM;

    public Doctor() {
        availableDays = new ArrayList<>();
    }

    public Doctor(String doctorId, String name, String specialization,
                  List<String> availableDays, boolean availableAM, boolean availablePM) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialization = specialization;
        this.availableDays = availableDays;
        this.availableAM = availableAM;
        this.availablePM = availablePM;
    }

    // Getters & Setters
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public List<String> getAvailableDays() { return availableDays; }
    public void setAvailableDays(List<String> availableDays) { this.availableDays = availableDays; }

    public boolean isAvailableAM() { return availableAM; }
    public void setAvailableAM(boolean availableAM) { this.availableAM = availableAM; }

    public boolean isAvailablePM() { return availablePM; }
    public void setAvailablePM(boolean availablePM) { this.availablePM = availablePM; }
}
