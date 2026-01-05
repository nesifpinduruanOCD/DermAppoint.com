package models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a specific time slot for doctor appointments.
 * Used in scheduling and availability management.
 */
public class TimeSlot {
    private String slotId;
    private String doctorId;
    private LocalDate slotDate;
    private LocalTime slotTime;
    private boolean isAvailable;
    private String appointmentId;
    private LocalTime endTime;
    private String slotLabel;
    private int durationMinutes;
    
    // Time slot types
    public static final String TYPE_MORNING = "MORNING";
    public static final String TYPE_AFTERNOON = "AFTERNOON";
    public static final String TYPE_EVENING = "EVENING";
    
    // Standard appointment durations in minutes
    public static final int DURATION_CONSULTATION = 30;
    public static final int DURATION_TREATMENT = 60;
    public static final int DURATION_PROCEDURE = 90;
    
    // Time boundaries for different slot types
    public static final LocalTime MORNING_START = LocalTime.of(8, 0);
    public static final LocalTime MORNING_END = LocalTime.of(12, 0);
    public static final LocalTime AFTERNOON_START = LocalTime.of(13, 0);
    public static final LocalTime AFTERNOON_END = LocalTime.of(17, 0);
    public static final LocalTime EVENING_START = LocalTime.of(17, 0);
    public static final LocalTime EVENING_END = LocalTime.of(20, 0);
    
    /**
     * Default constructor
     */
    public TimeSlot() {
        this.isAvailable = true;
        this.durationMinutes = DURATION_CONSULTATION;
        calculateEndTime();
    }
    
    /**
     * Constructor with time string
     */
    public TimeSlot(String time) {
        this();
        if (time != null && !time.isEmpty()) {
            try {
                // Try parsing HH:mm format
                if (time.contains(":")) {
                    String[] parts = time.split(":");
                    int hour = Integer.parseInt(parts[0]);
                    int minute = Integer.parseInt(parts[1]);
                    this.slotTime = LocalTime.of(hour, minute);
                } else {
                    // Try parsing as military time (e.g., "1430")
                    int hour = Integer.parseInt(time.substring(0, 2));
                    int minute = Integer.parseInt(time.substring(2));
                    this.slotTime = LocalTime.of(hour, minute);
                }
                calculateEndTime();
            } catch (Exception e) {
                // Default to current time if parsing fails
                this.slotTime = LocalTime.now();
                calculateEndTime();
            }
        }
    }
    
    /**
     * Full constructor
     */
    public TimeSlot(String slotId, String doctorId, LocalDate slotDate, 
                   LocalTime slotTime, int durationMinutes, boolean isAvailable) {
        this.slotId = slotId;
        this.doctorId = doctorId;
        this.slotDate = slotDate;
        this.slotTime = slotTime;
        this.durationMinutes = durationMinutes;
        this.isAvailable = isAvailable;
        calculateEndTime();
        generateSlotLabel();
    }
    
    /**
     * Constructor for quick creation
     */
    public TimeSlot(LocalDate date, LocalTime time, int duration) {
        this.slotDate = date;
        this.slotTime = time;
        this.durationMinutes = duration;
        calculateEndTime();
        generateSlotLabel();
    }
    
    /**
     * Calculate end time based on start time and duration
     */
    private void calculateEndTime() {
        if (slotTime != null && durationMinutes > 0) {
            this.endTime = slotTime.plusMinutes(durationMinutes);
        }
    }
    
    /**
     * Generate a user-friendly label for the time slot
     */
    private void generateSlotLabel() {
        if (slotTime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
            String startTimeStr = slotTime.format(formatter);
            
            if (endTime != null) {
                String endTimeStr = endTime.format(formatter);
                this.slotLabel = startTimeStr + " - " + endTimeStr;
            } else {
                this.slotLabel = startTimeStr;
            }
        }
    }
    
    /**
     * Check if this time slot overlaps with another time slot
     */
    public boolean overlapsWith(TimeSlot other) {
        if (this.slotDate == null || other.slotDate == null || 
            !this.slotDate.equals(other.slotDate)) {
            return false;
        }
        
        if (this.slotTime == null || other.slotTime == null) {
            return false;
        }
        
        LocalTime thisStart = this.slotTime;
        LocalTime thisEnd = this.endTime != null ? this.endTime : thisStart.plusMinutes(this.durationMinutes);
        LocalTime otherStart = other.slotTime;
        LocalTime otherEnd = other.endTime != null ? other.endTime : otherStart.plusMinutes(other.durationMinutes);
        
        // Check for overlap
        return (thisStart.isBefore(otherEnd) && otherStart.isBefore(thisEnd));
    }
    
