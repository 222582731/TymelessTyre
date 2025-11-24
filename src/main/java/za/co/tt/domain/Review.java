package za.co.tt.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name ="reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    private String reviewerName;
    private String comment;
    private int rating;
    
    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    public Review() {}

    public Review(Builder builder) {
        this.reviewId = builder.reviewId;
        this.reviewerName = builder.reviewerName;
        this.comment = builder.comment;
        this.rating = builder.rating;
        this.product = builder.product;
        this.user = builder.user;
        this.order = builder.order;
        this.reviewDate = builder.reviewDate;
    }

    // Getters and setters
    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    
    public LocalDateTime getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDateTime reviewDate) { this.reviewDate = reviewDate; }

    @Override
    public String toString() {
        return "Review{" +
                "reviewId='" + reviewId + '\'' +
                ", reviewerName='" + reviewerName + '\'' +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", reviewDate=" + reviewDate +
                ", product=" + (product != null ? product.getProductName(): "null") +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", order=" + (order != null ? order.getOrderId() : "null") +
                '}';
    }

    public static class Builder {
        private Long reviewId;
        private String reviewerName;
        private String comment;
        private int rating;
        private Product product;
        private User user;
        private Order order;
        private LocalDateTime reviewDate;

        public Builder setReviewId(Long reviewId) { this.reviewId = reviewId; return this; }
        public Builder setReviewerName(String reviewerName) { this.reviewerName = reviewerName; return this; }
        public Builder setComment(String comment) { this.comment = comment; return this; }
        public Builder setRating(int rating) { this.rating = rating; return this; }
        public Builder setProduct(Product product) { this.product = product; return this; }
        public Builder setUser(User user) { this.user = user; return this; }
        public Builder setOrder(Order order) { this.order = order; return this; }
        public Builder setReviewDate(LocalDateTime reviewDate) { this.reviewDate = reviewDate; return this; }

        public Builder copy(Review review) {
            this.reviewId = review.reviewId;
            this.reviewerName = review.reviewerName;
            this.comment = review.comment;
            this.rating = review.rating;
            this.product = review.product;
            this.user = review.user;
            this.order = review.order;
            this.reviewDate = review.reviewDate;
            return this;
        }

        public Review build() {
            return new Review(this);
        }
    }
}