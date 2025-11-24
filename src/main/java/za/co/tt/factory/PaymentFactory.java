package za.co.tt.factory;

import za.co.tt.domain.Order;
import za.co.tt.domain.Payment;
import za.co.tt.domain.User;
import za.co.tt.domain.Enum.PaymentMethod;
import za.co.tt.domain.Enum.PaymentStatus;
import za.co.tt.util.Helper;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class PaymentFactory {

    public static Payment createPayment(PaymentMethod paymentMethod, PaymentStatus paymentStatus, 
                                      BigDecimal amount, LocalDateTime paymentDate, User user, Order order) {
        if (paymentMethod == null || paymentStatus == null ||
                amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || 
                paymentDate == null || user == null || order == null || 
                paymentDate.isAfter(LocalDateTime.now())) {
            return null;
        }

        return new Payment.Builder()
                .setPaymentMethod(paymentMethod)
                .setPaymentStatus(paymentStatus)
                .setAmount(amount)
                .setPaymentDate(paymentDate)
                .setUser(user)
                .setOrder(order)
                .build();
    }

    // Convenience method for backward compatibility with String parameters
    public static Payment createPayment(String paymentMethodStr, String paymentStatusStr, 
                                      double amount, LocalDateTime paymentDate, User user, Order order) {
        if (Helper.isNullOrEmpty(paymentMethodStr) || Helper.isNullOrEmpty(paymentStatusStr)) {
            return null;
        }

        try {
            PaymentMethod paymentMethod = PaymentMethod.valueOf(paymentMethodStr.toUpperCase().replace(" ", "_"));
            PaymentStatus paymentStatus = PaymentStatus.valueOf(paymentStatusStr.toUpperCase().replace(" ", "_"));
            BigDecimal amountBD = BigDecimal.valueOf(amount);
            
            return createPayment(paymentMethod, paymentStatus, amountBD, paymentDate, user, order);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
