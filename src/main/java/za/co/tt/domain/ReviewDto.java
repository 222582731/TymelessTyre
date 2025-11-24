package za.co.tt.domain;

public class ReviewDto {
    private Long orderId;
    private Long productId;
    private String comment;
    private int rating;

    public ReviewDto() {}

    public ReviewDto(Long orderId, Long productId, String comment, int rating) {
        this.orderId = orderId;
        this.productId = productId;
        this.comment = comment;
        this.rating = rating;
    }

    // Getters and setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    @Override
    public String toString() {
        return "ReviewDto{" +
                "orderId=" + orderId +
                ", productId=" + productId +
                ", comment='" + comment + '\'' +
                ", rating=" + rating +
                '}';
    }
}