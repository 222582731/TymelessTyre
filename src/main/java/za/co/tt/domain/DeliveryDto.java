package za.co.tt.domain;

import za.co.tt.domain.Enum.DeliveryMethod;
import za.co.tt.domain.Enum.DeliveryStatus;
import java.time.LocalDateTime;

public class DeliveryDto {
    private Long deliveryId;
    private Long orderId;
    private Long addressId;
    private DeliveryMethod deliveryMethod;
    private DeliveryStatus deliveryStatus;
    private String courierName;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private AddressDto deliveryAddress;

    // Default constructor
    public DeliveryDto() {}

    // Constructor for creation (without ID)
    public DeliveryDto(Long orderId, Long addressId, DeliveryMethod deliveryMethod,
                      DeliveryStatus deliveryStatus, String courierName, LocalDateTime estimatedDeliveryDate) {
        this.orderId = orderId;
        this.addressId = addressId;
        this.deliveryMethod = deliveryMethod;
        this.deliveryStatus = deliveryStatus;
        this.courierName = courierName;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    // Full constructor
    public DeliveryDto(Long deliveryId, Long orderId, Long addressId, DeliveryMethod deliveryMethod,
                      DeliveryStatus deliveryStatus, String courierName, 
                      LocalDateTime estimatedDeliveryDate, LocalDateTime actualDeliveryDate) {
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.addressId = addressId;
        this.deliveryMethod = deliveryMethod;
        this.deliveryStatus = deliveryStatus;
        this.courierName = courierName;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        this.actualDeliveryDate = actualDeliveryDate;
    }

    // Getters and setters
    public Long getDeliveryId() { return deliveryId; }
    public void setDeliveryId(Long deliveryId) { this.deliveryId = deliveryId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }

    public DeliveryMethod getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(DeliveryMethod deliveryMethod) { this.deliveryMethod = deliveryMethod; }

    public DeliveryStatus getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(DeliveryStatus deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public String getCourierName() { return courierName; }
    public void setCourierName(String courierName) { this.courierName = courierName; }

    public LocalDateTime getEstimatedDeliveryDate() { return estimatedDeliveryDate; }
    public void setEstimatedDeliveryDate(LocalDateTime estimatedDeliveryDate) { this.estimatedDeliveryDate = estimatedDeliveryDate; }

    public LocalDateTime getActualDeliveryDate() { return actualDeliveryDate; }
    public void setActualDeliveryDate(LocalDateTime actualDeliveryDate) { this.actualDeliveryDate = actualDeliveryDate; }

    public AddressDto getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(AddressDto deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    @Override
    public String toString() {
        return "DeliveryDto{" +
                "deliveryId=" + deliveryId +
                ", orderId=" + orderId +
                ", addressId=" + addressId +
                ", deliveryMethod=" + deliveryMethod +
                ", deliveryStatus=" + deliveryStatus +
                ", courierName='" + courierName + '\'' +
                ", estimatedDeliveryDate=" + estimatedDeliveryDate +
                ", actualDeliveryDate=" + actualDeliveryDate +
                '}';
    }
}