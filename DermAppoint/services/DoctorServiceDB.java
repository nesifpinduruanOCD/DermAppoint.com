package services;

import db.DataBaseConnection;
import models.*;
import models.TimeSlot;
import utils.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DoctorServiceDB {
    
    public List<Doctor> getAllDoctors(boolean activeOnly) {
        List<Doctor> doctors = new ArrayList<>();
        String sql = activeOnly ? 
            "SELECT * FROM doctors WHERE is_active = TRUE ORDER BY name" :
            "SELECT * FROM doctors ORDER BY name";
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                doctors.add(mapResultSetToDoctor(rs));
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to get doctors", e);
        }
        
        return doctors;
    }
    
    public Doctor getDoctorById(String doctorId) {
        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, doctorId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDoctor(rs);
                }
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to get doctor: " + doctorId, e);
        }
        
        return null;
    }
    
    public List<Doctor> getAvailableDoctors(LocalDate date, String timeSlot) {
        List<Doctor> doctors = new ArrayList<>();
        String dayOfWeek = date.getDayOfWeek().toString().substring(0, 3);
        
        String sql = """
            SELECT * FROM doctors 
            WHERE is_active = TRUE
            AND FIND_IN_SET(?, available_days) > 0
            AND (
                (? = 'AM' AND available_am = TRUE) OR 
                (? = 'PM' AND available_pm = TRUE)
            )
        """;
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, dayOfWeek);
            pstmt.setString(2, timeSlot);
            pstmt.setString(3, timeSlot);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    doctors.add(mapResultSetToDoctor(rs));
                }
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to get available doctors for " + date + " " + timeSlot, e);
        }
        
        return doctors;
    }
    
    public List<TimeSlot> getAvailableTimeSlots(String doctorId, LocalDate date) {
        List<TimeSlot> timeSlots = new ArrayList<>();
        
        Doctor doctor = getDoctorById(doctorId);
        if (doctor == null) {
            return timeSlots;
        }
        
        String dayOfWeek = date.getDayOfWeek().toString().substring(0, 3);
        List<String> availableDays = doctor.getAvailableDays();
        
        if (!availableDays.contains(dayOfWeek)) {
            return timeSlots;
        }
        
        // Generate time slots based on doctor availability
        List<String> amSlots = Arrays.asList("08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30");
        List<String> pmSlots = Arrays.asList("13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30");
        
        // Check for booked slots
        String sql = """
            SELECT appointment_time 
            FROM appointments 
            WHERE doctor_id = ? 
            AND appointment_date = ?
            AND status NOT IN ('CANCELLED')
        """;
        
        List<String> bookedTimes = new ArrayList<>();
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, doctorId);
            pstmt.setDate(2, Date.valueOf(date));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookedTimes.add(rs.getTime("appointment_time").toString().substring(0, 5));
                }
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to get booked time slots", e);
        }
        
        // Add AM slots if available
        if (doctor.isAvailableAM()) {
            for (String time : amSlots) {
                TimeSlot slot = new TimeSlot(time);
                slot.setAvailable(!bookedTimes.contains(time));
                timeSlots.add(slot);
            }
        }
        
        // Add PM slots if available
        if (doctor.isAvailablePM()) {
            for (String time : pmSlots) {
                TimeSlot slot = new TimeSlot(time);
                slot.setAvailable(!bookedTimes.contains(time));
                timeSlots.add(slot);
            }
        }
        
        return timeSlots;
    }
    
    public Doctor addDoctor(String name, String specialization, List<String> availableDays, 
                           boolean availableAM, boolean availablePM, String contactEmail, 
                           String contactPhone) {
        
        String doctorId = generateDoctorId();
        String availableDaysStr = String.join(",", availableDays);
        
        String sql = """
            INSERT INTO doctors (doctor_id, name, specialization, available_days, 
                               available_am, available_pm, contact_email, contact_phone, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, TRUE)
        """;
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, doctorId);
            pstmt.setString(2, name);
            pstmt.setString(3, specialization);
            pstmt.setString(4, availableDaysStr);
            pstmt.setBoolean(5, availableAM);
            pstmt.setBoolean(6, availablePM);
            pstmt.setString(7, contactEmail);
            pstmt.setString(8, contactPhone);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                Doctor doctor = new Doctor();
                doctor.setDoctorId(doctorId);
                doctor.setName(name);
                doctor.setSpecialization(specialization);
                doctor.setAvailableDays(availableDays);
                doctor.setAvailableAM(availableAM);
                doctor.setAvailablePM(availablePM);
                
                Logger.log("Doctor added: " + name);
                return doctor;
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to add doctor: " + name, e);
        }
        
        return null;
    }
    
    public boolean updateDoctor(String doctorId, String name, String specialization, 
                               List<String> availableDays, boolean availableAM, 
                               boolean availablePM, String contactEmail, String contactPhone) {
        
        String availableDaysStr = String.join(",", availableDays);
        String sql = """
            UPDATE doctors 
            SET name = ?, specialization = ?, available_days = ?, 
                available_am = ?, available_pm = ?, contact_email = ?, contact_phone = ?
            WHERE doctor_id = ?
        """;
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, specialization);
            pstmt.setString(3, availableDaysStr);
            pstmt.setBoolean(4, availableAM);
            pstmt.setBoolean(5, availablePM);
            pstmt.setString(6, contactEmail);
            pstmt.setString(7, contactPhone);
            pstmt.setString(8, doctorId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                Logger.log("Doctor updated: " + doctorId);
                return true;
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to update doctor: " + doctorId, e);
        }
        
        return false;
    }
    
    public boolean deleteDoctor(String doctorId) {
        String sql = "UPDATE doctors SET is_active = FALSE WHERE doctor_id = ?";
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, doctorId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                Logger.log("Doctor deleted: " + doctorId);
                return true;
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to delete doctor: " + doctorId, e);
        }
        
        return false;
    }
    
    private Doctor mapResultSetToDoctor(ResultSet rs) throws SQLException {
        Doctor doctor = new Doctor();
        doctor.setDoctorId(rs.getString("doctor_id"));
        doctor.setName(rs.getString("name"));
        doctor.setSpecialization(rs.getString("specialization"));
        
        String availableDaysStr = rs.getString("available_days");
        if (availableDaysStr != null && !availableDaysStr.isEmpty()) {
            doctor.setAvailableDays(Arrays.asList(availableDaysStr.split(",")));
        }
        
        doctor.setAvailableAM(rs.getBoolean("available_am"));
        doctor.setAvailablePM(rs.getBoolean("available_pm"));
        
        return doctor;
    }
    
    private String generateDoctorId() {
        String sql = "SELECT COUNT(*) FROM doctors";
        
        try (Connection conn = DataBaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                return "DOC" + String.format("%03d", count);
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to generate doctor ID", e);
        }
        
        return "DOC001";
    }
}