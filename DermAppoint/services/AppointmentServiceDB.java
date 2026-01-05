package services;

import db.DataBaseConnection;
import UI.IAppointmentLimiter;
import models.Appointment;
import models.Patient;
import utils.Logger;
import utils.Config;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class AppointmentServiceDB implements IAppointmentLimiter {
    
    @Override
    public boolean isSlotAvailable(LocalDate date, String timeSlot) {
        return getAvailableSlots(date, timeSlot) > 0;
    }
    
    @Override
    public int getAvailableSlots(LocalDate date, String timeSlot) {
        String sql = """
            SELECT 
                (CASE WHEN ? = 'AM' THEN ? ELSE ? END) - 
                COUNT(CASE WHEN TIME(appointment_time) BETWEEN ? AND ? THEN 1 END) as available_slots
            FROM appointments 
            WHERE appointment_date = ? 
            AND status NOT IN ('CANCELLED')
        """;
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, timeSlot);
            pstmt.setInt(2, Config.MAX_AM_SLOTS);
            pstmt.setInt(3, Config.MAX_PM_SLOTS);
            
            // Set time ranges for AM (8:00-12:00) and PM (13:00-17:00)
            if ("AM".equals(timeSlot)) {
                pstmt.setTime(4, Time.valueOf("08:00:00"));
                pstmt.setTime(5, Time.valueOf("12:00:00"));
            } else {
                pstmt.setTime(4, Time.valueOf("13:00:00"));
                pstmt.setTime(5, Time.valueOf("17:00:00"));
            }
            
            pstmt.setDate(6, java.sql.Date.valueOf(date));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("available_slots");
                }
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to get available slots for " + date + " " + timeSlot, e);
        }
        
        return 0;
    }
    
    @Override
    public boolean checkDailyLimit(LocalDate date, String timeSlot) {
        return isSlotAvailable(date, timeSlot);
    }
    
    public Appointment createAppointment(Patient patient, String serviceId, 
                                        LocalDateTime dateTime, String doctorId) {
        
        // Check daily limit
        String timeSlot = dateTime.getHour() < 12 ? "AM" : "PM";
        if (!checkDailyLimit(dateTime.toLocalDate(), timeSlot)) {
            throw new RuntimeException("Daily limit reached for this time slot");
        }
        
        // Check if patient has active appointment
        if (hasActiveAppointment(patient.getUserId())) {
            throw new RuntimeException("Patient already has an active appointment");
        }
        
        // Check doctor availability
        if (!isDoctorAvailable(doctorId, dateTime)) {
            throw new RuntimeException("Doctor is not available at this time");
        }
        
        String appointmentId = generateAppointmentId();
        String sql = """
            INSERT INTO appointments (
                appointment_id, patient_id, patient_name, service_id, 
                appointment_date, appointment_time, doctor_id, status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING')
        """;
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, appointmentId);
            pstmt.setString(2, patient.getUserId());
            pstmt.setString(3, patient.getFullName());
            pstmt.setString(4, serviceId);
            pstmt.setDate(5, java.sql.Date.valueOf(dateTime.toLocalDate()));
            pstmt.setTime(6, Time.valueOf(dateTime.toLocalTime()));
            pstmt.setString(7, doctorId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                Appointment appointment = new Appointment();
                appointment.setAppointmentId(appointmentId);
                appointment.setPatientId(patient.getUserId());
                appointment.setPatientName(patient.getFullName());
                appointment.setServiceId(serviceId);
                appointment.setAppointmentDateTime(dateTime);
                appointment.setDoctorId(doctorId);
                appointment.setStatus("PENDING");
                
                Logger.logAppointmentAction("created", appointmentId, patient.getUserId());
                return appointment;
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to create appointment", e);
        }
        
        return null;
    }
    
    public Appointment createAppointmentForAnother(Patient patient, String otherPersonName,
                                                   String otherPersonContact, Integer otherPersonAge,
                                                   String relationship, String serviceId,
                                                   LocalDateTime dateTime, String doctorId) {
        
        Appointment appointment = createAppointment(patient, serviceId, dateTime, doctorId);
        
        if (appointment != null) {
            String sql = """
                UPDATE appointments 
                SET is_for_another_person = TRUE,
                    other_person_name = ?,
                    other_person_contact = ?,
                    other_person_age = ?,
                    relationship = ?
                WHERE appointment_id = ?
            """;
            
            try (Connection conn = DataBaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, otherPersonName);
                pstmt.setString(2, otherPersonContact);
                pstmt.setInt(3, otherPersonAge);
                pstmt.setString(4, relationship);
                pstmt.setString(5, appointment.getAppointmentId());
                
                pstmt.executeUpdate();
                
                appointment.setForAnotherPerson(true);
                appointment.setOtherPersonName(otherPersonName);
                appointment.setOtherPersonContact(otherPersonContact);
                appointment.setOtherPersonAge(otherPersonAge);
                appointment.setRelationship(relationship);
                
                Logger.logAppointmentAction("created for another", appointment.getAppointmentId(), patient.getUserId());
            } catch (SQLException e) {
                Logger.logError("Failed to update appointment for another person", e);
            }
        }
        
        return appointment;
    }
    
    public List<Appointment> getAppointmentsByPatient(String patientId) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE patient_id = ? ORDER BY appointment_date DESC, appointment_time DESC";
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, patientId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to get appointments for patient: " + patientId, e);
        }
        
        return appointments;
    }
    
    public List<Appointment> getAppointmentsByStatus(String status) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE status = ? ORDER BY appointment_date, appointment_time";
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to get appointments with status: " + status, e);
        }
        
        return appointments;
    }
    
    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE appointment_date = ? ORDER BY appointment_time";
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, java.sql.Date.valueOf(date));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to get appointments for date: " + date, e);
        }
        
        return appointments;
    }
    
    public boolean updateAppointmentStatus(String appointmentId, String status, String notes) {
        String sql = "UPDATE appointments SET status = ?, notes = ?, updated_at = NOW() WHERE appointment_id = ?";
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setString(2, notes);
            pstmt.setString(3, appointmentId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                Logger.logAppointmentAction("status updated to " + status, appointmentId, "system");
                return true;
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to update appointment status", e);
        }
        
        return false;
    }
    
    public boolean rescheduleAppointment(String appointmentId, LocalDateTime newDateTime, String doctorId) {
        String sql = """
            UPDATE appointments 
            SET appointment_date = ?, appointment_time = ?, doctor_id = ?, 
                status = 'RESCHEDULED', updated_at = NOW()
            WHERE appointment_id = ?
        """;
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, java.sql.Date.valueOf(newDateTime.toLocalDate()));
            pstmt.setTime(2, Time.valueOf(newDateTime.toLocalTime()));
            pstmt.setString(3, doctorId);
            pstmt.setString(4, appointmentId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                Logger.logAppointmentAction("rescheduled", appointmentId, "system");
                return true;
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to reschedule appointment", e);
        }
        
        return false;
    }
    
    public boolean cancelAppointment(String appointmentId, String reason) {
        return updateAppointmentStatus(appointmentId, "CANCELLED", reason);
    }
    
    public List<Appointment> getUpcomingAppointments(int daysAhead) {
        List<Appointment> appointments = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysAhead);
        
        String sql = """
            SELECT * FROM appointments 
            WHERE appointment_date BETWEEN ? AND ? 
            AND status IN ('PENDING', 'APPROVED', 'RESCHEDULED')
            ORDER BY appointment_date, appointment_time
        """;
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, java.sql.Date.valueOf(today));
            pstmt.setDate(2, java.sql.Date.valueOf(endDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to get upcoming appointments", e);
        }
        
        return appointments;
    }
    
    private boolean hasActiveAppointment(String patientId) {
        String sql = """
            SELECT COUNT(*) 
            FROM appointments 
            WHERE patient_id = ? 
            AND status IN ('PENDING', 'APPROVED', 'RESCHEDULED')
            AND appointment_date >= CURDATE()
        """;
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, patientId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to check active appointments for patient: " + patientId, e);
        }
        
        return false;
    }
    
    private boolean isDoctorAvailable(String doctorId, LocalDateTime dateTime) {
        String sql = """
            SELECT * FROM doctors 
            WHERE doctor_id = ? 
            AND is_active = TRUE
            AND FIND_IN_SET(?, available_days) > 0
            AND (
                (? = 'AM' AND available_am = TRUE) OR 
                (? = 'PM' AND available_pm = TRUE)
            )
        """;
        
        String dayOfWeek = dateTime.getDayOfWeek().toString().substring(0, 3);
        String timeSlot = dateTime.getHour() < 12 ? "AM" : "PM";
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, doctorId);
            pstmt.setString(2, dayOfWeek);
            pstmt.setString(3, timeSlot);
            pstmt.setString(4, timeSlot);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to check doctor availability", e);
        }
        
        return false;
    }
    
    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(rs.getString("appointment_id"));
        appointment.setPatientId(rs.getString("patient_id"));
        appointment.setPatientName(rs.getString("patient_name"));
        appointment.setServiceId(rs.getString("service_id"));
        
        LocalDate date = rs.getDate("appointment_date").toLocalDate();
        LocalTime time = rs.getTime("appointment_time").toLocalTime();
        appointment.setAppointmentDateTime(LocalDateTime.of(date, time));
        
        appointment.setDoctorId(rs.getString("doctor_id"));
        appointment.setStatus(rs.getString("status"));
        appointment.setNotes(rs.getString("notes"));
        appointment.setForAnotherPerson(rs.getBoolean("is_for_another_person"));
        appointment.setOtherPersonName(rs.getString("other_person_name"));
        appointment.setOtherPersonContact(rs.getString("other_person_contact"));
        appointment.setOtherPersonAge(rs.getInt("other_person_age"));
        appointment.setRelationship(rs.getString("relationship"));
        
        return appointment;
    }
    
    private String generateAppointmentId() {
        String sql = "SELECT COUNT(*) FROM appointments WHERE DATE(created_at) = CURDATE()";
        
        try (Connection conn = DataBaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                String dateStr = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
                return "APP" + dateStr + String.format("%03d", count);
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to generate appointment ID", e);
        }
        
        return "APP" + System.currentTimeMillis();
    }
}