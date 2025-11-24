package za.co.tt.domain.Enum;

public enum DeliveryStatus {
    PENDING("Pending Collection/Delivery"),
    CONFIRMED("Confirmed for Collection/Delivery"),
    IN_TRANSIT("In Transit"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    READY_FOR_COLLECTION("Ready for Collection"),
    DELIVERED("Delivered"),
    COLLECTED("Collected"),
    FAILED_DELIVERY("Failed Delivery Attempt"),
    RETURNED("Returned to Store");

    private final String displayName;

    DeliveryStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}