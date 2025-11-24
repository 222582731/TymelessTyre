/*Author: Bonke Bulana - 220539995*/
package za.co.tt.domain;

import jakarta.persistence.*;
import za.co.tt.domain.Enum.PaymentMethod;
import za.co.tt.domain.Enum.PaymentStatus;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name="payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Order order;

    protected Payment() {
    }

    private Payment(Builder builder) {
        this.paymentId = builder.paymentId;
        this.paymentMethod = builder.paymentMethod;
        this.paymentStatus = builder.paymentStatus;
        this.amount = builder.amount;
        this.paymentDate = builder.paymentDate;
        this.user = builder.user;
        this.order = builder.order;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public User getUser() {
        return user;
    }

    public Order getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", amount=" + amount +
                ", paymentDate=" + paymentDate +
                ", user=" + (user != null ? user.getUserId() : null) +
                ", order=" + (order != null ? order.getOrderId() : null) +
                '}';
    }

    public static class Builder {
        private Long paymentId;
        private PaymentMethod paymentMethod;
        private PaymentStatus paymentStatus;
        private BigDecimal amount;
        private LocalDateTime paymentDate;
        private User user;
        private Order order;

        public Builder setPaymentId(Long paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public Builder setPaymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder setPaymentStatus(PaymentStatus paymentStatus) {
            this.paymentStatus = paymentStatus;
            return this;
        }

        public Builder setAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder setAmount(double amount) {
            this.amount = BigDecimal.valueOf(amount);
            return this;
        }

        public Builder setPaymentDate(LocalDateTime paymentDate) {
            this.paymentDate = paymentDate;
            return this;
        }

        public Builder setUser(User user) {
            this.user = user;
            return this;
        }

        public Builder setOrder(Order order) {
            this.order = order;
            return this;
        }

        public Builder copy(Payment payment) {
            this.paymentId = payment.paymentId;
            this.paymentMethod = payment.paymentMethod;
            this.paymentStatus = payment.paymentStatus;
            this.amount = payment.amount;
            this.paymentDate = payment.paymentDate;
            this.user = payment.user;
            this.order = payment.order;
            return this;
        }

        public Payment build() {
            return new Payment(this);
        }
    }
}

