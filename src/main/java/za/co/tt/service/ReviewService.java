package za.co.tt.service;

import za.co.tt.domain.Review;
import za.co.tt.domain.ReviewDto;
import za.co.tt.domain.User;
import za.co.tt.domain.Product;
import za.co.tt.domain.Order;
import za.co.tt.domain.OrderItem;
import za.co.tt.domain.Enum.OrderStatus;
import za.co.tt.repository.ReviewRepository;
import za.co.tt.repository.OrderRepository;
import za.co.tt.repository.IProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService implements IReviewService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final IProductRepository productRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, 
                        OrderRepository orderRepository,
                        IProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @Override
    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    @Override
    public Review createReview(Review review) {
        return reviewRepository.save(review);
    }
    
    public Review createReviewFromDto(ReviewDto reviewDto, User user) {
        // Validate that the order exists and belongs to the user
        Optional<Order> orderOpt = orderRepository.findById(reviewDto.getOrderId());
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order not found");
        }
        
        Order order = orderOpt.get();
        
        // Check if order belongs to the user
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("You can only review your own orders");
        }
        
        // Check if order is completed (delivered/collected)
        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("You can only review products from completed orders");
        }
        
        // Check if the product is in this order
        boolean productInOrder = order.getOrderItems().stream()
                .anyMatch(item -> item.getProduct().getProductId().equals(reviewDto.getProductId()));
        
        if (!productInOrder) {
            throw new IllegalArgumentException("This product is not in the specified order");
        }
        
        // Check if user has already reviewed this specific order-product combination
        if (hasUserReviewedOrderProduct(reviewDto.getOrderId(), reviewDto.getProductId())) {
            throw new IllegalArgumentException("You have already reviewed this product from this order");
        }
        
        // Get the product
        Optional<Product> productOpt = productRepository.findById(reviewDto.getProductId());
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found");
        }
        
        Product product = productOpt.get();
        
        // Validate rating
        if (reviewDto.getRating() < 1 || reviewDto.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        // Create the review
        Review review = new Review.Builder()
                .setProduct(product)
                .setUser(user)
                .setOrder(order)
                .setComment(reviewDto.getComment())
                .setRating(reviewDto.getRating())
                .setReviewerName(user.getName() + " " + user.getSurname())
                .setReviewDate(LocalDateTime.now())
                .build();
        
        return reviewRepository.save(review);
    }
    
    public boolean canUserReviewProduct(Long userId, Long productId) {
        // Check if user has any completed orders containing this product
        List<Order> completedOrders = orderRepository.findByUser_UserIdAndOrderStatus(userId, OrderStatus.COMPLETED);
        
        for (Order order : completedOrders) {
            for (OrderItem item : order.getOrderItems()) {
                if (item.getProduct().getProductId().equals(productId)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean hasUserReviewedProduct(Long userId, Long productId) {
        return reviewRepository.existsByUser_UserIdAndProduct_ProductId(userId, productId);
    }
    
    public boolean hasUserReviewedOrderProduct(Long orderId, Long productId) {
        return reviewRepository.existsByOrder_OrderIdAndProduct_ProductId(orderId, productId);
    }
    
    public List<Order> getReviewableOrdersForUser(Long userId) {
        // Get all completed orders for the user
        return orderRepository.findByUser_UserIdAndOrderStatus(userId, OrderStatus.COMPLETED);
    }
    
    public List<Product> getReviewableProductsForOrder(Long orderId) {
        // Get the order and return products that haven't been reviewed yet
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return List.of();
        }
        
        Order order = orderOpt.get();
        
        // Log any null products for debugging and detailed analysis
        List<OrderItem> nullProductItems = order.getOrderItems().stream()
                .filter(item -> item.getProduct() == null)
                .toList();
        
        if (!nullProductItems.isEmpty()) {
            logger.warn("Found {} order items with null products in order {}. OrderItem IDs: {}", 
                nullProductItems.size(), orderId, 
                nullProductItems.stream().map(OrderItem::getId).toList());
        }
        
        return order.getOrderItems().stream()
                .map(OrderItem::getProduct)
                .filter(product -> product != null) // Filter out null products
                .filter(product -> !hasUserReviewedOrderProduct(orderId, product.getProductId()))
                .distinct()
                .toList();
    }
    
    public List<Product> getReviewableProductsForUser(Long userId) {
        List<Order> completedOrders = orderRepository.findByUser_UserIdAndOrderStatus(userId, OrderStatus.COMPLETED);
        
        return completedOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(OrderItem::getProduct)
                .filter(product -> product != null) // Filter out null products
                .filter(product -> !hasUserReviewedProduct(userId, product.getProductId()))
                .distinct()
                .toList();
    }
    
    public List<Review> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUser_UserId(userId);
    }

    @Override
    public Review updateReview(Long id, Review review) {
        if (!reviewRepository.existsById(id)) {
            throw new RuntimeException("Review not found with id: " + id);
        }
        review.setReviewId(id);
        return reviewRepository.save(review);
    }

    @Override
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new RuntimeException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }

    @Override
    public List<Review> getReviewsByProductId(Long productId) {
    return reviewRepository.findByProduct_ProductId(productId);
    }

    @Override
    public List<Review> getReviewsByRating(int rating) {
        return reviewRepository.findByRating(rating);
    }

    @Override
    public Double getAverageRatingByProductId(Long productId) {
        return reviewRepository.findAverageRatingByProductId(productId);
    }
    
    /**
     * Diagnostic method to help identify order items with data integrity issues
     */
    public void diagnoseOrderDataIntegrity(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            logger.warn("Order {} not found", orderId);
            return;
        }
        
        Order order = orderOpt.get();
        logger.info("Diagnosing order {} with {} order items", orderId, order.getOrderItems().size());
        
        for (OrderItem item : order.getOrderItems()) {
            if (item.getProduct() == null) {
                logger.error("OrderItem {} has null product. OrderItem details: ID={}, Quantity={}, Price={}", 
                    item.getId(), item.getId(), item.getQuantity(), item.getPrice());
            } else {
                logger.debug("OrderItem {} has valid product: {}", item.getId(), item.getProduct().getProductId());
            }
        }
    }
}