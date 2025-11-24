package za.co.tt.service;


import za.co.tt.domain.Order;
import za.co.tt.domain.User;
import za.co.tt.domain.OrderItem;
import za.co.tt.domain.OrderItemDto;
import za.co.tt.domain.Product;
import za.co.tt.domain.Payment;
import za.co.tt.domain.Delivery;
import za.co.tt.domain.Enum.PaymentMethod;
import za.co.tt.domain.Enum.DeliveryMethod;
import za.co.tt.domain.Enum.OrderStatus;
import za.co.tt.repository.OrderRepository;
import za.co.tt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService implements IOrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final IProductService productService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private DeliveryService deliveryService;
    
    @Autowired
    private AddressService addressService;

    @Autowired
    public OrderService(OrderRepository orderRepository, UserRepository userRepository, IProductService productService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productService = productService;
    }

    /**
     * Debug method to test basic JPA functionality
     */
    public List<Order> getAllOrdersBasic() {
        logger.info("Fetching all orders using basic findAll()");
        List<Order> orders = orderRepository.findAll();
        logger.info("Found {} orders using basic findAll()", orders.size());
        return orders;
    }

    @Override
    public List<Order> getAllOrders() {
        logger.info("Fetching all orders from database");
        List<Order> orders = orderRepository.findAllWithItems();
        logger.info("Found {} orders in database", orders.size());
        return orders;
    }

    @Override
    public Optional<Order> getOrderById(Long id) {
        logger.info("Fetching order with ID: {}", id);
        Optional<Order> order = orderRepository.findByIdWithItems(id);
        logger.info("Order found: {}", order.isPresent());
        return order;
    }

    @Override
    @Transactional
    public Order createOrder(za.co.tt.domain.OrderDto orderDto) {
        logger.info("Creating order for userId: {}", orderDto.getUserId());
        
        // Parse status from DTO, default to PENDING if invalid or null
        OrderStatus orderStatus = OrderStatus.PENDING;
        if (orderDto.getStatus() != null) {
            try {
                orderStatus = OrderStatus.valueOf(orderDto.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid order status '{}', defaulting to PENDING", orderDto.getStatus());
                orderStatus = OrderStatus.PENDING;
            }
        }

    // Validate userId
    if (orderDto.getUserId() == null) {
        throw new IllegalArgumentException("UserId must not be null");
    }
    // Fetch managed User entity
    User managedUser = userRepository.findById(orderDto.getUserId())
        .orElseThrow(() -> new RuntimeException("User not found for id: " + orderDto.getUserId()));

        // Validate order items
        List<OrderItemDto> itemDtos = orderDto.getOrderItems();
        logger.debug("Validating {} order items", itemDtos != null ? itemDtos.size() : 0);
        
        if (itemDtos == null || itemDtos.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        
        // Convert DTOs to entities and validate each order item
        List<OrderItem> items = new ArrayList<>();
        for (int i = 0; i < itemDtos.size(); i++) {
            OrderItemDto itemDto = itemDtos.get(i);
            if (itemDto == null) {
                throw new IllegalArgumentException("OrderItem at index " + i + " is null");
            }
            
            Long productId = itemDto.getProductId();
            if (productId == null) {
                throw new IllegalArgumentException("OrderItem at index " + i + " is missing productId. Please ensure each item has a valid productId.");
            }
            if (itemDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("OrderItem at index " + i + " must have a positive quantity");
            }
            if (itemDto.getPrice() == null || itemDto.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("OrderItem at index " + i + " must have a valid positive price");
            }
            
            // Check if product exists and has sufficient stock
            Product product = productService.getProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product with ID " + productId + " not found"));
            
            if (product.getStockQuantity() < itemDto.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product '" + product.getProductName() + "'. " +
                    "Available: " + product.getStockQuantity() + ", Requested: " + itemDto.getQuantity());
            }
            
            // Create OrderItem entity from DTO
            OrderItem orderItem = new OrderItem(product, itemDto.getQuantity(), itemDto.getPrice());
            items.add(orderItem);
            
            logger.debug("Stock check passed for product {}: available={}, requested={}", 
                product.getProductName(), product.getStockQuantity(), itemDto.getQuantity());
        }

        Order order = new Order();
        order.setUser(managedUser);
        order.setOrderStatus(orderStatus);
        order.setOrderDate(java.time.LocalDateTime.now());
        
        // Set the order reference on each item and calculate total
        java.math.BigDecimal calculatedTotal = java.math.BigDecimal.ZERO;
        for (OrderItem item : items) {
            item.setOrder(order);
            // Calculate subtotal for each item
            java.math.BigDecimal itemSubtotal = item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity()));
            item.setSubtotal(itemSubtotal);
            calculatedTotal = calculatedTotal.add(itemSubtotal);
        }
        order.setOrderItems(items);
        
        // Use calculated total or provided total (whichever is present)
        Double providedTotal = orderDto.getTotalPrice();
        if (providedTotal != null && providedTotal > 0) {
            order.setTotalAmount(providedTotal);
        } else {
            order.setTotalAmount(calculatedTotal);
        }

        // Set order reference on items
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                item.setOrder(order);
            }
        }

        // Save the order first to get the ID
        Order savedOrder = orderRepository.save(order);
        
        // Reduce stock quantities for each ordered item
        for (OrderItem item : savedOrder.getOrderItems()) {
            Long productId = item.getProductId();
            Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found during stock reduction: " + productId));
            
            int newStockQuantity = product.getStockQuantity() - item.getQuantity();
            productService.updateStockQuantity(product.getProductId(), newStockQuantity);
            
            logger.info("Reduced stock for product {}: {} -> {} (ordered: {})", 
                product.getProductName(), product.getStockQuantity(), newStockQuantity, item.getQuantity());
        }
        
        return savedOrder;
    }

    @Override
    @Transactional
    public Order updateOrder(Long id, Order updatedOrder) {
        Order existingOrder = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        // If the order status is being changed to "cancelled", restore stock
        if (updatedOrder.getOrderStatus() == OrderStatus.CANCELLED && 
            existingOrder.getOrderStatus() != OrderStatus.CANCELLED) {
            restoreStockQuantities(existingOrder);
            logger.info("Order {} cancelled, stock quantities restored", id);
        }
        
        // Update the order fields (excluding items for now to keep it simple)
        existingOrder.setOrderStatus(updatedOrder.getOrderStatus());
        if (updatedOrder.getTotalAmount() != null) {
            existingOrder.setTotalAmount(updatedOrder.getTotalAmount());
        }
        
        return orderRepository.save(existingOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        // Get the order to restore stock quantities
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        // Restore stock quantities before deleting the order
        restoreStockQuantities(order);
        
        orderRepository.deleteById(id);
        logger.info("Deleted order {} and restored stock quantities", id);
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        logger.info("Fetching orders for user ID: {}", userId);
        List<Order> orders = orderRepository.findByUser_UserId(userId);
        logger.info("Found {} orders for user {}", orders.size(), userId);
        return orders;
    }

    @Override
    public List<Order> getOrdersByStatus(String status) {
        logger.info("Fetching orders with status: {}", status);
        
        // Try to parse as OrderStatus enum first
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            List<Order> orders = orderRepository.findByOrderStatus(orderStatus);
            logger.info("Found {} orders with status {}", orders.size(), status);
            return orders;
        } catch (IllegalArgumentException e) {
            // Fallback to string-based search for backward compatibility
            logger.warn("Invalid OrderStatus '{}', falling back to string search", status);
            List<Order> orders = orderRepository.findByOrderStatus(status);
            logger.info("Found {} orders with status {} (string search)", orders.size(), status);
            return orders;
        }
    }
    
    public List<Order> getOrdersByStatus(OrderStatus status) {
        logger.info("Fetching orders with status: {}", status);
        List<Order> orders = orderRepository.findByOrderStatus(status);
        logger.info("Found {} orders with status {}", orders.size(), status);
        return orders;
    }

    @Override
    public Order save(Order entity) {
        return null;
    }

    @Override
    public Order update(Order entity) {
        return null;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (!orderRepository.existsById(id)) {
            throw new IllegalArgumentException("Entity with ID " + id + " not found");
        }
        orderRepository.deleteById(id);
    } //made some changes to avoid error


    @Override
    public Order read(Long aLong) {
        return null;
    }

    @Override
    public List<Order> findAll() {
        return List.of();
    }

    /**
     * Restores stock quantities when an order is cancelled or deleted
     * @param order The order whose stock quantities should be restored
     */
    private void restoreStockQuantities(Order order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return;
        }
        
        for (OrderItem item : order.getOrderItems()) {
            Long productId = item.getProductId();
            Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found during stock restoration: " + productId));
            
            int restoredStockQuantity = product.getStockQuantity() + item.getQuantity();
            productService.updateStockQuantity(product.getProductId(), restoredStockQuantity);
            
            logger.info("Restored stock for product {}: {} -> {} (restored: {})", 
                product.getProductName(), product.getStockQuantity(), restoredStockQuantity, item.getQuantity());
        }
    }

    /**
     * Checks if an order can be fulfilled based on current stock levels (private helper)
     * @param orderItems The list of items to check
     * @return true if all items can be fulfilled, false otherwise
     */
    private boolean canFulfillOrder(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return false;
        }
        
        for (OrderItem item : orderItems) {
            Long productId = item.getProductId();
            Product product = productService.getProductById(productId)
                .orElse(null);
            
            if (product == null || product.getStockQuantity() < item.getQuantity()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create complete order with payment and delivery
     */
    @Transactional
    public Order createOrderWithPaymentAndDelivery(za.co.tt.domain.OrderDto orderDto, 
                                                  PaymentMethod paymentMethod, 
                                                  DeliveryMethod deliveryMethod, 
                                                  Long addressId) {
        logger.info("Creating order with payment and delivery for userId: {}", orderDto.getUserId());
        
        // First create the basic order
        Order order = createOrder(orderDto);
        
        // Additional safety check: ensure this order doesn't already have payment/delivery
        if (order.getPayment() != null) {
            logger.warn("Order {} already has a payment, skipping payment creation", order.getOrderId());
        }
        if (order.getDelivery() != null) {
            logger.warn("Order {} already has a delivery, skipping delivery creation", order.getOrderId());
        }
        
        try {
            // Create payment based on delivery method (only if not already exists)
            Payment payment = order.getPayment();
            if (payment == null) {
                if (deliveryMethod == DeliveryMethod.COLLECTION) {
                    payment = paymentService.createCashOnCollectionPayment(order.getOrderId(), order.getUser().getUserId());
                } else {
                    payment = paymentService.createCashOnDeliveryPayment(order.getOrderId(), order.getUser().getUserId());
                }
                logger.info("Created payment {} for order {}", payment.getPaymentId(), order.getOrderId());
            }
            
            // Create delivery (only if not already exists)
            Delivery delivery = order.getDelivery();
            if (delivery == null) {
                // Double-check if delivery exists in database
                Optional<Delivery> existingDelivery = deliveryService.findByOrderId(order.getOrderId());
                if (existingDelivery.isPresent()) {
                    delivery = existingDelivery.get();
                    logger.info("Found existing delivery {} for order {}", delivery.getDeliveryId(), order.getOrderId());
                } else {
                    delivery = deliveryService.createDeliveryForOrder(
                        order.getOrderId(), 
                        order.getUser().getUserId(), 
                        deliveryMethod, 
                        addressId
                    );
                    logger.info("Created delivery {} for order {}", delivery.getDeliveryId(), order.getOrderId());
                }
            }
            
            // Update the order with payment and delivery references
            order.setPayment(payment);
            order.setDelivery(delivery);
            
            Order finalOrder = orderRepository.save(order);
            
            logger.info("Successfully created order {} with payment {} and delivery {}", 
                finalOrder.getOrderId(), payment.getPaymentId(), delivery.getDeliveryId());
            
            return finalOrder;
            
        } catch (Exception e) {
            logger.error("Failed to create payment/delivery for order {}: {}", order.getOrderId(), e.getMessage());
            // If payment/delivery creation fails, we could choose to:
            // 1. Let the order exist without payment/delivery
            // 2. Delete the order and throw exception
            // For now, let's throw an exception to rollback the transaction
            throw new RuntimeException("Failed to create payment and delivery: " + e.getMessage(), e);
        }
    }

    /**
     * Get order with all related information (payment, delivery, items)
     */
    public Optional<Order> getCompleteOrderById(Long orderId) {
        logger.info("Fetching complete order information for ID: {}", orderId);
        Optional<Order> orderOpt = orderRepository.findByIdWithItems(orderId);
        
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            
            // Payment and delivery should be loaded automatically due to relationships
            // but we can add explicit logging
            if (order.getPayment() != null) {
                logger.debug("Order {} has payment: {}", orderId, order.getPayment().getPaymentId());
            }
            if (order.getDelivery() != null) {
                logger.debug("Order {} has delivery: {}", orderId, order.getDelivery().getDeliveryId());
            }
        }
        
        return orderOpt;
    }

    /**
     * Check if user has valid addresses for delivery
     */
    public boolean userHasValidAddressesForDelivery(Long userId) {
        return addressService.userHasAddresses(userId);
    }

    /**
     * Verify that an address belongs to a specific user
     */
    public boolean verifyAddressOwnership(Long addressId, Long userId) {
        Optional<za.co.tt.domain.Address> addressOpt = addressService.findById(addressId);
        if (addressOpt.isEmpty()) {
            logger.warn("Address verification failed: Address {} not found", addressId);
            return false;
        }
        
        za.co.tt.domain.Address address = addressOpt.get();
        boolean isOwner = address.getUser().getUserId().equals(userId);
        
        if (!isOwner) {
            logger.warn("Address verification failed: Address {} does not belong to user {}", addressId, userId);
        } else {
            logger.debug("Address verification successful: Address {} belongs to user {}", addressId, userId);
        }
        
        return isOwner;
    }

    /**
     * Check if order has existing payment or delivery records
     */
    public boolean hasExistingPaymentOrDelivery(Long orderId) {
        if (orderId == null) {
            return false;
        }
        
        try {
            // Check for existing payment
            boolean hasPayment = paymentService.findPaymentByOrderId(orderId).isPresent();
            
            // Check for existing delivery
            boolean hasDelivery = deliveryService.findByOrderId(orderId).isPresent();
            
            if (hasPayment || hasDelivery) {
                logger.info("Order {} already has payment: {}, delivery: {}", orderId, hasPayment, hasDelivery);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            logger.error("Error checking existing payment/delivery for order {}: {}", orderId, e.getMessage());
            return false;
        }
    }

    /**
     * Validate order creation request with payment and delivery info
     */
    public void validateOrderCreationRequest(za.co.tt.domain.OrderDto orderDto, 
                                           PaymentMethod paymentMethod, 
                                           DeliveryMethod deliveryMethod, 
                                           Long addressId) {
        // Basic order validation (already done in createOrder)
        
        // Payment method validation
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
        
        // Delivery method validation
        if (deliveryMethod == null) {
            throw new IllegalArgumentException("Delivery method is required");
        }
        
        // Address validation for delivery
        if (deliveryMethod == DeliveryMethod.DELIVERY) {
            if (addressId == null) {
                throw new IllegalArgumentException("Address is required for delivery method");
            }
            
            // Verify address exists and belongs to user
            Optional<za.co.tt.domain.Address> addressOpt = addressService.findById(addressId);
            if (addressOpt.isEmpty()) {
                throw new IllegalArgumentException("Address not found with ID: " + addressId);
            }
            
            za.co.tt.domain.Address address = addressOpt.get();
            if (!address.getUser().getUserId().equals(orderDto.getUserId())) {
                throw new IllegalArgumentException("Address does not belong to user");
            }
        }
        
        logger.info("Order creation request validation passed for userId: {}", orderDto.getUserId());
    }
}