
package za.co.tt.controller;

import za.co.tt.domain.Order;
import za.co.tt.domain.OrderDto;
import za.co.tt.domain.OrderStatusUpdateRequest;
import za.co.tt.domain.User;
import za.co.tt.domain.Enum.PaymentMethod;
import za.co.tt.domain.Enum.DeliveryMethod;
import za.co.tt.domain.Enum.OrderStatus;
import za.co.tt.service.IOrderService;
import za.co.tt.service.UserService;
import za.co.tt.service.OrderService;
import za.co.tt.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    private final IOrderService orderService;
    private final OrderService orderServiceImpl;
    private final UserService userService;

    @Autowired
    public OrderController(IOrderService orderService, OrderService orderServiceImpl, UserService userService) {
        this.orderService = orderService;
        this.orderServiceImpl = orderServiceImpl;
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/debug/basic")
    public ResponseEntity<?> getOrdersBasic() {
        try {
            List<Order> orders = orderService.getAllOrdersBasic();
            return ResponseEntity.ok("Basic findAll() found " + orders.size() + " orders");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error with basic findAll(): " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/debug/count")
    public ResponseEntity<?> getOrderCount() {
        try {
            long count = orderService.getAllOrders().size();
            return ResponseEntity.ok("Total orders in database: " + count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error counting orders: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/debug/raw")
    public ResponseEntity<?> getRawOrders() {
        try {
            List<Order> orders = orderService.getAllOrders();
            return ResponseEntity.ok("Found " + orders.size() + " orders: " + orders.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching raw orders: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<?> getAllOrdersForAdmin() {
        try {
            List<Order> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching orders: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Missing or invalid authorization header");
            }
            
            String token = authHeader.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
            }
            
            User user = userOpt.get();
            List<Order> orders = orderService.getOrdersByUserId(user.getUserId());
            
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid token or error fetching orders: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id, HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Missing or invalid authorization header");
            }
            
            String token = authHeader.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
            }
            
            User user = userOpt.get();
            Optional<Order> orderOpt = orderService.getOrderById(id);
            
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Order not found");
            }
            
            Order order = orderOpt.get();
            
            // Check if user has permission to view this order
            // Customers can only view their own orders, admins can view any order
            if (!"ADMIN".equals(user.getRole()) && !"ROLE_ADMIN".equals(user.getRole())) {
                if (!order.getUser().getUserId().equals(user.getUserId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You don't have permission to view this order");
                }
            }
            
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid token or error fetching order: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderDto orderDto, HttpServletRequest request) {
        try {
            // Log authentication details for debugging
            logger.info("Creating order request received");
            logger.info("Authorization header: {}", request.getHeader("Authorization"));
            
            // Get current authentication context
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (auth != null) {
                logger.info("Authentication found - Principal: {}, Authorities: {}", 
                    auth.getName(), auth.getAuthorities());
            } else {
                logger.warn("No authentication found in security context");
            }
            
            Order savedOrder = orderService.createOrder(orderDto);
            return ResponseEntity.ok(savedOrder);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid order data: {}", e.getMessage());
            return ResponseEntity.status(400).body("Invalid order data: " + e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Error creating order: {}", e.getMessage());
            return ResponseEntity.status(400).body("Error creating order: " + e.getMessage());
        }
    }

    // Temporary debug endpoint without authentication
    @PostMapping("/debug")
    public ResponseEntity<?> createOrderDebug(@RequestBody OrderDto orderDto, HttpServletRequest request) {
        try {
            logger.info("Debug: Creating order without authentication");
            logger.info("Debug: Order data - userId: {}, totalPrice: {}", orderDto.getUserId(), orderDto.getTotalPrice());
            
            Order savedOrder = orderService.createOrder(orderDto);
            return ResponseEntity.ok(savedOrder);
        } catch (IllegalArgumentException e) {
            logger.error("Debug: Invalid order data: {}", e.getMessage());
            return ResponseEntity.status(400).body("Invalid order data: " + e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Debug: Error creating order: {}", e.getMessage());
            return ResponseEntity.status(400).body("Error creating order: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/debug-status")
    public ResponseEntity<?> debugStatusUpdate(@PathVariable Long id, @RequestBody String rawBody) {
        try {
            logger.info("Debug: Raw request body for order {}: '{}'", id, rawBody);
            return ResponseEntity.ok("Raw body: " + rawBody);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Debug error: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status-simple")
    public ResponseEntity<?> updateOrderStatusSimple(@PathVariable Long id, @RequestBody String status) {
        try {
            logger.info("Received simple status update for order {}: '{}'", id, status);
            
            // Remove quotes if the status is sent as a JSON string
            String cleanStatus = status.replaceAll("\"", "").trim().toLowerCase();
            logger.info("Clean status: '{}'", cleanStatus);
            
            // Use the same validation logic
            OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(cleanStatus);
            return updateOrderStatus(id, request);
            
        } catch (Exception e) {
            logger.error("Error in simple status update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating order status: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody OrderStatusUpdateRequest request) {
        try {
            String status = request.getStatus();
            logger.info("Received status update request for order {}: status = '{}'", id, status);
            
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Status is required");
            }
            
            // Parse status to OrderStatus enum
            OrderStatus orderStatus;
            try {
                orderStatus = OrderStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid status received: '{}'. Valid statuses: {}", status, OrderStatus.values());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid status '" + status + "'. Valid statuses: " + java.util.Arrays.toString(OrderStatus.values()));
            }
            
            logger.info("Status validation successful: '{}' -> '{}'", status, orderStatus);
            
            Optional<Order> orderOpt = orderService.getOrderById(id);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Order not found with id: " + id);
            }
            
            Order existingOrder = orderOpt.get();
            
            // Create updated order with new status
            Order updatedOrder = new Order();
            updatedOrder.setOrderStatus(orderStatus);
            updatedOrder.setTotalAmount(existingOrder.getTotalAmount());
            
            Order result = orderService.updateOrder(id, updatedOrder);
            logger.info("Order {} status updated to '{}'", id, orderStatus);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error updating order status for order {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating order status: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order order) {
        try {
            Order updatedOrder = orderService.updateOrder(id, order);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable Long userId) {
        try {
            List<Order> orders = orderService.getOrdersByUserId(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching orders: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        try {
            List<Order> orders = orderService.getOrdersByStatus(status);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching orders: " + e.getMessage());
        }
    }

    /**
     * Create order with payment and delivery - Enhanced endpoint
     * Requires authentication - customers can create orders for themselves, admins can create for anyone
     */
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @PostMapping("/complete")
    public ResponseEntity<?> createCompleteOrder(@RequestBody java.util.Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            // Verify authentication and get current user
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("error", "Missing or invalid authorization header"));
            }
            
            String token = authHeader.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("error", "User not found"));
            }
            
            User authenticatedUser = userOpt.get();
            
            // Extract order data
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> orderData = (java.util.Map<String, Object>) request.get("order");
            String paymentMethodStr = (String) request.get("paymentMethod");
            String deliveryMethodStr = (String) request.get("deliveryMethod");
            Long addressId = request.get("addressId") != null ? 
                Long.valueOf(request.get("addressId").toString()) : null;

            // Convert order data to OrderDto
            OrderDto orderDto = convertMapToOrderDto(orderData);
            
            // Security check: Ensure customer can only create orders for themselves
            if (orderDto.getUserId() != null) {
                if (!"ADMIN".equals(authenticatedUser.getRole()) && !"ROLE_ADMIN".equals(authenticatedUser.getRole())) {
                    if (!orderDto.getUserId().equals(authenticatedUser.getUserId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(java.util.Map.of("error", "You can only create orders for yourself"));
                    }
                }
            } else {
                // If no userId specified, use the authenticated user's ID (for customers)
                if (!"ADMIN".equals(authenticatedUser.getRole()) && !"ROLE_ADMIN".equals(authenticatedUser.getRole())) {
                    orderDto.setUserId(authenticatedUser.getUserId());
                }
            }
            
            // Security check for address ownership (if addressId is provided)
            if (addressId != null && !"ADMIN".equals(authenticatedUser.getRole()) && !"ROLE_ADMIN".equals(authenticatedUser.getRole())) {
                // Verify that the address belongs to the authenticated user
                boolean addressBelongsToUser = orderServiceImpl.verifyAddressOwnership(addressId, authenticatedUser.getUserId());
                if (!addressBelongsToUser) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(java.util.Map.of("error", "You can only use your own addresses"));
                }
            }
            
            // Parse enums
            PaymentMethod paymentMethod = PaymentMethod.valueOf(paymentMethodStr.toUpperCase().replace(" ", "_"));
            DeliveryMethod deliveryMethod = DeliveryMethod.valueOf(deliveryMethodStr.toUpperCase());

            // Validate the request
            orderServiceImpl.validateOrderCreationRequest(orderDto, paymentMethod, deliveryMethod, addressId);

            // Create complete order
            Order completedOrder = orderServiceImpl.createOrderWithPaymentAndDelivery(
                orderDto, paymentMethod, deliveryMethod, addressId);

            logger.info("Successfully created complete order {} with payment and delivery", 
                completedOrder.getOrderId());

            return ResponseEntity.status(HttpStatus.CREATED).body(java.util.Map.of(
                "message", "Order created successfully",
                "orderId", completedOrder.getOrderId(),
                "paymentId", completedOrder.getPayment() != null ? completedOrder.getPayment().getPaymentId() : null,
                "deliveryId", completedOrder.getDelivery() != null ? completedOrder.getDelivery().getDeliveryId() : null,
                "orderStatus", completedOrder.getOrderStatus(),
                "totalAmount", completedOrder.getTotalAmount()
            ));

        } catch (IllegalArgumentException e) {
            logger.error("Validation error creating complete order: {}", e.getMessage());
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating complete order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("error", "Failed to create order: " + e.getMessage()));
        }
    }

    /**
     * Get complete order information including payment and delivery
     * Requires authentication - customers can only view their own orders, admins can view any order
     */
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @GetMapping("/{id}/complete")
    public ResponseEntity<?> getCompleteOrder(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            // Verify authentication and get current user
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("error", "Missing or invalid authorization header"));
            }
            
            String token = authHeader.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("error", "User not found"));
            }
            
            User authenticatedUser = userOpt.get();
            
            Optional<Order> orderOpt = orderServiceImpl.getCompleteOrderById(id);
            
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("error", "Order not found"));
            }
            
            Order order = orderOpt.get();
            
            // Security check: Customers can only view their own orders, admins can view any order
            if (!"ADMIN".equals(authenticatedUser.getRole()) && !"ROLE_ADMIN".equals(authenticatedUser.getRole())) {
                if (!order.getUser().getUserId().equals(authenticatedUser.getUserId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(java.util.Map.of("error", "You don't have permission to view this order"));
                }
            }
            
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("order", order);
            response.put("hasPayment", order.getPayment() != null);
            response.put("hasDelivery", order.getDelivery() != null);
            
            if (order.getPayment() != null) {
                response.put("paymentMethod", order.getPayment().getPaymentMethod());
                response.put("paymentStatus", order.getPayment().getPaymentStatus());
            }
            
            if (order.getDelivery() != null) {
                response.put("deliveryMethod", order.getDelivery().getDeliveryMethod());
                response.put("deliveryStatus", order.getDelivery().getDeliveryStatus());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching complete order {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("error", "Failed to fetch order details"));
        }
    }

    /**
     * Check if user has valid addresses for delivery
     * Requires authentication - customers can only check their own readiness, admins can check for anyone
     */
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @GetMapping("/user/{userId}/delivery-ready")
    public ResponseEntity<?> checkDeliveryReadiness(@PathVariable Long userId, HttpServletRequest httpRequest) {
        try {
            // Verify authentication and get current user
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("error", "Missing or invalid authorization header"));
            }
            
            String token = authHeader.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("error", "User not found"));
            }
            
            User authenticatedUser = userOpt.get();
            
            // Security check: Customers can only check their own delivery readiness, admins can check for anyone
            if (!"ADMIN".equals(authenticatedUser.getRole()) && !"ROLE_ADMIN".equals(authenticatedUser.getRole())) {
                if (!userId.equals(authenticatedUser.getUserId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(java.util.Map.of("error", "You can only check your own delivery readiness"));
                }
            }
            
            boolean hasValidAddresses = orderServiceImpl.userHasValidAddressesForDelivery(userId);
            return ResponseEntity.ok(java.util.Map.of(
                "hasValidAddresses", hasValidAddresses,
                "canCreateDeliveryOrder", hasValidAddresses
            ));
        } catch (Exception e) {
            logger.error("Error checking delivery readiness for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("error", "Failed to check delivery readiness"));
        }
    }

    /**
     * Helper method to convert Map to OrderDto
     */
    @SuppressWarnings("unchecked")
    private OrderDto convertMapToOrderDto(java.util.Map<String, Object> orderData) {
        OrderDto orderDto = new OrderDto();
        
        if (orderData.get("userId") != null) {
            orderDto.setUserId(Long.valueOf(orderData.get("userId").toString()));
        }
        
        if (orderData.get("status") != null) {
            orderDto.setStatus((String) orderData.get("status"));
        }
        
        if (orderData.get("totalPrice") != null) {
            orderDto.setTotalPrice(Double.valueOf(orderData.get("totalPrice").toString()));
        }
        
        if (orderData.get("orderItems") != null) {
            java.util.List<java.util.Map<String, Object>> itemsData = 
                (java.util.List<java.util.Map<String, Object>>) orderData.get("orderItems");
            
            java.util.List<za.co.tt.domain.OrderItemDto> items = new java.util.ArrayList<>();
            
            for (java.util.Map<String, Object> itemData : itemsData) {
                za.co.tt.domain.OrderItemDto itemDto = new za.co.tt.domain.OrderItemDto();
                
                if (itemData.get("productId") != null) {
                    itemDto.setProductId(Long.valueOf(itemData.get("productId").toString()));
                }
                if (itemData.get("quantity") != null) {
                    itemDto.setQuantity(Integer.valueOf(itemData.get("quantity").toString()));
                }
                if (itemData.get("price") != null) {
                    itemDto.setPrice(new java.math.BigDecimal(itemData.get("price").toString()));
                }
                
                items.add(itemDto);
            }
            
            orderDto.setOrderItems(items);
        }
        
        return orderDto;
    }
}