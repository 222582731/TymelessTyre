package za.co.tt.domain.Enum;

public enum DeliveryMethod {
    COLLECTION("Collection from Store"),
    DELIVERY("Delivery to Address");

    private final String displayName;

    DeliveryMethod(String displayName) {
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