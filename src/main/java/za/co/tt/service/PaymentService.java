package za.co.tt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.co.tt.domain.Payment;
import za.co.tt.domain.PaymentDto;
import za.co.tt.domain.Order;
import za.co.tt.domain.User;
import za.co.tt.domain.Enum.PaymentMethod;
import za.co.tt.domain.Enum.PaymentStatus;
import za.co.tt.repository.PaymentRepository;
import za.co.tt.repository.OrderRepository;
import za.co.tt.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService implements IPaymentService {

    @Autowired
    private PaymentRepository repository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public Payment save(Payment payment) {
        return repository.save(payment);
    }

    @Override
    public Payment update(Payment payment) {
        if (repository.existsById(payment.getPaymentId())) {
            return repository.save(payment);
        }
        return null; // or throw exception
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Entity with ID " + id + " not found");
        }

        repository.deleteById(id);
    } //made some changes to avoid having errors

    @Override
    public Payment read(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public List<Payment> findAll() {
        return repository.findAll();
    }

    // Additional methods specific to PaymentService
    public List<Payment> findPaymentsByUserId(Long userId) {
        return repository.findByUser_UserId(userId);
    }

    public Optional<Payment> findPaymentByOrderId(Long orderId) {
        return repository.findByOrder_OrderId(orderId);
    }

    public List<Payment> findPaymentsByStatus(PaymentStatus status) {
        return repository.findByPaymentStatus(status);
    }

    public List<Payment> findPaymentsByMethod(PaymentMethod method) {
        return repository.findByPaymentMethod(method);
    }

    /**
     * Create a Cash on Delivery payment for an order
     */
    public Payment createCashOnDeliveryPayment(Long orderId, Long userId) {
        // Validate order exists and belongs to user
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order not found with ID: " + orderId);
        }
        
        Order order = orderOpt.get();
        if (!order.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to user");
        }

        // Check if payment already exists for this order
        Optional<Payment> existingPayment = repository.findByOrder_OrderId(orderId);
        if (existingPayment.isPresent()) {
            throw new IllegalArgumentException("Payment already exists for order: " + orderId);
        }

        // Create the payment
        Payment payment = new Payment.Builder()
                .setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                .setPaymentStatus(PaymentStatus.PENDING)
                .setAmount(order.getTotalAmount())
                .setPaymentDate(LocalDateTime.now())
                .setUser(order.getUser())
                .setOrder(order)
                .build();

        return repository.save(payment);
    }

    /**
     * Create a Cash on Collection payment for an order
     */
    public Payment createCashOnCollectionPayment(Long orderId, Long userId) {
        // Validate order exists and belongs to user
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order not found with ID: " + orderId);
        }
        
        Order order = orderOpt.get();
        if (!order.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to user");
        }

        // Check if payment already exists for this order
        Optional<Payment> existingPayment = repository.findByOrder_OrderId(orderId);
        if (existingPayment.isPresent()) {
            throw new IllegalArgumentException("Payment already exists for order: " + orderId);
        }

        // Create the payment
        Payment payment = new Payment.Builder()
                .setPaymentMethod(PaymentMethod.CASH_ON_COLLECTION)
                .setPaymentStatus(PaymentStatus.PENDING)
                .setAmount(order.getTotalAmount())
                .setPaymentDate(LocalDateTime.now())
                .setUser(order.getUser())
                .setOrder(order)
                .build();

        return repository.save(payment);
    }

    /**
     * Update payment status (for admin or automated processes)
     */
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus newStatus) {
        Optional<Payment> paymentOpt = repository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new IllegalArgumentException("Payment not found with ID: " + paymentId);
        }

        Payment payment = paymentOpt.get();
        Payment updatedPayment = new Payment.Builder()
                .copy(payment)
                .setPaymentStatus(newStatus)
                .build();

        return repository.save(updatedPayment);
    }

    /**
     * Convert Payment to PaymentDto
     */
    public PaymentDto convertToDto(Payment payment) {
        if (payment == null) {
            return null;
        }

        return new PaymentDto(
                payment.getPaymentId(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getAmount(),
                payment.getPaymentDate(),
                payment.getUser() != null ? payment.getUser().getUserId() : null,
                payment.getOrder() != null ? payment.getOrder().getOrderId() : null
        );
    }

    /**
     * Get available payment methods (currently only cash options)
     */
    public PaymentMethod[] getAvailablePaymentMethods() {
        return PaymentMethod.values();
    }
}