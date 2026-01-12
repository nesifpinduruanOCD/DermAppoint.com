package models;

import java.time.LocalDateTime;

public class Appointment {

    private String appointmentId;
    private String patientId;
    private String patientName;
    private String serviceId;
    private LocalDateTime appointmentDateTime;
    private String doctorId;
    private String status;

    private boolean forAnotherPerson;
    private String otherPersonName;
    private String otherPersonContact;
    private Integer otherPersonAge;
    private String relationship;

    // Getters & Setters
    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isForAnotherPerson() { return forAnotherPerson; }
    public void setForAnotherPerson(boolean forAnotherPerson) { this.forAnotherPerson = forAnotherPerson; }

    public String getOtherPersonName() { return otherPersonName; }
    public void setOtherPersonName(String otherPersonName) { this.otherPersonName = otherPersonName; }

    public String getOtherPersonContact() { return otherPersonContact; }
    public void setOtherPersonContact(String otherPersonContact) { this.otherPersonContact = otherPersonContact; }

    public Integer getOtherPersonAge() { return otherPersonAge; }
    public void setOtherPersonAge(Integer otherPersonAge) { this.otherPersonAge = otherPersonAge; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
}
