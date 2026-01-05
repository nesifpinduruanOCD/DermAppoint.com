package services;

import db.DataBaseConnection;
import models.Service;
import utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicesServiceDB {
    
    public List<Service> getAllServices(boolean activeOnly) {
        List<Service> services = new ArrayList<>();
        String sql = activeOnly ? 
            "SELECT * FROM services WHERE is_active = TRUE ORDER BY service_name" :
            "SELECT * FROM services ORDER BY service_name";
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                services.add(mapResultSetToService(rs));
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to get services", e);
        }
        
        return services;
    }
    
    public Service getServiceById(String serviceId) {
        String sql = "SELECT * FROM services WHERE service_id = ?";
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, serviceId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToService(rs);
                }
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to get service: " + serviceId, e);
        }
        
        return null;
    }
    
    public Service addService(String serviceName, String description, double price, 
                             int durationMinutes, String requiredPreparation) {
        
        String serviceId = generateServiceId();
        String sql = """
            INSERT INTO services (service_id, service_name, description, price, 
                                duration_minutes, required_preparation, is_active)
            VALUES (?, ?, ?, ?, ?, ?, TRUE)
        """;
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, serviceId);
            pstmt.setString(2, serviceName);
            pstmt.setString(3, description);
            pstmt.setDouble(4, price);
            pstmt.setInt(5, durationMinutes);
            pstmt.setString(6, requiredPreparation);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                Service service = new Service();
                service.setServiceId(serviceId);
                service.setServiceName(serviceName);
                service.setDescription(description);
                service.setPrice(price);
                service.setDurationMinutes(durationMinutes);
                service.setRequiredPreparation(requiredPreparation);
                service.setActive(true);
                
                Logger.log("Service added: " + serviceName);
                return service;
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to add service: " + serviceName, e);
        }
        
        return null;
    }
    
    public boolean updateService(String serviceId, String serviceName, String description, 
                                double price, int durationMinutes, String requiredPreparation) {
        String sql = """
            UPDATE services 
            SET service_name = ?, description = ?, price = ?, 
                duration_minutes = ?, required_preparation = ?
            WHERE service_id = ?
        """;
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, serviceName);
            pstmt.setString(2, description);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, durationMinutes);
            pstmt.setString(5, requiredPreparation);
            pstmt.setString(6, serviceId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                Logger.log("Service updated: " + serviceId);
                return true;
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to update service: " + serviceId, e);
        }
        
        return false;
    }
    
    public boolean deleteService(String serviceId) {
        String sql = "UPDATE services SET is_active = FALSE WHERE service_id = ?";
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, serviceId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                Logger.log("Service deleted: " + serviceId);
                return true;
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to delete service: " + serviceId, e);
        }
        
        return false;
    }
    
    public boolean toggleServiceStatus(String serviceId, boolean active) {
        String sql = "UPDATE services SET is_active = ? WHERE service_id = ?";
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, active);
            pstmt.setString(2, serviceId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                Logger.log("Service status updated: " + serviceId + " = " + active);
                return true;
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to toggle service status: " + serviceId, e);
        }
        
        return false;
    }
    
    private Service mapResultSetToService(ResultSet rs) throws SQLException {
        Service service = new Service();
        service.setServiceId(rs.getString("service_id"));
        service.setServiceName(rs.getString("service_name"));
        service.setDescription(rs.getString("description"));
        service.setPrice(rs.getDouble("price"));
        service.setDurationMinutes(rs.getInt("duration_minutes"));
        service.setRequiredPreparation(rs.getString("required_preparation"));
        service.setActive(rs.getBoolean("is_active"));
        return service;
    }
    
    private String generateServiceId() {
        String sql = "SELECT COUNT(*) FROM services";
        
        try (Connection conn = DataBaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                return "SERV" + String.format("%03d", count);
            }
            
        } catch (SQLException e) {
            Logger.logError("Failed to generate service ID", e);
        }
        
        return "SERV001";
    }
}