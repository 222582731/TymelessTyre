package za.co.tt.domain;

import jakarta.persistence.*;
import za.co.tt.domain.Enum.DeliveryMethod;
import za.co.tt.domain.Enum.DeliveryStatus;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deliveryId;

    @OneToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Order order;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryMethod deliveryMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus deliveryStatus;

    private String courierName;

    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;

    public Delivery() {
    }
    
    public Delivery(Builder builder) {
        this.deliveryId = builder.deliveryId;
        this.order = builder.order;
        this.deliveryAddress = builder.deliveryAddress;
        this.deliveryMethod = builder.deliveryMethod;
        this.deliveryStatus = builder.deliveryStatus;
        this.courierName = builder.courierName;
        this.estimatedDeliveryDate = builder.estimatedDeliveryDate;
        this.actualDeliveryDate = builder.actualDeliveryDate;
    }

    public Long getDeliveryId() {
        return deliveryId;
    }

    public Order getOrder() {
        return order;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public DeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public String getCourierName() {
        return courierName;
    }

    public LocalDateTime getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public LocalDateTime getActualDeliveryDate() {
        return actualDeliveryDate;
    }

    @Override
    public String toString() {
        return "Delivery{" +
                "deliveryId=" + deliveryId +
                ", order=" + (order != null ? order.getOrderId() : null) +
                ", deliveryAddress=" + (deliveryAddress != null ? deliveryAddress.getAddressId() : null) +
                ", deliveryMethod=" + deliveryMethod +
                ", deliveryStatus=" + deliveryStatus +
                ", courierName='" + courierName + '\'' +
                ", estimatedDeliveryDate=" + estimatedDeliveryDate +
                ", actualDeliveryDate=" + actualDeliveryDate +
                '}';
    }

    public static class Builder {
        private Long deliveryId;
        private Order order;
        private Address deliveryAddress;
        private DeliveryMethod deliveryMethod;
        private DeliveryStatus deliveryStatus;
        private String courierName;
        private LocalDateTime estimatedDeliveryDate;
        private LocalDateTime actualDeliveryDate;

        public Builder setDeliveryId(Long deliveryId) {
            this.deliveryId = deliveryId;
            return this;
        }
        
        public Builder setOrder(Order order) {
            this.order = order;
            return this;
        }
        
        public Builder setDeliveryAddress(Address deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
            return this;
        }
        
        public Builder setDeliveryMethod(DeliveryMethod deliveryMethod) {
            this.deliveryMethod = deliveryMethod;
            return this;
        }
        
        public Builder setDeliveryStatus(DeliveryStatus deliveryStatus) {
            this.deliveryStatus = deliveryStatus;
            return this;
        }
        public Builder setCourierName(String courierName) {
            this.courierName = courierName;
            return this;
        }
        
        public Builder setEstimatedDeliveryDate(LocalDateTime estimatedDeliveryDate) {
            this.estimatedDeliveryDate = estimatedDeliveryDate;
            return this;
        }
        
        public Builder setActualDeliveryDate(LocalDateTime actualDeliveryDate) {
            this.actualDeliveryDate = actualDeliveryDate;
            return this;
        }

        public Builder copy(Delivery delivery) {
            this.deliveryId = delivery.deliveryId;
            this.order = delivery.order;
            this.deliveryAddress = delivery.deliveryAddress;
            this.deliveryMethod = delivery.deliveryMethod;
            this.deliveryStatus = delivery.deliveryStatus;
            this.courierName = delivery.courierName;
            this.estimatedDeliveryDate = delivery.estimatedDeliveryDate;
            this.actualDeliveryDate = delivery.actualDeliveryDate;
            return this;
        }

        public Delivery build() {
            return new Delivery(this);
        }
    }
}
