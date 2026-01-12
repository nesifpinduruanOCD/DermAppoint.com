package models;

/**
 * Represents a single time slot for doctor appointments.
 */
public class TimeSlot {
    private String time;      // e.g., "08:00"
    private boolean available;

    public TimeSlot(String time) {
        this.time = time;
        this.available = true; // default available
    }

    // Getters & Setters
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return time + (available ? " (Available)" : " (Booked)");
    }
}
