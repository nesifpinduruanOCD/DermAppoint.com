package models;

import java.util.ArrayList;
import java.util.List;

public class Patient extends User {
    private String phone;
    private String address; // add this
    private List<Appointment> activeAppointments = new ArrayList<>(); // optional, if you track appointments

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Address getter and setter
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // Optional: Active appointments getter
    public List<Appointment> getActiveAppointments() {
        return activeAppointments;
    }
}
