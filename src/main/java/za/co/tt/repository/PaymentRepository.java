package za.co.tt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.co.tt.domain.Payment;
import za.co.tt.domain.Enum.PaymentStatus;
import za.co.tt.domain.Enum.PaymentMethod;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUser_UserId(Long userId);
    Optional<Payment> findByOrder_OrderId(Long orderId);
    List<Payment> findAllByOrder_OrderId(Long orderId); // For finding potential duplicates
    List<Payment> findByPaymentStatus(PaymentStatus status);
    List<Payment> findByPaymentMethod(PaymentMethod method);
    
    @Query("SELECT p FROM Payment p WHERE p.user.userId = :userId AND p.paymentStatus = :status")
    List<Payment> findByUserIdAndPaymentStatus(@Param("userId") Long userId, @Param("status") PaymentStatus status);
}
