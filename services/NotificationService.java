package services;

import UI.INotification;
import utils.Logger;

import javax.swing.*;

public class NotificationService implements INotification {
    
    @Override
    public void sendAppointmentConfirmation(String recipient, String appointmentDetails) {
        String message = "Appointment Confirmed!\n\n" + appointmentDetails;
        showNotification("DermAppoint - Confirmation", message);
        Logger.log("Appointment confirmation sent to: " + recipient);
    }
    
    @Override
    public void sendReminder(String recipient, String appointmentDetails) {
        String message = "Reminder: You have an appointment tomorrow!\n\n" + appointmentDetails;
        showNotification("DermAppoint - Reminder", message);
        Logger.log("Reminder sent to: " + recipient);
    }
    
    @Override
    public void sendRescheduleNotice(String recipient, String oldDetails, String newDetails) {
        String message = "Appointment Rescheduled!\n\nOld: " + oldDetails + "\n\nNew: " + newDetails;
        showNotification("DermAppoint - Rescheduled", message);
        Logger.log("Reschedule notice sent to: " + recipient);
    }
    
    @Override
    public void sendCancellationNotice(String recipient, String appointmentDetails) {
        String message = "Appointment Cancelled\n\n" + appointmentDetails;
        showNotification("DermAppoint - Cancellation", message);
        Logger.log("Cancellation notice sent to: " + recipient);
    }
    
    private void showNotification(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}