    /**
     * Check if time slot is in the morning
     */
    public boolean isMorningSlot() {
        return slotTime != null && 
               !slotTime.isBefore(MORNING_START) && 
               slotTime.isBefore(MORNING_END);
    }
    
    /**
     * Check if time slot is in the afternoon
     */
    public boolean isAfternoonSlot() {
        return slotTime != null && 
               !slotTime.isBefore(AFTERNOON_START) && 
               slotTime.isBefore(AFTERNOON_END);
    }
    
    /**
     * Check if time slot is in the evening
     */
    public boolean isEveningSlot() {
        return slotTime != null && 
               !slotTime.isBefore(EVENING_START) && 
               slotTime.isBefore(EVENING_END);
    }
    
    /**
     * Get the slot type (MORNING, AFTERNOON, or EVENING)
     */
    public String getSlotType() {
        if (isMorningSlot()) return TYPE_MORNING;
        if (isAfternoonSlot()) return TYPE_AFTERNOON;
        if (isEveningSlot()) return TYPE_EVENING;
        return "UNKNOWN";
    }
    
    /**
     * Check if this time slot is before a specific time
     */
    public boolean isBefore(LocalTime time) {
        return slotTime != null && slotTime.isBefore(time);
    }
    
    /**
     * Check if this time slot is after a specific time
     */
    public boolean isAfter(LocalTime time) {
        return slotTime != null && slotTime.isAfter(time);
    }
    
    /**
     * Check if this time slot is within working hours (8 AM - 8 PM)
     */
    public boolean isWithinWorkingHours() {
        if (slotTime == null) return false;
        LocalTime workStart = LocalTime.of(8, 0);
        LocalTime workEnd = LocalTime.of(20, 0);
        return !slotTime.isBefore(workStart) && !slotTime.isAfter(workEnd);
    }
    
    /**
     * Check if this time slot is valid for dermatology appointments
     * (Monday-Friday, 8 AM - 8 PM, no weekends)
     */
    public boolean isValidForDermatology() {
        if (slotDate == null || slotTime == null) return false;
        
        // Check day of week (1=Monday, 7=Sunday)
        int dayOfWeek = slotDate.getDayOfWeek().getValue();
        
        // Dermatology clinic typically operates Monday-Friday
        if (dayOfWeek > 5) { // 6=Saturday, 7=Sunday
            return false;
        }
        
        // Check working hours
        return isWithinWorkingHours();
    }
    
    /**
     * Create a copy of this time slot
     */
    public TimeSlot copy() {
        TimeSlot copy = new TimeSlot();
        copy.setSlotId(this.slotId);
        copy.setDoctorId(this.doctorId);
        copy.setSlotDate(this.slotDate);
        copy.setSlotTime(this.slotTime);
        copy.setAvailable(this.isAvailable);
        copy.setAppointmentId(this.appointmentId);
        copy.setDurationMinutes(this.durationMinutes);
        copy.calculateEndTime();
        copy.generateSlotLabel();
        return copy;
    }
    
    /**
     * Get formatted date string
     */
    public String getFormattedDate() {
        if (slotDate != null) {
            return slotDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        }
        return "";
    }
    
    /**
     * Get formatted time string
     */
    public String getFormattedTime() {
        if (slotTime != null) {
            return slotTime.format(DateTimeFormatter.ofPattern("h:mm a"));
        }
        return "";
    }
    
    /**
     * Get formatted date and time string
     */
    public String getFormattedDateTime() {
        return getFormattedDate() + " at " + getFormattedTime();
    }
    
    /**
     * Check if this slot is in the past
     */
    public boolean isPast() {
        if (slotDate == null || slotTime == null) {
            return false;
        }
        
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        
        if (slotDate.isBefore(today)) {
            return true;
        } else if (slotDate.equals(today)) {
            return slotTime.isBefore(now);
        }
        
        return false;
    }
    
    /**
     * Check if this slot is today
     */
    public boolean isToday() {
        return slotDate != null && slotDate.equals(LocalDate.now());
    }
    
