package models;

import java.util.ArrayList;
import java.util.List;

public class Doctor {
    private String doctorId;
    private String name;
    private String specialization;
    private List<String> availableDays; // MON, TUE, WED, etc.
    private boolean availableAM;
    private boolean availablePM;
    private List<TimeSlot> timeSlots;
    
    public Doctor() {
        this.availableDays = new ArrayList<>();
        this.timeSlots = new ArrayList<>();
    }
    
    // Getters and setters
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
    
    public List<TimeSlot> getTimeSlots() { return timeSlots; }
    public void setTimeSlots(List<TimeSlot> timeSlots) { this.timeSlots = timeSlots; }
}

class TimeSlot {
    private String time;
    private boolean isAvailable;
    
    public TimeSlot(String time) {
        this.time = time;
        this.isAvailable = true;
    }
    
    public String getTime() { return time; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
}