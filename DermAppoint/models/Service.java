package models;

public class Service {

    private String serviceId;
    private String serviceName;
    private String description;
    private double price;
    private int durationMinutes;
    private String requiredPreparation;
    private boolean active;

    // ✅ No-arg constructor (required for DB, frameworks, Swing tables)
    public Service() {
    }

    // ✅ Full constructor (useful when creating objects)
    public Service(String serviceId,
            String serviceName,
            String description,
            double price,
            int durationMinutes,
            String requiredPreparation,
            boolean active) {

        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.description = description;
        this.price = price;
        this.durationMinutes = durationMinutes;
        this.requiredPreparation = requiredPreparation;
        this.active = active;
    }

    // Getters and setters
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Duration must be greater than zero");
        }
        this.durationMinutes = durationMinutes;
    }

    public String getRequiredPreparation() {
        return requiredPreparation;
    }

    public void setRequiredPreparation(String requiredPreparation) {
        this.requiredPreparation = requiredPreparation;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
