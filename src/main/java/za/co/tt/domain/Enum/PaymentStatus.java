package za.co.tt.domain.Enum;

public enum PaymentStatus {
    PENDING("Payment Pending"),
    CONFIRMED("Payment Confirmed"),
    COMPLETED("Payment Completed"),
    FAILED("Payment Failed"),
    CANCELLED("Payment Cancelled");

    private final String displayName;

    PaymentStatus(String displayName) {
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