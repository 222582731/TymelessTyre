package za.co.tt.domain.Enum;

public enum AddressType {
    HOME("Home Address"),
    WORK("Work Address"),
    BILLING("Billing Address"),
    SHIPPING("Shipping Address"),
    OTHER("Other Address");

    private final String displayName;

    AddressType(String displayName) {
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
