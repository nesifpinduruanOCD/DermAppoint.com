package UI;

import java.time.LocalDate;

public interface IAppointmentLimiter {

    boolean isSlotAvailable(LocalDate date, String timeSlot);

    int getAvailableSlots(LocalDate date, String timeSlot);

    boolean checkDailyLimit(LocalDate date, String timeSlot);
}
