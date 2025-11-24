package za.co.tt.domain.Enum;

public enum OrderStatus {
    PENDING,     // Order created, waiting for payment
    CONFIRMED,   // Payment received, order is being processed
    PROCESSING,  // Order being prepared/packed
    SHIPPED,     // Order has been shipped/ready for collection
    COMPLETED,   // Order delivered/collected successfully (enables reviews)
    CANCELLED    // Order was cancelled
}