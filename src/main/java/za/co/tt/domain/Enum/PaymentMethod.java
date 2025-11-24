package za.co.tt.domain.Enum;

public enum PaymentMethod {
    CASH_ON_DELIVERY("Cash on Delivery"),
    CASH_ON_COLLECTION("Cash on Collection");

    private final String displayName;

    PaymentMethod(String displayName) {
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