package za.co.tt.domain;

import za.co.tt.domain.Enum.PaymentMethod;
import za.co.tt.domain.Enum.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDto {
    private Long paymentId;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private Long userId;
    private Long orderId;

    // Default constructor
    public PaymentDto() {}

    // Constructor for creation (without ID)
    public PaymentDto(PaymentMethod paymentMethod, PaymentStatus paymentStatus, 
                     BigDecimal amount, LocalDateTime paymentDate, Long userId, Long orderId) {
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.userId = userId;
        this.orderId = orderId;
    }

    // Full constructor
    public PaymentDto(Long paymentId, PaymentMethod paymentMethod, PaymentStatus paymentStatus,
                     BigDecimal amount, LocalDateTime paymentDate, Long userId, Long orderId) {
        this.paymentId = paymentId;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.userId = userId;
        this.orderId = orderId;
    }

    // Getters and setters
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    @Override
    public String toString() {
        return "PaymentDto{" +
                "paymentId=" + paymentId +
                ", paymentMethod=" + paymentMethod +
                ", paymentStatus=" + paymentStatus +
                ", amount=" + amount +
                ", paymentDate=" + paymentDate +
                ", userId=" + userId +
                ", orderId=" + orderId +
                '}';
    }
}