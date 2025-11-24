package za.co.tt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import za.co.tt.domain.Order;
import za.co.tt.domain.Delivery;
import za.co.tt.domain.Payment;
import za.co.tt.domain.Enum.OrderStatus;
import za.co.tt.domain.Enum.DeliveryStatus;
import za.co.tt.domain.Enum.PaymentStatus;
import za.co.tt.repository.OrderRepository;

import java.util.Optional;

/**
 * Service to manage order status transitions and synchronization with delivery/payment status
 */
@Service
public class OrderStatusService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderStatusService.class);

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Update order status based on delivery completion
     * Called when delivery status changes to DELIVERED or COLLECTED
     */
    @Transactional
    public void updateOrderStatusFromDelivery(Long orderId, DeliveryStatus deliveryStatus) {
        if (orderId == null) {
            logger.warn("Cannot update order status: orderId is null");
            return;
        }

        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                logger.warn("Order not found with ID: {}", orderId);
                return;
            }

            Order order = orderOpt.get();
            OrderStatus currentStatus = order.getOrderStatus();
            
            // Only update if delivery is completed and order is not already completed or cancelled
            if (isDeliveryCompleted(deliveryStatus) && 
                currentStatus != OrderStatus.COMPLETED && 
                currentStatus != OrderStatus.CANCELLED) {
                
                logger.info("Updating order {} status from {} to COMPLETED due to delivery completion ({})", 
                    orderId, currentStatus, deliveryStatus);
                
                order.setOrderStatus(OrderStatus.COMPLETED);
                orderRepository.save(order);
                
                logger.info("Order {} status successfully updated to COMPLETED", orderId);
            } else {
                logger.debug("Order {} status not updated: current={}, deliveryStatus={}, isCompleted={}", 
                    orderId, currentStatus, deliveryStatus, isDeliveryCompleted(deliveryStatus));
            }
            
        } catch (Exception e) {
            logger.error("Error updating order status from delivery for order {}: {}", orderId, e.getMessage(), e);
        }
    }

    /**
     * Update order status based on payment confirmation
     * Called when payment status changes to CONFIRMED or COMPLETED
     */
    @Transactional
    public void updateOrderStatusFromPayment(Long orderId, PaymentStatus paymentStatus) {
        if (orderId == null) {
            logger.warn("Cannot update order status: orderId is null");
            return;
        }

        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                logger.warn("Order not found with ID: {}", orderId);
                return;
            }

            Order order = orderOpt.get();
            OrderStatus currentStatus = order.getOrderStatus();
            
            // Update to CONFIRMED when payment is confirmed and order is PENDING
            if (isPaymentConfirmed(paymentStatus) && currentStatus == OrderStatus.PENDING) {
                logger.info("Updating order {} status from PENDING to CONFIRMED due to payment confirmation", orderId);
                
                order.setOrderStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
                
                logger.info("Order {} status successfully updated to CONFIRMED", orderId);
            } else {
                logger.debug("Order {} status not updated from payment: current={}, paymentStatus={}", 
                    orderId, currentStatus, paymentStatus);
            }
            
        } catch (Exception e) {
            logger.error("Error updating order status from payment for order {}: {}", orderId, e.getMessage(), e);
        }
    }

    /**
     * Manually update order status with validation
     */
    @Transactional
    public boolean updateOrderStatus(Long orderId, OrderStatus newStatus) {
        if (orderId == null || newStatus == null) {
            logger.warn("Cannot update order status: orderId or newStatus is null");
            return false;
        }

        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                logger.warn("Order not found with ID: {}", orderId);
                return false;
            }

            Order order = orderOpt.get();
            OrderStatus currentStatus = order.getOrderStatus();
            
            // Validate status transition
            if (isValidStatusTransition(currentStatus, newStatus)) {
                logger.info("Updating order {} status from {} to {}", orderId, currentStatus, newStatus);
                
                order.setOrderStatus(newStatus);
                orderRepository.save(order);
                
                logger.info("Order {} status successfully updated to {}", orderId, newStatus);
                return true;
            } else {
                logger.warn("Invalid status transition for order {}: {} -> {}", orderId, currentStatus, newStatus);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error updating order status for order {}: {}", orderId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if order can be reviewed (order is completed)
     */
    public boolean canOrderBeReviewed(Long orderId) {
        if (orderId == null) {
            return false;
        }

        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return false;
            }

            Order order = orderOpt.get();
            return order.getOrderStatus() == OrderStatus.COMPLETED;
            
        } catch (Exception e) {
            logger.error("Error checking review eligibility for order {}: {}", orderId, e.getMessage());
            return false;
        }
    }

    /**
     * Get order status for an order
     */
    public Optional<OrderStatus> getOrderStatus(Long orderId) {
        if (orderId == null) {
            return Optional.empty();
        }

        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            return orderOpt.map(Order::getOrderStatus);
            
        } catch (Exception e) {
            logger.error("Error getting order status for order {}: {}", orderId, e.getMessage());
            return Optional.empty();
        }
    }

    // Helper methods

    private boolean isDeliveryCompleted(DeliveryStatus status) {
        return status == DeliveryStatus.DELIVERED || status == DeliveryStatus.COLLECTED;
    }

    private boolean isPaymentConfirmed(PaymentStatus status) {
        return status == PaymentStatus.CONFIRMED || status == PaymentStatus.COMPLETED;
    }

    private boolean isValidStatusTransition(OrderStatus from, OrderStatus to) {
        if (from == to) {
            return true; // Same status is valid
        }

        // Define valid transitions
        switch (from) {
            case PENDING:
                return to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED:
                return to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED;
            case PROCESSING:
                return to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
            case SHIPPED:
                return to == OrderStatus.COMPLETED || to == OrderStatus.CANCELLED;
            case COMPLETED:
                return false; // Completed orders cannot be changed
            case CANCELLED:
                return false; // Cancelled orders cannot be changed
            default:
                return false;
        }
    }
}