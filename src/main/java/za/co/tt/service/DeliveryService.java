package za.co.tt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.co.tt.domain.Delivery;
import za.co.tt.domain.DeliveryDto;
import za.co.tt.domain.Order;
import za.co.tt.domain.Address;
import za.co.tt.domain.Enum.DeliveryMethod;
import za.co.tt.domain.Enum.DeliveryStatus;
import za.co.tt.repository.DeliveryRepository;
import za.co.tt.repository.OrderRepository;
import za.co.tt.repository.AddressRepository;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Service
public class DeliveryService implements IDeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private AddressService addressService;

    @Autowired
    private OrderStatusService orderStatusService;

    @Override
    public Delivery save(Delivery delivery) {
        return deliveryRepository.save(delivery);
    }

    @Override
    public Delivery update(Delivery delivery) {
        if (deliveryRepository.existsById(delivery.getDeliveryId())) {
            return deliveryRepository.save(delivery);
        }
        return null;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        if (!deliveryRepository.existsById(id)) {
            throw new IllegalArgumentException("Entity with ID " + id + " not found");
        }

        deliveryRepository.deleteById(id);
    } //made some changes to avoid having errorseturn null;

    @Override
    public Delivery read(Long id) {
        return deliveryRepository.findById(id).orElse(null);
    }

    @Override
    public List<Delivery> findAll() {
        return deliveryRepository.findAll();
    }

    /**
     * Find delivery by order ID
     */
    public Optional<Delivery> findByOrderId(Long orderId) {
        return deliveryRepository.findByOrder_OrderId(orderId);
    }

    /**
     * Find deliveries by status
     */
    public List<Delivery> findByStatus(DeliveryStatus status) {
        return deliveryRepository.findByDeliveryStatus(status);
    }

    /**
     * Find deliveries by method
     */
    public List<Delivery> findByMethod(DeliveryMethod method) {
        return deliveryRepository.findByDeliveryMethod(method);
    }

    /**
     * Create delivery for order with delivery method
     */
    public Delivery createDeliveryForOrder(Long orderId, Long userId, DeliveryMethod deliveryMethod, Long addressId) {
        // Validate order exists and belongs to user
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order not found with ID: " + orderId);
        }
        
        Order order = orderOpt.get();
        if (!order.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to user");
        }

        // Check if delivery already exists for this order
        Optional<Delivery> existingDelivery = deliveryRepository.findByOrder_OrderId(orderId);
        if (existingDelivery.isPresent()) {
            String errorMsg = String.format("Delivery already exists for order: %d (existing delivery ID: %d)", 
                orderId, existingDelivery.get().getDeliveryId());
            throw new IllegalArgumentException(errorMsg);
        }

        Address deliveryAddress = null;
        
        // If delivery method is DELIVERY, validate and get address
        if (deliveryMethod == DeliveryMethod.DELIVERY) {
            if (addressId == null) {
                throw new IllegalArgumentException("Address ID is required for delivery method");
            }
            
            Optional<Address> addressOpt = addressRepository.findById(addressId);
            if (addressOpt.isEmpty()) {
                throw new IllegalArgumentException("Address not found with ID: " + addressId);
            }
            
            deliveryAddress = addressOpt.get();
            if (!deliveryAddress.getUser().getUserId().equals(userId)) {
                throw new IllegalArgumentException("Address does not belong to user");
            }
        }

        // Create delivery
        Delivery.Builder deliveryBuilder = new Delivery.Builder()
                .setOrder(order)
                .setDeliveryMethod(deliveryMethod)
                .setDeliveryStatus(DeliveryStatus.PENDING);

        // Set estimated date based on delivery method
        if (deliveryMethod == DeliveryMethod.DELIVERY) {
            // For delivery: set address, courier, and estimated delivery date
            deliveryBuilder.setDeliveryAddress(deliveryAddress);
            LocalDateTime estimatedDeliveryDate = calculateWorkingDaysFromDate(order.getOrderDate(), 3);
            deliveryBuilder.setEstimatedDeliveryDate(estimatedDeliveryDate);
            String courierName = assignCourier();
            deliveryBuilder.setCourierName(courierName);
        } else if (deliveryMethod == DeliveryMethod.COLLECTION) {
            // For collection: set estimated ready date (usually 1-2 working days)
            LocalDateTime estimatedReadyDate = calculateWorkingDaysFromDate(order.getOrderDate(), 2);
            deliveryBuilder.setEstimatedDeliveryDate(estimatedReadyDate);
            deliveryBuilder.setCourierName("Self Collection");
        }

        Delivery delivery = deliveryBuilder.build();
        return deliveryRepository.save(delivery);
    }

    /**
     * Update delivery status
     */
    public Delivery updateDeliveryStatus(Long deliveryId, DeliveryStatus newStatus) {
        Optional<Delivery> deliveryOpt = deliveryRepository.findById(deliveryId);
        if (deliveryOpt.isEmpty()) {
            throw new IllegalArgumentException("Delivery not found with ID: " + deliveryId);
        }

        Delivery delivery = deliveryOpt.get();
        
        // If setting to delivered or collected, set actual date
        LocalDateTime actualDate = null;
        if (newStatus == DeliveryStatus.DELIVERED || newStatus == DeliveryStatus.COLLECTED) {
            actualDate = LocalDateTime.now();
        }

        Delivery updatedDelivery = new Delivery.Builder()
                .copy(delivery)
                .setDeliveryStatus(newStatus)
                .setActualDeliveryDate(actualDate)
                .build();

        Delivery savedDelivery = deliveryRepository.save(updatedDelivery);
        
        // Update order status if delivery is completed
        if (newStatus == DeliveryStatus.DELIVERED || newStatus == DeliveryStatus.COLLECTED) {
            Long orderId = savedDelivery.getOrder().getOrderId();
            orderStatusService.updateOrderStatusFromDelivery(orderId, newStatus);
        }

        return savedDelivery;
    }

    /**
     * Update courier information
     */
    public Delivery updateCourierInfo(Long deliveryId, String courierName) {
        Optional<Delivery> deliveryOpt = deliveryRepository.findById(deliveryId);
        if (deliveryOpt.isEmpty()) {
            throw new IllegalArgumentException("Delivery not found with ID: " + deliveryId);
        }

        Delivery delivery = deliveryOpt.get();
        Delivery updatedDelivery = new Delivery.Builder()
                .copy(delivery)
                .setCourierName(courierName)
                .build();

        return deliveryRepository.save(updatedDelivery);
    }

    /**
     * Convert Delivery to DeliveryDto
     */
    public DeliveryDto convertToDto(Delivery delivery) {
        if (delivery == null) {
            return null;
        }

        DeliveryDto dto = new DeliveryDto(
                delivery.getDeliveryId(),
                delivery.getOrder() != null ? delivery.getOrder().getOrderId() : null,
                delivery.getDeliveryAddress() != null ? delivery.getDeliveryAddress().getAddressId() : null,
                delivery.getDeliveryMethod(),
                delivery.getDeliveryStatus(),
                delivery.getCourierName(),
                delivery.getEstimatedDeliveryDate(),
                delivery.getActualDeliveryDate()
        );

        // Include address details if available
        if (delivery.getDeliveryAddress() != null) {
            dto.setDeliveryAddress(addressService.convertToDto(delivery.getDeliveryAddress()));
        }

        return dto;
    }

    /**
     * Get available delivery methods
     */
    public DeliveryMethod[] getAvailableDeliveryMethods() {
        return DeliveryMethod.values();
    }

    /**
     * Get available delivery statuses
     */
    public DeliveryStatus[] getAvailableDeliveryStatuses() {
        return DeliveryStatus.values();
    }

    /**
     * Check if delivery is ready for collection
     */
    public boolean isReadyForCollection(Long deliveryId) {
        Optional<Delivery> deliveryOpt = deliveryRepository.findById(deliveryId);
        if (deliveryOpt.isEmpty()) {
            return false;
        }

        Delivery delivery = deliveryOpt.get();
        return delivery.getDeliveryMethod() == DeliveryMethod.COLLECTION &&
               delivery.getDeliveryStatus() == DeliveryStatus.READY_FOR_COLLECTION;
    }

    /**
     * Check if delivery is completed
     */
    public boolean isDeliveryCompleted(Long deliveryId) {
        Optional<Delivery> deliveryOpt = deliveryRepository.findById(deliveryId);
        if (deliveryOpt.isEmpty()) {
            return false;
        }

        Delivery delivery = deliveryOpt.get();
        return delivery.getDeliveryStatus() == DeliveryStatus.DELIVERED ||
               delivery.getDeliveryStatus() == DeliveryStatus.COLLECTED;
    }

    /**
     * Assign a courier for delivery
     * @return A randomly selected courier name
     */
    private String assignCourier() {
        String[] couriers = {
            "FastTrack Express",
            "TyreDirect Courier",
            "SpeedyWheels Delivery",
            "RoadRunner Express",
            "Swift Logistics",
            "TurboTyre Delivery",
            "QuickShip Couriers"
        };
        
        // Simple random selection based on current time
        int index = (int) (System.currentTimeMillis() % couriers.length);
        return couriers[index];
    }

    /**
     * Calculate a date that is a specified number of working days from a given date
     * Working days exclude weekends (Saturday and Sunday)
     * @param startDate The starting date
     * @param workingDays Number of working days to add
     * @return The calculated date
     */
    private LocalDateTime calculateWorkingDaysFromDate(LocalDateTime startDate, int workingDays) {
        if (startDate == null) {
            startDate = LocalDateTime.now();
        }
        
        LocalDateTime currentDate = startDate;
        int daysAdded = 0;
        
        while (daysAdded < workingDays) {
            currentDate = currentDate.plusDays(1);
            
            // Check if the current day is a working day (Monday to Friday)
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                daysAdded++;
            }
        }
        
        return currentDate;
    }
}