    /**
     * Check if this slot is tomorrow
     */
    public boolean isTomorrow() {
        return slotDate != null && slotDate.equals(LocalDate.now().plusDays(1));
    }
    
    // Getters and Setters
    public String getSlotId() {
        return slotId;
    }
    
    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }
    
    public String getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }
    
    public LocalDate getSlotDate() {
        return slotDate;
    }
    
    public void setSlotDate(LocalDate slotDate) {
        this.slotDate = slotDate;
    }
    
    public LocalTime getSlotTime() {
        return slotTime;
    }
    
    public void setSlotTime(LocalTime slotTime) {
        this.slotTime = slotTime;
        calculateEndTime();
        generateSlotLabel();
    }
    
    public boolean isAvailable() {
        return isAvailable;
    }
    
    public void setAvailable(boolean available) {
        isAvailable = available;
    }
    
    public String getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
        // If an appointment is assigned, slot is no longer available
        if (appointmentId != null && !appointmentId.isEmpty()) {
            this.isAvailable = false;
        }
    }
    
    public LocalTime getEndTime() {
        return endTime;
    }
    
    public String getSlotLabel() {
        if (slotLabel == null || slotLabel.isEmpty()) {
            generateSlotLabel();
        }
        return slotLabel;
    }
    
    public int getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
        calculateEndTime();
        generateSlotLabel();
    }
    
    /**
     * Set duration based on service type
     */
    public void setDurationByService(String serviceType) {
        switch (serviceType.toUpperCase()) {
            case "CONSULTATION":
            case "CHECKUP":
                this.durationMinutes = DURATION_CONSULTATION;
                break;
            case "TREATMENT":
            case "PROCEDURE":
                this.durationMinutes = DURATION_TREATMENT;
                break;
            case "SURGERY":
            case "LASER":
                this.durationMinutes = DURATION_PROCEDURE;
                break;
            default:
                this.durationMinutes = DURATION_CONSULTATION;
        }
        calculateEndTime();
        generateSlotLabel();
    }
    
    /**
     * Get duration in formatted string
     */
    public String getFormattedDuration() {
        if (durationMinutes < 60) {
            return durationMinutes + " minutes";
        } else {
            int hours = durationMinutes / 60;
            int minutes = durationMinutes % 60;
            if (minutes == 0) {
                return hours + " hour" + (hours > 1 ? "s" : "");
            } else {
                return hours + " hour" + (hours > 1 ? "s" : "") + " " + minutes + " minutes";
            }
        }
    }
    
    @Override
    public String toString() {
        return "TimeSlot{" +
                "slotId='" + slotId + '\'' +
                ", doctorId='" + doctorId + '\'' +
                ", slotDate=" + slotDate +
                ", slotTime=" + slotTime +
                ", isAvailable=" + isAvailable +
                ", appointmentId='" + appointmentId + '\'' +
                ", durationMinutes=" + durationMinutes +
                '}';
    }
    
    /**
     * Compare two time slots for equality (excluding slotId)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        TimeSlot timeSlot = (TimeSlot) o;
        
        if (isAvailable != timeSlot.isAvailable) return false;
        if (durationMinutes != timeSlot.durationMinutes) return false;
        if (doctorId != null ? !doctorId.equals(timeSlot.doctorId) : timeSlot.doctorId != null) return false;
        if (slotDate != null ? !slotDate.equals(timeSlot.slotDate) : timeSlot.slotDate != null) return false;
        return slotTime != null ? slotTime.equals(timeSlot.slotTime) : timeSlot.slotTime == null;
    }
    
    @Override
    public int hashCode() {
        int result = doctorId != null ? doctorId.hashCode() : 0;
        result = 31 * result + (slotDate != null ? slotDate.hashCode() : 0);
        result = 31 * result + (slotTime != null ? slotTime.hashCode() : 0);
        result = 31 * result + (isAvailable ? 1 : 0);
        result = 31 * result + durationMinutes;
        return result;
    }
    
    /**
     * Builder pattern for TimeSlot
     */
    public static class Builder {
        private String slotId;
        private String doctorId;
        private LocalDate slotDate;
        private LocalTime slotTime;
        private boolean isAvailable = true;
        private String appointmentId;
        private int durationMinutes = DURATION_CONSULTATION;
        
        public Builder() {}
        
        public Builder withSlotId(String slotId) {
            this.slotId = slotId;
            return this;
        }
        
        public Builder forDoctor(String doctorId) {
            this.doctorId = doctorId;
            return this;
        }
        
        public Builder onDate(LocalDate date) {
            this.slotDate = date;
            return this;
        }
        
        public Builder atTime(LocalTime time) {
            this.slotTime = time;
            return this;
        }
        
        public Builder atTime(String timeString) {
            try {
                this.slotTime = LocalTime.parse(timeString);
            } catch (Exception e) {
                // Try with custom format
                String[] parts = timeString.split(":");
                if (parts.length >= 2) {
                    int hour = Integer.parseInt(parts[0]);
                    int minute = Integer.parseInt(parts[1]);
                    this.slotTime = LocalTime.of(hour, minute);
                }
            }
            return this;
        }
        
        public Builder available(boolean available) {
            this.isAvailable = available;
            return this;
        }
        
        public Builder withAppointment(String appointmentId) {
            this.appointmentId = appointmentId;
            return this;
        }
        
        public Builder withDuration(int minutes) {
            this.durationMinutes = minutes;
            return this;
        }
        
        public TimeSlot build() {
            TimeSlot timeSlot = new TimeSlot();
            timeSlot.setSlotId(slotId);
            timeSlot.setDoctorId(doctorId);
            timeSlot.setSlotDate(slotDate);
            timeSlot.setSlotTime(slotTime);
            timeSlot.setAvailable(isAvailable);
            timeSlot.setAppointmentId(appointmentId);
            timeSlot.setDurationMinutes(durationMinutes);
            timeSlot.calculateEndTime();
            timeSlot.generateSlotLabel();
            return timeSlot;
        }
    }
    
    /**
     * Factory methods for common time slots
     */
    public static TimeSlot createMorningSlot(String doctorId, LocalDate date, LocalTime time) {
        return new Builder()
                .forDoctor(doctorId)
                .onDate(date)
                .atTime(time)
                .withDuration(DURATION_CONSULTATION)
                .build();
    }
    
    public static TimeSlot createAfternoonSlot(String doctorId, LocalDate date, LocalTime time) {
        return new Builder()
                .forDoctor(doctorId)
                .onDate(date)
                .atTime(time)
                .withDuration(DURATION_TREATMENT)
                .build();
    }
    
    public static TimeSlot createEveningSlot(String doctorId, LocalDate date, LocalTime time) {
        return new Builder()
                .forDoctor(doctorId)
                .onDate(date)
                .atTime(time)
                .withDuration(DURATION_CONSULTATION)
                .build();
    }
    
    /**
     * Generate standard time slots for a day
     */
    public static java.util.List<TimeSlot> generateDailySlots(String doctorId, LocalDate date, 
                                                             int slotDuration, LocalTime startTime, 
                                                             LocalTime endTime) {
        java.util.List<TimeSlot> slots = new java.util.ArrayList<>();
        
        LocalTime currentTime = startTime;
        int slotCounter = 1;
        
        while (currentTime.isBefore(endTime)) {
            TimeSlot slot = new Builder()
                    .withSlotId(doctorId + "_" + date + "_" + String.format("%03d", slotCounter))
                    .forDoctor(doctorId)
                    .onDate(date)
                    .atTime(currentTime)
                    .withDuration(slotDuration)
                    .available(true)
                    .build();
            
            slots.add(slot);
            currentTime = currentTime.plusMinutes(slotDuration);
            slotCounter++;
            
            // Add 15-minute buffer between appointments
            currentTime = currentTime.plusMinutes(15);
        }
        
        return slots;
    }
    
    /**
     * Generate standard clinic hours slots
     */
    public static java.util.List<TimeSlot> generateClinicHours(String doctorId, LocalDate date) {
        java.util.List<TimeSlot> allSlots = new java.util.ArrayList<>();
        
        // Morning slots: 8:00 AM - 12:00 PM (30 min appointments)
        allSlots.addAll(generateDailySlots(doctorId, date, DURATION_CONSULTATION, MORNING_START, MORNING_END));
        
        // Afternoon slots: 1:00 PM - 5:00 PM (30 min appointments)
        allSlots.addAll(generateDailySlots(doctorId, date, DURATION_CONSULTATION, AFTERNOON_START, AFTERNOON_END));
        
        return allSlots;
    }
}