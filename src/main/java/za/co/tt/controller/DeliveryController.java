package za.co.tt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.co.tt.domain.Delivery;
import za.co.tt.domain.DeliveryDto;
import za.co.tt.domain.Enum.DeliveryMethod;
import za.co.tt.domain.Enum.DeliveryStatus;
import za.co.tt.service.DeliveryService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @Autowired
    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping
    public ResponseEntity<List<DeliveryDto>> getAllDeliveries() {
        try {
            List<Delivery> deliveries = deliveryService.findAll();
            List<DeliveryDto> deliveryDtos = deliveries.stream()
                    .map(deliveryService::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(deliveryDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryDto> getDeliveryById(@PathVariable Long id) {
        try {
            Delivery delivery = deliveryService.read(id);
            if (delivery == null) {
                return ResponseEntity.notFound().build();
            }
            DeliveryDto deliveryDto = deliveryService.convertToDto(delivery);
            return ResponseEntity.ok(deliveryDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryDto> getDeliveryByOrderId(@PathVariable Long orderId) {
        try {
            Optional<Delivery> deliveryOpt = deliveryService.findByOrderId(orderId);
            if (deliveryOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            DeliveryDto deliveryDto = deliveryService.convertToDto(deliveryOpt.get());
            return ResponseEntity.ok(deliveryDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeliveryDto>> getDeliveriesByStatus(@PathVariable DeliveryStatus status) {
        try {
            List<Delivery> deliveries = deliveryService.findByStatus(status);
            List<DeliveryDto> deliveryDtos = deliveries.stream()
                    .map(deliveryService::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(deliveryDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/method/{method}")
    public ResponseEntity<List<DeliveryDto>> getDeliveriesByMethod(@PathVariable DeliveryMethod method) {
        try {
            List<Delivery> deliveries = deliveryService.findByMethod(method);
            List<DeliveryDto> deliveryDtos = deliveries.stream()
                    .map(deliveryService::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(deliveryDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createDelivery(@RequestBody Map<String, Object> request) {
        try {
            Long orderId = Long.valueOf(request.get("orderId").toString());
            Long userId = Long.valueOf(request.get("userId").toString());
            DeliveryMethod deliveryMethod = DeliveryMethod.valueOf(request.get("deliveryMethod").toString().toUpperCase());
            
            Long addressId = null;
            if (request.get("addressId") != null) {
                addressId = Long.valueOf(request.get("addressId").toString());
            }

            Delivery delivery = deliveryService.createDeliveryForOrder(orderId, userId, deliveryMethod, addressId);
            DeliveryDto deliveryDto = deliveryService.convertToDto(delivery);
            return ResponseEntity.status(HttpStatus.CREATED).body(deliveryDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create delivery"));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateDeliveryStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String statusStr = request.get("status");
            DeliveryStatus status = DeliveryStatus.valueOf(statusStr.toUpperCase());
            
            Delivery updatedDelivery = deliveryService.updateDeliveryStatus(id, status);
            DeliveryDto deliveryDto = deliveryService.convertToDto(updatedDelivery);
            return ResponseEntity.ok(deliveryDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update delivery status"));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/courier")
    public ResponseEntity<?> updateCourierInfo(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String courierName = request.get("courierName");
            
            Delivery updatedDelivery = deliveryService.updateCourierInfo(id, courierName);
            DeliveryDto deliveryDto = deliveryService.convertToDto(updatedDelivery);
            return ResponseEntity.ok(deliveryDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update courier information"));
        }
    }

    @GetMapping("/{id}/ready-for-collection")
    public ResponseEntity<Boolean> isReadyForCollection(@PathVariable Long id) {
        try {
            boolean ready = deliveryService.isReadyForCollection(id);
            return ResponseEntity.ok(ready);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/completed")
    public ResponseEntity<Boolean> isDeliveryCompleted(@PathVariable Long id) {
        try {
            boolean completed = deliveryService.isDeliveryCompleted(id);
            return ResponseEntity.ok(completed);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/methods")
    public ResponseEntity<DeliveryMethod[]> getDeliveryMethods() {
        return ResponseEntity.ok(deliveryService.getAvailableDeliveryMethods());
    }

    @GetMapping("/statuses")
    public ResponseEntity<DeliveryStatus[]> getDeliveryStatuses() {
        return ResponseEntity.ok(deliveryService.getAvailableDeliveryStatuses());
    }
}