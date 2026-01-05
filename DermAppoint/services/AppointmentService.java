package services;

import UI.IAppointmentLimiter;
import models.Appointment;
import models.Patient;
import utils.ValidationUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class AppointmentService implements IAppointmentLimiter {

    private Map<String, Appointment> appointments;
    private Map<LocalDate, Map<String, Integer>> dailyAppointments;

    private static final int MAX_AM_SLOTS = 20;
    private static final int MAX_PM_SLOTS = 20;

    public AppointmentService() {
        appointments = new HashMap<>();
        dailyAppointments = new HashMap<>();
    }

    public Appointment createAppointment(
            Patient patient,
            String serviceId,
            LocalDateTime dateTime,
            String doctorId
    ) {

        String timeSlot = getTimeSlot(dateTime);

        if (!checkDailyLimit(dateTime.toLocalDate(), timeSlot)) {
            throw new RuntimeException("Daily limit reached for this time slot");
        }

        if (patient.getActiveAppointments().size() >= 1) {
            throw new RuntimeException("Patient already has an active appointment");
        }

        Appointment appointment = new Appointment();
        appointment.setAppointmentId(UUID.randomUUID().toString());
        appointment.setPatientId(patient.getUserId());
        appointment.setPatientName(patient.getFullName());
        appointment.setServiceId(serviceId);
        appointment.setAppointmentDateTime(dateTime);
        appointment.setDoctorId(doctorId);
        appointment.setStatus("PENDING");

        appointments.put(appointment.getAppointmentId(), appointment);
        patient.getActiveAppointments().add(appointment);

        updateDailyCount(dateTime.toLocalDate(), timeSlot, 1);

        return appointment;
    }

    public Appointment createAppointmentForAnother(
            Patient patient,
            String otherPersonName,
            String otherPersonContact,
            Integer otherPersonAge,
            String relationship,
            String serviceId,
            LocalDateTime dateTime,
            String doctorId
    ) {

        if (!ValidationUtils.validatePhone(otherPersonContact)) {
            throw new RuntimeException("Invalid phone number");
        }

        Appointment appointment = createAppointment(patient, serviceId, dateTime, doctorId);

        appointment.setForAnotherPerson(true);
        appointment.setOtherPersonName(otherPersonName);
        appointment.setOtherPersonContact(otherPersonContact);
        appointment.setOtherPersonAge(otherPersonAge);
        appointment.setRelationship(relationship);

        return appointment;
    }

    @Override
    public boolean isSlotAvailable(LocalDate date, String timeSlot) {
        return getAvailableSlots(date, timeSlot) > 0;
    }

    @Override
    public int getAvailableSlots(LocalDate date, String timeSlot) {
        Map<String, Integer> slots = dailyAppointments.getOrDefault(date, new HashMap<>());
        int booked = slots.getOrDefault(timeSlot, 0);

        return "AM".equals(timeSlot)
                ? MAX_AM_SLOTS - booked
                : MAX_PM_SLOTS - booked;
    }

    @Override
    public boolean checkDailyLimit(LocalDate date, String timeSlot) {
        return getAvailableSlots(date, timeSlot) > 0;
    }

    private String getTimeSlot(LocalDateTime dateTime) {
        return dateTime.getHour() < 12 ? "AM" : "PM";
    }

    private void updateDailyCount(LocalDate date, String timeSlot, int change) {
        Map<String, Integer> slots = dailyAppointments.getOrDefault(date, new HashMap<>());
        int current = slots.getOrDefault(timeSlot, 0);
        slots.put(timeSlot, current + change);
        dailyAppointments.put(date, slots);
    }

    public void cancelAppointment(String appointmentId, Patient patient) {
        Appointment appointment = appointments.get(appointmentId);
        if (appointment == null) return;

        appointment.setStatus("CANCELLED");
        patient.getActiveAppointments().remove(appointment);

        updateDailyCount(
                appointment.getAppointmentDateTime().toLocalDate(),
                getTimeSlot(appointment.getAppointmentDateTime()),
                -1
        );
    }
}
