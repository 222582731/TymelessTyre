package za.co.tt.controller;

import za.co.tt.service.OrderService;
import za.co.tt.service.PaymentService;
import za.co.tt.service.DeliveryService;
import za.co.tt.domain.Order;
import za.co.tt.domain.Payment;
import za.co.tt.domain.Delivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Debug controller to help understand database state
 */
@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "http://localhost:5173")
public class DebugController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private DeliveryService deliveryService;

    /**
     * Get database state summary
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/database-state")
    public ResponseEntity<?> getDatabaseState() {
        try {
            Map<String, Object> state = new HashMap<>();
            
            // Get recent orders
            List<Order> recentOrders = orderService.getAllOrders();
            state.put("totalOrders", recentOrders.size());
            
            // Get recent payments
            List<Payment> allPayments = paymentService.findAll();
            state.put("totalPayments", allPayments.size());
            
            // Get recent deliveries  
            List<Delivery> allDeliveries = deliveryService.findAll();
            state.put("totalDeliveries", allDeliveries.size());
            
            // Show last few orders with their payment/delivery status
            Map<String, Object> recentOrderDetails = new HashMap<>();
            for (int i = Math.max(0, recentOrders.size() - 5); i < recentOrders.size(); i++) {
                Order order = recentOrders.get(i);
                Map<String, Object> orderInfo = new HashMap<>();
                orderInfo.put("orderId", order.getOrderId());
                orderInfo.put("status", order.getOrderStatus());
                orderInfo.put("hasPayment", order.getPayment() != null);
                orderInfo.put("hasDelivery", order.getDelivery() != null);
                
                if (order.getPayment() != null) {
                    orderInfo.put("paymentId", order.getPayment().getPaymentId());
                }
                if (order.getDelivery() != null) {
                    orderInfo.put("deliveryId", order.getDelivery().getDeliveryId());
                }
                
                recentOrderDetails.put("order_" + order.getOrderId(), orderInfo);
            }
            
            state.put("recentOrders", recentOrderDetails);
            
            return ResponseEntity.ok(state);
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to get database state: " + e.getMessage()));
        }
    }
    
    /**
     * Check specific order status
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> checkOrder(@PathVariable Long orderId) {
        try {
            Map<String, Object> info = new HashMap<>();
            
            // Check if order exists
            var orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isEmpty()) {
                info.put("orderExists", false);
                return ResponseEntity.ok(info);
            }
            
            Order order = orderOpt.get();
            info.put("orderExists", true);
            info.put("orderId", order.getOrderId());
            info.put("orderStatus", order.getOrderStatus());
            info.put("userId", order.getUser().getUserId());
            
            // Check payment
            var paymentOpt = paymentService.findPaymentByOrderId(orderId);
            info.put("hasPayment", paymentOpt.isPresent());
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                info.put("paymentId", payment.getPaymentId());
                info.put("paymentStatus", payment.getPaymentStatus());
                info.put("paymentMethod", payment.getPaymentMethod());
            }
            
            // Check delivery
            var deliveryOpt = deliveryService.findByOrderId(orderId);
            info.put("hasDelivery", deliveryOpt.isPresent());
            if (deliveryOpt.isPresent()) {
                Delivery delivery = deliveryOpt.get();
                info.put("deliveryId", delivery.getDeliveryId());
                info.put("deliveryStatus", delivery.getDeliveryStatus());
                info.put("deliveryMethod", delivery.getDeliveryMethod());
            }
            
            return ResponseEntity.ok(info);
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to check order: " + e.getMessage()));
        }
    }
}