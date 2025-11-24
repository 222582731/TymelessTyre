package za.co.tt.domain;

public class ReviewEligibilityResponse {
    private boolean canReview;
    private boolean hasReviewed;

    public ReviewEligibilityResponse() {}

    public ReviewEligibilityResponse(boolean canReview, boolean hasReviewed) {
        this.canReview = canReview;
        this.hasReviewed = hasReviewed;
    }

    // Getters and setters
    public boolean isCanReview() { return canReview; }
    public void setCanReview(boolean canReview) { this.canReview = canReview; }

    public boolean isHasReviewed() { return hasReviewed; }
    public void setHasReviewed(boolean hasReviewed) { this.hasReviewed = hasReviewed; }

    @Override
    public String toString() {
        return "ReviewEligibilityResponse{" +
                "canReview=" + canReview +
                ", hasReviewed=" + hasReviewed +
                '}';
    }
}