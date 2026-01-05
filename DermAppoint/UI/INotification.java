package UI;

public interface INotification {
    void sendAppointmentConfirmation(String recipient, String appointmentDetails);
    void sendReminder(String recipient, String appointmentDetails);
    void sendRescheduleNotice(String recipient, String oldDetails, String newDetails);
    void sendCancellationNotice(String recipient, String appointmentDetails);
}