package za.co.tt.repository;

import za.co.tt.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct_ProductId(Long productId);
    List<Review> findByRating(int rating);
    List<Review> findByUser_UserId(Long userId);
    
    // Check if user has already reviewed this specific order-product combination
    boolean existsByOrder_OrderIdAndProduct_ProductId(Long orderId, Long productId);
    
    // Check if user has reviewed this product in any order (for eligibility display)
    boolean existsByUser_UserIdAndProduct_ProductId(Long userId, Long productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.productId = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);
}