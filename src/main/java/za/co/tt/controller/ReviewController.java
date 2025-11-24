
package za.co.tt.controller;

import za.co.tt.domain.Review;
import za.co.tt.domain.ReviewDto;
import za.co.tt.domain.User;
import za.co.tt.domain.Product;
import za.co.tt.domain.Order;
import za.co.tt.domain.ReviewEligibilityResponse;
import za.co.tt.domain.OrderReviewEligibilityResponse;
import za.co.tt.domain.Enum.OrderStatus;
import za.co.tt.service.IReviewService;
import za.co.tt.service.ReviewService;
import za.co.tt.service.UserService;
import za.co.tt.service.IOrderService;
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
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:5173")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);
    
    private final IReviewService reviewService;
    private final ReviewService reviewServiceImpl;
    private final UserService userService;
    private final IOrderService orderService;

    @Autowired
    public ReviewController(IReviewService reviewService, 
                           ReviewService reviewServiceImpl,
                           UserService userService,
                           IOrderService orderService) {
        this.reviewService = reviewService;
        this.reviewServiceImpl = reviewServiceImpl;
        this.userService = userService;
        this.orderService = orderService;
    }

   
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        List<Review> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

   
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        Optional<Review> review = reviewService.getReviewById(id);
        return review.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

   
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> createReviewFromDto(@RequestBody ReviewDto reviewDto, HttpServletRequest request) {
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
            Review savedReview = reviewServiceImpl.createReviewFromDto(reviewDto, user);
            
            logger.info("User {} created review for product {}", username, reviewDto.getProductId());
            return ResponseEntity.ok(savedReview);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Review creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating review: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating review: " + e.getMessage());
        }
    }

    @Deprecated
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        Review savedReview = reviewService.createReview(review);
        return ResponseEntity.ok(savedReview);
    }
    
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @GetMapping("/can-review/{productId}")
    public ResponseEntity<?> canUserReviewProduct(@PathVariable Long productId, HttpServletRequest request) {
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
            boolean canReview = reviewServiceImpl.canUserReviewProduct(user.getUserId(), productId);
            boolean hasReviewed = reviewServiceImpl.hasUserReviewedProduct(user.getUserId(), productId);
            
            return ResponseEntity.ok(new ReviewEligibilityResponse(canReview, hasReviewed));
            
        } catch (Exception e) {
            logger.error("Error checking review eligibility: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error checking review eligibility: " + e.getMessage());
        }
    }
    
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @GetMapping("/can-review-order/{orderId}/product/{productId}")
    public ResponseEntity<?> canUserReviewOrderProduct(@PathVariable Long orderId, @PathVariable Long productId, HttpServletRequest request) {
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
            
            // Check if order belongs to user
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Order not found");
            }
            
            Order order = orderOpt.get();
            if (!order.getUser().getUserId().equals(user.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only review your own orders");
            }
            
            // Check if order is completed (delivered/collected)
            boolean canReview = order.getOrderStatus() == OrderStatus.COMPLETED;
            
            // Check if product is in this order
            boolean productInOrder = order.getOrderItems().stream()
                    .anyMatch(item -> item.getProduct().getProductId().equals(productId));
            
            // Check if already reviewed this order-product combination
            boolean hasReviewedOrder = reviewServiceImpl.hasUserReviewedOrderProduct(orderId, productId);
            
            OrderReviewEligibilityResponse response = new OrderReviewEligibilityResponse(
                canReview && productInOrder, 
                hasReviewedOrder,
                productInOrder,
                order.getOrderStatus().toString()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error checking order review eligibility: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error checking order review eligibility: " + e.getMessage());
        }
    }
    
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @GetMapping("/reviewable-orders")
    public ResponseEntity<?> getReviewableOrders(HttpServletRequest request) {
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
            List<Order> reviewableOrders = reviewServiceImpl.getReviewableOrdersForUser(user.getUserId());
            
            return ResponseEntity.ok(reviewableOrders);
            
        } catch (Exception e) {
            logger.error("Error getting reviewable orders: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error getting reviewable orders: " + e.getMessage());
        }
    }
    
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @GetMapping("/reviewable-products/order/{orderId}")
    public ResponseEntity<?> getReviewableProductsForOrder(@PathVariable Long orderId, HttpServletRequest request) {
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
            
            // Verify order belongs to user
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Order not found");
            }
            
            Order order = orderOpt.get();
            if (!order.getUser().getUserId().equals(user.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only view your own orders");
            }
            
            List<Product> reviewableProducts = reviewServiceImpl.getReviewableProductsForOrder(orderId);
            
            return ResponseEntity.ok(reviewableProducts);
            
        } catch (Exception e) {
            logger.error("Error getting reviewable products for order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error getting reviewable products for order: " + e.getMessage());
        }
    }
    
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @GetMapping("/reviewable-products")
    public ResponseEntity<?> getReviewableProducts(HttpServletRequest request) {
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
            List<Product> reviewableProducts = reviewServiceImpl.getReviewableProductsForUser(user.getUserId());
            
            return ResponseEntity.ok(reviewableProducts);
            
        } catch (Exception e) {
            logger.error("Error getting reviewable products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error getting reviewable products: " + e.getMessage());
        }
    }
    
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @GetMapping("/my-reviews")
    public ResponseEntity<?> getMyReviews(HttpServletRequest request) {
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
            List<Review> userReviews = reviewServiceImpl.getReviewsByUserId(user.getUserId());
            
            return ResponseEntity.ok(userReviews);
            
        } catch (Exception e) {
            logger.error("Error getting user reviews: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error getting user reviews: " + e.getMessage());
        }
    }

   
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable Long id, @RequestBody Review review) {
        try {
            Review updatedReview = reviewService.updateReview(id, review);
            return ResponseEntity.ok(updatedReview);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

   
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getReviewsByProductId(@PathVariable Long productId) {
        List<Review> reviews = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/rating/{rating}")
    public ResponseEntity<List<Review>> getReviewsByRating(@PathVariable int rating) {
        List<Review> reviews = reviewService.getReviewsByRating(rating);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/product/{productId}/average-rating")
    public ResponseEntity<Double> getAverageRatingByProductId(@PathVariable Long productId) {
        Double averageRating = reviewService.getAverageRatingByProductId(productId);
        return ResponseEntity.ok(averageRating);
    }
    
    /**
     * Diagnostic endpoint to help troubleshoot order data integrity issues
     * Should be removed or secured in production
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/debug/order/{orderId}/integrity")
    public ResponseEntity<String> diagnoseOrderIntegrity(@PathVariable Long orderId) {
        try {
            reviewServiceImpl.diagnoseOrderDataIntegrity(orderId);
            return ResponseEntity.ok("Diagnostic information logged for order " + orderId);
        } catch (Exception e) {
            logger.error("Error diagnosing order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error diagnosing order: " + e.getMessage());
        }
    }
}