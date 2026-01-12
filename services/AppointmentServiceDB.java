package services;

import db.DataBaseConnection;
import models.Appointment;
import models.Patient;
import utils.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AppointmentServiceDB {

    public Appointment createAppointment(Patient patient, String serviceId, LocalDateTime dateTime, String doctorId) {
        if (patient.getActiveAppointments().size() >= 1) {
            throw new RuntimeException("Patient already has an active appointment");
        }

        String appointmentId = UUID.randomUUID().toString();
        String sql = "INSERT INTO appointments (appointment_id, patient_id, patient_name, service_id, appointment_date, appointment_time, doctor_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, appointmentId);
            stmt.setString(2, patient.getUserId());
            stmt.setString(3, patient.getFullName());
            stmt.setString(4, serviceId);
            stmt.setDate(5, Date.valueOf(dateTime.toLocalDate()));
            stmt.setTime(6, Time.valueOf(dateTime.toLocalTime()));
            stmt.setString(7, doctorId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                Appointment appointment = new Appointment();
                appointment.setAppointmentId(appointmentId);
                appointment.setPatientId(patient.getUserId());
                appointment.setPatientName(patient.getFullName());
                appointment.setServiceId(serviceId);
                appointment.setAppointmentDateTime(dateTime);
                appointment.setDoctorId(doctorId);
                appointment.setStatus("PENDING");

                patient.getActiveAppointments().add(appointment);
                Logger.logError("Created appointment: " + appointmentId, null);
                return appointment;
            }

        } catch (SQLException e) {
            Logger.logError("Failed to create appointment", e);
        }

        return null;
    }

    public boolean cancelAppointment(Appointment appointment, Patient patient) {
        String sql = "UPDATE appointments SET status = 'CANCELLED' WHERE appointment_id = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, appointment.getAppointmentId());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                patient.getActiveAppointments().remove(appointment);
                return true;
            }
        } catch (SQLException e) {
            Logger.logError("Failed to cancel appointment", e);
        }
        return false;
    }

    public List<Appointment> findByPatientId(String patientId) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE patient_id = ? ORDER BY appointment_date DESC, appointment_time DESC";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            Logger.logError("Failed to load appointments for patient: " + patientId, e);
        }

        return appointments;
    }

    public List<Appointment> findByDoctorId(String doctorId) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE doctor_id = ? ORDER BY appointment_date ASC, appointment_time ASC";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, doctorId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            Logger.logError("Failed to load appointments for doctor: " + doctorId, e);
        }

        return appointments;
    }

    public List<Appointment> findAll() {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments ORDER BY appointment_date DESC, appointment_time DESC";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        } catch (SQLException e) {
            Logger.logError("Failed to load all appointments", e);
        }

        return appointments;
    }

    public boolean updateAppointmentStatus(String appointmentId, String status) {
        if (appointmentId == null || appointmentId.isBlank() || status == null || status.isBlank()) {
            return false;
        }

        String normalized = status.trim().toUpperCase();
        if (!normalized.equals("PENDING") && !normalized.equals("APPROVED")
                && !normalized.equals("COMPLETED") && !normalized.equals("CANCELLED")) {
            return false;
        }

        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, normalized);
            stmt.setString(2, appointmentId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.logError("Failed to update appointment status: " + appointmentId, e);
        }

        return false;
    }

    public List<Appointment> findActiveByPatientId(String patientId) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE patient_id = ? AND status NOT IN ('CANCELLED','COMPLETED') ORDER BY appointment_date ASC, appointment_time ASC";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            Logger.logError("Failed to load active appointments for patient: " + patientId, e);
        }

        return appointments;
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setAppointmentId(rs.getString("appointment_id"));
        a.setPatientId(rs.getString("patient_id"));
        try {
            a.setPatientName(rs.getString("patient_name"));
        } catch (SQLException ignored) {
        }
        a.setServiceId(rs.getString("service_id"));
        a.setDoctorId(rs.getString("doctor_id"));
        a.setStatus(rs.getString("status"));

        Date date = rs.getDate("appointment_date");
        Time time = rs.getTime("appointment_time");
        if (date != null && time != null) {
            LocalDate d = date.toLocalDate();
            LocalTime t = time.toLocalTime();
            a.setAppointmentDateTime(LocalDateTime.of(d, t));
        }
        return a;
    }
}
