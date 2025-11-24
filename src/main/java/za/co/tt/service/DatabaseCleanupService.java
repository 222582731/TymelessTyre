package za.co.tt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import za.co.tt.domain.Order;
import za.co.tt.domain.Payment;
import za.co.tt.domain.Delivery;
import za.co.tt.repository.OrderRepository;
import za.co.tt.repository.PaymentRepository;
import za.co.tt.repository.DeliveryRepository;

import java.util.List;
import java.util.Optional;

/**
 * Database cleanup and integrity service for TymelessTyre
 * Handles orphaned records and constraint violations
 */
@Service
public class DatabaseCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseCleanupService.class);

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private DeliveryRepository deliveryRepository;

    /**
     * Check for and report duplicate delivery records
     * @param orderId Order ID to check
     * @return true if duplicates exist
     */
    public boolean hasDuplicateDeliveries(Long orderId) {
        if (orderId == null) {
            return false;
        }
        
        try {
            List<Delivery> deliveries = deliveryRepository.findAllByOrder_OrderId(orderId);
            if (deliveries.size() > 1) {
                logger.warn("Found {} duplicate deliveries for order {}", deliveries.size(), orderId);
                for (Delivery delivery : deliveries) {
                    logger.warn("  - Delivery ID: {}, Status: {}, Method: {}", 
                        delivery.getDeliveryId(), 
                        delivery.getDeliveryStatus(), 
                        delivery.getDeliveryMethod());
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error checking duplicate deliveries for order {}: {}", orderId, e.getMessage());
            return false;
        }
    }

    /**
     * Check for and report duplicate payment records
     * @param orderId Order ID to check
     * @return true if duplicates exist
     */
    public boolean hasDuplicatePayments(Long orderId) {
        if (orderId == null) {
            return false;
        }
        
        try {
            List<Payment> payments = paymentRepository.findAllByOrder_OrderId(orderId);
            if (payments.size() > 1) {
                logger.warn("Found {} duplicate payments for order {}", payments.size(), orderId);
                for (Payment payment : payments) {
                    logger.warn("  - Payment ID: {}, Status: {}, Method: {}", 
                        payment.getPaymentId(), 
                        payment.getPaymentStatus(), 
                        payment.getPaymentMethod());
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error checking duplicate payments for order {}: {}", orderId, e.getMessage());
            return false;
        }
    }

    /**
     * Clean up duplicate delivery records for an order (keeps the first one)
     * @param orderId Order ID to clean up
     * @return number of records removed
     */
    @Transactional
    public int cleanupDuplicateDeliveries(Long orderId) {
        if (orderId == null) {
            return 0;
        }
        
        try {
            List<Delivery> deliveries = deliveryRepository.findAllByOrder_OrderId(orderId);
            if (deliveries.size() <= 1) {
                return 0; // No duplicates
            }
            
            // Keep the first delivery, remove the rest
            Delivery keepDelivery = deliveries.get(0);
            int removedCount = 0;
            
            for (int i = 1; i < deliveries.size(); i++) {
                Delivery duplicateDelivery = deliveries.get(i);
                logger.info("Removing duplicate delivery ID {} for order {}", 
                    duplicateDelivery.getDeliveryId(), orderId);
                deliveryRepository.deleteById(duplicateDelivery.getDeliveryId());
                removedCount++;
            }
            
            logger.info("Cleaned up {} duplicate deliveries for order {}, kept delivery ID {}", 
                removedCount, orderId, keepDelivery.getDeliveryId());
            
            return removedCount;
        } catch (Exception e) {
            logger.error("Error cleaning up duplicate deliveries for order {}: {}", orderId, e.getMessage());
            return 0;
        }
    }

    /**
     * Clean up duplicate payment records for an order (keeps the first one)
     * @param orderId Order ID to clean up
     * @return number of records removed
     */
    @Transactional
    public int cleanupDuplicatePayments(Long orderId) {
        if (orderId == null) {
            return 0;
        }
        
        try {
            List<Payment> payments = paymentRepository.findAllByOrder_OrderId(orderId);
            if (payments.size() <= 1) {
                return 0; // No duplicates
            }
            
            // Keep the first payment, remove the rest
            Payment keepPayment = payments.get(0);
            int removedCount = 0;
            
            for (int i = 1; i < payments.size(); i++) {
                Payment duplicatePayment = payments.get(i);
                logger.info("Removing duplicate payment ID {} for order {}", 
                    duplicatePayment.getPaymentId(), orderId);
                paymentRepository.deleteById(duplicatePayment.getPaymentId());
                removedCount++;
            }
            
            logger.info("Cleaned up {} duplicate payments for order {}, kept payment ID {}", 
                removedCount, orderId, keepPayment.getPaymentId());
            
            return removedCount;
        } catch (Exception e) {
            logger.error("Error cleaning up duplicate payments for order {}: {}", orderId, e.getMessage());
            return 0;
        }
    }

    /**
     * Get integrity report for an order
     * @param orderId Order ID to check
     * @return integrity report
     */
    public String getIntegrityReport(Long orderId) {
        if (orderId == null) {
            return "Invalid order ID";
        }
        
        StringBuilder report = new StringBuilder();
        report.append("Integrity Report for Order ID: ").append(orderId).append("\n");
        
        try {
            // Check if order exists
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                report.append("❌ Order not found\n");
                return report.toString();
            }
            
            Order order = orderOpt.get();
            report.append("✅ Order exists - Status: ").append(order.getOrderStatus()).append("\n");
            
            // Check payments
            List<Payment> payments = paymentRepository.findAllByOrder_OrderId(orderId);
            report.append("💰 Payments: ").append(payments.size());
            if (payments.size() == 0) {
                report.append(" (None)");
            } else if (payments.size() == 1) {
                report.append(" (Normal)");
            } else {
                report.append(" (⚠️  Duplicates detected!)");
            }
            report.append("\n");
            
            // Check deliveries
            List<Delivery> deliveries = deliveryRepository.findAllByOrder_OrderId(orderId);
            report.append("🚚 Deliveries: ").append(deliveries.size());
            if (deliveries.size() == 0) {
                report.append(" (None)");
            } else if (deliveries.size() == 1) {
                report.append(" (Normal)");
            } else {
                report.append(" (⚠️  Duplicates detected!)");
            }
            report.append("\n");
            
            // Overall status
            boolean hasIssues = payments.size() > 1 || deliveries.size() > 1;
            if (hasIssues) {
                report.append("🔧 Recommendation: Run cleanup for this order\n");
            } else {
                report.append("✅ Order integrity looks good\n");
            }
            
        } catch (Exception e) {
            report.append("❌ Error checking integrity: ").append(e.getMessage()).append("\n");
        }
        
        return report.toString();
    }
    
    /**
     * Perform full cleanup for an order
     * @param orderId Order ID to clean up
     * @return cleanup summary
     */
    @Transactional
    public String performFullCleanup(Long orderId) {
        StringBuilder summary = new StringBuilder();
        summary.append("Cleanup Summary for Order ID: ").append(orderId).append("\n");
        
        try {
            int removedPayments = cleanupDuplicatePayments(orderId);
            int removedDeliveries = cleanupDuplicateDeliveries(orderId);
            
            summary.append("💰 Removed duplicate payments: ").append(removedPayments).append("\n");
            summary.append("🚚 Removed duplicate deliveries: ").append(removedDeliveries).append("\n");
            
            if (removedPayments > 0 || removedDeliveries > 0) {
                summary.append("✅ Cleanup completed successfully\n");
            } else {
                summary.append("ℹ️  No cleanup needed - order is already clean\n");
            }
            
        } catch (Exception e) {
            summary.append("❌ Cleanup failed: ").append(e.getMessage()).append("\n");
        }
        
        return summary.toString();
    }
}