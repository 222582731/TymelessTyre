package za.co.tt.factory;

import za.co.tt.domain.Delivery;
import za.co.tt.domain.Order;
import za.co.tt.domain.Address;
import za.co.tt.domain.Enum.DeliveryMethod;
import za.co.tt.domain.Enum.DeliveryStatus;
import za.co.tt.util.Helper;
import java.time.LocalDateTime;

public class DeliveryFactory {

    public static Delivery createDelivery(Order order, Address deliveryAddress, DeliveryMethod deliveryMethod,
                                        DeliveryStatus deliveryStatus, String courierName, 
                                        LocalDateTime estimatedDeliveryDate) {

        if (order == null || deliveryMethod == null || deliveryStatus == null || estimatedDeliveryDate == null) {
            return null;
        }

        // For collection, address is optional; for delivery, address is required
        if (deliveryMethod == DeliveryMethod.DELIVERY && deliveryAddress == null) {
            return null;
        }

        return new Delivery.Builder()
                .setOrder(order)
                .setDeliveryAddress(deliveryAddress)
                .setDeliveryMethod(deliveryMethod)
                .setDeliveryStatus(deliveryStatus)
                .setCourierName(courierName)
                .setEstimatedDeliveryDate(estimatedDeliveryDate)
                .build();
    }

    // Convenience method for backward compatibility with String parameters
    public static Delivery createDelivery(Order order, Address deliveryAddress, String deliveryMethodStr,
                                        String deliveryStatusStr, String courierName, 
                                        LocalDateTime estimatedDeliveryDate) {

        if (Helper.isNullOrEmpty(deliveryMethodStr) || Helper.isNullOrEmpty(deliveryStatusStr)) {
            return null;
        }

        try {
            DeliveryMethod deliveryMethod = DeliveryMethod.valueOf(deliveryMethodStr.toUpperCase().replace(" ", "_"));
            DeliveryStatus deliveryStatus = DeliveryStatus.valueOf(deliveryStatusStr.toUpperCase().replace(" ", "_"));
            
            return createDelivery(order, deliveryAddress, deliveryMethod, deliveryStatus, courierName, estimatedDeliveryDate);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Legacy method for existing code compatibility (deprecated)
    @Deprecated
    public static Delivery createDelivery(Long addressId, String deliveryStatus,
                                          String courierName, LocalDateTime estimatedDeliveryDate) {
        // This method is deprecated and should not be used with the new entity structure
        // It's kept for backward compatibility but will return null to prevent errors
        return null;
    }
}
