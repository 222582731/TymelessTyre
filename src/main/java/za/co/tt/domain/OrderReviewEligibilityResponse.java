package za.co.tt.domain;

public class OrderReviewEligibilityResponse {
    private boolean canReview;
    private boolean hasReviewed;
    private boolean productInOrder;
    private String orderStatus;

    public OrderReviewEligibilityResponse() {}

    public OrderReviewEligibilityResponse(boolean canReview, boolean hasReviewed, boolean productInOrder, String orderStatus) {
        this.canReview = canReview;
        this.hasReviewed = hasReviewed;
        this.productInOrder = productInOrder;
        this.orderStatus = orderStatus;
    }

    // Getters and setters
    public boolean isCanReview() { return canReview; }
    public void setCanReview(boolean canReview) { this.canReview = canReview; }

    public boolean isHasReviewed() { return hasReviewed; }
    public void setHasReviewed(boolean hasReviewed) { this.hasReviewed = hasReviewed; }

    public boolean isProductInOrder() { return productInOrder; }
    public void setProductInOrder(boolean productInOrder) { this.productInOrder = productInOrder; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    @Override
    public String toString() {
        return "OrderReviewEligibilityResponse{" +
                "canReview=" + canReview +
                ", hasReviewed=" + hasReviewed +
                ", productInOrder=" + productInOrder +
                ", orderStatus='" + orderStatus + '\'' +
                '}';
    }
}