package za.co.tt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.co.tt.domain.Payment;
import za.co.tt.domain.PaymentDto;
import za.co.tt.domain.Enum.PaymentMethod;
import za.co.tt.domain.Enum.PaymentStatus;
import za.co.tt.service.PaymentService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<PaymentDto>> getAllPayments() {
        try {
            List<Payment> payments = paymentService.findAll();
            List<PaymentDto> paymentDtos = payments.stream()
                    .map(paymentService::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(paymentDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable Long id) {
        try {
            Payment payment = paymentService.read(id);
            if (payment == null) {
                return ResponseEntity.notFound().build();
            }
            PaymentDto paymentDto = paymentService.convertToDto(payment);
            return ResponseEntity.ok(paymentDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentDto>> getPaymentsByUserId(@PathVariable Long userId) {
        try {
            List<Payment> payments = paymentService.findPaymentsByUserId(userId);
            List<PaymentDto> paymentDtos = payments.stream()
                    .map(paymentService::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(paymentDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDto> getPaymentByOrderId(@PathVariable Long orderId) {
        try {
            Optional<Payment> paymentOpt = paymentService.findPaymentByOrderId(orderId);
            if (paymentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            PaymentDto paymentDto = paymentService.convertToDto(paymentOpt.get());
            return ResponseEntity.ok(paymentDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentDto>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        try {
            List<Payment> payments = paymentService.findPaymentsByStatus(status);
            List<PaymentDto> paymentDtos = payments.stream()
                    .map(paymentService::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(paymentDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/cash-on-delivery")
    public ResponseEntity<?> createCashOnDeliveryPayment(@RequestBody Map<String, Object> request) {
        try {
            Long orderId = Long.valueOf(request.get("orderId").toString());
            Long userId = Long.valueOf(request.get("userId").toString());

            Payment payment = paymentService.createCashOnDeliveryPayment(orderId, userId);
            PaymentDto paymentDto = paymentService.convertToDto(payment);
            return ResponseEntity.status(HttpStatus.CREATED).body(paymentDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create Cash on Delivery payment"));
        }
    }

    @PostMapping("/cash-on-collection")
    public ResponseEntity<?> createCashOnCollectionPayment(@RequestBody Map<String, Object> request) {
        try {
            Long orderId = Long.valueOf(request.get("orderId").toString());
            Long userId = Long.valueOf(request.get("userId").toString());

            Payment payment = paymentService.createCashOnCollectionPayment(orderId, userId);
            PaymentDto paymentDto = paymentService.convertToDto(payment);
            return ResponseEntity.status(HttpStatus.CREATED).body(paymentDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create Cash on Collection payment"));
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String statusStr = request.get("status");
            PaymentStatus status = PaymentStatus.valueOf(statusStr.toUpperCase());
            
            Payment updatedPayment = paymentService.updatePaymentStatus(id, status);
            PaymentDto paymentDto = paymentService.convertToDto(updatedPayment);
            return ResponseEntity.ok(paymentDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update payment status"));
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/methods")
    public ResponseEntity<PaymentMethod[]> getPaymentMethods() {
        return ResponseEntity.ok(paymentService.getAvailablePaymentMethods());
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statuses")
    public ResponseEntity<PaymentStatus[]> getPaymentStatuses() {
        return ResponseEntity.ok(PaymentStatus.values());
    }
}