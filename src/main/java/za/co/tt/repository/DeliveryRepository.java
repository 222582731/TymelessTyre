package za.co.tt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.co.tt.domain.Delivery;
import za.co.tt.domain.Enum.DeliveryStatus;
import za.co.tt.domain.Enum.DeliveryMethod;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrder_OrderId(Long orderId);
    List<Delivery> findAllByOrder_OrderId(Long orderId); // For finding potential duplicates
    List<Delivery> findByDeliveryStatus(DeliveryStatus status);
    List<Delivery> findByDeliveryMethod(DeliveryMethod method);
    List<Delivery> findByCourierName(String courierName);
    
    @Query("SELECT d FROM Delivery d WHERE d.deliveryAddress.user.userId = :userId")
    List<Delivery> findByDeliveryAddressUserId(@Param("userId") Long userId);
    
    @Query("SELECT d FROM Delivery d WHERE d.deliveryStatus = :status AND d.deliveryMethod = :method")
    List<Delivery> findByStatusAndMethod(@Param("status") DeliveryStatus status, @Param("method") DeliveryMethod method);
}
